#ifndef IOSSERVERSDK_VNCBEARERCONNECTION_H
#define IOSSERVERSDK_VNCBEARERCONNECTION_H

/**
 * \file VNCBearerConnection.h
 * 
 * \brief VNC Automotive Bearer Connection API.
 *
 * Copyright (C) 2011-2018 VNC Automotive Ltd.  All Rights Reserved.
 */

// Bearer API (C).
#include <vncbearer.h>

/**
 * An invalid event handle value on iOS.
 */
static const VNCConnectionEventHandle VNC_INVALID_EVENT_HANDLE = -1;

/**
 * \brief Objective-C Bearer Connection API.
 * 
 * This is the Objective-C API for bearer connections used by the iOS server SDK.
 * Instances of this class are obtained from VNCBearer::newConnectionWithContext:.
 */
@protocol VNCBearerConnection < NSObject >

/**
 * \brief Get the connection's listening information.
 * 
 * \return The listening information for this connection, or nil if there is nothing relevant.
 */
-(NSString *) listeningInfo;

/**
 * \brief Get the connection's local endpoint.
 * 
 * \return The local endpoint (i.e. 'our' endpoint) for this connection, or nil if there is nothing relevant.
 */
-(NSString *) localEndpoint;

/**
 * \brief Get the connection's remote endpoint.
 * 
 * \return The remote endpoint (i.e. 'their' endpoint) for this connection, or nil if there is nothing relevant.
 */
-(NSString *) remoteEndpoint;

/**
 * \brief Get the connection's latest error.
 * 
 * \return The latest error, if any (VNCBearerErrorNone for no error).
 */
-(VNCBearerError) error;

/**
 * \brief Get the connection's event handle.
 * 
 * \return The connection's event handle.
 */
-(VNCConnectionEventHandle) eventHandle;

/**
 * \brief Check for connection activity.
 * 
 * Checks whether any activity can be performed on the connection,
 * and processes any events on the connection.
 * 
 * \return Whether the connection is readable and/or writeable.
 */
-(VNCConnectionActivity) activity;

/**
 * \brief Read from the connection.
 * 
 * Returns 0 if no data is available or an error occurs (check
 * VNCBearerConnection::error).
 * 
 * \param buffer Data buffer to read into.
 * \param length Data buffer length.
 * \return The amount of data read.
 */
-(NSUInteger) read:(uint8_t *)buffer maxLength:(NSUInteger)length;

/**
 * \brief Write to the connection.
 * 
 * Returns 0 if no space is available or an error occurs (check
 * VNCBearerConnection::error).
 * 
 * \param buffer Data buffer to write from.
 * \param length Data buffer length.
 * \return The amount of data written.
 */
-(NSUInteger) write:(const uint8_t *)buffer maxLength:(NSUInteger)length;

@end

#endif
