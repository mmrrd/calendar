package com.example.calendarapp.weekview

import android.util.Log
import org.joda.time.LocalDate
import java.util.Calendar

/**
 * Created by Raquib-ul-Alam Kanak on 7/21/2014.
 * Website: http://april-shower.com
 * and modify some code by jignesh khunt for https://github.com/jignesh13/googlecalendar
 */
class WeekViewEvent {
    var id: Long = 0
    lateinit var startTime: Calendar
    var actualstart: Calendar? = null
    lateinit var endTime: Calendar
    var actualend: Calendar? = null
    var name: String? = null
    var location: String? = null
    var accountname: String? = null
    var color = 0
    var isAllDay = false
    var daytype = 0
    var isIsmoreday = false
        private set
    var noofday: Long = 0
    var isAlreadyset = false
    var myday = 0

    constructor()

    /**
     * Initializes the event for week view.
     *
     * @param id          The id of the event.
     * @param name        Name of the event.
     * @param startYear   Year when the event starts.
     * @param startMonth  Month when the event starts.
     * @param startDay    Day when the event starts.
     * @param startHour   Hour (in 24-hour format) when the event starts.
     * @param startMinute Minute when the event starts.
     * @param endYear     Year when the event ends.
     * @param endMonth    Month when the event ends.
     * @param endDay      Day when the event ends.
     * @param endHour     Hour (in 24-hour format) when the event ends.
     * @param endMinute   Minute when the event ends.
     */
    constructor(id: Long, name: String?, startYear: Int, startMonth: Int, startDay: Int, startHour: Int, startMinute: Int, endYear: Int, endMonth: Int, endDay: Int, endHour: Int, endMinute: Int) {
        this.id = id
        startTime = Calendar.getInstance()
        startTime.set(Calendar.YEAR, startYear)
        startTime.set(Calendar.MONTH, startMonth - 1)
        startTime.set(Calendar.DAY_OF_MONTH, startDay)
        startTime.set(Calendar.HOUR_OF_DAY, startHour)
        startTime.set(Calendar.MINUTE, startMinute)
        endTime = Calendar.getInstance()
        endTime.set(Calendar.YEAR, endYear)
        endTime.set(Calendar.MONTH, endMonth - 1)
        endTime.set(Calendar.DAY_OF_MONTH, endDay)
        endTime.set(Calendar.HOUR_OF_DAY, endHour)
        endTime.set(Calendar.MINUTE, endMinute)
        this.name = name
    }

    /**
     * Initializes the event for week view.
     *
     * @param id        The id of the event.
     * @param name      Name of the event.
     * @param location  The location of the event.
     * @param startTime The time when the event starts.
     * @param endTime   The time when the event ends.
     * @param allDay    Is the event an all day event.
     */
    constructor(id: Long, name: String?, location: String?, startTime: Calendar, endTime: Calendar, allDay: Boolean, accountname: String?) {
        this.id = id
        this.name = name
        this.location = location
        this.startTime = startTime
        this.endTime = endTime
        isAllDay = allDay
        this.accountname = accountname
    }

    /**
     * Initializes the event for week view.
     *
     * @param id        The id of the event.
     * @param name      Name of the event.
     * @param location  The location of the event.
     * @param startTime The time when the event starts.
     * @param endTime   The time when the event ends.
     */
    constructor(id: Long, name: String?, location: String?, startTime: Calendar, endTime: Calendar, accountname: String?) : this(id, name, location, startTime, endTime, false, accountname)

    /**
     * Initializes the event for week view.
     *
     * @param id        The id of the event.
     * @param name      Name of the event.
     * @param startTime The time when the event starts.
     * @param endTime   The time when the event ends.
     */
    constructor(id: Long, name: String?, startTime: Calendar, endTime: Calendar, accountname: String?) : this(id, name, null, startTime, endTime, accountname)

    fun setIsmoreday(ismoreday: Boolean) {
        isIsmoreday = ismoreday
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as WeekViewEvent
        return id == that.id
    }

    override fun hashCode(): Int {
        return (id xor (id ushr 32)).toInt()
    }

    fun splitWeekViewEvents(): List<WeekViewEvent> {
        //This function splits the WeekViewEvent in WeekViewEvents by day
        val events: MutableList<WeekViewEvent> = ArrayList()
        // The first millisecond of the next day is still the same day. (no need to split events for this).
        var endTime = endTime!!.clone() as Calendar
        endTime.add(Calendar.MILLISECOND, -1)
        if (!WeekViewUtil.isSameDay(startTime, endTime)) {
            Log.e("jmore" + name, LocalDate(startTime).toString() + "," + LocalDate(this.endTime))
            val remainingDays = Math.round((endTime.timeInMillis - startTime!!.timeInMillis).toFloat() / (24 * 60 * 60 * 1000)).toLong()
            endTime = startTime!!.clone() as Calendar
            endTime[Calendar.HOUR_OF_DAY] = 23
            endTime[Calendar.MINUTE] = 59
            var k = 1
            val event1 = WeekViewEvent(id, name, location, startTime, endTime, isAllDay, accountname)
            event1.setIsmoreday(true)
            event1.daytype = k
            event1.actualstart = startTime
            event1.actualend = this.endTime
            event1.noofday = remainingDays
            event1.color = color
            events.add(event1)

            // Add other days.
            val otherDay = startTime!!.clone() as Calendar
            otherDay.add(Calendar.DATE, 1)
            Log.e("jtestbefore", name + LocalDate(otherDay.timeInMillis))
            while (!WeekViewUtil.isSameDay(otherDay, this.endTime)) {
                val overDay = otherDay.clone() as Calendar
                overDay[Calendar.HOUR_OF_DAY] = 0
                overDay[Calendar.MINUTE] = 0
                val endOfOverDay = overDay.clone() as Calendar
                endOfOverDay[Calendar.HOUR_OF_DAY] = 23
                endOfOverDay[Calendar.MINUTE] = 59
                val eventMore = WeekViewEvent(id, name, null, overDay, endOfOverDay, isAllDay, accountname)
                eventMore.color = color
                eventMore.setIsmoreday(true)
                eventMore.actualstart = startTime
                eventMore.actualend = this.endTime
                k++
                eventMore.daytype = k
                eventMore.noofday = remainingDays
                events.add(eventMore)

                // Add next day.
                otherDay.add(Calendar.DATE, 1)
            }
            //
            // Add last day.
//            Calendar startTime = (Calendar) this.getEndTime().clone();
//            startTime.set(Calendar.HOUR_OF_DAY, 0);
//            startTime.set(Calendar.MINUTE, 0);
//
//            WeekViewEvent event2 = new WeekViewEvent(this.getId(), this.getName(), this.getLocation(), startTime, this.getEndTime(), this.isAllDay());
//            event2.setColor(this.getColor());
//            event2.setIsmoreday(true);
//            event2.setNoofday(remainingDays);
//            k++;
//            event2.setDaytype(k);
//            events.add(event2);
        } else {
            events.add(this)
        }
        return events
    }
}