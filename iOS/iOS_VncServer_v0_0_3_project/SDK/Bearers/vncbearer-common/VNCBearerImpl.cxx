/* Copyright (C) 2002-2018 RealVNC Ltd.  All Rights Reserved.
*/

#include <stdio.h>
#include <string.h>

#include "VNCBearerImpl.h"
#include "VNCConnectionImpl.h"

#ifdef VNC_BEARER_VERSION
#define VNC_QUOTE_(x) #x
#define VNC_QUOTE(x) VNC_QUOTE_(x) 
#define VNC_BEARER_VERSION_STRING VNC_QUOTE(VNC_BEARER_VERSION)
#else
#define VNC_BEARER_VERSION_STRING "<development>"
#endif

VNCBearerImpl::VNCBearerImpl(const char *bearerName,
                             VNCBearerContext bearerContext,
                             VNCBearerInterface *pBearerInterface,
                             size_t bearerInterfaceSize,
                             const VNCBearerSupportingAPI *pBearerSupportingAPI,
                             size_t bearerSupportingAPISize)
  : bearerContext(bearerContext),
    name(bearerName)
{
#undef IMPLEMENT
#define IMPLEMENT(name, implementation) \
  if (offsetof(VNCBearerInterface, name) + sizeof(void *) <= bearerInterfaceSize) \
    pBearerInterface->name = implementation;

  IMPLEMENT(vncBearerTerminate, &VNCBearerImpl::Terminate);
  IMPLEMENT(vncBearerCreateConnection, &VNCBearerImpl::CreateConnection);
  IMPLEMENT(vncBearerGetProperty, &VNCBearerImpl::GetProperty);

  IMPLEMENT(vncConnectionEstablish, &VNCConnectionImpl::Establish);
  IMPLEMENT(vncConnectionDestroy, &VNCConnectionImpl::Destroy);
  IMPLEMENT(vncConnectionRead, &VNCConnectionImpl::Read);
  IMPLEMENT(vncConnectionWrite, &VNCConnectionImpl::Write);
  IMPLEMENT(vncConnectionGetEventHandle, &VNCConnectionImpl::GetEventHandle);
  IMPLEMENT(vncConnectionGetActivity, &VNCConnectionImpl::GetActivity);
  IMPLEMENT(vncConnectionGetProperty, &VNCConnectionImpl::GetProperty);
  IMPLEMENT(vncConnectionTimerExpired, &VNCConnectionImpl::TimerExpired);

  memset(&api, 0, sizeof(api));
  memcpy(&api,
         pBearerSupportingAPI,
         bearerSupportingAPISize < sizeof(api) ? bearerSupportingAPISize
                                               : sizeof(api));
}

VNCBearerImpl::~VNCBearerImpl()
{
}

char *VNCBearerImpl::getProperty(VNCBearerProperty property) const
{
  switch (property)
  {
    case VNCBearerPropertyName:
      return sdkStrdup(getName());

    case VNCBearerPropertyVersion:
      return sdkStrdup(VNC_BEARER_VERSION_STRING);

    default:
      return NULL;
  }
}

void *VNCBearerImpl::sdkAlloc(size_t size) const
{
  return (*api.vncBearerAlloc)(bearerContext, size);
}

char *VNCBearerImpl::sdkStrdup(const char *s) const
{
  char *result = static_cast<char *> (sdkAlloc(strlen(s) + 1));

#ifdef _MSC_VER
#pragma warning(push)
#pragma warning(disable: 4996)
#endif

  return result ? strcpy(result, s) : NULL;

#ifdef _MSC_VER
#pragma warning(pop)
#endif
}

void VNCBearerImpl::sdkFree(void *buffer) const
{
  (*api.vncBearerFree)(bearerContext, buffer);
}

void VNCBearerImpl::log(const char *text) const
{
  (*api.vncBearerLog)(bearerContext, text);
}

void VNCBearerImpl::logf(const char *format, ...) const
{
  va_list ap;

  va_start(ap, format);
  logv(format, ap);
  va_end(ap);
}

void VNCBearerImpl::logv(const char *format, va_list ap) const
{
  char buf[256];
#if defined(_WIN32_WCE)
  _vsnprintf(buf, sizeof(buf), format, ap);
#elif defined(_WIN32)
  vsnprintf_s(buf, sizeof(buf), _TRUNCATE, format, ap);
#else
  vsnprintf(buf, sizeof(buf), format, ap);
#endif

  buf[sizeof(buf) - 1] = '\0';
  log(buf);
}

VNCBearerDynamicContext VNCBearerImpl::getDynamicContext() const
{
  return (*api.vncBearerGetDynamicContext)(bearerContext);
}

void VNCCALL VNCBearerImpl::Terminate(VNCBearer *pBearer)
{
  delete pBearer;
}

VNCBearerError VNCCALL VNCBearerImpl::CreateConnection(
    VNCBearer *pBearer,
    VNCConnectionContext connectionContext,
    VNCConnection **ppConnection)
{
  try
  {
    return pBearer->createConnection(connectionContext, ppConnection);
  }
  catch(const std::exception& e)
  {
    pBearer->logf("VNCBearerImpl::CreateConnection caught exception: %s\n", e.what());
  }
  catch(...)
  {
    pBearer->logf("VNCBearerImpl::CreateConnection caught unknown exception\n");
  }

  return VNCBearerErrorFailed;
}

char *VNCCALL VNCBearerImpl::GetProperty(VNCBearer *pBearer,
                                         VNCBearerProperty property)
{
  try
  {
    return pBearer->getProperty(property);
  }
  catch(const std::exception& e)
  {
    pBearer->logf("VNCBearerImpl::GetProperty caught exception: %s\n", e.what());
  }
  catch(...)
  {
    pBearer->logf("VNCBearerImpl::GetProperty caught unknown exception\n");
  }

  return NULL;
}

