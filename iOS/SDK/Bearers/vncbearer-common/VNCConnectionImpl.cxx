/* Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
*/

#include <stdarg.h>
#include <stdio.h>

#include "VNCBearerImpl.h"
#include "VNCConnectionImpl.h"

VNCConnectionImpl::VNCConnectionImpl(VNCBearerImpl &bearer,
                                     VNCConnectionContext connectionContext)
  : bearer(bearer),
    connectionContext(connectionContext),
    status(VNCConnectionStatusNone)
{

}

VNCConnectionImpl::~VNCConnectionImpl()
{
}

VNCBearerImpl &VNCConnectionImpl::getBearer() const
{
  return bearer;
}

void VNCConnectionImpl::log(const char *text) const
{
  if (bearer.api.vncConnectionLog)
    (*bearer.api.vncConnectionLog)(connectionContext, text);
  else
    bearer.log(text);
}

void VNCConnectionImpl::logf(const char *format, ...) const
{
  va_list ap;

  va_start(ap, format);
  logv(format, ap);
  va_end(ap);
}

void VNCConnectionImpl::logv(const char *format, va_list ap) const
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

char *VNCConnectionImpl::getCommandStringField(const char *name,
                                               int base64decode,
                                               size_t *pSize) const
{
  return (*bearer.api.vncConnectionGetCommandStringField)(connectionContext,
                                                          name,
                                                          base64decode,
                                                          pSize);
}

void VNCConnectionImpl::statusChange(VNCConnectionStatus newStatus)
{
  status = newStatus;
  (*bearer.api.vncConnectionStatusChange)(connectionContext, newStatus);
}

void VNCConnectionImpl::setTimer(int timeoutMs)
{
  (*bearer.api.vncConnectionSetTimer)(connectionContext, timeoutMs);
}

VNCBearerError VNCConnectionImpl::localFeatureCheck(const unsigned *featureIDs,
                                                    size_t featureIDCount,
                                                    bool *pResult) const
{
  int result = 0;
  VNCBearerError error = (*bearer.api.vncConnectionLocalFeatureCheck)(
      connectionContext,
      featureIDs,
      featureIDCount,
      &result);

  if (error == VNCBearerErrorNone)
    *pResult = result != 0;

  return error;
}

char *VNCConnectionImpl::getBearerConfiguration() const
{
  return (*bearer.api.vncConnectionGetBearerConfiguration)(connectionContext);
}

char *VNCConnectionImpl::getProperty(VNCConnectionProperty property) const
{
  (void) property;
  return NULL;
}

VNCBearerError VNCConnectionImpl::timerExpired()
{
  return VNCBearerErrorNone;
}

VNCBearerError VNCCALL VNCConnectionImpl::Establish(VNCConnection *pConnection)
{
  try
  {
    return pConnection->establish();
  }
  catch(const std::exception& e)
  {
    pConnection->logf("VNCConnectionImpl::Establish caught exception: %s\n", e.what());
  }
  catch(...)
  {
    pConnection->logf("VNCConnectionImpl::Establish caught unknown exception.\n");
  }

  return VNCBearerErrorFailed;
}

void VNCCALL VNCConnectionImpl::Destroy(VNCConnection *pConnection)
{
  delete pConnection;
}

VNCBearerError VNCCALL VNCConnectionImpl::Read(VNCConnection *pConnection,
                                               unsigned char *buffer,
                                               size_t *pBufferSize)
{
  try
  {
    return pConnection->read(buffer, pBufferSize);
  }
  catch(const std::exception& e)
  {
    pConnection->logf("VNCConnectionImpl::Read caught exception: %s\n", e.what());
  }
  catch(...)
  {
    pConnection->logf("VNCConnectionImpl::Read caught unknown exception.\n");
  }

  return VNCBearerErrorFailed;
}

VNCBearerError VNCCALL VNCConnectionImpl::Write(VNCConnection *pConnection,
                                                const unsigned char *buffer,
                                                size_t *pBufferSize)
{
  try
  {
    return pConnection->write(buffer, pBufferSize);
  }
  catch(const std::exception& e)
  {
    pConnection->logf("VNCConnectionImpl::Write caught exception: %s\n", e.what());
  }
  catch(...)
  {
    pConnection->logf("VNCConnectionImpl::Write caught unknown exception.\n");
  }

  return VNCBearerErrorFailed;
}

VNCBearerError VNCCALL VNCConnectionImpl::GetEventHandle(
     VNCConnection *pConnection,
     VNCConnectionEventHandle *pEventHandle,
     int *pWriteNotification)
{
  try
  {
    return pConnection->getEventHandle(pEventHandle, pWriteNotification);
  }
  catch(const std::exception& e)
  {
    pConnection->logf("VNCConnectionImpl::GetEventHandle caught exception: %s\n", e.what());
  }
  catch(...)
  {
    pConnection->logf("VNCConnectionImpl::GetEventHandle caught unknown exception.\n");
  }

  return VNCBearerErrorFailed;
}

VNCBearerError VNCCALL VNCConnectionImpl::GetActivity(
     VNCConnection *pConnection,
     VNCConnectionActivity *pActivity)
{
  try
  {
    return pConnection->getActivity(pActivity);
  }
  catch(const std::exception& e)
  {
    pConnection->logf("VNCConnectionImpl::GetActivity caught exception: %s\n", e.what());
  }
  catch(...)
  {
    pConnection->logf("VNCConnectionImpl::GetActivity caught unknown exception.\n");
  }

  return VNCBearerErrorFailed;
}

char *VNCCALL VNCConnectionImpl::GetProperty(VNCConnection *pConnection,
                                             VNCConnectionProperty property)
{
  try
  {
    return pConnection->getProperty(property);
  }
  catch(const std::exception& e)
  {
    pConnection->logf("VNCConnectionImpl::GetProperty caught exception: %s\n", e.what());
  }
  catch(...)
  {
    pConnection->logf("VNCConnectionImpl::GetProperty caught unknown exception.\n");
  }

  return NULL;
}

VNCBearerError VNCCALL VNCConnectionImpl::TimerExpired(
    VNCConnection *pConnection)
{
  try
  {
    return pConnection->timerExpired();
  }
  catch(const std::exception& e)
  {
    pConnection->logf("VNCConnectionImpl::TimerExpired caught exception: %s\n", e.what());
  }
  catch(...)
  {
    pConnection->logf("VNCConnectionImpl::TimerExpired caught unknown exception.\n");
  }

  return VNCBearerErrorFailed;
}

