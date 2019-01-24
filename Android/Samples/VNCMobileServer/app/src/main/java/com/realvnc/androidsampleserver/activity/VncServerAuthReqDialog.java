/* Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of a
 * VNC Automotive SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.androidsampleserver.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.realvnc.androidsampleserver.R;

public class VncServerAuthReqDialog extends VncServerDialog {

    VncServerAuthReqDialog(VNCMobileServer ctx) {
        super(ctx, true);

        setTitle(R.string.authreq_dialog_title);
        setContentView(R.layout.authreq_dialog);
    }

    public void setArgs(Bundle args) {
        View view;

        view = findViewById(R.id.authreq_dialog_username_layout);
        view.setVisibility(args.getBoolean("username") ? View.VISIBLE : View.GONE);

        view = findViewById(R.id.authreq_dialog_password_layout);
        view.setVisibility(args.getBoolean("password") ? View.VISIBLE : View.GONE);

        Button button;

        button = (Button) findViewById(R.id.authreq_dialog_ok);
        button.setOnClickListener(onSubmit);

        setOnCancelListener(onCancel);
    }

    private View.OnClickListener onSubmit = new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();

                EditText text = (EditText) findViewById(R.id.authreq_dialog_username);
                String username = text.getText().toString();

                text = (EditText) findViewById(R.id.authreq_dialog_password);
                String password = text.getText().toString();

                mCtx.handleAuthReqResult(username, password);
            }
        };

    private DialogInterface.OnCancelListener onCancel = new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface d) {
                mCtx.handleAuthReqCancel();
            }
        };
}
