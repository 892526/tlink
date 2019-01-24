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
 * [TutorialLaunchFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [TutorialLaunchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TutorialLaunchFragment : Fragment() {

    /*------------------------------------------------------------------------------------------*/
    //  protected methods
    /*------------------------------------------------------------------------------------------*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_tutorial_launch, container, false)
        initViewPager(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        activity?.title = getString(R.string.SS_81_3200)
    }

    /*------------------------------------------------------------------------------------------*/
    //  private methods
    /*------------------------------------------------------------------------------------------*/
    private fun initViewPager(view: View) {
        val viewPager = view.findViewById<ViewPager>(R.id.fragment_tutorial_launch_view_pager)

        viewPager?.let { viewPager ->
            viewPager.adapter = ViewPagerAdapter(childFragmentManager)
        }

        val tabLayout = view.findViewById<TabLayout>(R.id.fragment_tutorial_launch_tab_layout)
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
                0 -> TutorialLaunchPage1Fragment.newInstance()
                1 -> TutorialLaunchPage2Fragment.newInstance()
                2 -> TutorialLaunchPage3Fragment.newInstance()
                3 -> TutorialLaunchPage4Fragment.newInstance()
                4 -> TutorialLaunchPage5Fragment.newInstance()
                5 -> TutorialLaunchPage6Fragment.newInstance()
                6 -> TutorialLaunchPage7Fragment.newInstance()
                else -> TutorialLaunchPage1Fragment.newInstance()
            }
        }

        /**
         * Get the page num.
         *
         * @return                      Page 数を返す
         */
        override fun getCount(): Int {
            return 7
        }
    }

    /*------------------------------------------------------------------------------------------*/
    //  Companion
    /*------------------------------------------------------------------------------------------*/
    companion object {

        /*------------------------------------------------------------------------------------------*/
        //  private members
        /*------------------------------------------------------------------------------------------*/
        private val TAG = "TutorialLaunchFragment"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment OverviewFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(): TutorialLaunchFragment {
            return TutorialLaunchFragment()
        }
    }

}// Required empty public constructor
