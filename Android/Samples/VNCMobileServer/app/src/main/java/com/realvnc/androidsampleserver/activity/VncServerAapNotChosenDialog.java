/* Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of a
 * VNC Automotive SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.androidsampleserver.activity;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.realvnc.androidsampleserver.R;
import com.realvnc.androidsampleserver.SampleIntents;

public class VncServerAapNotChosenDialog extends VncServerDialog {
    boolean mReplyPending;
    BroadcastReceiver mReceiver;

    VncServerAapNotChosenDialog(VNCMobileServer ctx) {
        super(ctx, false);

        setTitle(R.string.aap_not_chosen_dialog_title);
        setContentView(R.layout.aap_not_chosen_dialog);
    }

    void setArgs(Bundle args) {
        TextView text;

        text = (TextView) findViewById(R.id.aap_not_chosen_dialog_text);
        text.setText(mCtx.getResources().getString(
                                R.string.aap_not_chosen_dialog_text));

        Button button;

        button = (Button) findViewById(R.id.aap_not_chosen_dialog_accept);
        button.setOnClickListener(onAccept);

        button = (Button) findViewById(R.id.aap_not_chosen_dialog_settings);
        button.setOnClickListener(onSettings);
    }

    private View.OnClickListener onAccept = new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        };

    private View.OnClickListener onSettings = new View.OnClickListener() {
            public void onClick(View v) {
                Intent settingsIntent = new Intent(mCtx,
                        VncServerPreferenceActivity.class);
                settingsIntent.setAction(SampleIntents.PREFERENCES_INTENT);
                settingsIntent.setPackage(mCtx.getPackageName());
                settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mCtx.startActivity(settingsIntent);
                dismiss();
            }
        };
}
