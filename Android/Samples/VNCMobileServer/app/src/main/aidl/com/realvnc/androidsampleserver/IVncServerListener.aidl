/* Copyright (C) 2002-2018 RealVNC Ltd. All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of the
 * VNC Mobile Solution SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.androidsampleserver;

/** Interface for callbacks from the VNC server to the user interface layer.
 * This interface should be implemented by the user interface program
 * (activity) which wishes to make use of the VNC server. It will need
 * to know them in order to report progress to the user, but also so
 * that it knows what calls it can validly make into the VNC server at
 * any given time.
 */
interface IVncServerListener {
    /** The server is now listening on the given selection of IP
     * addresses. Note that the IP address list may be blank, or may
     * just contain a port number. */
    void listeningCb(String ipAddresses);

    /** The server is attempting to establish a connection */
    void connectingCb();

    /** The server has now connected to the given IP address. Note
     * that the IP address may be blank. */
    void connectedCb(String ipAddress);

    /** All negotiation and authentication has succeeded, and the VNC
     * connection is now up and running. */
    void runningCb();

    /** The server is no longer connected. */
    void disconnectedCb();

    /** The server has experienced some sort of error condition. */
    void errorCb(int errorCode);

    /** Key generation is complete */
    void keygenCb(in byte[] keyPair);

    /** Viewer has provided a username and password */
    void authCb(String username, String password);

    /** Viewer has requested a username and/or password */
    void loginCb(boolean usernameReq, boolean passwordReq);

    /** The server state hasn't changed, but the UI needs updating for
     * some other reason.
     *
     * Used when receiving a HTTP trigger, so we can display the
     * "Please pull down the notification bar" message. */
    void updateUiCb();
}
