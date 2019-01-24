/* Copyright (C) 2017-2018 VNC Automotive Ltd.  All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of a
 * VNC Automotive SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.h264sampleencoder;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.view.Surface;

import com.realvnc.vncserver.android.VncH264Encoder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Logger;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class H264MediaCodec {

    public enum ReadBufferResult {
        P_FRAME_PARTIAL,
        P_FRAME_END,
        I_FRAME_PARTIAL,
        I_FRAME_END,
        PARAMETER_SETS,
        TRY_AGAIN_LATER,
        OUTPUT_FORMAT_CHANGED
    }

    public static class H264MediaCodecException extends Exception {

        public H264MediaCodecException(
                final String message) {

            super(message);
        }

        public H264MediaCodecException(
                final String message,
                final Exception exception) {

            super(message, exception);
        }
    }

    private static final Logger LOG = Logger.getLogger("H264MediaCodec");

    // This constant was only added in Android API level 26, so we define it
    // here to avoid compatibility issues.
    public static final int BUFFER_FLAG_PARTIAL_FRAME = 8;

    // This constant was only added in Android API level 23, so we define it
    // here to avoid compatibility issues.
    public static final String KEY_LEVEL = "level";

    private static final String H264_MIMETYPE = "video/avc";

    // Largely arbitrary values below, as I think these are ignored. We still
    // need to set them, or the MediaCodec throws an exception.
    private static final int PARAMETER_BIT_RATE_MEGABITS_PER_SECOND = 10;
    private static final int PARAMETER_FRAME_RATE = 60;
    private static final int PARAMETER_I_FRAME_INTERVAL_SECS = 120;

    private MediaCodec mCodec;
    private final Surface mInputSurface; // Owned by mCodec

    private ByteBuffer mParameterSetBuffer;

    public static MediaCodecInfo.VideoCapabilities getVideoCapabilities() {

        final MediaCodecInfo[] codecInfos
                = new MediaCodecList(MediaCodecList.REGULAR_CODECS).getCodecInfos();

        for(final MediaCodecInfo info : codecInfos) {

            if(isCodecSuitable(info)) {

                final MediaCodecInfo.CodecCapabilities codecCaps
                        = info.getCapabilitiesForType(H264_MIMETYPE);

                if(codecCaps != null) {

                    final MediaCodecInfo.VideoCapabilities videoCapabilities
                            = codecCaps.getVideoCapabilities();

                    if(videoCapabilities != null) {
                        return videoCapabilities;
                    }
                }
            }
        }

        return null;
    }

    private static boolean isCodecSuitable(final MediaCodecInfo info) {

        if(!info.isEncoder()) {
            return false;
        }

        // Ignore Google's software encoder
        if(info.getName().equals("OMX.google.h264.encoder")) {
            return false;
        }

        if(!Arrays.asList(info.getSupportedTypes())
                .contains(H264_MIMETYPE)) {
            return false;
        }

        return true;
    }


    public H264MediaCodec(
            final int outputWidth,
            final int outputHeight,
            final int h264Level) throws H264MediaCodecException {

        final MediaFormat format;

        try {
            format = new MediaFormat();

            format.setString(MediaFormat.KEY_MIME, H264_MIMETYPE);

            format.setInteger(
                    MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);

            format.setInteger(
                    MediaFormat.KEY_BIT_RATE,
                    PARAMETER_BIT_RATE_MEGABITS_PER_SECOND * 1024 * 1024); // Bits per second

            format.setInteger(
                    MediaFormat.KEY_BITRATE_MODE,
                    MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);

            format.setInteger(MediaFormat.KEY_FRAME_RATE, PARAMETER_FRAME_RATE);

            format.setInteger(
                    MediaFormat.KEY_I_FRAME_INTERVAL,
                    PARAMETER_I_FRAME_INTERVAL_SECS);

            format.setInteger(MediaFormat.KEY_MAX_WIDTH, outputWidth);
            format.setInteger(MediaFormat.KEY_MAX_HEIGHT, outputHeight);

            format.setInteger(MediaFormat.KEY_WIDTH, outputWidth);
            format.setInteger(MediaFormat.KEY_HEIGHT, outputHeight);

            format.setInteger(
                    MediaFormat.KEY_PROFILE,
                    MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline);

            switch(h264Level) {

                case VncH264Encoder.H264_LEVEL_4_1: {

                    LOG.info("Setting H.264 level to 4.1");

                    format.setInteger(
                            KEY_LEVEL,
                            MediaCodecInfo.CodecProfileLevel.AVCLevel41);
                    break;
                }

                default: {

                    LOG.info("Setting H.264 level to 3.1");

                    format.setInteger(
                            KEY_LEVEL,
                            MediaCodecInfo.CodecProfileLevel.AVCLevel31);
                    break;
                }
            }

        } catch(final RuntimeException e) {
            throw new H264MediaCodecException(
                    "Got exception when creating MediaFormat",
                    e);
        }

        try {
            mCodec = MediaCodec.createEncoderByType(H264_MIMETYPE);
            mCodec.reset();

            mCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            mInputSurface = mCodec.createInputSurface();

            mCodec.start();

        } catch(final RuntimeException e) {

            close();

            throw new H264MediaCodecException(
                    "Got exception when starting MediaCodec",
                    e);

        } catch(final IOException e) {

            close();

            throw new H264MediaCodecException(
                    "Got IOException when starting MediaCodec",
                    e);
        }
    }

    public ReadBufferResult readBuffer(
            final ByteBuffer output,
            final MediaCodec.BufferInfo bufferInfo) throws H264MediaCodecException {

        // 100ms is ample time to dequeue a buffer. We don't actually expect to
        // wait this long, because we always know that a buffer is available
        // (except during initialisation, where the encoder pipeline may need
        // filling)
        final long dequeueTimeoutUs = 100 * 1000;

        final int bufId;

        try {
            bufId = mCodec.dequeueOutputBuffer(
                    bufferInfo,
                    dequeueTimeoutUs);

        } catch(final RuntimeException e) {

            throw new H264MediaCodecException(
                    "Got exception from MediaCodec.dequeueOutputBuffer()",
                    e);
        }

        if(bufId >= 0) {

            try {
                final ByteBuffer buf;

                try {
                    buf = mCodec.getOutputBuffer(bufId);

                } catch(final RuntimeException e) {
                    LOG.severe("Got exception from MediaCodec.getOutputBuffer(): "
                            + e.toString());

                    throw new H264MediaCodecException(
                            "Got exception from MediaCodec.getOutputBuffer()",
                            e);
                }

                if(buf == null) {
                    throw new H264MediaCodecException("getOutputBuffer returned null");
                }

                buf.limit(bufferInfo.offset + bufferInfo.size);
                buf.position(bufferInfo.offset);

                output.put(buf);

                final boolean isPS
                        = (bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0;

                final boolean isIFrame
                        = (bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0;

                final boolean isPartialFrame
                        = (bufferInfo.flags & BUFFER_FLAG_PARTIAL_FRAME) != 0;

                if(isPS) {

                    if(isIFrame || isPartialFrame) {
                        throw new H264MediaCodecException(
                                "Invalid flags: isPS="
                                        + isPS
                                        + ", isIFrame="
                                        + isIFrame
                                        + ", isPartialFrame="
                                        + isPartialFrame);
                    }

                    // Save the parameter set data

                    buf.limit(bufferInfo.offset + bufferInfo.size);
                    buf.position(bufferInfo.offset);

                    mParameterSetBuffer = ByteBuffer.allocateDirect(
                            buf.limit() - buf.position());

                    mParameterSetBuffer.put(buf);

                    return ReadBufferResult.PARAMETER_SETS;
                }

                if(isIFrame) {
                    if(isPartialFrame) {
                        return ReadBufferResult.I_FRAME_PARTIAL;
                    } else {
                        return ReadBufferResult.I_FRAME_END;
                    }

                } else {
                    if(isPartialFrame) {
                        return ReadBufferResult.P_FRAME_PARTIAL;
                    } else {
                        return ReadBufferResult.P_FRAME_END;
                    }
                }

            } finally {
                mCodec.releaseOutputBuffer(bufId, false);
            }

        } else {
            switch(bufId) {

                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED: {
                    return ReadBufferResult.OUTPUT_FORMAT_CHANGED;
                }

                case MediaCodec.INFO_TRY_AGAIN_LATER: {
                   return ReadBufferResult.TRY_AGAIN_LATER;
                }

                default: {
                    LOG.severe("Got unexpected buffer ID " + bufId);
                    throw new H264MediaCodecException("Unexpected buffer ID");
                }
            }
        }
    }

    public Surface getInputSurface() {
        return mInputSurface;
    }

    public void flush() {
        mCodec.flush();
    }

    public int getParameterSetsLengthBytes() {

        if(mParameterSetBuffer != null) {
            return mParameterSetBuffer.capacity();

        } else {
            return 0;
        }
    }

    public void getParameterSets(final ByteBuffer output) {

        if(mParameterSetBuffer != null) {
            mParameterSetBuffer.clear();
            output.put(mParameterSetBuffer);
        }
    }

    public void close() {

        if(mCodec != null) {
            mCodec.stop();
            mCodec.reset();
            mCodec.release();
            mCodec = null;
        }
    }
}
