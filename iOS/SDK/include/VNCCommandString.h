#ifndef IOSSERVERSDK_VNCCOMMANDSTRING_H
#define IOSSERVERSDK_VNCCOMMANDSTRING_H

/**
 * \file VNCCommandString.h
 * 
 * \brief VNC Automotive Command String
 *
 * Copyright (C) 2014-2018 VNC Automotive Ltd.  All Rights Reserved.
 */

// VNC Automotive Symbol Exporting.
#import <VNCExport.h>

/**
 * \brief VNC Automotive Command String class.
 * 
 * Parses a command string and provides access to its parameters.
 */
VNCEXPORT
@interface VNCCommandString : NSObject < NSCopying >

/**
 * \brief Create a VNC Automotive Command String instance by
 *        parsing a text form command.
 *
 * \param text A VNC Automotive Command String in text form.
 * \return VNC Automotive Command String. Returns nil if command
 *         string is invalid.
 */
+(VNCCommandString*) commandStringWithText:(NSString*)text;

/**
 * \brief Initialise a VNC Automotive Command String instance by
 *        parsing a text form command.
 *
 * \param text A VNC Automotive Command String in text form.
 * \return VNC Automotive Command String. Returns nil if command
 *         string is invalid.
 */
-(id) initWithText:(NSString*)text;

/**
 * \brief Check if the named parameter exists in
 *        the command string.
 *
 * \param parameterName Name of the parameter.
 * \return Whether the parameter exists.
 */
-(BOOL) hasParameter:(NSString*)parameterName;

/**
 * \brief Get the command string parameter value.
 *
 * \param parameterName Name of the parameter.
 * \return The parameter value.
 */
-(NSString*) getParameter:(NSString*)parameterName;

@end

#endif
