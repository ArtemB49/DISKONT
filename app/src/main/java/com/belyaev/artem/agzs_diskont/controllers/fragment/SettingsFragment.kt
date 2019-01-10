package com.belyaev.artem.agzs_diskont.controllers.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.belyaev.artem.agzs_diskont.R
import com.belyaev.artem.agzs_diskont.utils.AsyOperationType
import com.belyaev.artem.agzs_diskont.service.AsyWebService


class SettingsFragment : Fragment() {

    private val OPERATION = "OPERATION"


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_purchases, container, false)
        val buttonStation = view.findViewById<Button>(R.id.btn_call)
        val buttonDoc = view.findViewById<Button>(R.id.btn_doc)

        buttonStation.setOnClickListener {
            val intent = Intent(activity, AsyWebService::class.java)
            intent.putExtra(OPERATION, AsyOperationType.STATIONS.toString())

            activity?.startService(intent)
        }

        buttonDoc.setOnClickListener {
            val intent = Intent(activity, AsyWebService::class.java)
            intent.putExtra(OPERATION, AsyOperationType.PRIMARY_DOCS.toString())

            activity?.startService(intent)
        }


        return view
    }


}
