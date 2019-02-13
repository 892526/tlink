/* Copyright (C) 2002-2018 RealVNC Ltd.  All Rights Reserved.
*/

#include <VNCBearerTcpBase.h>
#include <VNCConnectionTcpBase.h>
#include "vncbearer-L.h"

class VNCConnectionL : public VNCConnectionTcpBase
{
public:
  VNCConnectionL(VNCBearerImpl &bearer,
                 VNCConnectionContext connectionContext)
    : VNCConnectionTcpBase(bearer, connectionContext)
  {
  }

  virtual VNCBearerError establish()
  {
    return listen();
  }

};

class VNCBearerL : public VNCBearerTcpBase
{
public:
  VNCBearerL(const char *bearerName,
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
    *ppConnection = new VNCConnectionL(*this, connectionContext);
    return VNCBearerErrorNone;
  }

  virtual char *getProperty(VNCBearerProperty property) const
  {
    switch (property)
    {
      case VNCBearerPropertyFullName:
        return sdkStrdup("VNC TCP Listen bearer");

      case VNCBearerPropertyDescription:
        return sdkStrdup("Listens for incoming TCP connections from a VNC Mobile Viewer or VNC Mobile Server");

      default:
        return VNCBearerImpl::getProperty(property);
    }
  }
};

#ifdef VNC_BEARER_STATIC
extern "C" VNCBearer *VNCCALL VNCBearerInitialize_L(
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
  return new (std::nothrow) VNCBearerL(
                        bearerName,
                        bearerContext,
                        pBearerInterface,
                        bearerInterfaceSize,
                        pBearerSupportingAPI,
                        bearerSupportingAPISize);
}

