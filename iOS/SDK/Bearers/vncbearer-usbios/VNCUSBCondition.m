//
//  VNCUSBCondition.m
//  iOS Server SDK
//
//  Copyright (C) 2013-2018 VNC Automotive Ltd.  All Rights Reserved.
//

#include <unistd.h>

#import <vncbearer.h>

#import "VNCUSBCondition.h"

@implementation VNCUSBCondition  {
	VNCConnectionEventHandle m_handles[2];
	BOOL m_isActive;
}

-(id) init {
	if (self = [super init]) {
		const int result = pipe(m_handles);
		if (result != 0) {
			[NSException raise:@"Failed to create pipe" format:@"pipe() failed with errno %d", errno];
		}
		assert(result == 0);
		m_isActive = NO;
	}
	return self;
}

-(void) dealloc {
	close(m_handles[0]);
	close(m_handles[1]);
	[super dealloc];
}

-(BOOL) isActive {
	@synchronized (self) {
		return m_isActive;
	}
}

-(void) activate {
	@synchronized (self) {
		if (m_isActive) return;
		m_isActive = YES;
		const uint8_t data[] = { 0xFF };
		const ssize_t result = write(m_handles[1], data, sizeof(data)/sizeof(data[0]));
		if (result != 1) {
			if (result < 0) {
				[NSException raise:@"Failed to write a byte to pipe" format:@"1 byte write() to pipe failed with errno %d", errno];
			} else {
				[NSException raise:@"Failed to write a byte to pipe" format:@"1 byte write() to pipe failed (wrote %llu bytes)", (unsigned long long) result];
			}
		}
		assert(result == 1);
	}
}

-(void) reset {
	@synchronized (self) {
		if (!m_isActive) return;
		m_isActive = NO;
		uint8_t data[1];
		const ssize_t result = read(m_handles[0], data, sizeof(data)/sizeof(data[0]));
		if (result != 1) {
			if (result < 0) {
				[NSException raise:@"Failed to read a byte from pipe" format:@"1 byte read() from pipe failed with errno %d", errno];
			} else {
				[NSException raise:@"Failed to read a byte from pipe" format:@"1 byte read() from pipe failed (read %llu bytes)", (unsigned long long) result];
			}
		}
		assert(result == 1);
	}
}

-(VNCConnectionEventHandle) eventHandle {
	return m_handles[0];
}

@end

