package com.realvnc.jvckenwood.controls

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView

class WebViewCustom : WebView {

    private var scrollListener: ScrollListener? = null

    interface ScrollListener {
        fun onScrolledEnd()
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {}
    constructor(context: Context, attributeSet: AttributeSet, style: Int) : super(context, attributeSet, style) {}

    fun setScrollListener(scrollListener: ScrollListener) {
        this.scrollListener = scrollListener
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)

        scrollListener?.let {
            if (computeVerticalScrollRange() <= t + computeVerticalScrollExtent()) {
                scrollListener!!.onScrolledEnd()
            }
        }
    }
}
