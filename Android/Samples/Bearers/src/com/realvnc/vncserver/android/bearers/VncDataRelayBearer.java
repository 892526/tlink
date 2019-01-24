/*
 * VncDataRelayBearer.java
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class implements a pluggable bearer for data relay connections
 * by extending upon the TCP out bearer.  It uses the TCP out bearer
 * to establish a TCP connection to the data relay, and then performs
 * the required handshaking before indicating that the connection has
 * been successfully established.
 */
public class VncDataRelayBearer extends VncTcpOutBearer implements VncBearer {
    protected static final String SESSION_ID_KEY = "id";
    protected static final String SESSION_SECRET_KEY = "s";
    protected static final int MAXIMUM_SESSION_ID_LENGTH = 32;
    protected static final int MAXIMUM_SESSION_SECRET_LENGTH = 60;

    /**
     * Representation of a data relay connection.  Again this builds
     * upon the connection class from the TCP out bearer.
     */
    class VncDataRelayConnection extends VncTcpOutConnection implements VncConnection {

        /**
         * Session ID for the data relay connection.
         */
        byte[] sessionID;

        /**
         * Session secret for the data relay connection.
         */
        byte[] sessionSecret;

        /**
         * Create a new data relay connection object but don't initiate
         * the connection yet.  Will use the common connect method in
         * the case class to create the server socket.
         * @param url for Connector.open
         * @param sessionID for the data relay connection
         * @param sessionSecret for the data relay connection
         * @throws VncException an error occurred creating the
         * connection object
         */
        VncDataRelayConnection (String address,
                                int port,
                                byte[] sessionID,
                                byte[] sessionSecret)
            throws VncException
        {
            super(address, port);
            this.sessionID = sessionID;
            this.sessionSecret = sessionSecret;
        }

        /**
         * Establish a connection over the bearer using the connection
         * details in the command string passed to the
         * createConnection() method of the object implementing the
         * VncBearer interface.  This call blocks until the connection
         * is full established or an error occurs.  In the case of a
         * listening bearer this would mean that the call blocks until
         * someone connects and completes the connection.
         *
         * @return false if close was called before the connection
         * could be established, or true if the connection was
         * successfully established.
         * @throws VncException an error occurred during the attempt
         * to establish the connection
         */
        public boolean establish () throws VncException {
            boolean established = super.establish();
            if (established) {
                try {
                    VncDataRelayConnector connector =
                        new VncDataRelayConnector(socket.getInputStream(),
                                                  socket.getOutputStream(),
                                                  sessionID,
                                                  sessionSecret);
                    connector.connect();
                    established = true;
                } catch(IOException e) {
                    established = false;
                }
            }
            return established;
        }

        /**
         * If the connection is already established then close it, or if
         * we're still trying to establish a connection give up.  This
         * will cause any blocked calls to establish() to return false at
         * some point in the future but not necessarily immediately.
         */
        public void close () {
            super.close();
        }
    }

    /**
     * Create and initialise a new VncDataRelayBearer instance.
     */
    public VncDataRelayBearer (Context ctx) {
        super(ctx);
    }

    /**
     * Create a new connection object which can be used to establish a
     * data relay connection through bearer.  This method will either
     * succeed or throw an exception and will not block.  This call
     * does not cause the connection attempt to be started - for that
     * the establish() method of the VncConnection object should be
     * used.
     *
     * @param commandString details used for establishing the
     * connection
     * @return VncConnection a connection object was successfully
     * created and can be used to establish the connection
     * @throws VncException a connection object could not be created
     */
    public VncConnection createConnection (String address,
                                           int port,
                                           byte[] sessionID,
                                           byte[] sessionSecret)
        throws VncException
    {
        try {
            return new VncDataRelayConnection(address, port, sessionID, sessionSecret);
        }
        catch (IllegalArgumentException e) {
            throw new VncException(VncServerCoreErrors.VNCSERVER_ERR_INVALID_COMMAND_STRING, e);
        }
    }

    @Override
    public VncConnection createConnection (VncCommandStringBase commandString, VncBearerCallbacks callbacks)
        throws VncException
    {
        byte[] sessionID = 
            commandString.getBase64Value(SESSION_ID_KEY);
        byte[] sessionSecret =
            commandString.getBase64Value(SESSION_SECRET_KEY);
        if(sessionID == null || sessionSecret == null ||
                sessionID.length > MAXIMUM_SESSION_ID_LENGTH ||
                sessionSecret.length > MAXIMUM_SESSION_SECRET_LENGTH)
            throw new VncException(VncServerCoreErrors.VNCSERVER_ERR_INVALID_COMMAND_STRING);
        String address = getAddress(commandString);
        int port = getPort(commandString);
        return createConnection(address, port, sessionID, sessionSecret);
    }

    /**
     * Returns an object containing descriptive information about the
     * data relay bearer.
     */
    public VncBearerInfo getInfo () {
        return new VncBearerInfo() {
            public String getName() { return "D"; }
            public String getFullName() { return "VNC Data Relay bearer"; }
            public String getDescription() { return "Makes outgoing TCP connections to a VNC Data Relay"; }
            public String getVersionString () {
                return VncVersionInfo.VNC_VERSION;
            }   
        };
    }
}

/**
 * A class for setting up connection using the VNC Data Relay.  Speaks
 * Data Relay Transport Protocol to the relay until the connection is
 * either fully established or fails.
 * <p>
 * Note that we need to take some care here when writing messages.  On
 * some platforms writing small amounts of data to the network can be
 * unreliable and it is recommended that writes be batched up wherever
 * possible.  For that reason we take care to compose an entire data
 * relay message into a buffer and sending it in one go rather than
 * sending the message header and body with separate calls to
 * os.write().
 */
class VncDataRelayConnector
{
    private static final int VNCRELAY_KEEPALIVE_INTERVAL_MS = (45*1000);

    private static final String VNCRELAY_LEADER = "REALVNC DATA-RLY";

    private static final byte RELAYMSG_SESSION_ID           = (byte) 0x00;
    private static final byte RELAYMSG_CHALLENGE_RESPONSE   = (byte) 0x01;
    private static final byte RELAYMSG_KEEP_ALIVE           = (byte) 0x02;
    private static final byte RELAYMSG_START_TRANSFER_ACK   = (byte) 0x03;
    private static final byte RELAYMSG_FAILED               = (byte) 0x80;
    private static final byte RELAYMSG_CHALLENGE            = (byte) 0x81;
    private static final byte RELAYMSG_KEEP_ALIVE_ACK       = (byte) 0x82;
    private static final byte RELAYMSG_START_TRANSFER       = (byte) 0x83;

    private static final int VNCRELAY_STARTING              = 0;
    private static final int VNCRELAY_WRITE_LEADER          = 1;
    private static final int VNCRELAY_WRITE_MESSAGE         = 2;
    private static final int VNCRELAY_READ_MSG_HEADER       = 3;
    private static final int VNCRELAY_READ_FAIL_REASON      = 4;
    private static final int VNCRELAY_READ_CHALLENGE        = 5;
    private static final int VNCRELAY_COMPLETE              = 6;
    private static final int VNCRELAY_EOS_ERROR             = 7;

    private static final int MAXMSGSZ = 256;
    private byte[] inbuf;
    private int inbufLen = 0;
    private VncDataRelayKeepAliveThread keepAliveThread;
    private Object keepaliveLock = new Object();
    private long lastIOTime = 0;
    private int state = VNCRELAY_STARTING;

    private InputStream is;
    private OutputStream os;
    private byte[] sessionID;
    private byte[] sessionSecret;

    /**
     * Create a connector object for a new data relay connection.
     * @param is input stream connected to data relay
     * @param os output stream connect to data relay
     * @param sessionID session ID to use for this connection
     * @param sessionSecret shared session secret to use for this
     * connection
     */
    public VncDataRelayConnector (InputStream is, OutputStream os,
                                  byte[] sessionID, byte[] sessionSecret)
    {
        this.is = is;
        this.os = os;
        this.sessionID = sessionID;
        this.sessionSecret = sessionSecret;
        this.inbuf = new byte[MAXMSGSZ];
    }

    /**
     * Set up a connection throw the data relay.  Performs the handshake
     * with the data relay which may block for some time while waiting
     * for a viewer to connect.  If the handshake completes successfully
     * and the connection is established to a viewer then this method
     * will return.
     *
     * @throws VncException there is a problem with the handshake or
     * the data relay connection has timed out
     */
    public void connect () throws VncException {
        try {
            while(true) {
                switch(state) {
                    case VNCRELAY_STARTING:
                        state = VNCRELAY_WRITE_LEADER;
                        relayWriteLeader();
                    break;
                    case VNCRELAY_WRITE_LEADER:
                        state = VNCRELAY_WRITE_MESSAGE;
                        relayWriteSessionID();
                    break;
                    case VNCRELAY_WRITE_MESSAGE:
                        state = VNCRELAY_READ_MSG_HEADER;
                        read(3);
                    break;
                    case VNCRELAY_READ_MSG_HEADER:
                        relayReadMsgHeader();
                    break;
                    case VNCRELAY_READ_FAIL_REASON:
                        relayReadFailReason();
                    break;
                    case VNCRELAY_READ_CHALLENGE:
                        relayReadChallenge();
                    break;
                    case VNCRELAY_EOS_ERROR:
                        throw new IOException("End of stream during write");
                    case VNCRELAY_COMPLETE:
                        return; // Success! 
                }
            }
        } catch (IOException e) {
            throw new VncException(VncServerCoreErrors.VNCSERVER_ERR_NETWORK, e.getMessage());
        } finally {
            stopKeepaliveTimer();
        }
    }

    private void writeBytes (byte type, byte[] msg) throws IOException {
        if (msg.length > 255)
            throw new IOException("data relay connector msg too big");
        byte[] buf = new byte[msg.length + 3];
        buf[0] = type;
        buf[1] = 0;
        buf[2] = (byte) msg.length;
        System.arraycopy(msg, 0, buf, 3, msg.length);
        os.write(buf);
    }

    private void read (int len) throws IOException {
        int count, off = 0;
        while (off < len) {
            count = is.read(inbuf, off, len-off);
            if (count == -1)
                throw new IOException("End of Stream during read");
            off += count;
        }
        inbufLen = len;
        updateIOTime();
    }

    private void relayReadFailReason () throws VncException {
        int error = inbuf[0];
        if (error >= 1 && error <= 4)
            error += VncServerCoreErrors.VNCSERVER_ERR_BAD_MESSAGE - 1;
        else
            error = VncServerCoreErrors.VNCSERVER_ERR_BAD_MESSAGE;
        throw new VncException(error);
    }

    private void relayWriteLeader () throws IOException {
        synchronized(keepaliveLock) {
            os.write(VNCRELAY_LEADER.getBytes());
        }
        updateIOTime();
    }

    private void relayWriteSessionID () throws IOException {
        synchronized(keepaliveLock) {
            writeBytes(RELAYMSG_SESSION_ID, sessionID);
        }
        updateIOTime();
        startKeepaliveTimer();
    }

    private void relayWriteStartTransferAck () throws IOException {
        stopKeepaliveTimer();
        byte buf[] = { RELAYMSG_START_TRANSFER_ACK, 0, 0 };
        synchronized(keepaliveLock) {
            os.write(buf);
        }
    }

    private void relayReadMsgHeader () throws IOException, VncException {
        byte type = inbuf[0];
        byte version = inbuf[1];
        byte payload  = inbuf[2];

        if (version != 0)
            throw new VncException(VncServerCoreErrors.VNCSERVER_ERR_BAD_MESSAGE);

        switch(type)
        {
            case RELAYMSG_FAILED:
                state = VNCRELAY_READ_FAIL_REASON;
                read(1);
                return;

            case RELAYMSG_CHALLENGE:
                state = VNCRELAY_READ_CHALLENGE;
                read(payload);
                return;

            case RELAYMSG_KEEP_ALIVE_ACK:
                state = VNCRELAY_READ_MSG_HEADER;
                read(3);
                return;

            case RELAYMSG_START_TRANSFER:
                state = VNCRELAY_COMPLETE;
                relayWriteStartTransferAck();
                return;

            default:
                throw new VncException(VncServerCoreErrors.VNCSERVER_ERR_BAD_MESSAGE);
        }
    }
    
    private void relayWriteKeepAlive () throws IOException {
        byte[] buf = { RELAYMSG_KEEP_ALIVE, 0, 0 };
        os.write(buf);
        updateIOTime();
    }

    private void relayReadChallenge () throws IOException
    {
        MessageDigest hash;
        try {
            hash = MessageDigest.getInstance("SHA1");
        } catch(NoSuchAlgorithmException e) {
            return;
        }

        /* CHALLENGE_RESPONSE message: "This client to service handshake
         * message has a payload which consists of the SHA-1 hash of the
         * shared secret, session id and nonce bytes concatenated in that
         * order. The shared secret and session id are as previously
         * returned from the reserve channel API on a control connection.
         * The nonce comes from the preceding CHALLENGE message as sent by
         * the service."
         */
        hash.reset();
        hash.update(sessionSecret);
        hash.update(sessionID);
        hash.update(inbuf, 0, inbufLen);

        synchronized(keepaliveLock) {
            writeBytes(RELAYMSG_CHALLENGE_RESPONSE, hash.digest());
        }
        updateIOTime();

        state = VNCRELAY_WRITE_MESSAGE;

        startKeepaliveTimer();
    }

    private void startKeepaliveTimer() {
        if (keepAliveThread == null) {
            keepAliveThread = new VncDataRelayKeepAliveThread();
            keepAliveThread.start();
        }
    }

    private void stopKeepaliveTimer() {
        if (keepAliveThread != null)
            keepAliveThread.interrupt();
        keepAliveThread = null;
    }

    private void updateIOTime() {
        lastIOTime = System.currentTimeMillis();
    }

    /**
     * A separate thread for sending keep alive messages across the
     * connection to the data relay to ensure that the connection is not
     * closed while we're waiting for the other end to connect.
     */
    class VncDataRelayKeepAliveThread extends Thread {
        public VncDataRelayKeepAliveThread() {
            super("VncDataRelayKeepAliveThread");
        }

        public void run() {
            boolean quit = false;
            while(!quit) {
                try {
                    Thread.sleep(1000);
                    synchronized(keepaliveLock) {
                        if (System.currentTimeMillis() - lastIOTime > VNCRELAY_KEEPALIVE_INTERVAL_MS)
                            relayWriteKeepAlive();
                    }
                } catch(InterruptedException e) {
                    quit = true;
                }
                catch(IOException e) {
                    quit = true;
                    state = VNCRELAY_EOS_ERROR;
                }
            }
        }
    }
}

