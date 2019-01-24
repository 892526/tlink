package com.realvnc.androidsampleserver.activity

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.util.Log
import com.realvnc.androidsampleserver.R

class AccessibilityDialogFragment : DialogFragment() {

    companion object {
        private const val TAG = "AccessibilityDialogFrag"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity as Context)
                .setTitle(R.string.SS_04_201)
                .setMessage(R.string.SS_04_202)
                .setCancelable(false)
                .setPositiveButton(R.string.SS_04_203) { _, _ ->
                    try {
                        activity?.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    } catch (e: ActivityNotFoundException) {
                        Log.e(TAG, "Unable to start accessibility settings activity")
                    }
                }

        isCancelable = false

        return builder.create()
    }
}