VNC Automotive Server SDK for Android
====================================

In this directory you will find seven sub-directories:

   EmbeddedRemoteControlService   - This contains an embedded Remote Control
                                    Service suitable for use on Android
                                    devices. Please see the README.txt file for
                                    further information.

   RootedRemoteControlService     - This contains a Remote Control Service
                                    suitable for use on rooted Android devices.
                                    Please see the README.txt file for further
                                    information.

   SamsungRemoteControlService    - This contains a Remote Control Service
                                    suitable for use on Samsung Android
                                    devices. Please see the README.txt file for
                                    further information.

   SDK                            - This contains what you need to build your
                                    own applications embedding an Android VNC Automotive
                                    server.

   VNCNetworkAdvertiserSDK        - This contains the VNC Automotive Network Advertiser
                                    SDK, which you can use to make your VNC Automotive
                                    server application discoverable over IP
                                    networks.

   Samples                        - This contains a sample application which
                                    embeds an Android VNC Automotive server, with full
                                    source code.

   Docs                           - This contains the documentation for the
                                    VNC Automotive Server SDK, the VNC Automotive Network
                                    Advertiser SDK and the Bluetooth Audio
                                    Router in HTML format. Please refer to this
                                    for further information on the SDKs.

Running the VNC Automotive Server Sample Application
=========================================

What you need
-------------
1. Evaluation/Server/Android/Server.apk.
   This is the sample GUI application. It contains the VNC Automotive
   Solution server SDK and a sample GUI.

2. Suitable Android device or emulator.
   The VNC Automotive Android server makes use of a new service called
   the remote control service. This service requires permissions
   which are typically configured as signature permissions. This
   means that any application making use of them must be signed
   with a suitable key.

   Remote control services are provided for a variety of configurations.

3. VNC Automotive viewer.
   Configured as described later.

How to use it
-------------

Refer to Documentation/SampleApplicationsGuide.pdf for installation
and usage instructions.

Q+A
---

Does it use native code or Java code?
    A mixture. Java for the UI layers plus key and pointer handling. Native
    for the connection handling and screen updates, which are the performance-
    critical bits.

Will this work on any Android device?
    Only a device with which have a suitably signed remote control service
    will be usable.

Does it require root access to the device?
    No, although the rooted remote control service makes use of the
    superuser application to control application permissions.

Does it use any internal unpublished APIs?
    Only internally. The API exposed to applications should be suitable to be
    added to Android as an officially supported API.

Integrating the VNC Automotive Server into your application
================================================

See the instructions on the front page of the Javadoc documentation.


Trademark information
=====================

VNC Automotive is a trademark of VNC Automotive Limited and is protected by
trademark registrations and/or pending trademark applications in the
European Union, United States of America and other jurisdictions.
Other trademarks are the property of their respective owners.

Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
