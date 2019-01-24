/* Copyright (C) 2002-2018 RealVNC Ltd. All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of the
 * VNC Mobile Solution SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.androidsampleserver.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.realvnc.androidsampleserver.R;

public class VncServerConnectDialog extends VncServerDialog {

    VncServerConnectDialog(VNCMobileServer ctx) {
        super(ctx, false);

        setTitle(R.string.connect_dialog_title);
        setContentView(R.layout.connect_dialog);
    }

    public void setArgs(Bundle args) {
        EditText text = (EditText) findViewById(R.id.connect_dialog_address);
        text.setText(args.getString("address"));

        Button button;

        button = (Button) findViewById(R.id.connect_dialog_ok);
        button.setOnClickListener(onClickOk);

        button = (Button) findViewById(R.id.connect_dialog_cancel);
        button.setOnClickListener(onClickCancel);
    }

    private View.OnClickListener onClickOk = new View.OnClickListener() {
            public void onClick(View v) {
                EditText text = (EditText) findViewById(R.id.connect_dialog_address);
                String address = text.getText().toString();
                mCtx.handleConnect(address);

                dismiss();
            }
        };

    private View.OnClickListener onClickCancel = new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        };
}
