package com.example.calendarapp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.DragEvent
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager.widget.ViewPager.OnPageChangeListener


class JCalendarMonthView @JvmOverloads constructor(private val mContext: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(mContext, attrs, defStyleAttr) {
    var eachcellheight = 0f
    var eachcellwidth = 0f
    var lastsec: Long = 0
    var selectedcell = 0
    private var currentscrollstate = 0
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
    private var selectedrect: Rect? = null
    var onPageChangeListener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
        override fun onPageSelected(position: Int) {}
        override fun onPageScrollStateChanged(state: Int) {
            Log.e("scrollstate", state.toString() + "")
            if (state == 2) {
                selectedrect = null
                selectedcell = -1
                downx = -1f
                downy = -1f
                invalidate()
            }
            currentscrollstate = state
        }
    }
    private var isup = false
    private var dayModels: ArrayList<DayModel>? = null
    private val mDefaultEventColor = Color.parseColor("#9fc6e7")
    private var mHeaderTextPaintRect: Rect? = null
    private var jDateTextPaintRect: Rect? = null
    private var jeventtextpaintRect: Rect? = null
    private var currentdaynameindex = 0
    private var mDetector: GestureDetector? = null

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
            mDetector = GestureDetector(mContext, MyGestureListener())
        } finally {
            a.recycle()
        }
    }

    fun setDayModels(dayModels: ArrayList<DayModel>?, currentdaynameindex: Int) {
        this.dayModels = dayModels
        this.currentdaynameindex = currentdaynameindex
        invalidate()
    }

    //    @Override
    //    public boolean dispatchTouchEvent(MotionEvent event) {
    //        if (event.getAction()==MotionEvent.ACTION_UP)return true;
    //
    //        if (event.getAction()==MotionEvent.ACTION_MOVE)return false;
    //        return super.dispatchTouchEvent(event);
    //    }
    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        val xtouch = motionEvent.x.toInt()
        val ytouch = motionEvent.y.toInt()
        if (ytouch < dayHeight) return true
        if (motionEvent.action == MotionEvent.ACTION_DOWN) {
            isup = false
            downx = xtouch.toFloat()
            downy = ytouch.toFloat()
            lastsec = System.currentTimeMillis()
            return true
        } else if (currentscrollstate == 0 && motionEvent.action == MotionEvent.ACTION_MOVE) {
            if (xtouch.toFloat() == downx && ytouch.toFloat() == downy && System.currentTimeMillis() - lastsec >= 80) {
                val column = (xtouch / eachcellwidth).toInt()
                val row = ((ytouch - dayHeight) / eachcellheight).toInt()
                val cell = row * 7 + column
                if (selectedcell != cell) {
                    selectedcell = cell
                    val reachxend = (eachcellwidth * (column + 1)).toInt()
                    val reachxstart = (eachcellwidth * column).toInt()
                    val reachyend = (eachcellheight * (row + 1) + dayHeight).toInt()
                    val reachystart = (eachcellheight * row + dayHeight).toInt()
                    val widthAnimator = ValueAnimator.ofInt(0, 100)
                    widthAnimator.addUpdateListener { animation ->
                        val progress = animation.animatedValue as Int
                        val start = xtouch - (xtouch - reachxstart) * progress / 100
                        val endside = xtouch + (reachxend - xtouch) * progress / 100
                        val topside = ytouch - (ytouch - reachystart) * progress / 100
                        val bottomside = ytouch + (reachyend - ytouch) * progress / 100
                        selectedrect = Rect(start, topside, endside, bottomside)
                        invalidate()
                    }
                    widthAnimator.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            if (isup) {
                                selectedrect = null
                                selectedcell = -1
                                downx = -1f
                                downy = -1f
                                invalidate()
                            }
                        }
                    })
                    widthAnimator.duration = 220
                    widthAnimator.start()
                }
            } else {
                selectedrect = null
                selectedcell = -1
                invalidate()
            }
            return super.onTouchEvent(motionEvent)
        } else if (motionEvent.action == MotionEvent.ACTION_UP) {
            if (xtouch.toFloat() == downx && ytouch.toFloat() == downy) {
                val column = (xtouch / eachcellwidth).toInt()
                val row = ((ytouch - dayHeight) / eachcellheight).toInt()
                val cell = row * 7 + column
                selectedcell = cell
                val reachxend = (eachcellwidth * (column + 1)).toInt()
                val reachxstart = (eachcellwidth * column).toInt()
                val reachyend = (eachcellheight * (row + 1) + dayHeight).toInt()
                val reachystart = (eachcellheight * row + dayHeight).toInt()
                val widthAnimator = ValueAnimator.ofInt(0, 100)
                widthAnimator.addUpdateListener { animation ->
                    val progress = animation.animatedValue as Int
                    val start = xtouch - (xtouch - reachxstart) * progress / 100
                    val endside = xtouch + (reachxend - xtouch) * progress / 100
                    val topside = ytouch - (ytouch - reachystart) * progress / 100
                    val bottomside = ytouch + (reachyend - ytouch) * progress / 100
                    selectedrect = Rect(start, topside, endside, bottomside)
                    invalidate()
                    if (progress == 100) {
                        val mainActivity = mContext as MainActivity
                        if (mainActivity != null && selectedcell != -1) {
                            val dayModel = dayModels!![selectedcell]
                            mainActivity.selectdateFromMonthPager(dayModel.year, dayModel.month, dayModel.day)
                        }
                        selectedrect = null
                        selectedcell = -1
                        downx = -1f
                        downy = -1f
                        invalidate()
                    }
                }
                widthAnimator.duration = 150
                widthAnimator.start()
            } else {
                selectedrect = null
                selectedcell = -1
                downx = -1f
                downy = -1f
                invalidate()
            }
            isup = true
            return super.onTouchEvent(motionEvent)
        } else {
            selectedrect = null
            selectedcell = -1
            downx = -1f
            downy = -1f
            invalidate()
        }
        return super.onTouchEvent(motionEvent)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        selectedrect = null
        selectedcell = -1
        downx = -1f
        downy = -1f
        mHeaderTextPaintRect = Rect()
        jDateTextPaintRect = Rect()
        jeventtextpaintRect = Rect()
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
        jeventtextpaint!!.getTextBounds("a", 0, "a".length, jeventtextpaintRect)
        jeventRectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        jeventRectPaint!!.style = Paint.Style.FILL
        jeventRectPaint!!.color = Color.parseColor("#009688")
        jselectrectpaint = Paint(Paint.ANTI_ALIAS_FLAG)
        jselectrectpaint!!.style = Paint.Style.FILL
        jselectrectpaint!!.color = Color.parseColor("#F0F0F0")
        jtodaypaint = Paint(Paint.ANTI_ALIAS_FLAG)
        jtodaypaint!!.style = Paint.Style.FILL
        jtodaypaint!!.color = resources.getColor(R.color.selectday)
        Log.e("height", "Test")
    }

    override fun onDragEvent(event: DragEvent): Boolean {
        Log.e("event", "drag")
        return super.onDragEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        eachcellheight = ((height - dayHeight) / 6).toFloat()
        eachcellwidth = (width / 7).toFloat()
        if (selectedrect != null) {
            canvas.drawRect(selectedrect!!, jselectrectpaint!!)
        }
        val point = FloatArray(4)
        var begining = dayHeight.toFloat()
        for (i in 0..6) {
            if (i < 6) {
                point[0] = 0f
                point[1] = begining
                point[2] = width.toFloat()
                point[3] = begining
                canvas.drawLines(point, paint!!)
            }
            point[0] = eachcellwidth + eachcellwidth * i
            point[1] = dayHeight / 1.5f
            point[2] = eachcellwidth + eachcellwidth * i
            point[3] = height.toFloat()
            canvas.drawLines(point, paint!!)
            begining = begining + eachcellheight
        }
        val topspace = IntArray(42)
        var i = 0
        while (i < 7 && dayModels != null && dayModels!!.size == 42) {
            var j = 0
            while (j < 7 && i < 6) {
                if (i == 0) {
                    if (j == currentdaynameindex) mHeaderTextPaint!!.color = resources.getColor(R.color.selectday) //todaycolor
                    else mHeaderTextPaint!!.color = daytextcolor
                    canvas.drawText(dayname[j], eachcellwidth * j + eachcellwidth / 2.0f - mHeaderTextPaintRect!!.right / 2.0f, (5 + mHeaderTextPaintRect!!.height()).toFloat(), mHeaderTextPaint!!)
                }
                val mydayModel = dayModels!![i * 7 + j]
                val ss = mydayModel.day.toString() + ""
                jDateTextPaint!!.getTextBounds(ss, 0, ss.length, jDateTextPaintRect)
                if (mydayModel.isToday) { //istoday
                    val centerx = eachcellwidth * j + eachcellwidth / 2.0f
                    val centery = datemargintop + dayHeight + i * eachcellheight + jDateTextPaintRect!!.height() / 2.0f
                    val max = Math.max(jDateTextPaintRect!!.width(), jDateTextPaintRect!!.height()).toFloat()
                    jDateTextPaint!!.color = Color.WHITE
                    canvas.drawRoundRect(centerx - max, centery - max, centerx + max, centery + max, max, max, jtodaypaint!!)
                } else {
                    if (!mydayModel.isenable()) jDateTextPaint!!.color = daytextcolor //date disable color
                    else jDateTextPaint!!.color = datetextcolor //date enable color
                }
                canvas.drawText(ss, eachcellwidth * j + eachcellwidth / 2.0f, datemargintop + dayHeight + i * eachcellheight + jDateTextPaintRect!!.height(), jDateTextPaint!!)
                var eventInfo = mydayModel.eventInfo
                val constant = 2 * datemargintop + dayHeight + i * eachcellheight + jDateTextPaintRect!!.height()
                var k = topspace[i * 7 + j]
                var noofevent = 0
                while (eventInfo != null) {
                    Log.e("jcalendar", mydayModel.toString() + "," + eventInfo.noofdayevent)
                    val row = i
                    val col = j
                    var jnoofday = eventInfo.noofdayevent
                    if (jnoofday == 0) jnoofday = 1
                    if (jnoofday > 1) {
                        var b = true
                        var myrow = row + 1
                        if (row * 7 + col + jnoofday >= myrow * 7) {
                            while (b && myrow < 6) {
                                if (row * 7 + col + jnoofday < (myrow + 1) * 7) {
                                    val diff = row * 7 + j + jnoofday - myrow * 7
                                    val rect1 = RectF()
                                    rect1.left = eachcellwidth * 0 - linewidth
                                    rect1.right = eachcellwidth * diff
                                    rect1.top = dayHeight + myrow * eachcellheight //(2 * datemargintop + dayHeight + (i * eachcellheight) + rect.height());
                                    rect1.bottom = dayHeight + (myrow + 1) * eachcellheight //(2 * datemargintop + dayHeight + (i * eachcellheight) + rect.height() + 50);
                                    canvas.save()
                                    canvas.clipRect(rect1)
                                    val colorrect = RectF()
                                    colorrect.left = rect1.left + 8 //0th column left padding
                                    colorrect.right = rect1.right - 12
                                    val myconstant = 2 * datemargintop + dayHeight + myrow * eachcellheight + jDateTextPaintRect!!.height()
                                    val newk = topspace[myrow * 7 + 0]
                                    colorrect.top = myconstant + 42 * newk + 3 * newk
                                    colorrect.bottom = colorrect.top + 42
                                    val color = if (eventInfo.eventcolor == 0) mDefaultEventColor else eventInfo.eventcolor
                                    jeventRectPaint!!.color = color
                                    canvas.drawRoundRect(colorrect, 6f, 6f, jeventRectPaint!!)
                                    canvas.drawText(eventInfo.title!!, colorrect.left + 5, colorrect.centerY() + jeventtextpaintRect!!.height() / 2.0f, jeventtextpaint!!)
                                    canvas.restore()
                                    b = false
                                } else {
                                    val rect1 = RectF()
                                    rect1.left = eachcellwidth * 0 - linewidth
                                    rect1.right = eachcellwidth * (0 + 7)
                                    rect1.top = dayHeight + myrow * eachcellheight //(2 * datemargintop + dayHeight + (i * eachcellheight) + rect.height());
                                    rect1.bottom = dayHeight + (myrow + 1) * eachcellheight //(2 * datemargintop + dayHeight + (i * eachcellheight) + rect.height() + 50);
                                    canvas.save()
                                    canvas.clipRect(rect1)
                                    val colorrect = RectF()
                                    colorrect.left = rect1.left + 8 //0th column left padding
                                    colorrect.right = rect1.right - 12
                                    val myconstant = 2 * datemargintop + dayHeight + myrow * eachcellheight + jDateTextPaintRect!!.height()
                                    val newk = topspace[myrow * 7 + 0]
                                    colorrect.top = myconstant + 42 * newk + 3 * newk
                                    colorrect.bottom = colorrect.top + 42
                                    val color = if (eventInfo.eventcolor == 0) mDefaultEventColor else eventInfo.eventcolor
                                    jeventRectPaint!!.color = color
                                    canvas.drawRoundRect(colorrect, 6f, 6f, jeventRectPaint!!)
                                    canvas.drawText(eventInfo.title!!, colorrect.left + 5, colorrect.centerY() + jeventtextpaintRect!!.height() / 2.0f, jeventtextpaint!!)
                                    canvas.restore()
                                }
                                myrow++
                            }
                        }
                        val begin = i * 7 + j
                        for (ia in 1 until jnoofday) {
                            if (begin + ia > 41) continue
                            topspace[begin + ia] = k + 1
                        }
                    }
                    val rect1 = RectF()
                    rect1.left = eachcellwidth * col - linewidth
                    val calculateday = if (col + jnoofday > 7) 7 - col else jnoofday
                    rect1.right = eachcellwidth * (col + calculateday)
                    Log.e("right", rect1.right.toString() + "," + col + jnoofday)
                    rect1.top = dayHeight + row * eachcellheight //(2 * datemargintop + dayHeight + (i * eachcellheight) + rect.height());
                    rect1.bottom = dayHeight + (row + 1) * eachcellheight //(2 * datemargintop + dayHeight + (i * eachcellheight) + rect.height() + 50);
                    canvas.save()
                    canvas.clipRect(rect1)
                    val colorrect = RectF()
                    if (j > 0) colorrect.left = rect1.left else colorrect.left = rect1.left + 8 //0th column left padding
                    colorrect.right = rect1.right - 12
                    colorrect.top = constant + 42 * k + 3 * k
                    colorrect.bottom = colorrect.top + 42
                    val color = if (eventInfo.eventcolor == 0) mDefaultEventColor else eventInfo.eventcolor
                    jeventRectPaint!!.color = color
                    if (noofevent > 2) {
                        jeventtextpaint!!.color = Color.BLACK
                        canvas.drawText("•••", colorrect.left + 5, colorrect.centerY() + jeventtextpaintRect!!.height() / 2.0f, jeventtextpaint!!)
                    } else {
                        Log.e("noofevent", noofevent.toString() + "")
                        jeventtextpaint!!.color = Color.WHITE
                        canvas.drawRoundRect(colorrect, 6f, 6f, jeventRectPaint!!)
                        canvas.drawText(eventInfo.title!!, colorrect.left + 5, colorrect.centerY() + jeventtextpaintRect!!.height() / 2.0f, jeventtextpaint!!)
                    }
                    canvas.restore()
                    k++
                    noofevent++
                    eventInfo = eventInfo.nextnode
                }
                j++
            }
            i++
        }
    }

    // In the SimpleOnGestureListener subclass you should override
    // onDown and any other gesture that you want to detect.
    internal inner class MyGestureListener : SimpleOnGestureListener() {
        override fun onDown(event: MotionEvent): Boolean {
            Log.d("TAG", "onDown: ")

            // don't return false here or else none of the other
            // gestures will work
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            Log.i("TAG", "onSingleTapConfirmed: ")
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            Log.i("TAG", "onLongPress: ")
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            Log.i("TAG", "onDoubleTap: ")
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent,
                              distanceX: Float, distanceY: Float): Boolean {
            Log.i("TAG", "onScroll: ")
            return true
        }

        override fun onFling(event1: MotionEvent?, event2: MotionEvent,
                             velocityX: Float, velocityY: Float): Boolean {
            Log.d("TAG", "onFling: ")
            return true
        }
    }
}