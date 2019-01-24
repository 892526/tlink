/* Copyright (C) 2002-2018 RealVNC Ltd. All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of the
 * VNC Mobile Solution SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.androidsampleserver.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.realvnc.androidsampleserver.R;

public class VncServerAuthDialog extends VncServerDialog {

    VncServerAuthDialog(VNCMobileServer ctx) {
        super(ctx, true);

        setTitle(R.string.auth_dialog_title);
        setContentView(R.layout.auth_dialog);
    }

    public void setArgs(Bundle args) {
        TextView statusText;

        statusText = (TextView) findViewById(R.id.auth_dialog_username);
        statusText.setText(mCtx.getResources().getString(R.string.auth_dialog_username,
                                                         args.getString("username")));

        statusText = (TextView) findViewById(R.id.auth_dialog_password);
        statusText.setText(mCtx.getResources().getString(R.string.auth_dialog_password,
                                                         args.getString("password")));

        Button button;

        button = (Button) findViewById(R.id.auth_dialog_accept);
        button.setOnClickListener(onAccept);

        button = (Button) findViewById(R.id.auth_dialog_reject);
        button.setOnClickListener(onReject);

        setOnCancelListener(onCancel);
    }

    private View.OnClickListener onAccept = new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
                mCtx.handleAuthResult(true);
            }
        };

    private View.OnClickListener onReject = new View.OnClickListener() { 
            public void onClick(View v) {
                dismiss();
                mCtx.handleAuthResult(false);
            }
        };

    private DialogInterface.OnCancelListener onCancel = new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface d) {
                mCtx.handleAuthResult(false);
            }
        };
}
