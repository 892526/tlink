#ifndef IOSSERVERSDK_VNCBEARERCONNECTIONCONTEXT_H
#define IOSSERVERSDK_VNCBEARERCONNECTIONCONTEXT_H

/**
 * \file VNCBearerConnectionContext.h
 * 
 * \brief VNC Bearer Connection Context API.
 *
 * Copyright RealVNC Ltd. 2013-2018. All rights reserved.
 */

// Bearer API (C).
#include <vncbearer.h>

/**
 * \brief Objective-C Bearer Connection Context API.
 * 
 * Objects implementing this protocol should be passed to
 * VNCBearer::newConnectionWithContext:, and provides information
 * to the bearer connection, as well as being notified about
 * bearer connection events.
 */
@protocol VNCBearerConnectionContext < NSObject >

/**
 * \brief Log a message for the bearer connection.
 * 
 * \param text Message to log.
 */
-(void) log:(NSString*)text;

/**
 * \brief Get a command string field.
 * 
 * \param fieldName The name of the command string field.
 * \return The field value, or nil if none exists.
 */
-(NSString*) getCommandStringField:(NSString*)fieldName;

/**
 * \brief Get the bearer configuration.
 * 
 * \return A string representing the bearer's configuration,
 *         or nil if none exists.
 */
-(NSString*) getBearerConfiguration;

/**
 * \brief Notify a bearer connection status change.
 * 
 * \param status The new connection status.
 */
-(void) connectionStatusChange:(VNCConnectionStatus)status;

/**
 * \brief Check if a set of features are available.
 * 
 * \param features An array of NSNumbers containing the feature values.
 * \return Whether the features are licensed.
 */
-(BOOL) localFeatureCheck:(NSArray*)features;

@end

#endif
