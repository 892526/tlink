package com.realvnc.androidsampleserver.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [OverviewFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [OverviewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OverviewFragment : WebViewFragment() {

    /*------------------------------------------------------------------------------------------*/
    //  protected methods
    /*------------------------------------------------------------------------------------------*/
    override fun getDisplayUrl(): String {
        return "http://www2.jvckenwood.com/cs/ce/mdvpj/kmi_summary.htm"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //activity!!.setTitle(R.string.overview)
    }

    companion object {

        /*------------------------------------------------------------------------------------------*/
        //  private members
        /*------------------------------------------------------------------------------------------*/
        private val TAG = "OverviewFragment"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment OverviewFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(): OverviewFragment {
            return OverviewFragment()
        }
    }

}// Required empty public constructor
