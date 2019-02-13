//
//  VNCUSBConnection.h
//  iOS Server SDK
//
//  Copyright (C) 2011-2018 VNC Automotive Ltd.  All Rights Reserved.
//

#import <ExternalAccessory/ExternalAccessory.h>

#import "VNCBearerConnection.h"
#import "VNCBearerConnectionContext.h"

@interface VNCUSBConnection : NSObject < VNCBearerConnection >

-(id) initWithContext:(id<VNCBearerConnectionContext>)context;

@end


