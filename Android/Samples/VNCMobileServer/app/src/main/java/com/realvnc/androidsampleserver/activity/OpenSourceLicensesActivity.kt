package com.realvnc.androidsampleserver.activity

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem

import com.realvnc.androidsampleserver.R
import com.realvnc.androidsampleserver.fragment.OpenSourceLicensesFragment

class OpenSourceLicensesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_source_licenses)

        // << set action bar >>
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        setSettingsFragment()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    /*------------------------------------------------------------------------------------------*/
    //  private methods
    /*------------------------------------------------------------------------------------------*/
    private fun setSettingsFragment() {
        val fragment = OpenSourceLicensesFragment.newInstance()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.content_dummy, fragment)
        fragmentTransaction.commit()
    }
}
