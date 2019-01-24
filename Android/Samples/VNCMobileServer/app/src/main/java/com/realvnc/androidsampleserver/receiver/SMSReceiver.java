/* Copyright (C) 2002-2018 RealVNC Ltd. All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of the
 * VNC Mobile Solution SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.androidsampleserver.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.realvnc.androidsampleserver.SampleIntents;
import com.realvnc.androidsampleserver.service.HTTPTriggerService;

public class SMSReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "SMSReceiver";

    /* package */ static final String ACTION =
        "android.intent.action.DATA_SMS_RECEIVED";

    private SmsMessage[] getMessagesFromIntent(Intent intent)
        {
            SmsMessage retMsgs[] = null;
            Bundle bdl = intent.getExtras();
            try{
                Object pdus[] = (Object [])bdl.get("pdus");
                retMsgs = new SmsMessage[pdus.length];
                for(int n=0; n < pdus.length; n++)
                {
                    byte[] byteData = (byte[])pdus[n];
                    retMsgs[n] =
                        SmsMessage.createFromPdu(byteData);
                }
            }
            catch(Exception e)
            {
                Log.e("GetMessages", "fail", e);
            }
            return retMsgs;
        }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION)) {
            StringBuilder buf = new StringBuilder();
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                SmsMessage[] messages = getMessagesFromIntent(intent);
                for (int i = 0; i < messages.length; i++) {
                    SmsMessage message = messages[i];
                    buf.append("Received SMS from  ");
                    buf.append(message.getDisplayOriginatingAddress());
                    buf.append(" - ");
                    buf.append(message.getDisplayMessageBody());
                }
            }
            Log.i(LOG_TAG, "onReceiveIntent: " + buf);

            Intent i = new Intent(context, HTTPTriggerService.class);
            i.setAction(SampleIntents.HTTP_TRIGGER_INTENT);
            i.setPackage(context.getPackageName());
            context.startService(i);

        } else {
            Log.i(LOG_TAG, "Bad intent: " + intent.getAction());
        }
    }
}
