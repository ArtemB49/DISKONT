package com.belyaev.artem.agzs_diskont.trash

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.belyaev.artem.agzs_diskont.R
import com.belyaev.artem.agzs_diskont.model.Purchase
import java.text.SimpleDateFormat
import java.util.*

class PurchaseAdapter (
        private val context: Context,
        private val list: ArrayList<Purchase>

) : BaseAdapter(){


    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val rowView: View
        val holder: ViewHolder

        if (convertView == null) {

            rowView = inflater.inflate(R.layout.list_item_purchase_simple, parent, false)

            holder = ViewHolder()
            holder.dateTextView = rowView.findViewById(R.id.item_tv_date)
            holder.stationTextView = rowView.findViewById(R.id.item_tv_station)
            //holder.countTextView = rowView.findViewById(R.id.item_tv_count)
            //holder.priceTextView = rowView.findViewById(R.id.item_tv_price)
            holder.sumTextView = rowView.findViewById(R.id.item_tv_sum)

            rowView.tag = holder
        } else {
            rowView = convertView
            holder = convertView.tag as ViewHolder
        }

        val purchase = getItem(position) as Purchase

        val dateTextView = holder.dateTextView
        val stationTextView = holder.stationTextView
        //val countTextView = holder.countTextView
        //val priceTextView = holder.priceTextView
        val sumTextView = holder.sumTextView

        dateTextView.text =
                SimpleDateFormat("dd.MM.yyyy", Locale.FRANCE).format(purchase.date)
        stationTextView.text = purchase.station?.name
        //countTextView.text = purchase.gas.toString()
        //priceTextView.text = purchase.price.toString()
        sumTextView.text = "%.2f".format(purchase.price * purchase.gas)


        return rowView
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return  list.size
    }


    private class ViewHolder{
        lateinit var dateTextView: TextView
        lateinit var stationTextView: TextView
        //lateinit var countTextView: TextView
        //lateinit var priceTextView: TextView
        lateinit var sumTextView: TextView
    }
}