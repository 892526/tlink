#ifndef IOSSERVERSDK_VNCSERVER_H
#define IOSSERVERSDK_VNCSERVER_H

/**
 * \file VNCServer.h
 * 
 * \brief VNC Server
 *
 * Copyright RealVNC Ltd. 2011-2018. All rights reserved.
 */

// Bearer API (C).
#include <vncbearer.h>

// Command string.
#import <VNCCommandString.h>

// VNC Symbol Exporting.
#import <VNCExport.h>

// Bearer API (Objective-C).
#import <VNCServerBearer.h>

// Server delegate.
#import <VNCServerDelegate.h>

// Server error codes.
#import <VNCServerError.h>

// Server states.
#import <VNCServerState.h>

#import <VNCRPCaptureDelegate.h>

/**
 * \brief Authentication mode values.
 * 
 * Set these as the VNCServer::authenticationMode property.
 */
typedef enum {
	/**
	 * No authentication.
	 */
	VNC_AUTHMODE_NONE,
	
	/**
	 * Viewer can choose to request a username and/or a password from the server.
	 */
	VNC_AUTHMODE_REV,
	
	/**
	 * Viewer must send a password to the server.
	 */
	VNC_AUTHMODE_PASS,
	
	/**
	 * Viewer must send a username and a password to the server.
	 */
	VNC_AUTHMODE_USER_PASS
} VNCAuthMode;

/**
 * \brief Constant value that indicates invalid unicode value for XKeySym.
 * 
 * This is returned by VNCServer::convertXKeySymToUnicode: if
 * there is no unicode value corresponding to the XKeySym provided.
 */
VNCEXPORT
extern const uint32_t VNC_INVALID_XKEYSYM_UNICODE;

/**
 * \brief VNC Server class.
 * 
 * Note that all APIs MUST be called on the main thread.
 */
VNCEXPORT
@interface VNCServer : NSObject

/**
 * \brief Gets the SDK build version.
 * 
 * \return SDK build version.
 */
+(NSString*) buildVersion;

/**
 * \brief The server's current state.
 *
 * Initial state is VNCStateDisconnected.
 */
@property (nonatomic, readonly) VNCServerState state;

/**
 * \brief Whether the server is currently in any state
 * other than VNCStateDisconnected.
 */
@property (nonatomic, readonly) BOOL isActive;

/**
 * \brief The server's local address, or nil if there is none.
 */
@property (nonatomic, readonly) NSString* localEndpoint;

/**
 * \brief The server's remote address, or nil if there is none.
 */
@property (nonatomic, readonly) NSString* remoteEndpoint;

/**
 * \brief The server's listening info, or nil if there is none.
 */
@property (nonatomic, readonly) NSString* listeningInfo;

/**
 * \brief Whether encryption is used.
 * 
 * Default is NO.
 */
@property (nonatomic, assign) BOOL useEncryption;

/**
 * \brief VNC Server private key.
 * 
 * Use the VNCKeyGenerator class to generate this. This is required
 * for both authentication and encryption.
 * 
 * Default is nil.
 */
@property (nonatomic, copy) NSData * privateKey;

/**
 * \brief Authentication mode.
 * 
 * See above for a list and description of values that
 * can be provided for this property.
 * 
 * Default is VNC_AUTHMODE_NONE.
 */
@property (nonatomic, assign) VNCAuthMode authenticationMode;

/**
 * \brief  Whether to send the clipboard when a
 * connection is first established.
 * 
 * Default is NO.
 */
@property (nonatomic, assign) BOOL clipboardSendOnConnect;

/**
 * \brief Whether to send clipboard updates during an
 * active connection.
 * 
 * Default is YES.
 */
@property (nonatomic, assign) BOOL clipboardEnabled;

/**
 * \brief Initialise a VNC Server instance.
 *
 * \param delegate The server's delegate, which will be
 *                 retained by the server. Methods of
 *                 delegates are always called on the
 *                 main thread.
 * \return VNC Server.
 */
-(id) initWithDelegate:(id<VNCServerDelegate>)delegate;

-(id) initWithDelegate:(id<VNCServerDelegate>)delegate
	 withRPCapture:(id<VNCRPCaptureDelegate>)rpCapture;

/**
 * \brief Invalidate the VNC Server instance.
 * 
 * This will cancel any active connections/threads of
 * the server. This MUST be called before the server is
 * released to ensure that these threads are stopped
 * and the server releases its reference to the delegate.
 */
-(void) invalidate;

/**
 * \brief Add a bearer to the server.
 * 
 * \param bearer A pointer to the bearer.
 * \return VNCServerError indicating operation result.
 */
-(VNCServerError) addBearer:(id<VNCBearer>)bearer;

/**
 * \brief Connect using a command string object.
 *
 * This will start a new VNC server connection using the bearer and
 * parameters specified in the command string.
 *
 * \param commandString The command string object for the connection.
 * \return VNCServerError indicating operation result.
 */
-(VNCServerError) connect:(VNCCommandString*)commandString;

/**
 * \brief Connect using a command string.
 *
 * This will start a new VNC server connection using the bearer and
 * parameters specified in the command string.
 * 
 * \param commandString The command string for the connection.
 * \return VNCServerError indicating operation result.
 */
-(VNCServerError) connectWithCommandString:(NSString*)commandString;

/**
 * \brief Reset the current connection, if any.
 */
-(void) reset;

/**
 * \brief Add a license.
 * 
 * \param licenseString The license text.
 * \param serialData Out parameter returning the serial data of the license.
 *                   This will be set to point to the license's serial if the
 *                   license is added successfully (and won't be set otherwise);
 *                   pass nil for this parameter if the serial data isn't needed.
 * \return VNCServerError indicating operation result.
 */
-(VNCServerError) addLicense:(NSString*)licenseString withSerial:(out NSData**)serialData;

/**
 * \brief Add a licensing feature to the server.
 * 
 * Features must be added be added to the server before they
 * can be used in remote feature checks.
 * 
 * \param featureId The id of the feature to be added.
 * \param featureKey A 16 byte secret key for the feature.
 * \return VNCServerError indicating operation result.
 */
-(VNCServerError) addLicenseFeature:(NSUInteger)featureId withKey:(NSData*)featureKey;

/**
 * \brief Perform a local feature for a set of feature IDs.
 * 
 * \param featureIds An array of NSNumber (holding NSUInteger) feature IDs.
 * \param result Whether at least one of these features is licensed.
 * \return VNCServerError indicating operation result.
 */
-(VNCServerError) localFeatureCheck:(NSArray*)featureIds withResult:(BOOL*)result;

/**
 * \brief Mark the server as providing the specified license feature.
 * 
 * \param featureId The ID of the feature.
 * \return VNCServerError indicating operation result.
 */
-(VNCServerError) provideLicenseFeature:(NSUInteger)featureId;

/**
 * \brief Schedules a remote feature check to be made against VNC
 * viewers in all future sessions.
 * 
 * A VNC viewer must prove that it has the correct feature key for
 * at least one of the features specified here.
 * 
 * \param featureIds Features to be checked; viewer must have the feature key
 *                   for at least one of these features to pass the remote
 *                   feature check.
 * \param featureCheckId Out parameter returning an ID associated with the
 *                       remote feature check; this will be passed to the
 *                       relevant callbacks.
 * \return VNCServerError indicating operation result.
 * 
 * \see VNCServerDelegate::onRemoteFeatureCheckSucceeded:withFeature:
 * \see VNCServerDelegate::onRemoteFeatureCheckFailed:
 */
-(VNCServerError) scheduleRemoteFeatureCheck:(NSArray*)featureIds
			withFeatureCheckId:(NSUInteger*)featureCheckId;

/**
 * \brief Accept or reject authentication credentials (i.e. username
 * and/or password) that have been received from a VNC viewer.
 
 * The server must be in state VNCStateAuth (or this will return
 * VNCServerErrorState).
 * 
 * \param accept Whether to accept the authentication credentials.
 * \return VNCServerError indicating operation result.
 * 
 * \see VNCServerDelegate::onAuthUsername:withPassword:
 */
-(VNCServerError) acceptAuthentication:(BOOL)accept;

/**
 * \brief Accept or reject an established connection from a VNC viewer.
 * 
 * The server must be in state VNCStateAccepting (or this will return
 * VNCServerErrorState).
 * 
 * \param accept Whether to accept the connection.
 * \return VNCServerError indicating operation result.
 * 
 * \see VNCServerDelegate::onConnectedAtLocalEndpoint:toRemoteEndpoint:
 */
-(VNCServerError) acceptConnection:(BOOL)accept;

/**
 * \brief Accept or reject an RSA key from a VNC viewer.
 
 * The server must be in state VNCStateAcceptRemoteKey (or this will
 * return VNCServerErrorState).
 * 
 * \param accept Whether to accept the RSA key.
 * \return VNCServerError indicating operation result.
 * 
 * \see VNCServerDelegate::onRemoteKey:withSignature:
 */
-(VNCServerError) acceptRemoteKey:(BOOL)accept;

/**
 * \brief Provide username and/or password to VNC viewer during reverse
 * authentication.
 * 
 * The server must be in state VNCStateReverseAuth (or this will return
 * VNCServerErrorState).
 * 
 * \param username Username to be supplied to VNC viewer (pass nil
 *                 if the username wasn't requested).
 * \param password Password to be supplied to VNC viewer (pass nil
 *                 if the password wasn't requested).
 * \return VNCServerError indicating operation result.
 * 
 * \see VNCServerDelegate::onAuthNeedUsername:needPassword:
 */
-(VNCServerError) provideUsername:(NSString*)username withPassword:(NSString*)password;

/**
 * \brief Translate an X key symbol into a Unicode character.
 * 
 * This takes account of the type of the VNC Viewer to which the VNC
 * Server is connected (or uses a default conversion if not connected).
 * 
 * \param keysym An X11 key symbol.
 * \return The Unicode character corresponding to the X key symbol, or
 *         #VNC_INVALID_XKEYSYM_UNICODE if there is none.
 */
-(uint32_t) convertXKeySymToUnicode:(uint32_t)keysym;

@end

#endif
