package com.belyaev.artem.agzs_diskont.controllers

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isLogin = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                .getBoolean("isLogin", false)

        if (isLogin){
            startActivity(Intent(this, NavigationActivity::class.java))
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }



        finish()
    }
}
