#ifndef IOSSERVERSDK_VNCEXPORT_H
#define IOSSERVERSDK_VNCEXPORT_H

/**
 * \file VNCExport.h
 * 
 * \brief VNC Symbol Exporting
 *
 * Copyright RealVNC Ltd. 2013-2018. All rights reserved.
 */

#ifndef VNCEXPORT
/**
 * \brief Specify whether to export Server SDK symbols.
 * 
 * By default, don't specify any symbol visibility for
 * Server SDK symbols; this only has an effect when
 * building a library that contains the SDK.
 * 
 * For example, to specify hidden visibility for all
 * Server SDK symbols (before including any Server SDK
 * header files):
 * 
 * \code
 * #define VNCEXPORT __attribute__((visibility("hidden")))
 * \endcode
 */
#define VNCEXPORT
#endif

#endif
