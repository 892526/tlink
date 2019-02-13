/* Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
*/

#include <VNCBearerTcpBase.h>
#include "VNCConnectionD.h"
#include "vncbearer-D.h"

class VNCBearerD : public VNCBearerTcpBase
{
public:
  VNCBearerD(const char *bearerName,
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
    *ppConnection = new VNCConnectionD(*this, connectionContext);

    return VNCBearerErrorNone;
  }

  virtual char *getProperty(VNCBearerProperty property) const
  {
    switch (property)
    {
      case VNCBearerPropertyFullName:
        return sdkStrdup("VNC Automotive Data Relay bearer");

      case VNCBearerPropertyDescription:
        return sdkStrdup("Makes outgoing TCP connections to a VNC Automotive Data Relay");

      default:
        return VNCBearerImpl::getProperty(property);
    }
  }
};

#ifdef VNC_BEARER_STATIC
extern "C" VNCBearer *VNCCALL VNCBearerInitialize_D(
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
  return new (std::nothrow) VNCBearerD(
                        bearerName,
                        bearerContext,
                        pBearerInterface,
                        bearerInterfaceSize,
                        pBearerSupportingAPI,
                        bearerSupportingAPISize);
}

