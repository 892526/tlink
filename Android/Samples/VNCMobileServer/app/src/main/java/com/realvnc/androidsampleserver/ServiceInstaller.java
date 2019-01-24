/* Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of a
 * VNC Automotive SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.androidsampleserver;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.realvnc.androidsampleserver.activity.VNCMobileServer;
import com.realvnc.androidsampleserver.service.VncServerService;
import com.realvnc.util.IniFile;
import com.realvnc.vncserver.core.VncServerCoreErrors;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

public class ServiceInstaller {
    private static final String TAG = "ServiceInstaller";

    private static final String ANY_CPU_ABI = "any";

    public static void installApkFile(VncServerService ctx, InputStream apkFile) {

        try {
            OutputStream os = ctx.openFileOutput("temp.apk",
                    Context.MODE_WORLD_READABLE);
            String path = ctx.getFileStreamPath("temp.apk").getAbsolutePath();

            int nb;
            byte[] buffer = new byte[1024];

            do {
                nb = apkFile.read(buffer);
                if(nb > 0) {
                    os.write(buffer, 0, nb);
                }
            } while(nb > 0);
            apkFile.close();
            os.close();

            Intent intent = new Intent(ctx, VNCMobileServer.class);
            intent.setAction(SampleIntents.INSTALL_RCS_INTENT);
            intent.setPackage(ctx.getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            intent.setDataAndType(Uri.parse("file://" + path),
                                  "application/vnd.android.package-archive");

            /* The activity which is started will call installationResult on
             * the service at some point in the future. */
            ctx.startActivity(intent);

        } catch(IOException e) {
            Log.e(TAG, "Failed to install APK with exception: "+e);
            ctx.installationResult(VncServerCoreErrors.VNCSERVER_ERR_INTERNAL_ERROR);
        }

    }

    public static String getHexString(byte[] b) {
        StringBuilder result = new StringBuilder();
        for (int i=0; i < b.length; i++) {
            result.append(
                          Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 ));
        }
        return result.toString();
    }

    public static Set<String> getSystemSigningKeys(Context ctx) {
        PackageManager pm = ctx.getPackageManager();

        Set<String> results = new HashSet<String>();

        try {
            PackageInfo pi = pm.getPackageInfo("android",
                    PackageManager.GET_SIGNATURES);
            if(pi != null) {
                int i;

                Log.i(TAG, "Found " + pi.signatures.length + " system signing keys");

                for(i=0; i<pi.signatures.length; i++) {
                    MessageDigest md = MessageDigest.getInstance("SHA1");
                    md.update(pi.signatures[i].toByteArray());
                    String key = getHexString(md.digest());
                    results.add(key);
                    Log.i(TAG, "Key " + i + " is " + key);
                }

            }
        } catch(NoSuchAlgorithmException e) {
            Log.e(TAG, "Failed to find algorithm for system signing keys");
        } catch(PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to find package for system signing keys");
        }

        return results;
    }

    @android.annotation.TargetApi(21)
    private static String[] getSupportedAbisApi21() {
        /* Here we ought to be able to directly access the SUPPORTED_ABIS
         * field because we are within a TargetApi annotated method.
         * Unfortunately Android tries to optimize it out, and throws up ugly
         * warnings such as:
         *   "VFY: unable to resolve static field" */
        try {
            return (String[]) android.os.Build.class.getField("SUPPORTED_ABIS").get(null);
        } catch (NoSuchFieldException e) {
            return new String[] {};
        } catch (IllegalAccessException e) {
            return new String[] {};
        }
    }

    @SuppressWarnings("deprecation")
    private static String[] getSupportedAbisApi8() {
        return new String[] {android.os.Build.CPU_ABI, android.os.Build.CPU_ABI2};
    }

    private static String[] getSupportedAbis() {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            return getSupportedAbisApi21();
        } else {
            return getSupportedAbisApi8();
        }
    }

    public static void do_install(VncServerService ctx) {

        Log.i(TAG, "Attempting to install the remote control service");

        Set<String> platformkeys = getSystemSigningKeys(ctx);

        String[] supported_abis = getSupportedAbis();

        Log.i(TAG, "device = " + Build.DEVICE);
        Log.i(TAG, "version_release = " + Build.VERSION.RELEASE);
        Log.i(TAG, "model = " + Build.MODEL);
        Log.i(TAG, "product = " + Build.PRODUCT);
        Log.i(TAG, "cpu_abi options = " + java.util.Arrays.toString(supported_abis));
	Log.i(TAG, "api_level = " + Build.VERSION.SDK_INT);

        AssetManager am = ctx.getAssets();

        try {
            IniFile file = new IniFile();
            file.parse(am.open("supported-devices.ini"));

            Hashtable<String, Hashtable<String, String>> sections = file.allSections();

            Hashtable<String, String> best_match = null;
            String best_match_name = null;
            double best_match_score = 0.0d;

            for(String k : Collections.list(sections.keys())) {
                Hashtable<String, String> device = sections.get(k);

                double match_score = 0.0d;

                Log.i(TAG, "Package: " + k + " signed with key: " + device.get("platformkey"));
                // Check for a matching platform key - required
                String platkey = device.get("platformkey");
                if(!platformkeys.contains(platkey)) {
                    Log.i(TAG, "Package: " + k + " has no matching keys");
                    continue;
                } else {
                    Log.i(TAG, "Package: " + k + " has a matching platform key");
                }

                // Check the architectures match - required
                boolean cpu_abi_matches = false;
                String match_cpu_abi = device.get("cpu_abi");
                if (match_cpu_abi != null) {
                    // Give a bonus score that is higher, the earlier in
                    // the list of supported ABIs it is found.
                    double cpu_abi_bonus = 16.0d;
                    for (String supported_abi : supported_abis) {
                        if (match_cpu_abi.equals(supported_abi)
                                || match_cpu_abi.equals(ANY_CPU_ABI)) {
                            cpu_abi_matches = true;
                            match_score += 1.0d; // medium priority basic score
                            match_score += cpu_abi_bonus;
                            break;
                        }
                        // Reduce the bonus
                        cpu_abi_bonus = cpu_abi_bonus * 0.5d;
                    }
                }
                if (!cpu_abi_matches) {
                    Log.i(TAG, "Package: " + k + " has incompatible architecture");
                    continue;
                }

                // Check the API levels are compatible - required
                Set<Integer> rcsApiLevels = parseInts(device.get("api_level"));
                if (rcsApiLevels.contains(Build.VERSION.SDK_INT)) {
                    // higher API level compatibility is given higher priority
                    match_score += 0.01d * Collections.max(rcsApiLevels);
                } else {
                    Log.i(TAG, "Package: " + k + " has incompatible API level");
                    continue;
                }

                // Perform other optional checks.

                String match_device = device.get("device");
                if((match_device != null) && match_device.equals(Build.DEVICE))
                    match_score += 100.0d; // high priority

                String match_model = device.get("model");
                if((match_model != null) && match_model.equals(Build.MODEL))
                    match_score += 100.0d; // high priority

                String match_product = device.get("product");
                if((match_product != null) && match_product.equals(Build.PRODUCT))
                    match_score += 100.0d; // high priority

                Log.i(TAG, "Package: " + k + " score: " + match_score);

                if(match_score > best_match_score) {
                    // We've found a better matching device.
                    best_match_score = match_score;
                    best_match_name = k;
                    best_match = device;
                }
            }

            if(best_match != null) {
                String apk_name = best_match.get("package_name");

                Log.i(TAG, "Using package: " + best_match_name + " " + apk_name);

                installApkFile(ctx, am.open(apk_name));
            } else {
                Log.e(TAG, "No matching package found");
                ctx.installationResult(VncServerCoreErrors.VNCSERVER_ERR_NO_SUITABLE_RCS);
            }

        } catch(IOException e) {
            Log.e(TAG, "Failed to install with exception: "+e);
            ctx.installationResult(VncServerCoreErrors.VNCSERVER_ERR_INTERNAL_ERROR);
        } catch(IniFile.BadFormatException e) {
            Log.e(TAG, "Failed to install with exception: "+e);
            ctx.installationResult(VncServerCoreErrors.VNCSERVER_ERR_INTERNAL_ERROR);
        }
    }

    private static Set<Integer> parseInts(String text) {
        String[] values = text.split(",");
        Set<Integer> ret = new HashSet<Integer>(values.length);
        for (int i = 0; i < values.length; ++i) {
            ret.add(Integer.parseInt(values[i]));
        }
        return ret;
    }
}
