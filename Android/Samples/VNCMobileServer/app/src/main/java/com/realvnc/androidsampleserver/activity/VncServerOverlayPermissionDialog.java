/* Copyright (C) 2018 VNC Automotive Ltd.  All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of a
 * VNC Automotive SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.androidsampleserver.activity;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.realvnc.androidsampleserver.R;

public class VncServerOverlayPermissionDialog extends VncServerDialog {
    private static final String TAG = "VNCMobileServer";

    VncServerOverlayPermissionDialog(final VNCMobileServer ctx) {
        super(ctx, true);

        final View.OnClickListener onAccept = new View.OnClickListener() {
            @Override
            @TargetApi(26)
            public void onClick(final View v) {
                dismiss();

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    throw new RuntimeException(
                            "Overlay Permission Dialog displayed on an old system");
                }

                if (!Settings.canDrawOverlays(mCtx)) {
                    try {
                        Log.w(TAG, "Opening the 'Manage Overlay Permission' settings");

                        final Intent intent = new Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + mCtx.getPackageName()));

                        mCtx.startActivityForResult(
                                intent,
                                VNCMobileServer.Companion.getMANAGE_OVERLAY_PERMISSION_REQUEST()
                                );
                    } catch (final ActivityNotFoundException e) {
                        Log.e(TAG, "Unable to start overlay permission activity");
                    }
                } else {
                    Log.w(TAG, "Overlay permission already granted");
                }
            }
        };

        /*
        final View.OnClickListener onReject = new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                dismiss();

                Log.w(TAG, "Overlay permission dialog rejected");

                Toast.makeText(
                        mCtx,
                        mCtx.getResources().getString(
                                R.string.orientation_lock_disabled),
                        Toast.LENGTH_LONG).show();
            }
        };
        */

        setTitle(R.string.SS_04_204);
        setContentView(R.layout.overlay_permission_dialog);

        final Button acceptButton = (Button) findViewById(
                R.id.overlay_permission_dialog_accept);
        acceptButton.setOnClickListener(onAccept);

        /*
        final Button rejectButton = (Button) findViewById(
                R.id.overlay_permission_dialog_reject);
        rejectButton.setOnClickListener(onReject);
        */
    }
}
