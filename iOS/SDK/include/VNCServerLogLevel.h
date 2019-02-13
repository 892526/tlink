#ifndef IOSSERVERSDK_VNCSERVERLOGLEVEL_H
#define IOSSERVERSDK_VNCSERVERLOGLEVEL_H

/**
 * \file VNCServerLogLevel.h
 * 
 * \brief VNC Automotive Server Log Levels
 *
 * Copyright (C) 2013-2018 VNC Automotive Ltd.  All Rights Reserved.
 */

/**
 * \enum VNCServerLogLevel
 * \brief VNC Automotive Server Log Levels.
 * 
 * Currently the server SDK will only pass log levels
 * to VNCServerDelegate::onServerLog:withLevel: that
 * are one of these log levels; the mapped values
 * are spaced to allow adding intermediate log
 * levels in future.
 * 
 * \see VNCServerDelegate::onServerLog:withLevel:
 */
typedef enum {
	/**
	 * \brief CRITICAL log level.
	 * 
	 * Highest/most important log level.
	 * Indicates critical failures.
	 */
	VNCSERVER_LOG_CRITICAL = 0,
	
	/**
	 * \brief WARNING log level.
	 * 
	 * Indicates significant problems.
	 */
	VNCSERVER_LOG_WARNING = 1000,
	
	/**
	 * \brief NOTICE log level.
	 * 
	 * Indicates important information.
	 */
	VNCSERVER_LOG_NOTICE = 2000,
	
	/**
	 * \brief INFO log level.
	 * 
	 * Indicates potentially interesting information.
	 */
	VNCSERVER_LOG_INFO = 3000,
	
	/**
	 * \brief DEBUG log level.
	 * 
	 * Indicates logging for debugging purposes.
	 */
	VNCSERVER_LOG_DEBUG = 4000
} VNCServerLogLevel;

#endif
