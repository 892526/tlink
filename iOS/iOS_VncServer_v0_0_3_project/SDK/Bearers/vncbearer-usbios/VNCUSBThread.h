//
//  VNCUSBThread.h
//  iOS Server SDK
//
//  Copyright RealVNC Ltd. 2014-2018. All rights reserved.
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
