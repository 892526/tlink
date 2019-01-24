package com.realvnc.androidsampleserver.activity

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.util.Log
import com.realvnc.androidsampleserver.R

class OverlayPermissionDialogFragment : DialogFragment() {

    companion object {
        private const val TAG = "OverlayPermissionDialog"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity as Context)
                .setTitle(R.string.SS_04_204)
                .setMessage(R.string.SS_04_205)
                .setCancelable(false)
                .setPositiveButton(R.string.SS_04_203) { _, _ ->
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        throw RuntimeException(
                                "Overlay Permission Dialog displayed on an old system")
                    } else if (!Settings.canDrawOverlays(activity)) {
                        try {
                            Log.w(TAG, "Opening the 'Manage Overlay Permission' settings")

                            val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + activity?.packageName)
                            )

                            activity?.startActivityForResult(
                                    intent,
                                    VNCMobileServer.MANAGE_OVERLAY_PERMISSION_REQUEST
                            )
                        } catch (e: ActivityNotFoundException) {
                            Log.e(TAG, "Unable to start overlay permission activity")
                        }

                    } else {
                        Log.w(TAG, "Overlay permission already granted")
                    }
                }

        isCancelable = false

        return builder.create()
    }
}