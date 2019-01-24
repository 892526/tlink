/* Copyright (C) 2002-2018 RealVNC Ltd. All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of the
 * VNC Mobile Solution SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.androidsampleserver;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.realvnc.androidsampleserver.service.VncServerService;
import com.realvnc.vncserver.core.VncAuthType;

public class VncServerApp extends Application {
    private static final String TAG = "VncServerApp";

    private static final String PREFERENCES_NAME = "TLINK_PREFERENCES";
    private static final String KEY_AGREEMENT = "agreement";

    /* Some devices fail to connect if attempting to open the
     * accessory too soon after being notified by the OS that it's
     * available. */

    private static final int AAP_OPEN_DELAY = 3000;

    private static VncServerApp thiz = null;

    private String mPreviousConnect;
    private String mPreviousCmdString;

    private Handler mHandler;
    private Runnable mDelayedAapConnect = new Runnable() {
            public void run() {
                Intent i = new Intent(VncServerApp.this,
                        VncServerService.class);
                i.setData(Uri.parse("vnccmd:v=1;t=AAP"));
                i.setAction(SampleIntents.START_SERVER_INTENT);
                i.setPackage(getPackageName());
                NotificationHelper.ServiceUtils
                        .startForegroundServiceWithIntent(VncServerApp.this, i);
            }
        };

    @Override
    public void onCreate() {
        super.onCreate();
        // Need to make sure that clipboard support defaults to off
        // on Android 4.3 devices prior to 4.3_r2.2. The reasons for
        // this are described in:
        //
        // https://code.google.com/p/android/issues/detail?id=58043
        //
        // The internal RealVNC reference for this is MOB-9408
        //
        // Testing for the exact "4.3" string is working on the
        // assumption that if "4.3.1" is ever released that it will
        // contain the fix present in 4.3_r2.2 of the Android Open Source
        // Project.
        thiz = this;

        if (android.os.Build.VERSION.RELEASE.equals("4.3")) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            if (!prefs.contains("vnc_clipboard")) {
                SharedPreferences.Editor edit = prefs.edit();
                edit.putBoolean("vnc_clipboard", false);
                if (edit.commit()) {
                    Log.w(TAG,
                          "Disabling clipboard support due to AOSP bug 58043");
                } else {
                    Log.e(TAG,
                          "Failed to disable clipboard support");
                }
            }
        }

        if (NotificationHelper.createNotificationChannel(
                getApplicationContext())) {
            Log.i(TAG, "Notification channel created");
        }
    }

    public String GetPreviousConnect() {
        return mPreviousConnect;
    }

    public void SetPreviousConnect(String newConnect) {
        mPreviousConnect = newConnect;
    }

    public String GetPreviousCmdString() {
        return mPreviousCmdString;
    }

    public void SetPreviousCmdString(String newCmdString) {
        mPreviousCmdString = newCmdString;
    }

    /* Launch an AAP connection after a suitable delay.
     *
     * This method is in the Application as it needs to live for a
     * longer time than the UsbAccessoryProxy and also needs to cancel
     * any previous delayed attempt before starting the next one.
     */
    public synchronized void startAapDelayed() {
        if (mHandler == null) {
            mHandler = new Handler(getMainLooper());
        }
        /* Some devices deliver multiple accessory attached
         * notifications, so cancel any pending action. */
        mHandler.removeCallbacks(mDelayedAapConnect);
        mHandler.postDelayed(mDelayedAapConnect, AAP_OPEN_DELAY); 
    }


    public static Context getContext() {
        if (thiz != null) {
            return thiz.getApplicationContext();
        }
        return null;
    }

    public static Boolean isAgreement() {
        if (thiz != null) {
            SharedPreferences sharedPreferences = thiz.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
            return sharedPreferences.getBoolean(KEY_AGREEMENT, false);
        }
        return false;
    }

    public static void doAgreement() {
        if (thiz != null) {
            SharedPreferences.Editor editor = thiz.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
            editor.putBoolean(KEY_AGREEMENT, true).apply();
        }
    }
}
