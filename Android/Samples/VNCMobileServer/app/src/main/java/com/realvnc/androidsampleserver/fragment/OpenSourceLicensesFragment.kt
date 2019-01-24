package com.realvnc.androidsampleserver.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import com.realvnc.androidsampleserver.R


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [OpenSourceLicensesFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [OpenSourceLicensesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
/*------------------------------------------------------------------------------------------*/
//  public methods
/*------------------------------------------------------------------------------------------*/
class OpenSourceLicensesFragment : WebViewFragment() {

    /*------------------------------------------------------------------------------------------*/
    //  protected methods
    /*------------------------------------------------------------------------------------------*/
    override fun getDisplayUrl(): String {
        return "file:///android_asset/open_source_license.html"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity!!.setTitle(R.string.SS_01_005)
    }

    companion object {

        /*------------------------------------------------------------------------------------------*/
        //  private members
        /*------------------------------------------------------------------------------------------*/
        private val TAG = "OpenSourceLicensesFragment"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment OverviewFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(): OpenSourceLicensesFragment {
            return OpenSourceLicensesFragment()
        }
    }
}// Required empty public constructor
