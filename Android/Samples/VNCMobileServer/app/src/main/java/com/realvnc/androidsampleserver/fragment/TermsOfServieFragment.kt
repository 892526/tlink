package com.realvnc.androidsampleserver.fragment

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ProgressBar
import com.realvnc.androidsampleserver.R
import com.realvnc.androidsampleserver.VncServerApp
import com.realvnc.androidsampleserver.activity.VNCMobileServerProxy
import com.realvnc.jvckenwood.controls.WebViewCustom

class TermsOfServieFragment : Fragment() {

    /*------------------------------------------------------------------------------------------*/
    //  Companion
    /*------------------------------------------------------------------------------------------*/
    companion object {
        /*------------------------------------------------------------------------------------------*/
        //  Variables
        /*------------------------------------------------------------------------------------------*/
        private val TAG = "TermsOfServieFragment"

        private val KEY_DISPLAY_AGREEMENT = "DisplayAgreement"

        /*------------------------------------------------------------------------------------------*/
        //  Functions
        /*------------------------------------------------------------------------------------------*/
        fun newInstance(displayAgreement: Boolean = false): TermsOfServieFragment {
            val fragment = TermsOfServieFragment()

            val bundle = Bundle()
            bundle.putBoolean(KEY_DISPLAY_AGREEMENT, displayAgreement)
            fragment.arguments = bundle

            return fragment
        }
    }

    /*------------------------------------------------------------------------------------------*/
    //  Variables
    /*------------------------------------------------------------------------------------------*/
    private var displayAgreement = false

    /*------------------------------------------------------------------------------------------*/
    //  Lifecycle methods
    /*------------------------------------------------------------------------------------------*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        displayAgreement = arguments?.getBoolean(KEY_DISPLAY_AGREEMENT) ?: false
    }

    /*------------------------------------------------------------------------------------------*/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity!!.setTitle(R.string.TID_5227)
    }

    /*------------------------------------------------------------------------------------------*/
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_terms_of_service, container, false)

        val page = view.findViewById<View>(R.id.fragment_terms_of_service_page)
        val progressBar = view.findViewById<ProgressBar>(R.id.fragment_terms_of_service_progressbar)
        val webView = view.findViewById<WebViewCustom>(R.id.fragment_terms_of_service_webview)
        val agreeButton = view.findViewById<Button>(R.id.fragment_terms_of_service_agree_button)

        // << Load the page >>
        val file = "file:///android_asset/terms_of_service/${getString(R.string.terms_of_service)}"
        webView.loadUrl(file)

        // << Set the WebViewClient >>
        webView.webViewClient = object : WebViewClient() {

            private var isFailure = false

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                isFailure = false

                progressBar.visibility = View.VISIBLE
                page.visibility = View.INVISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                if (isFailure) {
                    agreeButton.visibility = View.GONE
                }
                else {
                    if (displayAgreement) {
                        agreeButton.visibility = View.VISIBLE
                    }
                    else {
                        agreeButton.visibility = View.GONE
                    }
                }
                page.visibility = View.VISIBLE
                progressBar.visibility = View.INVISIBLE
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                isFailure = request?.isForMainFrame ?: true
            }
        }

        // << Agreement >>
        if (displayAgreement) {
            agreeButton.isEnabled = false
            webView.setScrollListener(object : WebViewCustom.ScrollListener {
                override fun onScrolledEnd() {
                    agreeButton.isEnabled = true
                }
            })
            agreeButton.setOnClickListener {
                VncServerApp.doAgreement()

                val intent = Intent(context, VNCMobileServerProxy::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)

                activity?.finish()
            }
        }

        return view
    }

    /*
    private fun restart(context: Context, period: Int) {
        val intent = Intent(context, VNCMobileServerProxy::class.java)

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        alarmManager?.let {
            val trigger = System.currentTimeMillis() + period
            alarmManager.setExact(AlarmManager.RTC, trigger, pendingIntent)
        }

        activity?.finish()
    }
    */


}// Required empty public constructor
