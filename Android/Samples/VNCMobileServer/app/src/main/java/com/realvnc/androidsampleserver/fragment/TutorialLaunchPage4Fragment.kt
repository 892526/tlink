package com.realvnc.androidsampleserver.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.realvnc.androidsampleserver.R

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [TutorialLaunchPage4Fragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [TutorialLaunchPage4Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TutorialLaunchPage4Fragment : Fragment() {

    /*------------------------------------------------------------------------------------------*/
    //  protected methods
    /*------------------------------------------------------------------------------------------*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_tutorial_launch_page4, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    /*------------------------------------------------------------------------------------------*/
    //  Companion
    /*------------------------------------------------------------------------------------------*/
    companion object {

        /*------------------------------------------------------------------------------------------*/
        //  private members
        /*------------------------------------------------------------------------------------------*/
        private val TAG = "TutorialLaunchPage4Fragment"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment OverviewFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(): TutorialLaunchPage4Fragment {
            return TutorialLaunchPage4Fragment()
        }
    }

}// Required empty public constructor
