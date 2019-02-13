//
//  VNCUSBCondition.h
//  iOS Server SDK
//
//  Copyright RealVNC Ltd. 2013-2018. All rights reserved.
//

#import <vncbearer.h>

@interface VNCUSBCondition : NSObject

-(id) init;

-(BOOL) isActive;

-(void) activate;

-(void) reset;

-(VNCConnectionEventHandle) eventHandle;

@end

