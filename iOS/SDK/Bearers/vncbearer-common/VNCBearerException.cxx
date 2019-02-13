/* Copyright (C) 2016-2018 VNC Automotive Ltd.  All Rights Reserved.
*/

#include "VNCBearerException.h"

VNCBearerException::VNCBearerException(const std::string& msg,
                                       const VNCBearerError error)
  : mMsg(std::string("VNCBearerException: " + msg)),
    mError(error)
{
}

VNCBearerException::~VNCBearerException() throw()
{
}

const char* VNCBearerException::what() const throw()
{
    return mMsg.c_str();
}

VNCBearerError VNCBearerException::getError() const
{
  return mError;
}
