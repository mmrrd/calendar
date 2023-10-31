package com.example.calendarapp

import com.example.calendarapp.EventInfo

class EventInfo {
    lateinit var eventtitles: Array<String>
    var isallday = false
    var id = 0
    var accountname: String? = null
    var noofdayevent = 0
    var starttime: Long = 0
    var endtime: Long = 0
     var nextnode: EventInfo? =null
    var title: String? = null
    var timezone: String? = null
    var eventcolor = 0
    var isalreadyset = false

    constructor(eventtitles: Array<String>) {
        this.eventtitles = eventtitles
    }

    constructor()
    constructor(eventInfo: EventInfo?) {
        eventtitles = eventInfo!!.eventtitles
        isallday = eventInfo.isallday
        id = eventInfo.id
        accountname = eventInfo.accountname
        noofdayevent = eventInfo.noofdayevent
        starttime = eventInfo.starttime
        endtime = eventInfo.endtime
        title = eventInfo.title
        timezone = eventInfo.timezone
        eventcolor = eventInfo.eventcolor
        isalreadyset = eventInfo.isalreadyset
    }
}