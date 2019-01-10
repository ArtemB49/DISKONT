package com.belyaev.artem.agzs_diskont.trash

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.belyaev.artem.agzs_diskont.R
import com.belyaev.artem.agzs_diskont.model.GazStationParcelable
import com.belyaev.artem.agzs_diskont.model.Purchase
import io.realm.Realm
import io.realm.RealmResults

class ListActivity : AppCompatActivity() {

    public var arrayString: ArrayList<Purchase>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

    }

}
