/*
 * VncTcpOutBearer.java
 *
 * This is sample code intended to demonstrate part of the
 * VNC Mobile Solution SDK. It is not intended as a production-ready
 * component.
 *
 * Copyright (C) 2011-2018 RealVNC Ltd. All Rights Reserved.
 * Confidential and proprietary.
 */

package com.realvnc.vncserver.android.bearers;

import android.content.Context;

import com.realvnc.vncserver.android.VncVersionInfo;
import com.realvnc.vncserver.core.VncBearer;
import com.realvnc.vncserver.core.VncBearerCallbacks;
import com.realvnc.vncserver.core.VncBearerInfo;
import com.realvnc.vncserver.core.VncCommandStringBase;
import com.realvnc.vncserver.core.VncConnection;
import com.realvnc.vncserver.core.VncException;
import com.realvnc.vncserver.core.VncServerCoreErrors;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements a pluggable bearer for outbound TCP
 * connections.
 */
public class VncTcpOutBearer extends VncTcpBearerBase implements VncBearer {
    private static final Logger LOG = Logger.getLogger("com.realvnc.bearer.tcpout");

    /**
     * Representation of an outbound TCP connection.
     */
    public class VncTcpOutConnection extends VncTcpConnectionBase
        implements VncConnection {

        private InetSocketAddress peerAddress;
        protected Socket socket;
        private String address;
        private int port;

        public VncTcpOutConnection (String address, int port) {
            this.address = address;
            this.port = port;
        }

        private boolean anyNonLoopbackInterfaces() {
            try {
                Enumeration<?> ifs = NetworkInterface.getNetworkInterfaces();

                while(ifs.hasMoreElements()) {
                    NetworkInterface nif = (NetworkInterface) ifs.nextElement();
                    Enumeration<?> addrs = nif.getInetAddresses();

                    while(addrs.hasMoreElements()) {
                        InetAddress addr = (InetAddress) addrs.nextElement();
                        if(!addr.isLoopbackAddress() && !addr.isLinkLocalAddress())
                            return true;
                    }
                }

            } catch(SocketException e) {
                LOG.log(Level.SEVERE, "getLocalAddress(): exception", e);
            }
            return false;
        }

        public boolean establish () throws VncException {

            if(!anyNonLoopbackInterfaces() && !allowLoopbackConnection)
                throw new VncException(VncServerCoreErrors.VNCSERVER_ERR_NETWORK_LOST);

            try {
                // We only support IPv4 connections for now.
                InetAddress ipv4Addr = null;
                InetAddress[] addrs = InetAddress.getAllByName(address);
                for (InetAddress addr : addrs) {
                    if (addr instanceof Inet4Address) {
                        ipv4Addr = addr;
                        break;
                    }
                }
                if (ipv4Addr == null) {
                    LOG.severe("establish(): couldn't resolve to an IPv4 address");
                    throw new VncException(VncServerCoreErrors.VNCSERVER_ERR_HOST_UNREACHABLE);
                }
                peerAddress = new InetSocketAddress(ipv4Addr, port);
            } catch(UnknownHostException e) {
                throw new VncException(VncServerCoreErrors.VNCSERVER_ERR_NAME_LOOKUP_FAILED);
            }

            try {
                socket = new Socket();
                LOG.info("Connecting to " + peerAddress.toString());
                socket.connect(peerAddress);
            } catch(IOException e) {
                LOG.severe("Exception '" + e.getMessage() + "' while connecting");
                handleIOException(e);
            }
            return true;
        }

        @Override
        public void close () {
            try {
                socket.shutdownInput();
            } catch(NullPointerException e) {
                // socket not open yet - don't worry about it
            } catch(IOException e) {
                LOG.severe("Ignoring exception during socket close");
            }
            try {
                socket.shutdownOutput();
            } catch(NullPointerException e) {
                // socket not open yet - don't worry about it
            } catch(IOException e) {
                LOG.severe("Ignoring exception during socket close");
            }
            try {
                socket.close();
            } catch(NullPointerException e) {
                // socket not open yet - don't worry about it
            } catch(IOException e) {
                LOG.severe("Ignoring exception during socket close");
            }
            socket = null;
        }

        @Override
        public InputStream getInputStream () {
            Socket sock = socket;
            if(sock == null)
                return null;
            try {
                return sock.getInputStream();
            } catch(IOException e) {
                LOG.log(Level.SEVERE, "exception", e);
                return null;
            }
        }

        @Override
        public OutputStream getOutputStream () {
            Socket sock = socket;
            if(sock == null)
                return null;
            try {
                return sock.getOutputStream();
            } catch(IOException e) {
                LOG.log(Level.SEVERE, "exception", e);
                return null;
            }
        }

        @Override
        public String getLocalAddress () {
            Socket sock = socket;
            if(sock != null) {
                SocketAddress localAddr = sock.getLocalSocketAddress();
                if(localAddr != null)
                    return localAddr.toString();
            }
            return null;
        }

        @Override
        public String getRemoteAddress () {
            Socket sock = socket;
            if(sock != null)
                return sock.getInetAddress().getHostAddress();
            if(peerAddress != null)
                return peerAddress.toString();
            return super.getRemoteAddress();
        }
    }

    /**
     * Create and initialise a new VncTcpOutBearer instance.
     */
    public VncTcpOutBearer (Context ctx) {
        super(ctx);
    }

    /**
     * Create a new connection object which can be used to establish
     * an outbound TCP session over this bearer.  This method will
     * either succeed or throw an exception and will not block.  This
     * call does not cause the connection attempt to be started - for
     * that the establish() method of the VncConnection object should
     * be used.
     *
     * @param commandString details used for establishing the
     * connection
     * @return VncConnection a connection object was successfully
     * created and can be used to establish the connection
     * @throws VncException a connection object could not be created
     */
    public VncConnection createConnection (String address, int port)
        throws VncException
        {
            return new VncTcpOutConnection(address, port);
        }

    public VncConnection createConnection (VncCommandStringBase commandString, VncBearerCallbacks callbacks)
        throws VncException
        {
            String address = getAddress(commandString);
            int port = getPort(commandString);
            return createConnection(address, port);
        }

    /**
     * Returns an object containing descriptive information about the
     * outbound TCP bearer.
     */
    public VncBearerInfo getInfo () {
        return new VncBearerInfo() {
                public String getName() { return "C"; }
                public String getFullName() { return "VNC TCP Connect bearer"; }
                public String getDescription() { return "Makes outgoing TCP connections to a VNC Mobile Viewer or VNC Mobile Server"; }
                public String getVersionString () {
                    return VncVersionInfo.VNC_VERSION;
                }   
            };
    }
}
