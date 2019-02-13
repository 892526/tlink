//
//  UIAlertController+VNCExtensions.h
//  iOS Server SDK
//
//  Copyright RealVNC Ltd. 2015-2018. All rights reserved.
//

#ifndef vncserversdkios_UIAlertController_VNCExtensions_h
#define vncserversdkios_UIAlertController_VNCExtensions_h

// Extend UIAlertController using a Category to store a code block for
// use during dismissal as a property using an associated reference.

// This is a workaround for not being able to access UIAlertActions
// during programmatic dialog dismissal on iOS 8.

// Note that if defined the completion block will be responsible for
// the actual dismissal of the dialog to allow for "re-entrant" dialogs
// - i.e. the creation of secondary dialogs within the block itself.

@interface UIAlertController (VNCExtensions)

@property (copy, nonatomic) void (^completion)(UIAlertAction*);

@end

#endif // vncserversdkios_UIAlertController_VNCExtensions_h
