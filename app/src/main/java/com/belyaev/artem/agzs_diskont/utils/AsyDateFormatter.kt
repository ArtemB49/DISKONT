package com.belyaev.artem.agzs_diskont.utils

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class AsyDateFormatter private constructor(){

    private lateinit var dateFormatter: DateFormat

    // День
    var startOfDay: Date? = null
    var endOfDay: Date? = null
    // Неделя
    var startOfWeek: Date? = null
    var endOfWeek: Date? = null
    // Месяц
    var startOfMonth: Date? = null
    var endOfMonth: Date? = null
    // Год
    var startOfYear: Date? = null
    var endOfYear: Date? = null

    init {


        dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val groupDate = ThreadGroup("AsyDate")
        thread {
            initDay()
            initWeek()
            initMonth()
            initYear()
        }

    }

    private object Holder { val INSTANCE = AsyDateFormatter() }
    companion object {
        val instance: AsyDateFormatter by lazy { Holder.INSTANCE }
    }

    private fun getCalendar(): Calendar {
        val calendar = GregorianCalendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.clear(Calendar.MINUTE)
        calendar.clear(Calendar.SECOND)
        calendar.clear(Calendar.MILLISECOND)
        return calendar
    }


    private fun initDay(){

            val calendarD = getCalendar()
            endOfDay = calendarD.time

            calendarD.add(Calendar.DAY_OF_WEEK, -1)
            startOfDay = calendarD.time

    }

    private fun initWeek(){

            val calendarEW = getCalendar()
            calendarEW.set(Calendar.DAY_OF_WEEK, calendarEW.getActualMaximum(Calendar.DAY_OF_WEEK) + 1)
            endOfWeek = calendarEW.time

            val calendarSW = getCalendar()
            calendarSW.set(Calendar.DAY_OF_WEEK, calendarSW.firstDayOfWeek)
            startOfWeek = calendarSW.time

    }

    private fun initMonth(){

            val calendarEM = getCalendar()
            calendarEM.set(Calendar.DAY_OF_MONTH, calendarEM.getActualMaximum(Calendar.DAY_OF_MONTH))
            endOfMonth = calendarEM.time

            val calendarSM = getCalendar()
            calendarSM.set(Calendar.DAY_OF_MONTH, calendarSM.getActualMinimum(Calendar.DAY_OF_MONTH))
            startOfMonth = calendarSM.time

    }

    private fun initYear(){

            val calendarEY = getCalendar()
            calendarEY.set(Calendar.DAY_OF_MONTH, calendarEY.getActualMaximum(Calendar.DAY_OF_MONTH))
            endOfYear = calendarEY.time

            val calendarSY = getCalendar()
            calendarSY.set(Calendar.DAY_OF_YEAR, 1)
            startOfYear = calendarSY.time

    }


    fun getDate(range: AsyRange): String {

        return when (range){

             AsyRange.START_DAY ->  dateFormatter.format(startOfDay)
             AsyRange.END_DAY ->  dateFormatter.format(endOfDay)
             AsyRange.START_WEEK ->  dateFormatter.format(startOfWeek)
             AsyRange.END_WEEK ->  dateFormatter.format(endOfWeek)
             AsyRange.START_MONTH ->  dateFormatter.format(startOfMonth)
             AsyRange.END_MONTH ->  dateFormatter.format(endOfMonth)
             AsyRange.START_YEAR ->  dateFormatter.format(startOfYear)
             AsyRange.END_YEAR ->  dateFormatter.format(endOfYear)

         }
    }


    enum class AsyRange{
        START_DAY,
        END_DAY,
        START_WEEK,
        END_WEEK,
        START_MONTH,
        END_MONTH,
        START_YEAR,
        END_YEAR
    }
}
