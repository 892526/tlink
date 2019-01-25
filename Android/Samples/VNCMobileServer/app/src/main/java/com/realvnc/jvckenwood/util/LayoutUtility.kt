package com.realvnc.jvckenwood.util

import java.util.*

object LayoutUtility {

    fun isRTL(): Boolean {
        return isRTL(Locale.getDefault())
    }

    fun isRTL(locale: Locale): Boolean {
        val directionality = Character.getDirectionality(locale.displayName.elementAt(0)).toInt()
        return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT.toInt() || directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC.toInt()
    }

}