/* Copyright (C) 2002-2018 RealVNC Ltd. All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of the
 * VNC Mobile Solution SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.androidsampleserver.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.realvnc.androidsampleserver.SampleIntents;


/**
 * This class is a proxy to the the main GUI view. 
 * The reason the main view activity isn't exported is for security.
 * Many of the intents that the main GUI can perform should only be
 * allowed to be called from code within this package.
 * 
 * This class therefore acts as a proxy to pass publicly accessible
 * intents along to the main GUI without exposing other functionality.
 *
 */
public class VNCMobileServerProxy extends Activity {

     /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    }
	
    /*
     * (non-Javadoc)
     * @see android.app.Activity#onDestroy()
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent i) {
        super.onNewIntent(i);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        takeAction();
        // Always finish as this activity uses the hidden style
        finish();
    }

    private void takeAction() {
        Intent i = new Intent(this, VNCMobileServer.class);
        i.setAction(SampleIntents.LAUNCHER_INTENT);
        i.setPackage(getPackageName());
        startActivity(i);
    }
}
