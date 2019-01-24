package com.realvnc.jvckenwood.util

import com.realvnc.androidsampleserver.VncServerApp

object AssetUtility {


    fun readFileAsString(filename: String): String {
        val context      = VncServerApp.getContext()
        val assetManager = context?.resources?.assets

        assetManager?.let {
            return assetManager.open(filename).bufferedReader().use {
                it.readText()
            }
        }

        return ""
    }
}
