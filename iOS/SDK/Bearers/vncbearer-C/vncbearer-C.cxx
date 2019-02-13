/* Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
*/

#include <VNCBearerTcpBase.h>
#include <VNCConnectionTcpBase.h>
#include "vncbearer-C.h"

class VNCConnectionC : public VNCConnectionTcpBase
{
public:
  VNCConnectionC(VNCBearerImpl &bearer,
                 VNCConnectionContext connectionContext)
    : VNCConnectionTcpBase(bearer, connectionContext)
  {
  }

  virtual VNCBearerError establish()
  {
    return connect();
  }
};

class VNCBearerC : public VNCBearerTcpBase
{
public:
  VNCBearerC(const char *bearerName,
             VNCBearerContext bearerContext,
             VNCBearerInterface *pBearerInterface,
             size_t bearerInterfaceSize,
             const VNCBearerSupportingAPI *pBearerSupportingAPI,
             size_t bearerSupportingAPISize)
    : VNCBearerTcpBase(bearerName,
                       bearerContext,
                       pBearerInterface,
                       bearerInterfaceSize,
                       pBearerSupportingAPI,
                       bearerSupportingAPISize)
  {
  }

  virtual VNCBearerError createConnection(
    VNCConnectionContext connectionContext,
    VNCConnection **ppConnection)
  {
    *ppConnection = new VNCConnectionC(*this, connectionContext);
    return VNCBearerErrorNone;
  }

  virtual char *getProperty(VNCBearerProperty property) const
  {
    switch (property)
    {
      case VNCBearerPropertyFullName:
        return sdkStrdup("VNC Automotive TCP Connect bearer");

      case VNCBearerPropertyDescription:
        return sdkStrdup("Makes outgoing TCP connections to a VNC Automotive Viewer or VNC Automotive Server");

      default:
        return VNCBearerImpl::getProperty(property);
    }
  }
};

#ifdef VNC_BEARER_STATIC
extern "C" VNCBearer *VNCCALL VNCBearerInitialize_C(
#else
extern "C" VNCBearer *VNCCALL VNCBearerInitialize(
#endif
    const char *bearerName,
    VNCBearerContext bearerContext,
    VNCBearerInterface *pBearerInterface,
    size_t bearerInterfaceSize,
    const VNCBearerSupportingAPI *pBearerSupportingAPI,
    size_t bearerSupportingAPISize)
{
  return new (std::nothrow) VNCBearerC(
                        bearerName,
                        bearerContext,
                        pBearerInterface,
                        bearerInterfaceSize,
                        pBearerSupportingAPI,
                        bearerSupportingAPISize);
}

