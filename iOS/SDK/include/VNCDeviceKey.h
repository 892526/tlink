/* Copyright (C) 2011-2018 VNC Automotive Ltd.  All Rights Reserved. */

#ifndef IOSSERVERSDK_VNCDEVICEKEY_H
#define IOSSERVERSDK_VNCDEVICEKEY_H

/**
 * \file VNCDeviceKey.h
 * 
 * \brief VNC Automotive Device Keys
 */

/**
 * \brief Abstracted button codes for use with VNCPublicAPIDeviceKeyResponder.
 *
 * These abstractions represent buttons that are present on the majority of
 * mobile devices, and, where they are present, are necessary to perform the
 * majority of remote support tasks.  Buttons that are present only on a
 * minority of devices, or which have functions that can be carried out by
 * other means, are deliberately not included.
 *
 * The number keys in this enumeration represent number buttons on a telephone
 * keypad.  These are distinct from the X key symbols representing numbers on
 * the top row of a US keyboard (e.g. XK_1) and those representing numbers on a
 * PC keyboard's numeric keypad (e.g. XK_KP_1).
 *
 * \see VNCPublicAPIDeviceKeyResponder::handleVNCDeviceKey:isDown:isRaw:
 */
typedef enum
{
    /** A cellphone handset's left soft key. */
    VNCDeviceKeyLeftSoftKey = 0,

    /** A cellphone handset's right soft key. */
    VNCDeviceKeyRightSoftKey = 1,

    /** A cellphone handset's send (make voice call) button. */
    VNCDeviceKeySend = 2,

    /** A cellphone handset's end (hangup) button. */
    VNCDeviceKeyEnd = 3,

    /** A cellphone handset's volume up button. */
    VNCDeviceKeyVolumeUp = '+',

    /** A cellphone handset's volume down button. */
    VNCDeviceKeyVolumeDown = '-',

    /** The 0 key on a cellphone handset's phone keypad. */
    VNCDeviceKeyKeypad0 = '0',

    /** The 1 key on a cellphone handset's phone keypad. */
    VNCDeviceKeyKeypad1 = '1',

    /** The 2 key on a cellphone handset's phone keypad. */
    VNCDeviceKeyKeypad2 = '2',

    /** The 3 key on a cellphone handset's phone keypad. */
    VNCDeviceKeyKeypad3 = '3',

    /** The 4 key on a cellphone handset's phone keypad. */
    VNCDeviceKeyKeypad4 = '4',

    /** The 5 key on a cellphone handset's phone keypad. */
    VNCDeviceKeyKeypad5 = '5',

    /** The 6 key on a cellphone handset's phone keypad. */
    VNCDeviceKeyKeypad6 = '6',

    /** The 7 key on a cellphone handset's phone keypad. */
    VNCDeviceKeyKeypad7 = '7',

    /** The 8 key on a cellphone handset's phone keypad. */
    VNCDeviceKeyKeypad8 = '8',

    /** The 9 key on a cellphone handset's phone keypad. */
    VNCDeviceKeyKeypad9 = '9',

    /** The star key on a cellphone handset's phone keypad. */
    VNCDeviceKeyKeypadStar = '*',

    /** The pound sign (hash) key on a cellphone handset's phone keypad. */
    VNCDeviceKeyKeypadPoundSign = '#',

    /** The left directional button, or a roll of the trackball to the left. */
    VNCDeviceKeyLeft = 'h',

    /** The right directional button, or a roll of the trackball to the right. */
    VNCDeviceKeyRight = 'l',

    /** The up directional button, or a roll of the trackball upwards. */
    VNCDeviceKeyUp = 'k',

    /** The down directional button, or a roll of the trackball downwards. */
    VNCDeviceKeyDown = 'j',

    /** An abstraction for a device button that selects an item from a menu. */
    VNCDeviceKeySelect = 'S',

    /** An abstraction for a device button that cancels an action or navigates backwards. */
    VNCDeviceKeyDismiss = 'D',

    /** An abstraction for a device button that navigates backwards. */
    VNCDeviceKeyBack = 'B',

    /** An abstraction for a device button that returns the device to its homescreen. */
    VNCDeviceKeyHome = 'H',

    /** An abstraction for a device button that opens the device's main menu. */
    VNCDeviceKeyMenu = 'M',

    /** An abstraction for a device button that unlocks a locked phone keypad. */
    VNCDeviceKeyUnlock = 'U',

    /** An abstraction for a device button that changes the text input mode. */
    VNCDeviceKeyEdit = 'E',

    /** A cellphone handset's power button. */
    VNCDeviceKeyPower = 'P',
} VNCDeviceKey;

#endif
