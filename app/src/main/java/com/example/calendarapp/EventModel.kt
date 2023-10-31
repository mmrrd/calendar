package com.example.calendarapp

import org.joda.time.LocalDate


class EventModel : Comparable<EventModel> {
    var eventname: String
    var localDate: LocalDate
    var type: Int
        private set
    var color = 0

    constructor(eventname: String, localDate: LocalDate, type: Int) {
        this.eventname = eventname
        this.localDate = localDate
        this.type = type
        //this.color= Color.parseColor("#009688");
    }

    constructor(color: Int, eventname: String, localDate: LocalDate, type: Int) {
        this.eventname = eventname
        this.localDate = localDate
        this.type = type
        this.color = color
    }

    override fun compareTo(eventModel: EventModel): Int {
        return localDate.compareTo(eventModel.localDate)
    }
}