package com.example.calendarapp

import com.example.calendarapp.DayModel



class MonthModel {
    var monthnamestr: String? = null
    var month = 0
    var year = 0
    var noofday = 0
    var noofweek = 0
    lateinit var dayModelArrayList: ArrayList<DayModel>
    var firstday = 0
}