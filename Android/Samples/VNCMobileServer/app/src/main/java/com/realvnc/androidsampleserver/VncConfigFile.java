/* Copyright (C) 2002-2018 RealVNC Ltd. All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of the
 * VNC Mobile Solution SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.androidsampleserver;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.realvnc.util.IniFile;

import java.io.IOException;
import java.util.logging.Logger;

public class VncConfigFile extends IniFile {
    private static final String TAG = "VNCConfigFile";
    private static final String CONFIG_INI = "vnc_config.ini";
    private static final Logger LOG = Logger.getLogger(VncConfigFile.class.getName());

    private static final String our_section = "VNCServerSettings";

    public String get(String key) {
        return get(our_section, key);
    }

    // Check if we should apply settings from vnc_config.ini
    public static void checkVncConfig(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        boolean applyConfig = prefs.getBoolean("vnc_apply_config", true);

        if(applyConfig) {
            LOG.info("Reading settings from vnc_config.ini...");

            VncConfigFile cfg = new VncConfigFile();

            try {
                cfg.parse(ctx.getAssets().open(CONFIG_INI));
            } catch (IniFile.BadFormatException e) {
                Log.e(TAG, "Exception parsing "+CONFIG_INI+": "+e);
            } catch (IOException e) {
                Log.e(TAG, "Exception parsing "+CONFIG_INI+": "+e);
            }

            SharedPreferences.Editor edit = prefs.edit();
            String value;

            if((value = cfg.get("fetchUrl")) != null) {
                edit.putString("url_connection", value);
            }

            if((value = cfg.get("reportUrl")) != null) {
                edit.putString("url_logging", value);
            }

            if((value = cfg.get("port")) != null) {
                edit.putString("vnc_port", value);
            }

            if((value = cfg.get("enableNetworkAdvertiser")) != null) {
                edit.putBoolean("vnc_enable_network_advertiser", value.equals("1"));
            }

            if((value = cfg.get("encrypt")) != null) {
                edit.putBoolean("vnc_encryption", value.equals("1"));
            }

            if((value = cfg.get("auth")) != null) {
                edit.putString("vnc_authtype", value);
            }

            if((value = cfg.get("query")) != null) {
                edit.putBoolean("vnc_accept_prompt", value.equals("1"));
            }

            if((value = cfg.get("validateViewerSignatures")) != null) {
                edit.putBoolean("vnc_signature_validation", value.equals("1"));
            }

            if((value = cfg.get("usbAutoConnect")) != null) {
                edit.putString("vnc_usb_autoconnect", value);
            }

            if((value = cfg.get("usbBearer")) != null) {
                edit.putString("vnc_usb_bearer", value);
            }

            edit.putBoolean("vnc_apply_config", false);
            edit.apply();
        }
    }
}
