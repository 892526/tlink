//
//  VNCUSBConnection.m
//  iOS Server SDK
//
//  Copyright (C) 2011-2018 VNC Automotive Ltd.  All Rights Reserved.
//

#import <ExternalAccessory/ExternalAccessory.h>

#import "VNCBearerConnection.h"
#import "VNCBearerConnectionContext.h"
#import "VNCUSBConnection.h"
#import "VNCUSBThread.h"

static EAAccessory * getConnectedAccessory(NSString * protocol) {
	EAAccessoryManager * manager = [EAAccessoryManager sharedAccessoryManager];
	
	NSArray * accessories = [manager connectedAccessories];
	
	for(EAAccessory * accessory in accessories){
		if([[accessory protocolStrings] containsObject:protocol]){
			return accessory;
		}
	}
	
	return nil;
}

@implementation VNCUSBConnection {
	VNCBearerError m_error;
	EASession * m_session;
	id<VNCBearerConnectionContext> m_context;
	VNCUSBCondition* m_condition;
	VNCUSBThread* m_readNotifyThread;
	VNCUSBThread* m_writeNotifyThread;
}

-(id) initWithContext:(id<VNCBearerConnectionContext>)context {
	if(self = [super init]){
		assert(context != nil);
		m_context = context;
		m_error = VNCBearerErrorNone;
		m_session = nil;
		m_condition = [[VNCUSBCondition alloc] init];
		m_readNotifyThread = nil;
		m_writeNotifyThread = nil;
		
		// Notify any initial events.
		[m_condition activate];
		
		[m_context connectionStatusChange:VNCConnectionStatusConnecting];

		NSString * protocol = [m_context getCommandStringField:@"p"];
		if (protocol == nil)
		{
			protocol = @"com.realvnc.sample.rfb4";
		}
		EAAccessory * accessory = getConnectedAccessory(protocol);
		
		if(accessory == nil){
			NSLog(@"No compatible USB accessory is connected.");
			m_error = VNCBearerErrorUSBNotConnected;
			[m_context connectionStatusChange:VNCConnectionStatusNone];
			return self;
		}
		
		m_session = [[EASession alloc] initWithAccessory:accessory forProtocol:protocol];
		
		if(m_session != nil){
			m_readNotifyThread = [[VNCUSBThread alloc] initWithStream:m_session.inputStream
						withCondition:m_condition];
			m_writeNotifyThread = [[VNCUSBThread alloc] initWithStream:m_session.outputStream
						withCondition:m_condition];
			// FIXME: Just making sure the streams are open before we changes status to connected
			sleep(1);
			[m_context connectionStatusChange:VNCConnectionStatusConnected];
		}else{
			m_error = VNCBearerErrorUSBNotConnected;
			[m_context connectionStatusChange:VNCConnectionStatusNone];
		}
	}
	return self;
}

-(void) dealloc {
	[m_writeNotifyThread cancel];
	[m_readNotifyThread cancel];
	
	[m_writeNotifyThread release];
	[m_readNotifyThread release];
	[m_condition release];
	[m_session release];
	[super dealloc];
}

-(NSString *) listeningInfo {
	return nil;
}

-(NSString *) localEndpoint {
	return nil;
}

-(NSString *) remoteEndpoint {
	return nil;
}

-(VNCConnectionEventHandle) eventHandle {
	return [m_condition eventHandle];
}

-(VNCConnectionActivity) activity {
	@synchronized(self) {
		switch (m_session.inputStream.streamStatus) {
			case NSStreamStatusAtEnd:
			case NSStreamStatusClosed:
			case NSStreamStatusError:
				// The connection has been unexpectedly closed, for example
				// if the USB cable was disconnected, so notify the SDK.
				m_error = VNCBearerErrorDisconnected;
				[m_context connectionStatusChange:VNCConnectionStatusNone];
				break;
			default:
				// No errors so continue.
				break;
		}

		VNCConnectionActivity activity = VNCConnectionActivityNone;
		if ([m_session.inputStream hasBytesAvailable]) {
			activity |= VNCConnectionActivityReadReady;
		}
		if ([m_session.outputStream hasSpaceAvailable]) {
			activity |= VNCConnectionActivityWriteReady;
		}
		return activity;
	}
}

-(VNCBearerError) error {
	@synchronized(self) {
		return m_error;
	}
}

-(void) resetCondition {
	//if (![m_session.inputStream hasBytesAvailable] && ![m_session.outputStream hasSpaceAvailable]) {
		[m_condition reset];
	//}
}

-(NSUInteger) read:(uint8_t *)buffer maxLength:(NSUInteger)length {
	@synchronized(self) {
		if (![m_session.inputStream hasBytesAvailable]) {
			[self resetCondition];
			return 0;
		}
		
		if (length == 0) {
			return 0;
		}
		
		const NSInteger result = [m_session.inputStream read:buffer maxLength:length];
		if (result > 0) {
			return (NSUInteger) result;
		} else if (result == 0) {
			m_error = VNCBearerErrorDisconnected;
			[m_context connectionStatusChange:VNCConnectionStatusNone];
			return 0;
		} else {
			m_error = VNCBearerErrorFailed;
			[m_context connectionStatusChange:VNCConnectionStatusNone];
			return 0;
		}
	}
}

-(NSUInteger) write:(const uint8_t *)buffer maxLength:(NSUInteger)length {
	@synchronized(self) {
		if (![m_session.outputStream hasSpaceAvailable]) {
			[self resetCondition];
			return 0;
		}
		
		if (length == 0) {
			return 0;
		}
		
		const NSInteger result = [m_session.outputStream write:buffer maxLength:length];
		if (result > 0) {
			return (NSUInteger) result;
		} else if (result == 0) {
			m_error = VNCBearerErrorDisconnected;
			[m_context connectionStatusChange:VNCConnectionStatusNone];
			return 0;
		} else {
			m_error = VNCBearerErrorFailed;
			[m_context connectionStatusChange:VNCConnectionStatusNone];
			return 0;
		}
	}
}

@end


