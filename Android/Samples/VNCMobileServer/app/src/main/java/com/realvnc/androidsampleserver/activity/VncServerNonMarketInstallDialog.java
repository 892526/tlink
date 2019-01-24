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

public class VncServerNonMarketInstallDialog extends VncServerDialog {

    VncServerNonMarketInstallDialog(VNCMobileServer ctx) {
        super(ctx, false);

        setTitle(R.string.non_market_install_dialog_title);
        setContentView(R.layout.non_market_install_dialog);

        Button button;

        button = (Button) findViewById(R.id.non_market_install_dialog_accept);
        button.setOnClickListener(onNonMarketInstall);

        button = (Button) findViewById(R.id.non_market_install_dialog_reject);
        button.setOnClickListener(onReject);

        setOnCancelListener(onCancel);
    }

    private View.OnClickListener onNonMarketInstall = new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
                mCtx.handleNonMarketInstallResult(true);
            }
        };

    private View.OnClickListener onReject = new View.OnClickListener() { 
            public void onClick(View v) {
                dismiss();
                mCtx.handleNonMarketInstallResult(false);
            }
        };

    private DialogInterface.OnCancelListener onCancel = new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface d) {
                mCtx.handleNonMarketInstallResult(false);
            }
        };
}
