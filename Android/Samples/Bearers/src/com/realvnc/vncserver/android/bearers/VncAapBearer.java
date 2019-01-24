/*
 * VncAapBearerBase.java
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
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;

import com.realvnc.vncserver.android.VncVersionInfo;
import com.realvnc.vncserver.core.VncBearer;
import com.realvnc.vncserver.core.VncBearerCallbacks;
import com.realvnc.vncserver.core.VncBearerInfo;
import com.realvnc.vncserver.core.VncCommandStringBase;
import com.realvnc.vncserver.core.VncConnection;
import com.realvnc.vncserver.core.VncException;
import com.realvnc.vncserver.core.VncServerCoreErrors;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class implements a pluggable bearer for Android Accessory
 * Protocol connections.
 */
public class VncAapBearer implements VncBearer {
    private static final Logger LOG = Logger.getLogger("com.realvnc.bearer.aap");

    // How often to wakeup and check the read thread status while
    // waiting for a read. This is just a precaution as the read thread
    // might exit without notifying the condvar. However usually
    // it does wake up the thread.
    private static final long READ_WAIT_SPIN_TIMEOUT = 30000;

    // A guess at the USB packet size being used.
    // If this isn't an even number then bad things
    // happen in write(...)!
    private static final int USB_PACKET_SIZE = 512;

    // How often to repeat the hello handshake in milliseconds.
    private static final long HELLO_REPEAT_PERIOD = 1000;

    // How long to wait when sending a goodbye message in milliseconds.
    private static final long GOODBYE_TIMEOUT_PERIOD = 1000;

    // Arrays used in handshaking and framing a connection. This is to
    // workaround the fact that the device must send data first as
    // otherwise the connection can get stuck in the driver on the
    // kernel side. It also allows remote application close to be
    // detected even if the USB is still connected.
    //
    // See "AAP-Protocol.txt" in the root of the bearer project
    // for a description of the behaviour.
    private static final byte[] DEVICE_HANDSHAKE_V1 =
      {(byte)0xff, (byte)0xff, 0x00, 0x00, 0x0a, 0x0a, 'D', 'E', 'V' };
    private static final byte[] HOST_HANDSHAKE_V1 =
      {(byte)0xff, (byte)0xff, 0x00, 0x00, 0x0a, 0x0a, 'H', 'O', 'S', 'T'};

    private static final byte[] V2_MESSAGE_HEADER =
      {(byte)0xff, (byte)0xff, 0x00, 0x00, 0x0a, 0x0a,
             0x76,       0x30, 0x32, 0x00, 0x00, 0x00};

    private static final int UUID_LENGTH = 16;

    private static abstract class IoThread extends Thread {
        protected IOException mThreadException = null;
        protected boolean mThreadShuttingDown = false;

        // Buffers to pass data to/from the thread
        protected LinkedList<ByteBuffer> mToThreadBuffers;
        protected LinkedList<ByteBuffer> mFromThreadBuffers;

        // Indicates if there is a stream which is open and using this thread
        protected boolean mClosed = true;

        // Indicates if this thread should be running
        protected boolean mRunning = false;

        // The header to append to all data sent and to
        // expect on all data received
        protected byte[] mHeader = null;

        IoThread(String name) {
            super(name);
            mToThreadBuffers = new LinkedList<ByteBuffer>();
            mFromThreadBuffers = new LinkedList<ByteBuffer>();
        }

        protected abstract void performIo(ByteBuffer buffer) throws IOException;

        public void run() {
            Object monitor = this;
            synchronized(monitor) {
                notifyAll();
            }
            try {
                while (mRunning) {
                    try {
                        ByteBuffer buffer;
                        synchronized (monitor) {
                            while (mToThreadBuffers.isEmpty() && mRunning)
                                monitor.wait(); // wait on buffers or close()

                            buffer = mToThreadBuffers.poll();
                            if (!mRunning || buffer == null) {
                                break; // kill thread
                            }
                        }

                        // The fact that closed can change between this
                        // synchronized block and before/during the
                        // actual read seems worrying, but sadly this is
                        // exactly the 1 special case that gives us a
                        // zombie thread.
                        performIo(buffer);
                        synchronized (monitor) {
                            mFromThreadBuffers.add(buffer);
                        }
                    } catch (InterruptedException e) {
                        mThreadException = new IOException(e);
                        break;
                    } catch (IOException e) {
                        // IOException will be passed on to the calling thread.
                        // Allow this thread to die. Any subsequent attempts at
                        // IO will throw IOException.
                        LOG.log(Level.SEVERE,
                                "IoThread failure",
                                e);
                        mThreadException = e;
                        break;
                    } finally {
                        synchronized (monitor) {
                            monitor.notifyAll(); // wake up any pending IO
                        }
                    }
                }
            } catch (Throwable e) {
                LOG.log(Level.SEVERE,
                        "Received unexpected throwable",
                        e);
                if (mThreadException == null) {
                    if (e instanceof IOException) {
                        mThreadException = (IOException)e;
                    } else {
                        mThreadException = new IOException(e);
                    }
                }
            } finally {
                // Thread exiting, make sure nothing is waiting on this
                // to notify it.
                synchronized (monitor) {
                    LOG.severe("Thread " + getClass().getName() + " is quiting");
                    mThreadShuttingDown = true;
                    monitor.notifyAll(); // wake up any pending IO
                }
            }
        }

        synchronized void startThread() {
            if (mRunning) {
                return;
            }
            try {
                mRunning = true;
                mThreadShuttingDown = false;
                start();
                while (!isAlive()) {
                    wait(); // wait until we know the thread has begun
                            // see comments at the start of run().
                }
            } catch (InterruptedException e) {
                LOG.log(Level.SEVERE,
                        "InterruptedException when waiting to sync with read thread.",
                        e);
            }
        }

        synchronized void stopThread() {
            mRunning = false;
        }

        synchronized void streamCreated() throws VncException {
            if (!mClosed) {
                throw new VncException(VncServerCoreErrors.VNCSERVER_ERR_PORT_IN_USE);
            }
            mClosed = false;
        }

        synchronized void streamClosed() {
            mClosed = true;
            mHeader = null;
            notifyAll();
        }

        synchronized void setHeader(byte[] header) {
            mHeader = header;
        }
    }

    private static class ReadThread extends IoThread {
        // Maximum number of bytes to attempt to read/write in one go. Some
        // versions of the AAP kernel driver will hang on reads which are
        // too large.
        private static final int BUFFER_SIZE = USB_PACKET_SIZE;
        private static final int BUFFER_COUNT = 16;

        private InputStream mStream; // underlying stream
        private WriteThread mWriteThread;
        private long mLastReadTime;

        ReadThread(WriteThread writeThread, InputStream underlyingStream) {
            super("VNC AAP Read Thread");
            mStream = underlyingStream;
            mWriteThread = writeThread;
            for(int i = 0; i < BUFFER_COUNT; ++i) {
                mToThreadBuffers.add(ByteBuffer.allocate(BUFFER_SIZE));
            }
        }

        /* Verify that the data at the start of the buffer matches the
         * expected header.
         *
         * Throws an IOException if the header doesn't match.
         *
         * Returns true on success and false on a disconnection header:
         * a disconnection header is when all the non-matching bytes are zero.
         **/
        private boolean verifyHeaderPresent(ByteBuffer buffer)
            throws IOException {
            boolean ret = true;
            int savedPosition = buffer.position();
            try {
                // Reset the buffer to the start to read the header
                buffer.position(0);
                if (buffer.remaining() < mHeader.length) {
                    throw new IOException("Short framing header on buffer");
                }
                byte[] bufferBytes = buffer.array();
                for (int i = 0; i < mHeader.length; ++i) {
                    if (bufferBytes[i] == mHeader[i])
                        continue;
                    if (mHeader[i] != 0) {
                        throw new IOException("Invalid framing header on read");
                    } else {
                        // So far all non-matching header bytes have been zero,
                        // which indicates a disconnection.
                        ret = false;
                    }
                }
                // All ok, update the saved position if hasn't yet
                // taken into account the header length.
                if (savedPosition < mHeader.length) {
                    savedPosition = mHeader.length;
                }
            } finally {
                buffer.position(savedPosition);
            }
            return ret;
        }

        // wakes the thread up to do the read
        // The timeoutTime is measured in SystemClock.uptimeMillis()
        synchronized int doRead(byte[] b, int off, int len, long timeoutTime)
            throws IOException
        {
            if (!isAlive() || mThreadShuttingDown || mClosed)
                throw new IOException("Failed to read," +
                        " thread is alive: " + isAlive() +
                        " is shutting down: " + mThreadShuttingDown +
                        " is closed: " + mClosed);

            ByteBuffer buffer = null;
            long currentTime = SystemClock.uptimeMillis();
            long toSleep = READ_WAIT_SPIN_TIMEOUT;
            if (timeoutTime != 0 &&
                    toSleep > timeoutTime - currentTime) {
                toSleep = timeoutTime - currentTime;
            }
            try {
                while (!mClosed &&
                        mRunning &&
                        isAlive() &&
                        !mThreadShuttingDown &&
                        mFromThreadBuffers.isEmpty() &&
                        toSleep > 0) {
                    // Wait on close() or read thread.
                    wait(toSleep);
                    toSleep = READ_WAIT_SPIN_TIMEOUT;
                    currentTime = SystemClock.uptimeMillis();
                    if (timeoutTime != 0 &&
                            toSleep > timeoutTime - currentTime) {
                        toSleep = timeoutTime - currentTime;
                    }
                }
            } catch (InterruptedException e) {
                if(mThreadException == null)
                    mThreadException = new IOException(e);
            } finally {
                // regardless of whether there was an exception what we do
                // next depends on this condition
                if (toSleep < 0) {
                    /* Timeout */
                    return 0;
                }
                buffer = mFromThreadBuffers.peekFirst();
                if (buffer == null) {
                    mClosed = true;
                    if(mThreadException != null)
                        throw mThreadException;
                    else
                        throw new IOException();
                }
            }
            if (mHeader != null) {
                // verify that the buffer has the expected header
                if(!verifyHeaderPresent(buffer)) {
                    /* Reached end of stream */
                    return -1;
                }
            }
            // There is a buffer to use
            if (len > buffer.remaining())
                len = buffer.remaining();
            buffer.get(b, off, len);
            if (buffer.remaining() < 1) {
                // Used all of the buffer contents,
                // return it to the free list
                mFromThreadBuffers.removeFirst();
                buffer.clear();
                mToThreadBuffers.push(buffer);
                // Wake up the read thread as it might be waiting
                // for a buffer
                notifyAll();
            }
            return len;
        }

        protected void performIo(ByteBuffer buffer) throws IOException {
            if(!mWriteThread.waitForFirstWrite())
                return;

            byte[] bufferBytes = buffer.array();
            int actualLength = mStream.read(bufferBytes,
                    0, bufferBytes.length);
            if (actualLength >= 0)
                buffer.limit(actualLength);
            else
                throw new IOException("Disconnected");
            mLastReadTime = SystemClock.uptimeMillis();
        }

        long lastReadTime() {
            return mLastReadTime;
        }
    }


    private static class WriteThread extends IoThread {
        // Maximum number of bytes to attempt to read/write in one go. Some
        // versions of the AAP kernel driver will hang on reads which are
        // too large.
        //
        // The minus one is important for performance reasons. We need
        // to make sure that the end of a buffer is not a multiple of
        // packet size (as some AAP kernel drivers don't send
        // ZLPs). If the buffers are exactly multiples of packets then
        // a full buffer will be sent as two transfers, both of about
        // half the size. By making the buffer one less than a packet size
        // a full buffer will be sent as a single transfer.
        //
        // Thirty one packets are chosen as that's the current read
        // transfer size in the viewer side AAP bearer.

        private static final int BUFFER_SIZE = (USB_PACKET_SIZE * 31) -1;
        private static final int BUFFER_COUNT = 4;

        /* Wait for a maximum of 1,000 milliseconds before giving up
         * on the connection attempt */
        private static final int MAX_FIRST_WRITE_RETRY = 20;
        private static final int WRITE_RETRY_WAIT_TIME = 50;

        private OutputStream mStream; // underlying stream
        private boolean mWrittenData;

        WriteThread(OutputStream underlyingStream) {
            super("VNC AAP Write Thread");
            mStream = underlyingStream;
            for(int i = 0; i < BUFFER_COUNT; ++i) {
                mFromThreadBuffers.add(ByteBuffer.allocate(BUFFER_SIZE));
            }
        }

        synchronized int doWrite(byte[] b, int off, int len, long timeoutTime)
            throws IOException {
            int total = 0;
            do {
                int written = doPartialWrite(b, off, len, timeoutTime);
                if (written == 0) {
                    /* Timeout */
                    return total;
                }
                len -= written;
                off += written;
                total += written;
            } while (len > 0);
            return total;
        }

        // Waits until all the outgoing buffers have been written
        synchronized void sync() {
            try {
                while(mFromThreadBuffers.size() != BUFFER_COUNT &&
                        !mClosed &&
                        mRunning &&
                        isAlive()) {
                    wait(READ_WAIT_SPIN_TIMEOUT);
                }
            } catch (InterruptedException ie) {
                /* Do nothing, just return */
            }
        }

        // wakes the thread up to do the write
        synchronized int doPartialWrite(byte[] b, int off, int len,
                long timeoutTime)
            throws IOException
        {
            if (!isAlive() || mThreadShuttingDown || mClosed)
                throw new IOException("Can't write, alive: " + isAlive() +
                                      " shutdown: " + mThreadShuttingDown +
                                      " closed: " + mClosed);

            ByteBuffer buffer = null;
            long currentTime = SystemClock.uptimeMillis();
            long toSleep = READ_WAIT_SPIN_TIMEOUT;
            if (timeoutTime != 0 &&
                    toSleep > timeoutTime - currentTime) {
                toSleep = timeoutTime - currentTime;
            }
            try {
                while (!mClosed &&
                        mRunning &&
                        isAlive() &&
                        !mThreadShuttingDown &&
                        mFromThreadBuffers.isEmpty() &&
                        toSleep >= 0) {
                    // Wait on close() or read thread.
                    wait(toSleep);
                    toSleep = READ_WAIT_SPIN_TIMEOUT;
                    currentTime = SystemClock.uptimeMillis();
                    if (timeoutTime != 0 &&
                            toSleep > timeoutTime - currentTime) {
                        toSleep = timeoutTime - currentTime;
                    }
                }
            } catch (InterruptedException e) {
                if(mThreadException == null)
                    mThreadException = new IOException(e);
            } finally {
                if (toSleep < 0) {
                    /* Timeout */
                    return 0;
                }
                if (mFromThreadBuffers.isEmpty()) {
                    mClosed = true;
                    if(mThreadException != null)
                        throw mThreadException;
                    else
                        throw new IOException();
                }
            }
            buffer = mFromThreadBuffers.remove();
            // Add header if necessary
            if (mHeader != null) {
                buffer.put(mHeader);
            }
            // There is a buffer to use
            if (len > buffer.remaining())
                len = buffer.remaining();
            // Some AAP driver implementations don't correctly send
            // a zero length DATA token if writing a multiple of the
            // packet size. This confuses USB hosts, so decrease
            // the length by one byte.
            if (((buffer.position() + len) % USB_PACKET_SIZE) == 0 &&
                len > 0) {
                --len;
            }
            buffer.put(b, off, len);
            buffer.flip();
            mToThreadBuffers.add(buffer);
            // Wake up the thread as it might be waiting
            // for a buffer
            notifyAll();
            return len;
        }

        protected void performIo(ByteBuffer buffer) throws IOException {
            byte[] bufferBytes = buffer.array();
            int offset = buffer.position();
            int count = buffer.remaining();
            if(mWrittenData) {
                // Already successfully written, so device
                // was ready at least once, so call direct
                mStream.write(bufferBytes, offset, count);
                buffer.clear();
                return;
            } else {
                // The AAP accessory will fail writes with
                // ENODEV if the viewer side hasn't opened
                // the connection yet (setting the alt setting
                // value seems to be the essential factor).
                // This code retries writes which have failed due
                // to ENODEV, as long as it's the first write.
                for(int i = MAX_FIRST_WRITE_RETRY; i >= 0; --i) {
                    try {
                        mStream.write(bufferBytes, offset, count);
                        synchronized(this) {
                            mWrittenData = true;
                            // Wake up any threads sitting in waitForFirstWrite
                            notifyAll();
                        }
                        buffer.clear();
                        return;
                    } catch (IOException e) {
                        e.getMessage();
                        if (i <= 0) {
                            // Retries expired, give up
                            throw e;
                        }
                    }
                    try {
                        synchronized(this) {
                            if(mClosed) {
                                notifyAll();
                                throw new IOException("Connection Closed");
                            }
                            wait(WRITE_RETRY_WAIT_TIME);
                        }
                    } catch(InterruptedException ie) {
                        throw new IOException(ie);
                    }
                }
            }
            // Should be unreachable, but here for completeness
            throw new IOException("Failed to write successfully");
        }

        public synchronized boolean waitForFirstWrite() throws IOException
        {
            while(!mWrittenData && isAlive() && !mThreadShuttingDown)
                try {
                    wait();
                } catch (InterruptedException ie) {
                    throw new IOException(ie);
                }
            return mWrittenData && isAlive() && !mThreadShuttingDown;
        }
    }

    private static ParcelFileDescriptor mParcelFileDescriptor;
    private static ReadThread mReadThread;
    private static WriteThread mWriteThread;

    public static synchronized
        ParcelFileDescriptor getParcelFileDescriptor() {
        return mParcelFileDescriptor;
    }

    // Sets the file descriptor to use for the next connection attempt
    // using this bearer.
    //
    // Ownership of the parcel file descriptor is not transferred; it
    // is the responsibility of the called to close the parcel file
    // descriptor when it is no longer in use by the bearer.
    public static synchronized
        void setParcelFileDescriptor(ParcelFileDescriptor next) {
            if (mReadThread != null) {
                mReadThread.stopThread();
                mReadThread = null;
            }
            if (mWriteThread != null) {
                mWriteThread.stopThread();
                mWriteThread = null;
            }
            mParcelFileDescriptor = next;
            if (mParcelFileDescriptor != null) {
                FileDescriptor fd = mParcelFileDescriptor.getFileDescriptor();
                mWriteThread = new WriteThread(new FileOutputStream(fd));
                mWriteThread.startThread();
                mReadThread = new ReadThread(mWriteThread,
                        new FileInputStream(fd));
                mReadThread.startThread();
            }
    }

    public static class VncAapFramer {
        private VncAapConnection mConnection;
        private ReadThread mReadThread;
        private WriteThread mWriteThread;
        private VncAapInputStream mInputStream;
        private VncAapOutputStream mOutputStream;
        private byte[] mLocalUuidBytes = new byte[UUID_LENGTH];
        private byte[] mRemoteUuidBytes = new byte[UUID_LENGTH];
        private int mFramingVersion;
        private Timer mTimer;

        public VncAapFramer(VncAapConnection connection,
                WriteThread writeThread, ReadThread readThread) {
            mConnection = connection;
            mReadThread = readThread;
            mWriteThread = writeThread;
            UUID uuid = UUID.randomUUID();
            // java.util.UUID doesn't have an easy way of getting a byte[] so
            // use a ByteBuffer
            ByteBuffer bb = ByteBuffer.wrap(mLocalUuidBytes);
            bb.putLong(uuid.getMostSignificantBits());
            bb.putLong(uuid.getLeastSignificantBits());
        }

        private boolean matchesHostHandshakeV1(byte[] buf, int len) {
            if (len != HOST_HANDSHAKE_V1.length)
                /* Too short or too long */
                return false;
            for (int i = 0; i < HOST_HANDSHAKE_V1.length; ++i) {
                if (buf[i] != HOST_HANDSHAKE_V1[i])
                    return false;
            }
            return true;
        }

        private boolean matchesMessageHeaderV2(byte[] buf, int len) {
            if (len < V2_MESSAGE_HEADER.length + UUID_LENGTH + UUID_LENGTH)
                /* Too short */
                return false;
            /* Store the remote UUID */
            System.arraycopy(buf, V2_MESSAGE_HEADER.length,
                    mRemoteUuidBytes, 0, UUID_LENGTH);
            /* Check if the remote thinks it's talking to us */
            byte[] remoteExpectingUid = new byte[UUID_LENGTH];
            System.arraycopy(buf, V2_MESSAGE_HEADER.length + UUID_LENGTH,
                    remoteExpectingUid, 0, UUID_LENGTH);
            if (Arrays.equals(remoteExpectingUid, mLocalUuidBytes)) {
                /* The remote end knows about this end */
                mFramingVersion = 2;
                return true;
            } else {
                /* The remote end doesn't know about this end yet, so
                 * indicate a match but don't start framing for it
                 * just yet. */
                return true;
            }
        }

        private boolean waitForHostHandshake(long timeoutTime) throws IOException {
            byte[] readBuf = new byte[V2_MESSAGE_HEADER.length +
                    UUID_LENGTH + UUID_LENGTH];
            int valid = 0;
            boolean matched = false;
            while(!matched && !mConnection.mClosed) {
                int read = mReadThread.doRead(readBuf,
                        valid, readBuf.length - valid, timeoutTime);
                if (read == 0) {
                    /* Timeout */
                    return false;
                }
                valid += read;
                if (read < HOST_HANDSHAKE_V1.length - valid) {
                    // Reached end of stream
                    throw new IOException("Failed to get host handshake, got EOF");
                }
                if (matchesHostHandshakeV1(readBuf, valid)) {
                    matched = true;
                    mFramingVersion = 1;
                } else if (matchesMessageHeaderV2(readBuf, valid)) {
                    /* Can match framing version 2 but not start
                     * framing for version 2 if the remote end isn't
                     * reporting the UUID of this end yet. */
                    return mFramingVersion == 2;
                } else if (valid == readBuf.length) {
                    // Didn't match so shift everything down a byte
                    System.arraycopy(readBuf, 1, readBuf, 0,
                            readBuf.length - 1);
                    --valid;
                }
            }
            return matched;
        }

        public void establish() throws IOException, VncException {
            boolean success = false;
            do {
                mWriteThread.streamCreated();
                try {
                    mReadThread.streamCreated();
                    try {
                        if (!mConnection.mClosed) {
                            long timeoutTime = SystemClock.uptimeMillis() + HELLO_REPEAT_PERIOD;
                            writeHelloV2(timeoutTime);
                            mWriteThread.doWrite(DEVICE_HANDSHAKE_V1,
                                    0, DEVICE_HANDSHAKE_V1.length, timeoutTime);
                            /* Check that the hellos have all been written.
                             * As gadget drivers will generally always have a
                             * buffer of at least a single packet, this doesn't
                             * guarentee that the host has received the data. */
                            mWriteThread.sync();
                            success = waitForHostHandshake(timeoutTime);
                        }
                    } finally {
                        mReadThread.streamClosed();
                    }
                } finally {
                    mWriteThread.streamClosed();
                }
            } while (!success && !mConnection.mClosed);
            if (!success) {
                throw new IOException("Connection already closed");
            }

            switch (mFramingVersion) {
            case 1: {
                mInputStream = new VncAapInputStream(mReadThread);
                mOutputStream = new VncAapOutputStream(mWriteThread);
                break;
            }
            case 2: {
                byte[] sentHeader =
                    new byte[V2_MESSAGE_HEADER.length +
                            UUID_LENGTH + UUID_LENGTH];
                byte[] receivedHeader =
                    new byte[V2_MESSAGE_HEADER.length +
                            UUID_LENGTH + UUID_LENGTH];
                fillSentHeader(sentHeader, true);
                fillReceivedHeader(receivedHeader);
                mInputStream = new VncAapInputStream(mReadThread);
                mReadThread.setHeader(receivedHeader);
                mOutputStream = new VncAapOutputStream(mWriteThread);
                mWriteThread.setHeader(sentHeader);
                mTimer = new Timer();
                /* Start the periodic writer */
                mTimer.schedule(new TimerTask() {
                    public void run() {
                        try {
                            mWriteThread.doWrite(new byte[0], 0, 0,
                                    SystemClock.uptimeMillis() + HELLO_REPEAT_PERIOD);
                        } catch (IOException ie) {
                            /* Ignore */
                        }
                    }}, HELLO_REPEAT_PERIOD, HELLO_REPEAT_PERIOD);
                /* Start the check for periodic reads */
                mTimer.schedule(new TimerTask() {
                    public void run() {
                        long lastRead = mReadThread.lastReadTime();
                        long now = SystemClock.uptimeMillis();
                        if (now - lastRead > (2 * HELLO_REPEAT_PERIOD)) {
                            // Failed to receive data regularily enough,
                            // abort the connection.
                            LOG.severe("Frame receive timeout, now: " + now +
                                       " last: " + lastRead);
                            try {
                                mInputStream.close();
                                mOutputStream.close();
                            } catch (IOException ioe) {
                                // Ignore
                            }
                        }
                    }}, HELLO_REPEAT_PERIOD, HELLO_REPEAT_PERIOD);
                break;
            }
            default:
                throw new IOException("Unknown framing version");
            }
        }

        private void writeHelloV2(long timeoutTime) throws IOException {
            byte[] output = new byte[V2_MESSAGE_HEADER.length +
                    UUID_LENGTH + UUID_LENGTH];
            fillSentHeader(output, false);
            mWriteThread.doWrite(output,
                    0, output.length, timeoutTime);
        }

        private int fillSentHeader(byte[] output, boolean includeRemote) {
            int offset = 0;
            System.arraycopy(V2_MESSAGE_HEADER, 0,
                    output, 0, V2_MESSAGE_HEADER.length);
            offset += V2_MESSAGE_HEADER.length;
            System.arraycopy(mLocalUuidBytes, 0,
                    output, offset, mLocalUuidBytes.length);
            offset += mLocalUuidBytes.length;
            if (includeRemote) {
                System.arraycopy(mRemoteUuidBytes, 0,
                        output, offset, mRemoteUuidBytes.length);
                offset += mRemoteUuidBytes.length;
            } else {
                for (int i = 0; i < UUID_LENGTH; ++i) {
                    output[offset + i] = 0;
                }
            }
            return offset;
        }

        private int fillReceivedHeader(byte[] output) {
            int offset = 0;
            System.arraycopy(V2_MESSAGE_HEADER, 0,
                    output, 0, V2_MESSAGE_HEADER.length);
            offset += V2_MESSAGE_HEADER.length;
            System.arraycopy(mRemoteUuidBytes, 0,
                    output, offset, mRemoteUuidBytes.length);
            offset += mRemoteUuidBytes.length;
            System.arraycopy(mLocalUuidBytes, 0,
                    output, offset, mLocalUuidBytes.length);
            offset += mLocalUuidBytes.length;
            return offset;
        }

        public VncAapInputStream getInputStream() {
            return mInputStream;
        }

        public VncAapOutputStream getOutputStream() {
            return mOutputStream;
        }

        void close()
            throws IOException {
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
            if (mFramingVersion == 2) {
                // Need to send a disconnect framing message by making
                // the remote UUID be blank.
                byte[] header = new byte[V2_MESSAGE_HEADER.length +
                        UUID_LENGTH + UUID_LENGTH];
                fillSentHeader(header, false);
                mWriteThread.setHeader(header);
                // Make sure the new header will be sent by sending blank
                // user data. Only try for one second though.
                mWriteThread.doWrite(new byte[0], 0, 0,
                        SystemClock.uptimeMillis() + GOODBYE_TIMEOUT_PERIOD);
            }
            mReadThread.streamClosed();
            mWriteThread.streamClosed();
        }
    }

    // Currently, the Android server assumes that if the underlying stream
    // is closed, then any read()s that are blocking on that stream will
    // immediately unblock and throw an exception. This seems to be the case
    // with streams. Sadly, this isn't the case with Files - under Apache 
    // Harmony/Android libcore (class library) their implementation of java.nio
    // is incorrect, and the read() blocks forever. This wrapper for an
    // InputStream ensures that all read()s unblock as expected, at the slight
    // cost of a potential zombie thread. 
    public static class VncAapInputStream extends InputStream {
        private ReadThread mReadThread;

        public VncAapInputStream(ReadThread readThread) throws VncException
        {
            mReadThread = readThread;
            mReadThread.streamCreated();
        }

        @Override
        public void close()
            throws IOException
        {
            mReadThread.streamClosed();
        }

        @Override
        public int read()
            throws IOException
        {
            byte[] buf = new byte[1];
            int ret = mReadThread.doRead(buf, 0 , 1, 0);
            if (ret == 1)
                return buf[0];
            else if (ret == -1)
                return ret;
            else
                throw new IOException("Unexpected short read");
        }

        @Override
        public int read(byte[] b) 
            throws IOException
        {
            return mReadThread.doRead(b, 0, b.length, 0);
        }

        @Override
        public int read(byte[] b, int off, int len) 
            throws IOException
        {
            return mReadThread.doRead(b, off, len, 0);
        }
    }

    public static class VncAapOutputStream extends OutputStream {
        private WriteThread mWriteThread;

        public VncAapOutputStream(WriteThread writeThread) throws VncException
        {
            mWriteThread = writeThread;
            mWriteThread.streamCreated();
        }

        @Override
        public void close()
            throws IOException
        {
            mWriteThread.streamClosed();
        }

        @Override
        public void write(int oneByte)
            throws IOException
        {
            byte[] buf = new byte[1];
            buf[0] = (byte) oneByte;
            mWriteThread.doWrite(buf, 0 , 1, 0);
        }

        @Override
        public void write(byte[] b) 
            throws IOException
        {
            mWriteThread.doWrite(b, 0, b.length, 0);
        }

        @Override
        public void write(byte[] b, int off, int len)
            throws IOException
        {
            mWriteThread.doWrite(b, off, len, 0);
        }
    }

    /**
     * Representation of an connection using the Android Accessory Protocol
     */
    public class VncAapConnection implements VncConnection {
        private VncAapFramer mFramer;
        private VncAapInputStream mStreamIn;
        private VncAapOutputStream mStreamOut;
        private VncAapBearer mBearer;
        private boolean mClosed;

        public VncAapConnection(VncAapBearer bearer, VncCommandStringBase cmdString) throws VncException
        {
        }

        /**
         * Blocks until a connection is established with the host over AAP.
         *
         * @return false if close was called before the connection could
         * be established, or true if the connection was successfully
         * established.
         * @throws VncException an error occurred during the attempt to
         * establish the connection
         */
        public boolean establish () 
            throws VncException
        {
            if (mClosed) {
                close();
                return false;
            }

            // close() may be called while we're establish()ing
            synchronized (this) {
                mFramer = new VncAapFramer(this, mWriteThread, mReadThread);
            }

            if (mClosed) {
                close();
                return false;
            }

            try {
                mFramer.establish();
                mStreamOut = mFramer.getOutputStream();
                mStreamIn = mFramer.getInputStream();
            } catch (IOException e) {
                LOG.log(Level.SEVERE,
                        "Failed to perform handshake with host", e);

                if (mWriteThread == null || !mWriteThread.isAlive()) {
                    // USB has been disconnected
                    throw new VncException(VncServerCoreErrors.VNCSERVER_ERR_USB_NOT_CONNECTED);
                } else {
                    // This means the connection was pulled or reset
                    // while attempting to handshake, seems most sensible
                    // to treat this as a close so that the server
                    // can relisten.
                    close();
                    return false;
                }
            }

            synchronized (this) {
                // If the SDK called close() while we were
                // establish()'ing (unlikely) then we need to return
                // false. We'll actually call .close() again in this
                // case to make sure everything is cleared up.
                if (mClosed) {
                    close();
                    return false;
                } else
                    return true;
            }
        }

        /**
         * If the connection is already established then close it, or if
         * we're still trying to establish a connection give up.  This
         * will cause any blocked calls to {@link establish} to return
         * false at some point in the future but not necessarily
         * immediately.
         */
        public synchronized void close () {
            LOG.info("Closing AAP connection");
            mClosed = true;
            if (mFramer != null) {
                try {
                    mFramer.close();
                } catch (IOException e) {
                    LOG.severe("IOException caught when attempting to close AAP bearer framer connection.");
                }
            }
            // Wake up anything waiting in establish
            notifyAll();
            try {
                if (mStreamIn != null)
                    mStreamIn.close();
                if (mStreamOut != null)
                    mStreamOut.close();
                mStreamIn = null;
                mStreamOut = null;
            } catch (IOException e) {
                LOG.severe("IOException caught when attempting to close AAP bearer connection.");
            }
        }

        /**
         * Once a connection has been established returns an InputStream
         * which can be used to read data over the bearer.
         *
         * @return InputStream or null if the connection was not
         * established
         */
        public InputStream getInputStream () {
            return mStreamIn;
        }

        /**
         * Once a connection has been established returns an OutputStream
         * which can be used to write data over the bearer.
         *
         * @return OutputStream or null if the connection was not
         * established
         */
        public OutputStream getOutputStream () {
            return mStreamOut;
        }

        public String getLocalAddress () {
            return "USB";
        }

        public String getRemoteAddress () {
            return "USB";
        }
    }

    /**
     * Create and initialise a new VncAapBearer instance.
     */
    public VncAapBearer (Context ctx) {
        super();
    }

    /**
     * Create a new connection object which can be used to establish a
     * new transport session over this bearer.  This method must
     * either succeed or throw an exception and should not block.
     * This call does not cause the connection attempt to be started -
     * for that the {@link VncConnection#establish} method of the
     * {@link VncConnection} object should be used.
     *
     * @param commandString details used for establishing the
     * connection
     * @return VncConnection a connection object was successfully
     * created and can be used to establish the connection
     * @throws VncException a connection object could not be created
     */
    public VncConnection createConnection (VncCommandStringBase commandString, VncBearerCallbacks callbacks)
        throws VncException
    {
        return new VncAapConnection(this, commandString);
    }

    /**
     * Returns an object containing descriptive information about the
     * AAP bearer.
     */
    public VncBearerInfo getInfo () {
        return new VncBearerInfo() {
            public String getName() { return "AAP"; }
            public String getFullName() { return "VNC AAP bearer"; }
            public String getDescription() { return "Uses Android Accessory Protocol to communicate with the viewer."; }
            public String getVersionString () {
                return VncVersionInfo.VNC_VERSION;
            }
        };
    }
}
