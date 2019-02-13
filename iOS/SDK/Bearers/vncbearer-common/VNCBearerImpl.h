/* Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
*/

#ifndef __VNCBEARERIMPL_H__
#define __VNCBEARERIMPL_H__

#include <string>
#include <stdarg.h>

#include <vncbearer.h>

struct VNCConnectionImpl;

/// \brief Base class for bearer implementations conforming to the definition
/// in vncbearer.h.
struct VNCBearerImpl
{
public:
  /// \brief Allocate memory suitable for returning to the calling SDK.
  ///
  /// This is a wrapper around VNCBearerAlloc() in VNCBearerSupportingAPI.
  ///
  /// \param size The size of the buffer to allocate.
  /// \return The allocated memory, or NULL on an allocation failure.
  void *sdkAlloc(size_t size) const;

  /// \brief Copy a string into memory suitable for returning to the calling
  /// SDK.
  ///
  /// This is a wrapper around VNCBearerAlloc() in VNCBearerSupportingAPI.
  ///
  /// \param s The string to copy.
  /// \return The allocated memory, or NULL on an allocation failure.
  char *sdkStrdup(const char *s) const;

  /// \brief Free memory whose ownership has been passed to the bearer by the
  /// calling SDK.
  /// 
  /// This is a wrapper around VNCBearerFree() in VNCBearerSupportingAPI.
  ///
  /// \param buffer 
  void sdkFree(void *buffer) const;

  /// \brief Write a fixed text string to the log.
  ///
  /// This is a wrapper around VNCBearerLog() in VNCBearerSupportingAPI.
  ///
  /// \param text The text to log.
  void log(const char *text) const;

  /// \brief Write a formatted text string to the log.
  ///
  /// This is a wrapper around VNCBearerLog() in VNCBearerSupportingAPI.
  ///
  /// \param format The printf() format to use to format the string.  The
  /// remaining arguments are the inserts to the format string.
  void logf(const char *format, ...) const;

  /// \brief Write a formatted text string to the log.
  ///
  /// This is a wrapper around VNCBearerLog() in VNCBearerSupportingAPI.
  ///
  /// \param format The printf() format to use to format the string.
  /// \param args The argument list of inserts to the format string.
  void logv(const char *format, va_list args) const;

  /// \brief Return the name of this bearer.
  /// \return The bearer's name.
  inline const char *getName() const;

  /// \brief Create a new connection and return it.
  ///
  /// Implements VNCBearerCreateConnection() in VNCBearerInterface.
  ///
  /// \param connectionContext The VNCConnectionContext supplied by the calling
  /// SDK.
  /// \param ppConnection On successful return, *ppConnection should point to
  /// a new instance of a subclass of VNCConnectionImpl. When a failure is returned
  /// this will be ignored by the SDK.
  ///
  /// \return VNCBearerErrorNone on success, or some other VNCBearerError on
  /// failure.
  virtual VNCBearerError createConnection(
    VNCConnectionContext connectionContext,
    VNCConnection **ppConnection) = 0;

  /// \brief Return a property of this bearer.
  ///
  /// Implements VNCBearerGetProperty() in VNCBearerInterface.
  ///
  /// This base class implementation provides VNCBearerPropertyName and
  /// VNCBearerPropertyVersion.  Subclasses should override this method to
  /// provide additional property values and fall back to calling
  /// VNCBearerImpl::getProperty() for those that they don't.
  ///
  /// \param property The property whose value to return.
  ///
  /// \return The property's value on success.  This should be allocated using
  /// sdkAlloc() or sdkStrdup(). Ownership passes to the caller. Return NULL
  /// if this bearer does not define a value for this property.
  virtual char *getProperty(VNCBearerProperty property) const;

  /// \brief Obtain a pointer to the bearer's dynamic context.
  ///
  /// This is a wrapper around VNCBearerGetDynamicContext() in VNCBearerSupportingAPI.
  ///
  /// \return The bearer's dynamic context pointer.
  VNCBearerDynamicContext getDynamicContext() const;

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
  VNCBearerImpl(const char *bearerName,
                VNCBearerContext bearerContext,
                VNCBearerInterface *pBearerInterface,
                size_t bearerInterfaceSize,
                const VNCBearerSupportingAPI *pBearerSupportingAPI,
                size_t bearerSupportingAPISize);

  /// \brief Destructor.
  virtual ~VNCBearerImpl();

private:
  static VNCBearerTerminate Terminate;
  static VNCBearerCreateConnection CreateConnection;
  static VNCBearerGetProperty GetProperty;

  VNCBearerContext bearerContext;
  VNCBearerSupportingAPI api;
  std::string name;

  friend struct VNCConnectionImpl;
};

inline const char *VNCBearerImpl::getName() const
{
  return name.c_str();
}

#endif /* !defined(__VNCBEARERIMPL_H__) */
