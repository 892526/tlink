/* Copyright (C) 2011-2018 VNC Automotive Ltd.  All Rights Reserved. */

#ifndef IOSSERVERSDK_VNCPUBLICAPIDEVICEKEYRESPONDER_H
#define IOSSERVERSDK_VNCPUBLICAPIDEVICEKEYRESPONDER_H

/**
 * \file VNCPublicAPIDeviceKeyResponder.h
 * 
 * \brief VNC Automotive Public API Device Key Responder API
 *
 * The protocol documented here makes it possible to implement
 * public API device key injection for custom controls.
 * 
 * This behaviour can be added to existing control classes
 * using Objective-C categories. For example:
 *
 * \code
 * @implementation YourCustomControlClass (VNCPublicAPIDeviceKeyResponderCategory)
 * // Your implementation...
 * @end
 * \endcode
 *
 * \see VNCDeviceKey
 */

/**
 * \brief A VNC Automotive Public API device key responder.
 * 
 * Implement this for a responder to handle VNC Automotive device key events.
 * The VNC Automotive Public API device key injector will search for the
 * first responder (see UIResponder), and call this method on that
 * responder.
 */
@protocol VNCPublicAPIDeviceKeyResponder

/**
 * \brief Handle a VNC Automotive device key.
 *
 * VNC Automotive device keys can be either 'raw' or abstractions, the former
 * being device key codes that are specific to the device, while
 * the latter are values in the VNCDeviceKey enum that are applicable
 * to most devices.
 * 
 * \param deviceKey The device key.
 * \param isDown Whether the device key is being pressed down.
 * \param isRaw Whether the device key is a raw keycode (device specific),
 *              or whether it is an abstraction (i.e. one of VNCDeviceKey).
 *
 * \see VNCDeviceKey
 */
-(void) handleVNCDeviceKey:(int)deviceKey isDown:(BOOL)isDown isRaw:(BOOL)isRaw;

@end

#endif

