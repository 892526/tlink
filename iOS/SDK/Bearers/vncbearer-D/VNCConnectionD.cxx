/* Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
*/

#include "VNCConnectionD.h"
#include "vncsha1.h"

const unsigned char VNCConnectionD::ProtocolLeader[] =
{
  'R', 'E', 'A', 'L', 'V', 'N', 'C', ' ', 'D', 'A', 'T', 'A', '-', 'R', 'L', 'Y'
};
const unsigned char VNCConnectionD::ProtocolVersion = 0;
const size_t VNCConnectionD::HandshakeMessageHeaderSize = 3;
const int VNCConnectionD::KeepAliveTimeout = 5000;

VNCConnectionD::VNCConnectionD(VNCBearerImpl &bearer,
                               VNCConnectionContext connectionContext)
  : VNCConnectionTcpBase(bearer, connectionContext),
    sessionId(NULL),
    sharedSecret(NULL),
    sessionIdSize(0),
    sharedSecretSize(0),
    writeable(false)
{
  sessionId = reinterpret_cast<unsigned char *> (
    getCommandStringField("id", true, &sessionIdSize));

  sharedSecret = reinterpret_cast<unsigned char *> (
    getCommandStringField("s", true, &sharedSecretSize));
}

VNCConnectionD::~VNCConnectionD()
{
  if (sharedSecret)
    bearer.sdkFree(sharedSecret);

  if (sessionId)
    bearer.sdkFree(sessionId);
}

VNCBearerError VNCConnectionD::establish()
{
  VNCBearerError error = VNCBearerErrorInvalidCommandString;

  if (sessionId && sessionIdSize && sharedSecret && sharedSecretSize)
  {
    // The 'a' and 'p' command string fields are exactly as for the base class
    // 'C' (TCP connect outwards) bearer, so just let it deal with them.
    error = connect();
  }

  return error;
}

VNCBearerError VNCConnectionD::getActivity(VNCConnectionActivity *pActivity)
{
  VNCBearerError error = VNCConnectionTcpBase::getActivity(pActivity);

  if (error == VNCBearerErrorNone)
  {
    switch (getStatus())
    {
      case VNCConnectionStatusNegotiatingWithDataRelay:
      case VNCConnectionStatusWaitingForDataRelayPeer:
        if (*pActivity & VNCConnectionActivityReadReady)
          error = readInput();

        // Due to the nature of Winsock, we can get a network error
        // reported at the same time as we successfully read data.  So, if
        // we read data, always attempt to process it, rather than just
        // returning 'disconnected'.  The input may contain a Failed message
        // (see the explanation of the handshake, below) with the reason for
        // the disconnection.
        if (inputBuffer.getUsed())
        {
          VNCBearerError handshakeError = handshake();

          if (handshakeError != VNCBearerErrorNone)
            error = handshakeError;
        }

        if (error == VNCBearerErrorNone)
        {
          if (*pActivity & VNCConnectionActivityWriteReady)
            writeable = true;

          error = flushOutput();
        }

        if (getStatus() != VNCConnectionStatusConnected)
        {
          // As far as the calling SDK is concerned, we're still connecting.
          // Make sure it doesn't attempt to read or write yet.
          *pActivity = VNCConnectionActivityNone;
        }
        else if (writeable)
        {
          // We've just finished the handshake, and the socket is still (as
          // far as we know writeable.  Make sure we pass this notification
          // upwards.
          *pActivity |= VNCConnectionActivityWriteReady;
          writeable = false;
        }

        break;

      case VNCConnectionStatusConnecting:
#ifndef _WIN32
        // If the socket is readable before the handshake starts, then either
        // we're not connected to a read Data Relay, or the connection attempt
        // has failed.  Calling readInput() here detects both of these.
        // On the other hand, if the connection is not readable but is
        // writeable, then chances are that the connection has succeeded.  Have
        // our statusChange() override deal with this.
        if (*pActivity & VNCConnectionActivityReadReady)
          error = readInput();
        else if (*pActivity & VNCConnectionActivityWriteReady)
          statusChange(VNCConnectionStatusConnected);
#endif

        // As far as the calling SDK is concerned, we're still connecting.
        *pActivity = VNCConnectionActivityNone;
        break;

      default:
        // Make sure we've freed our buffers and report the activity to the
        // calling SDK.
        inputBuffer.clear();
        outputBuffer.clear();
        break;
    }
  }

  return error;
}

void VNCConnectionD::statusChange(VNCConnectionStatus status)
{
  // Don't tell the calling SDK that we're connected until we've finished the
  // Data Relay handshake!  As far as it's concerned, we're still establishing
  // the connection.
  if (getStatus() == VNCConnectionStatusConnecting && 
      status == VNCConnectionStatusConnected)
  {
    log("Sending protocol leader and session ID");

    outputBuffer.reserve(sizeof(ProtocolLeader) +
                         HandshakeMessageHeaderSize +
                         sessionIdSize);

    outputBuffer.append(ProtocolLeader, sizeof(ProtocolLeader));
    writeHandshakeMessage(HandshakeMessageSessionId, sessionId, sessionIdSize);

    VNCConnectionTcpBase::statusChange(VNCConnectionStatusNegotiatingWithDataRelay);

    // We periodically send keep alives while we're waiting for the Start
    // Transfer.
    setTimer(KeepAliveTimeout);
  }
  else
  {
    VNCConnectionTcpBase::statusChange(status);
  }
}

VNCBearerError VNCConnectionD::readInput()
{
  VNCBearerError error = VNCBearerErrorNone;

  for (;;)
  {
    if (inputBuffer.getFree() < 2 * (HandshakeMessageSessionId + 0xff))
      inputBuffer.reserve(512);

    size_t size = inputBuffer.getFree();

    if ((error = read(inputBuffer.getData() + inputBuffer.getUsed(),
                      &size)) != VNCBearerErrorNone ||
        size == 0)
    {
      break;
    }

    inputBuffer.use(size);
  }

  return error;
}

VNCBearerError VNCConnectionD::flushOutput()
{
  VNCBearerError error = VNCBearerErrorNone;
  size_t size;

  while (error == VNCBearerErrorNone &&
         writeable &&
         (size = outputBuffer.getUsed()) > 0)
  {
    if ((error = write(outputBuffer.getData(), &size)) == VNCBearerErrorNone)
    {
      outputBuffer.free(size);
      writeable = size > 0;
    }
  }

  return error;
}

// The format of a Data Relay channel protocol message is:
//
//  - message type (one byte)
//  - message version (one byte)
//  - payload size (one byte)
//  - payload (length given by payload size byte)
//
// The only exception to this is the protocol leader, which is always the 16
// bytes 'REALVNC DATA-RLY' sent exactly as is with no header.
//
// Messages that must contain payloads are:
//
//  - HandshakeMessageSessionId
//  - HandshakeMessageChallenge
//  - HandshakeMessageChallengeResponse
//  - HandshakeMessageFailed (an error code)
//
// Other messages must not contain payloads.
//
// A successful handshake is as follows (message payloads in parentheses):
//
// Client                               Data Relay
// ------                               ----------
//
// ProtocolLeader
// SessionId(session ID)
//                                      Challenge(challenge)
// ChallengeResponse(SHA1(shared secret ++ session ID ++ challenge))
//
// (zero or more of)
// KeepAlive
//                                      KeepAliveAck
// 
// (and finally)
//                                      StartTransfer
// StartTransferAck
//
// The Data Relay may send a Failed message at any time before sending
// StartTransfer.  A Failed message always has a one byte payload, which is an
// error code.

VNCBearerError VNCConnectionD::handshake()
{
  VNCBearerError error = VNCBearerErrorNone;
  ReadResult result = ReadResultError;
  HandshakeMessage type;
  size_t totalSize;
  const unsigned char *payload;
  size_t payloadSize;

  while (getStatus() != VNCConnectionStatusConnected &&
         (result = readHandshakeMessage(&type,
                                        &totalSize,
                                        &payload,
                                        &payloadSize)) == ReadResultSuccess)
  {
    switch (type)
    {
      case HandshakeMessageChallenge:
        // Response to the challenge.
        if (payload && payloadSize)
        {
          SHA1_CTX sha1;
          unsigned char response[SHA1_SIZE]; 

          SHA1_Init(&sha1);
          SHA1_Update(&sha1, sharedSecret, static_cast<int32_t> (sharedSecretSize));
          SHA1_Update(&sha1, sessionId, static_cast<int32_t> (sessionIdSize));
          SHA1_Update(&sha1, payload, static_cast<int32_t> (payloadSize));
          SHA1_Final(response, &sha1);

          log("Received challenge - sending response");
          writeHandshakeMessage(HandshakeMessageChallengeResponse,
                                response,
                                sizeof(response));
        }
        else
        {
          result = ReadResultDataRelayErrorInvalidMessage;
        }
        break;

      case HandshakeMessageKeepAliveAck:
        log("Received keep alive ack");

        // It may sometimes take a while for the other peer to establish its
        // own connection to the Data Relay.  We use the keep-alive ack as our
        // cue to notify the application that progress is still being made.
        if (getStatus() != VNCConnectionStatusWaitingForDataRelayPeer)
          statusChange(VNCConnectionStatusWaitingForDataRelayPeer);

        setTimer(KeepAliveTimeout);
        break;

      case HandshakeMessageStartTransfer:
        // The handshake has finished and the connection is fully established.
        log("Received start transfer - sending ack");
        writeHandshakeMessage(HandshakeMessageStartTransferAck);
        writeable = true;
        error = flushOutput();
        statusChange(VNCConnectionStatusConnected);

        // Make sure we don't attempt to send a keep alive after this point!
        setTimer(-1);
        break;

      case HandshakeMessageFailed: // readHandshakeMessage() traps these
      default:
        result = ReadResultDataRelayErrorInvalidMessage;
        break;
    }

    inputBuffer.free(totalSize);
  }

  if ( VNCBearerErrorNone == error )
  {
    switch (result)
    {
      case ReadResultSuccess:
      case ReadResultIncomplete:
        error = VNCBearerErrorNone;
        break;

      case ReadResultDataRelayErrorUnknownSessionId:
        error = VNCBearerErrorUnknownDataRelaySessionId;
        break;

      case ReadResultDataRelayErrorTimeoutWaitingForPeer:
        error = VNCBearerErrorDataRelayChannelTimeout;
        break;

      case ReadResultDataRelayErrorInvalidResponseToChallenge:
        error = VNCBearerErrorDataRelayInvalidResponseToChallenge;
        break;

      case ReadResultDataRelayErrorInvalidMessage:
        error = VNCBearerErrorDataRelayInvalidMessage;
        break;

      case ReadResultDataRelayErrorUnknown:
      case ReadResultError:
      default:
        error = VNCBearerErrorDataRelayProtocolError;
        break;
    }
  }
  return error;
}

VNCBearerError VNCConnectionD::timerExpired()
{
  log("Sending keep alive");
  writeHandshakeMessage(HandshakeMessageKeepAlive);
  
  return flushOutput();
}

void VNCConnectionD::writeHandshakeMessage(HandshakeMessage type,
                                           const unsigned char *payload,
                                           size_t payloadSize)
{
  unsigned char header[HandshakeMessageHeaderSize];

  header[0] = static_cast<unsigned char> (type);
  header[1] = static_cast<unsigned char> (ProtocolVersion);
  header[2] = static_cast<unsigned char> (payloadSize);

  outputBuffer.reserve(HandshakeMessageHeaderSize + payloadSize);
  outputBuffer.append(header, sizeof(header));

  if (payload && payloadSize)
    outputBuffer.append(payload, payloadSize);
}

VNCConnectionD::ReadResult VNCConnectionD::readHandshakeMessage(
    HandshakeMessage *pType,
    size_t *pTotalSize,
    const unsigned char **pPayload,
    size_t *pPayloadSize)
{
  ReadResult result = ReadResultIncomplete;

  *pType = HandshakeMessageNone;
  *pTotalSize = static_cast<size_t>(0u);
  *pPayload = NULL;
  *pPayloadSize = static_cast<size_t>(0u);

  // All messages have a fixed-size header.  If we don't have all of it yet, we
  // return ReadResultIncomplete.
  if (inputBuffer.getUsed() >= HandshakeMessageHeaderSize)
  {
    const unsigned char *const data = inputBuffer.getData();
    HandshakeMessage type = static_cast<HandshakeMessage> (data[0]);
    unsigned char protocolVersion = data[1];
    unsigned char payloadSize = data[2];
    bool shouldHavePayload = false;

    // Validate the protocol version and message type.
    if (protocolVersion != ProtocolVersion)
    {
      result = ReadResultError;
    }
    else if (type == HandshakeMessageFailed ||
             type == HandshakeMessageChallenge)
    {
      shouldHavePayload = true;
    }
    else if (type != HandshakeMessageKeepAliveAck &&
             type != HandshakeMessageStartTransfer)
    {
      result = ReadResultError;
    }

    if (result == ReadResultIncomplete)
    {
      // Check that there is a payload if and only if the message needs one.
      // If there is a payload, but we don't have all of it yet, we return
      // ReadResultIncomplete.
      if (payloadSize)
      {
        if (!shouldHavePayload)
          result = ReadResultError;
        else if (inputBuffer.getUsed() >= HandshakeMessageHeaderSize + payloadSize)
          result = ReadResultSuccess;
      }
      else if (shouldHavePayload)
      {
        result = ReadResultError;
      }
      else
      {
        result = ReadResultSuccess;
      }
    }

    if (result == ReadResultSuccess)
    {
      // If the Data Relay sent a Failed message, return its error code.
      if (type == HandshakeMessageFailed)
      {
        unsigned char error = data[HandshakeMessageHeaderSize];

        logf("Received error %u from Data Relay", error);

        result = error >= ReadResultDataRelayErrorUnknown
          ? ReadResultDataRelayErrorUnknown
          : static_cast<ReadResult> (error);
      }
      else
      {
        *pType = type;
        *pTotalSize = HandshakeMessageHeaderSize + payloadSize;
        *pPayloadSize = payloadSize;
        *pPayload = payloadSize ? &data[HandshakeMessageHeaderSize] : NULL;
      }
    }
  }

  return result;
}

