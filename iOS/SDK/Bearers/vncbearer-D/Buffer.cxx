/* Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
*/

#include <string.h>

#include "Buffer.h"

void Buffer::reserve(size_t n)
{
  if (used + n > data.size())
    data.resize(used + n);
}

void Buffer::append(const unsigned char *p, size_t n)
{
  reserve(n);
  memcpy(&data[used], p, n);
  used += n;
}

void Buffer::free(size_t n)
{
  used -= n;

  if (used)
    memmove(&data[0], &data[n], used);
}

