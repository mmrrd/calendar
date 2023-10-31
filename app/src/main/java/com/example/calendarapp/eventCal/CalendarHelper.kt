package com.example.calendarapp.eventCal

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.CalendarContract


class CalendarHelper {

    companion object {
        fun getCalendarIdForDefaultCalendar(context: Context): Long {
            val contentResolver = context.contentResolver
            val calendarsUri = CalendarContract.Calendars.CONTENT_URI
            val projection = arrayOf(CalendarContract.Calendars._ID)
            val cursor = contentResolver.query(calendarsUri, projection, null, null, null)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    val calendarId = cursor.getLong(0)
                    cursor.close()
                    return calendarId
                }
            }
            return -1
        }
        fun addEventToGoogleCalendar(context: Context, eventTitle: String, eventDescription: String,
                                     eventStartDateTime: Long, eventEndDateTime: Long, eventAllDay: Boolean,
                                     eventStatus: Int, eventTimezone: String) {

            val event = ContentValues()
            event.put(CalendarContract.Events.CALENDAR_ID, getCalendarIdForDefaultCalendar(context));
            event.put(CalendarContract.Events.TITLE, eventTitle)
            event.put(CalendarContract.Events.DESCRIPTION, eventDescription)
            event.put(CalendarContract.Events.DTSTART, eventStartDateTime)
            event.put(CalendarContract.Events.DTEND, eventEndDateTime)
            event.put(CalendarContract.Events.ALL_DAY, eventAllDay)
            event.put(CalendarContract.Events.STATUS, eventStatus)
            event.put(CalendarContract.Events.EVENT_TIMEZONE, eventTimezone)

            val eventUriString = "content://com.android.calendar/events"
            val eventUri = context.applicationContext.contentResolver.insert(Uri.parse(eventUriString), event)

            // Add a reminder to the event
            val reminders = ContentValues()
            reminders.put(CalendarContract.Reminders.EVENT_ID, eventUri!!.lastPathSegment!!.toLong())
            reminders.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALARM)
            reminders.put(CalendarContract.Reminders.MINUTES, 15) // Send the reminder 15 minutes before the event

            val reminderUriString = "content://com.android.calendar/reminders"
            context.applicationContext.contentResolver.insert(Uri.parse(reminderUriString), reminders)
        }
    }
}