/* Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
*/

#ifndef __VNCCONNECTIONTCPBASE_H__
#define __VNCCONNECTIONTCPBASE_H__

#include <VNCConnectionImpl.h>

#ifdef _WIN32
#include <windows.h>
#include <winsock2.h>
#include <ws2tcpip.h>
#else
#include <errno.h>
#include <netdb.h>
#include <netinet/in.h>
#endif

/// \brief Base class for TCP based connection implementations conforming
/// to the definition in vncbearer.h.
/// 
/// Implementations using this base class are expected to be created by a
/// bearer which is sub-classed from VNCBearerTcpBase. When sub-classing
/// this class, the only required additional method is an implementation 
/// of establish() as described in VNCConnectionImpl.
///
/// For example uses of this base class see VNCConnectionL, VNCConnectionD
/// and VNCConnectionC from the L, D and C bearers.
class VNCConnectionTcpBase : public VNCConnectionImpl
{
public:
  /// \brief Reads data from the TCP connection.
  ///
  /// \param buffer The buffer into which to read data.
  /// \param pBufferSize The size of the supplied buffer, on return this is
  /// modified to the number of bytes received.
  ///
  /// \return VNCBearerErrorNone on success, or some other VNCBearerError on
  /// failure.
  virtual VNCBearerError read(unsigned char *buffer, size_t *pBufferSize);
  /// \brief Writes data to the TCP connection.
  /// 
  /// \param buffer The buffer from which to write data.
  /// \param pBufferSize The size of the supplied buffer, on return this is
  /// modified to the number of bytes sent.
  ///
  /// \return VNCBearerErrorNone on success, or some other VNCBearerError on
  /// failure.
  virtual VNCBearerError write(const unsigned char *buffer, size_t *pBufferSize);
  /// \brief Retrieve the current VNCConnectionEventHandle for the connection.
  /// 
  /// The VNCConnectionEventHandle returned by this method can change depending
  /// on if the TCP socket is listening or is connected.
  /// 
  /// \param pEventHandle On successful return, the bearer should make
  /// *pEventHandle equal to the VNCConnectionEventHandle for the connection.
  /// \param pWriteNotification If not null then the bearer should set the value of
  /// *pWriteNotification to 0 if write ready notification for pEventHandle are not
  /// necessary.
  /// 
  /// \return VNCBearerErrorNone on success, or some other VNCBearerError on
  /// failure.
  virtual VNCBearerError getEventHandle(VNCConnectionEventHandle *pEventHandle, int *pWriteNotification);
  /// \brief Used to enquire about activity on the TCP connection.
  /// 
  /// \param pActivity On successful return, the bearer should make *pActivity
  /// equal to either VNCConnectionActivityNone or some bitwise combination of
  /// VNCConnectionActivityReadReady and VNCConnectionActivityWriteReady.
  ///
  /// \return VNCBearerErrorNone on success, or some other VNCBearerError on
  /// failure.  It is often convenient to use VNCConnectionGetActivity() to notify
  /// errors that occur while the SDK is blocked waiting for the
  /// VNCConnectionEventHandle (e.g. errors that are notified by another thread).
  virtual VNCBearerError getActivity(VNCConnectionActivity *pActivity);
  /// \brief Called by the SDK to obtain information about the given connection.
  /// 
  /// \param property The property whose value is being requested.
  ///
  /// \return The property's value on success.  This should be allocated using
  /// sdkAlloc() or sdkStrdup(). Ownership passes to the caller. Return NULL
  /// if this bearer does not define a value for this property.
  virtual char *getProperty(VNCConnectionProperty property) const;

#ifdef _WIN32
  /// \brief A TCP socket.
  typedef SOCKET socket_t;
  /// \brief Network error type.
  typedef DWORD network_error_t;
#else
  /// \brief A TCP socket.
  typedef int socket_t;
  /// \brief Network error type.
  typedef int network_error_t;
#endif

protected:
  /// \brief Constructor
  /// 
  /// \param bearer The bearer creating this connection.
  /// \param connectionContext The bearer context provided by the SDK.
  VNCConnectionTcpBase(VNCBearerImpl &bearer,
                       VNCConnectionContext connectionContext);
  /// \brief Destructor
  virtual ~VNCConnectionTcpBase();

  /// \brief Starts to establish a connection to a given address.
  /// 
  /// Starts connecting to a remote host using the provided details.
  /// 
  /// If sub-classes of this class wish to intercept and/or process data
  /// transmitted and received over the established connection then they 
  /// should override the implementations of getActivity(), read() and write().
  /// The implementation of the VNCConnectionD class in vncbearer-D shows
  /// one way to do this.
  /// 
  /// \param hostnameOrAddress Hostname or IP address of remote host.
  /// \param port Textual representation of the remote TCP port number.
  /// 
  /// \return VNCBearerErrorNone on success, or some other VNCBearerError on
  /// failure.
  VNCBearerError connect(const char *hostnameOrAddress, const char *port);

  /// \brief Starts to establish a connection.
  /// 
  /// Starts establishing a connection using the address and port number
  /// specified in the command string with 'a' and 'p' keys.
  /// 
  /// See connect(const char *, const char *) for further details.
  ///
  /// \return VNCBearerErrorNone on success, or some other VNCBearerError on
  /// failure.
  VNCBearerError connect();

  /// \brief Starts the connection listening on a given port.
  /// 
  /// Sets this connection as a listening connection and starts listening on
  /// the provide local port.
  /// 
  /// When a remote host connects to the listening port the connection will
  /// be automatically be established.
  /// 
  /// If sub-classes of this class wish to intercept and/or process data
  /// transmitted and received over the established connection then they 
  /// should override the implementations of getActivity(), read() and write().
  /// The implementation of the VNCConnectionD class in vncbearer-D shows
  /// one way to do this.
  /// 
  /// \param port Textual representation of the local port number.
  /// 
  /// \return VNCBearerErrorNone on success, or some other VNCBearerError on
  /// failure.
  VNCBearerError listen(const char* port);

  /// \brief Starts the connection listening on a given port and network address.
  /// 
  /// Sets this connection as a listening connection and starts listening on
  /// the provided local port and network address.
  /// 
  /// When a remote host connects to the listening port the connection will
  /// be automatically established.
  /// 
  /// If sub-classes of this class wish to intercept and/or process data
  /// transmitted and received over the established connection then they 
  /// should override the implementations of getActivity(), read() and write().
  /// The implementation of the VNCConnectionD class in vncbearer-D shows
  /// one way to do this.
  /// 
  /// \param hostAddress IP address on which to listen (in network byte order).
  /// \param port Textual representation of the local port number.
  /// 
  /// \return VNCBearerErrorNone on success, or some other VNCBearerError on
  /// failure.
  VNCBearerError listen(in_addr hostAddress, const char* port);

  /// \brief Starts the connection listening.
  /// 
  /// Starts listening for incoming connections using the port number specified
  /// in the command string with the 'p' key.
  /// 
  /// See listen(const char *) for further details.
  ///
  /// \return VNCBearerErrorNone on success, or some other VNCBearerError on
  /// failure.
  VNCBearerError listen();

  /// \brief Retrieves information about the listening socket.
  ///
  /// Retrieves information about the addresses and ports being listened on.
  /// This is a space separated list of IP address and port combinations.
  /// For example if listening on port 5900 this method might return:
  /// "127.0.0.1:5900 192.168.1.2:5900 203.0.113.7:5900".
  ///
  /// The caller should free the returned value with the sdkFree() method in 
  /// the bearer when it is no longer required. Consequently the returned 
  /// pointer should be allocated with sdkAlloc().
  ///
  /// \return Information about the listening socket, or NULL if no information
  /// available. 
  char *getListeningInformation() const;

  /// \brief Validates a string as a valid port number.
  /// 
  /// \param s Textual representation of port to decode (NUL terminated).
  /// \param pPort If not NULL then on successful validation the
  /// value pointed to will contain the port value decoded
  /// from the provided string.
  /// 
  /// \return VNCBearerErrorNone if valid port number, VNCBearerErrorInvalidCommandString
  /// if not a valid port number.
  static VNCBearerError validatePortNumber(const char *s,
                                           unsigned short *pPort = 0);

  /// \brief Sets the socket as non-blocking.
  /// 
  /// Performs platform specific actions necessary to enable
  /// non-blocking I/O on the provided socket.
  /// 
  /// \param s The socket to enable non-blocking I/O on.
  static VNCBearerError setNonBlocking(socket_t s);

  /// \brief Disable's Nagle's algorithm on a socket.
  ///
  /// Setting TCP_NODELAY improves viewer responsiveness by helping 
  /// pointer and key events get to the server quicker.  The SDKs use internal
  /// buffering to avoid making many small writes, so Nagle's algorithm would
  /// be redundant anyway.
  ///
  /// \param s The socket to set TCP_NODELAY on.
  static VNCBearerError setTcpNoDelay(socket_t s);

  /// \brief Enables address reuse on a socket.
  ///
  /// Performs platform specific actions necessary to enable
  /// SO_ADDRREUSE ioctl() or equivalent on the provided
  /// socket.
  ///
  /// \param s The socket to enable address reuse on.
  static VNCBearerError setReuseAddr(socket_t s);

  /// \brief Retrieves the last networking error.
  ///
  /// Uses getLastNetworkError() and bearerErrorForNetworkError() to
  /// retrieve and convert the last network error.
  ///
  /// \return The last networking error converted to a VNCBearerError.
  static inline VNCBearerError bearerErrorForNetworkError();

  /// \brief Converts a platform specific error to a VNCBearerError
  ///
  /// \param error Platform specific error to convert.
  /// \return The VNCBearerError equivalent to the platform specific error.
  static VNCBearerError bearerErrorForNetworkError(network_error_t error);
  
  /// \brief Retrieves the last networking error.
  /// 
  /// This uses the platform specific method of retrieving the last networking
  /// error.
  ///
  /// \return Platform specific error value.
  static network_error_t getLastNetworkError();

  /// \brief An invalid socket.
  ///
  /// Used as values for connection and rendezvous when they are not valid.
  static const socket_t InvalidSocket;
  
  /// \brief Connection socket.
  ///
  /// The socket for the established TCP connection. When no connection is
  /// established this will be equal to the InvalidSocket member.
  socket_t connection;
  
  /// \brief Rendezvous or socket.
  ///
  /// The listening socket for the TCP connection. When no listening connection
  /// is outstanding this will be equal to the InvalidSocket member.
  socket_t rendezvous;

private:
  VNCBearerError beginConnectionAttempt();
  VNCBearerError startListening();

  typedef int VNCCALL GetSockNamePrototype(socket_t, struct sockaddr *, socklen_t *);
  char *formatAddress(socket_t s, GetSockNamePrototype *pGetSockname) const;

  addrinfo *pPeerAddressInfo;
  addrinfo *pPeerAddressForCurrentConnectionAttempt;

#ifdef _WIN32

  static bool checkEvent(const WSANETWORKEVENTS &networkEvents,
                         long mask,
                         int bit,
                         VNCBearerError *pError);

  HANDLE event;
  VNCBearerError savedError;

#else

  VNCBearerError doSelect(int fd, bool *pReadable, bool *pWriteable = NULL);

#endif
};

inline VNCBearerError VNCConnectionTcpBase::bearerErrorForNetworkError()
{
  return bearerErrorForNetworkError(getLastNetworkError());
}

#ifdef _WIN32

inline VNCConnectionTcpBase::network_error_t
  VNCConnectionTcpBase::getLastNetworkError()
{
  return WSAGetLastError();
}

#else

inline VNCConnectionTcpBase::network_error_t
  VNCConnectionTcpBase::getLastNetworkError()
{
  return errno;
}

#endif

#endif /* !defined(__VNCCONNECTIONTCPBASE_H__) */
