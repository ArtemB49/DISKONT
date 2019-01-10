package com.belyaev.artem.agzs_diskont.service

import android.util.Log
import com.belyaev.artem.agzs_diskont.model.Purchase
import com.belyaev.artem.agzs_diskont.model.Station
import io.realm.Realm
import org.json.JSONArray
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class AsyService private constructor() {

    private object Holder {
        val INSTANCE = AsyService()
    }

    companion object {
        val instance: AsyService by lazy { Holder.INSTANCE }

    }

    fun createListPurchases(jsonArray: JSONArray, completion:(ArrayList<Purchase>)->Unit){
        thread {
            val list: ArrayList<Purchase> = ArrayList()
            val length = jsonArray.length() - 1
            for (i in (0..length)) {
                try {
                    val itemArray: JSONArray = jsonArray.getJSONArray(i)
                    val dateFormatter = SimpleDateFormat("dd.MM.yyyy ", Locale.FRANCE)
                    val pDate: Date = dateFormatter.parse(itemArray[0].toString())
                    var realmStation: Station? = null
                    val realm = Realm.getDefaultInstance()
                    realm.executeTransaction({
                        realmStation = it
                                .where(Station::class.java)
                                .equalTo("idOfService", itemArray[1].toString())
                                .findFirst()
                    })
                    val pStation = realm.copyFromRealm(realmStation!!)
                    val pGas: Double = itemArray[2].toString().toDouble()
                    val pPrice: Double = itemArray[3].toString().toDouble()
                    val pDiscount: Double = itemArray[4].toString().toDouble()

                    val purchase = Purchase(pDate, pStation, pGas, pPrice, pDiscount)


                    list.add(purchase)
                } catch (jEx: JSONException) {
                    jEx.printStackTrace()
                }
            }
            completion(list)
        }
    }

    fun createListStation(jsonArray: JSONArray, completion:(List<Station>)->Unit){
        thread {
            val list: MutableList<Station> = mutableListOf()
            val length = jsonArray.length() - 1
            for (i in (0..length)) {
                try {
                    val itemArray: JSONArray = jsonArray.getJSONArray(i)!!
                    val id: Long = itemArray[0].toString().toLong()
                    val idOfService = itemArray[0].toString()
                    val station = Station(id)
                    val name = itemArray[1].toString()
                    station.name = name
                    station.idOfService = idOfService
                    list.add(station)
                } catch (jEx: JSONException) {
                    jEx.printStackTrace()
                }
            }
            completion(list)
        }
    }

    fun updateRealmStation(list: List<Station>){
        thread {
            val realm = Realm.getDefaultInstance()
            val result = realm.where(Station::class.java).findAll()
            val length = result.size - 1
            if (list.size > length){
                for (item in list){
                    val pointRes = realm.where(Station::class.java)
                            .equalTo("idOfRealm", item.idOfRealm)
                            .findFirst()
                    if (pointRes == null) {
                        realm?.executeTransactionAsync{ lambdaRealm ->
                            val station = lambdaRealm.createObject(Station::class.java, item.idOfRealm)
                            station?.name = item.name
                            station?.idOfService = item.idOfService
                        }
                    }
                }
                Log.d("REALM", "Station list has been update")
            }
        }
    }


    fun updateRealmPurchase(list: ArrayList<Purchase>){
        thread {
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
                Log.d("REALM", "Purchase list has been update")

            }
        }


    }
}