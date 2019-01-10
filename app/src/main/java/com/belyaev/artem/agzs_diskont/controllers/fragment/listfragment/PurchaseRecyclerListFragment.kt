package com.belyaev.artem.agzs_diskont.controllers.fragment.listfragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.belyaev.artem.agzs_diskont.R
import com.belyaev.artem.agzs_diskont.adapter.PurchaseRecyclerAdapter
import com.belyaev.artem.agzs_diskont.fragment.CARD
import com.belyaev.artem.agzs_diskont.fragment.CARD_NOT_FOUND
import com.belyaev.artem.agzs_diskont.fragment.OPERATION
import com.belyaev.artem.agzs_diskont.utils.AsyDateFormatter
import com.belyaev.artem.agzs_diskont.model.Purchase
import com.belyaev.artem.agzs_diskont.model.Station
import com.belyaev.artem.agzs_diskont.service.AsyService
import com.belyaev.artem.agzs_diskont.service.AsyWebService
import com.belyaev.artem.agzs_diskont.utils.AsyOperationType
import com.belyaev.artem.agzs_diskont.utils.SimpleItemHelperCallback
import io.realm.Realm
import io.realm.Sort
import org.json.JSONArray
import org.json.JSONException
import java.util.*

class PurchaseRecyclerListFragment: Fragment()  {

    private val asyDate = AsyDateFormatter.instance
    private val BROADCAST_ID = "com.artem.agzs_project"
    private val DOCS = "DOCS"
    private val STATION = "STATION"

    private val purchaseList: ArrayList<Purchase> = ArrayList()
    private var purchaseCount = 0
    private val mRealm = Realm.getDefaultInstance()
    private val mAsyService = AsyService.instance
    private lateinit var mPurchaseAdapter: PurchaseRecyclerAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var mMainView: View
    private lateinit var mRecyclerView: RecyclerView

    private val lastVisibleItemPositiom: Int
        get() = mLayoutManager.findLastVisibleItemPosition()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.registerReceiver(broadcastReceiver, IntentFilter(BROADCAST_ID))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        mMainView = inflater.inflate(R.layout.purchase_recycler_fragment, null)

        mLayoutManager = LinearLayoutManager(activity)
        mRecyclerView = mMainView.findViewById(R.id.recyclerView)
        mRecyclerView.layoutManager = mLayoutManager

        initList {
            if (mMainView.context != null){

                updateUI(it)
            }
        }

        setRecyclerViewScrollListener()

        return mMainView
    }

    private fun setItemTouchHelper() {
        val callback: ItemTouchHelper.Callback = SimpleItemHelperCallback(mPurchaseAdapter)
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(mRecyclerView)
    }


    private fun updateUI(list: ArrayList<Purchase>){

        mPurchaseAdapter = PurchaseRecyclerAdapter(list)

        activity?.runOnUiThread{
            mRecyclerView.adapter = mPurchaseAdapter
        }

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

            val docsValue = intent?.getStringExtra(DOCS)
            if (docsValue != null) {
                val jsonArray = parse(docsValue)

                if (jsonArray != null) {
                    if (jsonArray.length() != purchaseCount){
                        mAsyService.createListPurchases(jsonArray, { list ->
                            updateUI(list)
                            //updateRealmPurchase(list)
                        })
                    }

                }
            } else {
                val stationValue = intent?.getIntExtra(STATION, 0)
                if (stationValue == 100){
                    initList {
                        updateUI(it)
                    }
                }
            }
        }

    }

    private fun setRecyclerViewScrollListener() {
        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val totalItemCount = recyclerView!!.layoutManager.itemCount

            }
        })
    }


    private fun initList(completion: (ArrayList<Purchase>) -> Unit) {

        val stations: ArrayList<Station> = ArrayList(mRealm.where(Station::class.java).findAll())
        if (stations.size == 0){
            updateStation()
            return
        }


        val purchases: ArrayList<Purchase> = ArrayList(mRealm.where(Purchase::class.java)
                .sort("date", Sort.DESCENDING)
                .findAll())


        if (purchases.size != 0){
            mRealm.executeTransaction {
                purchases[0].state = true
            }
            completion(purchases)
            purchaseCount = purchases.size

        } else {
            requestToAsy()
        }

    }

    private fun updateStation() {
        val intent = Intent(activity, AsyWebService::class.java)
        intent.putExtra(OPERATION, AsyOperationType.STATIONS.toString())

        activity?.startService(intent)
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

}