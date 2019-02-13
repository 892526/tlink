//
//  VNCUSBThread.m
//  iOS Server SDK
//
//  Copyright RealVNC Ltd. 2014-2018. All rights reserved.
//

#import "VNCUSBCondition.h"
#import "VNCUSBThread.h"

@implementation VNCUSBThread

-(id) initWithStream:(NSStream *)stream
	withCondition:(VNCUSBCondition*)condition {
	if (self = [super init]) {
		m_stream = [stream retain];
		m_condition = [condition retain];
		m_thread = [[NSThread alloc] initWithTarget:self
			selector:@selector(threadLoop:) object:nil];
		[m_thread start];
	}
	return self;
}

-(void) dealloc {
	[m_thread release];
	[m_condition release];
	[m_stream release];
	[super dealloc];
}

-(void) cancel {
	[m_thread cancel];
	[m_stream close];
}

-(void) threadLoop:(id)object {
	@autoreleasepool {
		[m_stream setDelegate:self];
		[m_stream scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
		[m_stream open];
		[m_condition activate];
		
		@try {
			do {
				[[NSRunLoop currentRunLoop] runUntilDate:[NSDate dateWithTimeIntervalSinceNow:0.2]];
			} while (![m_thread isCancelled]);
		} @finally {
			[m_stream close];
		}
	}
}

-(void) stream:(NSStream *)aStream handleEvent:(NSStreamEvent)eventCode {
	(void) eventCode;
	@synchronized(aStream) {
		[m_condition activate];
	}
}

@end

