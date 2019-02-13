//
//  VNCUSBBearer.m
//  iOS Server SDK
//
//  Copyright (C) 2011-2018 VNC Automotive Ltd.  All Rights Reserved.
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


