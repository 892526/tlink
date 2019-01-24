/* Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of a
 * VNC Automotive SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.androidsampleserver.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.realvnc.androidsampleserver.SampleIntents;
import com.realvnc.androidsampleserver.VncServerApp;
import com.realvnc.androidsampleserver.VncUsbState;

import java.lang.reflect.Field;

// We could use
//   import com.android.future.usb.UsbManager;
// to import the USBManager code, but then this code wouldn't compile
// without having the Google API plugins installed. Instead we
// use reflection as only the UsbManager.ACTION_USB_ACCESSORY_ATTACHED
// value is required.

/**
 * This class is a proxy to the the main GUI view. 
 * The reason the main view activity isn't exported is for security.
 * Many of the intents that the main GUI can perform should only be
 * allowed to be called from code within this package.
 * 
 * This class therefore acts as a proxy to pass publicly accessible
 * intents along to the main GUI without exposing other functionality.
 *
 */
public class UsbAccessoryProxy extends Activity {

    private static final String TAG = "UsbAccessoryProxy";

    private static String getUsbAccessoryAttachedAction() {
        String ret = null;
        try {
            // First attempt to use the core API added in Honeycomb
            Class<?> usbManagerClass = Class.forName("android.hardware.usb.UsbManager");
            Field field = usbManagerClass.getDeclaredField("ACTION_USB_ACCESSORY_ATTACHED");
            Object obj = field.get(null);
            if(obj instanceof String)
                ret = (String) obj;
        }
        catch (Exception honeycombException) {
            // Fall back to Gingerbread Google Add-on API implementation
            try {
                Class<?> usbManagerClass = Class.forName("com.android.future.usb.UsbManager");
                Field field = usbManagerClass.getDeclaredField("ACTION_USB_ACCESSORY_ATTACHED");
                Object obj = field.get(null);
                if(obj instanceof String)
                    ret = (String) obj;
            } catch (Exception e) {
                Log.v(TAG, "Exception when trying to find UsbManager class");
            }
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onDestroy()
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent i) {
        super.onNewIntent(i);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (VncUsbState.getUsbAutoConnect(this) != VncUsbState.AutoTetherStatus.VNC_USB_AUTOCONNECT_OFF) {
            takeAction();
        }
        // Always finish as this activity uses the hidden style
        finish();
    }

    private void takeAction() {
        // Don't try to redo the action if relaunched from history
        String intent = getIntent().getAction();
        final int flags = getIntent().getFlags();
        final boolean usingAAP = 
        VncUsbState.getUsbBearer(this).equals(VncUsbState.AAP_BEARER_TYPE);
        if ((flags & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0 &&
            (intent.equals(getUsbAccessoryAttachedAction()) ||
             intent.equals(SampleIntents.USB_ACCESSORY_ATTACHED))) {
            if (usingAAP) {
                VncServerApp serverApp = (VncServerApp) getApplication();
                serverApp.startAapDelayed();
            } else {
                /* Display a warning dialog about AAP being disabled */
                Intent i = new Intent(this, VNCMobileServer.class);
                i.setAction(SampleIntents.AAP_NOT_CHOSEN_DIALOG_INTENT);
                i.setPackage(getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        }
    }
}

