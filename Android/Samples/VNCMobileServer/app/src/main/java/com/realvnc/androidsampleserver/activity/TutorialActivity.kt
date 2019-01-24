package com.realvnc.androidsampleserver.activity

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem

import com.realvnc.androidsampleserver.R
import com.realvnc.androidsampleserver.fragment.*

class TutorialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        // << set action bar >>
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        if (supportFragmentManager.backStackEntryCount != 0) {
            val fragment = supportFragmentManager.fragments[0]

            supportFragmentManager.beginTransaction()
                    .replace(R.id.content_dummy, fragment)
                    .commit()
        }
        else {
            setSettingsFragment()
        }
0    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                if (supportFragmentManager.backStackEntryCount != 0) {
                    supportFragmentManager.popBackStack()
                }
                else {
                    finish()
                }
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    /*------------------------------------------------------------------------------------------*/
    fun onBluetoothTutorialClicked() {
        val fragment = TutorialBluetoothFragment.newInstance()
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(R.id.content_dummy, fragment)
                .addToBackStack(null)
                .commit()
    }

    fun onLaunchTutorialClicked() {
        val fragment = TutorialLaunchFragment.newInstance()
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(R.id.content_dummy, fragment)
                .addToBackStack(null)
                .commit()
    }

    fun onStartTutorialClicked() {
        val fragment = TutorialStartFragment.newInstance()
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(R.id.content_dummy, fragment)
                .addToBackStack(null)
                .commit()
    }


    /*------------------------------------------------------------------------------------------*/
    //  private methods
    /*------------------------------------------------------------------------------------------*/
    private fun setSettingsFragment() {
        val fragment = TutorialFragment.newInstance()
        supportFragmentManager.beginTransaction()
                .replace(R.id.content_dummy, fragment)
                .commit()
    }
}
