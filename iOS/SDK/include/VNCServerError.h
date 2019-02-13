#ifndef IOSSERVERSDK_VNCSERVERERROR_H
#define IOSSERVERSDK_VNCSERVERERROR_H

/**
 * \file VNCServerError.h
 * 
 * \brief VNC Automotive Server Error Codes
 *
 * Copyright (C) 2013-2018 VNC Automotive Ltd.  All Rights Reserved.
 */

// VNC Automotive Symbol Exporting.
#import <VNCExport.h>

/**
 * \enum VNCServerError
 * \brief Error codes that may be notified via the server delegate.
 * 
 * Any error indicates that the VNC Automotive session has ended.
 */
typedef enum
{
	/**
	 * No error.
	 */
	VNCServerErrorNone = 0,

	/**
	 * Insufficient system resources.
	 * <p>
	 * When the device has insufficient resources to satisfy
	 * a request then this error will be reported. For instance
	 * if the device does not have enough free memory available
	 * then an API may fail with this error code.
	 */
	VNCServerErrorResources = 1,

	/**
	 * An invalid API call was made.
	 * <p>
	 * Some API calls are only valid when the server is in a
	 * particular state. For instance it is illegal to ask the server
	 * to connect while it is already connected. This error will be
	 * reported in such circumstances.
	 */
	VNCServerErrorState = 2,

	/**
	 * Insufficient device permissions.
	 */
	VNCServerErrorPermissionDenied = 3,

	/* Network Errors */

	/**
	 * General network error.
	 */
	VNCServerErrorNetworkUnreachable = 20,

	/**
	 * IP address could not be contacted.
	 */
	VNCServerErrorHostUnreachable = 21,

	/**
	 * Port could not be contacted.
	 */
	VNCServerErrorConnectionRefused = 22,

	/**
	 * Domain name could not be resolved.
	 */
	VNCServerErrorDNSFailure = 23,

	/**
	 * Address/Port is in use.
	 * <p>
	 * This error occurs when the server is told to listen on a port
	 * which is already being used by another application.
	 */
	VNCServerErrorAddressInUse = 24,

	/**
	 * Invalid port number.
	 * <p>
	 * Valid TCP port numbers range from 1 to 65535 inclusive.
	 */
	VNCServerErrorBadPort = 25,

	/**
	 * No network connection,  The connection to the server was lost.
	 */
	VNCServerErrorDisconnected = 26,

	/**
	 * A general network time-out occured.
	 */
	VNCServerErrorConnectionTimedOut = 27,

	/**
	 * The bearer failed to establish a connection due to an authentication
	 * failure.
	 *
	 * Note that this error indicates a transport-level failure, rather than an
	 * application-level failure.  That is, the failure originates from within
	 * a pluggable bearer implementation, rather than with the Viewer SDK.
	 */
	VNCServerErrorBearerAuthenticationFailed = 28,

	/* Other Bearer Errors */

	/**
	 * USB Not Connected.
	 * <p>
	 * There is nothing connected via USB or the device
	 * is unable to communicate via USB.
	 */
	VNCServerErrorUSBNotConnected = 30,

	/**
	 * Underlying Library Not Found.
	 * <p>
	 * Failed to load a library for some particular functionality,
	 * for example OEM software for driving a particular type of
	 * communications.
	 */
	VNCServerErrorUnderlyingLibraryNotFound = 31,

	/**
	 * A static configuration required by the bearer has not been provided.
	 */
	VNCServerErrorBearerConfigurationNotProvided = 32,

	/**
	 * A static configuration provided for the bearer is invalid.
	 */
	VNCServerErrorBearerConfigurationInvalid = 33,

	/**
	 * A bearer could not be loaded.
	 */
	VNCServerErrorBearerLoadFailed = 34,

	/* VNC Automotive Errors */

	/**
	 * Protocol incompatible with that of the Viewer.
	 * <p>
	 * This error can occur if the Server is attempting to
	 * connect to a non-VNC Automotive Viewer or to something
	 * other than a VNC Automotive Viewer (e.g. a HTTP server).
	 * party, or to something other than a VNC Automotive viewer (eg a HTTP
	 * server).
	 */
	VNCServerErrorProtocolMismatch = 40,

	/**
	 * User rejected authentication credentials.
	 */
	VNCServerErrorLoginRejected = 41,

	/**
	 * License incompatible with that of VNC Automotive Viewer.
	 */
	VNCServerErrorNotLicensedForViewer = 42,

	/**
	 * VNC Automotive Viewer terminated the remote control session.
	 */
	VNCServerErrorConnectionClosed = 43,

	/**
	 * Invalid command string.
	 */
	VNCServerErrorInvalidCommandString = 44,

	/**
	 * Invalid authentication type.
	 * <p>
	 * The connection is encrypted but VNC Automotive Server did not
	 * provide RSA keys. Alternatively, VNC Automotive Viewer specified an
	 * unsupported authentication type.
	 */
	VNCServerErrorUnsupportedAuth = 45,

	/**
	 * The RSA key is too large.
	 */
	VNCServerErrorKeyTooBig = 46,

	/**
	 * RFB protocol or AES checksum is corrupt, or VNC Automotive Viewer
	 * did not have a matching private key.
	 */
	VNCServerErrorBadCrypt = 47,

	/**
	 * VNC Automotive Viewer specified an unsupported encoding.
	 */
	VNCServerErrorNoEncodings = 48,

	/**
	 * VNC Automotive Viewer specified an unsupported pixel color depth.
	 */
	VNCServerErrorBadPixelformat = 49,

	/**
	 * Transport mechanism specified in command string missing or corrupt.
	 */
	VNCServerErrorBearerNotFound = 50,

	/**
	 * VNC Automotive Viewer signature specified in command string not the
	 * same as that of the actual VNC Automotive Viewer that connects.
	 */
	VNCServerErrorSignatureRejected = 51,

	/**
	 * The requested operation could not be completed due to
	 * insufficient buffer space.
	 */
	VNCServerErrorInsufficientBufferSpace = 52,

	/**
	 * The requested operation could not be completed due to
	 * the provided license not being valid.
	 */
	VNCServerErrorLicenseNotValid = 53,

	/**
	 * The requested operation could not be completed due to
	 * the feature not being licensed.
	 */
	VNCServerErrorFeatureNotLicensed = 54,

	/* API Errors */

	/**
	 * An invalid parameter was passed to an API call.
	 * <p>
	 * This can occur when registering a custom extension with an
	 * invalid name, or sending an extension message with an invalid
	 * length.
	 */
	VNCServerErrorInvalidParameter = 60,

	/**
	 * The RSA key generation algorithm failed.
	 */
	VNCServerErrorKeyGeneration = 63,

	/**
	 * The underlying VNC Automotive Server service could not be started.
	 */
	VNCServerErrorUnableToStartService = 64,

	/**
	 * A custom extension with the same name has already been registered.
	 */
	VNCServerErrorAlreadyExists = 65,

	/**
	 * The maximum number of custom extensions (8) have already been
	 * registered.
	 */
	VNCServerErrorTooManyExtensions = 66,

	/**
	 * The server was reset by an external action
	 */
	VNCServerErrorReset = 67,

	/* Data Relay Errors */

	/**
	 * VNC Automotive Data Relay received an invalid message from the server.
	 */
	VNCServerErrorDataRelayProtocolError = 80,

	/**
	 * Either the command string contained an invalid VNC Automotive Data Relay
	 * session ID, or the communication channel to which it refers is
	 * no longer reserved.
	 */
	VNCServerErrorUnknownDataRelaySessionId = 81,

	/**
	 * VNC Automotive Data Relay could not authenticate the server.
	 */
	VNCServerErrorBadChallenge = 82,

	/**
	 * VNC Automotive Viewer did not connect to the other end of the
	 * reserved VNC Automotive Data Relay communication channel in time.
	 */
	VNCServerErrorDataRelayChannelTimeout = 83,

	/* Errors Detected in App */

	/**
	 * Device user rejected prompt authorizing remote control.
	 */
	VNCServerErrorUserRefusedConnection = 100,

	/**
	 * HTTP or HTTPS request to command string web service failed.
	 */
	VNCServerErrorCommandFetchFailed = 101,

	/**
	 * General error.
	 */
	VNCServerErrorFailed = 102,

	/**
	 * Feature not implement
	 */
	VNCServerErrorNotImplemented = 103,

	/**
	 * A command string for a different remote control session is
	 * received before the device user accepts the prompt authorizing
	 * the original session.
	 */
	VNCServerErrorCommandSuperseded = 106,

	/**
	 * The application environment is unsupported.
	 */
	VNCServerErrorEnvironment = 107,

	/**
	 * Screen capture is not implemented in this platform.
	 */
	VNCServerErrorCaptureFrameBufferNotImplemented = 120

} VNCServerError;

/**
 * \brief Utility function to produce a human-readable string
 *        from a server error value. Note that strings produced
 *        are not localised.
 *
 * \param error The error enum value.
 * \return A human-readable string representing the error.
 */
VNCEXPORT
NSString* VNCConvertServerErrorToString(VNCServerError error);

#endif
