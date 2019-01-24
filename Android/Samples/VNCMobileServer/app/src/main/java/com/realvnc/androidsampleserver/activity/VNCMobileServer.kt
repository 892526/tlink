/* Copyright (C) 2002-2018 RealVNC Ltd. All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of the
 * VNC Mobile Solution SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.androidsampleserver.activity

import android.Manifest.permission
import android.app.Activity
import android.app.Dialog
import android.app.admin.DevicePolicyManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.RemoteException
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast

import com.realvnc.androidsampleserver.IVncServerInterface
import com.realvnc.androidsampleserver.IVncServerListener
import com.realvnc.androidsampleserver.R
import com.realvnc.androidsampleserver.SampleIntents
import com.realvnc.androidsampleserver.ServiceInstaller
import com.realvnc.androidsampleserver.VncServerApp
import com.realvnc.androidsampleserver.VncServerState
import com.realvnc.androidsampleserver.VncServerState.VncServerMainState
import com.realvnc.androidsampleserver.VncUsbState
import com.realvnc.androidsampleserver.receiver.RemoteControlDeviceAdminReceiver
import com.realvnc.androidsampleserver.service.HTTPTriggerService
import com.realvnc.androidsampleserver.service.VncServerService
import com.realvnc.vncserver.core.VncServerCoreErrors
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.main.*

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.reflect.Field
import java.util.Locale

// We could use
//   import com.android.future.usb.UsbManager;
// to import the USBManager code, but then this code wouldn't compile
// without having the Google API plugins installed. Instead we
// use reflection as only the UsbManager.ACTION_USB_ACCESSORY_ATTACHED
// value is required.

/**
 * This class is the activity; that is, the main GUI view. In Android terms an [Activity] is
 * some sort of GUI view. However, such activities may be closed whenever they are no longer in
 * the foreground of the device. We therefore can't run the VNC server in the Activity; we instead
 * create a [Service] ([VncServerService]) to run the VNC server. To communicate
 * from the activity to the service we use [IVncServerInterface].
 * @author aat
 */
class VNCMobileServer : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    /**
     * An object which allows us to make asynchronous callbacks into the UI
     * thread from other threads. We will use this to update the status message.
     */
    private val mHandler = Handler()

    private var mServiceInterfaceConnection: ServerServiceConnection? = null

    private var mServiceInterface: IVncServerInterface? = null

    private var mPreviousState: VncServerState? = null
    private var mState: VncServerState? = null

    private var mApp: VncServerApp? = null

    private var mRequestedNonMarketInstall: Boolean = false
    private var mRcsApkFile: Uri? = null

    private var mDialog: Dialog? = null

    private val isNonMarketInstallationAllowed: Boolean
        get() = Settings.Secure.getInt(contentResolver,
                Settings.Secure.INSTALL_NON_MARKET_APPS, 0) > 0

    private val isSamsungDevice: Boolean
        get() = Build.MANUFACTURER.toLowerCase(Locale.US).contains("samsung")

    private var mCallbackHandler: CallbackHandler? = CallbackHandler()


    fun dialogDismissed() {
        // Nothing to do
    }


    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        setSupportActionBar(toolbar)
        title = resources.getString(R.string.app_name)

        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        ServiceInstaller.getSystemSigningKeys(this)
        Log.i(TAG, "device = " + Build.DEVICE)
        Log.i(TAG, "version_release = " + Build.VERSION.RELEASE)
        Log.i(TAG, "model = " + Build.MODEL)
        Log.i(TAG, "product = " + Build.PRODUCT)
        Log.i(TAG, "cpu_abi options = " + java.util.Arrays.toString(supportedAbis))
        Log.i(TAG, "api_level = " + Build.VERSION.SDK_INT)

        mApp = application as VncServerApp

        // Connect to the VNC server service, so we can control it.
        mServiceInterfaceConnection = ServerServiceConnection()
        val serviceIntent = Intent(this, VncServerService::class.java)
        serviceIntent.action = SampleIntents.BIND_SERVICE_INTENT
        serviceIntent.`package` = packageName
        bindService(serviceIntent, mServiceInterfaceConnection, Context.BIND_AUTO_CREATE)


    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onDestroy()
     */
    public override fun onDestroy() {
        super.onDestroy()
        try {
            mServiceInterface!!.unregisterListener(mCallbackHandler)
        } catch (e: NullPointerException) {
            /* activity destroyed before service connected - that's
             * ok, we can ignore this error */
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to unregister listener with exception: " + e)
        }

        unbindService(mServiceInterfaceConnection)
        mCallbackHandler = null
        mServiceInterface = null
        mServiceInterfaceConnection = null
    }

    override fun onNewIntent(i: Intent) {
        super.onNewIntent(i)
        intent = i
    }

    public override fun onStart() {
        super.onStart()
    }

    public override fun onResume() {
        super.onResume()

        updateStatusText()

        if (mServiceInterface != null) {
            try {
                mState = mServiceInterface!!.VNCServerStateGetState()
                stateUpdated()
            } catch (e: RemoteException) {
                Log.e(TAG, "Failed to get server state with exception: " + e)
            }

            takeAction()
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ACTIVITY_ENABLE_NON_MARKET_INSTALL) {
            tryToInstallService()
        } else if (requestCode == ACTIVITY_INSTALL_SERVICE) {
            reportInstallationResult(VncServerCoreErrors.VNCSERVER_ERR_NONE)
        }

    }

    private fun takeAction() {
        // Don't try to redo the action if relaunched from history
        var intent: String? = SampleIntents.LAUNCHER_INTENT
        if (getIntent().flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY == 0)
            intent = getIntent().action


        if (intent == SampleIntents.ACCEPT_PROMPT_DIALOG_INTENT) {

            val args = Bundle()
            args.putString("address", mState!!.connectedAddress)
            showDialog(DIALOG_ACCEPT, args)

        } else if (intent == SampleIntents.USB_CHOICE_DIALOG_INTENT) {

            val args = Bundle()
            showDialog(DIALOG_USB_CHOICE, args)

        } else if (intent == SampleIntents.REVAUTH_PROMPT_DIALOG_INTENT) {

            val args = Bundle()
            args.putBoolean("username", mState!!.usernameReq)
            args.putBoolean("password", mState!!.passwordReq)
            showDialog(DIALOG_AUTH_REQ, args)

        } else if (intent == SampleIntents.AUTH_ACCEPT_DIALOG_INTENT) {

            val args = Bundle()
            args.putString("username", mState!!.username)
            args.putString("password", mState!!.password)
            showDialog(DIALOG_AUTH, args)

        } else if (intent == SampleIntents.HTTP_ACCEPT_DIALOG_INTENT) {
            showDialog(DIALOG_HTTP_ACCEPT)
        } else if (intent == SampleIntents.INSTALL_RCS_INTENT) {
            Log.i(TAG, "Asked to install RCS")
            mRequestedNonMarketInstall = false
            mRcsApkFile = getIntent().data
            tryToInstallService()
        } else if (intent == SampleIntents.AAP_NOT_CHOSEN_DIALOG_INTENT) {
            Log.i(TAG, "Asked to display warning about AAP")
            val args = Bundle()
            showDialog(DIALOG_AAP_NOT_CHOSEN, args)
        } else if (intent == SampleIntents.ACCESSIBILITY_DIALOG_INTENT) {
            Log.i(TAG, "Asked to request that the accessibilty service be enabled")
            val args = Bundle()
            showDialog(DIALOG_ACCESSIBILITY, args)
        } else if (intent == SampleIntents.REQUEST_PERMISSIONS_INTENT) {
            val perms = getIntent().getStringArrayExtra("permissions")
            Log.i(TAG, "Asked to request permissions")
            doRequestPermissions(perms, PERMISSIONS_REQUEST_STARTUP)
        } else {
            enableDeviceAdminIfRequired()
        }

        setIntent(Intent(SampleIntents.LAUNCHER_INTENT))
    }

    protected fun reportInstallationResult(result: Int) {
        try {
            mServiceInterface!!.VNCServerInstallationResult(result)
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to get installation result with exception: " + e)
        }

    }

    override fun onCreateDialog(id: Int, args: Bundle): Dialog? {
        when (id) {
            DIALOG_ABOUT -> return VncServerAboutDialog(this)

            DIALOG_ACCEPT -> return VncServerAcceptDialog(this)

            DIALOG_AUTH -> return VncServerAuthDialog(this)

            DIALOG_AUTH_REQ -> return VncServerAuthReqDialog(this)

            DIALOG_COMMAND_STRING -> return VncServerCommandStringDialog(this)

            DIALOG_CONNECT -> return VncServerConnectDialog(this)

            DIALOG_HTTP_ACCEPT -> return VncServerHttpAcceptDialog(this)

            DIALOG_NON_MARKET_INSTALL -> return VncServerNonMarketInstallDialog(this)

            DIALOG_USB_CHOICE -> return VncServerUsbChoiceDialog(this)

            DIALOG_AAP_NOT_CHOSEN -> return VncServerAapNotChosenDialog(this)

            DIALOG_ACCESSIBILITY -> return VncServerAccessibilityDialog(this)
        }

        throw IllegalArgumentException("Bad dialog ID " + id)
    }

    override fun onPrepareDialog(id: Int, dialog: Dialog, args: Bundle) {
        super.onPrepareDialog(id, dialog, args)
        when (id) {
            DIALOG_ABOUT -> (dialog as VncServerAboutDialog).setArgs(args)

            DIALOG_ACCEPT -> (dialog as VncServerAcceptDialog).setArgs(args)

            DIALOG_AUTH -> (dialog as VncServerAuthDialog).setArgs(args)

            DIALOG_AUTH_REQ -> (dialog as VncServerAuthReqDialog).setArgs(args)

            DIALOG_COMMAND_STRING -> (dialog as VncServerCommandStringDialog).setArgs(args)

            DIALOG_CONNECT -> (dialog as VncServerConnectDialog).setArgs(args)

            DIALOG_USB_CHOICE -> (dialog as VncServerUsbChoiceDialog).setArgs(args)

            DIALOG_AAP_NOT_CHOSEN -> (dialog as VncServerAapNotChosenDialog).setArgs(args)

            else -> {
            }
        }
    }

    /*
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        var isListening = false
        var isConnected = false
        var isBroken = false
        var isGeneratingKey = false

        if (mState != null) {
            val s = mState!!.state
            if (s == VncServerMainState.KEYGEN) {
                isGeneratingKey = true
            }
            if (s == VncServerMainState.LISTENING) {
                isListening = true
            } else if (s == VncServerMainState.CONNECTING ||
                    s == VncServerMainState.ACCEPTING ||
                    s == VncServerMainState.AUTHENTICATING ||
                    s == VncServerMainState.REQUESTING_AUTH ||
                    mState!!.isRunning) {
                isConnected = true
            } else if (s == VncServerMainState.ERROR && mState!!.errorCode == VncServerCoreErrors.VNCSERVER_ERR_ENVIRONMENT) {
                isBroken = true

            }
        }

        if (isBroken || isGeneratingKey) {
            menu.findItem(R.id.settings).isVisible = false
            menu.findItem(R.id.listen).isVisible = false
            menu.findItem(R.id.usb_connect).isVisible = false
            menu.findItem(R.id.connect).isVisible = false
            menu.findItem(R.id.stop).isVisible = false
            menu.findItem(R.id.disconnect).isVisible = false
            menu.findItem(R.id.auto_connect).isVisible = false
            menu.findItem(R.id.cmdstring).isVisible = false
            menu.findItem(R.id.device_admin).isVisible = false
        } else if (isListening) {
            menu.findItem(R.id.settings).isVisible = false
            menu.findItem(R.id.listen).isVisible = false
            menu.findItem(R.id.usb_connect).isVisible = false
            menu.findItem(R.id.connect).isVisible = false
            menu.findItem(R.id.stop).isVisible = true
            menu.findItem(R.id.disconnect).isVisible = false
            menu.findItem(R.id.auto_connect).isVisible = true
            menu.findItem(R.id.cmdstring).isVisible = true
            menu.findItem(R.id.device_admin).isVisible = false
        } else if (isConnected) {
            menu.findItem(R.id.settings).isVisible = false
            menu.findItem(R.id.listen).isVisible = false
            menu.findItem(R.id.usb_connect).isVisible = false
            menu.findItem(R.id.connect).isVisible = false
            menu.findItem(R.id.stop).isVisible = false
            menu.findItem(R.id.disconnect).isVisible = true
            menu.findItem(R.id.auto_connect).isVisible = false
            menu.findItem(R.id.cmdstring).isVisible = true
            menu.findItem(R.id.device_admin).isVisible = false
        } else {
            menu.findItem(R.id.settings).isVisible = true
            menu.findItem(R.id.listen).isVisible = true
            menu.findItem(R.id.usb_connect).isVisible = true
            menu.findItem(R.id.connect).isVisible = true
            menu.findItem(R.id.stop).isVisible = false
            menu.findItem(R.id.disconnect).isVisible = false
            menu.findItem(R.id.auto_connect).isVisible = true
            menu.findItem(R.id.cmdstring).isVisible = true
            menu.findItem(R.id.device_admin).isVisible = isSamsungDevice
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.listen -> {
                /* Get listening port number from the preferences. */
                val prefs = PreferenceManager.getDefaultSharedPreferences(baseContext)
                val listenPort = Integer.parseInt(prefs.getString("vnc_port", "5900"))

                val cmdString = "vnccmd:v=1;t=L;p=" + listenPort

                try {
                    mServiceInterface!!.VNCServerConnect(cmdString, true)
                } catch (e: RemoteException) {
                    Log.e(TAG, "Failed to connect with exception: " + e)
                }

                return true
            }

            R.id.auto_connect -> {
                // We need this permission in order to get the device's IMEI.
                if (isPermissionGranted(permission.READ_PHONE_STATE)) {
                    autoConnect()
                } else {
                    doRequestPermissions(arrayOf(permission.READ_PHONE_STATE),
                            PERMISSIONS_REQUEST_AUTOCONNECT)
                }
                return true
            }

            R.id.usb_connect -> {
                try {
                    mServiceInterface!!.VNCServerConnect("vnccmd:v=1;t=" + VncUsbState.getUsbBearer(baseContext), true)
                } catch (e: RemoteException) {
                    Log.e(TAG, "Failed to connect with exception: " + e)
                }

                return true
            }

            R.id.stop -> {
                try {
                    mServiceInterface!!.VNCServerReset()
                } catch (e: RemoteException) {
                    Log.e(TAG, "Failed to reset server with exception: " + e)
                }

                return true
            }

            R.id.disconnect -> {
                try {
                    mServiceInterface!!.VNCServerDisconnect()
                } catch (e: RemoteException) {
                    Log.e(TAG, "Failed to disconnect server with exception: " + e)
                }

                return true
            }

            R.id.settings -> {
                val i = Intent(this, VncServerPreferenceActivity::class.java)
                i.action = SampleIntents.PREFERENCES_INTENT
                i.`package` = packageName
                startActivity(i)
                return true
            }

            R.id.showlog -> {
                /* We need this permission in order to copy the log to the SD
             * card, so external applications are able to open it. */
                if (isPermissionGranted(permission.WRITE_EXTERNAL_STORAGE)) {
                    showLog()
                } else {
                    doRequestPermissions(arrayOf(permission.WRITE_EXTERNAL_STORAGE),
                            PERMISSIONS_REQUEST_LOG)
                }
                return true
            }

            R.id.about -> {
                // Log device signing keys and details useful for debugging.
                ServiceInstaller.getSystemSigningKeys(this)
                Log.i(TAG, "device = " + Build.DEVICE)
                Log.i(TAG, "version_release = " + Build.VERSION.RELEASE)
                Log.i(TAG, "model = " + Build.MODEL)
                Log.i(TAG, "product = " + Build.PRODUCT)
                Log.i(TAG, "cpu_abi options = " + java.util.Arrays.toString(supportedAbis))
                Log.i(TAG, "api_level = " + Build.VERSION.SDK_INT)

                val about_args = Bundle()

                try {
                    about_args.putString("version", mServiceInterface!!.VNCServerGetVersionString())
                } catch (e: RemoteException) {
                    Log.e(TAG, "Failed to get version string with exception: " + e)
                    about_args.putString("version", "?")
                }

                showDialog(DIALOG_ABOUT, about_args)
                return true
            }

            R.id.connect -> {
                val connect_args = Bundle()
                connect_args.putString("address", mApp!!.GetPreviousConnect())
                showDialog(DIALOG_CONNECT, connect_args)
                return true
            }

            R.id.cmdstring -> {
                val cmdstring_args = Bundle()
                cmdstring_args.putString("cmdstring", mApp!!.GetPreviousCmdString())
                showDialog(DIALOG_COMMAND_STRING, cmdstring_args)
                return true
            }

            R.id.device_admin -> {
                if (!requestDeviceAdminIfDisabled()) {
                    Toast.makeText(this, resources.getString(R.string.admin_already_admin), Toast.LENGTH_LONG).show()
                }
                return true
            }
        }

        Log.e(TAG, "Unknown menu option")

        return false
    }
    */

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        var intent: Intent? = null

        when (item.itemId) {
            R.id.navigation_view_menu_about -> {
                //intent = Intent(this, OverviewActivity::class.java)
                Toast.makeText(this, "Unimplemented.", Toast.LENGTH_SHORT).show()
            }
            R.id.navigation_view_menu_qa -> {
                //val uri = Uri.parse("http://www.kenwood.com/jp/products/car_audio/app/kenwood_music_info/faq.html")
                //intent  = Intent(Intent.ACTION_VIEW, uri)
                Toast.makeText(this, "Unimplemented.", Toast.LENGTH_SHORT).show()
            }
            R.id.navigation_view_menu_tos -> {
                //intent = Intent(this, TermsOfServiceActivity::class.java)
                Toast.makeText(this, "Unimplemented.", Toast.LENGTH_SHORT).show()
            }
            R.id.navigation_view_menu_oss -> {
                //intent = Intent(this, OpenSourceLicensesActivity::class.java)
                Toast.makeText(this, "Unimplemented.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                return false
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)

        intent?.let {
            startActivity(intent)
        }

        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        if (permissions.size == 0) {
            // The request was cancelled, not rejected though, so ignore this.
            return
        }
        for (i in permissions.indices) {
            var errMsg = ""
            if (permissions[i] == permission.READ_EXTERNAL_STORAGE) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        mServiceInterface!!.VNCServerLoadLicenses()
                    } catch (e: RemoteException) {
                        Log.e(TAG, "Failed to load licenses with exception: " + e)
                    }

                    continue
                } else {
                    errMsg = "Permission to access SD card not granted, so no VNC license(s) could be loaded."
                }
            } else if (permissions[i] == permission.WRITE_EXTERNAL_STORAGE) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    showLog()
                    continue
                } else {
                    errMsg = "Permission to access SD card not granted, so can't open log file."
                }
            } else if (permissions[i] == permission.RECEIVE_SMS) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    continue
                } else {
                    errMsg = "Required permission not granted, so Mobile Bridge connections are not possible."
                }
            } else if (permissions[i] == permission.READ_PHONE_STATE) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    if (requestCode == PERMISSIONS_REQUEST_AUTOCONNECT) {
                        /* If we are here because 'Auto Connect' was selected then
                         * start the Mobile Bridge connection now that we have the
                         * required permission. */
                        autoConnect()
                    }
                    continue
                } else {
                    errMsg = "Required permission not granted, so Mobile Bridge connections are not possible."
                }
            }

            try {
                mServiceInterface!!.VNCServerSetError(VncServerCoreErrors.VNCSERVER_ERR_PERMISSIONS)
            } catch (e: RemoteException) {
                Log.e(TAG, "Failed to set error with exception: " + e)
            }

            Log.i(TAG, errMsg)
            Toast.makeText(this, errMsg, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Callback from the connect dialog when OK is pressed
     */
    fun handleConnect(address: String) {
        var address = address

        /* is there a port number? */
        val idx = address.indexOf(':')
        var port = DEFAULT_PORT

        mApp!!.SetPreviousConnect(address)

        try {
            if (idx >= 0) {
                if (address[idx + 1] == ':') {
                    port = Integer.parseInt(address.substring(idx + 2))
                } else {
                    port = Integer.parseInt(address.substring(idx + 1))
                    if (port < 0) throw NumberFormatException()
                    if (port < 100) port += DEFAULT_PORT
                }
                address = address.substring(0, idx)
            }

            if (port < 0 || port > MAX_TCP_PORT)
                throw NumberFormatException()

            val cmdString = "vnccmd:v=1;t=C;a=$address;p=$port"
            mServiceInterface!!.VNCServerConnect(cmdString, false)

        } catch (e: NumberFormatException) {
            // Invalid port number
            updateStatusText(getErrorString(VncServerCoreErrors.VNCSERVER_ERR_BAD_PORT))
        } catch (e: NullPointerException) {
            Log.e(TAG, "Failed to connect with exception: " + e)
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to connect with exception: " + e)
        }

    }

    /**
     * Callback from the command string dialog when OK is pressed
     */
    fun handleCommandString(command: String) {

        mApp!!.SetPreviousCmdString(command)

        try {

            mServiceInterface!!.VNCServerConnect(command, false)

        } catch (e: NullPointerException) {
            Log.e(TAG, "Failed to connect with exception: " + e)
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to connect with exception: " + e)
        }

    }

    private fun showLog() {
        try {
            val logPath = mServiceInterface!!.VNCServerGetLogPath()
            var logFile = File(logPath)
            /* See if we can place it on the SD card, as that's
             * more likely to be readable by other applications. */
            val sdFile = File(getExternalFilesDir(null),
                    logFile.name)
            if (copyFile(logFile, sdFile)) {
                logFile = sdFile
            } else {
                // If we failed then just make the file world-readable.
                Runtime.getRuntime().exec("chmod 644 " + logPath)
            }

            val i = Intent(Intent.ACTION_VIEW)
            val uri = Uri.fromFile(logFile)
            i.setDataAndType(uri, "text/plain")
            startActivity(i)
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "Failed to open log with exception: " + e)
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to open log with exception: " + e)
        } catch (e: IOException) {
            Log.e(TAG, "Can't open log, as chmod command failed")
        }

    }

    private fun autoConnect() {
        val i = Intent(this, HTTPTriggerService::class.java)
        i.action = SampleIntents.HTTP_TRIGGER_INTENT
        i.`package` = packageName
        startService(i)
    }

    @android.annotation.TargetApi(23)
    private fun isPermissionGranted(permission: String): Boolean {
        var isGranted = true
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            /* From Android 6.0 onwards we need to request 'dangerous'
             * permissions dynamically, they are no longer granted at
             * install time. */
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                isGranted = false
            }
        }
        return isGranted
    }

    @android.annotation.TargetApi(23)
    private fun doRequestPermissions(permList: Array<String>, reqCode: Int) {
        // We wrap around the Android API so we can handle this lint exception neatly.
        requestPermissions(permList, reqCode)
    }

    private fun getErrorString(code: Int): String {
        var statusText: String

        try {
            val field = R.string::class.java.getDeclaredField("error_vnc_" + code)
            val resCode = field.getInt(R.string::class.java)
            statusText = resources.getString(resCode)
        } catch (e: NoSuchFieldException) {
            /* This happens if there was no string resource
             * corresponding to this error code */
            statusText = resources.getString(R.string.error_no_errmsg)
        } catch (e: IllegalAccessException) {
            Log.e(TAG, "Failed to get error string with exception: " + e)
            statusText = resources.getString(R.string.status_exception,
                    e.message)
        }

        val header = resources.getString(R.string.error_msgheader, code)
        return header + statusText
    }

    private fun updateStatusText(message: String?, secondaryMessage: String? = null) {
        Log.i(TAG, "Status: $message $secondaryMessage")

        mHandler.post {
            val statusText = findViewById<View>(R.id.status_text) as TextView
            statusText.text = message

            val secondaryStatusText = findViewById<View>(R.id.secondary_status_text) as TextView
            if (secondaryMessage == null)
                secondaryStatusText.text = ""
            else
                secondaryStatusText.text = secondaryMessage
        }
    }

    /**
     * This method updates the status text which is displayed to the user.
     * It could do this either by keeping track of all the different
     * callbacks, or by asking the server for its status at the time
     * it's required. We do the latter.
     *
     *
     * This can be called from any thread since it uses a [Handler]
     * actually to display the text.
     */
    private fun updateStatusText() {
        var statusText: String? = null
        var secondaryStatusText: String? = null
        if (mServiceInterface == null) {
            statusText = resources.getString(R.string.status_starting)
        } else {
            try {
                mPreviousState = mState
                mState = mServiceInterface!!.VNCServerStateGetState()
                stateUpdated()

                /* If we've returned to 'disconnected' state after
                 * displaying an error, don't update the status UI;
                 * instead leave the error message visible.
                 *
                 * (We do allow the "Remote control service not
                 * installed" error to be cleared.) */
                if (mPreviousState != null &&
                        mPreviousState!!.state == VncServerMainState.ERROR &&
                        mPreviousState!!.errorCode != VncServerCoreErrors.VNCSERVER_ERR_UNABLE_TO_START_SERVICE &&
                        mState!!.state == VncServerMainState.DISCONNECTED)
                    return

                if (mState!!.requestingDialog())
                    secondaryStatusText = resources.getString(R.string.status2_dialog)

                if (mState!!.apiCalled != null) {
                    when (mState!!.apiCalled) {
                        VncServerState.VncServerAPICalledState.KEYGEN -> statusText = resources.getString(R.string.status_keygen)
                        VncServerState.VncServerAPICalledState.ACCEPT_AUTH -> statusText = resources.getString(R.string.status_accept_auth)
                        VncServerState.VncServerAPICalledState.ACCEPT_CONN -> statusText = resources.getString(R.string.status_accept_conn)
                        VncServerState.VncServerAPICalledState.CONNECT -> statusText = resources.getString(R.string.status_connect)
                        VncServerState.VncServerAPICalledState.DENY_AUTH -> statusText = resources.getString(R.string.status_deny_auth)
                        VncServerState.VncServerAPICalledState.DENY_CONN -> statusText = resources.getString(R.string.status_deny_conn)
                        VncServerState.VncServerAPICalledState.LISTEN -> statusText = resources.getString(R.string.status_listen)
                        VncServerState.VncServerAPICalledState.RESET -> statusText = resources.getString(R.string.status_reset)
                        VncServerState.VncServerAPICalledState.SUPPLY_AUTH -> statusText = resources.getString(R.string.status_supply_auth)
                    }
                } else {
                    when (mState!!.state) {
                        VncServerState.VncServerMainState.ACCEPTING -> statusText = resources.getString(R.string.status_accepting)
                        VncServerState.VncServerMainState.AUTHENTICATING -> statusText = resources.getString(R.string.status_authenticating)
                        VncServerState.VncServerMainState.REQUESTING_AUTH -> statusText = resources.getString(R.string.status_requesting_auth)
                        VncServerState.VncServerMainState.CONNECTING -> statusText = resources.getString(R.string.status_connecting)
                        VncServerState.VncServerMainState.DISCONNECTED -> statusText = resources.getString(R.string.status_disconnected)
                        VncServerState.VncServerMainState.LISTENING -> statusText = resources.getString(R.string.status_listening,
                                mState!!.listeningAddress)
                        VncServerState.VncServerMainState.RUNNING -> statusText = resources.getString(R.string.status_running,
                                mState!!.connectedAddress)
                        VncServerState.VncServerMainState.ERROR ->

                            statusText = getErrorString(mState!!.errorCode)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update status with exception: " + e)
                statusText = resources.getString(R.string.status_exception,
                        e.message)
            }

        }

        updateStatusText(statusText, secondaryStatusText)
    }

    fun handleHttpAcceptResult(accept: Boolean) {
        val i = Intent(this, HTTPTriggerService::class.java)
        i.`package` = packageName
        if (accept) {
            i.action = "com.realvnc.androidsampleserver.HTTP_ACCEPT_ACCEPT"
        } else {
            i.action = "com.realvnc.androidsampleserver.HTTP_ACCEPT_REJECT"
        }
        startService(i)
    }

    fun handleAuthResult(accept: Boolean) {
        try {
            mServiceInterface!!.VNCServerAuthenticate(accept)
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to authenticate with exception: " + e)
        }

    }

    fun handleAuthReqResult(username: String, password: String) {
        try {
            mServiceInterface!!.VNCServerLogin(username, password)
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to log in with exception: " + e)
        }

    }

    fun handleAuthReqCancel() {
        try {
            mServiceInterface!!.VNCServerDisconnect()
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to cancel connection with exception: " + e)
        }

    }

    fun handleAcceptResult(accept: Boolean) {
        try {
            mServiceInterface!!.VNCServerAccept(accept)
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to accept connection with exception: " + e)
        }

    }

    fun handleUsbChoiceResult(accept: Boolean) {
        if (accept) {
            VncUsbState.setStage(this,
                    VncUsbState.Stage.WAITING_FOR_ACTIVE_TETHER)
            VncUsbState.startUsbTethering(this)
        } else {
            VncUsbState.setStage(this,
                    VncUsbState.Stage.NOT_WAITING)
        }
    }

    fun handleNonMarketInstallResult(accept: Boolean) {

        if (accept) {
            val currentapiVersion = android.os.Build.VERSION.SDK_INT
            if (currentapiVersion >= 14) {
                // As of Ice Cream Sandwhich the option to install from
                // Unknown Sources now exists under the Security menu.

                // While it's not nice to use a magic number like this,
                // we have to compile against older SDKs where
                // android.os.Build.ICE_CREAM_SANDWICH isn't defined.
                try {
                    val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                    startActivityForResult(intent, ACTIVITY_ENABLE_NON_MARKET_INSTALL)
                } catch (e: ActivityNotFoundException) {
                    Log.e(TAG, "Failed to start security settings activity, as it can't be found.")
                }

            } else {
                // Option for installing from Unknown Sources used to exist
                // under the Applications menu.
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_SETTINGS)
                    startActivityForResult(intent, ACTIVITY_ENABLE_NON_MARKET_INSTALL)
                } catch (e: ActivityNotFoundException) {
                    Log.e(TAG, "Failed to start application settings activity, as it can't be found.")
                }

            }
        } else {
            if (mServiceInterface != null) {
                try {
                    mServiceInterface!!.VNCServerSetError(VncServerCoreErrors.VNCSERVER_ERR_UNABLE_TO_START_SERVICE)
                } catch (e: RemoteException) {
                }

            }
        }
    }

    private inner class ServerServiceConnection : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {

            mServiceInterface = IVncServerInterface.Stub.asInterface(service)

            updateStatusText()

            try {
                mServiceInterface!!.registerListener(mCallbackHandler)
                mState = mServiceInterface!!.VNCServerStateGetState()
                stateUpdated()
            } catch (e: RemoteException) {
                Log.e(TAG, "Failed to register listener with exception: " + e)
            }

            takeAction()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mCallbackHandler = null
            mServiceInterface = null
        }
    }

    private fun tryToInstallService() {
        if (!isNonMarketInstallationAllowed) {
            if (!mRequestedNonMarketInstall) {
                mRequestedNonMarketInstall = true
                showDialog(DIALOG_NON_MARKET_INSTALL)
            } else {
                reportInstallationResult(VncServerCoreErrors.VNCSERVER_ERR_UNABLE_TO_START_SERVICE)
            }
        } else {
            doInstallRcsApk()
        }

    }

    private fun doInstallRcsApk() {
        if (mRcsApkFile == null) {
            reportInstallationResult(VncServerCoreErrors.VNCSERVER_ERR_UNABLE_TO_START_SERVICE)
            return
        }
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(mRcsApkFile,
                    "application/vnd.android.package-archive")

            startActivityForResult(intent, ACTIVITY_INSTALL_SERVICE)
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "Failed to open RCS APK file, as no suitable activity can be found")
        }

        mRcsApkFile = null
    }

    private fun samsungRcsPresent(): Boolean {
        try {
            val rcsCls = Class.forName(SAMSUNG_REMOTE_CONTROL_CLASS)
            return true
        } catch (e: ClassNotFoundException) {
            return false
        }

    }

    private fun enableDeviceAdminIfRequired() {
        // Device admin is required for the Samsung Remote Control Service.
        // We need API level 17 (4.2) or above because we require the Samsung
        // Enterprise License Management service. However, some devices running
        // 4.2 have issues in the Samsung KNOX libraries, so limit ourselves to
        // API level 18 and above.
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val requestedDeviceAdmin = sharedPref.getBoolean(ENABLE_DEVICE_ADMIN_REQUESTED, false)

        if (isSamsungDevice &&
                Build.VERSION.SDK_INT >= 18 /*JELLY_BEAN_MR2*/ &&
                samsungRcsPresent() &&
                !requestedDeviceAdmin) {
            requestDeviceAdminIfDisabled()
        }
    }

    // Returns true if it requested the device admin, false otherwise
    private fun requestDeviceAdminIfDisabled(): Boolean {
        // Check whether we already are a device admin
        val mDpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminReceiverComponent = ComponentName(this, RemoteControlDeviceAdminReceiver::class.java)
        if (!mDpm.isAdminActive(adminReceiverComponent)) {
            Log.i(TAG, "Requesting to be added as a device administrator")
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminReceiverComponent)
            startActivity(intent)

            // Indicate that it has been requested
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
            sharedPref.edit().putBoolean(ENABLE_DEVICE_ADMIN_REQUESTED, true).apply()
            return true
        }
        return false
    }

    inner class CallbackHandler : IVncServerListener.Stub() {
        override fun listeningCb(ipAddresses: String) {
            updateStatusText()
        }

        override fun connectingCb() {
            updateStatusText()
        }

        override fun connectedCb(ipAddress: String) {
            updateStatusText()
        }

        override fun runningCb() {
            updateStatusText()
        }

        override fun disconnectedCb() {
            updateStatusText()

            if (mDialog != null) {
                mDialog!!.dismiss()
                mDialog = null
            }
        }

        override fun errorCb(errorCode: Int) {
            updateStatusText()

            if (mDialog != null) {
                mDialog!!.dismiss()
                mDialog = null
            }
        }

        override fun keygenCb(keyPair: ByteArray) {
            updateStatusText()
        }

        override fun authCb(user: String, pass: String) {
            updateStatusText()
        }

        override fun loginCb(userReq: Boolean, passReq: Boolean) {
            updateStatusText()
        }

        override fun updateUiCb() {
            updateStatusText()
        }
    }

    private fun stateUpdated() {
        /* On API level 11 and upwards the menu will be created and prepared
         * once, at the same time as the activity.
         *
         * As the menu contents depends on the state we need to invalidate it.
         */
        invalidateOptionsMenu()
    }

    companion object {

        private fun copyFile(src: File, dst: File): Boolean {
            var fis: FileInputStream? = null
            var fos: FileOutputStream? = null
            var ret = false
            try {
                fis = FileInputStream(src)
                fos = FileOutputStream(dst)
                val buf = ByteArray(4096)
                var read: Int
                do {
                    read = fis.read(buf)
                    if (read > 0) {
                        fos.write(buf, 0, read)
                    }
                } while (read > 0)
                ret = true
            } catch (ie: IOException) {
                ret = false
            } finally {
                try {
                    if (fis != null) {
                        fis.close()
                        fis = null
                    }
                } catch (e: IOException) {
                    // Ignore
                }

                try {
                    if (fos != null) {
                        fos.close()
                        fos = null
                    }
                } catch (e: IOException) {
                    // Ignore
                }

            }
            return ret
        }

        private val TAG = "VNCMobileServer"

        private val MAX_TCP_PORT = 0xFFFF
        private val DEFAULT_PORT = 5500

        val ACTIVITY_INSTALL_SERVICE = 2
        val ACTIVITY_ENABLE_NON_MARKET_INSTALL = 3

        val DIALOG_ABOUT = 1
        val DIALOG_ACCEPT = 2
        val DIALOG_AUTH = 3
        val DIALOG_AUTH_REQ = 4
        val DIALOG_COMMAND_STRING = 5
        val DIALOG_CONNECT = 6
        val DIALOG_HTTP_ACCEPT = 7
        val DIALOG_NON_MARKET_INSTALL = 8
        val DIALOG_USB_CHOICE = 9
        val DIALOG_AAP_NOT_CHOSEN = 10
        val DIALOG_ACCESSIBILITY = 11

        val PERMISSIONS_REQUEST_STARTUP = 1
        val PERMISSIONS_REQUEST_LOG = 2
        val PERMISSIONS_REQUEST_AUTOCONNECT = 3

        private val SAMSUNG_REMOTE_CONTROL_CLASS = "com.realvnc.android.remote.samsung.SamsungRemoteControl"

        private val ENABLE_DEVICE_ADMIN_REQUESTED = "enable_device_admin_already_requested"

        private/* Here we ought to be able to directly access the SUPPORTED_ABIS
         * field because we are within a TargetApi annotated method.
         * Unfortunately Android tries to optimize it out, and throws up ugly
         * warnings such as:
         *   "VFY: unable to resolve static field" */ val supportedAbisApi21: Array<String>
            @android.annotation.TargetApi(21)
            get() {
                try {
                    return android.os.Build::class.java.getField("SUPPORTED_ABIS").get(null) as Array<String>
                } catch (e: NoSuchFieldException) {
                    return arrayOf()
                } catch (e: IllegalAccessException) {
                    return arrayOf()
                }

            }

        private val supportedAbisApi8: Array<String>
            get() = arrayOf(android.os.Build.CPU_ABI, android.os.Build.CPU_ABI2)

        private val supportedAbis: Array<String>
            get() = if (android.os.Build.VERSION.SDK_INT >= 21) {
                supportedAbisApi21
            } else {
                supportedAbisApi8
            }
    }
}
