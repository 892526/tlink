#ifndef IOSSERVERSDK_VNCSERVERSTATE_H
#define IOSSERVERSDK_VNCSERVERSTATE_H

/**
 * \file VNCServerState.h
 * 
 * \brief VNC Automotive Server States
 *
 * Copyright (C) 2013-2018 VNC Automotive Ltd.  All Rights Reserved.
 */

// VNC Automotive Symbol Exporting.
#import <VNCExport.h>

/**
 * \enum VNCServerState
 * \brief States provided to the server delegate.
 *
 * The sequence of notifications varies according to the connection type.
 */
typedef enum
{
	/** \brief Server is idle */
	VNCStateDisconnected = 0,
	
	/** \brief Server is setting the parameters for the RFB session */
	VNCStateSetup = 1,

	/** \brief Server is waiting for an encryption key to be set */
	VNCStateAwaitingKey = 2,

	/** \brief Server is generating an encryption key */
	VNCStateGeneratingKey = 3,

	/** \brief Server is listening for an incoming connection */
	VNCStateListening = 4,

	/** \brief Server is initiating an outbound connection */
	VNCStateConnecting = 5,

	/** \brief Server is performing a data relay handshake */
	VNCStateConnectingRelay = 6,
	
	/** \brief Server is waiting for a connection to be accepted */
	VNCStateAccepting = 10,
	
	/** \brief Server is processing the RFB handshaking phase */
	VNCStateHandshaking = 11,

	/** \brief Server is waiting for a remote key to be accepted */
	VNCStateAcceptRemoteKey = 12,

	/** \brief Server is waiting for viewer credentials to be authenticated
	 * by the application.
	 */
	VNCStateAuth = 13,

	/** \brief Server is waiting for a reverse authentication password from
	 * the application.
	 */
	VNCStateReverseAuth = 14,

	/** \brief Server is connected to a viewer */
	VNCStateRunning = 15,
	
	/** \brief Server is disconnecting from a viewer */
	VNCStateDisconnecting = 16,

	/** \brief Server is in invalid state */
	VNCStateInvalid = 255
} VNCServerState;

/**
 * \brief Utility function to produce a human-readable string
 *        from a server state value. Note that strings produced
 *        are not localised.
 *
 * \param state The state enum value.
 * \return A human-readable string representing the state.
 */
VNCEXPORT
NSString* VNCConvertServerStateToString(VNCServerState state);

#endif
