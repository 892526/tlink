/* Copyright (C) 2002-2018 RealVNC Ltd.  All Rights Reserved.
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#ifdef _WIN32
#include <windows.h>
#include <Iphlpapi.h>
#include <valarray>
#else
#include <fcntl.h>
#include <sys/socket.h>
#include <net/if.h>
#include <netinet/tcp.h>
#include <sys/ioctl.h>
#include <sys/socket.h>
#include <sys/time.h>
#include <sys/types.h>
#include <unistd.h>
#endif

#include <VNCBearerImpl.h>
#include "VNCConnectionTcpBase.h"

namespace
{
  class AddressFormatter
  {
  public:
    AddressFormatter()
    {
      *buf = 0;
    }

    AddressFormatter(const struct sockaddr *pAddr, socklen_t length)
    {
      format(pAddr, length);
    }

    AddressFormatter(const addrinfo *pAddrInfo)
    {
      format(pAddrInfo->ai_addr, static_cast<socklen_t> (pAddrInfo->ai_addrlen));
    }

    inline operator bool() const
    {
      return *buf != 0;
    }

    inline operator const char *() const
    {
      return buf;
    }

    inline const char *s() const
    {
      return buf;
    }

    AddressFormatter &format(const struct sockaddr *pAddr, socklen_t length)
    {
      char address[80];
      char port[16];

      buf[0] = '\0';

      if (getnameinfo(pAddr,
                      length,
                      address,
                      sizeof(address),
                      port,
                      sizeof(port),
                      NI_NUMERICHOST | NI_NUMERICSERV) == 0)
      {
        const char *const format =
          pAddr->sa_family == AF_INET6 ? "[%s]:%s" : "%s:%s";

#if defined(_WIN32_WCE)
        _snprintf(buf, sizeof(buf), format, address, port);
#elif defined(_WIN32)
        _snprintf_s(buf, sizeof(buf), _TRUNCATE, format, address, port);
#else
        snprintf(buf, sizeof(buf), format, address, port);
#endif

        buf[sizeof(buf) - 1] = '\0';
      }

      return *this;
    }

  private:
    char buf[80];
  };
}

const VNCConnectionTcpBase::socket_t VNCConnectionTcpBase::InvalidSocket =
#ifdef _WIN32
  INVALID_SOCKET;
#else
  -1;
#endif

VNCConnectionTcpBase::VNCConnectionTcpBase(
     VNCBearerImpl &bearer,
     VNCConnectionContext connectionContext)
  : VNCConnectionImpl(bearer, connectionContext),
    connection(InvalidSocket),
    rendezvous(InvalidSocket),
    pPeerAddressInfo(NULL),
    pPeerAddressForCurrentConnectionAttempt(NULL)
#ifdef _WIN32
    , event(CreateEvent(NULL, FALSE, FALSE, NULL))
    , savedError(VNCBearerErrorNone)
#endif
{
}

VNCConnectionTcpBase::~VNCConnectionTcpBase()
{
#ifdef _WIN32

  if (event != INVALID_HANDLE_VALUE)
  {
    if (connection != InvalidSocket)
      WSAEventSelect(connection, event, 0);

    if (rendezvous != InvalidSocket)
      WSAEventSelect(rendezvous, event, 0);

    CloseHandle(event);
  }

  if (connection != InvalidSocket)
  {
    shutdown(connection, SD_SEND);
    closesocket(connection);
  }

  if (rendezvous != InvalidSocket)
  {
    shutdown(rendezvous, SD_SEND);
    closesocket(rendezvous);
  }

#else

  if (connection != InvalidSocket)
    close(connection);
  if (rendezvous != InvalidSocket)
    close(rendezvous);

#endif

  if (pPeerAddressInfo)
    freeaddrinfo(pPeerAddressInfo);
}

VNCBearerError VNCConnectionTcpBase::bearerErrorForNetworkError(
    network_error_t error)
{
  switch (error)
  {
#ifdef _WIN32

    case WSAEWOULDBLOCK:
      return VNCBearerErrorNone;

    case WSAENETUNREACH:
      return VNCBearerErrorNetworkUnreachable;

    case WSAEHOSTUNREACH:
      return VNCBearerErrorHostUnreachable;

    case WSAETIMEDOUT:
      return VNCBearerErrorConnectionTimedOut;

    case WSAECONNREFUSED:
      return VNCBearerErrorConnectionRefused;

    case WSAEADDRINUSE:
      return VNCBearerErrorAddressInUse;
      
#else

    case EAGAIN:
#if EAGAIN != EWOULDBLOCK
    case EWOULDBLOCK:
#endif
    case EINPROGRESS:
      return VNCBearerErrorNone;

    case EACCES:
    case EPERM:
      return VNCBearerErrorPermissionDenied;

    case ENETUNREACH:
    case ENOTCONN:
      return VNCBearerErrorNetworkUnreachable;

    case EHOSTUNREACH:
      return VNCBearerErrorHostUnreachable;

    case ETIMEDOUT:
      return VNCBearerErrorConnectionTimedOut;

    case ECONNREFUSED:
      return VNCBearerErrorConnectionRefused;

    case EADDRINUSE:
      return VNCBearerErrorAddressInUse;

#endif

    default:
      return VNCBearerErrorDisconnected;
  }
}

VNCBearerError VNCConnectionTcpBase::connect(const char *hostnameOrAddress,
                                             const char *port)
{
  VNCBearerError bearerError = validatePortNumber(port);

  if (bearerError == VNCBearerErrorNone)
  {
    // Check to see whether we were we were actually given an IP address, so
    // that we only notify VNCConnectionStatusPerformingNameLookup when we
    // actually do have a hostname to resolve.
    addrinfo hints = { AI_NUMERICHOST, AF_INET, SOCK_STREAM, 0, 0, 0, 0, 0 };
    int error = getaddrinfo(hostnameOrAddress, port, &hints, &pPeerAddressInfo);

    if (error)
    {
      // Not an IP address.  Try a hostname.
      statusChange(VNCConnectionStatusPerformingNameLookup);

      hints.ai_flags = 0;
      error = getaddrinfo(hostnameOrAddress, port, &hints, &pPeerAddressInfo);
    }

    if (error || !pPeerAddressInfo)
      bearerError = VNCBearerErrorNameLookupFailure;

    if (bearerError == VNCBearerErrorNone)
    {
      statusChange(VNCConnectionStatusConnecting);

      if ((connection = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP)) == InvalidSocket)
        bearerError = bearerErrorForNetworkError();
    }

    if (bearerError == VNCBearerErrorNone)
      bearerError = setNonBlocking(connection);

    if (bearerError == VNCBearerErrorNone)
      bearerError = setTcpNoDelay(connection);

    if (bearerError == VNCBearerErrorNone)
    {
      // Try each address in turn until we get a connection.
      pPeerAddressForCurrentConnectionAttempt = pPeerAddressInfo;
      bearerError = beginConnectionAttempt();
    }
  }

  return bearerError;
}

// Verify manually that a port number is valid.  If we just pass a string to
// getaddrinfo(), it'll return the same error as it does for DNS lookup
// failures, and we'd prefer to be more specific.
VNCBearerError VNCConnectionTcpBase::validatePortNumber(const char *s,
                                                        unsigned short *pPort)
{
  VNCBearerError error = VNCBearerErrorBadPort;

  if (s)
  {
    char *end = NULL;
    long port = strtol(s, &end, 10);

    if (end > s && end == s + strlen(s) && port >= 0 && port <= 0xffff)
    {
      error = VNCBearerErrorNone;

      if (pPort)
        *pPort = static_cast<unsigned short> (port);
    }
  }

  return error;
}

VNCBearerError VNCConnectionTcpBase::setNonBlocking(socket_t s)
{
  VNCBearerError bearerError = VNCBearerErrorNone;

#ifdef _WIN32

  ULONG one = 1;

  if (ioctlsocket(s, FIONBIO, &one) == InvalidSocket)
    bearerError = bearerErrorForNetworkError();

#else

  if (fcntl(s, F_SETFL, fcntl(s, F_GETFL, 0) | O_NONBLOCK) < 0)
    bearerError = bearerErrorForNetworkError();

#endif

  return bearerError;
}

VNCBearerError VNCConnectionTcpBase::setTcpNoDelay(socket_t s)
{
  VNCBearerError bearerError = VNCBearerErrorNone;

#ifdef _WIN32
  BOOL one = 1;
#else
  int one = 1;
#endif

  if (setsockopt(s,
                 IPPROTO_TCP,
                 TCP_NODELAY,
                 reinterpret_cast<const char *> (&one),
                 sizeof(one)) < 0)
  {
    bearerError = bearerErrorForNetworkError();
  }

  return bearerError;
}

VNCBearerError VNCConnectionTcpBase::setReuseAddr(socket_t s)
{
  VNCBearerError bearerError = VNCBearerErrorNone;

#ifdef _WIN32
  BOOL one = 1;
#else
  int one = 1;
#endif

  if (setsockopt(s,
                 SOL_SOCKET,
                 SO_REUSEADDR,
                 reinterpret_cast<const char *> (&one),
                 sizeof(one)) < 0)
  {
    bearerError = bearerErrorForNetworkError();
  }

  return bearerError;
}

VNCBearerError VNCConnectionTcpBase::connect()
{
  char *hostnameOrAddress = getCommandStringField("a");
  VNCBearerError error = VNCBearerErrorInvalidCommandString;

  if (hostnameOrAddress && strlen(hostnameOrAddress) > 0)
  {
    char *sPort = getCommandStringField("p");

    if (sPort)
    {
      error = connect(hostnameOrAddress, sPort);
      getBearer().sdkFree(sPort);
    }

    getBearer().sdkFree(hostnameOrAddress);
  }

  return error;
}

VNCBearerError VNCConnectionTcpBase::listen(in_addr hostAddress, const char *port)
{
  unsigned short nPort = 0;
  VNCBearerError bearerError = validatePortNumber(port, &nPort);

  if ( (VNCBearerErrorNone == bearerError) &&
       ((rendezvous = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP)) == InvalidSocket) )
  {
    bearerError = VNCBearerErrorDisconnected;
  }

  if ( VNCBearerErrorNone == bearerError )
    bearerError = setNonBlocking(rendezvous);

  if ( VNCBearerErrorNone == bearerError )
    bearerError = setReuseAddr(rendezvous);

  if ( VNCBearerErrorNone == bearerError )
  {
    struct sockaddr_in serverAddr;
    memset(&serverAddr, 0, sizeof(serverAddr));
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_addr = hostAddress;
    serverAddr.sin_port = htons(nPort);

    if (
#ifdef _WIN32
        ::bind(rendezvous, (SOCKADDR*) & serverAddr, sizeof(serverAddr))
#else
        ::bind(rendezvous, (sockaddr*) & serverAddr, sizeof(serverAddr))
#endif
          < 0)
    {
      bearerError = bearerErrorForNetworkError();
    }
  }

  if ( (VNCBearerErrorNone == bearerError) )
  {
    if ( ::listen(rendezvous, 5) == InvalidSocket )
      bearerError = bearerErrorForNetworkError();
  }

  if ( (VNCBearerErrorNone == bearerError) )
  {
#ifdef _WIN32
    WSAEventSelect(rendezvous, event, FD_ACCEPT);
#endif
    statusChange(VNCConnectionStatusListening);
  }

  return bearerError;
}

VNCBearerError VNCConnectionTcpBase::listen(const char *port)
{
  in_addr hostAddress;
  hostAddress.s_addr = htonl(INADDR_ANY);
  return listen(hostAddress, port);
}

VNCBearerError VNCConnectionTcpBase::listen()
{
  VNCBearerError error = VNCBearerErrorInvalidCommandString;

  char *sPort = getCommandStringField("p");

  if (sPort)
  {
    error = listen(sPort);
    getBearer().sdkFree(sPort);
  }

  return error;
}

#ifdef _WIN32
typedef int ssize_t;
#endif

VNCBearerError VNCConnectionTcpBase::read(unsigned char *buffer,
                                          size_t *pBufferSize)
{
  VNCBearerError error = VNCBearerErrorNone;
  ssize_t n = ::recv(connection,
                     reinterpret_cast<char *> (buffer),
#ifdef _WIN32
                     static_cast<int> (*pBufferSize),
#else
                     *pBufferSize,
#endif
                     0);

  if (n > 0)
  {
#ifndef _WIN32
    // See notes in the *NIX implementation of getActivity() below.
    if (getStatus() == VNCConnectionStatusConnecting)
      statusChange(VNCConnectionStatusConnected);
#endif

    *pBufferSize = static_cast<size_t> (n);

#ifdef _WIN32
  if(savedError != VNCBearerErrorNone) {
    SetEvent(event);
  }
#endif
  }
  else
  {
    // If the socket was readable and we 'successfully' read 0 bytes (i.e.
    // ::recv() returned 0), we've disconnected (the peer performed a clean
    // shutdown). If ::recv() returned -1, then it has set a network error for
    // us to inspect.
    if (n == 0)
    {
      error = VNCBearerErrorDisconnected;
    }
    else
    {
      error = bearerErrorForNetworkError();

#ifdef _WIN32
      if (error == VNCBearerErrorNone)
        error = savedError;
#endif
    }

    *pBufferSize = 0;
  }

  return error;
}


VNCBearerError VNCConnectionTcpBase::write(const unsigned char *buffer,
                                           size_t *pBufferSize)
{
  VNCBearerError error = VNCBearerErrorNone;
  ssize_t n = 0;
#ifdef _WIN32
  do {
  n = ::send(connection,
             reinterpret_cast<const char *> (buffer),
             static_cast <int> (*pBufferSize),
             0);
  } while (n == SOCKET_ERROR && WSAGetLastError() == WSAEINTR);

  if (n != SOCKET_ERROR)
  {
    *pBufferSize = static_cast<size_t> (n);
  }
  else
  {
    error = bearerErrorForNetworkError();
    *pBufferSize = 0;
  }

#else
  do {
    n = ::send(connection,
               reinterpret_cast<const char*> (buffer),
               *pBufferSize,
               0);
  } while (n == -1 && errno == EINTR);
  
  if (n >= 0)
  {
    *pBufferSize = static_cast<size_t> (n);
  }
  else
  {
    error = bearerErrorForNetworkError();
    *pBufferSize = 0;
  }
#endif

  return error;
}

char *VNCConnectionTcpBase::formatAddress(socket_t s,
                                          GetSockNamePrototype *pGetSockName) const
{
  char *result = NULL;
  struct sockaddr address;
  socklen_t length = static_cast<socklen_t> (sizeof(address));

  if ((*pGetSockName)(s, &address, &length) == 0)
    result = bearer.sdkStrdup(AddressFormatter(&address, length));

  return result;
}

char *VNCConnectionTcpBase::getProperty(VNCConnectionProperty property) const
{
  switch (property)
  {
    case VNCConnectionPropertyListeningInformation:
      return getStatus() == VNCConnectionStatusListening
        ? getListeningInformation()
        : NULL;

    case VNCConnectionPropertyLocalAddress:
      return formatAddress(connection, &getsockname);

    case VNCConnectionPropertyPeerAddress:
      return formatAddress(connection, &getpeername);

    default:
      return VNCConnectionImpl::getProperty(property);
  }
}

#ifdef _WIN32

VNCBearerError VNCConnectionTcpBase::getEventHandle(
    VNCConnectionEventHandle *pEventHandle,
    int *pWriteNotification)
{
  *pEventHandle = event;
  (void) pWriteNotification;
  return VNCBearerErrorNone;
}

bool VNCConnectionTcpBase::checkEvent(const WSANETWORKEVENTS &networkEvents,
                                      long mask,
                                      int bit,
                                      VNCBearerError *pError)
{
  bool result = false;

  // Don't bother checking for further events if we already have an error
  if (*pError == VNCBearerErrorNone && (networkEvents.lNetworkEvents & mask))
  {
    network_error_t networkError =
      static_cast<network_error_t> (networkEvents.iErrorCode[bit]);

    if (networkError)
      *pError = bearerErrorForNetworkError(networkError);

    result = true;
  }

  return result;
}

VNCBearerError VNCConnectionTcpBase::getActivity(
    VNCConnectionActivity *pActivity)
{
  VNCBearerError error = VNCBearerErrorNone;
  WSANETWORKEVENTS networkEvents = { 0 };

  *pActivity = VNCConnectionActivityNone;

  if ( getStatus() == VNCConnectionStatusListening )
  {
    unsigned activity = VNCConnectionActivityNone;
    if (WSAEnumNetworkEvents(rendezvous, event, &networkEvents) == SOCKET_ERROR)
    {
      error = bearerErrorForNetworkError();
    }
    else
    {
      if (checkEvent(networkEvents, FD_ACCEPT, FD_ACCEPT_BIT, &error))
      {
        struct sockaddr_in clientAddr;
        int len = sizeof(clientAddr);

        if ( (connection = ::accept(rendezvous,
                                    (struct sockaddr*)&clientAddr,
                                    &len)) == InvalidSocket )
        {
          error = bearerErrorForNetworkError();
        }
        else
        {
          if (error == VNCBearerErrorNone)
            error = setNonBlocking(connection);

          if (error == VNCBearerErrorNone)
            error = setTcpNoDelay(connection);

          if (error == VNCBearerErrorNone)
          {
            shutdown(rendezvous, SD_SEND);
            closesocket(rendezvous);
            rendezvous = InvalidSocket;

            if (WSAEventSelect(connection,
                               event,
                               FD_READ | FD_WRITE | FD_CLOSE) == SOCKET_ERROR)
            {
              error = bearerErrorForNetworkError();
            }
            else
            {
              statusChange(VNCConnectionStatusConnected);
            }
          }
          else
          {
            shutdown(connection, SD_SEND);
            closesocket(connection);
            connection = InvalidSocket;
          }
        }
      }
    }
  }
  else if (WSAEnumNetworkEvents(connection, event, &networkEvents) == SOCKET_ERROR)
  {
    error = bearerErrorForNetworkError();
  }
  else
  {
    if (getStatus() == VNCConnectionStatusConnecting)
    {
      // See if the connection attempt has completed.
      if (checkEvent(networkEvents, FD_CONNECT, FD_CONNECT_BIT, &error))
      {
        if (error == VNCBearerErrorNone)
        {
          logf("connected to %s",
               AddressFormatter(pPeerAddressForCurrentConnectionAttempt).s());
          statusChange(VNCConnectionStatusConnected);

          WSAEventSelect(connection, event, FD_READ | FD_WRITE | FD_CLOSE);
        }
        else if (pPeerAddressForCurrentConnectionAttempt->ai_next)
        {
          // We have another address to try.
          pPeerAddressForCurrentConnectionAttempt =
            pPeerAddressForCurrentConnectionAttempt->ai_next;

          error = beginConnectionAttempt();
        }
      }
    }
    else
    {
      if (checkEvent(networkEvents, FD_READ, FD_READ_BIT, &error))
        *pActivity |= VNCConnectionActivityReadReady;

      if (checkEvent(networkEvents, FD_WRITE, FD_WRITE_BIT, &error))
        *pActivity |= VNCConnectionActivityWriteReady;

      // Check for the connection having closed.  With Winsock, this can happen
      // at the same time as the socket being readable, especially if there's a
      // TCP frame with FIN and PSH flags both set.  In this case, save the
      // error code and return it when there's no more data to read.
      if (error == VNCBearerErrorNone)
      {
        if (checkEvent(networkEvents, FD_CLOSE, FD_CLOSE_BIT, &savedError))
        {
          if (savedError == VNCBearerErrorNone)
            savedError = VNCBearerErrorDisconnected;
        }

        // But if it's not readable, we can report it right away.
        if ((*pActivity & VNCConnectionActivityReadReady) == 0)
          error = savedError;
      }
    }
  }

  return error;
}

VNCBearerError VNCConnectionTcpBase::beginConnectionAttempt()
{
  VNCBearerError error = VNCBearerErrorNone;

  logf("attempting connection to %s",
       AddressFormatter(pPeerAddressForCurrentConnectionAttempt).s());

  if (WSAConnect(connection,
                 pPeerAddressForCurrentConnectionAttempt->ai_addr,
                 static_cast<int> (pPeerAddressForCurrentConnectionAttempt->ai_addrlen),
                 0,
                 0,
                 0,
                 0) == SOCKET_ERROR)
  {
    network_error_t networkError = getLastNetworkError();

    if (networkError == WSAEWOULDBLOCK)
      WSAEventSelect(connection, event, FD_CONNECT | FD_CLOSE);
    else if (networkError)
      error = bearerErrorForNetworkError(networkError);
    else
      error = VNCBearerErrorDisconnected;
  }
  else
  {
    WSAEventSelect(connection, event, FD_READ | FD_WRITE | FD_CLOSE);
    statusChange(VNCConnectionStatusConnected);
  }

  return error;
}

char *VNCConnectionTcpBase::getListeningInformation() const
{
  char *result = NULL;
  char *sPort = getCommandStringField("p");

  if (sPort)
  {
    std::string addresses;

    PIP_ADAPTER_INFO pAdapterInfo = NULL;
    PIP_ADAPTER_INFO pAdapter = NULL;

    // Make an initial call to GetAdaptersInfo to get
    // the necessary size into the ulOutBufLen variable
    ULONG ulOutBufLen = 0;

    if (GetAdaptersInfo(pAdapterInfo, &ulOutBufLen) == ERROR_BUFFER_OVERFLOW)
    {
      std::valarray<char> buffer(ulOutBufLen);
      pAdapterInfo = (IP_ADAPTER_INFO *) &buffer[0];

      if (GetAdaptersInfo(pAdapterInfo, &ulOutBufLen) == NO_ERROR)
      {
        pAdapter = pAdapterInfo;
        while (pAdapter)
        {
          if (strcmp(pAdapter->IpAddressList.IpAddress.String, "0.0.0.0") != 0)
          {
            if (!addresses.empty())
              addresses += " ";
            addresses += pAdapter->IpAddressList.IpAddress.String;
            addresses += ":";
            addresses += sPort;
          }
          pAdapter = pAdapter->Next;
        }
      }
    }

    if (addresses.length() > 0)
      result = bearer.sdkStrdup(addresses.c_str());

    bearer.sdkFree(sPort);
  }

  return result;
}

#else

VNCBearerError VNCConnectionTcpBase::getEventHandle(
    VNCConnectionEventHandle *pEventHandle,
    int *pWriteNotification)
{
  *pEventHandle = InvalidSocket;
  (void)pWriteNotification;
  if (connection != InvalidSocket)
    *pEventHandle = connection;
  else if (getStatus() == VNCConnectionStatusListening)
    *pEventHandle = rendezvous;

  return *pEventHandle != InvalidSocket ? VNCBearerErrorNone
                                        : VNCBearerErrorDisconnected;
}

VNCBearerError VNCConnectionTcpBase::doSelect(int fd,
                                              bool *pReadable,
                                              bool *pWriteable)
{
  VNCBearerError error = VNCBearerErrorNone;
  fd_set readFds, writeFds, errorFds;
  struct timeval timeout = { 0, 0 };

  *pReadable = false;

  if (pWriteable)
    *pWriteable = false;

  FD_ZERO(&readFds);
  FD_SET(fd, &readFds);
  FD_ZERO(&writeFds);
  FD_SET(fd, &writeFds);
  FD_ZERO(&errorFds);
  FD_SET(fd, &errorFds);

  int result = select(fd + 1,
                      &readFds,
                      pWriteable ? &writeFds : NULL,
                      &errorFds,
                      &timeout);

  if (result < 0)
  {
    error = bearerErrorForNetworkError();
  }
  else if (result > 0)
  {
    if (FD_ISSET(fd, &errorFds))
    {
      error = bearerErrorForNetworkError();
    }
    else
    {
      if (FD_ISSET(fd, &readFds))
        *pReadable = true;

      if (pWriteable && FD_ISSET(fd, &writeFds))
        *pWriteable = true;
    }
  }

  return error;
}

VNCBearerError VNCConnectionTcpBase::getActivity(
    VNCConnectionActivity *pActivity)
{
  VNCBearerError error = VNCBearerErrorNone;

  *pActivity = VNCConnectionActivityNone;

  if (getStatus() == VNCConnectionStatusListening)
  {
    bool readable = false;

    if ((error = doSelect(rendezvous, &readable)) == VNCBearerErrorNone)
    {
      struct sockaddr address;
      socklen_t addressSize = sizeof(address);
      memset(&address, 0, addressSize);

      connection = accept(rendezvous, &address, &addressSize);

      if (connection < 0)
      {
        error = bearerErrorForNetworkError();
      }
      else
      {
        if (error == VNCBearerErrorNone)
          error = setNonBlocking(connection);

        if (error == VNCBearerErrorNone)
          error = setTcpNoDelay(connection);

        if (error == VNCBearerErrorNone)
        {
          close(rendezvous);
          rendezvous = InvalidSocket;
          statusChange(VNCConnectionStatusConnected);
        }
        else
        {
          close(connection);
          connection = InvalidSocket;
        }
      }
    }
  }
  else
  {
    bool readable = false;
    bool writeable = false;

   
    // When connecting outwards, the OS will notify us of a 'connection
    // refused' by calling select to check for writability. Once the select
    // has returned, it can be verified if the connection was successful or
    // not by checking SO_ERROR (see EINPROGRESS section in the man page for
    // connect(2)) For this reason, we don't notify
    // VNCConnectionStatusConnected when the socket is readable immediately
    // after we connect, but when getActivity() is called in order to monitor
    // the connection.

    if ((error = doSelect(connection,
                          &readable,
                          &writeable)) == VNCBearerErrorNone)
    {
      if (writeable && getStatus() == VNCConnectionStatusConnecting) {
        int sockerr = 0;
        socklen_t len = sizeof(sockerr);
        if (!getsockopt(connection, SOL_SOCKET, SO_ERROR, &sockerr, &len)) {
          // If SO_ERROR is zero, connection was successful
          if (!sockerr) {
            logf("connected to %s",
                 AddressFormatter(pPeerAddressForCurrentConnectionAttempt).s());
            statusChange(VNCConnectionStatusConnected);     
          } else {
            error = bearerErrorForNetworkError(sockerr);
          }
        } else {
          error = VNCBearerErrorFailed;
        }
      }

      if (readable)
        *pActivity |= VNCConnectionActivityReadReady;
      
      if (writeable)
        *pActivity |= VNCConnectionActivityWriteReady;
    }
  }

  return error;
}

VNCBearerError VNCConnectionTcpBase::beginConnectionAttempt()
{
  VNCBearerError error = VNCBearerErrorNone;

  logf("attempting connection to %s",
       AddressFormatter(pPeerAddressForCurrentConnectionAttempt).s());

  if (::connect(connection,
                pPeerAddressForCurrentConnectionAttempt->ai_addr,
                pPeerAddressForCurrentConnectionAttempt->ai_addrlen) == -1)
  {
    error = bearerErrorForNetworkError();
  }

  return error;
}

char *VNCConnectionTcpBase::getListeningInformation() const
{
  char *result = NULL;
  char *sPort = getCommandStringField("p");

  if (sPort)
  {
    unsigned short port = 0;

    if (validatePortNumber(sPort, &port) == VNCBearerErrorNone)
    {
      char buf[1024];
      ifconf ifconf;
      memset(&ifconf, 0, sizeof ifconf);

      port = htons(port);

      ifconf.ifc_len = sizeof(buf);
      ifconf.ifc_buf = buf;

      int error = ioctl(rendezvous, SIOCGIFCONF, &ifconf);

      if (error >= 0)
      {
        std::string addresses;

#ifdef __APPLE__
        for (char *cpIfreq = (char*)ifconf.ifc_req;
             cpIfreq < ((char*)ifconf.ifc_req) + ifconf.ifc_len;
             cpIfreq += _SIZEOF_ADDR_IFREQ(*((ifreq*)cpIfreq)))
        {
          ifreq *pIfreq = (ifreq*)cpIfreq;
#else
        for (ifreq *pIfreq = ifconf.ifc_req;
             pIfreq < ifconf.ifc_req + ifconf.ifc_len / sizeof(ifreq);
             pIfreq++)
        {
#endif
          AddressFormatter address;
          size_t length = 0;

          if (pIfreq->ifr_addr.sa_family == AF_INET)
          {
            reinterpret_cast<sockaddr_in &> (pIfreq->ifr_addr).sin_port =
              port;
            length = sizeof(sockaddr_in);
          }
          else if (pIfreq->ifr_addr.sa_family == AF_INET6)
          {
            reinterpret_cast<sockaddr_in6 &> (pIfreq->ifr_addr).sin6_port =
              port;
            length = sizeof(sockaddr_in6);
          }

          if (length)
            address.format(&pIfreq->ifr_addr, static_cast<socklen_t> (length));
          
          if (address)
          {
            if (!addresses.empty())
              addresses += " ";
            addresses += address.s();
          }
        }

        if (addresses.length() > 0)
          result = bearer.sdkStrdup(addresses.c_str());
      }
    }

    bearer.sdkFree(sPort);
  }

  return result;
}

#endif

