//
//  VNCBearerWrapper.h
//
//  Copyright RealVNC Ltd. 2011-2018. All rights reserved.
//

/**
 * Wraps a standard bearer implementing the C API (see vncbearer.h)
 * so it can be used with the Objective-C Bearer API (see VNCServerBearer.h).
 */
@interface VNCBearerWrapper : NSObject < VNCBearer >

/**
 * Initialises the bearer wrapper using a C-API bearer.
 * \param name The bearer's name.
 * \param initializer The bearer's initialize function.
 * \return The bearer wrapper.
 */
-(id) initWithBearerName:(NSString*)name withInitializer:(VNCBearerInitializeType*)initializer;

/**
 * Get a property value from the bearer.
 * \param property The desired property to be returned.
 * \return An Objective-C string containing the property value.
 */
-(NSString *) getProperty:(VNCBearerProperty)property;

@end
