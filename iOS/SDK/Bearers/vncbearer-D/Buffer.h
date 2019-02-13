/* Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
*/

#ifndef __BUFFER_H__
#define __BUFFER_H__

#include <vector>

class Buffer
{
public:
  inline Buffer();

  inline unsigned char *getData();
  inline const unsigned char *getData() const;
  inline size_t getCapacity() const;
  inline size_t getUsed() const;
  inline size_t getFree() const;

  void reserve(size_t n);
  void append(const unsigned char *p, size_t n);
  inline void use(size_t n);
  void free(size_t n);
  inline void clear();

private:
  typedef std::vector<unsigned char> Data;

  Data data;
  size_t used;
};

inline Buffer::Buffer()
  : data(0),
    used(0)
{
}

inline const unsigned char *Buffer::getData() const
{
  return &data[0];
}

inline unsigned char *Buffer::getData()
{
  return &data[0];
}

inline size_t Buffer::getCapacity() const
{
  return data.size();
}

inline size_t Buffer::getUsed() const
{
  return used;
}

inline size_t Buffer::getFree() const
{
  return data.size() - used;
}

inline void Buffer::use(size_t n)
{
  used += n;
}

inline void Buffer::clear()
{
  data.resize(0);
}

#endif /* !defined(__BUFFER_H__) */
