#ifndef IOSSERVERSDK_VNCKEYGENERATOR_H
#define IOSSERVERSDK_VNCKEYGENERATOR_H

/**
 * \file VNCKeyGenerator.h
 * 
 * \brief VNC Private Key Generator
 *
 * Copyright RealVNC Ltd. 2012-2018. All rights reserved.
 */

// VNC Symbol Exporting.
#import <VNCExport.h>

/// VNCKeyGenerator forward declaration.
@class VNCKeyGenerator;

/**
 * \brief Delegate for keeping track of the key generation operation.
 * 
 * The methods here are always called on the main thread.
 */
@protocol VNCKeyGeneratorDelegate

/**
 * \brief Called by the key generator when progress has been made.
 * 
 * \param keyGenerator The key generator instance.
 * \param percent Percentage that has been completed.
 */
@required
-(void) keyGenerator:(VNCKeyGenerator*)keyGenerator atPercentage:(size_t)percent;

/**
 * \brief Called when key generation is complete. Once this
 * is called the private key will be available as VNCKeyGenerator::privateKey.
 * 
 * \param keyGenerator The key generator instance.
 * \param privateKey The generated private key.
 */
@required
-(void) keyGenerator:(VNCKeyGenerator*)keyGenerator keyComplete:(NSData*)privateKey;

@end

/**
 * \brief Represents the private key generation operation,
 * which is likely to take a significant amount of time
 * (i.e. tens or hundreds of milliseconds).
 *
 * All methods here are required to be called from
 * the main thread (all methods are non-blocking).
 */
VNCEXPORT
@interface VNCKeyGenerator : NSObject

/**
 * The key generator delegate, which will be called
 * (on the main thread) to indicate the progress of
 * the key generation operation. This delegate is NOT
 * retained by the key generator.
 */
@property (assign, nonatomic) id<VNCKeyGeneratorDelegate> delegate;

/**
 * Whether the key generation operation has completed.
 */
@property (nonatomic, readonly) BOOL isComplete;

/**
 * The private key that has been generated, or nil
 * if key generation is still ongoing.
 */
@property (nonatomic, readonly) NSData * privateKey;

/**
 * The number of modulus bits in the private key (e.g. 1024).
 */
@property (nonatomic, readonly) NSUInteger keySize;

/**
 * \brief Query whether a key size (in bits) can be generated
 * by this key generator.
 *
 * \param keySize The number of desired modulus bits in the key (e.g. 1024).
 * \return Whether the key size can be generated.
 */
+(BOOL) isValidKeySize:(size_t)keySize;

/**
 * \brief Initialise the key generator.
 *
 * Note that this will throw an exception if the key size is
 * invalid; use VNCKeyGenerator::isValidKeySize: to check if
 * a key size is valid before attempting key generation.
 * 
 * \param keySize The number of desired modulus bits in the key (e.g. 1024).
 * \return Key generator.
 */
-(id) initWithModBits:(size_t)keySize;

/**
 * \brief Instruct the key generator to start generating a key.
 * 
 * It will call the delegate to indicate its progress, and
 * to indicate when the operation has completed.
 *
 * Each key generator can only generate one key, so calling
 * this more than once has no effect. This also has no effect
 * once 'cancel' has been called.
 */
-(void) start;

/**
 * \brief Instruct the key generator to stop generating a key.
 
 * This can be useful to stop the key generator calling methods
 * on the delegate.
 *
 * Once this has been called, no further keys can be generated
 * from this key generator instance.
 */
-(void) cancel;

@end

#endif
