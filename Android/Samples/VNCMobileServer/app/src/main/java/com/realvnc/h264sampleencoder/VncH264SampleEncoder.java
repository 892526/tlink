/* Copyright (C) 2017-2018 VNC Automotive Ltd.  All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of a
 * VNC Automotive SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.h264sampleencoder;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import com.realvnc.vncserver.android.VncH264Encoder;
import com.realvnc.vncserver.android.VncSizeInt;

import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.logging.Logger;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class VncH264SampleEncoder extends VncH264Encoder {

    private final String TAG = "VncH264SampleEncoder";

    private enum ExpectedBufferType {
        PARAMETER_SETS,
        FRAME_I,
        FRAME_P
    }

    private static final Logger LOG = Logger.getLogger("VncH264SampleEncoder");

    private static final int MAX_RECT_SIZE_BYTES = 16 * 1024 * 1024; // 16 MiB
    private static final int MAX_ENCODE_PIPELINE_LENGTH = 5;

    // If the codec doesn't support the desired width, try these ones in order.
    private static final int[] COMMON_VIDEO_WIDTHS = {1920, 1600, 1400, 1360,
            1280, 1200, 1080, 1024, 900, 800, 768, 720, 640, 600, 480, 400, 360};

    // Reused to avoid unnecessary allocation/GC
    private final MediaCodec.BufferInfo mBufferInfo
            = new MediaCodec.BufferInfo();

    private static final ByteBuffer mEncodedDataBuffer = ByteBuffer.allocateDirect(MAX_RECT_SIZE_BYTES);

    private H264MediaCodec mH264Codec;
    private ExpectedBufferType mExpectedBufferType;

    private int mCurrentPipelineLength;

    private boolean mIsFirstFrameAfterEncoderStart;

    public VncH264SampleEncoder() {
    }

    @Override
    public boolean queryResolutionSupport(
            final int width,
            final int height,
            final int h264Level,
            final int h264Profile) {

        final MediaCodecInfo.VideoCapabilities videoCapabilities
                = H264MediaCodec.getVideoCapabilities();

        if(videoCapabilities == null) {
            // Hardware encoder unavailable
            return false;
        }

        if(((width & 0x07) + (height & 0x07)) != 0) {
            return false;
        }

        Log.i(TAG, "queryResolutionSupport w:"+width+",h:"+height+"@"+videoCapabilities.isSizeSupported(width,height));


        return videoCapabilities.isSizeSupported(width, height);
    }

    @Override
    public VncSizeInt getNearestSupportedResolution(
            final int width,
            final int height,
            final int h264Level,
            final int h264Profile) {

        final MediaCodecInfo.VideoCapabilities videoCapabilities
                = H264MediaCodec.getVideoCapabilities();

        if(videoCapabilities == null) {
            // Hardware encoder unavailable
            return null;
        }

        final float ratio = (float)height / (float)width;

        for(final int widthToTry : COMMON_VIDEO_WIDTHS) {

            if(widthToTry < width) {

                final int heightToTry = ((int)Math.ceil(ratio * (float)widthToTry)) & 0xfffff0;

                if(videoCapabilities.isSizeSupported(widthToTry, heightToTry)) {

                    LOG.info(String.format(
                            Locale.US,
                            "Nearest supported is %dx%d",
                            widthToTry,
                            heightToTry));

                    return new VncSizeInt(widthToTry, heightToTry);

                } else {
                    LOG.info(String.format(
                            Locale.US,
                            "%dx%d unsupported",
                            widthToTry,
                            heightToTry));
                }
            }
        }

        int widthToTry2  = width & 0xfffffff0;
        int heightToTry2 = ((int)Math.ceil(ratio * (float)widthToTry2)) & 0xfffff0;
        if(videoCapabilities.isSizeSupported(widthToTry2, heightToTry2)) {
            return new VncSizeInt(widthToTry2, heightToTry2);
        }

        return null;
    }

    @Override
    public Surface startEncoder(
            final int width,
            final int height,
            final int h264Level,
            final int h264Profile) {

        LOG.info(String.format(
                Locale.US,
                "Starting encoder (%dx%d, %d, %d)",
                width,
                height,
                h264Level,
                h264Profile));

        if(mH264Codec != null) {
            throw new RuntimeException("Encoder already running");
        }

        try {
            mH264Codec = new H264MediaCodec(width, height, h264Level);
            mExpectedBufferType = ExpectedBufferType.PARAMETER_SETS;
            mCurrentPipelineLength = 0;

        } catch(final H264MediaCodec.H264MediaCodecException e) {
            throw new RuntimeException("Failed to instantiate encoder", e);
        }

        mIsFirstFrameAfterEncoderStart = true;
        return mH264Codec.getInputSurface();
    }

    @Override
    public boolean onIFrameRequired() {
        // If the next frame is not the first frame after the H264MediaCodec
        // is started, then we cannot ensure that the next frame encoded will
        // be an I-Frame.
        return mIsFirstFrameAfterEncoderStart;
    }

    @Override
    public FrameType encodeFrame(
            final ScreenGrabHelper screenGrabHelper,
            final BufferOwner outputBufferOwner,
            final int reservedBytesAtBufferStart) {

        if(mH264Codec == null) {
            throw new RuntimeException("Encoder not running");
        }

        // Nifty trick -- let's leave enough room at the beginning of the buffer
        // for both the SDK reserved bytes, and the parameter sets (just in case
        // they're needed).

        final int dataStartPosition = reservedBytesAtBufferStart
                + mH264Codec.getParameterSetsLengthBytes();

        mEncodedDataBuffer.position(dataStartPosition);
        mEncodedDataBuffer.limit(mEncodedDataBuffer.capacity());

        boolean attemptedFlush = false;
        boolean gotParameterSetsForThisRectangle = false;

        mIsFirstFrameAfterEncoderStart = false;

        while(true) {

            final H264MediaCodec.ReadBufferResult result;

            try {
                result = mH264Codec.readBuffer(
                        mEncodedDataBuffer,
                        mBufferInfo);

            } catch(final H264MediaCodec.H264MediaCodecException e) {
                throw new RuntimeException("Got exception when reading buffer", e);
            }

            switch(result) {

                case P_FRAME_PARTIAL:

                    if(mExpectedBufferType != ExpectedBufferType.FRAME_P) {
                        throw new RuntimeException("Got "
                                + result.name()
                                + ", expected "
                                + mExpectedBufferType.name());
                    }

                    // Go back around the loop to read the rest
                    break;

                case P_FRAME_END:

                    if(mExpectedBufferType == ExpectedBufferType.FRAME_P) {

                        // Set the position and limit correctly.
                        mEncodedDataBuffer.limit(mEncodedDataBuffer.position());
                        mEncodedDataBuffer.position(
                                dataStartPosition - reservedBytesAtBufferStart);

                        // Give the buffer to the SDK
                        outputBufferOwner.giveBuffer(mEncodedDataBuffer);

                        // We're done. Expecting another P-frame next call.
                        return FrameType.FRAME_TYPE_PFRAME;

                    } else {
                        throw new RuntimeException("Got "
                                + result.name()
                                + ", expected "
                                + mExpectedBufferType.name());
                    }

                case I_FRAME_PARTIAL:

                    // We're always happy to get an I-frame. Go back around the
                    // loop to read the rest
                    break;

                case I_FRAME_END:

                    // We're always happy to get an I-frame. Expecting a P-frame
                    // next call.
                    mExpectedBufferType = ExpectedBufferType.FRAME_P;

                    // The data ends at our current write pointer
                    mEncodedDataBuffer.limit(mEncodedDataBuffer.position());

                    if(gotParameterSetsForThisRectangle) {

                        // Great, we already got the parameter sets. No need to
                        // add them at the beginning.
                        mEncodedDataBuffer.position(
                                dataStartPosition - reservedBytesAtBufferStart);

                    } else {
                        // Uh-oh, looks like we need to insert the parameter
                        // sets at the beginning of the buffer, after the
                        // reserved bytes.
                        mEncodedDataBuffer.position(reservedBytesAtBufferStart);
                        mH264Codec.getParameterSets(mEncodedDataBuffer);

                        if(mEncodedDataBuffer.position() != dataStartPosition) {
                            throw new RuntimeException("Calculation error:"
                                    + " invalid buffer position when inserting"
                                    + " PS before I-frame");
                        }

                        mEncodedDataBuffer.position(0);
                    }

                    // Give the buffer to the SDK
                    outputBufferOwner.giveBuffer(mEncodedDataBuffer);

                    return FrameType.FRAME_TYPE_IFRAME_AND_PS;


                case PARAMETER_SETS:

                    gotParameterSetsForThisRectangle = true;

                    if(mExpectedBufferType == ExpectedBufferType.PARAMETER_SETS) {
                        mExpectedBufferType = ExpectedBufferType.FRAME_I;

                    } else {
                        throw new RuntimeException("Got "
                                + result.name()
                                + ", expected "
                                + mExpectedBufferType.name());
                    }

                    // Loop back around to get the I-frame
                    break;

                case TRY_AGAIN_LATER:

                    if(mCurrentPipelineLength < MAX_ENCODE_PIPELINE_LENGTH) {

                        // We may need to fill up the encoding pipeline: send
                        // another screen grab to the encoder.
                        mCurrentPipelineLength++;
                        screenGrabHelper.forceScreenGrab();

                        LOG.info("Got TRY_AGAIN_LATER: retrying grab (pipeline length "
                                + mCurrentPipelineLength
                                + ")");

                    } else if(!attemptedFlush) {
                        attemptedFlush = true;
                        mH264Codec.flush();

                        mCurrentPipelineLength++;
                        screenGrabHelper.forceScreenGrab();

                        LOG.info("Got TRY_AGAIN_LATER: flushing and retrying"
                                + " grab (pipeline length "
                                + mCurrentPipelineLength
                                + ")");

                    } else {

                        LOG.severe("Got TRY_AGAIN_LATER: giving up (pipeline length "
                                + mCurrentPipelineLength
                                + ")");

                        throw new RuntimeException("Failed to encode frame");
                    }

                    break;

                case OUTPUT_FORMAT_CHANGED:
                    LOG.info("Ignoring OUTPUT_FORMAT_CHANGED");
                    break;
            }
        }
    }

    @Override
    public int getCurrentPipelineLag() {
        return mCurrentPipelineLength;
    }

    @Override
    public void stopEncoder() {

        LOG.info("Stopping encoder");

        if(mH264Codec == null) {
            throw new RuntimeException("Encoder not running");
        }

        mH264Codec.close();
        mH264Codec = null;
        mIsFirstFrameAfterEncoderStart = false;
    }
}
