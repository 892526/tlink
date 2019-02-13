//
//  Use this file to import your target's public headers that you would like to expose to Swift.
//

#import <Foundation/Foundation.h>

// for AES Encrypt/Decrypt
//#import <CommonCrypto/CommonCrypto.h>
#import <CommonCrypto/CommonCryptor.h>
//#include <CommonCrypto/CommonCrypto.h>

// ---- VNC iOS Server SDK
// VNC server.
#import <VNCServer.h>

// ---- Bearers
// Bearer wrapper.
#import <vncbearer-bearerwrapper/VNCBearerWrapper.h>
// All bearers are static libraries on iOS.
#define VNC_BEARER_STATIC
// C bearer API.
#include <vncbearer.h>
// TCP Connect Bearer.
#include <vncbearer-C/vncbearer-C.h>
// TCP Listen Bearer.
#include <vncbearer-L/vncbearer-L.h>
// iAP2 Bearer.
#include <vncbearer-usbios/VNCUSBBearer.h>
