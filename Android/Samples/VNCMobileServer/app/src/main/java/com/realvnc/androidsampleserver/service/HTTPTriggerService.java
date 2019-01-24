/* Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of a
 * VNC Automotive SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.androidsampleserver.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.realvnc.androidsampleserver.IVncServerInterface;
import com.realvnc.androidsampleserver.IVncServerListener;
import com.realvnc.androidsampleserver.R;
import com.realvnc.androidsampleserver.SampleIntents;
import com.realvnc.androidsampleserver.NotificationHelper;
import com.realvnc.androidsampleserver.VncConfigFile;
import com.realvnc.androidsampleserver.activity.VNCMobileServer;
import com.realvnc.vncserver.android.VncCommandString;
import com.realvnc.vncserver.core.VncException;
import com.realvnc.vncserver.core.VncServerCoreErrors;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/** HTTP trigger service
 *
 * This service is responsible for connecting to VNC Automotive Bridge,
 * retrieving a command string, prompting as needed, and passing it to
 * the server SDK to be actioned.
 *
 * This is meant to be a fairly literal interpretation of the flow
 * chart on the Mobile_VNC_Project/VNC_Command_String_Interactions
 * page of the wiki. Comments starting with '@' refer to the labels on
 * this flow chart.
 *
 * The main difference is that we don't wait for the user to do
 * anything after displaying the disconnect notification, because
 * Android's notifier mechanism doesn't work like a normal popup
 * dialog. */

public class HTTPTriggerService extends Service {
    private static final Logger LOG = Logger.getLogger(HTTPTriggerService.class.getName());
    private static final String TAG = "VNCTriggerService";

    private static final String COMMAND_ID_KEY = "-cid";
    private static final int MAXIMUM_COMMAND_ID_LENGTH = 10;

    private Intent mPostponedIntent = null;

    private NotificationManager mNotificationManager;

    private static final int FOREGROUND_SERVICE_NOTIFICATION_ID =
            NotificationHelper.UniqueIdGenerator.generate();

    private static final int CONNECTION_NOTIFICATION_ID =
            NotificationHelper.UniqueIdGenerator.generate();

    /** HTTP transfer
     *
     * This object performs a HTTP POST to the given URL and handles
     * the returned data. */
    private abstract class HTTPTransfer extends Thread
    {
        private String mUrl;
        private String mParams;

        public HTTPTransfer(String url) {
            mUrl = url;
        }

        public void setParameters(String params) {
            mParams = params;
        }

        public abstract void transferError(VncException error);
        public abstract void transferComplete(String data);

        public void run() {

            HttpURLConnection conn = null;

            try {

                URL url = new URL(mUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(2000);
                conn.setConnectTimeout(2000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(mParams);
                writer.flush();
                writer.close();
                os.close();

                // Execute the fetch and retrieve the result.
                conn.connect();
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String responseBody = reader.readLine();
                reader.close();
                is.close();

                /* If we get here then the transfer was successful.
                 * Note that if this is a status report then the
                 * reponse will be empty. */
                transferComplete(responseBody);

            } catch(Exception e) {
                LOG.log(Level.SEVERE, "HTTP transfer error", e);
                transferError(new VncException(VncServerCoreErrors.VNCSERVER_ERR_COMMAND_FETCH_FAILED));
            } finally {
                if (conn != null) conn.disconnect();
            }
        }
    }

    private boolean promptingUser;
    private boolean needReset;

    /** Trigger class
     *
     * This encapsulates all the state associated with a single
     * trigger event, from the initial command string fetch and prompt
     * through the status report which gets passed back to the
     * controlling app and finally the disconnection prompt */
    private class Trigger
    {
        private String mCommand;
        private String mCmdId;

        class CommandStringFetch extends HTTPTransfer {

            /*@ Retrieve new Command String */
            public CommandStringFetch(String url) {
                super(url);

                mCmdId = "?";

                /* Build the list of parameters to pass to the
                 * server. This just contains the device identifier
                 * (IMEI number). */
                Uri.Builder builder = new Uri.Builder();
                builder.appendQueryParameter("devid", mDeviceId);

                setParameters(builder.build().getEncodedQuery());
            }

            /*@ Retrieved -> Validate new command string */
            public void transferComplete(String data) {
                Log.println(Log.INFO, TAG, "transferComplete '" + data + "'");

                mCommand = data;

                try {
                    VncCommandString cmd = new VncCommandString();
                    cmd.parse(data);
                    if (cmd.parameterPresent(COMMAND_ID_KEY)) {
                        mCmdId = cmd.getString(COMMAND_ID_KEY);
                        if(mCmdId == null ||
                                mCmdId.length() > MAXIMUM_COMMAND_ID_LENGTH)
                            throw new VncException(VncServerCoreErrors.VNCSERVER_ERR_INVALID_COMMAND_STRING);
                    } else {
                        mCmdId = "?";
                    }

                } catch(VncException e) {
                    /*@ Invalid -> Report status */
                    Log.println(Log.ERROR, TAG, "exception: " + e);
                    transferError(e);
                    return;
                } catch(IllegalArgumentException e) {
                    /* No command ID field present - that's OK,
                     * we can continue without one */
                }

                commandStringRetrieved();
            }

            /*@ Failed -> Report status via HTTP for new command *
             *  string */
            public void transferError(VncException e) {
                startStatusReport(e.errorCode, e.getMessage());
                try {
                    mServiceInterface.VNCServerSetError(e.errorCode);
                } catch (RemoteException e2) {
                    LOG.log(Level.SEVERE, "Failed to set error code: exception", e2);
                }
            }
        }

        class StatusReport extends HTTPTransfer {
            public StatusReport(String url, int errorCode, String sysError) {
                super(url);

                Uri.Builder builder = new Uri.Builder();
                builder.appendQueryParameter("devid", mDeviceId);
                builder.appendQueryParameter("error", Integer.toString(errorCode));
                builder.appendQueryParameter("syserror", sysError);
                builder.appendQueryParameter("platform", "Android");

                try {
                    builder.appendQueryParameter("version", mServiceInterface.VNCServerGetVersionString());
                } catch(RemoteException e) {
                    LOG.log(Level.WARNING, "Failed to get version string: exception", e);
                }

                if(mCmdId != null) {
                    builder.appendQueryParameter("cmdid", mCmdId);
                }

                setParameters(builder.build().getEncodedQuery());
            }

            public void transferComplete(String data) {
                Log.println(Log.INFO, TAG, mCmdId + ": status report complete");
            }

            public void transferError(VncException e) {
                // HTTP status report failed. Not much we can do to recover.
                Log.println(Log.ERROR, TAG, mCmdId + ": transferError");
            }
        }

        private String mDeviceId;
        private CommandStringFetch mCommandFetch;
        private StatusReport mStatusReport;

        /*@ "SMS Trigger Received" */
        void start() {

            try {

                TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                //noinspection HardwareIds
                mDeviceId = tm.getDeviceId();

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                String url = prefs.getString("url_connection", "");

                LOG.info("Fetching command via HTTP from '" + url + "'");

                if("".equals(url)) throw new VncException(VncServerCoreErrors.VNCSERVER_ERR_COMMAND_FETCH_FAILED);

                mCommandFetch = new CommandStringFetch(url);
                mCommandFetch.start();
            } catch(SecurityException e) {
                LOG.log(Level.SEVERE, "Failed to initiate command string fetch: exception", e);
                toast(e.getMessage());
                startStatusReport(VncServerCoreErrors.VNCSERVER_ERR_INTERNAL_ERROR,
                                  e.getMessage());
            } catch(Exception e) {
                LOG.log(Level.SEVERE, "Failed to initiate command string fetch: exception", e);
                toast(e.getMessage());
                startStatusReport(VncServerCoreErrors.VNCSERVER_ERR_INTERNAL_ERROR,
                                  e.getMessage());
            }
        }

        void startStatusReport(int vncError, String platformError) {

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            String url = prefs.getString("url_logging", "");

            LOG.info(mCmdId + ": Reporting trigger status: " + Integer.toString(vncError) + " '" + platformError + "'");

            if("".equals(url)) {
                Log.println(Log.INFO, TAG, "No logging URL set - not reporting status");
            } else {
                mStatusReport = new StatusReport(url, vncError, platformError);
                mStatusReport.start();
            }
        }
    }

    private Trigger mNewTrigger;
    private Trigger mPromptTrigger;
    private Trigger mAcceptedTrigger;

    /* Parameters for the connection accept/reject notifier */
    private static final long[] mVibrationPattern = { 1500, 250 };

    private void commandStringRetrieved() {

        /*@ Prompt active? */
        if(mPromptTrigger != null) {
            /*@ Report status via HTTP for prompt command string */

            mPromptTrigger.startStatusReport(VncServerCoreErrors.VNCSERVER_ERR_COMMAND_SUPERSEDED,
                                             null);
            mPromptTrigger = null;

            /*@ Dismiss active prompt - since we're about to display a
             *  new one, and since the prompts are always the same as
             *  each other, we don't actually do anything here.
             *  However if we allowed the management system to
             *  customise the prompts remotely, we'd have to make sure
             *  the Activity was capable of updating itself on
             *  receiving new prompt text. */
        }

        /*@ Move new command string into prompt command string */
        mPromptTrigger = mNewTrigger;
        mNewTrigger = null;

        LOG.info(mPromptTrigger.mCmdId + ": Prompting user");

        showAcceptNotification();
    }

    /*@ Prompt for connection */
    private void showAcceptNotification() {
        final Context context = getApplicationContext();
        final Intent acceptIntent = new Intent(
                this,
                VNCMobileServer.class);
        acceptIntent.setAction(SampleIntents.HTTP_ACCEPT_DIALOG_INTENT);
        acceptIntent.setPackage(getPackageName());
        acceptIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

        final Notification notification =
                NotificationHelper.getNotificationBuilder(context)
                        .setContentTitle(getResources().getString(
                                R.string.http_accept_notifier_title))
                        .setContentText(getResources().getString(
                                R.string.http_accept_notifier_text))
                        .setContentIntent(PendingIntent.getActivity(
                                this,
                                0,
                                acceptIntent,
                                PendingIntent.FLAG_CANCEL_CURRENT))
                        .setTicker(getResources().getString(
                                R.string.http_accept_notifier_ticker))
                        .setSmallIcon(R.drawable.vncicon)
                        .setDefaults(
                                Notification.DEFAULT_SOUND
                                        | Notification.DEFAULT_LIGHTS)
                        .setVibrate(mVibrationPattern)
                        .setOngoing(true)
                        .getNotification();

        notification.flags |= Notification.FLAG_INSISTENT;

        mNotificationManager.notify(
                HTTPTriggerService.CONNECTION_NOTIFICATION_ID,
                notification);

        promptingUser = true;
        needReset = true;

        try {
            mServiceInterface.VNCServerRequestDialog();
        } catch(RemoteException e) {
            // This error is not fatal - the user can still get to the
            // dialog box
            LOG.log(Level.WARNING, "Failed to request dialog: exception", e);
        }

        /* we continue in acceptTrigger() or rejectTrigger() below */
    }

    public void acceptTrigger() {
        LOG.info("acceptTrigger");

        try {
            mServiceInterface.VNCServerClearRequestDialog();
        } catch(RemoteException e) {
            // This error is not fatal - the user can still get to the
            // dialog box
            LOG.log(Level.WARNING, "Failed to request dialog: exception", e);
        }
        hideNotification();

        try {

            /*@ Connection active? */
            if(needReset) {

                /*@ Disconnect previous session */
                needReset = false;
                mServiceInterface.VNCServerReset();

                /* When reset is complete we re-enter acceptTrigger()
                 * from disconnectedCb() and continue below */
                return;
            }

            promptingUser = false;

            /*@ Move "prompt command string" into "accepted command string" */

            mAcceptedTrigger = mPromptTrigger;
            mPromptTrigger = null;

            String data = mAcceptedTrigger.mCommand;
            LOG.info(mAcceptedTrigger.mCmdId + ": Acting on command: '" + data + "'");

            /*@ Connect via data relay */
            mServiceInterface.VNCServerConnect(data, false);

            /* we continue in the callbacks in TriggerListener below */

        } catch(RemoteException e) {
            LOG.log(Level.WARNING, "Failed to initiate connection: exception", e);

            /*@ Connection Fails */

            /*@ Show disconnect prompt */
            showDisconnectNotification();

            /*@ Report status via HTTP for accepted command string */
            mAcceptedTrigger.startStatusReport(VncServerCoreErrors.VNCSERVER_ERR_INTERNAL_ERROR,
                                               e.getMessage());
        }

    }

    public void rejectTrigger() {
        try {
            mServiceInterface.VNCServerClearRequestDialog();
        } catch(RemoteException e) {
            // This error is not fatal - the user can still get to the
            // dialog box
            LOG.log(Level.WARNING, "Failed to request dialog: exception", e);
        }
        hideNotification();

        /*@ Report status via HTTP for prompt command string */

        promptingUser = false;
        mPromptTrigger.startStatusReport(VncServerCoreErrors.VNCSERVER_ERR_USER_REFUSED_CONNECTION, null);
        mPromptTrigger = null;
    }

    private void showDisconnectNotification() {
        final Context context = getApplicationContext();
        final Intent disconnectIntent = new Intent(
                this,
                VNCMobileServer.class);
        disconnectIntent.setAction(SampleIntents.HTTP_DISCONNECT_DIALOG_INTENT);
        disconnectIntent.setPackage(getPackageName());

        final Notification notification =
                NotificationHelper.getNotificationBuilder(context)
                        .setContentTitle(getResources().getString(
                                R.string.http_disconnect_notifier_title))
                        .setContentText(getResources().getString(
                                R.string.http_disconnect_notifier_text))
                        .setContentIntent(PendingIntent.getActivity(
                                this,
                                0,
                                disconnectIntent,
                                0))
                        .setTicker(getResources().getString(
                                R.string.http_disconnect_notifier_ticker))
                        .setSmallIcon(R.drawable.vncicon)
                        .setAutoCancel(true)
                        .getNotification();

        mNotificationManager.notify(
                HTTPTriggerService.CONNECTION_NOTIFICATION_ID,
                notification);
    }

    private void hideNotification() {
        mNotificationManager.cancel(
                HTTPTriggerService.CONNECTION_NOTIFICATION_ID);
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
                HTTPTriggerService.FOREGROUND_SERVICE_NOTIFICATION_ID,
                getString(R.string.vnc_automotive_bridge),
                getString(R.string.SS_02_217),
                R.drawable.vncicon);

        VncConfigFile.checkVncConfig(getBaseContext());

        /* Bind to the VNC Automotive server */

        mService = new ServerServiceConnection();
        Intent serviceIntent = new Intent(this, VncServerService.class);
        serviceIntent.setAction(SampleIntents.BIND_SERVICE_INTENT);
        serviceIntent.setPackage(getPackageName());
        bindService(serviceIntent, mService,
                    BIND_AUTO_CREATE);

    }

    @Override
    public synchronized void onDestroy() {
        try {
            mServiceInterface.unregisterListener(mListener);
        } catch (RemoteException e) {
            LOG.log(Level.WARNING, "Failed to unregister listener: exception", e);
        }
        unbindService(mService);
    }

    public synchronized int onStartCommand(Intent intent, int flags, int startId) {
        LOG.setLevel(Level.INFO);

        if(mServiceInterface == null) {
            // The VNC Automotive server hasn't started yet. Postpone the action
            // until the service is bound.
            mPostponedIntent = intent;
        } else {
            handleIntent(intent);
        }

        return Service.START_STICKY_COMPATIBILITY;
    }

    private synchronized void handleIntent(Intent intent) {

        String action = intent.getAction();

        if(action.equals(SampleIntents.HTTP_TRIGGER_INTENT)) {

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

            if(prefs.getBoolean("vnc_sms_listener", true)) {
                mNewTrigger = new Trigger();
                mNewTrigger.start();
            } else {
                LOG.info("Ignoring SMS trigger - listener is disabled");
            }

        } else if(action.equals(SampleIntents.HTTP_TRIGGER_ACCEPT_INTENT)) {

            // Note that if we get restarted by the OS, eg after a
            // crash, we might not have a "prompt command string" to
            // act on.

            if(mPromptTrigger != null) {
                LOG.info(mPromptTrigger.mCmdId + ": Accepting HTTP trigger");
                acceptTrigger();
            } else LOG.info("Restarting after crash? Ignoring");

        } else if(action.equals(SampleIntents.HTTP_TRIGGER_REJECT_INTENT)) {

            if(mPromptTrigger != null) {
                LOG.info(mPromptTrigger.mCmdId + ": Rejecting HTTP trigger");
                rejectTrigger();
            } else LOG.info("Restarting after crash? Ignoring");

        } else {

            LOG.info("Unknown action: " + action);

        }
    }

    @Override
    public synchronized IBinder onBind(Intent intent) {
        Log.println(Log.INFO, TAG, "onBind " + intent);
        return null;
    }

    private class ServerServiceConnection implements ServiceConnection {

        public void onServiceConnected(ComponentName name, IBinder service) {

            mServiceInterface = IVncServerInterface.Stub.asInterface(service);

            try {
                mListener = new TriggerListener();
                mServiceInterface.registerListener(mListener);
            } catch (RemoteException e) {
                LOG.log(Level.WARNING, "Failed to register listener: exception", e);
            }

            if(mPostponedIntent != null) {
                Intent i = mPostponedIntent;
                mPostponedIntent = null;

                handleIntent(i);
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            LOG.info("Service unbound");
            mServiceInterface = null;
        }
    }

    private ServerServiceConnection mService;
    private IVncServerInterface mServiceInterface;

    private class TriggerListener extends IVncServerListener.Stub
    {
        /* Callbacks from the VNC Automotive server - most of which we don't use */
        @Override
        public void listeningCb(String ipAddresses) {}

        @Override
        public void connectingCb() {
            LOG.info(getId() + ": connectingCb");
        }

        @Override
        public void connectedCb(String ipAddress) {
            LOG.info(getId() + ": connectedCb");
        }

        /*@ Connection succeeds */
        @Override
        public void runningCb() {

            LOG.info(getId() + ": runningCb");

            needReset = true;

            if(mAcceptedTrigger != null) {
                /*@ Report status via HTTP for accepted command string */
                mAcceptedTrigger.startStatusReport(0, null);
            }
        }

        @Override
        public void disconnectedCb() {
            LOG.info("disconnectedCb");

            if(promptingUser) {
                if(!needReset) {

                    /*@ completion of "Disconnect previous session" */
                    mAcceptedTrigger = null;
                    acceptTrigger();

                } else {
                    /*@ Disconnection occurs */

                    needReset = false;

                    /*@ Show disconnect prompt */
                    showDisconnectNotification();
                    mAcceptedTrigger = null;

                }
            } else {
                /* Be sure to forget any trigger that might have been
                 * waiting to report any error. */
                mAcceptedTrigger = null;
            }
        }

        /*@ Connection fails */
        @Override
        public void errorCb(int errorCode) {
            hideNotification();

            /*@ Report status via HTTP for accepted command string */
            if(mAcceptedTrigger != null) {
                /*@ Show disconnect prompt */
                showDisconnectNotification();

                mAcceptedTrigger.startStatusReport(errorCode, null);
                mAcceptedTrigger = null;
            }

            // If we got an error while prompting the user, that means
            // the remote control API is unavailable, or failed to
            // install. Report this to the server.
            if(promptingUser) {
                promptingUser = false;
                mPromptTrigger.startStatusReport(errorCode, null);
                mPromptTrigger = null;
            }
        }

        @Override
        public void keygenCb(byte[] keyPair) {}

        @Override
        public void authCb(String user, String pass) {}

        @Override
        public void loginCb(boolean userReq, boolean passReq) {}

        @Override
        public void updateUiCb() {}

        public String getId() {
            if(mAcceptedTrigger != null) {
                return mAcceptedTrigger.mCmdId;
            }
            return "?";
        }
    }

    private TriggerListener mListener;

    /**
     * An object which allows us to make asynchronous callbacks into
     * the UI thread from other threads. We will use this to update
     * the status message.
     */
    private final Handler mHandler = new Handler();

    private void toast(final String message) {
        mHandler.post(new Runnable() {
                public void run() {
                    Toast.makeText(HTTPTriggerService.this, message, Toast.LENGTH_LONG).show();
                }
            });
    }
}

