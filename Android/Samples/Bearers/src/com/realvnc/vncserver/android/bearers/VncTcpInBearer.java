/*
 * VncTcpInBearer.java
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
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements a pluggable bearer for inbound TCP
 * connections.
 */
public class VncTcpInBearer extends VncTcpBearerBase implements VncBearer {
    private static final Logger LOG = Logger.getLogger("com.realvnc.bearer.tcpin");

    private int mListeningPort;

    /**
     * Representation of an inbound TCP connection.
     */
    public class VncTcpInConnection extends VncTcpConnectionBase implements VncConnection {

        private ServerSocket listening_socket;
        private Socket connected_socket;
        private SocketAddress mBoundAddress;

        public VncTcpInConnection (SocketAddress bindaddr) throws VncException {
            try {
                listening_socket = new ServerSocket();
                listening_socket.setReuseAddress(true);
                listening_socket.bind(bindaddr);
                mBoundAddress = bindaddr;
            } catch(IOException e) {
                LOG.log(Level.SEVERE, "ListenConnection: exception", e);
                throw new VncException(VncServerCoreErrors.VNCSERVER_ERR_NETWORK, e);
            }
        }

        public VncTcpInConnection (int port) throws VncException {
            try {
                listening_socket = new ServerSocket();
                listening_socket.setReuseAddress(true);
                listening_socket.bind(new InetSocketAddress(port));
            } catch(IOException e) {
                LOG.log(Level.SEVERE, "ListenConnection: exception", e);
                throw new VncException(VncServerCoreErrors.VNCSERVER_ERR_NETWORK, e);
            }
        }

        public boolean establish () throws VncException {

            if(getLocalAddress() == null || getLocalAddress().equals(""))
                throw new VncException(VncServerCoreErrors.VNCSERVER_ERR_NETWORK_LOST);

            try {
                /* listening_socket may be cleared whilst we're about to establish(). We
                 * can't hold a lock whilst accept()'ing, so take a copy of the reference. */
                ServerSocket listening_socket_ref = null;
                synchronized (this) {
                    if (listening_socket == null || listening_socket.isClosed()) {
                        return false;
                    }
                    listening_socket_ref = listening_socket;
                }

                /* On Android accept() can be interrupted (eg by a
                 * signal). We get a SocketTimeoutException when this
                 * happens, and react to it by retrying. */
                boolean loop;
                do {
                    loop = false;
                    try {
                        connected_socket = listening_socket_ref.accept();
                    } catch(SocketTimeoutException e) {
                        LOG.severe("Interrupted accept(), retrying");
                        loop = true;
                    }
                } while(loop);

                listening_socket_ref.close();
            } catch(IOException e) {
                /* Check state of listening_socket under lock to see if we've been closed. */
                boolean isClosed = false;
                synchronized (this) {
                    isClosed = (listening_socket == null || listening_socket.isClosed());
                }

                if(isClosed) {

                    /* This happens when close() is called while we are
                     * trying to accept(). This is a normal situation that
                     * arises when the user selects "Stop Listening". */
                    return false;

                } else {
                    LOG.severe("Exception '" + e.getMessage() + "' while accepting");
                    handleIOException(e);
                }
            }
            return true;
        }

        public void close () {
            /* Set listening_socket to null under lock before closing, to
             * avoid racing with establish(). */
            ServerSocket listening_socket_ref = null;
            synchronized (this) {
                listening_socket_ref = listening_socket;
                listening_socket = null;
            }
            try {
                if(connected_socket != null) {
                    try {
                        connected_socket.shutdownInput();
                    } catch(SocketException e) {
                        // already closed
                    }
                    try {
                        connected_socket.shutdownOutput();
                    } catch(SocketException e) {
                        // already closed
                    }
                    connected_socket.close();
                }
                listening_socket_ref.close();
            } catch(IOException e) {
                LOG.log(Level.SEVERE, "close(): exception", e);
            }
            connected_socket = null;
        }

        public InputStream getInputStream () {
            try {
                return connected_socket.getInputStream();
            } catch(IOException e) {
                LOG.log(Level.SEVERE, "getInputStream(): exception", e);
                return null;
            }
        }

        public OutputStream getOutputStream () {
            try {
                return connected_socket.getOutputStream();
            } catch(IOException e) {
                LOG.log(Level.SEVERE, "getOutputStream(): exception", e);
                return null;
            }
        }

        private String join(Collection<?> s, String delimiter) {
            StringBuffer buffer = new StringBuffer();
            Iterator<?> iter = s.iterator();
            while (iter.hasNext()) {
                buffer.append(iter.next());
                if (iter.hasNext()) {
                    buffer.append(delimiter);
                }
            }
            return buffer.toString();
        }

        public String getLocalAddress () {

            String portStr;

            if(mListeningPort == 5900)
                portStr = "";
            else
                portStr = ":" + mListeningPort;

            if (mBoundAddress != null) {
                /* Bound to a particular IP address so just return that address. */
                return mBoundAddress + portStr;
            }

            try {
                Enumeration<?> ifs = NetworkInterface.getNetworkInterfaces();
                ArrayList<String> all_addrs = new ArrayList<String>();

                while(ifs.hasMoreElements()) {
                    NetworkInterface nif = (NetworkInterface) ifs.nextElement();
                    Enumeration<?> addrs = nif.getInetAddresses();

                    while(addrs.hasMoreElements()) {
                        InetAddress addr = (InetAddress) addrs.nextElement();
                        if(allowLoopbackConnection || (!addr.isLoopbackAddress() && !addr.isLinkLocalAddress())) {
                            if(addr instanceof Inet6Address) {
                                all_addrs.add("[" + addr.getHostAddress() + "]" + portStr);
                            } else {
                                all_addrs.add(addr.getHostAddress() + portStr);
                            }
                        }
                    }
                }

                return join(all_addrs, ", ");

            } catch(SocketException e) {
                LOG.log(Level.SEVERE, "getLocalAddress(): exception", e);
                return "unknown";
            }
        }

        public String getRemoteAddress () {
            if(connected_socket != null)
                return connected_socket.getInetAddress().getHostAddress();
            return null;
        }

        protected InetAddress getRemoteInetAddress() {
            if(connected_socket != null)
                return connected_socket.getInetAddress();
            return null;
        }

        protected InetAddress getLocalInetAddress() {
            if(connected_socket != null)
                return connected_socket.getLocalAddress();
            return null;
        }
    }

    /**
     * Create and initialise a new VncTcpInBearer instance.
     */
    public VncTcpInBearer (Context ctx) {
        super(ctx);
    }

    /**
     * Create a new connection object which can be used to establish
     * an inbound TCP session over this bearer.  This method will
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
    public VncConnection createConnection (int port)
        throws VncException
    {
        mListeningPort = port;
        return new VncTcpInConnection(port);
    }

    public VncConnection createConnection (VncCommandStringBase commandString, VncBearerCallbacks callbacks)
        throws VncException
    {
        int port = getPort(commandString);
        return createConnection(port);
    }

    /**
     * Returns an object containing descriptive information about the
     * inbound TCP bearer.
     */
    public VncBearerInfo getInfo () {
        return new VncBearerInfo() {
            public String getName() { return "L"; }
            public String getFullName() { return "VNC Automotive TCP Listen bearer"; }
            public String getDescription() { return "Listens for incoming TCP connections from a VNC Automotive Viewer or VNC Automotive Server"; }
            public String getVersionString () {
                return VncVersionInfo.VNC_VERSION;
            }
        };
    }
}

