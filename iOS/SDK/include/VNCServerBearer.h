#ifndef IOSSERVERSDK_VNCSERVERBEARER_H
#define IOSSERVERSDK_VNCSERVERBEARER_H

/**
 * \file VNCServerBearer.h
 * 
 * \brief VNC Automotive Bearer API.
 *
 * Copyright (C) 2011-2018 VNC Automotive Ltd.  All Rights Reserved.
 */

// Bearer Connection API.
#import <VNCBearerConnection.h>

// Bearer Connection Context API.
#import <VNCBearerConnectionContext.h>

/**
 * \brief Objective-C Bearer API.
 * 
 * All bearers used with the iOS Server must implement this API,
 * and can be passed to the server using VNCServer::addBearer:.
 * 
 * VNCBearerWrapper bridges this API with the standard C bearer API.
 */
@protocol VNCBearer < NSObject >

/**
 * \brief Get the short name of the bearer.
 * 
 * \return A pointer to a new string containing the short name.
 */
-(NSString *) name;

/**
 * \brief Get the full name of the bearer.
 * 
 * \return A pointer to a new string containing the full name.
 */
-(NSString *) fullName;

/**
 * \brief Get a description of the bearer.
 * 
 * \return A pointer to a new string containing the description.
 */
-(NSString *) description;

/**
 * \brief Get the version of the bearer.
 * 
 * \return A pointer to a new string containing the version.
 */
-(NSString *) version;

/**
 * \brief Create a new bearer connection.
 * 
 * Create a connection based on a connection context (which should be called
 * to obtain any required information, such as command string fields).
 * 
 * \param context Context for the new connection, which is retained by the connection.
 * \return A pointer to the new connection (MUST NOT be nil). (Will be
 *         retained by this method, so callers must release/autorelease.)
 */
-(id<VNCBearerConnection>) newConnectionWithContext:(id<VNCBearerConnectionContext>)context;

@end

#endif
