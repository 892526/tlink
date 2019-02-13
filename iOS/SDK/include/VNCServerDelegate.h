#ifndef IOSSERVERSDK_VNCSERVERDELEGATE_H
#define IOSSERVERSDK_VNCSERVERDELEGATE_H

/**
 * \file VNCServerDelegate.h
 * 
 * \brief VNC Automotive Server Delegate
 *
 * Copyright (C) 2013-2018 VNC Automotive Ltd.  All Rights Reserved.
 */

// Server error codes.
#import <VNCServerError.h>

// Server log levels.
#import <VNCServerLogLevel.h>

/**
 * \brief VNC Automotive Server Delegate
 * 
 * Protocol to be implemented by classes which can
 * handle VNC Automotive Server events. This should be passed
 * to VNCServer::initWithDelegate: (which will retain
 * this instance, hence it must implement NSObject).
 */
@protocol VNCServerDelegate < NSObject >

/**
 * \brief Handle a (normal) authentication event, by either accepting
 * or rejecting the authentication information provided by the viewer.
 * 
 * \param username The username provided by the viewer.
 * \param password The password provided by the viewer.
 *
 * \see VNCServer::acceptAuthentication:
 */
@required
-(void) onAuthUsername:(NSString*)username withPassword:(NSString*)password;

/**
 * \brief Handle a (reverse) authentication request, by providing
 * the credentials to the viewer that it has requested.
 * 
 * \param needUsername Whether the viewer requests a username.
 * \param needPassword Whether the viewer requests a password.
 *
 * \see VNCServer::provideUsername:withPassword:
 */
@required
-(void) onAuthNeedUsername:(BOOL)needUsername needPassword:(BOOL)needPassword;

/**
 * \brief Handle the server moving to a connecting state.
 */
@required
-(void) onConnecting;

/**
 * \brief Handle the server moving to a connected state.
 * 
 * No data will be transferred until VNCServer::acceptConnection:
 * is called, to either accept or reject this connection.
 * 
 * \param localEndpoint A string describing the local endpoint,
 *                      or nil if there is no relevant information.
 * \param remoteEndpoint A string describing the remote endpoint,
 *                      or nil if there is no relevant information.
 *
 * \see VNCServer::acceptConnection:
 */
@required
-(void) onConnectedAtLocalEndpoint:(NSString*)localEndpoint
	toRemoteEndpoint:(NSString*)remoteEndpoint;

/**
 * \brief Handle the server moving to a disconnected state.
 *
 * When a connection terminates, the server SDK will call exactly
 * one of VNCServerDelegate::onDisconnected (to nofify a graceful
 * disconnection) and VNCServer::onServerError: (to notify a
 * non-graceful disconnection.)
 */
@required
-(void) onDisconnected;

/**
 * \brief Handle the server moving to a listening state.
 * 
 * \param listeningInfo A string describing the local endpoint,
 *                      or nil if there is no relevant information.
 */
@required
-(void) onListeningWithInfo:(NSString*)listeningInfo;

/**
 * \brief Handle a remote feature check success.
 * 
 * \param featureCheckId The id of the remote feature check.
 * \param featureId The id of the feature that the viewer is licensed for.
 * 
 * \see VNCServer::scheduleRemoteFeatureCheck:withFeatureCheckId:
 */
@required
-(void) onRemoteFeatureCheckSucceeded:(NSUInteger)featureCheckId
	withFeature:(NSUInteger)featureId;

/**
 * \brief Handle a remote feature check failure.
 * 
 * Unlike other delegate methods, this delegate is required
 * to return a BOOL value indicating whether the remote
 * feature check failure should be considered critical (i.e.
 * trigger a disconnection).
 * 
 * \param featureCheckId The id of the remote feature check.
 * \return Whether to refuse the connection as a result of this failure.
 * 
 * \see VNCServer::scheduleRemoteFeatureCheck:withFeatureCheckId:
 */
@required
-(BOOL) onRemoteFeatureCheckFailed:(NSUInteger)featureCheckId;

/**
 * \brief Handle a remote key event, by either accepting or rejecting the
 * encryption key.
 * 
 * \param remoteKeyData Key data provided by the viewer.
 * \param remoteKeySignature Key signature provided by the viewer.
 *
 * \see VNCServer::acceptRemoteKey:
 */
@required
-(void) onRemoteKey:(NSData*)remoteKeyData withSignature:(NSData*)remoteKeySignature;

/**
 * \brief Handle the server moving to a running state.
 * 
 * The 'running' state is where the server and viewer are connected,
 * the connection has been accepted, any remote keys have been accepted,
 * authentication has completed successfully and no critical remote
 * feature checks have failed.
 *
 * At this point the server SDK will begin responding to events from the
 * viewer, as well as sending the screen framebuffer.
 */
@required
-(void) onRunning;

/**
 * \brief Handle a VNC Automotive Server error.
 * 
 * When a connection terminates, the server SDK will call exactly
 * one of VNCServerDelegate::onDisconnected (to nofify a graceful
 * disconnection) and VNCServer::onServerError: (to notify a
 * non-graceful disconnection.)
 * 
 * \param error The VNC Automotive Server error.
 * 
 * \see VNCServerError
 */
@required
-(void) onServerError:(VNCServerError)error;

/**
 * \brief Handle a VNC Automotive Server log message.
 * 
 * Note that the server SDK will not log other than through this
 * method, so it's important that implementations do not discard
 * log messages given to this method in a situation where debugging
 * might be required.
 * 
 * A simple implementation might be:
 * 
 * \code
 * -(void) onServerLog:(NSString*)message withLevel:(VNCServerLogLevel)level {
 *     NSLog(@"VNC Automotive iOS Server Log (level %d): %@.", level, message);
 * }
 * \endcode
 * 
 * \param message The log message.
 * \param level The log level.
 * 
 * \see VNCServerLogLevel
 */
@required
-(void) onServerLog:(NSString*)message withLevel:(VNCServerLogLevel)level;

@end

#endif
