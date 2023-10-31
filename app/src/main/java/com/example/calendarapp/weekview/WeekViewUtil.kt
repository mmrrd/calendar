package com.example.calendarapp.weekview

import org.joda.time.LocalDate
import java.util.Calendar

/**
 * Created by jesse on 6/02/2016.
 * and modify some code by jignesh khunt for https://github.com/jignesh13/googlecalendar
 */
object WeekViewUtil {
    /////////////////////////////////////////////////////////////////
    //
    //      Helper methods.
    //
    /////////////////////////////////////////////////////////////////
    /**
     * Checks if two times are on the same day.
     *
     * @param dayOne The first day.
     * @param dayTwo The second day.
     * @return Whether the times are on the same day.
     */
    fun isSameDay(dayOne: Calendar?, dayTwo: Calendar?): Boolean {
        return LocalDate(dayOne!!.timeInMillis).isEqual(LocalDate(dayTwo!!.timeInMillis))
        //        Log.e("compare".,new LocalDate(dayOne.getTimeInMillis()) +"=="+new LocalDate(dayTwo.getTimeInMillis()));
//        return dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR) && dayOne.get(Calendar.DAY_OF_YEAR) == dayTwo.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Returns a calendar instance at the start of this day
     *
     * @return the calendar instance
     */
    fun today(): Calendar {
        val today = Calendar.getInstance()
        today[Calendar.HOUR_OF_DAY] = 0
        today[Calendar.MINUTE] = 0
        today[Calendar.SECOND] = 0
        today[Calendar.MILLISECOND] = 0
        return today
    }
}