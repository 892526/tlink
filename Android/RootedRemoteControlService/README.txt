Rooted Remote Control Service for Android
=========================================

This library project contains a Remote Control Service suitable for
use on rooted Android devices.

To support rooted devices from the server SDK it is
necessary to include the contents of this library project in the same
application as the server SDK.

This can be achieved in one of two ways:

 * Add the path of this directory as a library project by adding a line similar
   to the following to project.properties for your application project:

     android.library.reference.2=../../RootedRemoteControlService

 * Copy the contents of the libs folder in this directory into the
   libs folder for your application which uses the server SDK.
   
Trademark information
---------------------

VNC Automotive is a trademark of VNC Automotive Limited and is protected by
trademark registrations and/or pending trademark applications in the
European Union, United States of America and other jurisdictions.
MirrorLink is a registered trademark of Car Connectivity Consortium LLC.
Other trademarks are the property of their respective owners.

Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
