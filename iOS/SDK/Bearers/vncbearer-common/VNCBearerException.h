/* Copyright (C) 2016-2018 VNC Automotive Ltd.  All Rights Reserved.
*/

#ifndef VNCBEAREREXCEPTION_H_268617646522064612218434590292317083854
#define VNCBEAREREXCEPTION_H_268617646522064612218434590292317083854

#include <string>

#include <vncbearer.h>

class VNCBearerException : public std::exception
{
public:
  VNCBearerException(const std::string& msg, VNCBearerError error);
  virtual ~VNCBearerException() throw();

public:
  virtual const char* what() const throw();

  VNCBearerError getError() const;

private:
  const std::string mMsg;
  const VNCBearerError mError;
};

#endif // VNCBEAREREXCEPTION_H_268617646522064612218434590292317083854
