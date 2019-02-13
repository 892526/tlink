#ifndef IOSSERVERSDK_VNCCOMMANDSTRING_H
#define IOSSERVERSDK_VNCCOMMANDSTRING_H

/**
 * \file VNCCommandString.h
 * 
 * \brief VNC Command String
 *
 * Copyright RealVNC Ltd. 2014-2018. All rights reserved.
 */

// VNC Symbol Exporting.
#import <VNCExport.h>

/**
 * \brief VNC Command String class.
 * 
 * Parses a command string and provides access to its parameters.
 */
VNCEXPORT
@interface VNCCommandString : NSObject < NSCopying >

/**
 * \brief Create a VNC Command String instance by
 *        parsing a text form command.
 *
 * \param text A VNC Command String in text form.
 * \return VNC Command String. Returns nil if command
 *         string is invalid.
 */
+(VNCCommandString*) commandStringWithText:(NSString*)text;

/**
 * \brief Initialise a VNC Command String instance by
 *        parsing a text form command.
 *
 * \param text A VNC Command String in text form.
 * \return VNC Command String. Returns nil if command
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
