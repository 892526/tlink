//
//  VNCBearerWrapper.m
//
//  Copyright RealVNC Ltd. 2011-2018. All rights reserved.
//

#import "VNCBearerSupportingAPI.h"
#import "VNCBearerWrapper.h"
#import "VNCConnectionWrapper.h"

@implementation VNCBearerWrapper {
    /**
     * Pointer to the data of the standard bearer.
     */
    VNCBearer * m_bearer;
	
    /**
     * Pointer to the functions of the standard bearer.
     */
    VNCBearerInterface m_interface;
}

-(id) initWithBearerName:(NSString *)name withInitializer:(VNCBearerInitializeType *)initializer {
	if (self = [super init]) {
		assert(name != nil);
		memset(&m_interface, 0, sizeof(m_interface));
		
		VNCBearerSupportingAPI supportingAPI;
		VNCServerPopulateBearerSupportingAPI(&supportingAPI);
		
		m_bearer = initializer([name UTF8String],
			NULL, &m_interface, sizeof(m_interface),
			&supportingAPI, sizeof(supportingAPI));
		assert(m_bearer != NULL);
	}
	return self;
}

-(void) dealloc {
	(*m_interface.vncBearerTerminate)(m_bearer);
	[super dealloc];
}

-(NSString *) getProperty:(VNCBearerProperty)property {
	char * valueData = (*m_interface.vncBearerGetProperty)(m_bearer, property);
	@try {
		if(valueData == NULL){
			return nil;
		}
		
		return [NSString stringWithCString:valueData encoding:NSASCIIStringEncoding];
	} @finally {
		VNCServerBearer_Free(valueData);
	}
}

-(NSString *) name{
	return [self getProperty:VNCBearerPropertyName];
}

-(NSString *) fullName{
	return [self getProperty:VNCBearerPropertyFullName];
}

-(NSString *) description{
	return [self getProperty:VNCBearerPropertyDescription];
}

-(NSString *) version{
	return [self getProperty:VNCBearerPropertyVersion];
}

-(id<VNCBearerConnection>) newConnectionWithContext:(id<VNCBearerConnectionContext>)context {
	VNCConnectionWrapper* connectionWrapper =
		[[[VNCConnectionWrapper alloc] initWithBearer:self withInterface:m_interface withContext:context] autorelease];
	
	VNCConnection * connection = NULL;
	const VNCBearerError error = (*m_interface.vncBearerCreateConnection)(
		m_bearer, connectionWrapper, &connection);
	
	if (error != VNCBearerErrorNone) {
		NSLog(@"Bearer connection creation encountered error %d.", error);
		connectionWrapper.error = error;
		return [connectionWrapper retain];
	}
	
	if (connection == NULL) {
		NSLog(@"Bearer connection creation indicated success "
			"but returned NULL connection; returning InvalidParameter error.");
		connectionWrapper.error = VNCBearerErrorInvalidParameter;
		return [connectionWrapper retain];
	}
	
	assert(connection != NULL);
	
	connectionWrapper.connection = connection;
	[connectionWrapper establish];
	
	return [connectionWrapper retain];
}

@end


