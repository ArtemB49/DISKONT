package com.belyaev.artem.agzs_diskont.adapter

import android.support.v7.widget.RecyclerView
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import com.belyaev.artem.agzs_diskont.R
import com.belyaev.artem.agzs_diskont.model.Purchase
import com.belyaev.artem.agzs_diskont.utils.inflate
import io.realm.Realm

class PurchaseRecyclerAdapter(private val purchases: ArrayList<Purchase>):
        RecyclerView.Adapter<PurchaseRecyclerViewHolder>(){

    private var mChangePosition: Int = 0
    private var mPreviousChangePosition: Int = -1
    private val mRealm = Realm.getDefaultInstance()
    private lateinit var mRecyclerView: RecyclerView

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurchaseRecyclerViewHolder {

        val inflatedView = parent.inflate(R.layout.list_item_purchase, false)

        return PurchaseRecyclerViewHolder(inflatedView)

    }

    override fun onBindViewHolder(holder: PurchaseRecyclerViewHolder, position: Int) {

        val isChanced = position==mChangePosition
        val purchase = purchases[position]
        holder.bindData(purchase)
        if (isChanced){
            holder.moreInformationView.visibility = View.VISIBLE
            holder.baseSumTextView.visibility = View.GONE
            mPreviousChangePosition = position
        } else {
            holder.moreInformationView.visibility = View.GONE
            holder.baseSumTextView.visibility = View.VISIBLE
        }


        holder.mainView.isActivated = isChanced
        holder.mainView.setOnClickListener {
            mChangePosition = if (isChanced) -1 else position
            TransitionManager.beginDelayedTransition(mRecyclerView)
            notifyItemChanged(mPreviousChangePosition)
            notifyItemChanged(mChangePosition)

        }

    }



    override fun getItemCount() = purchases.size


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
    }

}


/* Если реализовывать через разные layout

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurchaseRecyclerViewHolder {


        when (viewType){
            0 -> {
                val inflatedView = parent.inflate(R.layout.list_item_purchase_simple, false)
                val selfHolder = PurchaseRecyclerViewHolder(inflatedView)
                inflatedView.setOnClickListener{
                    val position = selfHolder.adapterPosition
                    mRealm.executeTransaction {
                        purchases[mChangePosition].state = false
                        purchases[position].state = true
                    }
                    onItemSelected(it)
                    mChangePosition = position
                    notifyDataSetChanged()
                }
                return selfHolder
            }

            1 -> {
                val inflatedView = parent.inflate(R.layout.list_item_purchase, false)
                val changeHolder = PurchaseRecyclerViewHolder(inflatedView)
                inflatedView.setOnClickListener{
                    val position = changeHolder.adapterPosition
                    mRealm.executeTransaction {
                        purchases[position].state = false
                    }
                    onItemClear(it)
                    notifyDataSetChanged()
                }
                return changeHolder
            }
        }
        val inflatedView = parent.inflate(R.layout.list_item_purchase_simple, false)
        return PurchaseRecyclerViewHolder(inflatedView)
    }

        override fun getItemViewType(position: Int): Int {

        if (purchases[position].state != null && purchases[position].state != false){
            return 1
        } else {
            return 0
        }


    }
    */
