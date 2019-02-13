/* Copyright RealVNC Ltd. 2011-2018. All rights reserved. */

#ifndef IOSSERVERSDK_VNCPUBLICAPITOUCHRESPONDER_H
#define IOSSERVERSDK_VNCPUBLICAPITOUCHRESPONDER_H

/**
 * \file VNCPublicAPITouchResponder.h
 * 
 * \brief VNC Public API Touch Responder API
 *
 * The protocols documented here make it possible to
 * implement public API touch injection for custom controls.
 *
 * This behaviour can be added to existing control classes
 * using Objective-C categories. For example:
 *
 * \code
 * @implementation YourCustomControlClass (VNCPublicAPITouchResponderCategory)
 * // Your implementation...
 * @end
 * \endcode
 */

/**
 * \brief A VNC Public API touch responder.
 * 
 * Implement this to become a VNC touch handler and be able to receive
 * touch events. Note that this handler must be returned by an
 * instance of VNCPublicAPITouchResponderNode, that is reachable
 * (i.e. attached to a UIWindow), to be used by the touch injector.
 *
 * The RealVNC public API touch injector will search for the correct touch
 * handler using VNCPublicAPITouchResponderNode::findVNCTouchResponder, 
 * and then, if a responder is found, call:
 *
 * 1) VNCPublicAPITouchResponder::beginVNCTouchSequenceAtPoint: once to
 *    indicate the VNC touch sequence has started.
 * 2) VNCPublicAPITouchResponder::handleVNCTouchAtPoint: for each VNC touch.
 * 3) VNCPublicAPITouchResponder::endVNCTouchSequenceAtPoint: once to
 *    indicate the VNC touch sequence has ended.
 */
@protocol VNCPublicAPITouchResponder < NSObject >

/**
 * \brief Convert screen coordinates to local coordinates.
 * 
 * This method is called to convert screen coordinates to 'local'
 * coordinates, which will be passed to the other methods of
 * VNCPublicAPITouchResponder of this object. You can use this to
 * convert to any coordinate system that is convenient for these methods.
 * 
 * A typical implementation might look like:
 *
 * \code
 * const CGPoint windowCoords = (self.window != nil) ? [self.window convertPoint:point fromWindow:nil] : point;
 * return [self convertPoint:windowCoords fromView:nil];
 * \endcode
 * 
 * \param point A point in screen coordinates.
 * \return The equivalent point in 'local' coordinates.
 */
-(CGPoint) convertVNCTouchToLocalCoords:(CGPoint)point;

/**
 * \brief Start a VNC touch sequence.
 * 
 * This is called at the beginning of a VNC touch sequence. This
 * method should create any necessary state for the VNC touch sequence
 * and return it from this method.
 * 
 * \param point Location of the first touch in the sequence in the
 *              'local' coordinate system (as determined by
 *              VNCPublicAPITouchResponder::convertVNCTouchToLocalCoords:).
 *              VNCPublicAPITouchResponder::handleVNCTouchAtPoint: will
 *              be called immediately after this method with the same point.
 * \param tapCount Number of taps that caused the touch sequence
 *                 (which will be at least one). This is useful, for example,
 *                 to implement 'double click' behaviour (such as zooming in on
 *                 a map).
 * \return A pointer to some data, that will be held by the touch
           injector and passed to all methods in this touch sequence.
 */
-(id) beginVNCTouchSequenceAtPoint:(CGPoint)point withTapCount:(NSUInteger)tapCount;

/**
 * \brief End a VNC touch sequence.
 * 
 * This is called to indicate the end of a VNC touch sequence. This
 * method should destroy the state created in VNCPublicAPITouchResponder::beginVNCTouchSequenceAtPoint:
 * for this VNC touch sequence.
 * 
 * \param point Location of the last touch in the sequence in the
 *              'local' coordinate system (as determined by
 *              VNCPublicAPITouchResponder::convertVNCTouchToLocalCoords:).
 *              VNCPublicAPITouchResponder::handleVNCTouchAtPoint: will have
 *              been called immediately before this method with the same point.
 * \param data User data pointer (that was returned by VNCPublicAPITouchResponder::beginVNCTouchSequenceAtPoint:).
 */
-(void) endVNCTouchSequenceAtPoint:(CGPoint)point withData:(id)data;

/**
 * \brief Handle a VNC touch.
 * 
 * This will be called for every touch in the sequence (including
 * first and last).
 * 
 * \param point Location of the the touch in the 'local' coordinate
 *              system (as determined by VNCPublicAPITouchResponder::convertVNCTouchToLocalCoords:).
 * \param data User data pointer (that was returned by VNCPublicAPITouchResponder::beginVNCTouchSequenceAtPoint:).
 */
-(void) handleVNCTouchAtPoint:(CGPoint)point withData:(id)data;

/**
 * \brief Query whether a VNC touch sequence should be cancelled.
 *
 * This method can be used to query whether a VNC touch sequence should be cancelled.
 * For example, UIScrollView touch handling will call this method before cancelling touch
 * events to determine if it is appropriate to cancel the touch sequence.
 *
 * By default (i.e. when unimplemented), this method returns NO.
 *
 * \param data User data pointer (that was returned by VNCPublicAPITouchResponder::beginVNCTouchSequenceAtPoint:).
 * \return Whether the touch sequence should be cancelled.
 */
@optional
-(BOOL) shouldCancelVNCTouchSequenceWithData:(id)data;

/**
 * \brief Cancel a VNC touch sequence.
 *
 * This is called to indicate a VNC touch sequence should be cancelled. This
 * method should destroy the state created in VNCPublicAPITouchResponder::beginVNCTouchSequenceAtPoint:
 * for this VNC touch sequence.
 * 
 * By default (i.e. when unimplemented), this method calls VNCPublicAPITouchResponder::endVNCTouchSequenceAtPoint:.
 *
 * \param data User data pointer (that was returned by VNCPublicAPITouchResponder::beginVNCTouchSequenceAtPoint:).
 */
@optional
-(void) cancelVNCTouchSequenceWithData:(id)data;

@end

/**
 * \brief A VNC Public API touch responder node.
 * 
 * Implement this to find and return a VNC touch responder - many UI
 * elements implement this and return themselves as the handler.
 */
@protocol VNCPublicAPITouchResponderNode

/**
 * \brief Find a VNC touch responder.
 * 
 * This is called by upper tree elements (or RealVNC Public API Touch
 * Injector directly), to find a VNC touch responder for a VNC touch
 * sequence.
 * 
 * If this method returns nil, the RealVNC touch injector will continue to
 * search for touch responders in views/windows behind this node; to prevent
 * this, return self in this method and provide empty implementations of
 * the methods of VNCPublicAPITouchResponder, such as:
 * 
 * \code
 * -(id) beginVNCTouchSequenceAtPoint:(CGPoint)point withTapCount:(NSUInteger)tapCount {
 *     return nil;
 * }
 * 
 * -(void) endVNCTouchSequenceAtPoint:(CGPoint)point withData:(id)data { }
 * 
 * -(void) handleVNCTouchAtPoint:(CGPoint)point withData:(id)data { }
 * 
 * -(id<VNCPublicAPITouchResponder>) findVNCTouchResponder:(CGPoint)point {
 *     return self;
 * }
 * \endcode
 * 
 * \param point Location of the first VNC touch in screen coordinates.
 * \return A pointer to the responder, if one is found, or nil otherwise.
 */
-(id<VNCPublicAPITouchResponder>) findVNCTouchResponder:(CGPoint)point;

@end

#endif
