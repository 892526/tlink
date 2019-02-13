/* Copyright (C) 2016-2018 RealVNC Ltd.  All Rights Reserved.
*/

#include "VNCBearerSdkString.h"
#include "VNCBearerImpl.h"

#include <vnccommon/StringUtils.h>

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

vnccommon::Optional<std::string> VNCBearerSdkString::get() const
{
  if (mStr)
  {
    return vnccommon::Optional<std::string>(mStr);
  }
  return vnccommon::Optional<std::string>();
}

vnccommon::Optional<unsigned long> VNCBearerSdkString::toUnsignedLong() const
{
  if (mStr)
  {
    return vnccommon::StringUtils::toUnsignedLong(mStr, 10);
  }
  return vnccommon::Optional<unsigned long>();
}
