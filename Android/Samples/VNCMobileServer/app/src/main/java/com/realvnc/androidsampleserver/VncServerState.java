/* Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of a
 * VNC Automotive SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.androidsampleserver;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class represents the current state of the VNC Automotive server.
 * 
 * <p>This class exists because
 * the clients of the VNC Automotive server are, on Android, Activities ({@link Activity}). They may
 * be created and destroyed at any time, so they can't rely on remembering the state of the
 * server as communicated to its clients using the callbacks in {@link IVncServerListener}.
 * The {@link Activity} may be started at any point and need to display status to the user
 * about what the VNC Automotive server is doing. To do that, it can call {@link IVncServerInterface#VNCServerStateGetState()}
 * to retrieve one of these state objects, which contains all the information it would otherwise
 * have received using the callbacks.</p>
 * 
 * <p>Note that this class is passed by value to clients of the VNC Automotive server. They should not
 * retain a reference to it and expect that reference to change. Instead, each time they
 * want to discover the state of the VNC Automotive server they should make a fresh call to 
 * {@link IVncServerInterface#VNCServerStateGetState()}.</p>
 * @author aat
 *
 */
public class VncServerState implements Parcelable {
	
    private VncServerMainState state;
    private VncServerAPICalledState apiCalled;
    private String connectedAddress;
    private String listeningAddress;
    private int errorCode;
    private boolean mIsRunning;
    private boolean mRequestingDialog;

    // state = AUTHENTICATING:
    private String username, password;

    // state = REQUESTING_AUTH:
    private boolean usernameReq, passwordReq;

    /**
     * Get the main state of the VNC Automotive server. This is as per the documentation
     * for libvncserver.
     */
    public VncServerMainState getState() {
        return state;
    }
	
    /**
     * Return whether any API was called. Returns null if no API call is currently
     * in progress.
     */
    public VncServerAPICalledState getApiCalled() {
        return apiCalled;
    }
	
    public enum VncServerMainState {
        DISCONNECTED,
            KEYGEN,
            LISTENING,
            CONNECTING,
            ACCEPTING,
            AUTHENTICATING,
            REQUESTING_AUTH,
            RUNNING,
            ERROR
            }
	
    public enum VncServerAPICalledState {
        KEYGEN,
            LISTEN,
            CONNECT,
            DENY_CONN,
            ACCEPT_CONN,
            ACCEPT_AUTH,
            SUPPLY_AUTH,
            DENY_AUTH,
            RESET
            }

    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeInt(0); // version number
        arg0.writeInt(state.ordinal());
        arg0.writeInt(apiCalled == null ? 0 : 1);
        if (apiCalled != null)
            arg0.writeInt(apiCalled.ordinal());
        arg0.writeString(connectedAddress);
        arg0.writeString(listeningAddress);
    }

    public static final VncServerState.Creator<VncServerState> CREATOR = new Parcelable.Creator<VncServerState>() {
        public VncServerState createFromParcel(Parcel in) {
            return new VncServerState(in);
        }
		
        public VncServerState[] newArray(int size) {
            return new VncServerState[size];
        }
    };

    private VncServerState(Parcel in) {
        int versionNumber = in.readInt();
        if (versionNumber != 0)
            throw new RuntimeException("Incompatible VNC Automotive server states");
        state = VncServerMainState.values()[in.readInt()];
        boolean apiIsCalled = (in.readInt() == 1);
        if (apiIsCalled)
            apiCalled = VncServerAPICalledState.values()[in.readInt()];
        connectedAddress = in.readString();
        listeningAddress = in.readString();
    }

    public VncServerState(VncServerMainState state, String connectedAddress, String listeningAddress) {
        this.state = state;
        this.connectedAddress = connectedAddress;
        this.listeningAddress = listeningAddress;
        this.mIsRunning = (state == VncServerMainState.RUNNING);
    }
	
    public VncServerState(VncServerMainState state) {
        this(state, null, null);
    }

    public VncServerState(VncServerMainState state, int error) {
        this(state, null, null);
        errorCode = error;
    }
	
    public void setApiCalled(VncServerAPICalledState apiCalled) {
        this.apiCalled = apiCalled;
    }

    public int describeContents() {
        return 0;
    }

    public void setIsRunning(boolean isRunning) {
        mIsRunning = isRunning;
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    public void setRequestingDialog(boolean requestingDialog) {
        mRequestingDialog = requestingDialog;
    }

    public boolean requestingDialog() {
        return mRequestingDialog;
    }

    /**
     * Return a string containing the addresses on which we're listening, or null.
     */
    public String getListeningAddress() {
        return listeningAddress;
    }
	
    /**
     * Return a string containing the address to which we're connected, or null.
     */
    public String getConnectedAddress() {
        return connectedAddress;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setUserPass(String user, String pass) {
        username = user;
        password = pass;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }

    public void setUserPassReq(boolean userReq, boolean passReq) {
        usernameReq = userReq;
        passwordReq = passReq;
    }

    public boolean getUsernameReq() { return usernameReq; }
    public boolean getPasswordReq() { return passwordReq; }
}
