/* Copyright (C) 2002-2018 RealVNC Ltd. All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of the
 * VNC Mobile Solution SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.androidsampleserver.receiver;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.realvnc.androidsampleserver.R;

/**
 * Device admin class. Picks up when remote control is enabled.
 */
public class RemoteControlDeviceAdminReceiver extends DeviceAdminReceiver {
    private static final String TAG = "RCDeviceAdminReceiver";

    @Override
    public void onEnabled(Context context, Intent intent) {
        Log.i(TAG, "Device admin enabled");
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return context.getString(R.string.admin_disable_warning);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        Log.i(TAG, "Device admin disabled");
    }
}
