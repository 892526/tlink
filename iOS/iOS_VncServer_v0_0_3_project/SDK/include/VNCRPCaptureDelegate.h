#ifndef VNCRPCaptureDelegate_h
#define VNCRPCaptureDelegate_h

/**
 * \file VNCRPCaptureDelegate.h
 *
 * \brief Interface for screen capture
 *
 * Copyright RealVNC Ltd. 2018. All rights reserved.
 */

/**
 * Protocol to be implemented by classes which can capture screenshots.
 */
#import <ReplayKit/ReplayKit.h>

@protocol VNCRPCaptureDelegate < NSObject >

/**
 * Asks whether the screen has changed, so that the server
 * knows whether it's worthwhile to call 'captureScreen'.
 *
 * \return Whether the screen has changed.
 */
@required
-(BOOL)    hasScreenChanged;

/**
 * Capture the screen to an image. The delegate implementation should
 * return the most recent CMSampleBuffer received from the OS.
 *
 * \return The buffer, which should be autoreleased (must NOT
 *         be nil).
 */
@required
-(CMSampleBufferRef) captureScreen;

@optional
-(void) captureFinished;

@end
#endif /* VNCRPCaptureDelegate_h */
