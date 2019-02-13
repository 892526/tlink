/* Copyright RealVNC Ltd. 2011-2018. All rights reserved. */

#ifndef IOSSERVERSDK_VNCPUBLICAPIKEYRESPONDER_H
#define IOSSERVERSDK_VNCPUBLICAPIKEYRESPONDER_H

/**
 * \file VNCPublicAPIKeyResponder.h
 * 
 * \brief VNC Public API Key Responder API
 *
 * The protocol documented here makes it possible to
 * implement public API key injection for custom controls.
 * 
 * This behaviour can be added to existing control classes
 * using Objective-C categories. For example:
 *
 * \code
 * @implementation YourCustomControlClass (VNCPublicAPIKeyResponderCategory)
 * // Your implementation...
 * @end
 * \endcode
 */

/**
 * \brief A VNC Public API key responder.
 * 
 * Implement this for a responder to handle VNC key events.
 * The RealVNC Public API key injector will search for the
 * first responder (see UIResponder), and call this method on
 * that responder.
 */
@protocol VNCPublicAPIKeyResponder

/**
 * \brief Handle a VNC key.
 * 
 * \param key The X11 key sym that should be handled.
 * \param isDown Whether the key is being pressed down.
 * 
 * \see VNCServer::convertXKeySymToUnicode:
 */
-(void) handleVNCKey:(uint32_t)key isDown:(BOOL)isDown;

@end

#endif
