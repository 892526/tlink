//
//  VNCUSBThread.h
//  iOS Server SDK
//
//  Copyright (C) 2014-2018 VNC Automotive Ltd.  All Rights Reserved.
//

#import "VNCUSBCondition.h"

@interface VNCUSBThread: NSObject <NSStreamDelegate> {
	NSStream * m_stream;
	VNCUSBCondition* m_condition;
	NSThread * m_thread;
}

-(id) initWithStream:(NSStream *)stream
	withCondition:(VNCUSBCondition*)condition;
-(void) dealloc;

-(void) cancel;

@end
