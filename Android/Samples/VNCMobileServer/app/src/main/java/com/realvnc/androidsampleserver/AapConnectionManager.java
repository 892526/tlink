/* Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of a
 * VNC Automotive SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.androidsampleserver;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.realvnc.androidsampleserver.activity.VNCMobileServer;
import com.realvnc.androidsampleserver.activity.VNCMobileServerProxy;
import com.realvnc.androidsampleserver.service.VncServerService;
import com.realvnc.vncserver.android.bearers.VncAapBearer;
import com.realvnc.vncserver.core.VncException;
import com.realvnc.vncserver.core.VncServerCoreErrors;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AapConnectionManager {
    private static final Logger LOG =
        Logger.getLogger("com.realvnc.aapconnectionmanager");

    // These are the default values to use when matching with
    // an accessory. They should be replaced before being used in
    // a production system.
    //
    // If these values are changed then any usb-accessory values
    // specified in the accessory filter xml file will need updating,
    // as well as the values in the viewer side bearer.
    //
    // If the XML file isn't updated then the Android OS won't
    // direct the accessory request to the correct application.
    //
    // When updating these values it is also necessary to update the
    // same values in the discoverer and USBAAP viewer bearer.
    //
    private static final String DEFAULT_MANUFACTURER = "com.jvckenwood";
    private static final String DEFAULT_MODEL        = "tlink";
    private static final String DEFAULT_DESCRIPTION  = "tlink";
    private static final String DEFAULT_VERSION      = "1.0";
    private static final String DEFAULT_URI          = "https://play.google.com/store/apps/details?id=com.jvckenwood.tlink";
    private static final String DEFAULT_SERIAL       = "1234567890";

    private static final String USB_STATE_ACTION_VAR_NAME = "ACTION_USB_STATE";
    private static final String USB_STATE_CONNECTED_VAR_NAME = "USB_CONNECTED";

    public interface Listener {
        public void accessoryDetached();
    }

    private final Context mCtx;
    private BroadcastReceiver mBroadcastReceiver;
    private Listener mListener;
    private Class<?> mUsbAccessoryCls;
    private Class<?> mUsbManagerCls;
    private Object mUsbManager;
    private boolean mUseIntentForAccessory;
    private String mUsbStateIntentAction;
    private String mUsbStateConnectedKeyName;

    static private String getStaticStringField(Class<?> cls,
            String fieldName) {
        String ret = null;
        try {
            Field f = cls.getField(fieldName);
            ret = (String) f.get(null);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,
                    "Failed to get static string field " + fieldName +
                    " from " + cls,
                    e);
        }
        return ret;
    }

    static private Object callMethod(Class<?> cls, Object obj,
            String methodName, Class<?>[] types, Object... params) {
        Object ret = null;
        try {
            Method m = cls.getMethod(methodName, types);
            ret = m.invoke(obj, params);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,
                    "Failed to call method " + methodName +
                    " on object " + obj +
                    " for class " + cls,
                    e);
        }
        return ret;
    }

    private void loadUsbClasses()
        throws ClassNotFoundException {
        mUsbAccessoryCls = Class.forName("android.hardware.usb.UsbAccessory");
        mUsbManagerCls = Class.forName("android.hardware.usb.UsbManager");
        // mUsbManager = (UsbManager) mCtx.getSystemService(Context.USB_SERVICE);
        final String USB_SERVICE
                = AapConnectionManager.getStaticStringField(
                        Context.class,
                        "USB_SERVICE");
        mUsbManager = mCtx.getSystemService(USB_SERVICE);
        mUseIntentForAccessory = true;
        mUsbStateIntentAction
                = AapConnectionManager.getStaticStringField(
                        mUsbManagerCls,
                        AapConnectionManager.USB_STATE_ACTION_VAR_NAME);
        mUsbStateConnectedKeyName
                = AapConnectionManager.getStaticStringField(
                        mUsbManagerCls,
                        AapConnectionManager.USB_STATE_CONNECTED_VAR_NAME);
    }

    private AapConnectionManager(Context ctx, Listener listener) {
        mCtx = ctx;
        mListener = listener;
    }

    /*
     * Attempts to find the correct accessory from the
     * USB manager.
     */
    private Object findUsbAccessory(Object usbInterfaceManager)
        throws VncException
    {
        Object foundAccessory = null;
        Object[] accessoryList =
            (Object[])callMethod(mUsbManagerCls,
                    usbInterfaceManager, "getAccessoryList", new Class<?>[] {});
        if(accessoryList == null) {
            LOG.severe("Failed to find any UsbAccessories");
            throw new VncException(VncServerCoreErrors.VNCSERVER_ERR_USB_NOT_CONNECTED);
        }
        for(Object accessory : accessoryList) {
            if(accessoryIdentityMatches(accessory)) {
                if(foundAccessory != null) {
                    LOG.severe("Multiple matching accessories found, aborting");
                    throw new VncException(VncServerCoreErrors.VNCSERVER_ERR_INVALID_PARAMETER);
                }
                foundAccessory = accessory;
            }
        }
        if(foundAccessory == null) {
            LOG.severe("Failed to find any suitable UsbAccessories from list of size "
                    + accessoryList.length);
            throw new VncException(VncServerCoreErrors.VNCSERVER_ERR_USB_NOT_CONNECTED);
        }
        return foundAccessory;
    }

    protected void AddUsbFilterActions(IntentFilter filter) {
        filter.addAction(getStaticStringField(mUsbManagerCls,
                        "ACTION_USB_ACCESSORY_DETACHED"));
        filter.addAction(mUsbStateIntentAction);
    }

    protected boolean HasUsbAccessoryDetached(Intent intentReceived) {
        Object accessory = null;
        if (mUseIntentForAccessory) {
            // accessory =
            //   intentReceived.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
            String EXTRA_ACCESSORY = getStaticStringField(mUsbManagerCls,
                    "EXTRA_ACCESSORY");
            if (EXTRA_ACCESSORY != null) {
                accessory = intentReceived.getParcelableExtra(EXTRA_ACCESSORY);
            }
        } else {
            // accessory = UsbManager.getAccessory(intentReceived);
            accessory = callMethod(mUsbManagerCls, null,
                    "getAccessory", new Class<?>[] {Intent.class},
                    intentReceived);
        }
        String action = intentReceived.getAction();
        if (getStaticStringField(mUsbManagerCls,
                        "ACTION_USB_ACCESSORY_DETACHED").equals(action)) {
            if ((accessory == null) || accessoryIdentityMatches(accessory)) {
                return true;
            }
        }
        return false;
    }

    protected ParcelFileDescriptor OpenFileDescriptor()
        throws VncException {
        Object accessory = findUsbAccessory(mUsbManager);
        if (accessory == null) {
            throw new VncException(VncServerCoreErrors.VNCSERVER_ERR_USB_NOT_CONNECTED);
        }

        // Check permissions
        Object havePermission = callMethod(mUsbManagerCls, mUsbManager,
                "hasPermission", new Class<?>[] { mUsbAccessoryCls },
                accessory);
        if (havePermission != null && !(Boolean)havePermission) {
            LOG.info("Requesting permission to use accessory");
            Intent intent = new Intent(mCtx, VncServerService.class);
            intent.setAction(SampleIntents.START_SERVER_FROM_AAP_INTENT);
            intent.setData(Uri.parse("vnccmd:v=1;t=AAP"));
            intent.setPackage(mCtx.getPackageName());
            PendingIntent pi = PendingIntent.getService(mCtx, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            callMethod(mUsbManagerCls, mUsbManager,
                    "requestPermission",
                    new Class<?>[] { mUsbAccessoryCls, PendingIntent.class },
                    accessory, pi);
            throw new VncException(VncServerCoreErrors.VNCSERVER_ERR_PERMISSIONS);
        }

        ParcelFileDescriptor result = (ParcelFileDescriptor) callMethod(
                mUsbManagerCls, mUsbManager,
                "openAccessory", new Class<?>[] { mUsbAccessoryCls },
                accessory);
        if(result == null) {
            // The port is most likely in use, so return a port in use error.
            // It might also be that the viewer side died without setting the
            // configuration on the usb interface, but that's something that
            // can only be fixed from the viewer end the next time it tries to
            // open the port.
            throw new VncException(VncServerCoreErrors.VNCSERVER_ERR_PORT_IN_USE);
        }

        return result;
    }

    protected boolean accessoryIdentityMatches(Object accessory) {
        final Class<?>[] emptyList = new Class<?>[] {};
        final String manufacturer = (String) callMethod(mUsbAccessoryCls,
                accessory,"getManufacturer", emptyList);
        final String model = (String) callMethod(mUsbAccessoryCls,
                accessory, "getModel", emptyList);
        final String description = (String) callMethod(mUsbAccessoryCls,
                accessory, "getDescription", emptyList);
        final String version = (String) callMethod(mUsbAccessoryCls,
                accessory, "getVersion", emptyList);
        final String uri = (String) callMethod(mUsbAccessoryCls,
                accessory, "getUri", emptyList);
        final String serial = (String) callMethod(mUsbAccessoryCls,
                accessory, "getSerial", emptyList);


        //Log.d("AapConnectionManager", "----------------------------------------------------------------------");
        //Log.d("AapConnectionManager", "manufacturer:" + manufacturer);
        //Log.d("AapConnectionManager", "model       :" + model);
        //Log.d("AapConnectionManager", "description :" + description);
        //Log.d("AapConnectionManager", "version     :" + version);
        //Log.d("AapConnectionManager", "uri         :" + uri);
        //Log.d("AapConnectionManager", "serial      :" + serial);


        if(DEFAULT_MANUFACTURER != null &&
                !DEFAULT_MANUFACTURER.equals(manufacturer)) {
            LOG.info("Accessory doesn't match manufacturer: " +
                    DEFAULT_MANUFACTURER + " has: " + manufacturer);
            return false;
        }
        if(DEFAULT_MODEL != null &&
                !DEFAULT_MODEL.equals(model)) {
            LOG.info("Accessory doesn't match model: " +
                    DEFAULT_MODEL + " has: " + model);
            return false;
        }
        if(DEFAULT_DESCRIPTION != null &&
                !DEFAULT_DESCRIPTION.equals(description)) {
            LOG.info("Accessory doesn't match description: '" +
                    DEFAULT_DESCRIPTION + "' has: '" + description + "'");
            return false;
        }
        if(DEFAULT_VERSION != null &&
                !DEFAULT_VERSION.equals(version)) {
            LOG.info("Accessory doesn't match version: " +
                    DEFAULT_VERSION + " has: " + version);
            return false;
        }
        if(DEFAULT_URI != null &&
                !DEFAULT_URI.equals(uri)) {
            LOG.info("Accessory doesn't match URI: '" +
                    DEFAULT_URI + "' has: '" + uri + "'");
            return false;
        }
        if(DEFAULT_SERIAL != null &&
                !DEFAULT_SERIAL.equals(serial)) {
            LOG.info("Accessory doesn't match serial: " +
                    DEFAULT_SERIAL + " has: " + serial);
            return false;
        }

        if (!VncServerApp.isAgreement()) {
            Context context = VncServerApp.getContext();
            Intent i = new Intent(context, VNCMobileServerProxy.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
            return false;
        }

        return true;
    }

    private void registerBroadcastReceiver() {
        IntentFilter f = new IntentFilter();
        AddUsbFilterActions(f);
        mBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    LOG.info("Receive intent: " + intent.toUri(Intent.URI_INTENT_SCHEME));
                    String action = intent.getAction();
                    if (mUsbStateIntentAction != null
                            && mUsbStateIntentAction.equals(action)) {
                        if (!intent.getBooleanExtra(mUsbStateConnectedKeyName, false)) {
                            LOG.info("USB cable removed based on USB_STATE intent");
                            accessoryDetached();
                        }
                    } else if (HasUsbAccessoryDetached(intent)) {
                        LOG.info("USB Accessory removed");
                        accessoryDetached();
                    }  else {
                        LOG.info("Unhandled intent in usbBroadcastReceiver");
                    }
                }
            };
        mCtx.registerReceiver(mBroadcastReceiver, f);
    }

    private void unregisterBroadcastReceiver() {
        if(mBroadcastReceiver != null)
            mCtx.unregisterReceiver(mBroadcastReceiver);
        mBroadcastReceiver = null;
    }

    private void closeParcelFileDescriptor() {
        ParcelFileDescriptor oldDesc = VncAapBearer.getParcelFileDescriptor();
        VncAapBearer.setParcelFileDescriptor(null);
        if (oldDesc != null) {
            try {
                oldDesc.close();
            } catch (IOException ie) {
                LOG.log(Level.WARNING,
                        "Failed to close parce file descriptor", ie);
            }
        }
    }

    private void accessoryDetached() {
        destroy();
        if (mListener != null) {
            mListener.accessoryDetached();
        }
    }

    private void tryToOpenAccessory() throws VncException {
        if (VncAapBearer.getParcelFileDescriptor() != null) {
            return;
        }

        // Monitor usb state changes so that disconnections are
        // noticed immediately
        if (mBroadcastReceiver == null) {
            registerBroadcastReceiver();
        }

        // Try to open a matching accessory
        ParcelFileDescriptor pfd = OpenFileDescriptor();

        if (pfd != null) {
            // Make the accessory available to the bearer
            VncAapBearer.setParcelFileDescriptor(pfd);
        }
    }

    /**
     * Checks if AAP is ready, launching a permissions request if necessary.
     * @return true if it is ready, false otherwise.
     * @throw VncException if no accessory is available.
     */
    public boolean readyForConnection() throws VncException {
        tryToOpenAccessory();
        return VncAapBearer.getParcelFileDescriptor() != null;
    }

    /**
     * Releases any claimed resources.
     */
    public void destroy() {
        unregisterBroadcastReceiver();
        closeParcelFileDescriptor();
    }

    public static AapConnectionManager create(Context ctx, Listener listener) {
        AapConnectionManager ret = new AapConnectionManager(ctx, listener);

        try {
            ret.loadUsbClasses();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to load AAP classes", e);
            return null;
        } catch (LinkageError e) {
            LOG.log(Level.SEVERE, "Failed to load AAP classes", e);
            return null;
        }

        return ret;
    }

}
