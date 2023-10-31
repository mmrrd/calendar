package com.example.calendarapp

import com.example.calendarapp.EventInfo


class DayModel {
    var month = 0
    var day = 0
    var year = 0
    var isToday = false
    lateinit var events: Array<String>
    var noofdayevent = 0
    var eventInfo: EventInfo? = null
    var isSelected = false
    var eventlist = false
    private var isenable = false
    fun isenable(): Boolean {
        return isenable
    }

    fun setIsenable(isenable: Boolean) {
        this.isenable = isenable
    }

    override fun toString(): String {
        return "$day/$month/$year"
    }
}