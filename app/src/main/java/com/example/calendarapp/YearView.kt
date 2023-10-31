package com.example.calendarapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.example.calendarapp.R
import org.joda.time.LocalDate



class YearView : View {
    private val dayname = arrayOf("S", "M", "T", "W", "T", "F", "S")
    private var mContext: Context? = null
    private var daypaint: Paint? = null
    private var mHeaderTextPaint: Paint? = null
    private var datepaint: Paint? = null
    private var todaypaint: Paint? = null
    private var roundpaint: Paint? = null
    private var mHeaderTextPaintRect: Rect? = null
    private var mdayrect: Rect? = null
    private var mdaterect: Rect? = null
    private var monthname = "Jan"
    private var startofweek = 0
    private var month = 0
    private var noofday = 0
    private var year = 0
    private val currentdate = LocalDate.now()

    constructor(context: Context?) : super(context)

    @JvmOverloads
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
        mContext = context
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        mContext = context
    }

    fun updateYearView(year: Int) {
        this.year = year
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mHeaderTextPaintRect = Rect()
        mHeaderTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mHeaderTextPaint!!.color = Color.BLACK
        mHeaderTextPaint!!.typeface = ResourcesCompat.getFont(mContext!!, R.font.googlesansmed)
        mHeaderTextPaint!!.textSize = resources.getDimensionPixelSize(R.dimen.smalltextsize).toFloat()
        mdayrect = Rect()
        daypaint = Paint(Paint.ANTI_ALIAS_FLAG)
        daypaint!!.color = Color.GRAY
        daypaint!!.typeface = ResourcesCompat.getFont(mContext!!, R.font.googlesans_regular)
        daypaint!!.textSize = resources.getDimensionPixelSize(R.dimen.daytextsize).toFloat()
        daypaint!!.getTextBounds("S", 0, "S".length, mdayrect)
        mdaterect = Rect()
        datepaint = Paint(Paint.ANTI_ALIAS_FLAG)
        datepaint!!.color = Color.BLACK
        datepaint!!.typeface = ResourcesCompat.getFont(mContext!!, R.font.googlesans_regular)
        datepaint!!.textSize = resources.getDimensionPixelSize(R.dimen.daytextsize).toFloat()
        todaypaint = Paint(Paint.ANTI_ALIAS_FLAG)
        todaypaint!!.color = Color.WHITE
        todaypaint!!.typeface = ResourcesCompat.getFont(mContext!!, R.font.googlesansmed)
        todaypaint!!.textSize = resources.getDimensionPixelSize(R.dimen.daytextsize).toFloat()
        roundpaint = Paint(Paint.ANTI_ALIAS_FLAG)
        roundpaint!!.color = Color.parseColor("#1a73e8")
        roundpaint!!.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val eachmonthwidth = (width - 30) / 3.0f
        val eachmonthheight = (height - 30) / 4.0f
        for (m in 0..3) {
            for (n in 0..2) {
                val localDate = LocalDate(year, m * 3 + (n + 1), 1)
                startofweek = localDate.dayOfMonth().withMinimumValue().dayOfWeek().get()
                if (startofweek == 7) startofweek = 0
                month = m * 3 + n
                monthname = localDate.toString("MMMM")
                noofday = localDate.dayOfMonth().maximumValue
                mHeaderTextPaint!!.getTextBounds(monthname, 0, monthname.length, mHeaderTextPaintRect)
                val eachcellsize = (eachmonthwidth - 30) / 7.0f
                //30 because of margin left each month
                canvas.drawText(monthname, 30 + eachmonthwidth * n + eachcellsize / 2.0f - mdayrect!!.width() / 2.0f, 30 + eachmonthheight * m + mHeaderTextPaintRect!!.height(), mHeaderTextPaint!!)
                //20 is topwidth
                for (i in 0..6) {
                    canvas.drawText(dayname[i], 30 + eachmonthwidth * n + eachcellsize * i + eachcellsize / 2.0f - mdayrect!!.width() / 2.0f, 30 + eachmonthheight * m + mHeaderTextPaintRect!!.height() + mdayrect!!.height() + 20, daypaint!!)
                }
                //   20 is datestartheight
                val endofdayheight = 30 + eachmonthheight * m + mHeaderTextPaintRect!!.height() + mdayrect!!.height() + 20 + 20
                val remainingheight = eachmonthheight * (m + 1) - endofdayheight
                val eachcellheight = (remainingheight - 30) / 6.0f
                var startday = 1
                for (i in 0..5) {
                    for (j in 0..6) {
                        val dateindex = i * 7 + j
                        if (dateindex < startofweek || startday > noofday) continue
                        val thisdate = LocalDate(year, m * 3 + (n + 1), startday)
                        val text = startday.toString() + ""
                        datepaint!!.getTextBounds(text, 0, text.length, mdaterect)
                        if (thisdate.isEqual(currentdate)) {
                            canvas.drawCircle(30 + eachmonthwidth * n + eachcellsize * j + eachcellsize / 2.0f, endofdayheight + eachcellheight * i + eachcellheight / 2.0f, eachcellsize / 2.0f, roundpaint!!)
                            canvas.drawText(text, 30 + eachmonthwidth * n + eachcellsize * j + eachcellsize / 2.0f - mdaterect!!.width() / 2.0f, endofdayheight + eachcellheight * i + eachcellheight / 2.0f + mdaterect!!.height() / 2.0f, todaypaint!!)
                        } else {
                            canvas.drawText(text, 30 + eachmonthwidth * n + eachcellsize * j + eachcellsize / 2.0f - mdaterect!!.width() / 2.0f, endofdayheight + eachcellheight * i + eachcellheight / 2.0f + mdaterect!!.height() / 2.0f, datepaint!!)
                        }
                        startday++
                    }
                }
            }
        }
    }
}