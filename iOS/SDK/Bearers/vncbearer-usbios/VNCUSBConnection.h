//
//  VNCUSBConnection.h
//  iOS Server SDK
//
//  Copyright RealVNC Ltd. 2011-2018. All rights reserved.
//

#import <ExternalAccessory/ExternalAccessory.h>

#import "VNCBearerConnection.h"
#import "VNCBearerConnectionContext.h"

@interface VNCUSBConnection : NSObject < VNCBearerConnection >

-(id) initWithContext:(id<VNCBearerConnectionContext>)context;

@end


