/* Copyright (C) 2002-2018 RealVNC Ltd. All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of the
 * VNC Mobile Solution SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.androidsampleserver.service;

import android.content.Context;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Wrapper class hiding reflection needed to see if a Bluetooth
 * headset is connected.
 */
class BluetoothHeadset {

    private Class<?> mHeadsetCls = null;
    private Object mBtHeadset = null;

    BluetoothHeadset(Context ctx) {
        try {
            mHeadsetCls = Class.forName("android.bluetooth.BluetoothHeadset");
            Constructor<?> ctorsHeadset[] = mHeadsetCls.getConstructors();
            // Look for the correct constructor.
            for(int i = 0; i < ctorsHeadset.length; ++i) {
                Class<?> params[] = ctorsHeadset[i].getParameterTypes();
                if(params.length != 2)
                    continue;
                if(params[0].getName().compareTo("android.content.Context") == 0 &&
                        params[1].getName().endsWith("ServiceListener"))
                    {
                        // Found the desired constructor
                        mBtHeadset = ctorsHeadset[i].newInstance(new Object[] {ctx, null});
                        break;
                    }
            }
        } catch (Exception e) {
            // Ignore errors as this is just constructing
        }
    }

    void close() {
        if (mHeadsetCls != null && mBtHeadset != null) {
            try {
                Method meth = mHeadsetCls.getMethod("close", (Class[]) null);
                meth.invoke(mBtHeadset, (Object[]) null);
            } catch(Exception e) {
                // Ignore as just performing cleanup
            }
        }
    }

    boolean isConnected() {
        if (mHeadsetCls != null && mBtHeadset != null) {
            try {
                // Try to use the Android 2.3 headset method
                Method meth = mHeadsetCls.getMethod("getCurrentHeadset", (Class[])null);
                Object currentHset = meth.invoke(mBtHeadset, (Object[]) null);
                // If a headset is connected this will be non-null
                return currentHset != null;
            } catch (Exception e) {
                // Ignore 2.3 bluetooth method failures
            }
            try {
                // Try to use the Android 4.0 headset method
                Method meth = mHeadsetCls.getMethod("getConnectedDevices", (Class[])null);
                List<?> devices = (List<?>)meth.invoke(mBtHeadset, (Object[]) null);
                return !devices.isEmpty();
            } catch (Exception e) {
                // Ignore 4.0 bluetooth method failures
            }
        }
        return false;
    }
}
