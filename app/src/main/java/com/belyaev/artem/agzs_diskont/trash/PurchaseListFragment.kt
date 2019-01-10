package com.belyaev.artem.agzs_diskont.trash

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.ListFragment
import android.util.Log
import android.view.View
import android.widget.ListView
import com.belyaev.artem.agzs_diskont.adapter.PurchaseRecyclerAdapter
import com.belyaev.artem.agzs_diskont.fragment.CARD
import com.belyaev.artem.agzs_diskont.fragment.CARD_NOT_FOUND
import com.belyaev.artem.agzs_diskont.fragment.OPERATION
import com.belyaev.artem.agzs_diskont.utils.AsyDateFormatter
import com.belyaev.artem.agzs_diskont.utils.AsyOperationType
import com.belyaev.artem.agzs_diskont.model.Purchase
import com.belyaev.artem.agzs_diskont.model.Station
import com.belyaev.artem.agzs_diskont.service.AsyService
import com.belyaev.artem.agzs_diskont.service.AsyWebService
import io.realm.Realm
import org.json.JSONArray
import org.json.JSONException
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by Artem on 12.12.2017.
 */
class PurchaseListFragment: ListFragment() {

    private val asyDate = AsyDateFormatter.instance
    private val BROADCAST_ID = "com.artem.agzs_project"
    private val DOCS = "DOCS"
    private var purchaseCount = 0
    private val purchaseList: ArrayList<Purchase> = ArrayList()
    private val mAsyService = AsyService.instance
    private lateinit var adapterRecycler: PurchaseRecyclerAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.registerReceiver(broadcastReceiver, IntentFilter(BROADCAST_ID))

        initList {
            if (context != null){

                updateUI(it)
            }
        }

    }


    private fun initList(completion: (ArrayList<Purchase>) -> Unit) {

        val mRealm = Realm.getDefaultInstance()
        val purchases: ArrayList<Purchase> = ArrayList(mRealm.where(Purchase::class.java).findAll())
        val stations: ArrayList<Station> = ArrayList(mRealm.where(Station::class.java).findAll())

        if (purchases.size != 0 && stations.size != 0){

            completion(purchases)
            purchaseCount = purchases.size
            requestToAsy()
        } else {
            requestToAsy()
        }

    }

    private fun checkForUpdate() {

    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {

        view?.height
        super.onListItemClick(l, v, position, id)


    }

    private fun requestToAsy(){

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity?.applicationContext)
        var cardNumber = sharedPreferences?.getString(CARD, CARD_NOT_FOUND)

        if (cardNumber != null && cardNumber != CARD_NOT_FOUND) {
            cardNumber = cardNumber.substring(6, 12)
            val intent = Intent(activity, AsyWebService::class.java)
            intent.putExtra("card", cardNumber)
            intent.putExtra(OPERATION, AsyOperationType.PRIMARY_DOCS.toString())
            intent.putExtra("start", asyDate.getDate(AsyDateFormatter.AsyRange.START_YEAR))
            intent.putExtra("end", asyDate.getDate(AsyDateFormatter.AsyRange.END_YEAR))
            activity?.startService(intent)
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {

            val value = intent?.getStringExtra(DOCS)
            if (value != null) {
                val jsonArray = parse(value)


                if (jsonArray != null) {
                    if (jsonArray.length() != purchaseCount){
                        mAsyService.createListPurchases(jsonArray, { list ->
                            updateUI(list)
                            updateRealmPurchase(list)
                        })
                    }

                }
            }
        }

    }

    private fun updateUI(list: ArrayList<Purchase>){

        val adapter = PurchaseAdapter(context!!, list)
        activity?.runOnUiThread{
            listAdapter = adapter
        }

    }

    private fun parse(value: String): JSONArray?{
        var result: JSONArray?= null
        try {
            result = JSONArray(value)

        } catch (e: JSONException){
            e.printStackTrace()
        }
        return result
    }

    private fun createFaceList(): ArrayList<Purchase>{
        val list = ArrayList<Purchase>()
        list.add(Purchase(
                Date(),
                Station(17, "Липовая", "01"),
                32.1,
                17.5,
                2.871))
        val purchase1 = Purchase(Date(),
                Station(16, "Рязанка", "04"), 27.1, 16.5, 2.871)

        val  purchase2 = Purchase(Date(),
                Station(15, "Оханск", "05"), 33.1, 18.5, 2.871)

        val purchase3 = Purchase(Date(),
                Station(11, "Краснокамск", "06"), 34.1, 18.5, 2.871)

        val purchase4 = Purchase(Date(),
                Station(12, "Карпинка", "07"), 17.1, 18.5, 2.871)

        val purchase5 = Purchase(Date(),
                Station(13, "Мильчакова", "08"), 18.1, 18.5, 2.871)
        val purchase6 = Purchase(Date(),
                Station(14, "Оса-Гремяча", "09"), 20.1, 18.5, 2.871)
        list.add(purchase1)
        list.add(purchase2)
        list.add(purchase3)
        list.add(purchase4)
        list.add(purchase5)
        list.add(purchase6)

        val realm = Realm.getDefaultInstance()

        realm.executeTransaction { innerRealm ->
            innerRealm.copyToRealm(list)
        }

        return list
    }

    private fun updateRealmPurchase(list: ArrayList<Purchase>){
            val realm = Realm.getDefaultInstance()
            val result = realm.where(Purchase::class.java).findAll()
            val length = result.size - 1
            if (list.size > length) {
                result.deleteAllFromRealm()
                for (item in list) {
                    realm?.executeTransaction { lambdaRealm ->
                        val purchase = lambdaRealm.createObject(Purchase::class.java)
                        purchase?.date = item.date

                        val realmStation = realm.copyToRealmOrUpdate(item.station!!)
                        purchase?.station = realmStation
                        purchase?.gas = item.gas
                        purchase?.price = item.price
                        purchase?.discount = item.discount
                    }
                }
                Log.d("SERVICE", "Purchase list has been update")

            }

    }




}