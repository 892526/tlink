/* Copyright (C) 2002-2018 VNC Automotive Ltd.  All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of a
 * VNC Automotive SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.androidsampleserver;

public class SampleIntents {
    
    public static final String BIND_SERVICE_INTENT = "com.realvnc.androidsampleserver.ACTION_BIND_SERVICE";
	
    public static final String HTTP_TRIGGER_INTENT = "com.realvnc.androidsampleserver.ACTION_HTTP_TRIGGER";

    public static final String HTTP_TRIGGER_ACCEPT_INTENT = "com.realvnc.androidsampleserver.HTTP_ACCEPT_ACCEPT";

    public static final String HTTP_TRIGGER_REJECT_INTENT = "com.realvnc.androidsampleserver.HTTP_ACCEPT_REJECT";

    public static final String RUN_SERVER_INTENT = "com.realvnc.androidsampleserver.ACTION_RUN";

    public static final String SHOW_UI_INTENT = "com.realvnc.androidsampleserver.ACTION_SHOW";

    public static final String RESET_SERVER_INTENT = "com.realvnc.androidsampleserver.ACTION_RESET";
    public static final String RESET_SERVER_WAIT_FOR_FLUSH_EXTRA = "com.realvnc.androidsampleserver.wait_for_flush_extra";

    public static final String START_SERVER_INTENT = "com.realvnc.androidsampleserver.ACTION_START";
    public static final String START_SERVER_FROM_AAP_INTENT = "com.realvnc.androidsampleserver.ACTION_START_AAP";

    public static final String STOP_SERVER_INTENT = "com.realvnc.androidsampleserver.ACTION_STOP";

    public static final String PREFERENCES_INTENT = "com.realvnc.androidsampleserver.ACTION_PREFERENCES";

    /* Dialogs that the server activity can display. */

    public static final String HTTP_ACCEPT_DIALOG_INTENT = "com.realvnc.androidsampleserver.ACTION_HTTP_ACCEPT";
    public static final String HTTP_DISCONNECT_DIALOG_INTENT = "com.realvnc.androidsampleserver.ACTION_HTTP_DISCONNECT";
    public static final String AUTH_ACCEPT_DIALOG_INTENT = "com.realvnc.androidsampleserver.ACTION_AUTH_ACCEPT";
    public static final String REVAUTH_PROMPT_DIALOG_INTENT = "com.realvnc.androidsampleserver.ACTION_REVAUTH_PROMPT";
    public static final String ACCEPT_PROMPT_DIALOG_INTENT = "com.realvnc.androidsampleserver.ACTION_ACCEPT_PROMPT";
    public static final String USB_CHOICE_DIALOG_INTENT = "com.realvnc.androidsampleserver.ACTION_USB_CHOICE";
    public static final String AAP_NOT_CHOSEN_DIALOG_INTENT = "com.realvnc.androidsampleserver.ACTION_AAP_NOT_CHOSEN";
    public static final String ACCESSIBILITY_DIALOG_INTENT = "com.realvnc.androidsampleserver.ACTION_ACCESSIBILITY";
    public static final String OVERLAY_PERMISSION_DIALOG_INTENT = "com.realvnc.androidsampleserver.OVERLAY_PERMISSION";

    /* Requests that the server activity tries to install a remote control service */
    public static final String INSTALL_RCS_INTENT = "com.realvnc.androidsampleserver.ACTION_INSTALL_RCS";

    public static final String LAUNCHER_INTENT = "com.realvnc.androidsampleserver.ACTION_LAUNCHER";

    public static final String REQUEST_PERMISSIONS_INTENT = "com.realvnc.androidsampleserver.ACTION_REQUEST_PERMISSIONS";

    /* UsbAccessoryProxy actions */

    public static final String USB_ACCESSORY_ATTACHED = "com.realvnc.androidsampleserver.ACTION_USB_ACCESSORY_ATTACHED";

}
