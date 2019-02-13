/* Copyright (C) 2016-2018 VNC Automotive Ltd.  All Rights Reserved.
*/

#ifndef VNCBEARERSDKSTRING_H_35318748190379279264132193373607729358
#define VNCBEARERSDKSTRING_H_35318748190379279264132193373607729358

#include <vacommon/Optional.h>

struct VNCBearerImpl;

// Small RAII wrapper for SDK strings
class VNCBearerSdkString
{
public:
  VNCBearerSdkString(const VNCBearerImpl& bearer, char* str = NULL);
  ~VNCBearerSdkString();

  vacommon::Optional<std::string> get() const;

  // Utility method to transform this string to an unsigned long
  vacommon::Optional<unsigned long> toUnsignedLong() const;

private:
  // Copy operations are private to prevent copying
  VNCBearerSdkString(const VNCBearerSdkString&);            // no implementation
  VNCBearerSdkString& operator=(const VNCBearerSdkString&); // no implementation

private:
  const VNCBearerImpl& mBearer;
  char* const mStr;
};

#endif // VNCBEARERSDKSTRING_H_35318748190379279264132193373607729358
