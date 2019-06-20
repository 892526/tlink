package com.realvnc.androidsampleserver.activity

import android.content.Intent
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.realvnc.androidsampleserver.R
import com.realvnc.androidsampleserver.SampleIntents
import com.realvnc.androidsampleserver.VncServerApp
import com.realvnc.androidsampleserver.fragment.TermsOfServieFragment
import kotlinx.android.synthetic.main.activity_terms_of_service.*


class TermsOfServiceActivity : AppCompatActivity() {

    var displayAgreement: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_of_service)

        displayAgreement = intent.getBooleanExtra("RequestAgreement", false)

        // << set action bar >>
        if (!displayAgreement) {
            val actionBar = supportActionBar
            actionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()

        // フラグメント表示
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

    override fun onBackPressed() {
        if (displayAgreement) {
            moveTaskToBack(true)
        }
        else {
            super.onBackPressed()
        }
    }

    /*------------------------------------------------------------------------------------------*/
    //  private methods
    /*------------------------------------------------------------------------------------------*/
    private fun setSettingsFragment() {

        val fragment = TermsOfServieFragment.newInstance(displayAgreement)
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.content_dummy, fragment)
        fragmentTransaction.commit()
    }
}
