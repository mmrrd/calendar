package com.example.calendarapp.weekview

import java.util.Calendar

/**
 * Created by Raquib on 1/6/2015.
 */
interface DateTimeInterpreter {
    fun interpretday(date: Calendar): String
    fun interpretDate(date: Calendar): String
    fun interpretTime(hour: Int): String
}