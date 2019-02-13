/* Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
*/

#ifndef __VNCBEARERTCPBASE_H__
#define __VNCBEARERTCPBASE_H__

#include <VNCBearerImpl.h>

#ifdef _WIN32

/// \brief Base class for TCP based bearer implementations conforming
/// to the definition in vncbearer.h.
/// 
/// Implementations using this base class need to provide an implementation
/// of createConnection() as described in VNCBearerImpl. This will typically
/// create a connection which uses VNCConnectionTcpBase as it's base class.
class VNCBearerTcpBase : public VNCBearerImpl
{
public:
  /// \brief Destructor.
  virtual ~VNCBearerTcpBase();

protected:
  /// \brief Constructor.
  ///
  /// The DLL or shared object that contains the subclass implementation should
  /// pass all parameters from VNCBearerInitialize() straight into the base
  /// class constructor.
  /// 
  /// \param bearerName The name of the bearer.
  /// \param bearerContext The VNCBearerContext supplied by the calling SDK.
  /// \param pBearerInterface The VNCBearerInterface structure.  This
  /// constructor fills out this structure; it is not necessary for the
  /// subclass to do it.
  /// \param bearerInterfaceSize The size of *pBearerInterface.
  /// \param pBearerSupportingAPI The VNCBearerSupportingAPI structure.  The
  /// VNCBearerImpl and VNCConnectionImpl class provide methods that correspond
  /// to the members of this structure; it is recommended that your subclasses
  /// use this wrappers instead of calling the functions in
  /// *pBearerSupportingAPI directly.
  /// \param bearerSupportingAPISize The size of *pBearerSupportingAPI.
  VNCBearerTcpBase(const char *bearerName,
                   VNCBearerContext bearerContext,
                   VNCBearerInterface *pBearerInterface,
                   size_t bearerInterfaceSize,
                   const VNCBearerSupportingAPI *pBearerSupportingAPI,
                   size_t bearerSupportingAPISize);

private:
  bool initialized;
};

#else

typedef VNCBearerImpl VNCBearerTcpBase;

#endif

#endif /* !defined(__VNCBEARERTCPBASE_H__) */
