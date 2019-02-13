/* Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
*/

#ifndef __VNCCONNECTIONIMPL_H__
#define __VNCCONNECTIONIMPL_H__

#include <stdarg.h>

#include <vncbearer.h>

#include "VNCBearerImpl.h"

/// \brief Base class for connection implementations conforming to the
/// definition in vncbearer.h.
struct VNCConnectionImpl
{
public:
  /// \section interface Methods that you must implement or override

  /// \brief Called by the SDK to begin the process of establishing a
  /// connection.
  /// 
  /// For further information, consult the documentation on
  /// VNCConnectionEstablish() in vncbearer.h.
  ///
  /// \return VNCBearerErrorNone on success, or some other VNCBearerError on
  /// failure.
  virtual VNCBearerError establish() = 0;

  /// \brief Called by the SDK to read data from a connection.
  /// 
  /// For further information, consult the documentation on
  /// VNCConnectionRead() in vncbearer.h.
  ///
  /// \param buffer The buffer into which to read data.
  /// \param pBufferSize The size of the supplied buffer, on return this is
  /// modified to the number of bytes received.
  ///
  /// \return VNCBearerErrorNone on success, or some other VNCBearerError on
  /// failure.
  virtual VNCBearerError read(unsigned char *buffer, size_t *pBufferSize) = 0;

  /// \brief Called by the SDK to write data to a connection.
  /// 
  /// For further information, consult the documentation on
  /// VNCConnectionWrite() in vncbearer.h.
  ///
  /// \param buffer The buffer from which to write data.
  /// \param pBufferSize The size of the supplied buffer, on return this is
  /// modified to the number of bytes sent.
  ///
  /// \return VNCBearerErrorNone on success, or some other VNCBearerError on
  /// failure.
  virtual VNCBearerError write(const unsigned char *buffer, size_t *pBufferSize) = 0;

  /// \brief Called by the SDK to retrieve the current VNCConnectionEventHandle
  /// for the given connection.
  /// 
  /// For further information, consult the documentation on
  /// VNCConnectionGetEventHandle() in vncbearer.h.
  /// 
  /// \param pEventHandle On successful return, the bearer should make
  /// *pEventHandle equal to the VNCConnectionEventHandle for the connection.
  /// \param pWriteNotification If not null then the bearer should set the value of
  /// *pWriteNotification to 0 if write ready notification for pEventHandle are not
  /// necessary.
  /// 
  /// \return VNCBearerErrorNone on success, or some other VNCBearerError on
  /// failure.
  virtual VNCBearerError getEventHandle(VNCConnectionEventHandle *pEventHandle, int* pWriteNotification) = 0;

  /// \brief Called by the SDK to enquire about activity on the given connection.
  /// 
  /// For further information, consult the documentation on
  /// VNCConnectionGetActivity() in vncbearer.h.
  /// 
  /// \param pActivity On successful return, the bearer should make *pActivity
  /// equal to either VNCConnectionActivityNone or some bitwise combination of
  /// VNCConnectionActivityReadReady and VNCConnectionActivityWriteReady.
  ///
  /// \return VNCBearerErrorNone on success, or some other VNCBearerError on
  /// failure.  It is often convenient to use VNCConnectionGetActivity() to notify
  /// errors that occur while the SDK is blocked waiting for the
  /// VNCConnectionEventHandle (e.g. errors that are notified by another thread).
  virtual VNCBearerError getActivity(VNCConnectionActivity *pActivity) = 0;

  /// \brief Called by the SDK to obtain information about the given connection.
  ///
  /// For further information, consult the documentation on
  /// VNCConnectionGetProperty() in vncbearer.h.
  /// 
  /// \param property The property whose value is being requested.
  ///
  /// \return The property's value on success.  This should be allocated using
  /// sdkAlloc() or sdkStrdup(). Ownership passes to the caller. Return NULL
  /// if this bearer does not define a value for this property.
  virtual char *getProperty(VNCConnectionProperty property) const;

  /// \brief Called by the SDK to notify the bearer that the timer set with
  /// VNCConnectionSetTimer() has expired.
  /// 
  /// For further information, consult the documentation on
  /// VNCConnectionTimerExpired() and VNCConnectionSetTimer() in vncbearer.h.
  /// 
  /// To set the timer expiration time, use the setTimer() method in this class.
  /// 
  /// \return VNCBearerErrorNone on success, or some other VNCBearerError on
  /// failure.
  virtual VNCBearerError timerExpired();

  /// \section supporting Supporting methods that your implementation may call

  /// \brief Returns the bearer that created this connection.
  ///
  /// \return The associated bearer.
  virtual VNCBearerImpl &getBearer() const;

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

  /// \brief Extracts,a optionally Base64-decodes and returns a field from the
  /// command string that was responsible for this connection being created.
  /// 
  /// This is a wrapper around VNCConnectionGetCommandStringField() in
  /// VNCBearerSupportingAPI.
  ///
  /// Fields values that contain binary data should always be Base64-encoded.
  /// Field values that are strings but may contain characters that are
  /// significant in command strings (e.g. '=') or that may cause problems in
  /// transit should likewise always be Base64-encoded.  Use the base64decode
  /// parameter to indicate to the SDK whether the field's value is
  /// Base64-encoded or not.
  ///
  /// The SDK automatically appends a NUL byte to the returned value, so that
  /// it may be treated as a string if desired.  This NUL byte is not counted
  /// in the value returned in *pSize.
  ///
  /// The caller should free the returned value with the sdkFree() method in 
  /// the bearer when it is no longer required.
  ///
  /// \param name The name of the field.
  /// \param base64decode If true, then the value will be Base64-decoded before
  /// it is returned.
  /// \param pSize If pSize is not NULL, then on successful return, *pSize
  /// contains the size of the decoded field's value in bytes.  This size does
  /// not include the auto-added NUL.
  ///
  /// \return The field's value, or NULL if it is not present.
  virtual char *getCommandStringField(const char *name,
                                      int base64decode = false,
                                      size_t *pSize = 0) const;

  /// \brief Informs the SDK of changes in the connection's status.
  ///
  /// This API informs the SDK of changes in state that occur during a
  /// connection's lifetime.
  /// 
  /// This is a wrapper around VNCConnectionStatusChange() in
  /// VNCBearerSupportingAPI.
  ///
  /// \param status The new status of the connection.
  virtual void statusChange(VNCConnectionStatus status);

  /// \brief Requests that the SDK set a timer on the connection's behalf.
  ///
  /// The timeout is given by the timeoutMs parameter, and is in milliseconds.
  /// If the timeout elapses before the timer is cancelled, the timerExpired()
  /// method will be called.
  ///
  /// To cancel the timer, call setTimer() with timeoutMs <= 0.
  ///
  /// Note that there is only one timer.  A second call to setTimer() always
  /// cancels and supercedes the first.
  /// 
  /// This is a wrapper around VNCConnectionSetTimer() in
  /// VNCBearerSupportingAPI.
  /// 
  /// \param timeoutMs The timeout in milliseconds.
  void setTimer(int timeoutMs);

  /// \brief Checks to see check whether a particular feature is
  /// licensed.
  ///
  /// This method allows you to ensure that your custom bearers are usable only
  /// in certain applications.  The feature check succeeds if the SDK is
  /// authorized to use at least one of the requested features.  The features
  /// are searched for both in the the licenses available to the SDK and in the
  /// set of features defined by calls by the application to the SDK's API.
  ///
  /// If none of the features are licensed, then zero is returned to the
  /// bearer.  The bearer may either continue to operate, restricting
  /// functionality appropriate, or return VNCBearerErrorFeatureNotLicensed to
  /// the SDK, in which case the SDK will terminate the session.
  ///
  /// \param connectionContext The bearer VNCConnectionContext that was passed
  /// to VNCBearerCreateConnection().
  /// \param featureID The feature identifier for which to check for a license.
  /// \return Non-zero if the feature is licensed, or zero if not.
  VNCBearerError localFeatureCheck(const unsigned *featureIDs,
                                   size_t featureIDCount,
                                   bool *pResult) const;

  /// \brief Extracts a bearer configuration from the SDK.
  ///
  /// The SDK may be able to provide a bearer-specific configuration. This configuration 
  /// is opaque to the SDK. The structure of the configuration is defined by the bearer.
  /// The configuration should only contain settings that are static for the lifetime 
  /// of the SDK. Settings that vary between connections, such as an address to connect 
  /// to, should not be included.
  ///
  /// The bearer should free the returned value with VNCBearerFree() when it is no
  /// longer required.
  ///
  /// \param connectionContext The VNCConnectionContext that was passed to
  /// VNCBearerCreateConnection().
  ///
  /// \return The bearer configuration, or NULL if no configuration is available.
  char *getBearerConfiguration() const; 

  /// \brief Return the last VNCConnectionStatus that was notified with
  /// statusChange().
  ///
  /// \return The last VNCConnectionStatus.
  inline VNCConnectionStatus getStatus() const;

protected:
  /// \brief Constructor
  /// 
  /// \param bearer The bearer creating this connection.
  /// \param connectionContext The bearer context provided by the SDK.
  VNCConnectionImpl(VNCBearerImpl &bearer,
                    VNCConnectionContext connectionContext);
  /// \brief Destructor
  virtual ~VNCConnectionImpl();

  /// \brief The bearer that created this connection.
  VNCBearerImpl &bearer;

private:
  static VNCConnectionEstablish Establish;
  static VNCConnectionDestroy Destroy;
  static VNCConnectionRead Read;
  static VNCConnectionWrite Write;
  static VNCConnectionGetEventHandle GetEventHandle;
  static VNCConnectionGetActivity GetActivity;
  static VNCConnectionGetProperty GetProperty;
  static VNCConnectionTimerExpired TimerExpired;

  VNCConnectionContext connectionContext;
  VNCConnectionStatus status;

  friend struct VNCBearerImpl;
};

inline VNCConnectionStatus VNCConnection::getStatus() const
{
  return status;
}

#endif /* !defined(__VNCCONNECTIONIMPL_H__) */
