package com.realvnc.jvckenwood

import android.content.Intent
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.realvnc.androidsampleserver.VncServerApp
import com.realvnc.jvckenwood.util.AssetUtility

object WhiteListManager {

    /*------------------------------------------------------------------------------------------*/
    //  Enumeration
    /*------------------------------------------------------------------------------------------*/
    enum class Level(val value: Int) {
        Black(0),
        Gray(1),
        White(2);

        companion object {
            fun convValueToEnum(value: Int): Level {
                Level.values().forEach {
                    if(value == it.value) {
                        return it
                    }
                }
                return Black
            }
        }
    }

    /*------------------------------------------------------------------------------------------*/
    //  Variables
    /*------------------------------------------------------------------------------------------*/
    private const val TAG = "WhiteListManager"
    private const val WHITE_LIST_FILENAME = "white_list.json"

    private val homeAppName: String
    private val ignoreList = listOf(
            "com.android.systemui"
    )
    private val whiteList = listOf(
            WhiteListModel("com.google.android.apps.maps","com.google.android.apps.maps",2),
            WhiteListModel("com.waze","com.waze",2),
            WhiteListModel("com.sygic.aura","com.sygic.aura",2),
            WhiteListModel("com.here.app.maps","com.here.app.maps",2),
            WhiteListModel("com.tomtom.gplay.navapp","com.tomtom.gplay.navapp",2),
            WhiteListModel("cz.aponia.bor3","cz.aponia.bor3",2),
            WhiteListModel("com.navfree.android.OSM.OLD","com.navfree.android.OSM.OLD",2),
            WhiteListModel("com.navitel","com.navitel",2),
            WhiteListModel("com.tencent.ibg.joox","com.tencent.ibg.joox",2),
            WhiteListModel("com.spotify.music","com.spotify.music",2),
            WhiteListModel("com.soundcloud.android","com.soundcloud.android",2),
            WhiteListModel("com.shazam.android","com.shazam.android",2),
            WhiteListModel("com.devtab.thairadioplusplus","com.devtab.thairadioplusplus",2),
            WhiteListModel("com.jsmedia.android.eradio.thailand","com.jsmedia.android.eradio.thailand",2),
            WhiteListModel("com.wordbox.thaiRadio","com.wordbox.thaiRadio",2),
            WhiteListModel("com.zing.mp3","com.zing.mp3",2),
            WhiteListModel("com.smule.singandroid","com.smule.singandroid",2),
            WhiteListModel("com.google.android.apps.youtube.music","com.google.android.apps.youtube.music",2)
    )

    /*------------------------------------------------------------------------------------------*/
    //  Initialize
    /*------------------------------------------------------------------------------------------*/
    init {
        /*
        val jsonString = AssetUtility.readFileAsString(WHITE_LIST_FILENAME)
        val type       = object : TypeToken<List<WhiteListModel>>(){}.type
        whiteList = Gson().fromJson(jsonString, type)
        */

        // << Get the Home application name >>
        val packageManager = VncServerApp.getContext().packageManager
        val activityInfo   = packageManager.resolveActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0).activityInfo
        homeAppName = activityInfo.packageName
    }

    /*------------------------------------------------------------------------------------------*/
    //  Public functions
    /*------------------------------------------------------------------------------------------*/
    fun isIgnoreApplication(packageName: String): Boolean {
        return ignoreList.contains(packageName)
    }

    fun check(packageName: String): Level {
        // << Check whether application is home application >>
        if (packageName.equals(homeAppName)) {
            Log.d(TAG, "This is a home application [$packageName]")
            return Level.White
        }

        whiteList.forEach {
            if(packageName.equals(it.packageName)) {
                Log.d(TAG, "This is a ${Level.convValueToEnum(it.level).name} application [$packageName]")
                return Level.convValueToEnum(it.level)
            }
        }
        Log.d(TAG, "This is a BLACK application [$packageName]")
        return Level.Black
    }

    /*------------------------------------------------------------------------------------------*/
    fun dump() {
        Log.d(TAG, "List num : ${whiteList.count()}")
        whiteList.forEach {
            Log.d(TAG, "applicationId:${it.applicationId}, packageName:${it.packageName}, level:${it.level}")
        }
    }
}