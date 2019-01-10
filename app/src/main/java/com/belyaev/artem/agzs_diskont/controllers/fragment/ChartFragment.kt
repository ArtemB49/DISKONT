package com.belyaev.artem.agzs_diskont.controllers.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.belyaev.artem.agzs_diskont.R
import com.belyaev.artem.agzs_diskont.model.Purchase
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import io.realm.Realm
import java.util.*
import kotlin.collections.ArrayList

class ChartFragment: Fragment(){

    private lateinit var mMainView: View
    private lateinit var mBarChart: BarChart
    private val mRealm: Realm = Realm.getDefaultInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mMainView = inflater.inflate(R.layout.fragment_chart, null)
        mBarChart = mMainView.findViewById(R.id.bar_chart)
        mBarChart.description.isEnabled = false

        setData()

        mBarChart.setFitBars(true)

        return mMainView

    }

    private fun setData() {

        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)

        val chartDataList = ArrayList<BarEntry>()
        val labelsList = ArrayList<String>()


        for (item in 0..currentMonth){
            val month = when (item){
                0-> Month.JANUARY
                1-> Month.FEBRUARY
                2-> Month.MARCH
                3-> Month.APRIL
                4-> Month.MAY
                5-> Month.JUNE
                6-> Month.JULY
                7-> Month.AUGUST
                8-> Month.SEPTEMBER
                9-> Month.OCTOBER
                10-> Month.NOVEMBER
                11-> Month.DECEMBER
                else -> Month.JANUARY
            }
            labelsList.add(month.label)
            chartDataList.add(BarEntry((0.5+item).toFloat(), getData(month, 2018)))
        }




        val set = BarDataSet(chartDataList, "Месяц")
        set.setColors(ColorTemplate.MATERIAL_COLORS, 100)
        set.setDrawValues(true)
        val barData = BarData(set)

        mBarChart.data = barData
        mBarChart.xAxis.valueFormatter = IndexAxisValueFormatter(labelsList)
        mBarChart.xAxis.granularity = 1f
        mBarChart.xAxis.setCenterAxisLabels(true)
        mBarChart.xAxis.axisMinimum = 0f
        mBarChart.legend.isEnabled = false
        mBarChart.invalidate()
        mBarChart.animateY(500)

    }

    private fun getData(month: Month, year: Int): Float{

        val dates = getDate(month, year)
        return getSum(dates.first, dates.second)
    }

    private fun getSum(start: Date, end: Date): Float{
        val purchases: ArrayList<Purchase> = ArrayList(mRealm.where(Purchase::class.java)
                .between("date", start, end)
                .findAll())
        var result = 0f
        for (item in purchases){
            result += item.gas.toFloat()
        }
        return result
    }

    private fun getDate(month: Month, year: Int): Pair<Date, Date>{
        val calendar = Calendar.getInstance(Locale.FRANCE)
        calendar.set(Calendar.YEAR, year)
        val monthInt: Int = month.number
        calendar.set(Calendar.MONTH, monthInt)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val start = calendar.time

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val end = calendar.time

        return Pair(start, end)
    }

    enum class Month(val number: Int, val label: String){
        JANUARY(0, "Январь"),
        FEBRUARY(1, "Февраль"),
        MARCH(2, "Март"),
        APRIL(3, "Апрель"),
        MAY(4, "Май"),
        JUNE(5, "Июнь"),
        JULY(6, "Июль"),
        AUGUST(7, "Август"),
        SEPTEMBER(8, "Сентябрь"),
        OCTOBER(9, "Октябрь"),
        NOVEMBER(10, "Ноябрь"),
        DECEMBER(11, "Декабрь")
    }

    inner class MyXAxisValueFormatter(val list: ArrayList<String>): IAxisValueFormatter{

        override fun getFormattedValue(value: Float, axis: AxisBase?): String {
            return list[value.toInt()]
        }
    }

}