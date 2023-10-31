package com.example.calendarapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.provider.CalendarContract
import android.util.Log
import androidx.core.app.ActivityCompat
import org.joda.time.DateTimeZone
import org.joda.time.Days
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import java.util.Arrays


object Utility {
    var localDateHashMap = HashMap<LocalDate, EventInfo?>()
    fun readCalendarEvent(context: Context, mintime: LocalDate, maxtime: LocalDate): HashMap<LocalDate, EventInfo?>? {

//        CalendarProvider calendarProvider = new CalendarProvider(context);
//
//        List<Calendar> calendars = calendarProvider.getCalendars().getList();
//        for(int i=0;i<calendars.size();i++){
//            List<Event> calendars1 = calendarProvider.getEvents(calendars.get(i).id).getList();
//            for (Event event:calendars1) {
//                Log.e("name"+calendars.get(i).id,event.title+","+event.eventColor+","+calendars.get(i).calendarColor+","+event.calendarColor);
//            }
//
//        }
        val f = 1
        val selection = "(( " + CalendarContract.Events.SYNC_EVENTS + " = " + f + " ) AND ( " + CalendarContract.Events.DTSTART + " >= " + mintime.toDateTimeAtStartOfDay().millis + " ) AND ( " + CalendarContract.Events.DTSTART + " <= " + maxtime.toDateTimeAtStartOfDay().millis + " ))"
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null
        }
        //        int ff=0;
//      Cursor cursor1=  context.getContentResolver().query(CalendarContract.Colors.CONTENT_URI,new String[]{"color","color_index","color_type","account_name"},null,null);
//        cursor1.moveToFirst();
//        while (cursor1.moveToNext()) {
//        Log.e("str"+ff,cursor1.getString(0)+","+cursor1.getString(1)+","+cursor1.getString(2)+","+cursor1.getString(3));
//ff++;
//        }
        val cursor = context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI, arrayOf("_id", "title", "description",
                "dtstart", "dtend", "eventLocation", "calendar_displayName", CalendarContract.Events.ALL_DAY, CalendarContract.Events.EVENT_COLOR, CalendarContract.Events.CALENDAR_COLOR, CalendarContract.Events.EVENT_TIMEZONE, CalendarContract.Events.DURATION), null,
                null, null)
        cursor!!.moveToFirst()
        // fetching calendars name


        // fetching calendars id
        var syncacc: String? = null
        while (cursor.moveToNext()) {
            syncacc = cursor.getString(6)
            if (true) {
                Log.e(cursor.getString(1), cursor.getInt(7).toString() + "")
                val localDate = getDate(cursor.getString(3).toLong())
                if (!localDateHashMap.containsKey(localDate)) {
                    val eventInfo = EventInfo()
                    eventInfo.id = cursor.getInt(0)
                    eventInfo.starttime = cursor.getLong(3)
                    eventInfo.endtime = cursor.getLong(4)
                    if (cursor.getString(11) != null) eventInfo.endtime = eventInfo.starttime + RFC2445ToMilliseconds(cursor.getString(11))
                    eventInfo.accountname = cursor.getString(6)
                    eventInfo.timezone = cursor.getString(10)
                    eventInfo.eventtitles = arrayOf(cursor.getString(1))
                    eventInfo.isallday = if (cursor.getInt(7) == 1) true else false
                    eventInfo.title = cursor.getString(1)
                    eventInfo.eventcolor = if (cursor.getInt(8) == 0) Color.parseColor("#009688") else cursor.getInt(8)
                    val difference = eventInfo.endtime - eventInfo.starttime
                    if (difference > 86400000) {
                        if (cursor.getInt(7) == 0) {
                            eventInfo.endtime = eventInfo.endtime + 86400000L
                        }
                        val localDate1 = LocalDateTime(eventInfo.starttime, DateTimeZone.forID(eventInfo.timezone)).withTime(0, 0, 0, 0)
                        val localDate2 = LocalDateTime(eventInfo.endtime, DateTimeZone.forID(eventInfo.timezone)).withTime(23, 59, 59, 999)
                        val day = Days.daysBetween(localDate1, localDate2).days
                        eventInfo.noofdayevent = day
                        eventInfo.isallday = true
                    } else if (difference < 86400000) eventInfo.noofdayevent = 0 else eventInfo.noofdayevent = 1
                    localDateHashMap[localDate] = eventInfo
                } else {
                    val eventInfo = localDateHashMap[localDate]
                    var prev = eventInfo
                    while (prev!!.nextnode != null) prev = prev.nextnode
                    val s = eventInfo!!.eventtitles
                    var isneed = true
                    for (i in s.indices) {
                        if (s[i] == cursor.getString(1)) {
                            isneed = false
                            break
                        }
                        //                        if (i + 1 < s.length) prev = prev.nextnode;
                    }
                    if (isneed) {
                        val ss = Arrays.copyOf(s, s.size + 1)
                        ss[ss.size - 1] = cursor.getString(1)
                        eventInfo.eventtitles = ss
                        val nextnode = EventInfo()
                        nextnode.id = cursor.getInt(0)
                        nextnode.starttime = cursor.getLong(3)
                        nextnode.endtime = cursor.getLong(4)
                        if (cursor.getString(11) != null) nextnode.endtime = nextnode.starttime + RFC2445ToMilliseconds(cursor.getString(11))
                        nextnode.isallday = if (cursor.getInt(7) == 1) true else false
                        nextnode.timezone = cursor.getString(10)
                        nextnode.title = cursor.getString(1)
                        nextnode.accountname = cursor.getString(6)
                        nextnode.eventcolor = if (cursor.getInt(8) == 0) Color.parseColor("#009688") else cursor.getInt(8)
                        val difference = nextnode.endtime - nextnode.starttime
                        if (nextnode.endtime - nextnode.starttime > 86400000) {
                            if (cursor.getInt(7) == 0) {
                                nextnode.endtime = nextnode.endtime + 86400000L
                            }
                            nextnode.isallday = true
                            val localDate1 = LocalDateTime(nextnode.starttime, DateTimeZone.forID(nextnode.timezone)).withTime(0, 0, 0, 0)
                            val localDate2 = LocalDateTime(nextnode.endtime, DateTimeZone.forID(nextnode.timezone)).withTime(23, 59, 59, 999)
                            val day = Days.daysBetween(localDate1, localDate2).days
                            nextnode.noofdayevent = day
                        } else if (difference < 86400000) nextnode.noofdayevent = 0 else nextnode.noofdayevent = 1
                        prev.nextnode = nextnode
                        localDateHashMap[localDate] = eventInfo
                    }
                }
            }
        }
        return localDateHashMap
    }

    fun RFC2445ToMilliseconds(str: String?): Long {
        require(!(str == null || str.isEmpty())) { "Null or empty RFC string" }
        var sign = 1
        var weeks = 0
        var days = 0
        var hours = 0
        var minutes = 0
        var seconds = 0
        val len = str.length
        var index = 0
        var c: Char
        c = str[0]
        if (c == '-') {
            sign = -1
            index++
        } else if (c == '+') index++
        if (len < index) return 0
        c = str[index]
        require(c == 'P') { "Duration.parse(str='$str') expected 'P' at index=$index" }
        index++
        c = str[index]
        if (c == 'T') index++
        var n = 0
        while (index < len) {
            c = str[index]
            if (c >= '0' && c <= '9') {
                n *= 10
                n += (c.code - '0'.code)
            } else if (c == 'W') {
                weeks = n
                n = 0
            } else if (c == 'H') {
                hours = n
                n = 0
            } else if (c == 'M') {
                minutes = n
                n = 0
            } else if (c == 'S') {
                seconds = n
                n = 0
            } else if (c == 'D') {
                days = n
                n = 0
            } else if (c == 'T') {
            } else throw IllegalArgumentException("Duration.parse(str='$str') unexpected char '$c' at index=$index")
            index++
        }
        val factor = (1000 * sign).toLong()
        return factor * (7 * 24 * 60 * 60 * weeks + 24 * 60 * 60 * days + 60 * 60 * hours + 60 * minutes
                + seconds)
    }

    @SuppressLint("Range")
    fun getDataFromCalendarTable(context: Context) {
        var cur: Cursor? = null
        val cr = context.contentResolver
        val mProjection = arrayOf(
                CalendarContract.Calendars.ALLOWED_ATTENDEE_TYPES,
                CalendarContract.Calendars.ACCOUNT_NAME,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                CalendarContract.Calendars.CALENDAR_LOCATION,
                CalendarContract.Calendars.CALENDAR_TIME_ZONE,
                CalendarContract.Calendars._ID
        )
        val uri = CalendarContract.Calendars.CONTENT_URI
        val selection = ("((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))")
        val selectionArgs = arrayOf("jigneshkhunt13@gmail.com", "jigneshkhunt13@gmail.com",
                "jigneshkhunt13@gmail.com")
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        cur = cr.query(uri, mProjection, null, null, null)
        while (cur!!.moveToNext()) {
            val displayName = cur.getString(cur.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME))
            val accountName = cur.getString(cur.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME))
        }
    }

    fun getDate(milliSeconds: Long): LocalDate {
        val instantFromEpochMilli = Instant.ofEpochMilli(milliSeconds)
        return instantFromEpochMilli.toDateTime(DateTimeZone.getDefault()).toLocalDate()
    }
}