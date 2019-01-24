/* Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of a
 * VNC Automotive SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.androidsampleserver.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.realvnc.androidsampleserver.R;
import com.realvnc.androidsampleserver.VncUsbState;
import com.realvnc.androidsampleserver.receiver.USBReceiver;

public class VncServerUsbChoiceDialog extends VncServerDialog {
    boolean mReplyPending;
    BroadcastReceiver mReceiver;

    VncServerUsbChoiceDialog(VNCMobileServer ctx) {
        super(ctx, false);

        setTitle(R.string.accept_usb_choice_dialog_title);
        setContentView(R.layout.usb_choice_dialog);
    }

    void setArgs(Bundle args) {
        TextView text;

        text = (TextView) findViewById(R.id.accept_usb_choice_dialog_text);
        text.setText(mCtx.getResources().getString(
                                R.string.accept_usb_choice_dialog_text));

        Button button;

        button = (Button) findViewById(R.id.accept_usb_choice_dialog_accept);
        button.setOnClickListener(onAccept);

        button = (Button) findViewById(R.id.accept_usb_choice_dialog_reject);
        button.setOnClickListener(onReject);
    }

    private View.OnClickListener onAccept = new View.OnClickListener() {
            public void onClick(View v) {
                sendChoiceResult(true);
                dismiss();
            }
        };

    private View.OnClickListener onReject = new View.OnClickListener() { 
            public void onClick(View v) {
                sendChoiceResult(false);
                dismiss();
            }
        };

    @Override
    public void onStart() {
        mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {   
                    String action = intent.getAction(); 
                    if(!action.equals(USBReceiver.ACTION_TETHER_STATE_CHANGED)) {
                        return;
                    }
                    // Don't trust the extra's as this broadcast might not have come from
                    // a trusted source.
                    ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    String[] available =
                        VncUsbState.getAvailableTetheredInterfaces(cm);

                    if(!VncUsbState.hasUsbInterface(available, context)) {
                        /* Usb tethering interface has gone, so
                         * dismiss the dialog as it's now meaningless. */
                        sendChoiceResult(false);
                        dismiss();
                    }
                    
                }
            };
        IntentFilter filter = new IntentFilter(
                                    USBReceiver.ACTION_TETHER_STATE_CHANGED);
        mCtx.registerReceiver(mReceiver, filter);
        mReplyPending = true;
    }

    @Override
    public void onStop() {
        if(mReceiver != null) {
            mCtx.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        sendChoiceResult(false);
    }

    private void sendChoiceResult(boolean result) {
        if(mReplyPending) {
            mReplyPending = false;
            mCtx.handleUsbChoiceResult(result);
        }
    }
}
