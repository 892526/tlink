//
//  VNCUSBBearer.m
//  iOS Server SDK
//
//  Copyright RealVNC Ltd. 2011-2018. All rights reserved.
//

#import "VNCUSBBearer.h"
#import "VNCUSBConnection.h"

@implementation VNCUSBBearer

-(NSString *) name {
    return @"USB";
}

-(NSString *) fullName {
    return @"iAP USB Bearer";
}

-(NSString *) description {
    return @"Provides communication over USB via iAP, for iOS devices.";
}

-(NSString *) version {
    return @"<development>";
}

-(id<VNCBearerConnection>) newConnectionWithContext:(id<VNCBearerConnectionContext>)context {
    return [[VNCUSBConnection alloc] initWithContext:context];
}

@end


