/* Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
*/

#ifndef __VNCCONNECTIOND_H__
#define __VNCCONNECTIOND_H__

#include <VNCConnectionTcpBase.h>

#include "Buffer.h"

class VNCConnectionD : public VNCConnectionTcpBase
{
public:
  VNCConnectionD(VNCBearerImpl &bearer,
                 VNCConnectionContext connectionContext);

  virtual ~VNCConnectionD();

  virtual VNCBearerError establish();
  virtual VNCBearerError getActivity(VNCConnectionActivity *pActivity);
  VNCBearerError timerExpired();

  virtual void statusChange(VNCConnectionStatus status);

private:
  enum HandshakeMessage
  {
    // client to server
    HandshakeMessageSessionId = 0x00,
    HandshakeMessageChallengeResponse = 0x01,
    HandshakeMessageKeepAlive = 0x02,
    HandshakeMessageStartTransferAck = 0x03,

    // server to client
    HandshakeMessageFailed = 0x80,
    HandshakeMessageChallenge = 0x81,
    HandshakeMessageKeepAliveAck = 0x82,
    HandshakeMessageStartTransfer = 0x83,

    HandshakeMessageNone = 0xffff
  };

  enum ReadResult
  {
    ReadResultDataRelayErrorInvalidMessage = 0x01,
    ReadResultDataRelayErrorUnknownSessionId = 0x02,
    ReadResultDataRelayErrorInvalidResponseToChallenge = 0x03,
    ReadResultDataRelayErrorTimeoutWaitingForPeer = 0x04,
    ReadResultDataRelayErrorUnknown = 0x05,

    ReadResultSuccess = 0x100,
    ReadResultIncomplete,
    ReadResultError,
  };

  VNCBearerError readInput();
  VNCBearerError flushOutput();
  VNCBearerError handshake();

  void writeHandshakeMessage(HandshakeMessage type,
                             const unsigned char *payload,
                             size_t payloadSize);
  inline void writeHandshakeMessage(HandshakeMessage type);
  ReadResult readHandshakeMessage(HandshakeMessage *pType,
                                  size_t *pTotalSize,
                                  const unsigned char **pPayload,
                                  size_t *pPayloadSize);

  Buffer outputBuffer;
  Buffer inputBuffer;
  unsigned char *sessionId;
  unsigned char *sharedSecret;
  size_t sessionIdSize;
  size_t sharedSecretSize;
  bool writeable;

  static const unsigned char ProtocolLeader[];
  static const unsigned char ProtocolVersion;
  static const size_t HandshakeMessageHeaderSize;
  static const int KeepAliveTimeout;
};

inline void VNCConnectionD::writeHandshakeMessage(HandshakeMessage type)
{
  writeHandshakeMessage(type, 0, 0);
}

#endif /* !defined(__VNCCONNECTIOND_H__) */
