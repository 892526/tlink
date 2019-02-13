/* Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
*/

#ifndef VNCBEARER_D_H
#define VNCBEARER_D_H

#ifdef __cplusplus
extern "C"
{
#endif

#ifdef VNC_BEARER_STATIC
VNCBearer *VNCCALL VNCBearerInitialize_D(
#else
VNCBearer *VNCCALL VNCBearerInitialize(
#endif
    const char *bearerName,
    VNCBearerContext bearerContext,
    VNCBearerInterface *pBearerInterface,
    size_t bearerInterfaceSize,
    const VNCBearerSupportingAPI *pBearerSupportingAPI,
    size_t bearerSupportingAPISize);

#ifdef __cplusplus
}
#endif

#endif // VNCBEARER_D_H
