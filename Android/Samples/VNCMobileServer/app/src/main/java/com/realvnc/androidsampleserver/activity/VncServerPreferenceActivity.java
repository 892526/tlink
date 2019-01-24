/* Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of a
 * VNC Automotive SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.androidsampleserver.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.realvnc.androidsampleserver.R;
import com.realvnc.androidsampleserver.VncUsbState;
import com.realvnc.vncserver.core.VncAuthType;

import java.net.MalformedURLException;
import java.net.URL;

public class VncServerPreferenceActivity extends PreferenceActivity {
    private CheckBoxPreference mEncryption;
    private ListPreference mAuthentication;
    private CheckBoxPreference mSignatureValidation;
    private CheckBoxPreference mClipboardEnabled;
    private AlertDialog mDlg;

    /* Constraints on the authentication settings: Key signature
     * validation can only be enabled if either encryption or
     * authentication is enabled.
     *
     * Also warn for clipboard being enabled on Android 4.3. */

    private class ChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            checkConstraints();

            // Need to make sure that clipboard support defaults to off
            // on Android 4.3 devices prior to 4.3_r2.2. The reasons for
            // this are described in:
            //
            // https://code.google.com/p/android/issues/detail?id=58043
            //
            // The internal VNC Automotive reference for this is MOB-9408
            //
            // Testing for the exact "4.3" string is working on the
            // assumption that if "4.3.1" is ever released that it will
            // contain the fix present in 4.3_r2.2 of the Android Open Source
            // Project.
            if ( key.equals("vnc_clipboard") &&
                    mClipboardEnabled.isChecked() &&
                    Build.VERSION.RELEASE.equals("4.3")) {
                showPopup(R.string.clipboard_4_3_warning_title,
                        R.string.clipboard_4_3_warning_message);
            }
        }
    }

    private void checkConstraints() {

        int authType = Integer.parseInt(mAuthentication.getValue());

        if(!mEncryption.isChecked() && (authType == VncAuthType.VNC_AUTH_NONE)) {
            
            if(mSignatureValidation.isChecked()) {
                mSignatureValidation.setChecked(false);
                showPopup(R.string.signature_validation_title, R.string.signature_validation_message);
            }

            mSignatureValidation.setEnabled(false);
        } else {
            mSignatureValidation.setEnabled(true);
        }
    }

    /* Constraints on port entry field: it must be non-empty, and be
     * an integer from 1 to 65535 inclusive. Some of this is enforced
     * by the settings in preferences.xml - we already know that the
     * value contains only numeric characters, and no more than 5 of
     * them.  */

    private class PortChangeListener implements Preference.OnPreferenceChangeListener {
        public boolean onPreferenceChange(Preference pref, Object newValue) {
            String value = (String)newValue;

            if(value.length() == 0) {
                showPopup(R.string.port_empty_title, R.string.port_empty_message);
                return false;
            }

            int port = Integer.parseInt(value);

            if((port < 1) || (port > 65535)) {
                showPopup(R.string.port_range_title, R.string.port_range_message);
                return false;
            }

            return true;
        }
    }

    /* Constraints on VNC Automotive Bridge URL fields, which must not be
     * empty, and must be valid URLs */

    private class UrlChangeListener implements Preference.OnPreferenceChangeListener {
        public boolean onPreferenceChange(Preference pref, Object newValue) {
            String value = (String)newValue;

            if(value.length() == 0) {
                showPopup(R.string.url_empty_title, R.string.url_empty_message);
                return false;
            }

            try {
                new URL(value);
            } catch(MalformedURLException mue) {
                showPopup(R.string.url_invalid_title, R.string.url_invalid_message);
                return false;
            }

            return true;
        }
    }

    private class UsbBearerChangeListener implements Preference.OnPreferenceChangeListener {
        public boolean onPreferenceChange(Preference pref, Object newValue) {
            String value = (String)newValue;

            if (value.equals("USB")) {
                // USB tethering bearer
                showPopup(R.string.ics_usb_tethering_title, R.string.ics_usb_tethering_message);
            }


            return true;
        }
    }

    /* SharedPreferences keeps listeners in a WeakHashMap. This means
     * that if a reference isn't saved elsewhere, the listener will
     * become the target of garbage collection as soon as you leave
     * the current scope. It will work at first, but eventually, will
     * get garbage collected, removed from the WeakHashMap and stop
     * working. Keep a reference to the listener in a field of the
     * class to prevent this. */
    private ChangeListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make sure the Usb bearer and tethering options are set to the correct default for this platform
        VncUsbState.setupDefaults(this);

        addPreferencesFromResource(R.xml.preferences);

        mEncryption = (CheckBoxPreference) findPreference("vnc_encryption");
        mAuthentication = (ListPreference) findPreference("vnc_authtype");
        mSignatureValidation = (CheckBoxPreference) findPreference("vnc_signature_validation");

        mClipboardEnabled = (CheckBoxPreference) findPreference("vnc_clipboard");

        mListener = new ChangeListener();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(mListener);

        findPreference("vnc_port").setOnPreferenceChangeListener(new PortChangeListener());

        UrlChangeListener ucl = new UrlChangeListener();
        findPreference("url_connection").setOnPreferenceChangeListener(ucl);
        findPreference("url_logging").setOnPreferenceChangeListener(ucl);

        UsbBearerChangeListener usbListener = new UsbBearerChangeListener();
        findPreference("vnc_usb_bearer").setOnPreferenceChangeListener(usbListener);

        checkConstraints();
    }

    @Override
    protected void onDestroy() {
        if (mListener != null) {
            getPreferenceScreen().getSharedPreferences().
                unregisterOnSharedPreferenceChangeListener(mListener);
            mListener = null;
        }
        super.onDestroy();
    }

    private void showPopup(int title, int body) {
        mDlg = new AlertDialog.Builder(this)
            .setMessage(getResources().getString(body))
            .setTitle(title)
            .setNegativeButton(R.string.popup_ok_button, 
                               new DialogInterface.OnClickListener() {
                                   public void onClick(DialogInterface dialog, int id) {
                                       dialog.dismiss();
                                   }
                               })
            .create();
        mDlg.show();
    }
}
