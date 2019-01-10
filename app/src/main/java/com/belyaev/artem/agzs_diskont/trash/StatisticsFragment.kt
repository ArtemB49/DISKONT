package com.belyaev.artem.agzs_diskont.fragment

import android.content.*
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.belyaev.artem.agzs_diskont.R
import com.belyaev.artem.agzs_diskont.utils.AsyDateFormatter
import com.belyaev.artem.agzs_diskont.utils.AsyOperationType
import com.belyaev.artem.agzs_diskont.utils.TurnoverRange
import com.belyaev.artem.agzs_diskont.service.AsyWebService

const val PARAM_RESULT = "result"
const val TURNOVER = "TURNOVER"
const val OPERATION = "OPERATION"
const val CARD = "card"
const val CARD_NOT_FOUND = "card not found"

private val BROADCAST_ID = "com.artem.agzs_project"

class StatisticsFragment: Fragment() {




    private val asyDate = AsyDateFormatter.instance

    var tvDay: TextView? = null
    var tvWeek: TextView? = null
    var tvMonth: TextView? = null
    var tvYear: TextView? = null

    private val broadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val period = intent?.getStringExtra(TURNOVER)
            if (period != null) {
                val turnoverPeriod = TurnoverRange.valueOf(period)
                when (turnoverPeriod) {
                    TurnoverRange.DAY -> {
                        tvDay?.text = intent.getStringExtra(PARAM_RESULT)
                    }
                    TurnoverRange.WEEK -> {
                        tvWeek?.text = intent.getStringExtra(PARAM_RESULT)
                    }
                    TurnoverRange.MONTH -> {
                        tvMonth?.text = intent.getStringExtra(PARAM_RESULT)
                    }
                    TurnoverRange.YEAR -> {
                        tvYear?.text = intent.getStringExtra(PARAM_RESULT)
                    }
                }
            }
        }
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_statistics, null)

        tvDay = view?.findViewById(R.id.tv_day)
        tvWeek = view?.findViewById(R.id.tv_week)
        tvMonth = view?.findViewById(R.id.tv_month)
        tvYear = view?.findViewById(R.id.tv_year)

        activity?.registerReceiver(broadcastReceiver, IntentFilter(BROADCAST_ID))
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity?.applicationContext)
        var cardNumber = sharedPreferences?.getString(CARD, CARD_NOT_FOUND)

        if (cardNumber != null && cardNumber != CARD_NOT_FOUND){
            cardNumber = cardNumber.substring(6, 12)
            getTurnoverOf(TurnoverRange.DAY,cardNumber)
            getTurnoverOf(TurnoverRange.WEEK,cardNumber)
            getTurnoverOf(TurnoverRange.MONTH,cardNumber)
            getTurnoverOf(TurnoverRange.YEAR,cardNumber)

        }

        return view
    }


    private fun getTurnoverOf(period: TurnoverRange, card: String){

        val intent = Intent(activity, AsyWebService::class.java)
        intent.putExtra("card", card)
        intent.putExtra(OPERATION, AsyOperationType.TURNOVER.toString())
        intent.putExtra(TURNOVER, period.toString())

        when(period){
            TurnoverRange.DAY -> {
                intent.putExtra("start", asyDate.startOfDay)
                intent.putExtra("end", asyDate.endOfDay)
            }

            TurnoverRange.WEEK -> {
                intent.putExtra("start", asyDate.startOfWeek)
                intent.putExtra("end", asyDate.endOfWeek)
            }

            TurnoverRange.MONTH -> {
                intent.putExtra("start", asyDate.startOfMonth)
                intent.putExtra("end", asyDate.endOfMonth)
            }

            TurnoverRange.YEAR -> {
                intent.putExtra("start", asyDate.startOfYear)
                intent.putExtra("end", asyDate.endOfYear)
            }
        }

        activity?.startService(intent)
    }



}