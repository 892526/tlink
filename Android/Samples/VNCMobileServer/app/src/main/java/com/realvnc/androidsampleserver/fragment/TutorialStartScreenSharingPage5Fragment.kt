package com.realvnc.androidsampleserver.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.realvnc.androidsampleserver.R
import com.realvnc.jvckenwood.util.LayoutUtility

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [TutorialStartScreenSharingPage5Fragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [TutorialStartScreenSharingPage5Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TutorialStartScreenSharingPage5Fragment : Fragment() {

    /*------------------------------------------------------------------------------------------*/
    //  protected methods
    /*------------------------------------------------------------------------------------------*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_tutorial_start_screen_sharing_page5, container, false)
        if (LayoutUtility.isRTL()) {
            view.rotationY = 180f
        }
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
        private val TAG = "TutorialStartScreenSharingPage5Fragment"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment OverviewFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(): TutorialStartScreenSharingPage5Fragment {
            return TutorialStartScreenSharingPage5Fragment()
        }
    }

}// Required empty public constructor
