//
//  VNCUSBCondition.h
//  iOS Server SDK
//
//  Copyright (C) 2013-2018 VNC Automotive Ltd.  All Rights Reserved.
//

#import <vncbearer.h>

@interface VNCUSBCondition : NSObject

-(id) init;

-(BOOL) isActive;

-(void) activate;

-(void) reset;

-(VNCConnectionEventHandle) eventHandle;

@end

