Samsung Remote Control Service for Android
==========================================

This library project contains a Remote Control Service suitable for
use on Samsung Android devices running Android 4.2 or higher. It uses
the Samsung KNOX SDK to perform remote control.

However, some devices running 4.2 have issues in their KNOX implementations,
so you are advised to test carefully on all your target devices if you wish
to use this Remote Control Service on that Android version.

To make use of this library project it must be included in the same
application as the server SDK. It will only be usable when connected
to a compatible viewer. The server application must obtain appropriate
device admin authorization before this Remote Control Service can be
used; see the sample server for details.

Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
