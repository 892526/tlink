/* Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of a
 * VNC Automotive SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.androidsampleserver.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.realvnc.androidsampleserver.R;

public class VncServerAccessibilityDialog extends VncServerDialog {
    private static final String TAG = "VNCMobileServer";

    /** An intent for launching the system accessibility settings. */
    private static final Intent sSettingsIntent =
        new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);

    VncServerAccessibilityDialog(VNCMobileServer ctx) {
        super(ctx, true);

        setTitle(R.string.SS_04_201);
        setContentView(R.layout.accessibility_dialog);

        Button button;

        button = (Button) findViewById(R.id.accessibility_dialog_accept);
        button.setOnClickListener(onAccept);

        /*
        button = (Button) findViewById(R.id.accessibility_dialog_reject);
        button.setOnClickListener(onReject);
        */
    }

    private View.OnClickListener onAccept = new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
                try {
                    mCtx.startActivity(sSettingsIntent);
                } catch (ActivityNotFoundException e) {
                    Log.e(TAG, "Unable to start accessibility settings activity");
                }
            }
        };

    /*
    private View.OnClickListener onReject = new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        };
    */
}
