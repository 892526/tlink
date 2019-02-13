//
//  VNCConnectionWrapper.m
//
//  Copyright RealVNC Ltd. 2011-2018. All rights reserved.
//

#import "VNCBearerSupportingAPI.h"
#import "VNCConnectionWrapper.h"

// NSTimer has a strong reference to the object it
// calls, but we need a weak reference to prevent any
// reference cycles. Hence a proxy object is an easy
// way to turn the strong reference into a weak reference.
@interface VNCConnectionTimerProxy : NSObject {
	VNCConnectionWrapper* m_connectionWrapper;
}

@property (assign, nonatomic) VNCConnectionWrapper* connectionWrapper;

-(id) init;

-(void) timerExpired;

@end

@implementation VNCConnectionTimerProxy

@synthesize connectionWrapper = m_connectionWrapper;

-(id) init {
	if (self = [super init]) {
		m_connectionWrapper = nil;
	}
	return self;
}

-(void) timerExpired {
	[self.connectionWrapper timerExpired];
}

@end

@implementation VNCConnectionWrapper {
	// The bearer wrapper (retain this to ensure the bearer
	// isn't destroyed prior to the connection being destroyed).
	VNCBearerWrapper* m_bearerWrapper;
	
	VNCConnection * m_connection;
	id<VNCBearerConnectionContext> m_context;
	VNCBearerInterface m_interface;
	VNCBearerError m_error;
	
	// Some bearers require an initial read of more than
	// bytes for proper behaviour (e.g. to determine whether
	// the bearer is connected), which may not be requested
	// by the server, so must be done here.
	BOOL m_hasReadFirstByte, m_hasConsumedFirstByte;
	uint8_t m_firstByte;
	
	// Timer proxy (see above).
	VNCConnectionTimerProxy* m_timerProxy;
	
	// Timer for use by bearer connection.
	NSTimer* m_timer;
}

@synthesize connection = m_connection;
@synthesize context = m_context;
@synthesize error = m_error;

-(id) initWithBearer:(VNCBearerWrapper*)bearerWrapper
	withInterface:(VNCBearerInterface)interface
	withContext:(id<VNCBearerConnectionContext>)context {
	if (self = [super init]) {
		assert(bearerWrapper != NULL);
		
		m_bearerWrapper = [bearerWrapper retain];
		m_connection = NULL;
		m_context = [context retain];
		m_interface = interface;
		m_error = VNCBearerErrorNone;
		
		m_hasReadFirstByte = m_hasConsumedFirstByte = NO;
		m_firstByte = 0x00;
		
		m_timerProxy = [[VNCConnectionTimerProxy alloc] init];
		m_timerProxy.connectionWrapper = self;
		m_timer = nil;
	}
	return self;
}

-(void) dealloc {
	NSLog(@"Destroying bearer connection wrapper.");
	
	if (m_connection != NULL) {
		(*m_interface.vncConnectionDestroy)(m_connection);
	}
	
	[m_timer invalidate];
	[m_timer release];
	[m_timerProxy release];
	[m_context release];
	[m_bearerWrapper release];
	
	[super dealloc];
}

-(void) establish {
	assert(m_connection != NULL);
	const VNCBearerError error = (*m_interface.vncConnectionEstablish)(m_connection);
	if (error != VNCBearerErrorNone) {
		if (m_error == VNCBearerErrorNone) m_error = error;
		NSLog(@"Problem establishing bearer connection: error %d.", error);
	}
}

-(void) setTimer:(NSTimeInterval)timeInterval {
	// Cancel any previous timer.
	[self cancelTimer];
	
	m_timer = [[NSTimer scheduledTimerWithTimeInterval:timeInterval
		target:m_timerProxy selector:@selector(timerExpired) userInfo:nil repeats:NO] retain];
}

-(void) cancelTimer {
	[m_timer invalidate];
	[m_timer release];
	m_timer = nil;
}

-(void) timerExpired {
	if (m_connection == NULL) return;
	
	const VNCBearerError error = (*m_interface.vncConnectionTimerExpired)(m_connection);
	if (error != VNCBearerErrorNone) {
		if (m_error == VNCBearerErrorNone) m_error = error;
		NSLog(@"vncConnectionTimerExpired returned error %d.", error);
	}
}

-(NSString *) getProperty:(VNCConnectionProperty)property {
	if (m_connection == NULL || m_error != VNCBearerErrorNone) return nil;
	
	NSLog(@"Getting property: %d.", property);
	char * valueData = (*m_interface.vncConnectionGetProperty)(m_connection, property);
	if (valueData == NULL) return nil;
	
	@try {
		NSLog(@"Got property(%d) value: %s.", property, valueData);
		return [NSString stringWithCString:valueData encoding:NSASCIIStringEncoding];
	} @finally {
		VNCServerBearer_Free(valueData);
	}
}

-(NSString *) listeningInfo {
	return [self getProperty:VNCConnectionPropertyListeningInformation];
}

-(NSString *) localEndpoint {
	return [self getProperty:VNCConnectionPropertyLocalAddress];
}

-(NSString *) remoteEndpoint {
	return [self getProperty:VNCConnectionPropertyPeerAddress];
}

-(VNCConnectionEventHandle) eventHandle {
	if (m_connection == NULL) return VNC_INVALID_EVENT_HANDLE;
	
	VNCConnectionEventHandle connectionHandle = VNC_INVALID_EVENT_HANDLE;
	const VNCBearerError error = (*m_interface.vncConnectionGetEventHandle)(m_connection, &connectionHandle, NULL);
	
	if (error != VNCBearerErrorNone) {
		if (m_error == VNCBearerErrorNone) m_error = error;
		NSLog(@"vncConnectionGetEventHandle returned error %d.", error);
		return VNC_INVALID_EVENT_HANDLE;
	}
	
	return connectionHandle;
}

-(VNCConnectionActivity) activity {
	if (m_connection == NULL) return VNCConnectionActivityNone;
	
	VNCConnectionActivity activity = VNCConnectionActivityNone;
	const VNCBearerError error = (*m_interface.vncConnectionGetActivity)(m_connection, &activity);
	
	if (error != VNCBearerErrorNone) {
		if (m_error == VNCBearerErrorNone) m_error = error;
		NSLog(@"vncConnectionGetActivity returned error %d.", error);
		return VNCConnectionActivityNone;
	}
	
	return activity;
}

-(VNCBearerError) error{
	return m_error;
}

-(NSUInteger) doRead:(uint8_t *)buffer maxLength:(NSUInteger)length {
	assert(m_connection != NULL);
	
	// Bearers can't always handle zero length.
	if (length == 0) return 0;
	
	size_t readSize = length;
	
	const VNCBearerError error = (*m_interface.vncConnectionRead)(m_connection,
		(unsigned char*) buffer, &readSize);
	
	if (error != VNCBearerErrorNone) {
		if (m_error == VNCBearerErrorNone) m_error = error;
		NSLog(@"Problem reading from bearer: error %d.", error);
		return 0;
	}
	
	return (NSUInteger) readSize;
}

-(NSUInteger) read:(uint8_t *)buffer maxLength:(NSUInteger)length {
	if (m_connection == NULL) return 0;
	
	// For the benefit of the bearers, even if a
	// read of zero bytes is requested, make sure
	// the first read is for one byte.
	if (!m_hasReadFirstByte) {
		const NSUInteger readSize = [self doRead:&m_firstByte maxLength:1];
		assert(readSize == 0 || readSize == 1);
		if (readSize == 0) {
			return 0;
		}
		m_hasReadFirstByte = YES;
	}
	
	assert(m_hasReadFirstByte);
	
	if (!m_hasConsumedFirstByte) {
		if (length == 0) return 0;
		buffer[0] = m_firstByte;
		m_hasConsumedFirstByte = YES;
		return 1;
	}
	
	assert(m_hasReadFirstByte && m_hasConsumedFirstByte);
	
	return [self doRead:buffer maxLength:length];
}

-(NSUInteger) write:(const uint8_t *)buffer maxLength:(NSUInteger)length {
	if (m_connection == NULL) return 0;
	
	// Bearers can't always handle zero length.
	if (length == 0) return 0;
	
	size_t writeSize = length;
	
	const VNCBearerError error = (*m_interface.vncConnectionWrite)(m_connection,
		(const unsigned char *) buffer, &writeSize);
	
	if (error != VNCBearerErrorNone) {
		if (m_error == VNCBearerErrorNone) m_error = error;
		NSLog(@"Problem writing to bearer: error %d.", error);
		return 0;
	}
	
	return (NSUInteger) writeSize;
}

@end
