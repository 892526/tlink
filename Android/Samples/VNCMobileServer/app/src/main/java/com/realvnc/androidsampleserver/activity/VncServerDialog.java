/* Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of a
 * VNC Automotive SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.androidsampleserver.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.PowerManager;

public class VncServerDialog extends Dialog {
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;
    private final boolean mNeedScreenAwake;
    protected final VNCMobileServer mCtx;

    VncServerDialog(VNCMobileServer ctx, boolean needScreenAwake) {
        super(ctx);
        mCtx = ctx;
        mNeedScreenAwake = needScreenAwake;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mNeedScreenAwake && mWakeLock == null) {
            mPowerManager = (PowerManager)mCtx.getSystemService(Context.POWER_SERVICE);

            mWakeLock = mPowerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                                                  PowerManager.SCREEN_DIM_WAKE_LOCK |
                                                  PowerManager.ON_AFTER_RELEASE,
                                                  "VncDialogWakeLock");

            mWakeLock.acquire();
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mCtx.dialogDismissed();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }
}
