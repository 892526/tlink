/* Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of a
 * VNC Automotive SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.androidsampleserver.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.util.Log;

import com.realvnc.androidsampleserver.SampleIntents;
import com.realvnc.androidsampleserver.NotificationHelper;
import com.realvnc.androidsampleserver.VncUsbState;
import com.realvnc.androidsampleserver.activity.VNCMobileServer;
import com.realvnc.androidsampleserver.service.VncServerService;

public class USBReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "VNC-USB";

    /* These constants are marked '@hide' in the ConnectivityManager,
     * so we shouldn't rely on them working on newer OS versions */
    public static final String ACTION_TETHER_STATE_CHANGED =
            "android.net.conn.TETHER_STATE_CHANGED";

    private static final int TOGGLE_WAIT_TIME = 600;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        VncUsbState.AutoTetherStatus usbAutoconnectOption = VncUsbState.getUsbAutoConnect(context);
        String bearer = VncUsbState.getUsbBearer(context);
        if (!bearer.equals(VncUsbState.TETHERED_BEARER_TYPE)) {
            // If the bearer isn't the tethering bearer then don't consider
            // automatically enabling tethering.
            usbAutoconnectOption = VncUsbState.AutoTetherStatus.VNC_USB_AUTOCONNECT_OFF;
        }

        if (action.equals(ACTION_TETHER_STATE_CHANGED) || action.equals(Intent.ACTION_BOOT_COMPLETED)) {

            if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                // Reset state to NOT_WAITING. If a USB interface is available (and usb-autoconnect is 
                // enabled), then we shall restart tethering with our call to VncUsbState.startUsbTethering().
                VncUsbState.setStage(context, VncUsbState.Stage.NOT_WAITING);
            }

            // Don't trust the extra's as this broadcast might not have come from
            // a trusted source. We may also have received the ACTION_BOOT_COMPLETED
            // intent rather than ACTION_TETHER_STATE_CHANGED.
            ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

            String[] available = VncUsbState.getAvailableTetheredInterfaces(cm);
            String[] active = VncUsbState.getActiveTetheredInterfaces(cm);
            String[] errored = VncUsbState.getErroredTetheredInterfaces(cm);

            VncUsbState.Stage stage = VncUsbState.getStage(context);
            Log.i(LOG_TAG, "Tether: " + available.length +
                    ":" + active.length + ":" + errored.length +
                    " stage: " + stage
                  );

            if(!VncUsbState.hasUsbInterface(active, context)) {
                /* The USB cable has been unplugged or tethering is not enabled.
                 * For some reason we don't get a connectivity change
                 * event for this, so reset the server here if it's waiting
                 * for a disconnect. */

                if(stage == VncUsbState.Stage.WAITING_FOR_DISCONNECT) {
                    Log.i(LOG_TAG, "Stopping VNC Automotive server");
                    resetServer(context);
                }
                String usbBearer = VncUsbState.getUsbBearer(context);
                if(stage == VncUsbState.Stage.TOGGLING_TETHERING_OFF &&
                        usbBearer.equals(VncUsbState.AAP_BEARER_TYPE)) {
                    Log.i(LOG_TAG, "Tethering toggled off, re-listening");
                    Intent i = new Intent(context, VncServerService.class);
                    i.setAction(SampleIntents.START_SERVER_INTENT);
                    i.setData(Uri.parse("vnccmd:v=1;t=AAP"));
                    i.setPackage(context.getPackageName());
                    NotificationHelper.ServiceUtils
                            .startForegroundServiceWithIntent(context, i);
                }

                if(!(stage.mSwitchingOn &&
                            !VncUsbState.hasUsbInterface(available, context)))
                    /* Only clear the stage if not waiting for an active tether
                     * and the USB interface isn't available as it could be taking
                     * a while to come up and this is just a notification with
                     * the usb interface in neither active or available.
                     * These notifications occur when a USB interface is switching
                     * between available and active.
                     * Otherwise we'd forget to ask the server to listen. */
                    VncUsbState.setStage(context, VncUsbState.Stage.NOT_WAITING);

            } else {
                /* Tethering is active on USB, see if we should listen */
                if(stage == VncUsbState.Stage.WAITING_FOR_ACTIVE_TETHER) {
                    /* Was waiting for tethering to become active, so
                     * now ask the server to listen */
                    Log.i(LOG_TAG, "Starting VNC Automotive server");
                    Intent i = new Intent(context, VncServerService.class);
                    i.setAction(SampleIntents.START_SERVER_INTENT);
                    i.setData(Uri.parse("vnccmd:v=1;t=USB"));
                    i.setPackage(context.getPackageName());
                    NotificationHelper.ServiceUtils
                            .startForegroundServiceWithIntent(context, i);
                    VncUsbState.setStage(context,
                            VncUsbState.Stage.WAITING_FOR_DISCONNECT);
                }
                if(stage == VncUsbState.Stage.TOGGLING_TETHERING_ON) {
                    /* In the middle of toggling tethering, so switch off */
                    /* First sleep for a bit so the USB state doesn't
                     * change while the host is attempting to
                     * configure this device. */
                    synchronized(this) {
                        try {
                            wait(TOGGLE_WAIT_TIME);
                        } catch (InterruptedException ie) {
                            // Ignore
                        }
                    }
                    Log.i(LOG_TAG, "Tethering toggled on, disabling");
                    VncUsbState.setStage(context, 
                            VncUsbState.Stage.TOGGLING_TETHERING_OFF);
                    VncUsbState.stopUsbTethering(context);
                }
            }
            if(VncUsbState.hasUsbInterface(available, context)) {

                for(String iface : active) {
                    Log.i(LOG_TAG, "Tether: active " + iface);
                }

                for(String iface : errored) {
                    Log.i(LOG_TAG, "Tether: errored " + iface);
                }

                if(stage == VncUsbState.Stage.WAITING_FOR_DISCONNECT) {
                    /* User has manually disabled USB tethering */
                    Log.i(LOG_TAG, "Stopping VNC Automotive server");
                    resetServer(context);
                    VncUsbState.setStage(context, VncUsbState.Stage.NOT_WAITING);
                } else if(usbAutoconnectOption == VncUsbState.AutoTetherStatus.VNC_USB_AUTOCONNECT_ASK) {
                    Log.i(LOG_TAG, "Starting USB Choice dialog");
                    Intent i = new Intent(context, VNCMobileServer.class);
                    i.setAction(SampleIntents.USB_CHOICE_DIALOG_INTENT);
                    i.setPackage(context.getPackageName());
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                } else if (usbAutoconnectOption == VncUsbState.AutoTetherStatus.VNC_USB_AUTOCONNECT_ON) {
                    VncUsbState.setStage(context,
                            VncUsbState.Stage.WAITING_FOR_ACTIVE_TETHER);
                    VncUsbState.startUsbTethering(context);
                }
            }

        } else {
            Log.i(LOG_TAG, "Bad intent: " + intent.getAction());
        }
    }

    private void resetServer(Context context) {
        Intent i = new Intent(context, VncServerService.class);
        i.setAction(SampleIntents.RESET_SERVER_INTENT);
        i.putExtra(SampleIntents.RESET_SERVER_WAIT_FOR_FLUSH_EXTRA, false);
        i.setPackage(context.getPackageName());
        NotificationHelper.ServiceUtils
                .startForegroundServiceWithIntent(context, i);
    }
}
