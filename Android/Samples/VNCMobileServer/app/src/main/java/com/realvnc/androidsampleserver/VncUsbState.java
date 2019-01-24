/* Copyright (C) 2011-2018 RealVNC Ltd. All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of the
 * VNC Mobile Solution SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.androidsampleserver;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import java.lang.reflect.Method;

public class VncUsbState {

    private static final String TAG = "VNC-USB";

    private static final String stagePreferenceName = "vnc_usb_stage";
    private static final String VNC_USB_BEARER_SETTING = "vnc_usb_bearer";
    private static final String VNC_USB_AUTOCONNECT = "vnc_usb_autoconnect";

    public static final String TETHERED_BEARER_TYPE = "USB";
    public static final String AAP_BEARER_TYPE = "AAP";

    public enum Stage {
        NOT_WAITING,
        WAITING_FOR_ACTIVE_TETHER(true),
        WAITING_FOR_DISCONNECT,
        TOGGLING_TETHERING_ON(true),
        TOGGLING_TETHERING_OFF;
        public final boolean mSwitchingOn;
        Stage() {
            mSwitchingOn = false;
        }
        Stage(boolean switchingOn) {
            mSwitchingOn = switchingOn;
        }
    };

    // Ordering of this must match the ordering in the values used by preference.xml
    public enum AutoTetherStatus {
        VNC_USB_AUTOCONNECT_OFF,
        VNC_USB_AUTOCONNECT_ON,
        VNC_USB_AUTOCONNECT_ASK
    };

    public static String[] getAvailableTetheredInterfaces(ConnectivityManager cm) {
        String[] interfaces = null;
        try {
            Method meth = cm.getClass().getMethod("getTetherableIfaces", (Class[]) null);
            interfaces = (String[])(meth.invoke(cm, (Object[]) null));
        } catch(Exception e) {
            Log.e(TAG, "Exception when trying to get available tethered interfaces: "+e);
        }

        return interfaces != null ?
            interfaces :
            new String[0];
    }

    public static String[] getActiveTetheredInterfaces(ConnectivityManager cm) {
        String[] interfaces = null;
        try {
            Method meth = cm.getClass().getMethod("getTetheredIfaces", (Class[]) null);
            interfaces = (String[])(meth.invoke(cm, (Object[]) null));
        } catch (Exception e) {
            Log.e(TAG, "Exception when trying to get currently tethered interfaces: "+e);
        }

        return interfaces != null ?
            interfaces :
            new String[0];
    }

    public static String[] getErroredTetheredInterfaces(ConnectivityManager cm) {
        String[] interfaces = null;
        try {
            Method meth = cm.getClass().getMethod("getTetheringErroredIfaces", (Class[]) null);
            interfaces = (String[])(meth.invoke(cm, (Object[]) null));
        } catch (Exception e) {
            Log.e(TAG, "Exception when trying to get currently errored tethering interfaces: "+e);
        }

        return interfaces != null ?
            interfaces :
            new String[0];
    }

    public static String[] getUsbTetheringRegexes(ConnectivityManager cm) {
        String[] regexes = null;
        try {
            Method meth = cm.getClass().getMethod("getTetherableUsbRegexs");
            regexes = (String[])(meth.invoke(cm));
        } catch (Exception e) {
            Log.e(TAG, "Exception when trying to get list of USB regexes: "+e);
        }
        return regexes != null ?  regexes : new String[] { "usb0", "rndis0" };
    }

    // Start tethering, but delay it for a very short while. This is to allow the system to finish
    // all the USB configurations. Without the delay, sometimes the device can end up in a loop
    // where it switches tethering on and then it disconnects from the viewer platform and
    // reconnectes straight away.
    private static class DelayedTethering implements Runnable {
        private Context mContext;

        DelayedTethering(Context context) {
            mContext = context;
        }

        public void run() {
            try {
                Class<?> cls = Class.forName("android.net.ConnectivityManager");
                ConnectivityManager cm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

                String[] interfaces = getAvailableTetheredInterfaces(cm);

                for(String ifname : interfaces) {
                    if(!isUsbInterface(ifname, mContext))
                        continue;
                    Log.i(TAG, "Tether: available " + ifname);

                    Method meth = cls.getMethod("tether",
                            new Class<?>[] { String.class });
                    meth.invoke(cm, new Object[] { ifname });
                }
            } catch(Exception e) {
                Log.e(TAG, "Exception when trying to start tethering: "+e);
            }
        }
    }

    public static void startUsbTethering(Context context) {
        Handler handler = new Handler();
        handler.postDelayed(new DelayedTethering(context), 800);
    }

    public static void stopUsbTethering(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Class<?> cls = cm.getClass();

            String[] interfaces = getActiveTetheredInterfaces(cm);
            Method untetherMeth = cls.getMethod("untether",
                                          new Class<?>[] { String.class });

            for(String iface : interfaces) {
                if(isUsbInterface(iface, context))
                    untetherMeth.invoke(cm, new Object[] { iface });
            }

        } catch(Exception e) {
            Log.e(TAG, "Exception when trying to stop tethering: "+e);
        }
    }

    public static boolean isUsbInterface(String iface, Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            String[] regexes = getUsbTetheringRegexes(cm);
            for (String regex : regexes) {
                if (iface.matches(regex))
                    return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception trying to get USB tethering interface regexes: "+e);
        }
        return false;
    }

    public static boolean hasUsbInterface(String[] ifaceList, Context context) {
        for(String iface : ifaceList) {
            if(isUsbInterface(iface, context))
                return true;
        }
        return false;
    }

    public static void setStage(Context context, Stage stage) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(stagePreferenceName, stage.ordinal());
        editor.apply();
        return;
    }

    public static Stage getStage(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int usbStage = prefs.getInt(stagePreferenceName, Stage.NOT_WAITING.ordinal());
        Stage ret = Stage.NOT_WAITING;
        if(usbStage >= 0 && usbStage < Stage.values().length)
            ret = Stage.values()[usbStage];
        return ret;
    }

    private static boolean supportsUsbManager() {
        try {
            // First attempt to use the core API added in Honeycomb
            Class<?> usbManagerClass = Class.forName("android.hardware.usb.UsbManager");
            if(usbManagerClass != null) {
                return true;
            }
        }
        catch (Exception e)
        {
            // Ignore the exception, try to fall back to the
            // Gingerbread Google Add-on API implementation
        }

        try {
            // Fall back to Gingerbread Google Add-on API implementation
            Class<?> usbManagerClass = Class.forName("com.android.future.usb.UsbManager");
            if(usbManagerClass != null) {
                return true;
            }
        } catch (Exception e) {
            // Ignore the exception as this just means that the default USB tethering
            // bearer should be used
        }
        return false;
    }

    private static String getDefaultUsbBearer() {
        if(supportsUsbManager())
            return AAP_BEARER_TYPE;
        else
            return TETHERED_BEARER_TYPE;
    }

    public static String getUsbBearer(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String usbBearer = prefs.getString(VNC_USB_BEARER_SETTING, getDefaultUsbBearer());
        return usbBearer;
    }

    private static AutoTetherStatus getDefaultUsbAutoConnect() {
        return AutoTetherStatus.VNC_USB_AUTOCONNECT_ASK;
    }

    public static AutoTetherStatus getUsbAutoConnect(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int option = Integer.parseInt(prefs.getString(VNC_USB_AUTOCONNECT,
                        Integer.toString(getDefaultUsbAutoConnect().ordinal())));
        if(option >= 0 && option < AutoTetherStatus.values().length)
            return AutoTetherStatus.values()[option];
        return getDefaultUsbAutoConnect();
    }

    public static void setupDefaults(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String bearer = getUsbBearer(context);
        AutoTetherStatus autoTether = getUsbAutoConnect(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(VNC_USB_BEARER_SETTING, bearer);
        edit.putString(VNC_USB_AUTOCONNECT, Integer.toString(autoTether.ordinal()));
        edit.apply();
    }

}
