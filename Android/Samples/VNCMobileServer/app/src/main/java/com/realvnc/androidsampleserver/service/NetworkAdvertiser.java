/* Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of a
 * VNC Automotive SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.androidsampleserver.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.realvnc.networkadvertisersdk.VNCNetworkAdvertiser;
import com.realvnc.networkadvertisersdk.VNCNetworkAdvertiserDeviceIdentity;
import com.realvnc.networkadvertisersdk.VNCNetworkAdvertiserException;
import com.realvnc.networkadvertisersdk.VNCNetworkAdvertiserIcon;
import com.realvnc.networkadvertisersdk.VNCNetworkAdvertiserIconDetails;
import com.realvnc.networkadvertisersdk.VNCNetworkAdvertiserListener;
import com.realvnc.networkadvertisersdk.VNCNetworkAdvertiserParameter;
import com.realvnc.networkadvertisersdk.VNCNetworkAdvertiserSDK;
import com.realvnc.networkadvertisersdk.VNCNetworkAdvertiserServerDetails;
import com.realvnc.vncserver.android.VncServer;

import java.io.ByteArrayOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the server's VNC Automotive Network Advertiser.
 *
 * The Network Advertiser is used to advertise the server over IP networks.
 * An instance of this class is created and managed by the VncServerService.
 */
public class NetworkAdvertiser implements VNCNetworkAdvertiserListener {

    private static final Logger LOG = Logger.getLogger(NetworkAdvertiser.class.getName());

    // The base product string to use (with an appended version number)
    private static final String BASE_PRODUCT_DETAIL_STRING  = "VNCAutomotive-MobileSolution-Server";

    // VNC Automotive server details
    private static final String SERVER_NAME                 = "VNC Automotive Android Sample Server";
    private static final String SERVER_MANUFACTURER         = "VNC Automotive Ltd";

    // The shared preferences that we use
    private static final String PREFERENCE_VNC_ENABLE_NETWORK_ADVERTISER  = "vnc_enable_network_advertiser";
    private static final String PREFERENCE_VNC_LISTEN_PORT                = "vnc_port";

    // Listener receiving notification events from a NetworkAdvertiser
    public interface Listener {

        /**
         * Called when the VNC Automotive Network Advertiser has stopped running.
         *
         * The error code describing the reason for the stoppage is reported.
         * VNCNetworkAdvertiserException.STOPPED indicates that the advertiser
         * was stopped explicitly.
         *
         * @param error The error code describing the reason.
         */
        public void advertiserStopped(int error);

    }

    private Listener mListener;
    private Context mContext;
    private VncServer mServer;
    private VNCNetworkAdvertiser mAdvertiser;

    // The network interfaces currently registered
    // Accessed with Object monitor held
    private HashMap<String, String> mNetworkInterfaces;

    // Whether or not the server is currently listening
    // Accessed with Object monitor held
    private boolean mServerListening;


    private byte[] drawableToPNGBytes(Drawable drawable) {
        Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                                         drawable.getIntrinsicHeight(),
                                         Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    private VNCNetworkAdvertiserDeviceIdentity createDeviceIdentity() {
        /* Retrieving the IMEI on Android 6.0 onwards requires prompting
         * the user for permission, so we instead use the ANDROID_ID. */
        //noinspection HardwareIds
        String deviceId = Settings.Secure.getString(
                mContext.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        return new VNCNetworkAdvertiserDeviceIdentity(deviceId,
                                                      null,
                                                      null);
    }

    /**
     * Constructs a new NetworkAdvertiser instance for a given VncServer.
     *
     * @param listener The Listener for this instance.
     * @param context The Android context.
     * @param server The VncServer instance.
     * @param serverIcon An icon to use to represent the server, or null if no
     * icon is required.
     *
     * @throws VNCNetworkAdvertiserException if a network advertiser error occurs.
     */
    public NetworkAdvertiser(Listener listener,
                             Context context,
                             VncServer server,
                             Drawable serverIcon) throws VNCNetworkAdvertiserException {
        mListener = listener;
        mContext = context;
        mServer = server;
        mNetworkInterfaces = new HashMap<String, String>();

        // Create an advertiser
        mAdvertiser = VNCNetworkAdvertiserSDK.advertiserCreate(mContext, this);

        // Configure logging
        mAdvertiser.setParameter(VNCNetworkAdvertiserParameter.LOG, "*:30");

        // Set basic device details. In production devices, the unique device
        // name must be unique to each individual device, and be consistent (i.e.
        // persist across reboots).
        UUID uniqueDeviceName = UUID.randomUUID();
        String productDetailString = BASE_PRODUCT_DETAIL_STRING + "/" + mServer.getVersionString();
        mAdvertiser.setDeviceDetails(Build.PRODUCT,
                                     Build.MANUFACTURER,
                                     Build.MODEL,
                                     "", // Model Description
                                     "", // Model Number
                                     uniqueDeviceName,
                                     productDetailString);

        // Set the device identity
        VNCNetworkAdvertiserDeviceIdentity identity = createDeviceIdentity();
        mAdvertiser.setDeviceIdentity(identity);

        // Set the VNC Automotive server details
        VNCNetworkAdvertiserServerDetails serverDetails =
            new VNCNetworkAdvertiserServerDetails(SERVER_NAME,
                                                  mServer.getVersionString(),
                                                  SERVER_MANUFACTURER);
        if (serverIcon != null) {
            // Register the server icon
            VNCNetworkAdvertiserIconDetails iconDetails =
                new VNCNetworkAdvertiserIconDetails("image/png",
                                                    serverIcon.getIntrinsicWidth(),
                                                    serverIcon.getIntrinsicHeight(),
                                                    32);
            byte[] iconData = drawableToPNGBytes(serverIcon);
            VNCNetworkAdvertiserIcon icon = mAdvertiser.iconCreate(iconDetails, iconData);

            // Provide the VNC Automotive server details with the icon
            VNCNetworkAdvertiserIcon[] icons = new VNCNetworkAdvertiserIcon[] { icon };
            mAdvertiser.setGlobalServerDetails(serverDetails, icons);

            // Release our reference to the icon, now that it has been associated
            // with the VNC Automotive server.
            mAdvertiser.iconRelease(icon);
        } else {
            // Provide the VNC Automotive server details with no icon
            mAdvertiser.setGlobalServerDetails(serverDetails, null);
        }
    }

    /**
     * Adds a VNC Automotive license for the Network Advertiser.
     *
     * @param licenseText The entire text of the VNC Automotive license.
     *
     * @throws VNCNetworkAdvertiserException if a network advertiser error occurs.
     */
    public void addLicense(String licenseText) throws VNCNetworkAdvertiserException {
        mAdvertiser.addLicense(licenseText);
    }

    private synchronized HashMap<String, String> getNetworkInterfaces(String ipAddresses) throws SocketException {
        // Tokenize ipAddresses into a list
        ArrayList<String> ipAddressList = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(ipAddresses, ",");
        while (st.hasMoreTokens()) {
            String addrStr = st.nextToken().trim();

            // Extract just the IP address from the URI
            if (addrStr.charAt(0) == '[') {
                // An IPv6 URI, strip the square brackets and port number
                int closeIndex = addrStr.indexOf(']');
                if (closeIndex != -1) {
                    addrStr = addrStr.substring(1, closeIndex);
                }

            } else {
                // An IPv4 URI, strip the port number
                int colonIndex = addrStr.lastIndexOf(':');
                if (colonIndex != -1) {
                    addrStr = addrStr.substring(0, colonIndex);
                }
            }

            ipAddressList.add(addrStr);
        }

        HashMap<String, String> resultMap = new HashMap<String, String>();

        // Enumerate network interfaces, and return those whose IPv4 address
        // appears in ipAddressList.
        Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
        while(ifaces.hasMoreElements()) {
            // Only consider active networks which are not loopback, and which
            // support multicast.
            NetworkInterface iface = ifaces.nextElement();
            if (iface.isUp() &&
                !iface.isLoopback() &&
                iface.supportsMulticast()) {
                String ifaceName = iface.getName();

                // Check this interface's IPv4 addresses, provided they are not loopback or
                // link local.
                Enumeration<InetAddress> addrs = iface.getInetAddresses();
                while(addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (!addr.isLoopbackAddress() &&
                        !addr.isLinkLocalAddress() &&
                        addr instanceof Inet4Address) {

                        String addrStr = addr.getHostAddress();

                        // Is the server listening on this address?
                        if (ipAddressList.contains(addrStr)) {
                            resultMap.put(addrStr, ifaceName);
                            break;
                        }
                    }
                }
            }
        }

        return resultMap;
    }

    /**
     * Notifies that the VNC Automotive server has started listening.
     *
     * The VNC Automotive Network Advertiser is started if not already running. If ipAddresses
     * contains no valid IPv4 addresses, then no network interfaces are registered.
     *
     * @param ipAddresses A comma-separated list containing the IPv4 addresses (if
     * any) that the VNC Automotive server is listening on.
     *
     * @throws VNCNetworkAdvertiserException if a network advertiser error occurs.
     */
    public void serverListening(String ipAddresses) throws VNCNetworkAdvertiserException {

        // Check the default shared preferences file to see if we're enabled
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean isEnabled = prefs.getBoolean(PREFERENCE_VNC_ENABLE_NETWORK_ADVERTISER, true);

        synchronized(this) {
            mServerListening = true;

            if (!isEnabled) {
                return;
            }

            // Set the network interface(s) that the server is listening on
            // Unregister all previous network interfaces
            for (String iface : mNetworkInterfaces.keySet()) {
                mAdvertiser.unregisterInterface(iface);
            }
            mNetworkInterfaces.clear();
        }

        ArrayList<HashMap.Entry<String, String>> addedInterfaces = new ArrayList<HashMap.Entry<String, String>>();

        try {
            HashMap<String, String> ifaces = getNetworkInterfaces(ipAddresses);

            for (HashMap.Entry<String, String> entry : ifaces.entrySet()) {
                String addrStr = entry.getKey();
                String ifaceName = entry.getValue();
                // Register the interface. Keep going if this fails, so that we
                // can add other interfaces if possible.

                if(ifaceName != null && ifaceName.startsWith("ncm")) {
                    LOG.info("Ignoring interface " + ifaceName);

                } else {
                    try {
                        mAdvertiser.registerInterface(ifaceName, 0);
                        addedInterfaces.add(entry);
                        LOG.info("Registered interface " + ifaceName + " (" + addrStr + ")");
                    } catch (VNCNetworkAdvertiserException e) {
                        LOG.log(Level.SEVERE, "Failed to register network interface " + ifaceName, e);
                    }
                }
            }
        } catch (SocketException e) {
            LOG.log(Level.SEVERE, "Failed to enumerate network interfaces", e);
        }

        synchronized(this) {
            for (HashMap.Entry<String, String> entry : addedInterfaces) {
                mNetworkInterfaces.put(entry.getValue(), entry.getKey());
            }
        }

        // Ensure that the advertiser is running. This does nothing if the
        // advertiser is running already.
        mAdvertiser.start();
    }

    /**
     * Notifies that the VNC Automotive server has started connecting to a viewer.
     *
     * The VNC Automotive Network Advertiser continues running, but stops providing command
     * strings.
     */
    public synchronized void serverConnecting() {
        mServerListening = false;
    }

    /**
     * Notifies that the VNC Automotive server has connected to a viewer.
     *
     * The VNC Automotive Network Advertiser continues running, but stops providing command
     * strings.
     */
    public synchronized void serverConnected() {
        mServerListening = false;
    }

    /**
     * Notifies that the VNC Automotive server is no longer connecting, connected or listening.
     *
     * The VNC Automotive Network Advertiser is stopped if running.
     */
    public synchronized void serverStopped() {
        mServerListening = false;

        // Ensure that the advertiser has stopped.
        // This does nothing if it is not currently running.
        mAdvertiser.stop();
    }


    /**
     * From VNCNetworkAdvertiserListener.
     */

    @Override
    public void log(String category, int severity, String text) {
        LOG.info(category + ":" + severity + " " + text);
    }

    @Override
    public void error(int error) {
        String errorName = VNCNetworkAdvertiserSDK.getErrorName(error);
        if (errorName != null) {
            LOG.info("Network advertiser stopped with error " + error + " (" + errorName +")");
        } else {
            LOG.info("Network advertiser stopped with error " + error);
        }

        synchronized(this) {
            // If error is STOPPED, mServerListening has already been set in serverStopped().
            if (error != VNCNetworkAdvertiserException.STOPPED) {
                mServerListening = false;
            }
        }

        mListener.advertiserStopped(error);
    }

    @Override
    public void threadStarted() {
        LOG.info("UPnP device thread started");
    }

    @Override
    public void listening(String interfaceName, int port) {
        LOG.info("Listening on interface " + interfaceName + " (port " + port + ")");
    }

    @Override
    public void interfaceUnregistered(String interfaceName) {
        LOG.info("Interface " + interfaceName + " automatically unregistered");
        synchronized(this) {
            mNetworkInterfaces.remove(interfaceName);
        }
    }

    @Override
    public void globalServerCommandStringRequest(String interfaceName) {
        // Lookup the server IP address for this interface, if the server is listening
        String serverIp = null;
        synchronized (this) {
            if (mServerListening) {
                serverIp = mNetworkInterfaces.get(interfaceName);
            }
        }

        // Formulate the command string, if we have a server IP address
        // Lookup the server's current listening port from the default shared preferences file
        String commandString = "";
        if (serverIp != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            try {
                int listenPort = Integer.parseInt(prefs.getString(PREFERENCE_VNC_LISTEN_PORT, "5900"));
                commandString = String.format(Locale.US, "vnccmd:v=1;t=C;a=%s;p=%d", serverIp, listenPort);
            } catch (NumberFormatException e) {
                LOG.log(Level.SEVERE, "Shared preference '" + PREFERENCE_VNC_LISTEN_PORT + "' contains invalid value", e);
            }
        }

        // Notify the network advertiser of the result of the request
        try {
            mAdvertiser.globalServerCommandStringResult(interfaceName, commandString);
        } catch (VNCNetworkAdvertiserException e) {
            LOG.log(Level.SEVERE, "Failed to provide command string result", e);
        }
    }
}
