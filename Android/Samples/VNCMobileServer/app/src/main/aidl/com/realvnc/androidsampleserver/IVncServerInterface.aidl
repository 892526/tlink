/* Copyright (C) 2002-2018 RealVNC Ltd. All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of the
 * VNC Mobile Solution SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.androidsampleserver;

import com.realvnc.androidsampleserver.IVncServerListener;
import com.realvnc.androidsampleserver.VncServerState;

/**
 * Interface to the VNC server 'service'. This broadly corresponds to
 * the 'libvncserver' library in other platforms.
 *
 * <p>Clients can call these interfaces by using code such as:
 * <pre>
 * {@link android.content.ContextWrapper}.bindService({@link com.realvnc.vncserver.android.Intents}.CONTROL_VNC_SERVER_INTENT, mServerServiceConnection,
 *				BIND_AUTO_CREATE);
 * </pre>
 * then in the <tt>onServiceConnected</tt> of <tt>mServerServiceConnection</tt>, calling
 * <pre>
 * IVncServerInterface mServerService = IVncServerInterface.Stub.asInterface(service);
 * </pre>
 * </p>
 */
interface IVncServerInterface {

    void registerListener(IVncServerListener listener);
    void unregisterListener(IVncServerListener listener);
	
    /**
     * Connect as described by a command string
     *
     * For a "listen" command, if "autoReListen" is true, then the
     * server will start listening again automatically if the
     * connection breaks. If false, it will return to idle.
     */
    void VNCServerConnect(String command, boolean autoReListen);
	
    /** 
     * Stop listening for incoming VNC connections, and/or disconnect any currently
     * active connection.
     */
    void VNCServerReset();
	
    /**
     * Returns the current state of the VNC server.
     */
    VncServerState VNCServerStateGetState();

    /**
     * Accept or reject username and password provided by the viewer
     */
    void VNCServerAuthenticate(boolean accept);

    /**
     * Provide username and/or password to the viewer
     */
    void VNCServerLogin(String username, String password);

    /**
     * Disconnect the current connection
     * Similar to VNCServerReset(), but returns to listening if
     * appropriate, rather than disconnecting completely.
     */
    void VNCServerDisconnect();

    /**
     * Accept or reject incoming connection attempt
     */
    void VNCServerAccept(boolean accept);

    /**
     * Return the path to the log file
     */
    String VNCServerGetLogPath();

    /**
     * Return the build ID
     */
    String VNCServerGetVersionString();

    /**
     * Tell the VNC server Activity to display a message asking the
     * user to pull down the notification bar.
     *
     * Used by the HTTPTriggerService.
     */
    void VNCServerRequestDialog();

    /**
     * Tell the VNC server Activity to stop displaying a message asking the
     * user to pull down the notification bar.
     *
     * Used by the HTTPTriggerService.
     */
    void VNCServerClearRequestDialog();

    /**
     * Set an error state to be displayed by the UI.
     *
     * Used by the HTTP trigger retrieval service and the service auto
     * installer.
     */
    void VNCServerSetError(int errorCode);

    /**
     * Inform the service that the requested installation has completed.
     */
    void VNCServerInstallationResult(int errorCode);

    /**
     * Load the VNC server licenses.
     */
    void VNCServerLoadLicenses();
}
