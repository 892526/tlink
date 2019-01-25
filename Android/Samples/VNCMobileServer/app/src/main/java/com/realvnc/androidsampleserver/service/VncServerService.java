/* Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of a
 * VNC Automotive SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.androidsampleserver.service;

import android.Manifest.permission;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.jvckenwood.aessampleapp.AesUtility;
import com.jvckenwood.aessampleapp.VncLicenseEncryptionConst;
import com.realvnc.androidsampleserver.AapConnectionManager;
import com.realvnc.androidsampleserver.IVncServerInterface;
import com.realvnc.androidsampleserver.IVncServerListener;
import com.realvnc.androidsampleserver.R;
import com.realvnc.androidsampleserver.SampleIntents;
import com.realvnc.androidsampleserver.ServiceInstaller;
import com.realvnc.androidsampleserver.NotificationHelper;
import com.realvnc.androidsampleserver.VncConfigFile;
import com.realvnc.androidsampleserver.VncServerState;
import com.realvnc.androidsampleserver.VncServerState.VncServerAPICalledState;
import com.realvnc.androidsampleserver.VncServerState.VncServerMainState;
import com.realvnc.androidsampleserver.VncUsbState;
import com.realvnc.androidsampleserver.activity.VNCMobileServer;
import com.realvnc.btaudiorouter.VncBtAudioRouter;
import com.realvnc.h264sampleencoder.VncH264SampleEncoder;
import com.realvnc.jvckenwood.WhiteListManager;
import com.realvnc.jvckenwood.util.StringUtility;
import com.realvnc.networkadvertisersdk.VNCNetworkAdvertiserException;
import com.realvnc.util.VncLog;
import com.realvnc.vncserver.android.VncCommandString;
import com.realvnc.vncserver.android.VncContextInformationManager;
import com.realvnc.vncserver.android.VncContextInformationManager.CapturedContextInformation;
import com.realvnc.vncserver.android.VncExtension;
import com.realvnc.vncserver.android.VncExtensionListener;
import com.realvnc.vncserver.android.VncH264Encoder;
import com.realvnc.vncserver.android.VncOrientationManager;
import com.realvnc.vncserver.android.VncRemoteControlInfo;
import com.realvnc.vncserver.android.VncServer;
import com.realvnc.vncserver.android.VncServerListener;
import com.realvnc.vncserver.core.VncEncryptionType;
import com.realvnc.vncserver.core.VncException;
import com.realvnc.vncserver.core.VncServerCoreErrors;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This service runs a VNC Automotive server in the background within Android.
 * It's a {@link Service} so that it doesn't exit just because the foreground
 * application ("{@link Activity}"), {@link VNCMobileServer},
 * has been sent to the background.
 *
 * <p>This Service returns an {@link IBinder} which can be used
 * as an {@link IVncServerInterface}. That enables callers of this
 * service to interact with the {@link VncServer} class itself which implements
 * an interface much like the libvncserver interface of other platforms.</p>
 *
 */
public class VncServerService extends Service
    implements VncServerListener, AapConnectionManager.Listener, NetworkAdvertiser.Listener {
    private static final String TAG = "VNCService";

    private static final Logger LOG = Logger.getLogger(VncServerService.class.getName());

    /* Name of intent extra used in Android API level 12 and above
     * when an accessory has been granted permission. */
    private static final String EXTRA_PERMISSION_GRANTED = "permission";

    // This permission is only present in Android API level 16 onwards.
    private static final String PERMISSION_READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";

    private static final String KEY_FILE = "VNCMobileServer.key";
    private static final String LOG_FILE = "VNCMobileServerLog.txt";

    private static final String SDCARD_LICENSE_PATH =
        Environment.getExternalStorageDirectory().getPath() +
        "/vnc/serverlicenses";
    private static final String LICENSE_FILE_EXTENSION = ".vnclicense";

    // Assetのライセンスファイル格納フォルダ名
    private static final String ASSET_LICENSE_PATH = "licenses";

    // Assetフォルダ内のライセンスファイルを使用するかどうか（true : Assetフォルダのライセンスを使用する, false : SDカードのライセンスファイル使用する）
    private static final Boolean useLicenseFileInAssetFolder = true;

    private static final String DATA_RELAY_TYPE = "D";
    private static final String SIGNATURE_KEY = "sig";
    private static final int MAXIMUM_SIGNATURE_LENGTH = 32;

    private NotificationManager mNotificationManager;

    private static final int FOREGROUND_SERVICE_NOTIFICATION_ID =
            NotificationHelper.UniqueIdGenerator.generate();

    private static final int STATUS_NOTIFICATION_ID =
            NotificationHelper.UniqueIdGenerator.generate();

    /**
     * The last created server.
     */
    VncServer mServer;
    private final VncH264Encoder mH264Encoder = new VncH264SampleEncoder();

    private AapConnectionManager mAapConnectionManager;
    private BroadcastReceiver mReceiver;

    private int mStartId;
    private HandlerThread mAsyncHandlerThread;
    private Handler mAsyncHandler;

    /**
     * The state of the server.
     */
    private VncServerState mCurrentState = new VncServerState(VncServerMainState.DISCONNECTED);
    private String mConnectedAddress;

    /* Command string we're about to act on. If the server is
     * connected, we reset it before acting on the command; we store
     * it here until the reset completes. */
    private VncCommandString mCmd;

    /* Command string we're currently acting on. */
    private VncCommandString mCurrentCmd;

    /* interlocking to prevent us from trying to handle multiple
     * connectivity changes at once: */
    private boolean mConnectivityChanging = false;

    /* The command that will be acted on when the server goes to Disconnected
     * or error state */
    private String mQueuedCommand = "";
    /* The auto re-listen option that will be used when the server goes to
     * Disconnected or error state */
    private boolean mQueuedAutoReListen = false;

    /* The BT audio extension object. */
    private VncBtAudioRouter mBtAudio;

    /* True if an attempt to install the RCS has occured */
    private boolean mAttemptedRcsInstall;

    /* True if the server has been asked to disconnect by a call to
     * VNCServerDisconnect and has yet to process the disconnectedCb */
    private boolean mDisconnectRequested;

    /* True if the service is in the middle of attempting to install the
     * RCS. This means that further connection attempts should just be
     * queued rather than attempted immediately to prevent multiple RCS
     * installation dialogs. */
    private boolean mTryingToInstallRcs;

    // The network advertiser for this server
    private NetworkAdvertiser mAdvertiser;

    // Whether the landscape orientation lock is currently set
    private boolean mLandscapeLockSet;

    public class VNCBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                Log.i("VNCReceiver", "Detected network connectivity change");

                String listeningType = "";
                try {
                    if (mListeningCommand != null)
                        listeningType =
                            mListeningCommand.getString(VncCommandString.TYPE);
                } catch (VncException e) {
                    LOG.log(Level.SEVERE,
                            "Failed to retrieve type of listening command",
                            e);
                }


                if(!mConnectivityChanging &&
                   (mCurrentState.getState() == VncServerMainState.LISTENING) &&
                   (listeningType.equals("L"))) {
                    mConnectivityChanging = true;
                    // Note that we automatically start listening
                    // again after a disconnection/reset, so this is
                    // sufficient to stop and restart the listener
                    mAsyncHandler.post(new Runnable() {
                            public void run() {
                                mServer.reset();
                            }
                        });
                }

            } else {
                Log.e("VNCReceiver", "Unknown action: " + intent.getAction());
            }
        }

    }

    private interface BroadcastDispatcher {
        void dispatch(IVncServerListener l) throws RemoteException;
    }

    public void listeningCb(VncServer obj, final String ipAddresses) {
        Log.i(TAG, "listeningCb " + ipAddresses);
        mCurrentState = new VncServerState(VncServerMainState.LISTENING,null,ipAddresses);
        if (mAdvertiser != null) {
            try {
                /* ipAddresses may well not contain any IP addresses (e.g. it may be "USB").
                 * NetworkAdvertiser will ignore any tokens that it cannot match to an IPv4
                 * network interface.
                 */
                mAdvertiser.serverListening(ipAddresses);
            } catch (VNCNetworkAdvertiserException e) {
                LOG.log(Level.SEVERE, "Failed to start network advertiser", e);
                toast(getResources().getString(R.string.SS_03_261,
                                               e.errorCode));
            }
        }
        updateVncNotifier();

        dispatchBroadcast(new BroadcastDispatcher() {
            @Override
            public void dispatch(
                    final IVncServerListener l) throws RemoteException {
                l.listeningCb(ipAddresses);
            }
        });
    }

    public void connectingCb(VncServer obj) {
        Log.i(TAG, "connectingCb");

        mCurrentState = new VncServerState(VncServerMainState.CONNECTING,null,null);
        if (mAdvertiser != null) {
            mAdvertiser.serverConnecting();
        }

        dispatchBroadcast(new BroadcastDispatcher() {
            @Override
            public void dispatch(
                    final IVncServerListener l) throws RemoteException {
                l.connectingCb();
            }
        });
    }

    public void connectedCb(VncServer obj, final String ipAddress) {
        Log.i(TAG, "connectedCb " + ipAddress);

        if (mAutomotiveExt != null) {
            mAutomotiveExt.connected();
        }

        if (mAdvertiser != null) {
            mAdvertiser.serverConnected();
        }

        boolean needAcceptPrompt = true;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean acceptPromptEnabled = prefs.getBoolean("vnc_accept_prompt", false);

        boolean isListenBearer = false;
        try {
            isListenBearer = mListening &&
                (mListeningCommand != null) &&
                (mListeningCommand.getString(VncCommandString.TYPE).equals("L"));
        } catch (VncException e) {}

        if(!isListenBearer || !acceptPromptEnabled) {
            try {
                mServer.accept(true);
                needAcceptPrompt = false;
            } catch(VncException e) {
                LOG.log(Level.WARNING, "Failed to implicitly accept connection: exception", e);
            }
        }

        mCurrentState = new VncServerState(VncServerMainState.CONNECTING,ipAddress,null);
        mConnectedAddress = ipAddress;

        if(needAcceptPrompt) {
            mCurrentState.setRequestingDialog(true);
            updateVncNotifier();
        }

        dispatchBroadcast(new BroadcastDispatcher() {
            @Override
            public void dispatch(
                    final IVncServerListener l) throws RemoteException {
                l.connectedCb(ipAddress);
            }
        });
    }

    public void runningCb(VncServer obj) {
        Log.i(TAG, "runningCb");
        mCurrentState = new VncServerState(VncServerMainState.RUNNING, mConnectedAddress, null);
        updateVncNotifier();

        dispatchBroadcast(new BroadcastDispatcher() {
            @Override
            public void dispatch(
                    final IVncServerListener l) throws RemoteException {
                l.runningCb();
            }
        });
    }

    public synchronized void disconnectedCb(VncServer obj) {
        Log.i(TAG, "disconnectedCb");
        Log.i(TAG, "state is: " + mCurrentState.getState());
        Log.i(TAG, "server state is: " + mServer.getState());

        if (mAutomotiveExt != null) {
            mAutomotiveExt.disconnected();
        }

        // Check that this isn't a delayed disconnected callback
        if (mServer.getState() !=
                com.realvnc.vncserver.core.VncServerState.VNC_STATE_DISCONNECTED)
            return;

        mDisconnectRequested = false;

        mCurrentState = new VncServerState(VncServerMainState.DISCONNECTED);

        updateVncNotifier();

        dispatchBroadcast(new BroadcastDispatcher() {
            @Override
            public void dispatch(
                    final IVncServerListener l) throws RemoteException {
                Log.i(TAG, "disconnectedCb: " + l);
                l.disconnectedCb();
            }
        });

        mConnectivityChanging = false;

        if(mCmd != null) {
            actOnCurrentCommand();
        } else if(mAutoReListen && mListening) {
            startListening();
        } else {
            stopSelf(mStartId);
        }
        checkQueuedCommand();

        // If we're no longer listening, notify the NetworkAdvertiser that
        // we've stopped.
        if (!mListening && mAdvertiser != null) {
            mAdvertiser.serverStopped();
        }
    }

    public void errorCb(VncServer obj, int errorCode, Exception e) {
        if (mAdvertiser != null) {
            mAdvertiser.serverStopped();
        }

        if(errorCode != 0) {
            Log.i(TAG, "errorCb " + errorCode + " " + e, e);
            mCurrentState.setIsRunning(false);
            handleError(errorCode);
        }
    }

    public void keygenCb (VncServer vncServer, final byte[] keyPair) {
        writePrivateKey(keyPair);

        try {
            mServer.setKey(keyPair);
        } catch(VncException e) {
            LOG.log(Level.WARNING, "Failed to set key pair: exception", e);
        }

        mCurrentState = new VncServerState(VncServerMainState.DISCONNECTED);

        dispatchBroadcast(new BroadcastDispatcher() {
            @Override
            public void dispatch(
                    final IVncServerListener l) throws RemoteException {
                l.keygenCb(keyPair);
            }
        });
        checkQueuedCommand();
    }

    public void remoteKeyCb (VncServer vncServer, byte[] rsaKey, byte[] signature) {
        try {

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            boolean validate = prefs.getBoolean("vnc_signature_validation", true);

            if(mCurrentCmd != null) {
                String type = mCurrentCmd.getString(VncCommandString.TYPE);
                if(!type.equals(DATA_RELAY_TYPE)) {
                    validate = false;
                }
            } else {
                validate = false;
            }

            if(!validate) {
                mServer.acceptRemoteKey(true);
            } else {
                byte[] sig;
                try {
                    sig = mCurrentCmd.getBase64Value(SIGNATURE_KEY);
                } catch(VncException e) {
                    // This happens if there is no "-sig" field in
                    // the provided command string. Validation is
                    // turned on, so we should disallow this
                    // connection attempt.
                    mServer.acceptRemoteKey(false);
                    return;
                }

                if(sig.length > MAXIMUM_SIGNATURE_LENGTH)
                    throw new VncException(VncServerCoreErrors.VNCSERVER_ERR_INVALID_COMMAND_STRING);
                if(Arrays.equals(signature, sig))
                    mServer.acceptRemoteKey(true);
                else
                    mServer.acceptRemoteKey(false);
            }
        } catch(VncException e) {
            LOG.log(Level.WARNING, "Failed to validate client key: exception", e);
        }
    }

    public void authCb (VncServer vncServer,
            final String username,
            final String password) {

        Log.i(TAG, "Viewer auth: user '" + username + "' password '" + password + "'");

        mCurrentState = new VncServerState(VncServerMainState.AUTHENTICATING);
        mCurrentState.setUserPass(username, password);

        mCurrentState.setRequestingDialog(true);
        updateVncNotifier();

        dispatchBroadcast(new BroadcastDispatcher() {
            @Override
            public void dispatch(
                    final IVncServerListener l) throws RemoteException {
                l.authCb(username, password);
            }
        });
    }

    public void loginCb (VncServer vncServer,
            final boolean usernameReq,
            final boolean passwordReq) {

        Log.i(TAG, "RevAuth: needuser " + usernameReq + " password " + passwordReq);

        mCurrentState = new VncServerState(VncServerMainState.REQUESTING_AUTH);
        mCurrentState.setUserPassReq(usernameReq, passwordReq);

        mCurrentState.setRequestingDialog(true);
        updateVncNotifier();

        dispatchBroadcast(new BroadcastDispatcher() {
            @Override
            public void dispatch(
                    final IVncServerListener l) throws RemoteException {
                l.loginCb(usernameReq, passwordReq);
            }
        });
    }

    public void remoteControlAvailableCb (VncServer vncServer,
            final int error) {
        Log.i(TAG, "RemoteControlAvailable: " + error);

        // Check whether view-only is acceptable
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean allowViewOnly = prefs.getBoolean("vnc_allow_view_only", true);

        // Get the available remote control methods
        List<VncRemoteControlInfo> rcInfo;
        try {
            rcInfo = new ArrayList<VncRemoteControlInfo>(mServer.getRemoteControlInfo());
        } catch(VncException e) {
            LOG.log(Level.SEVERE, "Failed to get remote control info: exception", e);
            rcInfo = new ArrayList<VncRemoteControlInfo>();
        }

        // Filter the remote control methods to see if we have a suitable one
        boolean haveSuitableRemoteControl = false;
        Log.i(TAG, "Remote control methods available: " + rcInfo.size());
        for (VncRemoteControlInfo rc : rcInfo) {
            boolean injectionSupported = rc.getMotionInjectionSupport() && rc.getKeyInjectionSupport();
            Log.i(TAG, "View-only allowed: " + allowViewOnly + ", this method supports injection: " + injectionSupported);
            boolean suitable = allowViewOnly || injectionSupported;
            // Inform the SDK about whether this remote control method should be used
            try {
                mServer.blacklistRemoteControl(rc, !suitable);
            } catch(VncException e) {
                LOG.log(Level.WARNING, "Failed to (un)blacklist remote control: exception", e);
            }
            if (suitable)
                haveSuitableRemoteControl = true;
        }

        if (haveSuitableRemoteControl) {
            finishActingOnCurrentCommand();
            return;
        }

        // If we reach here but the error is VNCSERVER_ERR_NONE, it means that
        // a remote control method is available, but we decided that it was not
        // suitable. Hence we should attempt to install a different service
        // instead.
        if (error == VncServerCoreErrors.VNCSERVER_ERR_UNABLE_TO_START_SERVICE
                || error == VncServerCoreErrors.VNCSERVER_ERR_NONE) {
            if(!mAttemptedRcsInstall)
                startRemoteControlInstall();
            else
                /* Already tried to install once and failed, so act
                 * as if the installation failed immediately. */
                installationResult(VncServerCoreErrors.VNCSERVER_ERR_UNABLE_TO_START_SERVICE);
            mAttemptedRcsInstall = false;
        } else {
            reset();
            errorCb(null, error, null);
        }
    }

    /* General error handling function always moves the sample
     * application's state to ERROR. */
    public void handleError(final int errorCode) {
        handleError(errorCode, true);
    }

    public void handleError(final int errorCode, boolean moveToErrorState) {
        boolean isConnected =
            (mCurrentState.getState() != VncServerMainState.DISCONNECTED &&
            mCurrentState.getState() != VncServerMainState.ERROR) &&
            mCurrentState.isRunning();
        if (moveToErrorState) {
            mCurrentState = new VncServerState(VncServerMainState.ERROR, errorCode);
        }
        mCurrentState.setIsRunning(isConnected);
        String s = "?";

        try {
            Field field = R.string.class.getDeclaredField("error_vnc_" + errorCode);
            int resCode = field.getInt(R.string.class);
            s = getResources().getString(resCode);
        } catch(NoSuchFieldException e2) {
            /* This happens if there was no string resource
             * corresponding to this error code */
            s = getResources().getString(R.string.SS_03_259, errorCode);
        } catch(Exception e2) {
            s = e2.getMessage();
        }

        LOG.info("Error: " + errorCode + " message '" + s + "'");
        if((errorCode != VncServerCoreErrors.VNCSERVER_ERR_PERMISSIONS) &&
           (errorCode != VncServerCoreErrors.VNCSERVER_ERR_UNABLE_TO_START_SERVICE) &&
           (errorCode != 0)) {
            toast(s);
        }

        if (moveToErrorState) {
            updateVncNotifier();
        }

        dispatchBroadcast(new BroadcastDispatcher() {
            @Override
            public void dispatch(
                    final IVncServerListener l) throws RemoteException {
                l.errorCb(errorCode);
            }
        });

        String listeningType = null;
        if (mListeningCommand != null) {
            try {
                listeningType = mListeningCommand.getString(VncCommandString.TYPE);
            } catch (VncException e) {
                // Ignore type exceptions, as the code below handles a
                // null type.
            }
        }
        if(errorCode == VncServerCoreErrors.VNCSERVER_ERR_PORT_IN_USE &&
                mListening &&
                listeningType != null &&
                listeningType.equals(VncUsbState.AAP_BEARER_TYPE) &&
                VncUsbState.getStage(this) !=
                    VncUsbState.Stage.TOGGLING_TETHERING_ON) {
            // A port in use error when using AAP can usually be fixed by
            // toggling tethering on and off.
            VncUsbState.setStage(this, VncUsbState.Stage.TOGGLING_TETHERING_ON);
            VncUsbState.startUsbTethering(this);
        }

        if(mListening &&
           ((errorCode == VncServerCoreErrors.VNCSERVER_ERR_LOGIN_REJECTED) ||
            (errorCode == VncServerCoreErrors.VNCSERVER_ERR_NOT_LICENSED_FOR_VIEWER)))
        {
            startListening();
        }
        checkQueuedCommand();
    }

    private void dispatchBroadcast(final BroadcastDispatcher bd) {
        // ensure all callbacks come from the main service thread
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                int i = mListeners.beginBroadcast();
                while (i > 0) {
                    i--;
                    try {
                        bd.dispatch(mListeners.getBroadcastItem(i));
                    } catch (RemoteException e) {
                        // The RemoteCallbackList will take care of removing
                        // the dead object for us.
                    }
                }
                mListeners.finishBroadcast();
            }
        });
    }

    private boolean mAutoReListen;
    private boolean mListening;
    private VncCommandString mListeningCommand;

    private RemoteCallbackList<IVncServerListener> mListeners = new RemoteCallbackList<IVncServerListener>();

    private class VncServerClient extends IVncServerInterface.Stub {
        public void registerListener(
                final IVncServerListener listener) {
            if(listener != null) {
                mListeners.register(listener);
            }
        }

        public void unregisterListener(
                final IVncServerListener listener) {
            if(listener != null) {
                mListeners.unregister(listener);
            }
        }

        public void VNCServerReset() {
            reset();
        }

        public void VNCServerDisconnect() {
            mAsyncHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDisconnectRequested = true;
                    mServer.reset();
                }
            });
        }

        public VncServerState VNCServerStateGetState() {
            return mCurrentState;
        }

        public void VNCServerAuthenticate(boolean accept) {
            mCurrentState.setRequestingDialog(false);
            try {
                mServer.authenticate(accept);
            } catch(VncException e) {
                LOG.log(Level.WARNING, "Failed to authenticate: exception", e);
            }
            updateVncNotifier();
        }

        public void VNCServerLogin(String username, String password) {
            mCurrentState.setRequestingDialog(false);
            try {
                mServer.login(username, password);
            } catch(VncException e) {
                LOG.log(Level.WARNING, "Failed to set log in: exception", e);
            }
            updateVncNotifier();
        }

        public void VNCServerAccept(boolean accept) {
            mCurrentState.setRequestingDialog(false);
            try {
                mServer.accept(accept);
            } catch(VncException e) {
                LOG.log(Level.WARNING, "Failed to accept connection: exception", e);
            }
            updateVncNotifier();
        }

        public void VNCServerConnect(String command, boolean autoReListen) {
            doVNCServerConnect(command, autoReListen, false);
        }

        public String VNCServerGetLogPath() {
            return getFileStreamPath(LOG_FILE).getAbsolutePath();
        }

        public String VNCServerGetVersionString() {
            return mServer.getVersionString();
        }

        public void VNCServerRequestDialog() {
            mCurrentState.setRequestingDialog(true);
            dispatchBroadcast(new BroadcastDispatcher() {
                @Override
                public void dispatch(
                        final IVncServerListener l) throws RemoteException {
                    l.updateUiCb();
                }
            });
        }

        public void VNCServerClearRequestDialog() {
            mCurrentState.setRequestingDialog(false);
            dispatchBroadcast(new BroadcastDispatcher() {
                @Override
                public void dispatch(
                        final IVncServerListener l) throws RemoteException {
                    l.updateUiCb();
                }
            });
        }

        public void VNCServerSetError(int errorCode) {
            handleError(errorCode);
        }

        public void VNCServerInstallationResult(int errorCode) {
            installationResult(errorCode);
        }

        public void VNCServerLoadLicenses() {
            loadServerLicenses();
        }

        public void VNCServerSetLandscapeLock(final boolean landscapeLock) {
            doSetLandscapeLock(landscapeLock);
        }
    }

    void actOnCurrentCommand() {

        try {
            /* Is this a connection which should re-listen when it
             * closes? */
            if(mCmd.getString(VncCommandString.TYPE).equals("L") ||
               mCmd.getString(VncCommandString.TYPE).equals("USB") ||
               mCmd.getString(VncCommandString.TYPE).equals("AAP"))
            {
                mListening = true;
                mListeningCommand = mCmd;
            }
            else
            {
                mListening = false;
            }
            if(mCmd.getString(VncCommandString.TYPE).equals("USB")) {
                /* Ask the USB listener to notify on tethering disconnection */
                VncUsbState.setStage(VncServerService.this,
                        VncUsbState.Stage.WAITING_FOR_DISCONNECT);
            } else {
                /* Tell the USB listener not to disconnect this connection when
                 * tethering is disabled */
                VncUsbState.setStage(VncServerService.this,
                        VncUsbState.Stage.NOT_WAITING);
            }

            if (mCmd.getString(VncCommandString.TYPE).equals("AAP") &&
                    mAapConnectionManager != null &&
                    !mAapConnectionManager.readyForConnection()) {
                // The accessory isn't ready yet so just return.
                return;
            }

            configureFromPreferences();
            mCurrentCmd = mCmd;

            if (!mTryingToInstallRcs) {
                /* Need to check that remote control service is available,
                 * connection attempt will continue in the
                 * remoteControlAvailableCb() callback.
                 */
                mServer.checkRemoteControlAvailable();
            }
        } catch(VncException e) {
            reset();
            errorCb(null, e.errorCode, null);
            mCmd = null;
        }
    }

    void finishActingOnCurrentCommand() {
        try {
            if(mCmd != null) {
                mServer.connect(mCmd);
                mCmd = null;

                startSelf();
            }
        } catch(VncException e) {
            reset();
            errorCb(null, e.errorCode, null);
        }
    }

    void startRemoteControlInstall() {
        Log.i(TAG, "Attempting install of RemoteControlService");
        mTryingToInstallRcs = true;
        ServiceInstaller.do_install(this);
    }

    private void doSetLandscapeLock(final boolean landscapeLock) {
        mHandler.post(new Runnable() {
            @Override
            @TargetApi(26)
            public void run() {

                if (mLandscapeLockSet == landscapeLock) {
                    LOG.info("Landscape lock already set to: " + mLandscapeLockSet);
                    return;
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        && !Settings.canDrawOverlays(VncServerService.this)) {
                    LOG.info("Requesting Overlay Permission to lock orientation");
                    final Intent intent = new Intent(
                            VncServerService.this,
                            VNCMobileServer.class);
                    intent.setAction(SampleIntents.OVERLAY_PERMISSION_DIALOG_INTENT);
                    intent.setPackage(getPackageName());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    return;
                }

                LOG.info("Setting landscape lock: " + landscapeLock);
                try{
                    if (landscapeLock) {
                        mServer.getOrientationManager().lockOrientationEx(
                                VncOrientationManager.ORIENTATION_LANDSCAPE_LOCK);
                    } else {
                        mServer.getOrientationManager().lockOrientationEx(
                                VncOrientationManager.ORIENTATION_DISABLE_LOCK);
                    }
                    mLandscapeLockSet = landscapeLock;
                } catch(final VncException ve) {
                    LOG.log(
                            Level.WARNING,
                            "Could not "+ (landscapeLock ? "enable" : "disable")
                                    + " landscape lock",
                            ve);
                }
            }
        });
    }

    /*--------------------------------------------------------------------------------------------*/
    //  Extension message
    /*--------------------------------------------------------------------------------------------*/
    private class AutomotiveExt
        implements VncExtensionListener,
                   VncContextInformationManager.Listener,
                   VncContextInformationManager.AccessibilityServiceProvider {
        private boolean mContextListenerRegistered = false;

        private static final String UNKNOWN_ACTIVITY_NAME = "Unknown";

        /* Automotive extension message types: */
        private static final int AUTOEXT_FG_APP_REPORTING = 0;
        private static final int AUTOEXT_FG_APP_REPORTING_RESPONSE = 1;
        private static final int AUTOEXT_FG_APP_MINIMISE = 2;
        private static final int AUTOEXT_FG_APP_SCREEN_LOCK_DISABLE = 3;
        private static final int AUTOEXT_FG_APP_LANDSCAPE_LOCK = 4;
        private static final int AUTOEXT_FG_APP_VOICE_COMMAND = 5;

        /* Terminal Mode categories: */
        private static final int TMCAT_HOME_SCREEN = 0x00010001;
        private static final int TMCAT_PHONE = 0x00020000;
        private static final int TMCAT_PHONE_CONTACT_LIST = 0x00020001;
        private static final int TMCAT_MUSIC_APP = 0x00030001;
        private static final int TMCAT_MAPS = 0x00050000;
        private static final int TMCAT_VOICE_COMMAND = 0xF0000010;
        private static final int TMCAT_SYSTEM_UI = 0xffff0000;
        private static final int TMCAT_UNKNOWN = 0;

        /*----------------------------------------------------------------------------------------*/
        //  Definitions
        /*----------------------------------------------------------------------------------------*/
        private static final String MESSAGE_NAME = "com.jvckenwood.realvnc";

        private static final int MESSAGE_ID_FG_APP_INFO = 0x8001;

        private static final int APPLICATION_KIND_WHITE = 0x02;
        private static final int APPLICATION_KIND_GRAY  = 0x01;
        private static final int APPLICATION_KIND_BLACK = 0x00;

        /*----------------------------------------------------------------------------------------*/
        //  Variables
        /*----------------------------------------------------------------------------------------*/
        private VncServerService mSvc;
        private PowerManager mPowerManager;
        private PowerManager.WakeLock mWakeLock;
        private boolean mWakeLockAcquired;

        private VncExtension mExtHandle;

        private String mLastTopActivityName = null;
        private String mTopActivityName     = UNKNOWN_ACTIVITY_NAME;
        private String mTopPackageName      = UNKNOWN_ACTIVITY_NAME;

        /*----------------------------------------------------------------------------------------*/
        //  Constructor
        /*----------------------------------------------------------------------------------------*/
        AutomotiveExt(VncServerService svc) {
            mSvc = svc;

            mPowerManager = (PowerManager)svc.getSystemService(Context.POWER_SERVICE);

            mWakeLock = mPowerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                                                  PowerManager.SCREEN_DIM_WAKE_LOCK |
                                                  PowerManager.ON_AFTER_RELEASE,
                                                  "VncWakeLock");
            mWakeLockAcquired = false;
        }

        /*----------------------------------------------------------------------------------------*/
        //  Public / Package Private Functions
        /*----------------------------------------------------------------------------------------*/
        void register(VncServer server) throws VncException {
            mExtHandle = mServer.registerExtension(MESSAGE_NAME, this);
        }

        /*----------------------------------------------------------------------------------------*/
        @Override
        public void extensionEnabled(VncServer server, VncExtension extension, boolean enabledFlag) {
            LOG.info("Automotive extension: enabled=" + enabledFlag);

            if (enabledFlag) {
                //connected();
            }
            else {
                //disconnected();
            }
        }

        @Override
        public void extensionMessageReceived(VncServer server,
                                             VncExtension extension,
                                             byte[] payload,
                                             int payloadOffset,
                                             int payloadLength) {
        }

        /*----------------------------------------------------------------------------------------*/
        //  Private Functions
        /*----------------------------------------------------------------------------------------*/
        private void connected() {
            Log.d(TAG, "connected");
            setStayAwake(true);
            setLandscapeLock(true);

            VncServerService.this.mAsyncHandler.post(new Runnable() {
                @Override
                public void run() {
                    mLastTopActivityName = null;
                    registerContextListener();
                }
            });
        }

        private void disconnected() {
            Log.d(TAG, "disconnected");
            setStayAwake(false);
            setLandscapeLock(false);

            VncServerService.this.mAsyncHandler.post(new Runnable() {
                @Override
                public void run() {
                    unregisterContextListener();
                }
            });
        }

        /*-------------------------------------------------------   ---------------------------------*/
        private void registerContextListener() {
            Log.i(TAG, "+++++++++++++++++++++++++ registerContextListener +++++++++++++++++++++++++++++++++");
            //LOG.info("com.realvnc.automotive: start app reporting");
            if (!mContextListenerRegistered) {
                mServer.getContextInformationManager().addListener(this);
                mServer.getContextInformationManager().addAccessibilityServiceProvider(this);
                mContextListenerRegistered = true;
            }
        }

        private void unregisterContextListener() {
            //LOG.info("com.realvnc.automotive: stop app reporting");
            if (mContextListenerRegistered) {
                mServer.getContextInformationManager().removeAccessibilityServiceProvider(this);
                mServer.getContextInformationManager().removeListener(this);
                mContextListenerRegistered = false;
            }
        }

        @Override
        synchronized public void contextInformationChanged(List<CapturedContextInformation> items, int flags) {
            Log.i(TAG, "contextInformationChanged items:" + items.size() + " -------------------------------------------------->");

            if (items.size() < 1) {
                mTopPackageName  = UNKNOWN_ACTIVITY_NAME;
                mTopActivityName = UNKNOWN_ACTIVITY_NAME;
            } else {
                /* Only care about the top most context information */
                CapturedContextInformation top = items.get(items.size() - 1);
                try {
                    ActivityInfo activityInfo = getPackageManager().getActivityInfo(top.getActivity(), 0);
                    if (activityInfo == null) {
                        return;
                    }
                    mTopActivityName = activityInfo.loadLabel(getPackageManager()).toString();
                    //Log.d(TAG, "name : " + mTopActivityName);
                }
                catch (PackageManager.NameNotFoundException e) {
                    //mTopActivityName = UNKNOWN_ACTIVITY_NAME;
                    //Log.d(TAG, "name : Name not found exception.");
                    return;
                }
                mTopPackageName = top.getActivity().getPackageName();
            }
            checkActivity();
        }

        /*----------------------------------------------------------------------------------------*/
        private void setStayAwake(boolean stayAwake) {

            LOG.info("com.realvnc.automotive: disable screensaver: " + stayAwake);

            if(stayAwake && !mWakeLockAcquired)
            {
                mWakeLock.acquire();
                mWakeLockAcquired = true;
            }
            else if(!stayAwake && mWakeLockAcquired)
            {
                mWakeLock.release();
                mWakeLockAcquired = false;
            }
        }

        /*----------------------------------------------------------------------------------------*/
        public void setLandscapeLock(final boolean landscapeLock) {
            LOG.info("com.realvnc.automotive: setting landscape lock: " + landscapeLock);
            doSetLandscapeLock(landscapeLock);
        }

        private byte getApplicationType(String name) {
            return APPLICATION_KIND_WHITE;
        }

        synchronized public void accessibilityServiceRequired() {
            LOG.info("Accessibility service required for context information monitoring");
            Intent i = new Intent(VncServerService.this, VNCMobileServer.class);
            i.setAction(SampleIntents.ACCESSIBILITY_DIALOG_INTENT);
            i.setPackage(getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }

        synchronized public String getTopActivityName() {
            return mTopActivityName;
        }

        private byte[] createAppInfoCommand(String activityName, String packageName) {
            int type = WhiteListManager.INSTANCE.check(packageName).getValue();


            //toast(packageName + "@" + Integer.toString(type));


            int activityNameLength = activityName.getBytes().length + 1;
            int packageNameLength  = packageName.getBytes().length  + 1;
            int payloadSize        = 2 + 2 + packageNameLength + 2 + activityNameLength + 1;

            int offset = 0;
            byte[] msg = new byte[payloadSize];

            // << Copy the message ID >>
            System.arraycopy(new byte[] {
                            (byte)(MESSAGE_ID_FG_APP_INFO >>> 8),
                            (byte)(MESSAGE_ID_FG_APP_INFO)
                    }, 0,
                    msg, 0, 2);
            offset += 2;

            // << Copy the package name length >>
            System.arraycopy(new byte[] {
                            (byte)((packageNameLength >> 8) & 255),
                            (byte)((packageNameLength >> 0) & 255)
                    }, 0,
                    msg, offset, 2);
            offset += 2;

            // << Copy the package name >>
            System.arraycopy(packageName.getBytes(), 0, msg, offset, packageNameLength - 1);
            offset += packageNameLength;

            // << Copy the activity name length >>
            System.arraycopy(new byte[] {
                            (byte)((activityNameLength >> 8) & 255),
                            (byte)((activityNameLength >> 0) & 255)
                    }, 0,
                    msg, offset, 2);
            offset += 2;

            // << Copy the activity name >>
            System.arraycopy(activityName.getBytes(), 0, msg, offset, activityNameLength - 1);
            offset += activityNameLength;

            msg[offset] = (byte)type;

            Log.i(TAG, "Send:ActivityName[" + activityName + "], Package[" +
                    "" + packageName + "], level[" + type +"]");
            Log.i(TAG, "Length:"+payloadSize+"["+StringUtility.ByteToHexString(msg)+"]");

            return msg;
        }

        private void checkActivity() {
            String activityName = getTopActivityName();
            String packageName  = mTopPackageName;

            Log.i(TAG, "checkActivity@[ Activity:"+activityName+", Package:"+packageName + " ]");

            if (WhiteListManager.INSTANCE.isIgnoreApplication(packageName)) {
                return;
            }

            if(mExtHandle != null) {
                byte[] sendData = createAppInfoCommand(activityName, packageName);
                try {
                    mSvc.mServer.sendExtensionMessage(mExtHandle, sendData, 0, sendData.length);
                }
                catch(VncException e) {
                }
            }
        }

        @SuppressWarnings("deprecation")
        private float largestScaleApi1() {
            float windowAnimationScale = Settings.System.getFloat(mSvc.getContentResolver(),
                Settings.System.WINDOW_ANIMATION_SCALE, 1);
            float transitionAnimationScale = Settings.System.getFloat(mSvc.getContentResolver(),
                Settings.System.TRANSITION_ANIMATION_SCALE, 1);

            return Math.max(windowAnimationScale, transitionAnimationScale);
        }

        @TargetApi(17)
        private float largestScaleApi17() {
            float windowAnimationScale = Settings.Global.getFloat(mSvc.getContentResolver(),
                Settings.Global.WINDOW_ANIMATION_SCALE, 1);
            float transitionAnimationScale = Settings.Global.getFloat(mSvc.getContentResolver(),
                Settings.Global.TRANSITION_ANIMATION_SCALE, 1);

            return Math.max(windowAnimationScale, transitionAnimationScale);
        }

        /* How long could a transition from one app or window to
         * another one last? We need to wait for this long after
         * injecting a minimise request, to avoid sending any frame
         * buffer updates to the viewer until the minimised app has
         * been completely hidden.
         *
         * We are attempting to mimick what WindowManagerService.java
         * does here, which may not be entirely robust. */

        private float maxTransitionDuration() {
            float largestScale = (Build.VERSION.SDK_INT >= 17) ?
                    largestScaleApi17() : largestScaleApi1();

            float maxDurationMillis = 750 * largestScale;

            return maxDurationMillis;
        }

        /*
        public void appMinimiseRequest(VncServer server,
                                       String rx_name, int rx_category) {
            String cur_name = getTopActivityName();
            int cur_category = getCategory(cur_name);

            if((cur_category == rx_category) &&
               cur_name.equals(rx_name)) {

                try {
                    server.freeze(true);

                    Intent launchHome = new Intent(Intent.ACTION_MAIN);
                    launchHome.addCategory(Intent.CATEGORY_HOME);
                    launchHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(launchHome);

                    try {
                        Thread.sleep((int)maxTransitionDuration());
                    } catch (InterruptedException e) {}

                    server.freeze(false);

                } catch(VncException e) {
                    LOG.log(Level.WARNING, "Failed to minimize app: exception", e);
                }
            }
        }
        */
    }

    private AutomotiveExt mAutomotiveExt;
    private BluetoothHeadset mBtHeadset;

    /* Parameters for the connection accept/reject notifier */
    private static final long[] mVibrationPattern = { 1500, 250 };

    private void updateVncNotifier_i() {
        boolean visible = false;
        boolean pester = false;
        String intentAction = SampleIntents.SHOW_UI_INTENT;
        final Resources res = getResources();
        final Context context = getApplicationContext();

        final Notification.Builder builder =
                NotificationHelper.getNotificationBuilder(context);
        builder.setSmallIcon(R.mipmap.icon_notification);

        switch(mCurrentState.getState()) {

            case CONNECTING:
                visible = mCurrentState.requestingDialog();
                pester = visible;
                builder.setContentTitle(res.getString(R.string.SS_02_214));
                builder.setContentText(res.getString(R.string.SS_02_215));
                builder.setTicker(res.getString(R.string.SS_02_213));
                intentAction = SampleIntents.ACCEPT_PROMPT_DIALOG_INTENT;
                break;

            case RUNNING:
                builder.setContentTitle(res.getString(R.string.SS_02_211));
                builder.setContentText(res.getString(R.string.SS_02_212));
                builder.setTicker(res.getString(R.string.SS_02_210,
                        mCurrentState.getConnectedAddress()));
                builder.setOngoing(true);
                visible = true;
                pester = false;
                break;

            case AUTHENTICATING:
                builder.setContentTitle(res.getString(R.string.SS_02_214));
                builder.setContentText(res.getString(R.string.SS_02_215));
                builder.setTicker(res.getString(R.string.SS_02_213));
                visible = mCurrentState.requestingDialog();
                pester = visible;
                intentAction = SampleIntents.AUTH_ACCEPT_DIALOG_INTENT;
                break;

            case REQUESTING_AUTH:
                builder.setContentTitle(res.getString(R.string.SS_02_214));
                builder.setContentText(res.getString(R.string.notifier_authreq_text));
                builder.setTicker(res.getString(R.string.SS_02_213));
                visible = mCurrentState.requestingDialog();
                pester = visible;
                intentAction = SampleIntents.REVAUTH_PROMPT_DIALOG_INTENT;
                break;

            case DISCONNECTED:
                // Deliberate fallthrough.
            case LISTENING:
                // Deliberate fallthrough.
            case ERROR:
                visible = false;
                break;

            default:
                // no change, leave the notifier as it is
                return;
        }

        NotificationHelper.setServerContentIntent(
                context,
                builder,
                intentAction);

        final Notification notification = builder.getNotification();

        if(pester) {
            // We require input from the user before proceeding.
            // Pester the user by enabling sound, vibration and
            // flashing LEDs so that the user knows he has to do
            // something.
            notification.defaults |= (Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS);
            notification.flags |= Notification.FLAG_INSISTENT;
            notification.vibrate = mVibrationPattern;
        }

        // Clear the previous notification.
        mNotificationManager.cancel(VncServerService.STATUS_NOTIFICATION_ID);

        if(visible) {
            mNotificationManager.notify(
                    VncServerService.STATUS_NOTIFICATION_ID,
                    notification);
        }
    }

    private void updateVncNotifier() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                updateVncNotifier_i();
            }
        });
    }

    @android.annotation.TargetApi(21)
    private static String[] getSupportedAbisApi21() {
        /* Here we ought to be able to directly access the SUPPORTED_ABIS
         * field because we are within a TargetApi annotated method.
         * Unfortunately Android tries to optimize it out, and throws up ugly
         * warnings such as:
         *   "VFY: unable to resolve static field" */
        try {
            return (String[]) android.os.Build.class.getField("SUPPORTED_ABIS").get(null);
        } catch (NoSuchFieldException e) {
            return new String[] {};
        } catch (IllegalAccessException e) {
            return new String[] {};
        }
    }

    @SuppressWarnings("deprecation")
    private static String[] getSupportedAbisApi8() {
        return new String[] {android.os.Build.CPU_ABI, android.os.Build.CPU_ABI2};
    }

    private static String[] getSupportedAbis() {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            return getSupportedAbisApi21();
        } else {
            return getSupportedAbisApi8();
        }
    }

    @Override
    public synchronized void onCreate() {

        super.onCreate();

        mNotificationManager =
                NotificationHelper.getNotificationManager(
                        getApplicationContext());

        // Start the service in the foreground with a user-visible notification
        NotificationHelper.ServiceUtils.startServiceInForeground(
                this,
                VncServerService.FOREGROUND_SERVICE_NOTIFICATION_ID,
                getString(R.string.SS_02_216),
                getString(R.string.SS_02_217),
                R.mipmap.icon_notification);

        String filePath = getFileStreamPath(LOG_FILE).getAbsolutePath();
        VncLog.init(filePath, true);

        LOG.setLevel(Level.INFO);

        LOG.info("*** VNC AUTOMOTIVE SERVER SERVICE STARTS ***");

        // Log device signing keys and details useful for debugging.
        ServiceInstaller.getSystemSigningKeys(this);
        Log.i(TAG, "device = " + Build.DEVICE);
        Log.i(TAG, "version_release = " + Build.VERSION.RELEASE);
        Log.i(TAG, "model = " + Build.MODEL);
        Log.i(TAG, "product = " + Build.PRODUCT);
        Log.i(TAG, "cpu_abi options = " + java.util.Arrays.toString(getSupportedAbis()));
        Log.i(TAG, "api_level = " + Build.VERSION.SDK_INT);

        // Ensure all preferences get set to their default values,
        // even if the user has never opened the preferences screen
        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);

        VncConfigFile.checkVncConfig(getBaseContext());

        mAsyncHandlerThread = new HandlerThread("Service async handler");
        mAsyncHandlerThread.start();
        Looper l = mAsyncHandlerThread.getLooper();
        if (l == null) {
            Log.e(TAG, "Failed to start async handler thread");
            handleError(VncServerCoreErrors.VNCSERVER_ERR_ENVIRONMENT);
        }
        mAsyncHandler = new Handler(l);

        mBtHeadset = new BluetoothHeadset(this);

        mAutomotiveExt = new AutomotiveExt(this);

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mReceiver = new VNCBroadcastReceiver();
        registerReceiver(mReceiver, filter);

        // Dig out some useful information to pass into the VNC Automotive server which we create.
        //noinspection HardwareIds
        String deviceIdentifier = Settings.Secure.getString(
                getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        // If ANDROID_ID didn't work then try the device serial number
        if (deviceIdentifier == null || deviceIdentifier.equals("")) {
            //noinspection HardwareIds
            deviceIdentifier = Build.SERIAL;
        }

        // If *that* didn't work, throw in the towel
        if (deviceIdentifier == null || deviceIdentifier.equals("")) {
            deviceIdentifier = "<unknown>";
        }

        mAapConnectionManager = AapConnectionManager.create(this, this);

        try {
            mServer = VncServer.create(this, this);
        } catch (java.lang.UnsatisfiedLinkError e) {
            LOG.info("Received UnsatisfiedLinkException when trying to create the server");
            handleError(VncServerCoreErrors.VNCSERVER_ERR_ENVIRONMENT);
        }

        try {
            mServer.setH264Encoder(mH264Encoder, false);
        } catch (VncException e) {
            LOG.log(Level.WARNING, "Failed to register H.264 encoder", e);
        }

        // If remote control is not supported on this OS version then
        // we will have received the error before the constructor
        // returns
        if(mCurrentState.getState() != VncServerMainState.ERROR) {
            try {
                mAdvertiser = new NetworkAdvertiser(this,
                                                    getBaseContext(),
                                                    mServer,
                                                    getResources().getDrawable(R.drawable.vncserver48x48));
            } catch (VNCNetworkAdvertiserException e) {
                mAdvertiser = null;
                LOG.log(Level.SEVERE, "Failed to create network advertiser", e);
                toast(getResources().getString(R.string.SS_03_260,
                                               e.errorCode));
            }

            ArrayList<String> permList = new ArrayList<String>();
            // We need this permission to read the license from the SD card.
            if (isPermissionGranted(PERMISSION_READ_EXTERNAL_STORAGE) || useLicenseFileInAssetFolder) {
                loadServerLicenses();
            } else {
                permList.add(PERMISSION_READ_EXTERNAL_STORAGE);
            }
            if (!permList.isEmpty()) {
                requestPermissions(permList);
            }

            mServer.setDesktopName(deviceIdentifier);

            mListening = false;

            mBtAudio = new VncBtAudioRouter(this);

            try {
                mBtAudio.register(mServer);
                mAutomotiveExt.register(mServer);
                configureFromPreferences();
            } catch(VncException e) {
                // This should never happen since we just created the
                // server
                LOG.log(Level.WARNING, "Failed to configure server", e);
            }


            if(!readPrivateKey()) {
                startKeyGen();
            }
        }
    }

    @android.annotation.TargetApi(23)
    private boolean isPermissionGranted(String permission) {
        boolean isGranted = true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            /* From Android 6.0 onwards we need to request 'dangerous'
             * permissions dynamically, they are no longer granted at
             * install time. */
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                isGranted = false;
            }
        }
        return isGranted;
    }

    private void requestPermissions(ArrayList<String> permList) {
        Intent intent = new Intent(this, VNCMobileServer.class);
        intent.setAction(SampleIntents.REQUEST_PERMISSIONS_INTENT);
        intent.putExtra("permissions", permList.toArray(new String[0]));
        intent.setPackage(getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * Assetにあるライセンスファイルロードして追加する。
     * @param licenses ライセンス情報インスタンス
     * @return 追加したライセンスファイル数
     * @auther JKEG
     */
    private int addAssetLicenses(Vector<Pair<String, String>> licenses) {
        int added = 0;
        AssetManager assetManager = getResources().getAssets();
        String[] fileList = null;

        try {
            // Asset/licensesフォルダにあるファイル一覧取得する
            fileList = assetManager.list(ASSET_LICENSE_PATH);
            for (String filePath : fileList) {
                // ファイルの拡張子がライセンスファイルの拡張子かチェック
                if(!filePath.endsWith(LICENSE_FILE_EXTENSION)) {
                    // ライセンスファイルの拡張子でないので、次のファイルパスを確認する
                    continue;
                }

                // フルパス作成
                String fullPath = ASSET_LICENSE_PATH + "/" + filePath;

                try {
                    // Assetのファイルをオープン
                    InputStream           inputStream  = assetManager.open(fullPath);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                    int read;
                    byte[] readBuf = new byte[1024];
                    while ((read=inputStream.read(readBuf)) > 0) {
                        outputStream.write(readBuf, 0, read);
                    }

                    // << decryption >>
                    byte[] decData = AesUtility.Companion.decrypt(outputStream.toByteArray(), VncLicenseEncryptionConst.Companion.getPassKey(), VncLicenseEncryptionConst.Companion.getInitialVector());
                    if (decData != null) {
                        String licenseText = new String(decData);

                        // ライセンス情報追加
                        licenses.add(new Pair<>(fullPath, licenseText));

                        // 追加数インクリメント
                        ++added;
                    }

                    // << Close the streams. >>
                    inputStream.close();
                    outputStream.close();
                }
                catch (IOException e) {
                    Log.e(TAG, "Failed to open Asset license file: " + filePath);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Cannot access to aseet folder: " + ASSET_LICENSE_PATH);
        }
        return added;
    }

    private int addSdCardLicenses(Vector<Pair<String, String>> licenses) {
        addAssetLicenses(licenses);

        int added = 0;
        File dir = new File(SDCARD_LICENSE_PATH);
        File[] fileList = dir.listFiles();
        if(fileList == null)
            return added;
        for(File file : fileList) {
            if(!file.isFile())
                continue;
            if(!file.getName().endsWith(LICENSE_FILE_EXTENSION))
                continue;
            try {
                InputStream is = new FileInputStream(file);
                StringBuilder sb = new StringBuilder();
                byte[] buf = new byte[1024];
                int read;
                while((read = is.read(buf)) > 0) {
                    sb.append(new String(buf, 0, read, "UTF-8"));
                }
                String licenseText = sb.toString();
                licenses.add(new Pair<String, String>(file.toString(), licenseText));
                ++added;
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "Failed to open SD card license file: " + file);
            }
        }
        return added;
    }

    private void loadServerLicenses() {
        Vector<Pair<String, String>> licenses = new Vector<Pair<String, String>>();

        // Assetフォルダのライセンスファイルを使用するとき
        if(useLicenseFileInAssetFolder) {
            // Asset内のライセンスファイルをロードする。
            if(addAssetLicenses(licenses) == 0) {
                String text = "No VNC license(s) found on Asset folder. A license is required for VNC connections.";
                LOG.info(text);
                Toast.makeText(this, text, Toast.LENGTH_LONG).show();
                return;
            }
        }
        // SDカードのライセンスファイルを使用するとき
        else {
            // SDカード内のライセンスファイルをロードする。
            if (addSdCardLicenses(licenses) == 0) {
                String text = "No VNC license(s) found on SD card. A license is required for VNC connections.";
                LOG.info(text);
                Toast.makeText(this, text, Toast.LENGTH_LONG).show();
                return;
            }
        }


        for(Pair<String, String> license : licenses) {
            try {
                mServer.addLicense(license.second);
                if (mAdvertiser != null) {
                    mAdvertiser.addLicense(license.second);
                }
            } catch (VncException e) {
                LOG.info(license.first + " does not contain a valid VNC Automotive license.");
            } catch (VNCNetworkAdvertiserException e) {
                LOG.info(license.first + " was not accepted by the network advertiser");
            }
        }
    }

    private void configureFromPreferences() throws VncException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean encrypt = prefs.getBoolean("vnc_encryption", false);
        int authType = Integer.parseInt(prefs.getString("vnc_authtype", "1"));

        if(encrypt)
            mServer.setEncryption(VncEncryptionType.VNC_ENCRYPTION_AES_128);
        else
            mServer.setEncryption(VncEncryptionType.VNC_ENCRYPTION_NONE);

        mServer.setAuthentication(authType);

        mServer.enableFeature(VncServer.FEATURE_CLIPBOARD,
                              prefs.getBoolean("vnc_clipboard", true));

        mServer.enableFeature(VncServer.FEATURE_SEND_CLIPBOARD_ON_CONNECTION,
                              prefs.getBoolean("vnc_clipboard_on_connect", false));
    }

    boolean readPrivateKey() {

        byte[] key;
        FileInputStream keyFile = null;

        try {
            final ByteArrayOutputStream keyStream = new ByteArrayOutputStream(1024);
            keyFile = openFileInput(KEY_FILE);

            final byte[] buf = new byte[1024];
            int bytesRead;

            while((bytesRead = keyFile.read(buf)) > 0) {
                keyStream.write(buf, 0, bytesRead);
            }

            key = keyStream.toByteArray();

            mServer.setKey(key);
            return true;

        } catch(VncException e) {
            LOG.log(Level.SEVERE, "Failed to read private key: exception", e);
            return false;

        } catch(FileNotFoundException e) {
            /* probably first boot */
            return false;

        } catch(IOException e) {
            LOG.log(Level.SEVERE, "Failed to read private key: exception", e);
            return false;

        } finally {
            if(keyFile != null) {
                try {
                    keyFile.close();
                } catch (IOException e) {
                    LOG.log(Level.SEVERE, "Failed to close private key file: exception", e);
                }
            }
        }
    }

    void writePrivateKey(byte[] key) {
        try {
            FileOutputStream keyFile = openFileOutput(KEY_FILE, 0);
            keyFile.write(key);
            keyFile.close();
        } catch(IOException e) {
            LOG.log(Level.SEVERE, "Failed to write private key: exception", e);
        }
    }

    private void startKeyGen() {

        try {

            mCurrentState = new VncServerState(VncServerMainState.KEYGEN);
            mCurrentState.setApiCalled(VncServerAPICalledState.KEYGEN);

            mServer.generateKey(1024);

        } catch(VncException e) {
            LOG.log(Level.SEVERE, "Failed to start key generation: exception", e);
        }
    }

    @Override
    public synchronized void onDestroy() {
        unregisterReceiver(mReceiver);

        mAsyncHandlerThread.quit();

        mServer.destroy();
        mServer = null;

        if (mBtHeadset != null) {
            mBtHeadset.close();
            mBtHeadset = null;
        }

        if (mAapConnectionManager != null) {
            mAapConnectionManager.destroy();
            mAapConnectionManager = null;
        }

        VncLog.destroy();

        LOG.info("*** VNC AUTOMOTIVE SERVER SERVICE EXITS ***");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mStartId = startId;

        if (intent == null) {
            Log.i(TAG, "onStartCommand: null intent");
            stopSelf(mStartId);
            return Service.START_NOT_STICKY;
        }
        String action = intent.getAction();

        Log.println(Log.INFO, TAG, "Got action: " + action);

        if (mServer == null) {
            Log.e(TAG, "Failed to start command, server is NULL");
        } else if (action.equals(SampleIntents.RESET_SERVER_INTENT)) {
            final boolean waitForFlush = intent.getBooleanExtra(
                SampleIntents.RESET_SERVER_WAIT_FOR_FLUSH_EXTRA, true);
            mQueuedCommand = "";
            mQueuedAutoReListen = false;
            mAsyncHandler.post(new Runnable() {
                    public void run() {
                        reset(waitForFlush);
                    }
                });

        } else if (action.equals(SampleIntents.START_SERVER_INTENT)) {
            final String cmdString = intent.getData().toString();
            mAsyncHandler.post(new Runnable() {
                    public void run() {
                        doVNCServerConnect(cmdString, true, false);
                    }
                });

        } else if (action.equals(SampleIntents.START_SERVER_FROM_AAP_INTENT)) {
            // Check that this intent should really be acted on
            if (!intent.hasExtra(EXTRA_PERMISSION_GRANTED) ||
                    intent.getBooleanExtra(EXTRA_PERMISSION_GRANTED, true)) {

                final String cmdString = intent.getData().toString();
                mAsyncHandler.post(new Runnable() {
                        public void run() {
                            doVNCServerConnect(cmdString, true, false);
                        }
                    });
            } else {
                LOG.info("Permission wasn't granted to use USB accessory");
            }

        } else if (action.equals(SampleIntents.STOP_SERVER_INTENT)) {

            stopSelf();

        } else if (action.equals(SampleIntents.RUN_SERVER_INTENT)) {

            /* Do nothing as this is just a self start */

        } else {
            Log.println(Log.ERROR, TAG, "onStartCommand: Unknown intent: " + action);
        }

        return Service.START_NOT_STICKY;
    }

    /*
     * (non-Javadoc)
     * @see android.app.Service#onBind(android.content.Intent)
     */
    @Override
    public synchronized IBinder onBind(Intent intent) {

        String action = intent.getAction();

        if (action.equals(SampleIntents.BIND_SERVICE_INTENT)) {
            return new VncServerClient().asBinder();
        } else {
            Log.println(Log.ERROR, TAG, "onBind: Unknown intent: " + action);
            return null;
        }
    }

    private void doVNCServerConnect(String command, boolean autoReListen,
                          boolean onlyWhenDisconnected) {
        if((onlyWhenDisconnected &&
                        mCurrentState.getState() != VncServerMainState.DISCONNECTED &&
                        mCurrentState.getState() != VncServerMainState.ERROR) ||
                mCurrentState.getState() == VncServerMainState.KEYGEN) {
            // If we're not in disconnected state, or error state,
            // then wait until reaching one of those states before
            // acting on the command
            mQueuedCommand = command;
            mQueuedAutoReListen = autoReListen;
            return;
        }
        boolean originallyConnected = false;
        if (mCurrentState.getState() == VncServerMainState.RUNNING) {
            originallyConnected = true;
        }
        boolean commandParsed = false;
        try {
            mCmd = new VncCommandString();
            mCmd.parse(command);
            commandParsed = true;

            mAutoReListen = autoReListen;

            /* If the command string is invalid then parse() will
             * throw an exception. So if we get here, we know the
             * string was valid, and we need to close an existing
             * connection if one is open. */

            if(mCurrentState.getState() != VncServerMainState.DISCONNECTED) {
                reset();
            } else {
                actOnCurrentCommand();
            }
        } catch(VncException e) {
            mCmd = null;
            boolean moveToErrorState = true;
            if (originallyConnected && !commandParsed) {
                /* The new command failed to parse, the previous connection
                 * still stands. */
                moveToErrorState = false;
            }
            handleError(e.errorCode, moveToErrorState);
        }
    }

    private synchronized void startListening() {
        try {
            configureFromPreferences();
            mServer.connect(mListeningCommand);
        } catch(VncException e) {
            LOG.log(Level.SEVERE, "Failed to listen: exception", e);
            reset();
        }
    }

    private void checkQueuedCommand() {
        if(mQueuedCommand.length() != 0)
        {
          String cmd = mQueuedCommand;
          boolean autoReListen = mQueuedAutoReListen;
          mQueuedCommand = "";
          mQueuedAutoReListen = false;
          doVNCServerConnect(cmd, autoReListen, true);

        }
    }

    private void reset() {
        reset(true);
    }

    private void reset(boolean waitForFlush) {
        mListening = false;
        mCurrentState = new VncServerState(VncServerMainState.DISCONNECTED);
        mServer.reset(waitForFlush);
        checkQueuedCommand();
    }

    private void startSelf() {
        final Intent intent = new Intent(
                this,
                VncServerService.class);
        intent.setAction(SampleIntents.RUN_SERVER_INTENT);
        intent.setPackage(getPackageName());
        NotificationHelper.ServiceUtils
                .startForegroundServiceWithIntent(this, intent);
    }

    /**
     * An object which allows us to make asynchronous callbacks into
     * the UI thread from other threads. We will use this to update
     * the status message.
     */
    private final Handler mHandler = new Handler();

    public void toast(final String message) {
        mHandler.post(new Runnable() {
                public void run() {
                    Toast.makeText(VncServerService.this, message, Toast.LENGTH_LONG).show();
                }
            });
    }

    public void installationResult(int resultCode) {
        mTryingToInstallRcs = false;
        if(resultCode == VncServerCoreErrors.VNCSERVER_ERR_NONE) {
            mAttemptedRcsInstall = true;
            try {
                mServer.checkRemoteControlAvailable();
                return;
            } catch (VncException e) {
                resultCode = e.errorCode;
                /* Handle this error with all other error below... */
            }
        }
        if(resultCode == VncServerCoreErrors.VNCSERVER_ERR_UNABLE_TO_START_SERVICE ||
           resultCode == VncServerCoreErrors.VNCSERVER_ERR_NO_SUITABLE_RCS)
            /* Don't attempt to relisten if installation failed. */
            mCmd = null;
        if(mCurrentState.getState() != VncServerMainState.DISCONNECTED)
            reset();
        errorCb(null, resultCode, null);
    }

    public void accessoryDetached() {
        try {
            if ((mCmd != null &&
                            mCmd.getString(VncCommandString.TYPE).equals("AAP")) ||
                    (mListeningCommand != null &&
                            mListeningCommand.getString(VncCommandString.TYPE).equals("AAP"))) {
                reset();
                mCmd = null;
                mListening = false;
                mAutoReListen = false;
                mListeningCommand = null;
                errorCb(null,
                        VncServerCoreErrors.VNCSERVER_ERR_USB_NOT_CONNECTED,
                        null);
            }
        } catch (VncException e) {
            LOG.log(Level.WARNING,
                    "Failed to shutdown while handling accessory removal", e);
        }
    }

    @Override
    public void advertiserStopped(int error) {
        if (error != VNCNetworkAdvertiserException.STOPPED) {
            toast(getResources().getString(R.string.SS_03_262,
                                           error));
        }
    }
}
