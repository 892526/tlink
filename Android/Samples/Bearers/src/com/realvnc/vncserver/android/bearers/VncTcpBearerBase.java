/*
 * VncTcpBearerBase.java
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

import com.realvnc.vncserver.core.VncCommandStringBase;
import com.realvnc.vncserver.core.VncException;
import com.realvnc.vncserver.core.VncServerCoreErrors;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Abstract base class for TCP bearer classes.  TCP inbound and
 * outbound connections are implemented using separate bearers but
 * common code for each is implemented here in a common base class.
 */
public abstract class VncTcpBearerBase {

    private static final String ADDRESS_KEY = "a";
    private static final String PORT_KEY = "p";
    private static final int MAXIMUM_PORT_VALUE = 65535;
    private static final int MAXIMUM_ADDRESS_LENGTH = 256;

    protected static int getPort(VncCommandStringBase commandString)
        throws VncException {
        int ret = commandString.getInt(PORT_KEY);
        if(ret > MAXIMUM_PORT_VALUE) {
            throw new VncException(VncServerCoreErrors.VNCSERVER_ERR_BAD_PORT);
        }
        return ret;
    }

    protected static String getAddress(VncCommandStringBase commandString)
        throws VncException {
        String ret = commandString.getString(ADDRESS_KEY);
        if(ret == null || ret.length() == 0 ||
                ret.length() > MAXIMUM_ADDRESS_LENGTH) {
            throw new VncException(VncServerCoreErrors.VNCSERVER_ERR_INVALID_COMMAND_STRING);
        }
        return ret;
    }

    /**
     * Abstract base class for TCP connection classes.
     */
    static abstract class VncTcpConnectionBase {
       
        /**
         * If non-null a connection has been established and this is
         * the underlying socket for the connection.
         */
        protected Socket socket;

        /**
         * If non-null a connection has been established and this is
         * the InputStream which can be used to read data from the
         * connection.
         */
        protected InputStream inputStream;

        /**
         * If non-null a connection has been established and this is
         * the OutputStream which can be used to write data to the
         * connection.
         */
        protected OutputStream outputStream;

        /**
         * True if the close() method on this connection has been
         * called.  This is used so that subclasses can defer
         * returning a successful connection if close() is called
         * while they are stuck in a blocking call which can't be
         * interrupted or made to return early.
         */
        protected volatile boolean closed;

        /**
         * If the connection is already established then close it, or if
         * we're still trying to establish a connection give up.  This
         * will cause any blocked calls to establish() to return false at
         * some point in the future but not necessarily immediately.
         */
        public void close () {
            closed = true;
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) { }
                finally {
                    inputStream = null;
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) { }
                finally {
                    outputStream = null;
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) { }
                finally {
                    socket = null;
                }
            }
        }

        /**
         * Once a connection has been established returns an
         * InputStream which can be used to read data over the bearer.
         *
         * @return InputStream or null if the connection was not
         * established
         */
        public InputStream getInputStream () {
            return inputStream;
        }

        /**
         * Once a connection has been established returns an
         * OutputStream which can be used to write data over the
         * bearer.
         *
         * @return OutputStream or null if the connection was not
         * established
         */
        public OutputStream getOutputStream () {
            return outputStream;
        }

        /**
         * Return the local address associated with this connection.
         * By default this is null which indicates that the connection
         * is an out bound one.  A subclass that implements a
         * listening connection should override to provide details on
         * the local address.
         */
        public String getLocalAddress () {
            return null;
        }

        /**
         * Return the remote address associated with this connection.
         * This will be null if the connection has not yet been
         * established.
         */
        public String getRemoteAddress () {
            String address = null;
            if (socket != null) {
                address = socket.getInetAddress().getHostAddress();
            }
            return address;
        }

    };

    /**
     * Android context this bearer should execute in
     */
    private Context mCtx;

    /**
     * Get the Android context. This can be useful for subclasses if
     * they need to talk to other Android system services. */
    protected Context getContext() {
        return mCtx;
    }

    /**
     * True if the bearers should treat loopback as a valid network
     * interface (eg for autotesting)
     */
    protected static boolean allowLoopbackConnection = false;

    public static void setAllowLoopback(boolean allowLoopback) {
        allowLoopbackConnection = allowLoopback;
    }

    /**
     * Create and initialise a new VncTcpBearerBase instance.
     */
    protected VncTcpBearerBase (Context ctx) {
        super();
        mCtx = ctx;
    }

    /**
     * An attempt to establish a connection has thrown an IOException.
     * This method attempts to convert the exception into a more
     * meaningful error code and rethrows the exception as a
     * VncException.
     *
     * @param e exception to be rethrown
     * @throws VncException rethrown from the IOException
     */
    protected void handleIOException (IOException e) throws VncException {
        int errorCode = VncServerCoreErrors.VNCSERVER_ERR_NETWORK_LOST;
        String msg = e.getMessage();

        /* Examine the exception message and attempt to
         * generate a more meaningful error code.
         */
        if (msg != null) {
            if (msg.indexOf("imed out") != -1) // "Timed out" or "The operation timed out"
                errorCode = VncServerCoreErrors.VNCSERVER_ERR_TIMED_OUT;
            else if (msg.startsWith("Peer")) // "Peer refused the connection"
                errorCode = VncServerCoreErrors.VNCSERVER_ERR_CONNECTION_REFUSED;
            else if (msg.indexOf("DNS") != -1) // "DNS error" or "Bad DNS Address"
                errorCode = VncServerCoreErrors.VNCSERVER_ERR_NAME_LOOKUP_FAILED;
            else if (msg.indexOf("unresolved") != -1) // "Host is unresolved"
                errorCode = VncServerCoreErrors.VNCSERVER_ERR_NAME_LOOKUP_FAILED;
            else if (msg.startsWith("No Network")) // "No Network Connection"
                errorCode = VncServerCoreErrors.VNCSERVER_ERR_NETWORK_LOST;
            else if (msg.indexOf("Network unreachable") != -1)
                errorCode = VncServerCoreErrors.VNCSERVER_ERR_HOST_UNREACHABLE;
            else if (msg.startsWith("No route to host"))
                errorCode = VncServerCoreErrors.VNCSERVER_ERR_HOST_UNREACHABLE;
            else if (msg.equals("Port in use"))
                /* Special case - port in use means listening attempt
                 * failed - return in case caller wants to try
                 * again.
                 */
                return;
            else if (msg.startsWith("Interrupted system call"))
                errorCode = VncServerCoreErrors.VNCSERVER_ERR_CONNECTION_CLOSED;
            else if (msg.startsWith("Socket closed"))
                errorCode = VncServerCoreErrors.VNCSERVER_ERR_CONNECTION_CLOSED;
            else
                errorCode = VncServerCoreErrors.VNCSERVER_ERR_CONNECTION_REFUSED;
        }
        throw new VncException(errorCode, e);
    }
}

