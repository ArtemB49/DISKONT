package com.belyaev.artem.agzs_diskont.controllers

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.belyaev.artem.agzs_diskont.R
import com.belyaev.artem.agzs_diskont.controllers.fragment.BarcodeFragment
import com.belyaev.artem.agzs_diskont.controllers.fragment.ChartFragment
import com.belyaev.artem.agzs_diskont.controllers.fragment.SettingsFragment
import com.belyaev.artem.agzs_diskont.controllers.fragment.listfragment.PurchaseRecyclerListFragment
import com.belyaev.artem.agzs_diskont.utils.AsyDateFormatter
import kotlinx.android.synthetic.main.activity_navigation.*

class NavigationActivity : AppCompatActivity(),
        BottomNavigationView.OnNavigationItemSelectedListener {

    private var currentID: Int = R.id.navigation_barcode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        navigation.setOnNavigationItemSelectedListener(this)
        loadFragment(BarcodeFragment())

        AsyDateFormatter.instance
    }

    private fun loadFragment(fragment: Fragment?): Boolean{
        if (fragment != null){
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit()
            return true
        }

        return false

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        if (item.itemId == currentID){
            return false
        }

        currentID = item.itemId

        var fragment: Fragment? = null

        when (item.itemId){
            R.id.navigation_barcode -> fragment = BarcodeFragment()
            R.id.navigation_statistics -> fragment = ChartFragment()
            R.id.navigation_list -> fragment = PurchaseRecyclerListFragment()
            R.id.navigation_settings -> fragment = SettingsFragment()
        }

        return loadFragment(fragment)
    }


}
