package com.realvnc.androidsampleserver.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.support.annotation.StringDef
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import com.realvnc.androidsampleserver.R


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [WebViewFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [WebViewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
/*------------------------------------------------------------------------------------------*/
//  public methods
/*------------------------------------------------------------------------------------------*/
abstract class WebViewFragment() : Fragment() {

    private var webView: WebView? = null
    private var errorView: View? = null
    private var progressBar: ProgressBar? = null

    abstract fun getDisplayUrl(): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_webview, container, false)

        // << get progress bar object >>
        progressBar = view.findViewById(R.id.progressbar)

        // << get error view object >>
        errorView = view.findViewById(R.id.errorview)

        //
        webView = view.findViewById(R.id.webview)
        webView!!.loadUrl(getDisplayUrl())

        webView!!.webViewClient = object : WebViewClient() {

            private var isFailure = false

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)

                isFailure = false

                progressBar!!.visibility = View.VISIBLE
                webView!!.visibility = View.INVISIBLE
                errorView!!.visibility = View.INVISIBLE
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)

                if (isFailure) {
                    errorView!!.visibility = View.VISIBLE
                    webView!!.visibility = View.INVISIBLE
                } else {
                    errorView!!.visibility = View.INVISIBLE
                    webView!!.visibility = View.VISIBLE
                }

                progressBar!!.visibility = View.INVISIBLE
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                if (request.isForMainFrame) {
                    isFailure = true
                }
            }


        }

        return view
    }

    companion object {
        /*------------------------------------------------------------------------------------------*/
        //  private members
        /*------------------------------------------------------------------------------------------*/
        private val TAG = "WebViewFragment"
    }

}// Required empty public constructor
