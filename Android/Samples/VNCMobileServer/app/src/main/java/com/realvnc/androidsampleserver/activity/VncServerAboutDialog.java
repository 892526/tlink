/* Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of a
 * VNC Automotive SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.androidsampleserver.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.realvnc.androidsampleserver.R;
import com.realvnc.androidsampleserver.ServiceInstaller;

import java.util.Set;

public class VncServerAboutDialog extends VncServerDialog {

    VncServerAboutDialog(VNCMobileServer ctx) {
        super(ctx, false);

        setTitle(R.string.about_dialog_title);
        setContentView(R.layout.about_dialog);
    }

    public void setArgs(Bundle args) {
        TextView text;

        Set<String> signingKeys = ServiceInstaller.getSystemSigningKeys(mCtx);
        StringBuilder sb = new StringBuilder();
        for(String s : signingKeys) {
            sb.append(s);
            sb.append("\r\n");
        }
        String signingKeysString = sb.toString();
        

        text = (TextView) findViewById(R.id.about_dialog_text);
        text.setText(mCtx.getResources().getString(R.string.about_dialog_text,
                        args.getString("version"), 
                        signingKeysString));
    
        Button button;

        button = (Button) findViewById(R.id.about_dialog_ok);
        button.setOnClickListener(onClick);
    }

    private View.OnClickListener onClick = new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        };
}
