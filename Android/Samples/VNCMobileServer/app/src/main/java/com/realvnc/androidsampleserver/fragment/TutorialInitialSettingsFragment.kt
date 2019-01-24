package com.realvnc.androidsampleserver.fragment

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.realvnc.androidsampleserver.R

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [TutorialInitialSettingsFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [TutorialInitialSettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TutorialInitialSettingsFragment : Fragment() {

    /*------------------------------------------------------------------------------------------*/
    //  protected methods
    /*------------------------------------------------------------------------------------------*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_tutorial_initial_settings, container, false)
        initViewPager(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        activity?.title = getString(R.string.TID_5302)
    }

    /*------------------------------------------------------------------------------------------*/
    //  private methods
    /*------------------------------------------------------------------------------------------*/
    private fun initViewPager(view: View) {
        val viewPager = view.findViewById<ViewPager>(R.id.fragment_tutorial_initial_settings_view_pager)

        viewPager?.let { viewPager ->
            viewPager.adapter = ViewPagerAdapter(childFragmentManager)
        }

        val tabLayout = view.findViewById<TabLayout>(R.id.fragment_tutorial_initial_settings_tab_layout)
        tabLayout?.setupWithViewPager(viewPager, true)
    }

    /*------------------------------------------------------------------------------------------*/
    //  Adapter for ViewPager
    /*------------------------------------------------------------------------------------------*/
    inner class ViewPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {
        /**
         * Called when item is selected.
         *
         * @param[position]             Selected item position.
         * @return                      Fragment object.
         */
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> TutorialInitialSettingsPage2Fragment.newInstance()
                1 -> TutorialInitialSettingsPage3Fragment.newInstance()
                else -> TutorialInitialSettingsPage2Fragment.newInstance()
            }
        }

        /**
         * Get the page num.
         *
         * @return                      Page 数を返す
         */
        override fun getCount(): Int {
            return 2
        }
    }

    /*------------------------------------------------------------------------------------------*/
    //  Companion
    /*------------------------------------------------------------------------------------------*/
    companion object {

        /*------------------------------------------------------------------------------------------*/
        //  private members
        /*------------------------------------------------------------------------------------------*/
        private val TAG = "TutorialInitialSettingsFragment"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment OverviewFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(): TutorialInitialSettingsFragment {
            return TutorialInitialSettingsFragment()
        }
    }

}// Required empty public constructor
