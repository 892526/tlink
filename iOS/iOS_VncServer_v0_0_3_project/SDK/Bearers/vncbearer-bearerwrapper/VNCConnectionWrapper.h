//
//  VNCConnectionWrapper.h
//
//  Copyright RealVNC Ltd. 2011-2018. All rights reserved.
//

#import <VNCBearerConnectionContext.h>
#import "VNCBearerWrapper.h"

@interface VNCConnectionWrapper : NSObject < VNCBearerConnection >

@property (assign, nonatomic) VNCConnection* connection;
@property (readonly, nonatomic) id<VNCBearerConnectionContext> context;
@property (assign, nonatomic) VNCBearerError error;

-(id) initWithBearer:(VNCBearerWrapper*)bearerWrapper
	withInterface:(VNCBearerInterface)interface
	withContext:(id<VNCBearerConnectionContext>)context;

-(void) establish;

-(void) setTimer:(NSTimeInterval)timeInterval;

-(void) cancelTimer;

-(void) timerExpired;

@end
