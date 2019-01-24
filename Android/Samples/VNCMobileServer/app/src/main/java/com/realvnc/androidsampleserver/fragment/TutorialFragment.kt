package com.realvnc.androidsampleserver.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.realvnc.androidsampleserver.R
import com.realvnc.androidsampleserver.activity.TutorialActivity
import kotlinx.android.synthetic.main.fragment_tutorial.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [TutorialFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [TutorialFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TutorialFragment : Fragment() {

    /*------------------------------------------------------------------------------------------*/
    //  protected methods
    /*------------------------------------------------------------------------------------------*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_tutorial, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }

    override fun onResume() {
        super.onResume()
        activity?.title = getString(R.string.tutorial_title)
    }

    /*------------------------------------------------------------------------------------------*/
    private fun initView(view: View) {
        val tutorialBluetooth = view.findViewById<Button>(R.id.fragment_tutorial_bt)
        tutorialBluetooth?.setOnClickListener {
            (activity as? TutorialActivity)?.onBluetoothTutorialClicked()
        }

        val tutorialLaunch = view.findViewById<Button>(R.id.fragment_tutorial_launch)
        tutorialLaunch?.setOnClickListener {
            (activity as? TutorialActivity)?.onLaunchTutorialClicked()
        }

        val tutorialStart = view.findViewById<Button>(R.id.fragment_tutorial_start)
        tutorialStart?.setOnClickListener {
            (activity as? TutorialActivity)?.onStartTutorialClicked()
        }
    }

    /*------------------------------------------------------------------------------------------*/
    companion object {

        /*------------------------------------------------------------------------------------------*/
        //  private members
        /*------------------------------------------------------------------------------------------*/
        private val TAG = "TutorialFragment"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment OverviewFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(): TutorialFragment {
            return TutorialFragment()
        }
    }

}// Required empty public constructor
