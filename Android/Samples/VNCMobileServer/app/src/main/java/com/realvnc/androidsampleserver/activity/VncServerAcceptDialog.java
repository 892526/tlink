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

public class VncServerAcceptDialog extends VncServerDialog {

    VncServerAcceptDialog(VNCMobileServer ctx) {
        super(ctx, true);

        setTitle(R.string.accept_dialog_title);
        setContentView(R.layout.accept_dialog);
    }

    void setArgs(Bundle args) {
        TextView text;

        text = (TextView) findViewById(R.id.accept_dialog_text);
        text.setText(mCtx.getResources().getString(R.string.accept_dialog_text,
                                                   args.getString("address")));


        Button button;

        button = (Button) findViewById(R.id.accept_dialog_accept);
        button.setOnClickListener(onAccept);

        button = (Button) findViewById(R.id.accept_dialog_reject);
        button.setOnClickListener(onReject);

        setOnCancelListener(onCancel);
    }

    private View.OnClickListener onAccept = new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
                mCtx.handleAcceptResult(true);
            }
        };

    private View.OnClickListener onReject = new View.OnClickListener() { 
            public void onClick(View v) {
                dismiss();
                mCtx.handleAcceptResult(false);
            }
        };

    private DialogInterface.OnCancelListener onCancel = new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface d) {
                mCtx.handleAcceptResult(false);
            }
        };
}
