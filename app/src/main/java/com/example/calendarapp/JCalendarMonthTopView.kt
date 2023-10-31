package com.example.calendarapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import org.greenrobot.eventbus.EventBus
import org.joda.time.LocalDate
import java.util.Calendar


class JCalendarMonthTopView @JvmOverloads constructor(private val mContext: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(mContext, attrs, defStyleAttr) {
    var eachcellheight = 0f
    var eachcellwidth = 0f
    var lastsec: Long = 0
    var selectedcell = 0
    private var paint: Paint? = null
    private var mHeaderTextPaint: Paint? = null
    private var jDateTextPaint: Paint? = null
    private var jeventRectPaint: Paint? = null
    private var jeventtextpaint: Paint? = null
    private var jselectrectpaint: Paint? = null
    private var jtodaypaint: Paint? = null
    private val dayfont: Typeface? = null
    private var dayHeight = 0
    private var daytextsize = 0
    private var datemargintop = 0
    private var linecolor = 0
    private var linewidth = 0
    private var daytextcolor = 0
    private var datetextsize = 0
    private var datetextcolor = 0
    private var eventtextsize = 0
    private var downx = 0f
    private var downy = 0f
    private val dayname = arrayOf("S", "M", "T", "W", "T", "F", "S")
    private val selectedrect: Rect? = null
    private val isup = false
    private var mHeaderTextPaintRect: Rect? = null
    private var jDateTextPaintRect: Rect? = null
    private var numberofrow = 0
    private var dayModels = ArrayList<DayModel>()
    private var firstday = 4
    private var month = 0
    private var year = 0

    init {
        // Hold references.
//        if (attrs == null) {
//            return
//        }
        // Get the attribute values (if any).
        val a = mContext.theme.obtainStyledAttributes(attrs, R.styleable.JCalendarMonthView, 0, 0)
        try {
            dayHeight = a.getDimensionPixelSize(R.styleable.JCalendarMonthView_dayHeight, 200)
            daytextsize = a.getDimensionPixelSize(R.styleable.JCalendarMonthView_daytextsize, 12)
            datetextsize = a.getDimensionPixelSize(R.styleable.JCalendarMonthView_datetextsize, 14)
            eventtextsize = a.getDimensionPixelSize(R.styleable.JCalendarMonthView_eventtextsize, 11)
            daytextcolor = a.getColor(R.styleable.JCalendarMonthView_daytextcolor, Color.GRAY)
            datetextcolor = a.getColor(R.styleable.JCalendarMonthView_datetextcolor, Color.GRAY)
            datemargintop = a.getDimensionPixelSize(R.styleable.JCalendarMonthView_datemargintop, 25)
            linecolor = a.getColor(R.styleable.JCalendarMonthView_linecolor, Color.GRAY)
            linewidth = a.getDimensionPixelSize(R.styleable.JCalendarMonthView_linewidth, 2)
        } finally {
            a.recycle()
        }
    }

    fun initdata(dayModels: ArrayList<DayModel>, firstday: Int, month: Int, year: Int) {
        this.dayModels = dayModels
        this.firstday = firstday
        this.month = month
        this.year = year
        requestLayout()
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.y < dayHeight + datemargintop) {
            return true
        } else if (event.action == MotionEvent.ACTION_DOWN) {
            downx = event.x
            downy = event.y
            return true
        } else if (event.action == MotionEvent.ACTION_UP) {
            if (event.x == downx && event.y == downy) {
                val column = (event.x / eachcellwidth).toInt()
                val row = ((event.y - (dayHeight + datemargintop)) / eachcellheight).toInt()
                val cell = row * 7 + column
                if (cell >= firstday) {
                    val pos = cell - firstday
                    if (pos < dayModels.size) {
                        for (dayModel in dayModels) {
                            dayModel.isSelected = false
                        }
                        MainActivity.Companion.lastdate = LocalDate(year, month, dayModels[pos].day)
                        val mainActivity = mContext as MainActivity
                        if (mainActivity.mNestedView!!.visibility == VISIBLE) EventBus.getDefault().post(MessageEvent(LocalDate(year, month, dayModels[pos].day)))
                        if (mainActivity.weekviewcontainer!!.visibility == VISIBLE) {
                            val todaydate = Calendar.getInstance()
                            todaydate[Calendar.DAY_OF_MONTH] = MainActivity.Companion.lastdate.getDayOfMonth()
                            todaydate[Calendar.MONTH] = MainActivity.Companion.lastdate.getMonthOfYear() - 1
                            todaydate[Calendar.YEAR] = MainActivity.Companion.lastdate.getYear()
                            mainActivity.mWeekView!!.goToDate(todaydate)
                        }
                        invalidate()
                    }
                }
            }
            return super.onTouchEvent(event)
        }
        return super.onTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val size = dayModels.size + firstday
        numberofrow = if (size % 7 == 0) size / 7 else size / 7 + 1
        val setheight = mContext.resources.getDimensionPixelSize(R.dimen.itemheight) * numberofrow + dayHeight + datemargintop
        setMeasuredDimension(widthSize, setheight)
        mHeaderTextPaintRect = Rect()
        jDateTextPaintRect = Rect()
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint!!.isAntiAlias = true
        paint!!.style = Paint.Style.STROKE
        paint!!.strokeWidth = linewidth.toFloat()
        paint!!.color = linecolor
        mHeaderTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint!!.textAlign = Paint.Align.CENTER
        mHeaderTextPaint!!.color = daytextcolor
        mHeaderTextPaint!!.typeface = ResourcesCompat.getFont(mContext, R.font.googlesansmed)
        mHeaderTextPaint!!.textSize = daytextsize.toFloat()
        mHeaderTextPaint!!.getTextBounds("S", 0, "S".length, mHeaderTextPaintRect)
        jDateTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        jDateTextPaint!!.textAlign = Paint.Align.CENTER
        jDateTextPaint!!.color = datetextcolor
        jDateTextPaint!!.typeface = ResourcesCompat.getFont(mContext, R.font.latoregular)
        jDateTextPaint!!.textSize = datetextsize.toFloat()
        jeventtextpaint = Paint(Paint.ANTI_ALIAS_FLAG)
        jeventtextpaint!!.textAlign = Paint.Align.LEFT
        jeventtextpaint!!.color = Color.WHITE
        jeventtextpaint!!.typeface = ResourcesCompat.getFont(mContext, R.font.googlesansmed)
        jeventtextpaint!!.textSize = eventtextsize.toFloat()
        jeventRectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        jeventRectPaint!!.style = Paint.Style.FILL
        jeventRectPaint!!.color = Color.parseColor("#009688")
        jselectrectpaint = Paint(Paint.ANTI_ALIAS_FLAG)
        jselectrectpaint!!.style = Paint.Style.FILL
        jselectrectpaint!!.color = Color.parseColor("#F0F0F0")
        jtodaypaint = Paint(Paint.ANTI_ALIAS_FLAG)
        jtodaypaint!!.style = Paint.Style.FILL
        jtodaypaint!!.color = resources.getColor(R.color.selectday)


//        Log.e("height",rect.toString());
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = dayModels.size + firstday
        numberofrow = if (size % 7 == 0) size / 7 else size / 7 + 1
        eachcellheight = ((height - (dayHeight + datemargintop)) / numberofrow).toFloat()
        eachcellwidth = (width / 7).toFloat()
        if (selectedrect != null) {
            canvas.drawRect(selectedrect, jselectrectpaint!!)
        }
        val point = FloatArray(4)
        val begining = (dayHeight + datemargintop).toFloat()

// for draw line
//        for (int i = 0; i < 7; i++) {
//
//            if (i < 6) {
//                point[0] = 0;
//                point[1] = begining;
//                point[2] = getWidth();
//                point[3] = begining;
//                canvas.drawLines(point, paint);
//            }
//            point[0] = eachcellwidth + eachcellwidth * i;
//            point[1] = dayHeight / 1.5f;
//            point[2] = eachcellwidth + eachcellwidth * i;
//            point[3] = getHeight();
//            canvas.drawLines(point, paint);
//            begining = begining + eachcellheight;
//
//        }
        for (i in 0 until numberofrow) {
            for (j in 0..6) {
                if (i == 0) {
                    canvas.drawText(dayname[j], eachcellwidth * j + eachcellwidth / 2.0f - mHeaderTextPaintRect!!.right / 2.0f, (dayHeight - mHeaderTextPaintRect!!.height()).toFloat(), mHeaderTextPaint!!)
                }
                var position = i * 7 + j
                if (position >= firstday) {
                    position = position - firstday
                    if (position >= dayModels.size) continue
                    val dayModel = dayModels[position]
                    val selected = if (dayModel.day == MainActivity.Companion.lastdate.getDayOfMonth() && dayModel.month == MainActivity.Companion.lastdate.getMonthOfYear() && dayModel.year == MainActivity.Companion.lastdate.getYear()) true else false
                    val ss = dayModels[position].day.toString() + ""
                    jDateTextPaint!!.getTextBounds(ss, 0, ss.length, jDateTextPaintRect)
                    if (dayModel.isToday || selected) { //istoday
                        val centerx = eachcellwidth * j + eachcellwidth / 2.0f
                        val centery = datemargintop + dayHeight + i * eachcellheight + eachcellheight / 2.0f
                        val max = resources.getDimensionPixelSize(R.dimen.circlesize).toFloat()
                        if (dayModel.isToday) {
                            jtodaypaint!!.color = resources.getColor(R.color.selectday)
                            jDateTextPaint!!.color = Color.WHITE
                        } else {
                            jtodaypaint!!.color = Color.parseColor("#4D5B80E7")
                            jDateTextPaint!!.color = Color.rgb(91, 128, 231)
                        }
                        canvas.drawRoundRect(centerx - max, centery - max, centerx + max, centery + max, max, max, jtodaypaint!!)
                    } else {
                        jDateTextPaint!!.color = datetextcolor //date enable color
                    }
                    canvas.drawText(ss, eachcellwidth * j + eachcellwidth / 2.0f, datemargintop + dayHeight + i * eachcellheight + eachcellheight / 2.0f + jDateTextPaintRect!!.height() / 2.0f, jDateTextPaint!!)
                    if (dayModel.eventlist && !selected) { //event day
                        jtodaypaint!!.color = resources.getColor(R.color.event_color_04)
                        val centerx = eachcellwidth * j + eachcellwidth / 2.0f
                        val centery = datemargintop + dayHeight + i * eachcellheight + eachcellheight / 2.0f + jDateTextPaintRect!!.height() + 8
                        canvas.drawRoundRect(centerx - 5, centery - 5, centerx + 5, centery + 5, 5f, 5f, jtodaypaint!!)
                    }
                }
            }
        }
    }
}