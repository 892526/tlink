/* Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
*/

#ifndef __VNCBEARER_H__
#define __VNCBEARER_H__

/** \cond vncbearer_mainpage */

/**
 * \mainpage VNC Automotive Pluggable Bearer API
 *
 * The VNC Automotive Pluggable Bearer API is defined in vncbearer.h.
 *
 * \see vncbearer.h
 */

/** \endcond */

/**
 * \file vncbearer.h
 *
 * Pluggable bearers provide a means for applications to extend VNC Automotive SDKs with
 * support for new transport mechanisms.  This allows VNC Automotive Viewers and VNC Automotive
 * Servers to connect to each other using almost any transport, whether
 * standard or proprietary, wired or wireless.
 *
 * The bearer implementation must provide the SDK with a reliable and ordered
 * stream of data, in the same manner as TCP.
 *
 * vncbearer.h defines the interface that a pluggable bearer must implement.
 * This interface is compatible with:
 *
 *  - VNC Automotive Viewer SDK for Win32
 *  - VNC Automotive Viewer SDK for Linux
 *
 * The bearer interface is the same for all of these SDKs.  Therefore,
 * throughout the documentation for the VNC Automotive Pluggable Bearer API, the term
 * 'SDK' refers to whichever SDK has loaded the bearer, rather to any of the
 * VNC Automotive SDKs in particular.
 *
 * To implement a pluggable bearer, you must create a DLL or shared object that
 * exports an entry point named VNCBearerInitialize.  The entry point must
 * conform to the specification of VNCBearerInitializeType as described below.
 * The bearer must also provide an implementation for each of the methods in
 * the VNCBearerInterface structure.
 *
 * If your platform supports C++ and the STL, then you may wish to use the
 * VNCBearerImpl and VNCConnectionImpl classes as your starting point, rather
 * than implementing this API directly.  These classes may be found in the
 * vncbearer-common directory of the sample bearer source tree.
 *
 * \section section_legal Legal information
 *
 * Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
 *
 * VNC Automotive is a trademark of VNC Automotive Limited and is protected by
 * trademark registrations and/or pending trademark applications in the
 * European Union, United States of America and other jurisdictions.
 *
 *
 * \see VNCBearerInitializeType, VNCBearerInterface, VNCBearerSupportingAPI
 */

#ifdef __cplusplus
extern "C"
{
#endif

#include <stddef.h>

/* \cond */
#ifndef VNCCALL
    #ifdef _WIN32
        #define VNCCALL __stdcall
    #else
        #define VNCCALL
    #endif
#endif
/* \endcond */

/**
 * Represents a loaded pluggable bearer.
 *
 * This type is opaque to the SDK, which will pass the value returned by
 * VNCBearerInitialize() to any further bearer interface calls that may require
 * it.
 */
typedef struct VNCBearerImpl VNCBearer;

/**
 * Represents a connection created using a pluggable bearer.
 *
 * This type is opaque to the SDK, which will pass the value returned by
 * VNCBearerCreateConnection() to any further bearer interface calls that may
 * require it.
 */
typedef struct VNCConnectionImpl VNCConnection;

/**
 * Identifies the internal data structures of the SDK that correspond to a
 * loaded pluggable bearer.
 *
 * This type is opaque to the bearer implementation, which must pass the value
 * supplied to VNCBearerInitialize() to any supporting API calls that may
 * require it.
 */
typedef void *VNCBearerContext;

/**
 * Identifies the internal data structures of the SDK that correspond to a
 * connection created using a pluggable bearer.
 *
 * This type is opaque to the bearer implementation, which must pass the value
 * supplied to VNCBearerCreateConnection() to any supporting API calls that may
 * require it.
 */
typedef void *VNCConnectionContext;

/**
 * Identifies the dynamic bearer state that can be provided a loaded pluggable
 * bearer.
 *
 * This type is known to the bearer implementation and the client code (but not
 * to intermediaries such as SDKs), so may need to cast to the appropriate type.
 */
typedef void *VNCBearerDynamicContext;

/**
 * \typedef VNCConnectionEventHandle
 *
 * A VNCConnectionEventHandle is a platform-specific mechanism for the SDK to
 * detect that there has been activity on a pluggable bearer's connection.
 *
 * See VNCConnectionGetEventHandle() for a full discussion of how the SDK uses
 * VNCConnectionEventHandle.
 *
 * \see VNCConnectionGetEventHandle(), VNCConnectionGetActivity()
 */
#ifdef _WIN32
    #include <windows.h>
    typedef HANDLE VNCConnectionEventHandle;
#else
    typedef int VNCConnectionEventHandle;
#endif

/**
 * \brief Error codes that may be returned by the implementations of functions
 * in VNCBearerInterface.
 *
 * The error codes are split into three ranges:
 *
 *  - 0 to (VNCBearerErrorVENDOR - 1) - these error codes are a mixture of
 *    common networking error codes (e.g.  VNCBearerErrorConnectionRefused) and
 *    other error conditions that are pre-defined by VNC Automotive-provided bearers.
 *    You are encouraged to reuse error codes in this range, rather than
 *    defining your own, as long as it makes sense to do so.
 *  - VNCBearerErrorVENDOR and above - this range of error codes is reserved
 *    for the use of third parties developing bearers.  It is intended for
 *    error conditions that are specific to particular bearer implementations,
 *    and that do not map closely to the codes in the 0 to
 *    (VNCBearerErrorVENDOR - 1) range.
 */
typedef enum
{
    /**
     * The operation was successful - no error occurred.
     */
    VNCBearerErrorNone = 0,

    /**
     * A hostname could not be resolved.
     */
    VNCBearerErrorNameLookupFailure = 1,

    /**
     * The peer network is unreachable.
     */
    VNCBearerErrorNetworkUnreachable = 2,

    /**
     * The peer host is unreachable.
     */
    VNCBearerErrorHostUnreachable = 3,

    /**
     * The connection attempt timed out.
     */
    VNCBearerErrorConnectionTimedOut = 4,

    /**
     * The peer host refused the TCP connection.
     */
    VNCBearerErrorConnectionRefused = 5,

    /**
     * The connection was lost.
     */
    VNCBearerErrorDisconnected = 6,

    /**
     * Attempted to bind to an address that was already in use.
     */
    VNCBearerErrorAddressInUse = 7,

    /**
     * The command string is missing a required field, or a field value has an
     * invalid format.
     */
    VNCBearerErrorInvalidCommandString = 8,

    /**
     * Permission was denied by the operating system.
     */
    VNCBearerErrorPermissionDenied = 9,

    /**
     * The connection could not be established due to an authentication
     * failure.
     *
     * Note that this error indicates a transport-level failure, rather than an
     * application-level failure.  That is, the failure originates from within
     * the bearer implementation, rather than with the SDK.
     */
    VNCBearerErrorAuthenticationFailed = 10,

    /**
     * Non-specific failure error code.
     *
     * Where possible, your bearer implementation should attempt to provide a
     * more meaningful error code.
     */
    VNCBearerErrorFailed = 11,

    /**
     * The bearer received invalid protocol was received from the VNC Automotive Data
     * Relay.
     */
    VNCBearerErrorDataRelayProtocolError = 12,

    /**
     * The VNC Automotive Data Relay reported that an invalid message was sent to it.
     */
    VNCBearerErrorDataRelayInvalidMessage = 13,

    /**
     * The VNC Automotive Data Relay reported that the session ID in the channel details
     * string passed to VNCViewerProcessCommandString() is not valid
     * (probably because the channel lease has already expired).
     */
    VNCBearerErrorUnknownDataRelaySessionId = 14,

    /**
     * The VNC Automotive Data Relay reported that an the bearer's response to its
     * challenge was incorrect.
     */
    VNCBearerErrorDataRelayInvalidResponseToChallenge = 15,

    /**
     * The Data Relay channel lease expired while waiting for the other peer to
     * connect to the Data Relay.
     */
    VNCBearerErrorDataRelayChannelTimeout = 16,

    /**
     * The specified device is not connected via USB, or the VNC Automotive software on
     * the device is not ready to begin a USB connection.
     */
    VNCBearerErrorUSBNotConnected = 17,

    /**
     * The bearer was unable to an load an underlying software library (e.g.
     * OEM software for driving a particular type of communications), and
     * cannot proceed without it.
     */
    VNCBearerErrorUnderlyingLibraryNotFound = 18,

    /**
     * The buffer provided to the bearer was too small to be of use to the
     * bearer.
     * 
     * This indicates that the operation is likely to succeed if a larger
     * buffer is used.
     */
    VNCBearerErrorBufferTooSmall = 19,

    /**
     * The port number in the command string is invalid.
     */
    VNCBearerErrorBadPort = 20,

    /**
     * The bearer requires a licensed feature for which the SDK does not have a
     * license.
     */
    VNCBearerErrorFeatureNotLicensed = 21,

    /**
     * The value of one of the parameters to the call is not valid.
     */
    VNCBearerErrorInvalidParameter = 22,

    /**
     * The bearer requires a configuration from the SDK, and this was not 
     * provided. 
     */
    VNCBearerErrorConfigurationNotProvided = 23,

    /**
     * The bearer received a configuration from the SDK, but this was not valid.
     */
    VNCBearerErrorConfigurationInvalid = 24,

    /**
     * Start of range of third-party bearer-specific error codes.
     */
    VNCBearerErrorVENDOR = 0x10000
} VNCBearerError;

/**
 * \brief Values that may be supplied to VNCConnectionStatusChange().
 *
 * The statuses are split into three ranges:
 *
 *  - VNCBearerStatusGENERAL through to (VNCBearerStatusRESERVED - 1) - these
 *    stock status codes are likely to be meaningful to the majority of bearer
 *    implementations.  You are encouraged to reuse error codes in this range,
 *    rather than defining your own, as long as it makes sense to do so.
 *  - VNCBearerStatusRESERVED through to (VNCBearerStatusVENDOR - 1) - this
 *    range of status codes is reserved for VNC Automotive use.  It contains status
 *    codes that are specific to particular bearer implementations.
 *  - VNCBearerStatusVENDOR and above - this range of status codes is reserved
 *    for the use of third parties developing bearers.  It is intended for
 *    notifications that are specific to particular bearer implementations, and
 *    that do not map closely to the codes in the VNCBearerStatusGENERAL range.
 * 
 * \see VNCConnectionStatusChange()
 */
typedef enum
{
    /**
     * Start of range of common connection status codes.
     */
    VNCConnectionStatusGENERAL = 0,

    /**
     * \brief Placeholder value for variable initialization.
     */
    VNCConnectionStatusNone = VNCConnectionStatusGENERAL,

    /**
     * \brief Notified when the bearer is about to start listening for an
     * incoming connection.
     */
    VNCConnectionStatusListening,

    /**
     * \brief Notified when the bearer is about to perform a name lookup (e.g.
     * using DNS).
     */
    VNCConnectionStatusPerformingNameLookup,

    /**
     * \brief Notified when the bearer is about to start connecting outwards.
     *
     * VNCConnectionStatusConnecting should also be used for transports that
     * are symmetric when establishing connections.
     */
    VNCConnectionStatusConnecting,

    /**
     * \brief Notified when the bearer has fully established the connection.
     */
    VNCConnectionStatusConnected,

    /**
     * Start of range of VNC Automotive-reserved bearer-specific connection status
     * codes.
     */
    VNCConnectionStatusRESERVED = 0x1000,

    /**
     * \brief Notified by the 'D' bearer when the TCP connection to the VNC Automotive
     * Data Relay has been established, and the Data Relay handshake is in
     * progress.
     */
    VNCConnectionStatusNegotiatingWithDataRelay = VNCConnectionStatusRESERVED,

    /**
     * \brief Notified by the 'D' bearer the TCP connection to the VNC Automotive Data
     * Relay has been established, and the Data Relay handshake is in progress.
     * \brief Notified when the viewer thread is about to start negotiating
     * with a VNC Automotive Data Relay.
     */
    VNCConnectionStatusWaitingForDataRelayPeer = VNCConnectionStatusRESERVED + 1,

    /**
     * \brief Notified by the Android bearer when the TCP connection
     * to the adb server has been established, and the adb handshake
     * is in progress.
     */
    VNCConnectionStatusNegotiatingWithAdb = VNCConnectionStatusRESERVED + 2,

   /**
     * \brief Notified by the BlackBerry bearer when the Barry process
     * has been created, and the Barry handshake with the device is in
     * progress.
     *
     * \deprecated No longer used as of SDK Version 3.0 - BlackBerry
     * devices are no longer supported.
     */
    VNCConnectionStatusNegotiatingWithBarry = VNCConnectionStatusRESERVED + 3,

    /**
     * Start of range of third-party bearer-specific connection status codes.
     */
    VNCConnectionStatusVENDOR = 0x10000
} VNCConnectionStatus;

/**
 * \brief Flags that may be returned by VNCConnectionGetActivity().
 *
 * These values are a bitmask.  To indicate that a connection is ready for both
 * read and write, you should return
 * (VNCConnectionActivityReadReady | VNCConnectionActivityWriteReady).
 *
 * \see VNCConnectionGetActivity()
 */
typedef enum
{
    /** The connection is not ready for read or write. */
    VNCConnectionActivityNone = 0x00,

    /** The connection is ready for read. */
    VNCConnectionActivityReadReady = 0x01,

    /** The connection is ready for write. */
    VNCConnectionActivityWriteReady = 0x02
} VNCConnectionActivity;

/**
 * \brief Properties whose values may be queried with VNCBearerGetProperty().
 *
 * The bearer implementation is responsible for any internationalization.
 */
typedef enum
{
    /**
     * \brief The short name of the bearer.  This should be the same as the
     * string that is passed to VNCBearerInitialize().  
     */
    VNCBearerPropertyName,

    /**
     * \brief A human-readable longer name for the bearer.
     */
    VNCBearerPropertyFullName,

    /**
     * \brief A human-readable longer description for the bearer (for example,
     * copyright information).
     */
    VNCBearerPropertyDescription,

    /**
     * \brief A version number or other version identier for the bearer.
     */
    VNCBearerPropertyVersion
} VNCBearerProperty;

/**
 * \brief Properties whose values may be queried with
 * VNCConnectionGetProperty().
 *
 * The bearer implementation is responsible for any internationalization.
 */
typedef enum
{
    /**
     * \brief Contains details of how another application should connect to
     * this one.
     *
     * Used when a bearer is listening for an incoming connection.  This is a
     * formatted text string suitable for display to an end-user.  For example,
     * with the TCP 'L' listening bearer, this is a list of network interfaces
     * separated by commas and whitespace.
     *
     * Bearer implementations should return NULL if the VNCConnection is not
     * currently listening for an incoming connection.
     */
    VNCConnectionPropertyListeningInformation = 1,

    /**
     * \brief The address of the local endpoint of the connection.
     *
     * The format and meaning of the returned string is dependent on the
     * bearer.  For example, with the TCP 'C' bearer, this is an IP address and
     * a port number, separated by a colon.
     *
     * Bearer implementations should return NULL if the VNCConnection is not
     * currently connected.
     */
    VNCConnectionPropertyLocalAddress = 2,

    /**
     * \brief The address of the remote endpoint of the connection.
     *
     * The format and meaning of the returned string is dependent on the
     * bearer.  For example, with the TCP 'C' bearer, this is an IP address and
     * a port number, separated by a colon.
     *
     * Bearer implementations should return NULL if the VNCConnection is not
     * currently connected.
     */
    VNCConnectionPropertyPeerAddress = 3,

    /**
     * \brief The serial number of the remote endpoint of the connection.
     *
     * The format and meaning of the returned string is dependent on the
     * bearer.  For example, with the 'AAP' bearer, this is the serial number
     * of the USB device.
     *
     * Bearer implementations should return NULL if the VNCConnection is not
     * currently connected.
     */
    VNCConnectionPropertyDeviceSerial = 4

} VNCConnectionProperty;

/**
 * \brief Called by the SDK prior to unloading a bearer DLL or shared object.
 *
 * The implementation should free all resources associated with the bearer
 * implementation.
 *
 * The SDK will call VNCConnectionDestroy() for all VNCConnections created by
 * this bearer before calling VNCBearerTerminate().
 * 
 * \param pBearer The bearer to be unloaded.
 */
typedef void VNCCALL
VNCBearerTerminate(VNCBearer *pBearer);

/**
 * \brief Called by the SDK to obtain information about the bearer
 * implementation.
 *
 * \param pBearer The bearer for which the information is being requested.
 * \param property The property whose value is being requested.
 *
 * \return The property's value, which should be allocated with
 * VNCBearerAlloc(), or NULL if this bearer does not define a value for this
 * property.  The SDK will free a non-NULL return value with VNCBearerFree().
 *
 * \see VNCBearerProperty, VNCBearerAlloc()
 */
typedef char *VNCCALL
VNCBearerGetProperty(VNCBearer *pBearer, VNCBearerProperty property);

/**
 * \brief Called by the SDK to create a new VNCConnection.
 *
 * The implementation should not begin establishing the connection.  The
 * connection should be established only when VNCConnectionEstablish() is
 * called.
 *
 * The implementation should use VNCConnectionGetCommandStringField() 
 * obtain information about the connection to be created.
 *
 * The SDK will call VNCConnectionDestroy() to destroy the connection when it
 * is no longer needed.
 * 
 * \param pBearer The bearer that should create the connection.
 * \param connectionContext A value that must be supplied to the SDK when
 * calling certain APIs in the VNCBearerSupportingAPI structure.
 * \param pCommandString The command string that contains the details of the
 * connection to be created.
 * \param ppConnection On successful return, *ppConnection should
 * point to the new connection.  Otherwise, the SDK ignores the value at
 * *ppConnection.
 *
 * \return VNCBearerErrorNone on success, or some other VNCBearerError on
 * failure.
 *
 * \see VNCConnectionEstablish(), VNCConnectionDestroy()
 */
typedef VNCBearerError VNCCALL
VNCBearerCreateConnection(VNCBearer *pBearer,
                          VNCConnectionContext connectionContext,
                          VNCConnection **ppConnection);

/**
 * \brief Called by the SDK to begin the process of establishing a connection.
 *
 * For proper operation of the SDK, the attempt to establish the connection
 * should not block.  Instead, the implementation should begin the process of
 * establishing the connection, and return immediately.
 *
 * The bearer implementation should inform the SDK of its progress in
 * establishing the connection by calling VNCConnectionStatusChange().
 *
 * If the connection cannot be established for any reason, the bearer
 * implementation should return an appropriate VNCBearerError code, either from
 * VNCConnectionEstablish() or from a future call to
 * VNCConnectionGetActivity().
 *
 * \param pConnection The connection that should be established.
 *
 * \return VNCBearerErrorNone on success, or some other VNCBearerError on
 * failure.
 */
typedef VNCBearerError VNCCALL
VNCConnectionEstablish(VNCConnection *pConnection);

/**
 * \brief Called by the SDK to close and destroy a VNCConnection object.
 *
 * The implementation should free any resources associated with the connection.
 *
 * If the connection is still established, or is still in the process of being
 * established, then the implementation must close it before destroying it.
 * The implementation should block until the close has completed (or at least
 * until it is in progress and execution can safely proceeed).
 * 
 * \param pConnection The connection that should be destroyed.
 */
typedef void VNCCALL
VNCConnectionDestroy(VNCConnection *pConnection);

/**
 * \brief Called by the SDK to read data from a connection.
 * 
 * The SDK calls VNCConnectionRead() whenever VNCConnectionGetActivity()
 * notifies VNCConnectionActivityReadReady.  The SDK will continue to call
 * VNCConnectionRead() until VNCConnectionRead() indicates that a read would
 * block.
 *
 * If a read would block, then the implementation should change *pBufferSize to
 * zero and return VNCBearerNone.  In this case, the connection is no longer
 * considered ready for read, and the SDK will not attempt to read from the
 * connection again until VNCConnectionGetActivity() returns
 * VNCConnectionActivityReadReady again.
 * 
 * If the buffer provided isn't large enough then the implementation should
 * return VNCBearerErrorBufferTooSmall and set pBufferSize to the suggested
 * buffer size.
 *
 * If the connection has been lost, then the implementation should return an
 * appropriate VNCBearerError code.  The SDK will then call
 * VNCConnectionDestroy() to close and destroy the connection.
 * 
 * \param pConnection The connection from which data should be read.
 * \param buffer The buffer into which to read data.
 * \param pBufferSize The size of the supplied buffer, on return this is
 * modified to the number of bytes received on success or the desired buffer
 * size if returning VNCBearerErrorBufferTooSmall.
 *
 * \return VNCBearerErrorNone on success, or some other VNCBearerError on
 * failure.
 */
typedef VNCBearerError VNCCALL
VNCConnectionRead(VNCConnection *pConnection,
                  unsigned char *buffer,
                  size_t *pBufferSize);

/**
 * \brief Called by the SDK to write data to a connection.
 * 
 * The SDK calls VNCConnectionWrite() whenever VNCConnectionGetActivity()
 * notifies VNCConnectionActivityWriteReady and it has data to write.  The SDK
 * will continue to call VNCConnectionWrite() while it has data to write, until
 * VNCConnectionWrite() indicates that a write would block.
 *
 * If a write would block, then the implementation should change *pBufferSize
 * to zero and return VNCBearerNone.  In this case, the connection is no longer
 * considered ready for write, and the SDK will not attempt to write to the
 * connection again until VNCConnectionGetActivity() returns
 * VNCConnectionActivityWriteReady again.
 *
 * If the connection has been lost, then the implementation should return an
 * appropriate VNCBearerError code.  The SDK will then call
 * VNCConnectionDestroy() to close and destroy the connection.
 *
 * \param pConnection The connection to which data should be written.
 * \param buffer The buffer from which to write data.
 * \param pBufferSize The size of the supplied buffer, on return this is
 * modified to the number of bytes sent.
 *
 * \return VNCBearerErrorNone on success, or some other VNCBearerError on
 * failure.
 */
typedef VNCBearerError VNCCALL
VNCConnectionWrite(VNCConnection *pConnection,
                   const unsigned char *buffer,
                   size_t *pBufferSize);

/**
 * \brief Called by the SDK to retrieve the current VNCConnectionEventHandle
 * for the given connection.
 *
 * Each bearer connection is required to be able provide the SDK with exactly
 * one VNCConnectionEventHandle at all times.  The SDK obtains the
 * VNCConnectionEventHandle by calling VNCConnectionGetEventHandle() on each
 * iteration of its main loop.  (Calling VNCConnectionGetEventHandle() allows
 * the bearer to return a different VNCConnectionEventHandle at different
 * points in the connection's lifetime, if it is convenient to do so.)
 *
 * On Windows, VNCConnectionEventHandle is a HANDLE that must identify a
 * waitable kernel object.  An auto-reset event is recommended (see
 * CreateEvent() in the Windows Platform SDK documentation).  If your bearer
 * implementation uses Winsock, then you should use WSAEventSelect() to
 * associate the event with the socket, and then use WSAEnumNetworkEvents() to
 * implement VNCConnectionGetActivity().
 *
 * On UNIX platforms, VNCConnectionEventHandle is a file descriptor.  The
 * SDK will use a blocking select() to detect network activity.  You should use
 * a select() with zero timeout to implement VNCConnectionGetActivity().
 *
 * When the operating system indicates that a VNConnectionEventHandle is ready,
 * the SDK will call VNCConnectionGetActivity().  The bearer's implementation
 * of VNCConnectionGetActivity() should then determine whether it is actually
 * ready for the calling SDK to attempt to read or write data (e.g. by handling
 * any lower-level protocol events), and then use the VNCConnectionActivity
 * enumeration to report the results to the SDK.
 * 
 * If your bearer implementation is required to use a transport library that
 * does not support non-blocking writes, then you should have
 * VNCConnectionActivity() return VNCConnectionWriteReady (as well as possibly
 * also VNCConnectionReadReady) for all calls that are made to it after the
 * connection is established.  You can then safely implement
 * VNCConnectionWrite() using blocking writes.  (However, note that
 * non-blocking writes will result in smoother application performance, and are
 * greatly preferred.)
 *
 * If your bearer implementation is required to use a transport library that
 * does not provide any non-blocking I/O at all or does not expose a file
 * descriptor or an event handle, then you may wish to use a background thread
 * to drive the I/O.
 *
 * Bearers which implement software based flow control may wish to make use of
 * the write pending flag. This allows bearers to indicate that notification of
 * write being ready on the event handle should not lead to
 * VNCConnectionGetActivity() being called. Using this allows bearers to not
 * write data when flow control means the event handle is ready for write but
 * no data should be written. The pWriteNotification parameter can be NULL if a
 * platform can't distiguish between writes and other events on handles. If not
 * NULL then pWriteNotification will be set to 1 by the caller of
 * VNCConnectionGetEventHandle, allowing bearers without software flow control
 * to ignore the pWriteNotifications parameter.
 * 
 * \param pConnection The connection for which the VNCConnectionEventHandle
 * should be provided.
 * \param pEventHandle On successful return, the bearer should make
 * *pEventHandle equal to the VNCConnectionEventHandle for the connection.
 * \param pWriteNotification If not null then the bearer should set the value of
 * *pWriteNotification to 0 if write ready notification for pEventHandle are not
 * necessary.
 *
 * \return VNCBearerErrorNone on success, or some other VNCBearerError on
 * failure.
 *
 * \see VNCConnection, VNCConnectionEventHandle, VNCConnectionGetActivity()
 */
typedef VNCBearerError VNCCALL
VNCConnectionGetEventHandle(VNCConnection *pConnection,
                            VNCConnectionEventHandle *pEventHandle,
                            int *pWriteNotification);

/**
 * \brief Called by the SDK to enquire about activity on the given connection.
 *
 * After having been woken from its event loop by an notification from the OS
 * regarding the VNCConnectionEventHandle, the SDK will call
 * VNCConnectionGetActivity() to check whether the connection is ready for read
 * or write.  The SDK will then call VNCConnectionRead() and
 * VNCConnectionWrite() as necessary.
 * 
 * \param pConnection The connection for which the activity should be provided.
 * \param pActivity On successful return, the bearer should make *pActivity
 * equal to either VNCConnectionActivityNone or some combination of
 * VNCConnectionActivityReadReady and VNCConnectionActivityWriteReady.
 *
 * \return VNCBearerErrorNone on success, or some other VNCBearerError on
 * failure.  It is often convenient to use VNCConnectionGetActivity() to notify
 * errors that occur while the SDK is blocked waiting for the
 * VNCConnectionEventHandle (e.g. errors that are notified by another thread).
 *
 * \see VNCConnection, VNCConnectionActivity, VNCConnectionGetEventHandle()
 */
typedef VNCBearerError VNCCALL
VNCConnectionGetActivity(VNCConnection *pConnection,
                         VNCConnectionActivity *pActivity);

/**
 * \brief Called by the SDK to obtain information about the given connection.
 *
 * \param pConnection The connection for which the information is being
 * requested.
 * \param property The property whose value is being requested.
 *
 * \return The property's value, which should be allocated with
 * VNCBearerAlloc(), or NULL if this connection does not define a value for
 * this property.  The SDK will free a non-NULL return value with
 * VNCBearerFree().
 *
 * \see VNCConnectionProperty, VNCBearerAlloc()
 */
typedef char *VNCCALL
VNCConnectionGetProperty(VNCConnection *pConnection,
                         VNCConnectionProperty property);

/**
 * \brief Called by the SDK to notify the bearer that the timer set with
 * VNCConnectionSetTimer() has expired.
 *
 * See VNCConnectionSetTimer().  The SDK provides the ability for a bearer to
 * set a single timer for each connection.  If the bearer makes use of the
 * facility, then it must implement VNCConnectionSetTimer(), which will be
 * called by the SDK when the timer expires.
 *
 * \param pConnection The connection for which the timer has expired.
 *
 * \return VNCBearerErrorNone on success, or some other VNCBearerError on
 * failure.  If the bearer uses VNCConnectionSetTimer() to define a timeout
 * before which the connection must be established, then the return value
 * should be VNCBearerErrorConnectionTimedOut.
 *
 * \see VNCConnectionSetTimer().  
 */
typedef VNCBearerError VNCCALL
VNCConnectionTimerExpired(VNCConnection *pConnection);

/**
 * \brief APIs to be implemented by the bearer and called by the SDK.
 */
typedef struct
{
    /**
     * Called by the SDK prior to unloading a bearer DLL or shared object.
     */
    VNCBearerTerminate *vncBearerTerminate;
    /**
     * Called by the SDK to create a new VNCConnection.
     */
    VNCBearerCreateConnection *vncBearerCreateConnection;
    /**
     * Called by the SDK to obtain information about the bearer implementation.
     */
    VNCBearerGetProperty *vncBearerGetProperty;

    /**
     * Called by the SDK to begin the process of establishing a connection.
     */
    VNCConnectionEstablish *vncConnectionEstablish;
    /**
     * Called by the SDK to close and destroy a VNCConnection object.
     */
    VNCConnectionDestroy *vncConnectionDestroy;
    /**
     * Called by the SDK to read data from a connection.
     */
    VNCConnectionRead *vncConnectionRead;
    /**
     * Called by the SDK to write data to a connection.
     */
    VNCConnectionWrite *vncConnectionWrite;
    /**
     * Called by the SDK to retrieve the current VNCConnectionEventHandle for
     * the given connection.
     */
    VNCConnectionGetEventHandle *vncConnectionGetEventHandle;
    /**
     * Called by the SDK to enquire about activity on the given connection.
     */
    VNCConnectionGetActivity *vncConnectionGetActivity;
    /**
     * Called by the SDK to obtain information about the given connection.
     */
    VNCConnectionGetProperty *vncConnectionGetProperty;
    /**
     * Called by the SDK to notify the bearer that the timer set with
     * VNCConnectionSetTimer() has expired.
     */
    VNCConnectionTimerExpired *vncConnectionTimerExpired;
} VNCBearerInterface;

/**
 * \brief Called by the bearer to allocate memory whose ownership is to be
 * transferred to the SDK.
 *
 * The start address of the returned memory is aligned to a word boundary, and
 * the memory is filled with zeroes.
 * 
 * The SDK will free the allocated memory with VNCBearerFree().
 *
 * \param bearerContext The bearer VNCBearerContext that was passed to
 * VNCBearerInitialize().
 * \param size The size of data to be allocated.
 */
typedef void *VNCCALL
VNCBearerAlloc(VNCBearerContext bearerContext, size_t size);

/**
 * \brief Called by the bearer to free memory whose ownership has been
 * transferred to the bearer by the SDK.
 * 
 * \param bearerContext The bearer VNCBearerContext that was passed to
 * VNCBearerInitialize().
 * \param buffer The buffer to be freed.
 */
typedef void VNCCALL
VNCBearerFree(VNCBearerContext bearerContext, void *buffer);

/**
 * \brief Called by the bearer to write to the SDK's log.
 *
 * VNCConnectionLog() is preferred to VNCBearerLog() because VNCConnectionLog()
 * is guaranteed to work correctly if called from any thread. VNCBearerLog()
 * does not work if called from a thread that was started by the bearer.
 *
 * Otherwise, this method is identical to VNCConnectionLog().
 *
 * \param bearerContext The bearer VNCBearerContext that was passed to
 * VNCBearerInitialize().
 * \param text The text to write to the log.
 */
typedef void VNCCALL
VNCBearerLog(VNCBearerContext bearerContext, const char *text);

/**
 * \brief Called by the bearer to extract, optionally Base64-decode and return
 * a field from the given command string.
 *
 * Fields values that contain binary data should always be Base64-encoded.
 * Field values that are strings but may contain characters that are
 * significant in command strings (e.g. '=') or that may cause problems in
 * transit should likewise always be Base64-encoded.  Use the base64decode
 * parameter to indicate to the SDK whether the field's value is Base64-encoded
 * or not.
 *
 * In the case of fields whose values are not Base-64 encoded, the SDK
 * automatically appends a NUL byte to the returned value.  This NUL byte is
 * not counted in the value returned in *pSize.
 *
 * The bearer should free the returned value with VNCBearerFree() when it is no
 * longer required.
 * 
 * \param connectionContext The VNCConnectionContext that was passed to
 * VNCBearerCreateConnection().
 * \param name The name of the field.
 * \param base64decode If true, then the value will be Base64-decoded before
 * it is returned.
 * \param pSize If pSize is not NULL, then on successful return, *pSize
 * contains the size of the decoded field's value in bytes.  This size does not
 * include the auto-added NUL.
 *
 * \return The field's value, or NULL if it is not present.
 */
typedef char *VNCCALL
VNCConnectionGetCommandStringField(
    VNCConnectionContext connectionContext,
    const char *name,
    int base64decode,
    size_t *pSize);

/**
 * \brief Called by the bearer to extract a configuration from the SDK.
 *
 * The SDK may be able to provide a bearer-specific configuration. This configuration 
 * is opaque to the SDK. The structure of the configuration is defined by the bearer.
 * The configuration should only contain settings that are static for the lifetime 
 * of the SDK. Settings that vary between connections, such as an address to connect 
 * to, should not be included.
 *
 * The bearer should free the returned value with VNCBearerFree() when it is no
 * longer required.
 *
 * \param connectionContext The VNCConnectionContext that was passed to
 * VNCBearerCreateConnection().
 *
 * \return The bearer configuration, or NULL if no configuration is available.
 */
typedef char *VNCCALL
VNCConnectionGetBearerConfiguration(VNCConnectionContext connectionContext);

/**
 * \brief Called by the bearer to inform the SDK of changes in the connection's
 * status.
 *
 * At a minimum, the bearer should notify:
 *
 *  - whichever of VNCConnectionStatusConnecting or
 *    VNCConnectionStatusListening is more appropriate (notify this immediately
 *    before VNCConnectionEstablish() starts establishing the connection)
 *  - VNCConnectionStatusConnected (notify this once the connection is fully
 *    established)
 *
 * Bearer implementations are encouraged to provide more detailed status
 * notifications where appropriate.  A bearer may define its own status codes
 * if necessary.
 *
 * \param connectionContext The VNCConnectionContext that was passed to
 * VNCBearerCreateConnection().
 * \param status The new status of the connection.
 *
 * \see VNCConnectionStatus 
 */
typedef void VNCCALL
VNCConnectionStatusChange(VNCConnectionContext connectionContext,
                          VNCConnectionStatus status);

/**
 * \brief Called by the bearer to request that the SDK set a timer on the
 * bearer's behalf.
 *
 * The timeout is given by the timeoutMs parameter, and is in milliseconds.  If
 * the timeout elapses before the timer is cancelled, the
 * VNCConnectionTimerExpired() callback will be called.
 *
 * To cancel the timer, call VNCConnectionSetTimer() with timeoutMs <= 0.
 *
 * The SDK provides only one timer per connection.  A second call to
 * VNCConnectionSetTimer() always cancels and supercedes the first.
 *
 * Calls to this should only be made from the context of bearer callbacks from the
 * SDK. Calling this API from threads which are not managed by the SDK will have
 * undefined results.
 * 
 * \param connectionContext The VNCConnectionContext that was passed to
 * VNCBearerCreateConnection().
 * \param timeoutMs timeoutMs The timeout in milliseconds.
 *
 * \see VNCConnectionTimerExpired()
 */
typedef void VNCCALL
VNCConnectionSetTimer(VNCConnectionContext connectionContext,
                      int timeoutMs);

/**
 * \brief Called by the bearer to check whether a particular feature is
 * licensed.
 *
 * This method allows you to ensure that your custom bearers are usable only in
 * certain applications.  The feature check succeeds if the SDK is authorized
 * to use at least one of the requested features.  The features are searched
 * for both in the the licenses available to the SDK and in the set of features
 * defined by calls by the application to the SDK's API.
 *
 * If none of the features are licensed, then the bearer may either continue to
 * operate, restricting functionality as appropriate, or return
 * VNCBearerErrorFeatureNotLicensed to the SDK, in which case the SDK will
 * terminate the session.
 *
 * \param connectionContext The bearer VNCConnectionContext that was passed to
 * VNCBearerCreateConnection().
 * \param featureID The feature identifier for which to check for a license.
 * \param *pResult On successful return, *pResult is non-zero if any of the
 * features is licensed, and zero if not.
 * \retval VNCBearerErrorNone The feature check was made successfully.  This
 * does *not* imply that any of the features is licensed; check *pResult.
 * \retval VNCBearerErrorInvalidParameter featureIDs or pResult is NULL, or
 * featureIDCount is zero.
 *
 * Non-zero if the feature is licensed, or zero if not.
 */
typedef VNCBearerError VNCCALL
VNCConnectionLocalFeatureCheck(VNCConnectionContext connectionContext,
                               const unsigned *featureIDs,
                               size_t featureIDCount,
                               int *pResult);

/**
 * \brief Called by the bearer to write to the SDK's log.
 *
 * Bearer implementations are encouraged to log significant events during
 * connection establishment, and any other information that may help offline
 * diagnosis of problems.  For example:
 *
 *  - name and version of any underlying software (e.g. OEM transport APIs)
 *  - what is being connected to (e.g. the remote address)
 *  - any authentication steps
 *  - the reason why a connection failed
 *  - the reason why an established connection was lost
 *
 * It is particularly important to log the reason for a connection failures if
 * the reason cannot easily be conveyed with an error code.
 *
 * VNCConnectionLog() is preferred to VNCBearerLog() because VNCConnectionLog()
 * is guaranteed to work correctly if called from any thread. VNCBearerLog()
 * does not work if called from a thread that was started by the bearer.
 *
 * \param connectionContext The VNCConnectionContext that was passed to
 * VNCBearerCreateConnection().
 * \param text The text to write to the log.
 */
typedef void VNCCALL
VNCConnectionLog(VNCConnectionContext connectionContext, const char *text);

/**
 * \brief Called by the bearer to obtain a pointer to the 'dynamic context'.
 *
 * The dynamic context allows the bearer to access runtime state about
 * a related external module, which is not provided via the command string.
 * Typically this is useful where such state must be shared with another
 * part of the system (e.g. to use a single TCP or USB connection).
 * 
 * \param bearerContext The bearer VNCBearerContext that was passed to
 * VNCBearerInitialize().
 * \return The bearer VNCBearerDynamicContext.
 */
typedef VNCBearerDynamicContext VNCCALL
VNCBearerGetDynamicContext(VNCBearerContext bearerContext);

/**
 * \brief APIs provided by the SDK for calling by the bearer.
 */
typedef struct
{
    /**
     * Called by the bearer to allocate memory whose ownership is to be
     * transferred to the SDK.
     */
    VNCBearerAlloc *vncBearerAlloc;
    /**
     * Called by the bearer to free memory whose ownership has been transferred
     * to the bearer by the SDK.
     */
    VNCBearerFree *vncBearerFree;
    /**
     * Called by the bearer to write to the SDK's log.
     */
    VNCBearerLog *vncBearerLog;
    /**
     * Called by the bearer to extract, optionally Base64-decode and return a
     * field from the given command string.
     */
    VNCConnectionGetCommandStringField *vncConnectionGetCommandStringField;
    /**
     * Called by the bearer to inform the SDK of changes in the connection's
     * status.
     */
    VNCConnectionStatusChange *vncConnectionStatusChange;
    /**
     * Called by the bearer to request that the SDK set a timer on the bearer's
     * behalf.
     */
    VNCConnectionSetTimer *vncConnectionSetTimer;
    /**
     * Called by the bearer to check whether a particular feature is licensed.
     */
    VNCConnectionLocalFeatureCheck *vncConnectionLocalFeatureCheck;
    /**
     * Called by the bearer to extract a configuration from the SDK.
     */
    VNCConnectionGetBearerConfiguration *vncConnectionGetBearerConfiguration;
    /** Called by the bearer to write to the SDK's log. */
    VNCConnectionLog *vncConnectionLog;
    /**
     * Called by the bearer to obtain a pointer to the 'dynamic context'.
     */
    VNCBearerGetDynamicContext *vncBearerGetDynamicContext;
} VNCBearerSupportingAPI;

/**
 * \brief The type of the sole entry point to be exported by the bearer DLL or
 * shared object.
 * 
 * \param bearerName The name of the bearer implementation that is being
 * requested by the SDK.
 * \param pBearerInterface Structure to contain the addresses of the bearer's
 * implementation functions.  The implementation of VNCBearerInitialize() must
 * fill in this structure before returning.
 * \param bearerInterfaceSize The size of *pBearerInterface.
 * \param pBearerSupportingAPI Structure containing functions provided by the
 * SDK for calling by the bearer.
 * \param bearerSupportingAPISize The size of *pBearerSupportingAPI.
 *
 * \return The new bearer object.  The SDK will call VNCBearerTerminate() when
 * this object is no longer required.
 */
typedef VNCBearer *VNCCALL VNCBearerInitializeType(
    const char *bearerName,
    VNCBearerContext bearerContext,
    VNCBearerInterface *pBearerInterface,
    size_t bearerInterfaceSize,
    const VNCBearerSupportingAPI *pBearerSupportingAPI,
    size_t bearerSupportingAPISize);

#ifdef __cplusplus
// Terminate 'extern "C"' block.
}
#endif

#ifdef __cplusplus

/**
 * \brief Provides a bitwise-or operator for VNCConnectionActivity.
 *
 * This is a convenience for C++ developers.
 *
 * \param lhs The left-hand operand.
 * \param rhs The right-hand operand.
 *
 * \return The bitwise-or of the two operands.
 */
inline VNCConnectionActivity operator|(VNCConnectionActivity lhs,
                                       VNCConnectionActivity rhs)
{
    return static_cast<VNCConnectionActivity> (static_cast<unsigned> (lhs) |
                                               static_cast<unsigned> (rhs));
}

/**
 * \brief Provides a bitwise-or-assignment operator for VNCConnectionActivity.
 *
 * This is a convenience for C++ developers.
 *
 * \param lhs The left-hand operand, to which we will assign the result.
 * \param rhs The right-hand operand.
 *
 * \return The bitwise-or of the two operands.
 */
inline VNCConnectionActivity &operator|=(VNCConnectionActivity &lhs,
                                         VNCConnectionActivity rhs)
{
    lhs = lhs | rhs;
    return lhs;
}

#endif


#endif /* !defined(__VNCBEARER_H__) */
