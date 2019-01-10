package com.belyaev.artem.agzs_diskont.controllers.fragment

import android.app.Activity
import android.app.Fragment
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.belyaev.artem.agzs_diskont.R
import com.belyaev.artem.agzs_diskont.controllers.EAN13ScanActivity
import com.belyaev.artem.agzs_diskont.utils.OnFragmentInteractionListener
import com.google.android.gms.vision.barcode.Barcode

/**
 * Created by Artem on 19.02.2018.
 */

class FragmentCardScan: Fragment() {

    private var btnScanEAN13: Button? = null
    private var tvCardNumber: TextView? = null
    private var llButtonScan: LinearLayout? = null
    private val requestCode = 100
    private lateinit var mDataPasser: OnFragmentInteractionListener


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_card_scan, container, false)
        mDataPasser = activity as OnFragmentInteractionListener

        btnScanEAN13 = view?.findViewById(R.id.btn_scan_EAN13)
        btnScanEAN13?.setOnClickListener {
            val intent = Intent(activity,
                    EAN13ScanActivity::class.java)
            startActivityForResult(intent, requestCode)
        }
        tvCardNumber = view?.findViewById(R.id.tv_card_number)
        llButtonScan = view?.findViewById(R.id.ll_btn_scan)

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == this.requestCode && resultCode == Activity.RESULT_OK){
            if (data != null){
                val barcode = data.getParcelableExtra<Barcode>("barcode")
                tvCardNumber?.post{
                    val result = barcode.displayValue

                    updateTextView(result)
                    slideToTop(llButtonScan)

                    mDataPasser.onFragmentInteraction(1 , arrayOf(result))
                }
            }
        } else if (resultCode == 66){
            Snackbar
                    .make(activity.findViewById(R.id.main_content),
                    "Не установлены Сервисы Google Play\nНажмите чтобы установить",
                            Snackbar.LENGTH_LONG)
                    .setAction("UNDO", {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse("market://details?id=com.google.android.gms&hl=ru")
                        startActivity(intent)
                    })
                    .show()
        }
    }

    private fun updateTextView(value: String){

        tvCardNumber?.text = value.substring(6, 12)
        val color: Int?

        if (Build.VERSION.SDK_INT in 1..22){
            color = resources.getColor(R.color.tv_card_number_text)
        } else {
            color = ContextCompat.getColor(context, R.color.tv_card_number_text)
        }

        tvCardNumber?.setTextColor(color)
        tvCardNumber?.setTextSize(40f)
    }


    private fun slideToTop(layout: LinearLayout?){
        if (layout != null){
            val animation = TranslateAnimation((0).toFloat(), (0).toFloat(), (0).toFloat(),
                    (-layout.height).toFloat())
            animation.duration = 500
            animation.fillAfter = true
            layout.startAnimation(animation)
            for (i in 0 .. layout.childCount ){
                val child:View? = layout.getChildAt(i)
                child?.visibility = View.GONE
            }
            layout.visibility = View.GONE
            layout.visibility = View.INVISIBLE
        }
    }
}