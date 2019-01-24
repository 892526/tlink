package com.realvnc.androidsampleserver.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [TermsOfServieFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [TermsOfServieFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
/*------------------------------------------------------------------------------------------*/
//  public methods
/*------------------------------------------------------------------------------------------*/
class TermsOfServieFragment() : WebViewFragment() {

    /*------------------------------------------------------------------------------------------*/
    //  protected methods
    /*------------------------------------------------------------------------------------------*/
    override fun getDisplayUrl(): String {
        return "http://www2.jvckenwood.com/cs/ce/mdvpj/kmi_use_policy.htm"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //activity!!.setTitle(R.string.tos)
    }

    companion object {

        /*------------------------------------------------------------------------------------------*/
        //  private members
        /*------------------------------------------------------------------------------------------*/
        private val TAG = "TermsOfServieFragment"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment OverviewFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(): TermsOfServieFragment {
            return TermsOfServieFragment()
        }
    }
}// Required empty public constructor
