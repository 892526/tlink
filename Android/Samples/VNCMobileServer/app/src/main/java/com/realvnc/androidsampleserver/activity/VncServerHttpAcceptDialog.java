/* Copyright (C) 2002-2018 RealVNC Ltd. All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of the
 * VNC Mobile Solution SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.androidsampleserver.activity;

import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;

import com.realvnc.androidsampleserver.R;

public class VncServerHttpAcceptDialog extends VncServerDialog {

    VncServerHttpAcceptDialog(VNCMobileServer ctx) {
        super(ctx, true);

        setTitle(R.string.http_accept_title);
        setContentView(R.layout.http_accept);

        Button button;

        button = (Button) findViewById(R.id.http_accept_accept);
        button.setOnClickListener(onAccept);

        button = (Button) findViewById(R.id.http_accept_reject);
        button.setOnClickListener(onReject);

        setOnCancelListener(onCancel);
    }

    private View.OnClickListener onAccept = new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
                mCtx.handleHttpAcceptResult(true);
            }
        };

    private View.OnClickListener onReject = new View.OnClickListener() { 
            public void onClick(View v) {
                dismiss();
                mCtx.handleHttpAcceptResult(false);
            }
        };

    private DialogInterface.OnCancelListener onCancel = new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface d) {
                mCtx.handleHttpAcceptResult(false);
            }
        };
}
