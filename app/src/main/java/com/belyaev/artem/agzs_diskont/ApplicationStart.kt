package com.belyaev.artem.agzs_diskont

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

class ApplicationStart: Application() {

    override fun onCreate() {
        super.onCreate()

        Realm.init(this)


        val configuration = RealmConfiguration.Builder()
                .name("agzs3-db")
                .deleteRealmIfMigrationNeeded()
                .build()

        Realm.setDefaultConfiguration(configuration)

    }
}