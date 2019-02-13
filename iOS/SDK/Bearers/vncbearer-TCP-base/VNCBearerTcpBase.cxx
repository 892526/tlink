/* Copyright (C) 2002-2018 RealVNC Ltd.  All Rights Reserved.
*/

#include "VNCBearerTcpBase.h"

#ifdef _WIN32

#include <windows.h>
#include <winsock2.h>

VNCBearerTcpBase::VNCBearerTcpBase(
     const char *bearerName,
     VNCBearerContext bearerContext,
     VNCBearerInterface *pBearerInterface,
     size_t bearerInterfaceSize,
     const VNCBearerSupportingAPI *pBearerSupportingAPI,
     size_t bearerSupportingAPISize)
  : VNCBearerImpl(bearerName,
                  bearerContext,
                  pBearerInterface,
                  bearerInterfaceSize,
                  pBearerSupportingAPI,
                  bearerSupportingAPISize),
    initialized(false)
{
  WSADATA wsaData;

  initialized = WSAStartup(MAKEWORD(2, 2), &wsaData) == 0;
}

VNCBearerTcpBase::~VNCBearerTcpBase()
{
  if (initialized)
    WSACleanup();
}

#endif

