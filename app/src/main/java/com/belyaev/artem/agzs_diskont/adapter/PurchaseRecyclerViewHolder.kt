package com.belyaev.artem.agzs_diskont.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.belyaev.artem.agzs_diskont.R
import com.belyaev.artem.agzs_diskont.model.Purchase
import kotlinx.android.synthetic.main.list_item_purchase.view.*
import java.text.SimpleDateFormat
import java.util.*

class PurchaseRecyclerViewHolder (itemView: View ):
        RecyclerView.ViewHolder(itemView),
        View.OnClickListener{

    var mainView: View = itemView
    private val purchase: Purchase? = null
    private lateinit var mContext: Context
    var moreInformationView: LinearLayout
    var baseSumTextView: TextView


    init {
        itemView.setOnClickListener(this)
        mContext = itemView.context
        moreInformationView = itemView.findViewById(R.id.ll_more_info_purchase)
        baseSumTextView = itemView.findViewById(R.id.tv_base_sum)
    }

    override fun onClick(v: View?) {
        Log.d("ITEM", "onClick")

    }

    fun bindData(purchase: Purchase){
        mainView.item_tv_date.text =
                SimpleDateFormat("dd.MM.yyyy", Locale.FRANCE).format(purchase.date)
        mainView.item_tv_station.text = purchase.station?.name
        mainView.item_tv_price.text = purchase.price.toString()
        mainView.item_tv_count.text = purchase.gas.toString()
        mainView.item_tv_sum.text = "%.2f \u20BD".format(purchase.price * purchase.gas)
        mainView.tv_base_sum.text = mainView.item_tv_sum.text
    }

}