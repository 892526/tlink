/* Copyright (C) 2016-2018 VNC Automotive Ltd.  All Rights Reserved.
*/

#include "VNCBearerSdkString.h"
#include "VNCBearerImpl.h"

#include <vacommon/StringUtils.h>

VNCBearerSdkString::VNCBearerSdkString(const VNCBearerImpl& bearer,
                                       char* const str)
  : mBearer(bearer),
    mStr(str)
{
}

VNCBearerSdkString::~VNCBearerSdkString()
{
  // Free memory
  if (mStr)
  {
    mBearer.sdkFree(mStr);
  }
}

vacommon::Optional<std::string> VNCBearerSdkString::get() const
{
  if (mStr)
  {
    return vacommon::Optional<std::string>(mStr);
  }
  return vacommon::Optional<std::string>();
}

vacommon::Optional<unsigned long> VNCBearerSdkString::toUnsignedLong() const
{
  if (mStr)
  {
    return vacommon::StringUtils::toUnsignedLong(mStr, 10);
  }
  return vacommon::Optional<unsigned long>();
}
