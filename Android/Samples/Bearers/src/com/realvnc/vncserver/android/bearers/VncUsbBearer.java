/*
 * VncUsbBearer.java
 *
 * This is sample code intended to demonstrate part of a
 * VNC Automotive SDK. It is not intended as a production-ready
 * component.
 *
 * Copyright (C) 2011-2018 VNC Automotive Ltd.  All Rights Reserved.
 * Confidential and proprietary.
 */

package com.realvnc.vncserver.android.bearers;

import android.content.Context;
import android.net.ConnectivityManager;

import com.realvnc.vncserver.android.VncVersionInfo;
import com.realvnc.vncserver.core.VncBearerCallbacks;
import com.realvnc.vncserver.core.VncBearerInfo;
import com.realvnc.vncserver.core.VncCommandStringBase;
import com.realvnc.vncserver.core.VncConnection;
import com.realvnc.vncserver.core.VncException;
import com.realvnc.vncserver.core.VncServerCoreErrors;

import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements a pluggable bearer for inbound TCP
 * connections.
 */
public class VncUsbBearer extends VncTcpInBearer {
    private static final Logger LOG = Logger.getLogger("com.realvnc.bearer.usb");

    private static String[] getActiveTetheredInterfaces(ConnectivityManager cm) {
        try {
            Method meth = cm.getClass().getMethod("getTetheredIfaces", (Class[]) null);
            return (String[])(meth.invoke(cm, (Object[]) null));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Exception when trying to get currently tethered interfaces", e);
        }
        return new String[0];
    }

    private static String[] getUsbInterfaceRegexes(ConnectivityManager cm) {
        try {
            Method getUsb = cm.getClass().getMethod("getTetherableUsbRegexs");
            return (String[])getUsb.invoke(cm);
        } catch(Exception e) {
            LOG.log(Level.SEVERE, "Exception when trying to get list of USB tethering regexen", e);
        }
        return new String[] { "usb0", "rndis0" };
    }

    private static boolean isUsb(String ifName, Collection<String> usbRegexen) {
        for (String usbRegex : usbRegexen) {
            if (ifName.matches(usbRegex))
                return true;
        }
        return false;
    }

    /*
     * Probes the network interfaces to determine the address to listen on
     * for USB connections. This method is deliberately picky about only
     * working if there is a single USB interface with a single address.
     *
     * This is to be sure that the address bound to is USB only and there
     * aren't any unexpected complications.
     */
    private InetAddress findUsbIfaceAddress() throws VncException {
        try {
            // First get lists of which interfaces are (a) for USB tethering
            // and (b) actively being tethered.
            ConnectivityManager cm = (ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            List<String> activeTethered = Arrays.asList(getActiveTetheredInterfaces(cm));
            List<String> usbRegexen = Arrays.asList(getUsbInterfaceRegexes(cm));

            // The algorithm here we use is decidedly odd. We are trying to deal
            // with the fact that Android devices might theoretically have multiple
            // USB tethering interfaces, but we can listen only on one IP address.
            // The easy option would be to refuse to listen until tethering is enabled,
            // then we can check the currently active tethering interface.
            // However, this causes inconvenience for users where they might want to
            // choose 'Connect via USB' first, then go and turn on tethering.
            // We therefore try quite hard by doing this:
            // 1) See if USB tethering is active.
            //    a) If it's active on a single USB interface, use that address.
            //    b) If it's active on multiple USB interfaces, fail.
            // 2) If USB tethering is inactive...
            //    a) If there's a single USB interface, use that address
            //    b) If there are multiple USB interfaces, fail
            // In fact, case (2a) never seems to exist on Ice Cream Sandwich devices,
            // since it appears that the usb0 interface doesn't come into existence
            // unless tethering is turned on. However, it does work on older Android
            // devices and probably gives the best user experience.
            NetworkInterface iface;

            ArrayList<String> activeUsbTethered = new ArrayList<String>();
            for (String activeIfName : activeTethered) {
                if (isUsb(activeIfName, usbRegexen))
                    activeUsbTethered.add(activeIfName);
            }

            if (!activeUsbTethered.isEmpty()) {
                if (activeUsbTethered.size() > 1) {
                    // Multiple active USB tethering interfaces
                    LOG.severe("Too many active USB tethering interfaces found; unable to locate unique address on which to listen: "+activeUsbTethered.toString());
                    throw new VncException(VncServerCoreErrors.VNCSERVER_ERR_NETWORK);
                }
                // Single active USB tethering interface
                String ifName = activeUsbTethered.remove(0);
                iface = NetworkInterface.getByName(ifName);
            } else {
                // No active USB tethering interfaces
                ArrayList<NetworkInterface> inActiveUsbTethered = new ArrayList<NetworkInterface>();
                Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
                while(ifaces != null && ifaces.hasMoreElements()) {
                    NetworkInterface possIface = ifaces.nextElement();
                    if (isUsb(possIface.getName(), usbRegexen))
                        inActiveUsbTethered.add(possIface);
                }
                if (inActiveUsbTethered.size() != 1) {
                    // Multiple inactive USB tethering interfaces
                    LOG.severe("Unable to find one and only one inactive USB interface, instead found: " + inActiveUsbTethered.toString());
                    throw new VncException(VncServerCoreErrors.VNCSERVER_ERR_NETWORK);
                }
                iface = inActiveUsbTethered.remove(0);
            }
            
            /* Have found a single USB interface, check it has a single address */
            InetAddress ret = null;
            Enumeration<InetAddress> addrs = iface.getInetAddresses();
            while(addrs != null && addrs.hasMoreElements()) {
                if (ret != null) {
                    LOG.severe("USB interace " + iface.getName() + " has multiple valid addresses: "+Collections.list(iface.getInetAddresses()).toString());
                    throw new VncException(VncServerCoreErrors.VNCSERVER_ERR_NETWORK);
                }
                InetAddress addr = addrs.nextElement();
                if (addr instanceof Inet4Address)
                    ret = addr;
                else
                    LOG.info("Ignoring non-IPv4 tethering address "+addr.toString());
            }
            if (ret == null) {
                LOG.severe("USB interface " + iface.getName() + " doesn't have any valid addresses");
                throw new VncException(VncServerCoreErrors.VNCSERVER_ERR_NETWORK);
            }
            
            /* Everything ok, return the address */
            LOG.info("Determined that USB tethering IP address is "+ret.toString()+" based on interface "+iface.getName());
            return ret;
        } catch (SocketException e) {
            LOG.severe("Recevied SocketException when attempting to enumerate USB network interfaces.");
        };
        throw new VncException(VncServerCoreErrors.VNCSERVER_ERR_NETWORK);
    }
    


    /**
     * Representation of an inbound TCP connection.
     */
    public class VncUsbConnection extends VncTcpInBearer.VncTcpInConnection {

        public VncUsbConnection() throws VncException {
            /* Bind to the specific address for the USB interface.
             * This is done for security reasons as a USB connection
             * will typically have encryption and authentication
             * disabled. If this code just called:
             *    super(5900);
             * Then it would bind on all interfaces, including wifi and GPRS.
             */
            super(new InetSocketAddress(findUsbIfaceAddress(), 5900));
        }

        @Override
        public boolean establish() throws VncException {
            boolean success = super.establish();
            if(success) {
                InetAddress remote = getRemoteInetAddress();
                InetAddress local = getLocalInetAddress();
                InetAddress localhost = null;
                try {
                    localhost = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    // Ignore
                }
                if(remote != null &&
                        ((local != null && remote.equals(local)) ||
                         (localhost != null && remote.equals(localhost)))) {
                    // Remote host is a local address, don't allow it.
                    close();
                    return false;
                }
            }
            return success;
        }

        @Override
        public String getLocalAddress () {
            return "USB";
        }

        @Override
        public String getRemoteAddress () {
            return "USB";
        }
    }

    /**
     * Create and initialise a new VncUsbBearer instance.
     */
    public VncUsbBearer (Context ctx) {
        super(ctx);
    }

    public VncConnection createConnection (VncCommandStringBase commandString, VncBearerCallbacks callbacks)
        throws VncException
    {
        return new VncUsbConnection();
    }

    /**
     * Returns an object containing descriptive information about the
     * USB bearer.
     */
    public VncBearerInfo getInfo () {
        return new VncBearerInfo() {
            public String getName() { return "USB"; }
            public String getFullName() { return "VNC Automotive USB bearer"; }
            public String getDescription() { return "Listens for USB connections from a VNC Automotive Viewer or VNC Automotive Server"; }
            public String getVersionString () {
                return VncVersionInfo.VNC_VERSION;
            }   
        };
    }
}
