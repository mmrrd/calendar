package com.example.calendarapp.weekview

import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Region
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.SoundEffectConstants
import android.view.View
import android.view.ViewConfiguration
import android.widget.OverScroller
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import com.example.calendarapp.MainActivity
import com.example.calendarapp.R
import com.example.calendarapp.weekview.MonthLoader.MonthChangeListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Collections
import java.util.Locale

/**
 * Created by Raquib-ul-Alam Kanak on 7/21/2014.
 * Website: http://alamkanak.github.io/
 * and modify some code by jignesh khunt for https://github.com/jignesh13/googlecalendar
 */
class WeekView @JvmOverloads constructor(private val mContext: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(mContext, attrs, defStyleAttr) {
    var forward = 1080
    private var shadowPaint: Paint? = null
    private val stop = false
    private var mTimeTextPaint: Paint? = null
    private var mTimeTextWidth = 0f
    private var mTimeTextHeight = 0f
    private var mHeaderTextPaint: Paint? = null
    private var mHeaderTextHeight = 0f
    private var shadow: View? = null
    private var jHeaderTextPaint: Paint? = null
    private var jtodayHeaderTextPaint: Paint? = null
    private var jHeaderTextHeight = 0f
    private var mHeaderHeight = 0f
    private var mGestureDetector: GestureDetectorCompat? = null
    private var mScroller: OverScroller? = null
    private val mCurrentOrigin = PointF(0f, 0f)
    private var mCurrentScrollDirection = Direction.NONE
    private var mHeaderBackgroundPaint: Paint? = null
    private var mWidthPerDay = 0f
    private var mDayBackgroundPaint: Paint? = null
    private var mHourSeparatorPaint: Paint? = null
    private var mHeaderMarginBottom = 0f
    private var mTodayBackgroundPaint: Paint? = null
    private var jheaderEventTextpaint: Paint? = null
    private var jheaderEventheight = 0
    private var mFutureBackgroundPaint: Paint? = null
    private var mPastBackgroundPaint: Paint? = null
    private var mFutureWeekendBackgroundPaint: Paint? = null
    private var mPastWeekendBackgroundPaint: Paint? = null
    private var mNowLinePaint: Paint? = null
    private var jNowbackPaint: Paint? = null
    private var mTodayHeaderTextPaint: Paint? = null
    private var mEventBackgroundPaint: Paint? = null
    private var mHeaderColumnWidth = 0f
    private var mEventRects: MutableList<EventRect>? = null
    private var mPreviousPeriodEvents: List<WeekViewEvent?>? = null
    private var mCurrentPeriodEvents: List<WeekViewEvent?>? = null
    private var mNextPeriodEvents: List<WeekViewEvent?>? = null
    private var mEventTextPaint: TextPaint? = null
    private var mHeaderColumnBackgroundPaint: Paint? = null
    private var mFetchedPeriod = -1 // the middle period the calendar has fetched.
    private var mRefreshEvents = false
    private var mCurrentFlingDirection = Direction.NONE
    private var mScaleDetector: ScaleGestureDetector? = null
    private var mIsZooming = false

    /**
     * Returns the first visible day in the week view.
     *
     * @return The first visible day in the week view.
     */
    var firstVisibleDay: Calendar? = null
        private set

    /**
     * Returns the last visible day in the week view.
     *
     * @return The last visible day in the week view.
     */
    var lastVisibleDay: Calendar? = null
        private set
    var isShowFirstDayOfWeekFirst = false
    private var mDefaultEventColor = 0
    private var mMinimumFlingVelocity = 0
    private var mScaledTouchSlop = 0

    // Attributes and their default values.
    private var mHourHeight = 50
    private var mNewHourHeight = -1
    private var mMinHourHeight = 0 //no minimum specified (will be dynamic, based on screen)
    private var mEffectiveMinHourHeight = mMinHourHeight //compensates for the fact that you can't keep zooming out.
    private var mMaxHourHeight = 250
    private var mColumnGap = 10
    private var mFirstDayOfWeek = Calendar.MONDAY
    private var mTextSize = 12
    private var mHeaderColumnPadding = 10
    private var mHeaderColumnTextColor = Color.BLACK
    private var mNumberOfVisibleDays = 3
    private var mHeaderRowPadding = 10
    private var mHeaderRowBackgroundColor = Color.WHITE
    private var mDayBackgroundColor = Color.rgb(245, 245, 245)
    private var mPastBackgroundColor = Color.rgb(227, 227, 227)
    private var mFutureBackgroundColor = Color.rgb(245, 245, 245)
    private var mPastWeekendBackgroundColor = 0
    private var mFutureWeekendBackgroundColor = 0
    private var mNowLineColor = Color.rgb(102, 102, 102)
    private var mNowLineThickness = 5
    private var mHourSeparatorColor = Color.rgb(230, 230, 230)
    private var mTodayBackgroundColor = Color.rgb(239, 247, 254)
    private var mHourSeparatorHeight = 2
    private var mTodayHeaderTextColor = Color.rgb(39, 137, 228)
    private var mEventTextSize = 12
    private var mEventTextColor = Color.BLACK
    private var mEventPadding = 8
    private var mHeaderColumnBackgroundColor = Color.WHITE
    private var mIsFirstDraw = true
    private var mAreDimensionsInvalid = true

    @Deprecated("")
    private var mDayNameLength = LENGTH_LONG
    private var mOverlappingEventGap = 0
    private var mEventMarginVertical = 0
    /**
     * Get the scrolling speed factor in horizontal direction.
     *
     * @return The speed factor in horizontal direction.
     */
    /**
     * Sets the speed for horizontal scrolling.
     *
     * @param xScrollingSpeed The new horizontal scrolling speed.
     */
    var xScrollingSpeed = 1f
    private var mScrollToDay: Calendar? = null
    private var mScrollToHour = -1.0

    /**
     * Set corner radius for event rect.
     *
     * @param eventCornerRadius the radius in px.
     */
    var eventCornerRadius = 0
    private var mShowDistinctWeekendColor = false
    private var mShowNowLine = false
    private var mShowDistinctPastFutureColor = false
    /**
     * Get whether the week view should fling horizontally.
     *
     * @return True if the week view has horizontal fling enabled.
     */
    /**
     * Set whether the week view should fling horizontally.
     *
     * @return True if it should have horizontal fling enabled.
     */
    var isHorizontalFlingEnabled = true
    /**
     * Get whether the week view should fling vertically.
     *
     * @return True if the week view has vertical fling enabled.
     */
    /**
     * Set whether the week view should fling vertically.
     *
     * @return True if it should have vertical fling enabled.
     */
    var isVerticalFlingEnabled = true
    /**
     * Get the height of AllDay-events.
     *
     * @return Height of AllDay-events.
     */
    /**
     * Set the height of AllDay-events.
     */
    var allDayEventHeight = 100
    /**
     * Get scroll duration
     *
     * @return scroll duration
     */
    /**
     * Set the scroll duration
     */
    var scrollDuration = 150
    private var weekx = 0f

    // Listeners.
    var eventClickListener: EventClickListener? = null
        private set
    var eventLongPressListener: EventLongPressListener? = null
    /**
     * Get event loader in the week view. Event loaders define the  interval after which the events
     * are loaded in week view. For a MonthLoader events are loaded for every month. You can define
     * your custom event loader by extending WeekViewLoader.
     *
     * @return The event loader.
     */
    /**
     * Set event loader in the week view. For example, a MonthLoader. Event loaders define the
     * interval after which the events are loaded in week view. For a MonthLoader events are loaded
     * for every month. You can define your custom event loader by extending WeekViewLoader.
     *
     * @param loader The event loader.
     */
    var weekViewLoader: WeekViewLoader? = null
    var emptyViewClickListener: EmptyViewClickListener? = null
    var emptyViewLongPressListener: EmptyViewLongPressListener? = null
    private val mGestureListener: SimpleOnGestureListener = object : SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            weekx = mCurrentOrigin.x
            goToNearestOrigin()
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            // Check if view is zoomed.
            if (mIsZooming) return true
            when (mCurrentScrollDirection) {
                Direction.NONE -> {

                    // Allow scrolling only in one direction.
                    mCurrentScrollDirection = if (Math.abs(distanceX) > Math.abs(distanceY)) {
                        if (distanceX > 0) {
                            Direction.LEFT
                        } else {
                            Direction.RIGHT
                        }
                    } else {
                        Direction.VERTICAL
                    }
                }

                Direction.LEFT -> {

                    // Change direction if there was enough change.
                    if (Math.abs(distanceX) > Math.abs(distanceY) && distanceX < -mScaledTouchSlop) {
                        mCurrentScrollDirection = Direction.RIGHT
                    }
                }

                Direction.RIGHT -> {

                    // Change direction if there was enough change.
                    if (Math.abs(distanceX) > Math.abs(distanceY) && distanceX > mScaledTouchSlop) {
                        mCurrentScrollDirection = Direction.LEFT
                    }
                }

                else -> {}
            }
            when (mCurrentScrollDirection) {
                Direction.LEFT, Direction.RIGHT -> {
                    mCurrentOrigin.x -= distanceX * xScrollingSpeed
                    ViewCompat.postInvalidateOnAnimation(this@WeekView)
                }

                Direction.VERTICAL -> {
                    mCurrentOrigin.y -= distanceY
                    ViewCompat.postInvalidateOnAnimation(this@WeekView)
                }

                else -> {}
            }
            return true
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (mIsZooming) return true

//            if ((mCurrentFlingDirection == Direction.LEFT && !mHorizontalFlingEnabled) ||
//                    (mCurrentFlingDirection == Direction.RIGHT && !mHorizontalFlingEnabled) ||
//                    (mCurrentFlingDirection == Direction.VERTICAL && !mVerticalFlingEnabled)) {
//                return true;
//            }
            mScroller!!.forceFinished(true)
            mCurrentFlingDirection = mCurrentScrollDirection
            var target = 0f
            when (mCurrentFlingDirection) {
                Direction.LEFT -> {
                    target = weekx - mWidthPerDay * numberOfVisibleDays
                    val va = ValueAnimator.ofFloat(mCurrentOrigin.x, target)
                    va.duration = 70
                    va.addUpdateListener { animation ->
                        mCurrentOrigin.x = animation.animatedValue as Float
                        invalidate()
                    }
                    va.start()
                }

                Direction.RIGHT -> {
                    target = weekx + mWidthPerDay * numberOfVisibleDays
                    val va1 = ValueAnimator.ofFloat(mCurrentOrigin.x, target)
                    va1.duration = 70
                    va1.addUpdateListener { animation ->
                        mCurrentOrigin.x = animation.animatedValue as Float
                        invalidate()
                    }
                    va1.start()
                }

                Direction.VERTICAL -> {
                    mScroller!!.fling(mCurrentOrigin.x.toInt(), mCurrentOrigin.y.toInt(), 0, velocityY.toInt(), Int.MIN_VALUE, Int.MAX_VALUE, -(mHourHeight * 24 + mHeaderHeight + mHeaderRowPadding * 3 + mHeaderMarginBottom + mTimeTextHeight / 2 - height).toInt(), 0)
                    ViewCompat.postInvalidateOnAnimation(this@WeekView)
                }

                else -> {}
            }
            Handler().postDelayed({ invalidate() }, 100)
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            // If the tap was on an event then trigger the callback.
            if (mEventRects != null && eventClickListener != null) {
                val reversedEventRects: List<EventRect> = mEventRects as MutableList<EventRect>
                // Collections.reverse(reversedEventRects);
                for (event in reversedEventRects) {
                    if (event.rectF != null && e.x > event.rectF!!.left && e.x < event.rectF!!.right && e.y > event.rectF!!.top && e.y < event.rectF!!.bottom) {
                        eventClickListener!!.onEventClick(event.event, event.rectF)
                        playSoundEffect(SoundEffectConstants.CLICK)
                        return super.onSingleTapConfirmed(e)
                    }
                }
            }

            Log.e("TAG", "onSingleTapConfirmed: "+"9999" +emptyViewClickListener)
            // If the tap was on in an empty space, then trigger the callback.
            if (emptyViewClickListener != null &&
                    e.x > mHeaderColumnWidth &&
                    e.y > mHeaderHeight + mHeaderRowPadding * 3 + mHeaderMarginBottom) {
                Log.e("TAG", "onSingleTapConfirmed: "+"9999333" )
                val selectedTime = getTimeFromPoint(e.x, e.y)
                if (selectedTime != null) {
                    Log.e("TAG", "onSingleTapConfirmed: "+"999922" )
                    playSoundEffect(SoundEffectConstants.CLICK)
                    emptyViewClickListener!!.onEmptyViewClicked(selectedTime)
                }
            }
            return super.onSingleTapConfirmed(e)
        }

        override fun onLongPress(e: MotionEvent) {
            super.onLongPress(e)
            if (eventLongPressListener != null && mEventRects != null) {
                val reversedEventRects: List<EventRect> = mEventRects as MutableList<EventRect>
                //   Collections.reverse(reversedEventRects);
                for (event in reversedEventRects) {
                    if (event.rectF != null && e.x > event.rectF!!.left && e.x < event.rectF!!.right && e.y > event.rectF!!.top && e.y < event.rectF!!.bottom) {
                        eventLongPressListener!!.onEventLongPress(event.originalEvent, event.rectF)
                        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        return
                    }
                }
            }

            // If the tap was on in an empty space, then trigger the callback.
            if (emptyViewLongPressListener != null && e.x > mHeaderColumnWidth && e.y > mHeaderHeight + mHeaderRowPadding * 3 + mHeaderMarginBottom) {
                val selectedTime = getTimeFromPoint(e.x, e.y)
                if (selectedTime != null) {
                    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    emptyViewLongPressListener!!.onEmptyViewLongPress(selectedTime)
                }
            }
        }
    }
    private var mDateTimeInterpreter: DateTimeInterpreter? = null
    var scrollListener: ScrollListener? = null

    init {

        // Hold references.

        // Get the attribute values (if any).
        val a = mContext.theme.obtainStyledAttributes(attrs, R.styleable.WeekView, 0, 0)
        try {
            mFirstDayOfWeek = a.getInteger(R.styleable.WeekView_firstDayOfWeek, mFirstDayOfWeek)
            mHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_hourHeight, mHourHeight)
            mMinHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_minHourHeight, mMinHourHeight)
            mEffectiveMinHourHeight = mMinHourHeight
            mMaxHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_maxHourHeight, mMaxHourHeight)
            mTextSize = a.getDimensionPixelSize(R.styleable.WeekView_textSize, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mTextSize.toFloat(), mContext.resources.displayMetrics).toInt())
            mHeaderColumnPadding = a.getDimensionPixelSize(R.styleable.WeekView_headerColumnPadding, mHeaderColumnPadding)
            mColumnGap = a.getDimensionPixelSize(R.styleable.WeekView_columnGap, mColumnGap)
            mColumnGap = a.getDimensionPixelSize(R.styleable.WeekView_columnGap, mColumnGap)
            mHeaderColumnTextColor = a.getColor(R.styleable.WeekView_headerColumnTextColor, mHeaderColumnTextColor)
            mNumberOfVisibleDays = a.getInteger(R.styleable.WeekView_noOfVisibleDays, mNumberOfVisibleDays)
            isShowFirstDayOfWeekFirst = a.getBoolean(R.styleable.WeekView_showFirstDayOfWeekFirst, isShowFirstDayOfWeekFirst)
            mHeaderRowPadding = a.getDimensionPixelSize(R.styleable.WeekView_headerRowPadding, mHeaderRowPadding)
            mHeaderRowBackgroundColor = a.getColor(R.styleable.WeekView_headerRowBackgroundColor, mHeaderRowBackgroundColor)
            mDayBackgroundColor = a.getColor(R.styleable.WeekView_dayBackgroundColor, mDayBackgroundColor)
            mFutureBackgroundColor = a.getColor(R.styleable.WeekView_futureBackgroundColor, mFutureBackgroundColor)
            mPastBackgroundColor = a.getColor(R.styleable.WeekView_pastBackgroundColor, mPastBackgroundColor)
            mFutureWeekendBackgroundColor = a.getColor(R.styleable.WeekView_futureWeekendBackgroundColor, mFutureBackgroundColor) // If not set, use the same color as in the week
            mPastWeekendBackgroundColor = a.getColor(R.styleable.WeekView_pastWeekendBackgroundColor, mPastBackgroundColor)
            mNowLineColor = a.getColor(R.styleable.WeekView_nowLineColor, mNowLineColor)
            mNowLineThickness = a.getDimensionPixelSize(R.styleable.WeekView_nowLineThickness, mNowLineThickness)
            mHourSeparatorColor = a.getColor(R.styleable.WeekView_hourSeparatorColor, mHourSeparatorColor)
            mTodayBackgroundColor = a.getColor(R.styleable.WeekView_todayBackgroundColor, mTodayBackgroundColor)
            mHourSeparatorHeight = a.getDimensionPixelSize(R.styleable.WeekView_hourSeparatorHeight, mHourSeparatorHeight)
            mTodayHeaderTextColor = a.getColor(R.styleable.WeekView_todayHeaderTextColor, mTodayHeaderTextColor)
            mEventTextSize = a.getDimensionPixelSize(R.styleable.WeekView_eventTextSize, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mEventTextSize.toFloat(), mContext.resources.displayMetrics).toInt())
            mEventTextColor = a.getColor(R.styleable.WeekView_eventTextColor, mEventTextColor)
            mEventPadding = a.getDimensionPixelSize(R.styleable.WeekView_eventPadding, mEventPadding)
            mHeaderColumnBackgroundColor = a.getColor(R.styleable.WeekView_headerColumnBackground, mHeaderColumnBackgroundColor)
            mDayNameLength = a.getInteger(R.styleable.WeekView_dayNameLength, mDayNameLength)
            mOverlappingEventGap = a.getDimensionPixelSize(R.styleable.WeekView_overlappingEventGap, mOverlappingEventGap)
            mEventMarginVertical = a.getDimensionPixelSize(R.styleable.WeekView_eventMarginVertical, mEventMarginVertical)
            xScrollingSpeed = a.getFloat(R.styleable.WeekView_xScrollingSpeed, xScrollingSpeed)
            eventCornerRadius = a.getDimensionPixelSize(R.styleable.WeekView_eventCornerRadius, eventCornerRadius)
            mShowDistinctPastFutureColor = a.getBoolean(R.styleable.WeekView_showDistinctPastFutureColor, mShowDistinctPastFutureColor)
            mShowDistinctWeekendColor = a.getBoolean(R.styleable.WeekView_showDistinctWeekendColor, mShowDistinctWeekendColor)
            mShowNowLine = a.getBoolean(R.styleable.WeekView_showNowLine, mShowNowLine)
            isHorizontalFlingEnabled = a.getBoolean(R.styleable.WeekView_horizontalFlingEnabled, isHorizontalFlingEnabled)
            isVerticalFlingEnabled = a.getBoolean(R.styleable.WeekView_verticalFlingEnabled, isVerticalFlingEnabled)
            allDayEventHeight = a.getDimensionPixelSize(R.styleable.WeekView_allDayEventHeight, allDayEventHeight)
            scrollDuration = a.getInt(R.styleable.WeekView_scrollDuration, scrollDuration)
        } finally {
            a.recycle()
        }
        init()
    }

    private fun init() {
        // Scrolling initialization.
        mGestureDetector = GestureDetectorCompat(mContext, mGestureListener)
        mScroller = OverScroller(mContext, FastOutLinearInInterpolator())
        mMinimumFlingVelocity = ViewConfiguration.get(mContext).scaledMinimumFlingVelocity
        mScaledTouchSlop = ViewConfiguration.get(mContext).scaledTouchSlop
        shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        shadowPaint!!.style = Paint.Style.FILL
        shadowPaint!!.setShadowLayer(12f, 0f, 0f, Color.GRAY)

        // Important for certain APIs
        setLayerType(LAYER_TYPE_SOFTWARE, shadowPaint)
        // Measure settings for time column.
        mTimeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mTimeTextPaint!!.textAlign = Paint.Align.RIGHT
        mTimeTextPaint!!.textSize = mTextSize.toFloat()
        mTimeTextPaint!!.color = mHeaderColumnTextColor
        val rect = Rect()
        mTimeTextPaint!!.getTextBounds("00 PM", 0, "00 PM".length, rect)
        mTimeTextHeight = rect.height().toFloat()
        mHeaderMarginBottom = mTimeTextHeight / 2
        initTextTimeWidth()

        // Measure settings for header row.
        mHeaderTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mHeaderTextPaint!!.color = mHeaderColumnTextColor
        mHeaderTextPaint!!.textAlign = Paint.Align.CENTER
        mHeaderTextPaint!!.textSize = (mTextSize - 1).toFloat()
        mHeaderTextPaint!!.getTextBounds("00 PM", 0, "00 PM".length, rect)
        mHeaderTextHeight = rect.height().toFloat()
        jheaderEventTextpaint = Paint(Paint.ANTI_ALIAS_FLAG)
        jheaderEventTextpaint!!.color = Color.WHITE
        jheaderEventTextpaint!!.textAlign = Paint.Align.LEFT
        jheaderEventTextpaint!!.textSize = mTextSize.toFloat()
        jheaderEventTextpaint!!.getTextBounds("a", 0, "a".length, rect)
        jheaderEventheight = rect.centerY()
        jtodayHeaderTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        jtodayHeaderTextPaint!!.color = Color.WHITE
        jtodayHeaderTextPaint!!.textAlign = Paint.Align.CENTER
        jtodayHeaderTextPaint!!.textSize = mTextSize * 1.6f
        jHeaderTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        jHeaderTextPaint!!.color = Color.DKGRAY
        jHeaderTextPaint!!.textAlign = Paint.Align.CENTER
        jHeaderTextPaint!!.textSize = mTextSize * 1.6f
        jHeaderTextPaint!!.getTextBounds("00", 0, "00".length, rect)
        jHeaderTextHeight = rect.height().toFloat()

        // Prepare header background paint.
        mHeaderBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mHeaderBackgroundPaint!!.color = mHeaderRowBackgroundColor

        // Prepare day background color paint.
        mDayBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mDayBackgroundPaint!!.color = mDayBackgroundColor
        mFutureBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mFutureBackgroundPaint!!.color = mFutureBackgroundColor
        mPastBackgroundPaint = Paint()
        mPastBackgroundPaint!!.color = mPastBackgroundColor
        mFutureWeekendBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mFutureWeekendBackgroundPaint!!.color = mFutureWeekendBackgroundColor
        mPastWeekendBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPastWeekendBackgroundPaint!!.color = mPastWeekendBackgroundColor

        // Prepare hour separator color paint.
        mHourSeparatorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mHourSeparatorPaint!!.style = Paint.Style.STROKE
        mHourSeparatorPaint!!.strokeWidth = mHourSeparatorHeight.toFloat()
        mHourSeparatorPaint!!.color = mHourSeparatorColor

        // Prepare the "now" line color paint
        mNowLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mNowLinePaint!!.strokeWidth = mNowLineThickness.toFloat()
        mNowLinePaint!!.color = mNowLineColor
        jNowbackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        jNowbackPaint!!.color = mNowLineColor

        // Prepare today background color paint.
        mTodayBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mTodayBackgroundPaint!!.color = mTodayBackgroundColor

        // Prepare today header text color paint.
        mTodayHeaderTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mTodayHeaderTextPaint!!.textAlign = Paint.Align.CENTER
        mTodayHeaderTextPaint!!.textSize = mTextSize.toFloat()
        mTodayHeaderTextPaint!!.typeface = Typeface.DEFAULT_BOLD
        mTodayHeaderTextPaint!!.color = mTodayHeaderTextColor

        // Prepare event background color.
        mEventBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mEventBackgroundPaint!!.color = Color.rgb(174, 208, 238)

        // Prepare header column background color.
        mHeaderColumnBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mHeaderColumnBackgroundPaint!!.color = mHeaderColumnBackgroundColor

        // Prepare event text size and color.
        mEventTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.LINEAR_TEXT_FLAG)
        mEventTextPaint!!.style = Paint.Style.FILL
        mEventTextPaint!!.color = mEventTextColor
        mEventTextPaint!!.textSize = mEventTextSize.toFloat()

        // Set default event color.
        mDefaultEventColor = Color.parseColor("#9fc6e7")
        mScaleDetector = ScaleGestureDetector(mContext, object : OnScaleGestureListener {
            override fun onScaleEnd(detector: ScaleGestureDetector) {
                mIsZooming = false
            }

            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                mIsZooming = true
                goToNearestOrigin()
                return true
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                mNewHourHeight = Math.round(mHourHeight * detector.scaleFactor)
                invalidate()
                return true
            }
        })
    }

    // fix rotation changes
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mAreDimensionsInvalid = true
    }

    /**
     * Initialize time column width. Calculate value with all possible hours (supposed widest text).
     */
    private fun initTextTimeWidth() {
        mTimeTextWidth = 0f
        for (i in 0..23) {
            // Measure time string and get max width.
            val time = dateTimeInterpreter!!.interpretTime(i)
                    ?: throw IllegalStateException("A DateTimeInterpreter must not return null time")
            mTimeTextWidth = Math.max(mTimeTextWidth, mTimeTextPaint!!.measureText(time))
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the header row.
        drawHeaderRowAndEvents(canvas)

        // Draw the time column and all the axes/separators.
        if (mNumberOfVisibleDays != 1) drawTimeColumnAndAxes(canvas)
    }

    fun calculateHeaderHeight() {
        //Make sure the header is the right size (depends on AllDay events)
        var containsAllDayEvent = false
        var noofevent = 1
        if (mEventRects != null && mEventRects!!.size > 0) {
            for (dayNumber in 0 until mNumberOfVisibleDays) {
                val day = firstVisibleDay!!.clone() as Calendar
                day.add(Calendar.DATE, dayNumber)
                for (i in mEventRects!!.indices) {
                    if (WeekViewUtil.isSameDay(mEventRects!![i].event!!.startTime, day) && mEventRects!![i].event!!.isAllDay) {
                        if (mEventRects!![i].noofevent > noofevent) noofevent = mEventRects!![i].noofevent
                        containsAllDayEvent = true
                        break
                    }
                }
                //                if(containsAllDayEvent){
//                    break;
//                }
            }
        }
        //((noofevent-1)*5) where is 5 padding between event
        if (mNumberOfVisibleDays == 1) {
            if (containsAllDayEvent) {
                val without = mHeaderTextHeight + jHeaderTextHeight + mHeaderMarginBottom + 70 + mHeaderRowPadding / 3.0f
                val with = allDayEventHeight * noofevent + (noofevent - 1) * 5 + mHeaderMarginBottom
                mHeaderHeight = Math.max(without, with)
                shadow!!.y = mHeaderHeight - 70 + mHeaderRowPadding * 3
            } else {
                val without = mHeaderTextHeight + jHeaderTextHeight + mHeaderMarginBottom + 70 + mHeaderRowPadding / 3.0f
                mHeaderHeight = without
                shadow!!.y = mHeaderHeight - 70 + mHeaderRowPadding * 3
            }
        } else {
            if (containsAllDayEvent) {
                mHeaderHeight = mHeaderTextHeight + jHeaderTextHeight + (allDayEventHeight * noofevent + (noofevent - 1) * 5 + mHeaderMarginBottom) + 70
                shadow!!.y = mHeaderHeight - 70 + mHeaderRowPadding * 3
            } else {
                mHeaderHeight = mHeaderTextHeight + jHeaderTextHeight + mHeaderMarginBottom + 70
                shadow!!.y = mHeaderHeight - 70 + mHeaderRowPadding * 3
            }
        }
    }

    private fun canvasclipRect(mCanvas: Canvas, left: Float, top: Float, right: Float, bottom: Float, op: Region.Op) {
        if (op == Region.Op.REPLACE) {
            mCanvas.restore()
            mCanvas.save()
            mCanvas.clipRect(left, top, right, bottom)
            return
        }
    }

    private fun drawTimeColumnAndAxes(canvas: Canvas) {
        // Draw the background color for the header column.
        canvas.drawRect(0f, mHeaderHeight + mHeaderRowPadding * 3 - 70, mHeaderColumnWidth, height.toFloat(), mHeaderColumnBackgroundPaint!!)

        // Clip to paint in left column only.
        canvasclipRect(canvas, 0f, mHeaderHeight + mHeaderRowPadding * 3 - 70, mHeaderColumnWidth, height.toFloat(), Region.Op.REPLACE)
        for (i in 0..23) {
            val top = mHeaderHeight + mHeaderRowPadding * 3 + mCurrentOrigin.y + mHourHeight * i + mHeaderMarginBottom

            // Draw the text if its y position is not outside of the visible area. The pivot point of the text is the point at the bottom-right corner.
            val time = dateTimeInterpreter!!.interpretTime(i)
                    ?: throw IllegalStateException("A DateTimeInterpreter must not return null time")
            if (top < height) canvas.drawText(time, mTimeTextWidth + mHeaderColumnPadding, top + mTimeTextHeight, mTimeTextPaint!!)
        }
    }

    private fun drawTimeColumnAndAxes1day(canvas: Canvas, startx: Float) {
        // Draw the background color for the header column.
        // canvas.drawRect(0, mHeaderHeight + mHeaderRowPadding * 3-70, getWidth(), getHeight(), mHeaderColumnBackgroundPaint);

        // Clip to paint in left column only.
        canvasclipRect(canvas, 0f, mHeaderHeight + mHeaderRowPadding * 3 - 70, width.toFloat(), height.toFloat(), Region.Op.REPLACE)
        for (i in 0..23) {
            val top = mHeaderHeight + mHeaderRowPadding * 3 + mCurrentOrigin.y + mHourHeight * i + mHeaderMarginBottom

            // Draw the text if its y position is not outside of the visible area. The pivot point of the text is the point at the bottom-right corner.
            val time = dateTimeInterpreter!!.interpretTime(i)
                    ?: throw IllegalStateException("A DateTimeInterpreter must not return null time")
            if (top < height) canvas.drawText(time, startx + mTimeTextWidth + mHeaderColumnPadding, top + mTimeTextHeight, mTimeTextPaint!!)
        }
        canvasclipRect(canvas, 0f, 0f, width.toFloat(), height.toFloat(), Region.Op.REPLACE)
    }

    private fun drawHeaderRowAndEvents(canvas: Canvas) {
        // Calculate the available width for each day.
        mHeaderColumnWidth = mTimeTextWidth + mHeaderColumnPadding * 2
        mWidthPerDay = width - mHeaderColumnWidth - mColumnGap * (mNumberOfVisibleDays - 1)
        mWidthPerDay = if (mNumberOfVisibleDays == 1) width.toFloat() //mWidthPerDay/mNumberOfVisibleDays
        else mWidthPerDay / mNumberOfVisibleDays
        calculateHeaderHeight() //Make sure the header is the right size (depends on AllDay events)
        val today = WeekViewUtil.today()
        if (mAreDimensionsInvalid) {
            mEffectiveMinHourHeight = Math.max(mMinHourHeight, ((height - mHeaderHeight - mHeaderRowPadding * 3 - mHeaderMarginBottom) / 24).toInt())
            mAreDimensionsInvalid = false
            if (mScrollToDay != null) goToDate(mScrollToDay!!)
            mAreDimensionsInvalid = false
            if (mScrollToHour >= 0) goToHour(mScrollToHour)
            mScrollToDay = null
            mScrollToHour = -1.0
            mAreDimensionsInvalid = false
        }
        if (mIsFirstDraw) {
            mIsFirstDraw = false

            // If the week view is being drawn for the first time, then consider the first day of the week.
            if (mNumberOfVisibleDays >= 7 && today!![Calendar.DAY_OF_WEEK] != mFirstDayOfWeek && isShowFirstDayOfWeekFirst) {
                val difference = today[Calendar.DAY_OF_WEEK] - mFirstDayOfWeek
                mCurrentOrigin.x += (mWidthPerDay + mColumnGap) * difference
            }
        }

        // Calculate the new height due to the zooming.
        if (mNewHourHeight > 0) {
            if (mNewHourHeight < mEffectiveMinHourHeight) mNewHourHeight = mEffectiveMinHourHeight else if (mNewHourHeight > mMaxHourHeight) mNewHourHeight = mMaxHourHeight
            mCurrentOrigin.y = mCurrentOrigin.y / mHourHeight * mNewHourHeight
            mHourHeight = mNewHourHeight
            mNewHourHeight = -1
        }

        // If the new mCurrentOrigin.y is invalid, make it valid.
        if (mCurrentOrigin.y < height - mHourHeight * 24 - mHeaderHeight - mHeaderRowPadding * 3 - mHeaderMarginBottom - mTimeTextHeight / 2) mCurrentOrigin.y = height - mHourHeight * 24 - mHeaderHeight - mHeaderRowPadding * 3 - mHeaderMarginBottom - mTimeTextHeight / 2

        // Don't put an "else if" because it will trigger a glitch when completely zoomed out and
        // scrolling vertically.
        if (mCurrentOrigin.y > 0) {
            mCurrentOrigin.y = 0f
        }

        // Consider scroll offset.
        val leftDaysWithGaps = -Math.ceil((mCurrentOrigin.x / (mWidthPerDay + mColumnGap)).toDouble()).toInt()
        val startFromPixel = mCurrentOrigin.x + (mWidthPerDay + mColumnGap) * leftDaysWithGaps +
                mHeaderColumnWidth
        var startPixel = startFromPixel

        // Prepare to iterate for each day.
        var day = today!!.clone() as Calendar
        day.add(Calendar.HOUR, 6)

        // Prepare to iterate for each hour to draw the hour lines.
        var lineCount = ((height - mHeaderHeight - mHeaderRowPadding * 3 -
                mHeaderMarginBottom) / mHourHeight).toInt() + 1
        lineCount = lineCount * (mNumberOfVisibleDays + 1)
        val hourLines = FloatArray(lineCount * 4)

        // Clear the cache for event rectangles.
        if (mEventRects != null) {
            for (eventRect in mEventRects!!) {
                eventRect.rectF = null
            }
        }

        // Clip to paint events only.
        val stary = 0f
        canvasclipRect(canvas, 0f, stary, width.toFloat(), height.toFloat(), Region.Op.REPLACE)

        // Iterate through each day.
        val oldFirstVisibleDay = firstVisibleDay
        firstVisibleDay = today.clone() as Calendar
        firstVisibleDay!!.add(Calendar.DATE, -Math.round(mCurrentOrigin.x / (mWidthPerDay + mColumnGap)))
        if (firstVisibleDay != oldFirstVisibleDay && scrollListener != null) {
            scrollListener!!.onFirstVisibleDayChanged(firstVisibleDay, oldFirstVisibleDay)
        }
        for (dayNumber in leftDaysWithGaps + 1..leftDaysWithGaps + mNumberOfVisibleDays + 1) {

            // Check if the day is today.
            day = today.clone() as Calendar
            lastVisibleDay = day.clone() as Calendar
            day.add(Calendar.DATE, dayNumber - 1)
            lastVisibleDay!!.add(Calendar.DATE, dayNumber - 2)
            val sameDay = WeekViewUtil.isSameDay(day, today)

            // Get more events if necessary. We want to store the events 3 months beforehand. Get
            // events only when it is the first iteration of the loop.
            if (mEventRects == null || mRefreshEvents || dayNumber == leftDaysWithGaps + 1 && mFetchedPeriod != weekViewLoader!!.toWeekViewPeriodIndex(day).toInt() && Math.abs(mFetchedPeriod - weekViewLoader!!.toWeekViewPeriodIndex(day)) > 0.5) {
                getMoreEvents(day)
                mRefreshEvents = false
                calculateHeaderHeight()
            }

            // Draw background color for each day.
            val start = if (startPixel < mHeaderColumnWidth) mHeaderColumnWidth else startPixel
            //            if (mWidthPerDay + startPixel - start > 0){
//                if (mShowDistinctPastFutureColor){
//                    boolean isWeekend = day.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || day.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
//                    Paint pastPaint = isWeekend && mShowDistinctWeekendColor ? mPastWeekendBackgroundPaint : mPastBackgroundPaint;
//                    Paint futurePaint = isWeekend && mShowDistinctWeekendColor ? mFutureWeekendBackgroundPaint : mFutureBackgroundPaint;
//                    float startY = mHeaderHeight + mHeaderRowPadding * 3 + mTimeTextHeight/2 + mHeaderMarginBottom + mCurrentOrigin.y;
//                    startY=stary;
//                    if (sameDay){
//                        Calendar now = Calendar.getInstance();
//                        float beforeNow = (now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE)/60.0f) * mHourHeight;
//                        canvas.drawRect(start, startY, startPixel + mWidthPerDay, startY+beforeNow, pastPaint);
//                        canvas.drawRect(start, startY+beforeNow, startPixel + mWidthPerDay, getHeight(), futurePaint);
//                    }
//                    else if (day.before(today)) {
//                        canvas.drawRect(start, startY, startPixel + mWidthPerDay, getHeight(), pastPaint);
//                    }
//                    else {
//                        canvas.drawRect(start, startY, startPixel + mWidthPerDay, getHeight(), futurePaint);
//                    }
//
//                    canvas.drawLine(start,startY,start,getHeight(),mHourSeparatorPaint);
//                }
//                else {
//                    float ca=0 ;
//                   // canvas.drawRect(start, ca, startPixel + mWidthPerDay, getHeight(), sameDay ? mTodayBackgroundPaint : mDayBackgroundPaint);
//                    canvas.drawLine(startPixel,ca,startPixel,getHeight(),mHourSeparatorPaint);
//                    canvas.drawLine(startPixel+mWidthPerDay,ca,startPixel+mWidthPerDay,getHeight(),mHourSeparatorPaint);
//
//
//                }
//            }
            canvasclipRect(canvas, 0f, stary, width.toFloat(), height.toFloat(), Region.Op.REPLACE)


            // Prepare the separator lines for hours.
            var i = 0
            for (hourNumber in 0..23) {
                val top = mHeaderHeight + mHeaderRowPadding * 3 + mCurrentOrigin.y + mHourHeight * hourNumber + mTimeTextHeight / 2 + mHeaderMarginBottom
                if (top > mHeaderHeight - 70 + mHeaderRowPadding * 3 && top < height && startPixel + mWidthPerDay - start > 0) {
                    if (mNumberOfVisibleDays != 1) {
                        if (dayNumber == leftDaysWithGaps + 1) hourLines[i * 4] = start - 22 else hourLines[i * 4] = start
                        hourLines[i * 4 + 1] = top
                        hourLines[i * 4 + 2] = startPixel + mWidthPerDay
                        hourLines[i * 4 + 3] = top
                        i++
                    } else {
                        hourLines[i * 4] = startPixel - 22
                        hourLines[i * 4 + 1] = top
                        hourLines[i * 4 + 2] = startPixel + mWidthPerDay - mHeaderColumnWidth
                        hourLines[i * 4 + 3] = top
                        i++
                    }
                }
            }

            // Draw the lines for hours.
            canvas.drawLines(hourLines, mHourSeparatorPaint!!)
            if (mNumberOfVisibleDays != 1) canvasclipRect(canvas, mHeaderColumnWidth, mHeaderHeight + mHeaderRowPadding * 3 - 70, width.toFloat(), height.toFloat(), Region.Op.REPLACE)


            // Draw the events.
            if (mNumberOfVisibleDays != 1) drawEvents(day, startPixel, canvas) else drawEvents(day, startPixel, canvas)
            // Draw the line at the current time.


            // In the next iteration, start from the next day.
            startPixel += mWidthPerDay + mColumnGap
        }

        // Hide everything in the first cell (top left corner).
//        canvas.clipRect(0, 0, mTimeTextWidth + mHeaderColumnPadding * 2, mHeaderHeight + mHeaderRowPadding * 3, Region.Op.INTERSECT);
//        canvas.drawRect(0, 0, mTimeTextWidth + mHeaderColumnPadding * 2, mHeaderHeight + mHeaderRowPadding * 3, mHeaderBackgroundPaint);
//       float bottom = mHeaderHeight + mHeaderRowPadding * 3;
        // Clip to paint header row only.
        if (mNumberOfVisibleDays != 1) canvasclipRect(canvas, mHeaderColumnWidth, 0f, width.toFloat(), height.toFloat(), Region.Op.REPLACE)
        // Draw the header background.
        canvas.drawRect(0f, 0f, width.toFloat(), mHeaderHeight - 70 + mHeaderRowPadding * 3, mHeaderBackgroundPaint!!)
        mHourSeparatorPaint!!.strokeWidth = (mHourSeparatorHeight * 2).toFloat()
        if (mNumberOfVisibleDays != 1) canvas.drawLine(mHeaderColumnWidth, mHeaderHeight + mHeaderRowPadding * 3 - 105, mHeaderColumnWidth, height.toFloat(), mHourSeparatorPaint!!)
        mHourSeparatorPaint!!.strokeWidth = mHourSeparatorHeight.toFloat()
        startPixel = startFromPixel
        val begin = startPixel
        var start = today.clone() as Calendar
        for (dayNumber in leftDaysWithGaps + 1..leftDaysWithGaps + mNumberOfVisibleDays + 1) {
            // Check if the day is today.
            day = today.clone() as Calendar
            day.add(Calendar.DATE, dayNumber - 1)
            val sameDay = WeekViewUtil.isSameDay(day, today)
            val daycompare = day.compareTo(today)
            if (daycompare < 0) {
                jHeaderTextPaint!!.color = Color.parseColor("#606368")
            } else {
                jHeaderTextPaint!!.color = Color.BLACK
            }
            if (dayNumber == leftDaysWithGaps + 1) start = day

            // Draw the day labels.
            val dayLabel1 = dateTimeInterpreter!!.interpretDate(day)
            val dayLabel = dateTimeInterpreter!!.interpretday(day)
                    ?: throw IllegalStateException("A DateTimeInterpreter must not return null date")
            val x = startPixel + mWidthPerDay / 2
            val xx = startPixel - mHeaderColumnWidth / 2.0f
            val y = mHeaderTextHeight + mHeaderRowPadding * 1.76f + jHeaderTextHeight - jHeaderTextHeight / 2.0f
            val size = resources.getDimensionPixelSize(R.dimen.todaysize)
            if (mNumberOfVisibleDays != 1) canvas.drawLine(startPixel, mHeaderHeight + mHeaderRowPadding * 3 - 105, startPixel, height.toFloat(), mHourSeparatorPaint!!) else {
                canvas.drawLine(startPixel, mHeaderRowPadding / 3.0f, startPixel, height.toFloat(), mHourSeparatorPaint!!)
            }
            if (mNumberOfVisibleDays == 1) {
                if (sameDay) canvas.drawRoundRect(xx - size, y - size, xx + size, y + size, size.toFloat(), size.toFloat(), mTodayBackgroundPaint!!)
                canvas.drawText(dayLabel, startPixel - mHeaderColumnWidth / 2.0f, mHeaderTextHeight + mHeaderRowPadding / 3.0f, (if (sameDay) mTodayHeaderTextPaint else mHeaderTextPaint)!!)
                canvas.drawText(dayLabel1!!, startPixel - mHeaderColumnWidth / 2.0f, mHeaderTextHeight + mHeaderRowPadding * 1.76f + jHeaderTextHeight, (if (sameDay) jtodayHeaderTextPaint else jHeaderTextPaint)!!)
                drawAllDayEvents(day, startPixel, canvas, dayNumber, false)
            } else {
                if (sameDay) canvas.drawRoundRect(x - size, y - size, x + size, y + size, size.toFloat(), size.toFloat(), mTodayBackgroundPaint!!)
                canvas.drawText(dayLabel, startPixel + mWidthPerDay / 2, mHeaderTextHeight + mHeaderRowPadding / 3.0f, (if (sameDay) mTodayHeaderTextPaint else mHeaderTextPaint)!!)
                canvas.drawText(dayLabel1!!, startPixel + mWidthPerDay / 2, mHeaderTextHeight + mHeaderRowPadding * 1.76f + jHeaderTextHeight, (if (sameDay) jtodayHeaderTextPaint else jHeaderTextPaint)!!)
                drawAllDayEvents(day, startPixel, canvas, dayNumber, false)
            }
            if (mShowNowLine && sameDay) {
                val startY = mHeaderHeight + mHeaderRowPadding * 3 + mTimeTextHeight / 2 + mHeaderMarginBottom + mCurrentOrigin.y
                val now = Calendar.getInstance()
                val beforeNow = (now[Calendar.HOUR_OF_DAY] + now[Calendar.MINUTE] / 60.0f) * mHourHeight
                val startat = if (startPixel < mHeaderColumnWidth) mHeaderColumnWidth else startPixel
                val wid = mWidthPerDay
                val per = 20 * (1.0f - (startat - startPixel) / wid)
                if (mNumberOfVisibleDays != 1) {
                    if (mNumberOfVisibleDays != 1) canvasclipRect(canvas, 0f, mHeaderHeight + mHeaderRowPadding * 3 - 70, width.toFloat(), height.toFloat(), Region.Op.REPLACE)
                    canvas.drawRoundRect(startat - per, startY + beforeNow - per, startat + per, startY + beforeNow + per, per, per, jNowbackPaint!!)
                    canvas.drawLine(startat, startY + beforeNow, startPixel + wid, startY + beforeNow, mNowLinePaint!!)
                    canvasclipRect(canvas, mHeaderColumnWidth, 0f, width.toFloat(), height.toFloat(), Region.Op.REPLACE)
                } else {
                    canvasclipRect(canvas, 0f, mHeaderHeight + mHeaderRowPadding * 3 - 70, width.toFloat(), height.toFloat(), Region.Op.REPLACE)
                    canvas.drawRoundRect(startPixel - 20, startY + beforeNow - 20, startPixel + 20, startY + beforeNow + 20, 20f, 20f, jNowbackPaint!!)
                    canvas.drawLine(startPixel, startY + beforeNow, startPixel + wid - mHeaderColumnWidth, startY + beforeNow, mNowLinePaint!!)
                    canvasclipRect(canvas, 0f, 0f, width.toFloat(), height.toFloat(), Region.Op.REPLACE)
                }
            }
            if (mNumberOfVisibleDays == 1) drawTimeColumnAndAxes1day(canvas, startPixel - mHeaderColumnWidth)
            startPixel += mWidthPerDay + mColumnGap
        }

//        jheaderEventTextpaint.setColor(Color.BLACK);
//        Calendar jday = (Calendar) today.clone();
//        int df = (int) (mCurrentOrigin.x/mWidthPerDay);
//        jday.add(Calendar.DATE, -df);
//        String tit="test";
//        if (mEventRects != null && mEventRects.size() > 0) {
//
//            Log.e("size",mEventRects.size()+"");
//            for (int i = 0; i < mEventRects.size(); i++) {
//                if (isSameDay(mEventRects.get(i).event.getStartTime(), jday) && mEventRects.get(i).event.isAllDay()&&mEventRects.get(i).event.isIsmoreday()) {
//                    tit=mEventRects.get(i).event.getName();
//                    canvas.drawText(tit, mHeaderColumnWidth, mEventRects.get(i).rectF.centerY() - jheaderEventheight, jheaderEventTextpaint);
//                }
//            }
//        }
    }

    /**
     * Get the time and date where the user clicked on.
     *
     * @param x The x position of the touch event.
     * @param y The y position of the touch event.
     * @return The time and date at the clicked position.
     */
    private fun getTimeFromPoint(x: Float, y: Float): Calendar? {
        val leftDaysWithGaps = -Math.ceil((mCurrentOrigin.x / (mWidthPerDay + mColumnGap)).toDouble()).toInt()
        var startPixel = mCurrentOrigin.x + (mWidthPerDay + mColumnGap) * leftDaysWithGaps +
                mHeaderColumnWidth
        for (dayNumber in leftDaysWithGaps + 1..leftDaysWithGaps + mNumberOfVisibleDays + 1) {
            val start = if (startPixel < mHeaderColumnWidth) mHeaderColumnWidth else startPixel
            if (mWidthPerDay + startPixel - start > 0 && x > start && x < startPixel + mWidthPerDay) {
                val day = WeekViewUtil.today()
                day!!.add(Calendar.DATE, dayNumber - 1)
                val pixelsFromZero = y - mCurrentOrigin.y - mHeaderHeight - mHeaderRowPadding * 3 - mTimeTextHeight / 2 - mHeaderMarginBottom
                val hour = (pixelsFromZero / mHourHeight).toInt()
                val minute = (60 * (pixelsFromZero - hour * mHourHeight) / mHourHeight).toInt()
                day.add(Calendar.HOUR, hour)
                day[Calendar.MINUTE] = minute
                return day
            }
            startPixel += mWidthPerDay + mColumnGap
        }
        return null
    }

    /**
     * Draw all the events of a particular day.
     *
     * @param date           The day.
     * @param startFromPixel The left position of the day area. The events will never go any left from this value.
     * @param canvas         The canvas to draw upon.
     */
    private fun drawEvents(date: Calendar, startFromPixel: Float, canvas: Canvas) {
        if (mEventRects != null && mEventRects!!.size > 0) {
            for (i in mEventRects!!.indices) {
                if (WeekViewUtil.isSameDay(mEventRects!![i].event!!.startTime, date) && !mEventRects!![i].event!!.isAllDay) {

                    // Calculate top.
                    val top = mHourHeight * 24 * mEventRects!![i].top / 1440 + mCurrentOrigin.y + mHeaderHeight + mHeaderRowPadding * 3 + mHeaderMarginBottom + mTimeTextHeight / 2 + mEventMarginVertical

                    // Calculate bottom.
                    var bottom = mEventRects!![i].bottom
                    bottom = mHourHeight * 24 * bottom / 1440 + mCurrentOrigin.y + mHeaderHeight + mHeaderRowPadding * 3 + mHeaderMarginBottom + mTimeTextHeight / 2 - mEventMarginVertical

                    // Calculate left and right.
                    var left: Float = startFromPixel + mEventRects!![i].left * (mWidthPerDay - if (mNumberOfVisibleDays == 1) mHeaderColumnWidth else 0F)
                    if (left < startFromPixel) left += mOverlappingEventGap.toFloat()
                    var right: Float = left + mEventRects!![i].width * (mWidthPerDay - if (mNumberOfVisibleDays == 1) mHeaderColumnWidth else 0F)
                    if (right < startFromPixel + (mWidthPerDay - if (mNumberOfVisibleDays == 1) mHeaderColumnWidth else 0F)) right -= mOverlappingEventGap.toFloat()

                    // Draw the event and the event name on top of it.
                    if (left < right && left < width && top < height && right > mHeaderColumnWidth && bottom > mHeaderHeight + mHeaderRowPadding * 3 + mTimeTextHeight / 2 + mHeaderMarginBottom) {
                        mEventRects!![i].rectF = RectF(left, top, right, bottom)
                        mEventBackgroundPaint!!.color = if (mEventRects!![i].event!!.color == 0) mDefaultEventColor else mEventRects!![i].event!!.color
                        canvas.drawRoundRect(mEventRects!![i].rectF!!, eventCornerRadius.toFloat(), eventCornerRadius.toFloat(), mEventBackgroundPaint!!)
                        drawEventTitle(mEventRects!![i].event, mEventRects!![i].rectF, canvas, top, left)
                    } else mEventRects!![i].rectF = null
                }
            }
        }
    }

    /**
     * Draw all the Allday-events of a particular day.
     *
     * @param date           The day.
     * @param startFromPixel The left position of the day area. The events will never go any left from this value.
     * @param canvas         The canvas to draw upon.
     */
    private fun drawAllDayEvents(date: Calendar, startFromPixel: Float, canvas: Canvas, daynumber: Int, check: Boolean) {
        if (mEventRects != null && mEventRects!!.size > 0) {
            for (i in mEventRects!!.indices) {
                val format1 = SimpleDateFormat("yyyy-MM-dd")
                if (WeekViewUtil.isSameDay(mEventRects!![i].event!!.startTime, date) && mEventRects!![i].event!!.isAllDay) {
                    // Calculate top.
                    var top = 0f
                    // Calculate bottom.
                    top = if (mNumberOfVisibleDays == 1) {
                        mHeaderRowPadding / 3.0f + mEventMarginVertical + mEventRects!![i].top
                    } else {
                        3 * mHeaderRowPadding + mHeaderTextHeight + jHeaderTextHeight + mEventMarginVertical + mEventRects!![i].top
                    }
                    val bottom = top + mEventRects!![i].bottom

                    // Calculate left and right.
                    var left = startFromPixel
                    if (left < startFromPixel) left += mOverlappingEventGap.toFloat()
                    var right: Float = left + (mWidthPerDay - if (mNumberOfVisibleDays == 1) mHeaderColumnWidth else 0F) - 10
                    if (right < startFromPixel + (mWidthPerDay - if (mNumberOfVisibleDays == 1) mHeaderColumnWidth else 0F)) right -= mOverlappingEventGap.toFloat()

                    // Draw the event and the event name on top of it.
                    if (left < right && left < width && top < height && right > mHeaderColumnWidth && bottom > 0) {
                        mEventRects!![i].rectF = RectF(left, top, right, bottom)
                        val mycheck = numberOfVisibleDays != 1 && mEventRects!![i].event!!.isIsmoreday
                        mEventBackgroundPaint!!.color = if (mEventRects!![i].event!!.color == 0) mDefaultEventColor else mEventRects!![i].event!!.color
                        if (mycheck) {
                            if (true) {
                                val startat = if (startFromPixel < mHeaderColumnWidth) mHeaderColumnWidth else startFromPixel
                                val wid = mWidthPerDay
                                mEventRects!![i].rectF!!.left = startat
                                if (mEventRects!![i].event!!.daytype != 1 && startat > 200) {
                                    mEventRects!![i].rectF!!.left = startat - eventCornerRadius * 2
                                }
                                mEventRects!![i].rectF!!.right = startFromPixel + wid
                                val fd = mEventRects!![i].rectF
                                if (mEventRects!![i].event!!.daytype.toLong() == mEventRects!![i].event!!.noofday) {
                                    fd!!.right = fd.right - 10
                                }
                                canvas.drawRoundRect(fd!!, eventCornerRadius.toFloat(), eventCornerRadius.toFloat(), mEventBackgroundPaint!!)
                                if (startFromPixel < mHeaderColumnWidth + 5 || mEventRects!![i].event!!.daytype == 1) {
                                    canvas.drawText(mEventRects!![i].event!!.name!!, startat, mEventRects!![i].rectF!!.centerY() - jheaderEventheight, jheaderEventTextpaint!!)
                                }
                            }
                        } else {
                            canvas.drawRoundRect(mEventRects!![i].rectF!!, eventCornerRadius.toFloat(), eventCornerRadius.toFloat(), mEventBackgroundPaint!!)
                            canvas.drawText(mEventRects!![i].event!!.name!!, mEventRects!![i].rectF!!.left + 12, mEventRects!![i].rectF!!.centerY() - jheaderEventheight, jheaderEventTextpaint!!)
                        }
                    } else mEventRects!![i].rectF = null
                }
            }
        }
    }

    /**
     * Draw the name of the event on top of the event rectangle.
     *
     * @param event        The event of which the title (and location) should be drawn.
     * @param rect         The rectangle on which the text is to be drawn.
     * @param canvas       The canvas to draw upon.
     * @param originalTop  The original top position of the rectangle. The rectangle may have some of its portion outside of the visible area.
     * @param originalLeft The original left position of the rectangle. The rectangle may have some of its portion outside of the visible area.
     */
    private fun drawEventTitle(event: WeekViewEvent?, rect: RectF?, canvas: Canvas, originalTop: Float, originalLeft: Float) {
        if (rect!!.right - rect.left - mEventPadding * 2 < 0) return
        if (rect.bottom - rect.top - mEventPadding * 2 < 0) return

        // Prepare the name of the event.
        val bob = SpannableStringBuilder()
        if (event!!.name != null) {
            bob.append(event.name)
            bob.setSpan(StyleSpan(Typeface.BOLD), 0, bob.length, 0)
            bob.append(' ')
        }

        // Prepare the location of the event.
        if (event!!.location != null) {
            bob.append(event.location)
        }
        val availableHeight = (rect.bottom - originalTop - mEventPadding * 2).toInt()
        val availableWidth = (rect.right - originalLeft - mEventPadding * 2).toInt()

        // Get text dimensions.
        var textLayout = StaticLayout(bob, mEventTextPaint, availableWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false)
        val lineHeight = textLayout.height / textLayout.lineCount
        if (availableHeight >= lineHeight) {
            // Calculate available number of line counts.
            var availableLineCount = availableHeight / lineHeight
            do {
                // Ellipsize text to fit into event rect.
                textLayout = StaticLayout(TextUtils.ellipsize(bob, mEventTextPaint, (availableLineCount * availableWidth).toFloat(), TextUtils.TruncateAt.END), mEventTextPaint, (rect.right - originalLeft - mEventPadding * 2).toInt(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false)

                // Reduce line count.
                availableLineCount--

                // Repeat until text is short enough.
            } while (textLayout.height > availableHeight)

            // Draw text.
            canvas.save()
            canvas.translate(originalLeft + mEventPadding, originalTop + mEventPadding)
            textLayout.draw(canvas)
            canvas.restore()
        }
    }

    /**
     * Gets more events of one/more month(s) if necessary. This method is called when the user is
     * scrolling the week view. The week view stores the events of three months: the visible month,
     * the previous month, the next month.
     *
     * @param day The day where the user is currently is.
     */
    private fun getMoreEvents(day: Calendar) {

        // Get more events if the month is changed.
        if (mEventRects == null) mEventRects = ArrayList()
        check(!(weekViewLoader == null && !isInEditMode)) { "You must provide a MonthChangeListener" }

        // If a refresh was requested then reset some variables.
        if (mRefreshEvents) {
            mEventRects!!.clear()
            mPreviousPeriodEvents = null
            mCurrentPeriodEvents = null
            mNextPeriodEvents = null
            mFetchedPeriod = -1
        }
        if (weekViewLoader != null) {
            val periodToFetch = weekViewLoader!!.toWeekViewPeriodIndex(day).toInt()
            if (!isInEditMode && (mFetchedPeriod < 0 || mFetchedPeriod != periodToFetch || mRefreshEvents)) {
                var previousPeriodEvents: List<WeekViewEvent?>? = null
                var currentPeriodEvents: List<WeekViewEvent?>? = null
                var nextPeriodEvents: List<WeekViewEvent?>? = null
                if (mPreviousPeriodEvents != null && mCurrentPeriodEvents != null && mNextPeriodEvents != null) {
                    if (periodToFetch == mFetchedPeriod - 1) {
                        currentPeriodEvents = mPreviousPeriodEvents
                        nextPeriodEvents = mCurrentPeriodEvents
                    } else if (periodToFetch == mFetchedPeriod) {
                        previousPeriodEvents = mPreviousPeriodEvents
                        currentPeriodEvents = mCurrentPeriodEvents
                        nextPeriodEvents = mNextPeriodEvents
                    } else if (periodToFetch == mFetchedPeriod + 1) {
                        previousPeriodEvents = mCurrentPeriodEvents
                        currentPeriodEvents = mNextPeriodEvents
                    }
                }
                if (currentPeriodEvents == null) currentPeriodEvents = weekViewLoader!!.onLoad(periodToFetch)
                if (previousPeriodEvents == null) previousPeriodEvents = weekViewLoader!!.onLoad(periodToFetch - 1)
                if (nextPeriodEvents == null) nextPeriodEvents = weekViewLoader!!.onLoad(periodToFetch + 1)


                // Clear events.
                mEventRects!!.clear()
                sortAndCacheEvents(previousPeriodEvents)
                sortAndCacheEvents(currentPeriodEvents)
                sortAndCacheEvents(nextPeriodEvents)
                calculateHeaderHeight()
                mPreviousPeriodEvents = previousPeriodEvents
                mCurrentPeriodEvents = currentPeriodEvents
                mNextPeriodEvents = nextPeriodEvents
                mFetchedPeriod = periodToFetch
            }
        }

        // Prepare to calculate positions of each events.
        val tempEvents: MutableList<EventRect> = mEventRects!!
        mEventRects = ArrayList()

        // Iterate through each day with events to calculate the position of the events.
        while (tempEvents.size > 0) {
            val eventRects = ArrayList<EventRect>(tempEvents.size)

            // Get first event for a day.
            val eventRect1: EventRect = tempEvents.removeAt(0)
            eventRects.add(eventRect1)
            var i = 0
            while (i < tempEvents.size) {
                // Collect all other events for same day.
                val eventRect2 = tempEvents[i]
                if (WeekViewUtil.isSameDay(eventRect1.event!!.startTime, eventRect2.event!!.startTime)) {
                    tempEvents.removeAt(i)
                    eventRects.add(eventRect2)
                } else {
                    i++
                }
            }
            computePositionOfEvents(eventRects)
        }
    }

    /**
     * Cache the event for smooth scrolling functionality.
     *
     * @param event The event to cache.
     */
    private fun cacheEvent(event: WeekViewEvent?) {
        if (event!!.startTime.compareTo(event.endTime) >= 0) return
        val splitedEvents = event!!.splitWeekViewEvents()
        for (splitedEvent in splitedEvents!!) {
            mEventRects!!.add(EventRect(splitedEvent, event, null))
        }
    }

    /**
     * Sort and cache events.
     *
     * @param events The events to be sorted and cached.
     */
    private fun sortAndCacheEvents(events: List<WeekViewEvent?>?) {
        sortEvents(events)
        for (event in events!!) {
            cacheEvent(event)
        }
    }

    /**
     * Sorts the events in ascending order.
     *
     * @param events The events to be sorted.
     */
    private fun sortEvents(events: List<WeekViewEvent?>?) {
        Collections.sort(events) { event1, event2 ->
            Log.e("mecheck" + event1!!.name + "," + event2!!.name, event1.myday.toString() + "," + event2.myday)
            if (event1!!.myday > event2!!.myday) -1 else if (event1.myday < event2.myday) 1 else 0
            //                    long end2 = event2.getEndTime().getTimeInMillis();

//                long start1 = event1.getStartTime().getTimeInMillis();
//                long start2 = event2.getStartTime().getTimeInMillis();
//                int comparator = start1 > start2 ? 1 : (start1 < start2 ? -1 : 0);
//                if (comparator == 0) {
//                    long end1 = event1.getEndTime().getTimeInMillis();
//                    long end2 = event2.getEndTime().getTimeInMillis();
//                    comparator = end1 > end2 ? 1 : (end1 < end2 ? -1 : 0);
//                }
//                return comparator;
        }
    }

    /**
     * Calculates the left and right positions of each events. This comes handy specially if events
     * are overlapping.
     *
     * @param eventRects The events along with their wrapper class.
     */
    private fun computePositionOfEvents(eventRects: List<EventRect>) {
        // Make "collision groups" for all events that collide with others.
        val collisionGroups: MutableList<MutableList<EventRect>> = ArrayList()
        for (eventRect in eventRects) {
            var isPlaced = false
            outerLoop@ for (collisionGroup in collisionGroups) {
                for (groupEvent in collisionGroup) {
                    if (isEventsCollide(groupEvent.event, eventRect.event) && groupEvent.event!!.isAllDay == eventRect.event!!.isAllDay) {
                        collisionGroup.add(eventRect)
                        isPlaced = true
                        break@outerLoop
                    }
                }
            }
            if (!isPlaced) {
                val newGroup: MutableList<EventRect> = ArrayList()
                newGroup.add(eventRect)
                collisionGroups.add(newGroup)
            }
        }
        for (collisionGroup in collisionGroups) {
            expandEventsToMaxWidth(collisionGroup)
        }
    }

    /**
     * Expands all the events to maximum possible width. The events will try to occupy maximum
     * space available horizontally.
     *
     * @param collisionGroup The group of events which overlap with each other.
     */
    private fun expandEventsToMaxWidth(collisionGroup: List<EventRect>) {

        // Expand the events to maximum possible width.
        val columns: MutableList<MutableList<EventRect>> = ArrayList()
        columns.add(ArrayList())
        for (eventRect in collisionGroup) {
            var isPlaced = false
            for (column in columns) {
                if (column.size == 0) {
                    column.add(eventRect)
                    isPlaced = true
                } else if (!isEventsCollide(eventRect.event, column[column.size - 1].event)) {
                    column.add(eventRect)
                    isPlaced = true
                    break
                }
            }
            if (!isPlaced) {
                val newColumn: MutableList<EventRect> = ArrayList()
                newColumn.add(eventRect)
                columns.add(newColumn)
            }
        }


        // Calculate left and right position for all the events.
        // Get the maxRowCount by looking in all columns.
        var maxRowCount = 0
        for (column in columns) {
            maxRowCount = Math.max(maxRowCount, column.size)
        }
        for (i in 0 until maxRowCount) {
            // Set the left and right values of the event.
            var j = 0f
            for (column in columns) {
                if (column.size >= i + 1) {
                    val eventRect = column[i]
                    eventRect.width = 1f / columns.size
                    eventRect.left = j / columns.size
                    if (!eventRect.event!!.isAllDay) {
                        eventRect.top = (eventRect.event!!.startTime[Calendar.HOUR_OF_DAY] * 60 + eventRect.event!!.startTime[Calendar.MINUTE]).toFloat()
                        eventRect.bottom = (eventRect.event!!.endTime[Calendar.HOUR_OF_DAY] * 60 + eventRect.event!!.endTime[Calendar.MINUTE]).toFloat()
                    } else {
                        eventRect.top = j * allDayEventHeight + j * 5
                        eventRect.bottom = allDayEventHeight.toFloat()
                        eventRect.noofevent = columns.size
                    }
                    mEventRects!!.add(eventRect)
                }
                j++
            }
        }
    }

    /**
     * Checks if two events overlap.
     *
     * @param event1 The first event.
     * @param event2 The second event.
     * @return true if the events overlap.
     */
    private fun isEventsCollide(event1: WeekViewEvent?, event2: WeekViewEvent?): Boolean {
        val start1 = event1?.startTime!!.timeInMillis
        val end1 = event1?.endTime!!.timeInMillis
        val start2 = event2?.startTime!!.timeInMillis
        val end2 = event2?.endTime!!.timeInMillis
        return !(start1 >= end2 || end1 <= start2)
    }

    /**
     * Checks if time1 occurs after (or at the same time) time2.
     *
     * @param time1 The time to check.
     * @param time2 The time to check against.
     * @return true if time1 and time2 are equal or if time1 is after time2. Otherwise false.
     */
    private fun isTimeAfterOrEquals(time1: Calendar?, time2: Calendar?): Boolean {
        return !(time1 == null || time2 == null) && time1.timeInMillis >= time2.timeInMillis
    }

    override fun invalidate() {
        super.invalidate()
        mAreDimensionsInvalid = true
    }

    fun setOnEventClickListener(listener: EventClickListener?) {
        eventClickListener = listener
    }

    fun setOnEmptyViewClickListener(listener: EmptyViewClickListener?) {
        emptyViewClickListener = listener
    }

    fun setshadow(shadow: View?) {
        this.shadow = shadow
    }

    /////////////////////////////////////////////////////////////////
    //
    //      Functions related to setting and getting the properties.
    //
    /////////////////////////////////////////////////////////////////
    fun setfont(typeface: Typeface?, type: Int) {
        if (type == 0) {
            mTimeTextPaint!!.typeface = typeface
            jHeaderTextPaint!!.typeface = ResourcesCompat.getFont(mContext, R.font.latoregular)
        } else {
            mHeaderTextPaint!!.typeface = typeface
            jheaderEventTextpaint!!.typeface = typeface
        }
        invalidate()
    }

    val monthChangeListener: MonthChangeListener?
        get() = if (weekViewLoader is MonthLoader) (weekViewLoader as MonthLoader).onMonthChangeListener else null

    fun setMonthChangeListener(monthChangeListener: MonthChangeListener) {
        weekViewLoader = MonthLoader(monthChangeListener)
    }

    var dateTimeInterpreter: DateTimeInterpreter?
        /**
         * Get the interpreter which provides the text to show in the header column and the header row.
         *
         * @return The date, time interpreter.
         */
        get() {
            if (mDateTimeInterpreter == null) {
                mDateTimeInterpreter = object : DateTimeInterpreter {
                    override fun interpretday(date: Calendar): String {
                        return try {
                            val flags = DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_NO_YEAR or DateUtils.FORMAT_NUMERIC_DATE
                            val localizedDate = DateUtils.formatDateTime(context, date.time.time, flags)
                            val sdf = if (mDayNameLength == LENGTH_SHORT) SimpleDateFormat("EEEEE", Locale.getDefault()) else SimpleDateFormat("EEE", Locale.getDefault())
                            String.format("%s %s", sdf.format(date.time).uppercase(Locale.getDefault()), localizedDate)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            ""
                        }
                    }

                    override fun interpretDate(date: Calendar): String {
                        val dayOfMonth = date[Calendar.DAY_OF_MONTH]
                        return dayOfMonth.toString() + ""
                    }

                    override fun interpretTime(hour: Int): String {
                        val calendar = Calendar.getInstance()
                        calendar[Calendar.HOUR_OF_DAY] = hour
                        calendar[Calendar.MINUTE] = 0
                        return try {
                            val sdf = if (DateFormat.is24HourFormat(context)) SimpleDateFormat("HH:mm", Locale.getDefault()) else SimpleDateFormat("hh a", Locale.getDefault())
                            sdf.format(calendar.time)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            ""
                        }
                    }
                }
            }
            return mDateTimeInterpreter
        }
        /**
         * Set the interpreter which provides the text to show in the header column and the header row.
         *
         * @param dateTimeInterpreter The date, time interpreter.
         */
        set(dateTimeInterpreter) {
            mDateTimeInterpreter = dateTimeInterpreter

            // Refresh time column width.
            initTextTimeWidth()
        }
    var numberOfVisibleDays: Int
        /**
         * Get the number of visible days in a week.
         *
         * @return The number of visible days in a week.
         */
        get() = mNumberOfVisibleDays
        /**
         * Set the number of visible days in a week.
         *
         * @param numberOfVisibleDays The number of visible days in a week.
         */
        set(numberOfVisibleDays) {
            mNumberOfVisibleDays = numberOfVisibleDays
            mCurrentOrigin.x = 0f
            mCurrentOrigin.y = 0f
            invalidate()
        }
    var hourHeight: Int
        get() = mHourHeight
        set(hourHeight) {
            mNewHourHeight = hourHeight
            invalidate()
        }
    var columnGap: Int
        get() = mColumnGap
        set(columnGap) {
            mColumnGap = columnGap
            invalidate()
        }
    var firstDayOfWeek: Int
        get() = mFirstDayOfWeek
        /**
         * Set the first day of the week. First day of the week is used only when the week view is first
         * drawn. It does not of any effect after user starts scrolling horizontally.
         *
         *
         * **Note:** This method will only work if the week view is set to display more than 6 days at
         * once.
         *
         *
         * @param firstDayOfWeek The supported values are [Calendar.SUNDAY],
         * [Calendar.MONDAY], [Calendar.TUESDAY],
         * [Calendar.WEDNESDAY], [Calendar.THURSDAY],
         * [Calendar.FRIDAY].
         */
        set(firstDayOfWeek) {
            mFirstDayOfWeek = firstDayOfWeek
            invalidate()
        }
    var textSize: Int
        get() = mTextSize
        set(textSize) {
            mTextSize = textSize
            mTodayHeaderTextPaint!!.textSize = mTextSize.toFloat()
            mHeaderTextPaint!!.textSize = mTextSize.toFloat()
            mTimeTextPaint!!.textSize = mTextSize.toFloat()
            invalidate()
        }
    var headerColumnPadding: Int
        get() = mHeaderColumnPadding
        set(headerColumnPadding) {
            mHeaderColumnPadding = headerColumnPadding
            invalidate()
        }
    var headerColumnTextColor: Int
        get() = mHeaderColumnTextColor
        set(headerColumnTextColor) {
            mHeaderColumnTextColor = headerColumnTextColor
            mHeaderTextPaint!!.color = mHeaderColumnTextColor
            mTimeTextPaint!!.color = mHeaderColumnTextColor
            invalidate()
        }
    var headerRowPadding: Int
        get() = mHeaderRowPadding
        set(headerRowPadding) {
            mHeaderRowPadding = headerRowPadding
            invalidate()
        }
    var headerRowBackgroundColor: Int
        get() = mHeaderRowBackgroundColor
        set(headerRowBackgroundColor) {
            mHeaderRowBackgroundColor = headerRowBackgroundColor
            mHeaderBackgroundPaint!!.color = mHeaderRowBackgroundColor
            invalidate()
        }
    var dayBackgroundColor: Int
        get() = mDayBackgroundColor
        set(dayBackgroundColor) {
            mDayBackgroundColor = dayBackgroundColor
            mDayBackgroundPaint!!.color = mDayBackgroundColor
            invalidate()
        }
    var hourSeparatorColor: Int
        get() = mHourSeparatorColor
        set(hourSeparatorColor) {
            mHourSeparatorColor = hourSeparatorColor
            mHourSeparatorPaint!!.color = mHourSeparatorColor
            invalidate()
        }
    var todayBackgroundColor: Int
        get() = mTodayBackgroundColor
        set(todayBackgroundColor) {
            mTodayBackgroundColor = todayBackgroundColor
            mTodayBackgroundPaint!!.color = mTodayBackgroundColor
            invalidate()
        }
    var hourSeparatorHeight: Int
        get() = mHourSeparatorHeight
        set(hourSeparatorHeight) {
            mHourSeparatorHeight = hourSeparatorHeight
            mHourSeparatorPaint!!.strokeWidth = mHourSeparatorHeight.toFloat()
            invalidate()
        }
    var todayHeaderTextColor: Int
        get() = mTodayHeaderTextColor
        set(todayHeaderTextColor) {
            mTodayHeaderTextColor = todayHeaderTextColor
            mTodayHeaderTextPaint!!.color = mTodayHeaderTextColor
            invalidate()
        }
    var eventTextSize: Int
        get() = mEventTextSize
        set(eventTextSize) {
            mEventTextSize = eventTextSize
            mEventTextPaint!!.textSize = mEventTextSize.toFloat()
            invalidate()
        }
    var eventTextColor: Int
        get() = mEventTextColor
        set(eventTextColor) {
            mEventTextColor = eventTextColor
            mEventTextPaint!!.color = mEventTextColor
            invalidate()
        }
    var eventPadding: Int
        get() = mEventPadding
        set(eventPadding) {
            mEventPadding = eventPadding
            invalidate()
        }
    var headerColumnBackgroundColor: Int
        get() = mHeaderColumnBackgroundColor
        set(headerColumnBackgroundColor) {
            mHeaderColumnBackgroundColor = headerColumnBackgroundColor
            mHeaderColumnBackgroundPaint!!.color = mHeaderColumnBackgroundColor
            invalidate()
        }
    var defaultEventColor: Int
        get() = mDefaultEventColor
        set(defaultEventColor) {
            mDefaultEventColor = defaultEventColor
            invalidate()
        }

    @get:Deprecated("")
    @set:Deprecated("")
    var dayNameLength: Int
        /**
         * **Note:** Use [.setDateTimeInterpreter] and
         * [.getDateTimeInterpreter] instead.
         *
         * @return Either long or short day name is being used.
         */
        get() = mDayNameLength
        /**
         * Set the length of the day name displayed in the header row. Example of short day names is
         * 'M' for 'Monday' and example of long day names is 'Mon' for 'Monday'.
         *
         *
         * **Note:** Use [.setDateTimeInterpreter] instead.
         *
         */
        set(length) {
            require(!(length != LENGTH_LONG && length != LENGTH_SHORT)) { "length parameter must be either LENGTH_LONG or LENGTH_SHORT" }
            mDayNameLength = length
        }
    var overlappingEventGap: Int
        get() = mOverlappingEventGap
        /**
         * Set the gap between overlapping events.
         *
         * @param overlappingEventGap The gap between overlapping events.
         */
        set(overlappingEventGap) {
            mOverlappingEventGap = overlappingEventGap
            invalidate()
        }
    var eventMarginVertical: Int
        get() = mEventMarginVertical
        /**
         * Set the top and bottom margin of the event. The event will release this margin from the top
         * and bottom edge. This margin is useful for differentiation consecutive events.
         *
         * @param eventMarginVertical The top and bottom margin.
         */
        set(eventMarginVertical) {
            mEventMarginVertical = eventMarginVertical
            invalidate()
        }
    var isShowDistinctWeekendColor: Boolean
        /**
         * Whether weekends should have a background color different from the normal day background
         * color. The weekend background colors are defined by the attributes
         * `futureWeekendBackgroundColor` and `pastWeekendBackgroundColor`.
         *
         * @return True if weekends should have different background colors.
         */
        get() = mShowDistinctWeekendColor
        /**
         * Set whether weekends should have a background color different from the normal day background
         * color. The weekend background colors are defined by the attributes
         * `futureWeekendBackgroundColor` and `pastWeekendBackgroundColor`.
         *
         * @param showDistinctWeekendColor True if weekends should have different background colors.
         */
        set(showDistinctWeekendColor) {
            mShowDistinctWeekendColor = showDistinctWeekendColor
            invalidate()
        }
    var isShowDistinctPastFutureColor: Boolean
        /**
         * Whether past and future days should have two different background colors. The past and
         * future day colors are defined by the attributes `futureBackgroundColor` and
         * `pastBackgroundColor`.
         *
         * @return True if past and future days should have two different background colors.
         */
        get() = mShowDistinctPastFutureColor
        /**
         * Set whether weekends should have a background color different from the normal day background
         * color. The past and future day colors are defined by the attributes `futureBackgroundColor`
         * and `pastBackgroundColor`.
         *
         * @param showDistinctPastFutureColor True if past and future should have two different
         * background colors.
         */
        set(showDistinctPastFutureColor) {
            mShowDistinctPastFutureColor = showDistinctPastFutureColor
            invalidate()
        }
    var isShowNowLine: Boolean
        /**
         * Get whether "now" line should be displayed. "Now" line is defined by the attributes
         * `nowLineColor` and `nowLineThickness`.
         *
         * @return True if "now" line should be displayed.
         */
        get() = mShowNowLine
        /**
         * Set whether "now" line should be displayed. "Now" line is defined by the attributes
         * `nowLineColor` and `nowLineThickness`.
         *
         * @param showNowLine True if "now" line should be displayed.
         */
        set(showNowLine) {
            mShowNowLine = showNowLine
            invalidate()
        }
    var nowLineColor: Int
        /**
         * Get the "now" line color.
         *
         * @return The color of the "now" line.
         */
        get() = mNowLineColor
        /**
         * Set the "now" line color.
         *
         * @param nowLineColor The color of the "now" line.
         */
        set(nowLineColor) {
            mNowLineColor = nowLineColor
            invalidate()
        }
    var nowLineThickness: Int
        /**
         * Get the "now" line thickness.
         *
         * @return The thickness of the "now" line.
         */
        get() = mNowLineThickness
        /**
         * Set the "now" line thickness.
         *
         * @param nowLineThickness The thickness of the "now" line.
         */
        set(nowLineThickness) {
            mNowLineThickness = nowLineThickness
            invalidate()
        }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isclosed()) return false
        mScaleDetector!!.onTouchEvent(event)
        val `val` = mGestureDetector!!.onTouchEvent(event)

        // Check after call of mGestureDetector, so mCurrentFlingDirection and mCurrentScrollDirection are set.
        if (event.action == MotionEvent.ACTION_UP && !mIsZooming && mCurrentFlingDirection == Direction.NONE) {
            if (mCurrentScrollDirection == Direction.RIGHT || mCurrentScrollDirection == Direction.LEFT) {
                if (true) {
                    var k = 0f
                    if (mCurrentScrollDirection == Direction.RIGHT && numberOfVisibleDays == 1) k = mHeaderColumnWidth
                    var next = 0
                    next = if (numberOfVisibleDays == 7) {
                        if (mCurrentScrollDirection == Direction.LEFT) {
                            (weekx - mWidthPerDay * 7).toInt()
                        } else {
                            (weekx + mWidthPerDay * 7).toInt()
                        }
                    } else {
                        if (mCurrentScrollDirection == Direction.LEFT) {
                            (weekx - mWidthPerDay).toInt()
                        } else {
                            (weekx + mWidthPerDay).toInt()
                        }
                    }
                    val previous = weekx.toInt()
                    //                    if (mCurrentOrigin.x<0){
//                        next*=-1;
//                        previous*=-1;
//                    }
                    if (Math.abs(Math.abs(mCurrentOrigin.x + k) - Math.abs(next)) < Math.abs(Math.abs(mCurrentOrigin.x + k) - Math.abs(previous))) {
                        mCurrentOrigin.x = next.toFloat()
                        ViewCompat.postInvalidateOnAnimation(this@WeekView)
                        Handler().postDelayed({ invalidate() }, 100)
                    } else {
                        mCurrentOrigin.x = previous.toFloat()
                        ViewCompat.postInvalidateOnAnimation(this@WeekView)
                        Handler().postDelayed({ invalidate() }, 100)
                    }

//                    float small=Math.min(Math.abs(next-mCurrentOrigin.x),Math.abs(previous-mCurrentOrigin.x));
//                    int j=1;
//                    if (mCurrentOrigin.x<0)j=-1;
//                    mCurrentOrigin.x=(small-mCurrentOrigin.x)*j;
//                    invalidate();
                }
                //goToNearestOrigin();
            }
            mCurrentScrollDirection = Direction.NONE
        }
        return `val`
    }

    private fun goToNearestOrigin() {
        var leftDays = (mCurrentOrigin.x / (mWidthPerDay + mColumnGap)).toDouble()
        if (numberOfVisibleDays == 1 && mCurrentScrollDirection == Direction.RIGHT) {
            leftDays = ((mCurrentOrigin.x + mHeaderColumnWidth) / (mWidthPerDay + mColumnGap)).toDouble()
        }
        leftDays = if (mCurrentFlingDirection != Direction.NONE) {
            // snap to nearest day
            Math.round(leftDays).toDouble()
        } else if (mCurrentScrollDirection == Direction.LEFT) {
            // snap to last day
            Math.floor(leftDays)
        } else if (mCurrentScrollDirection == Direction.RIGHT) {
            // snap to next day
            Math.ceil(leftDays)
        } else {
            // snap to nearest day
            Math.round(leftDays).toDouble()
        }
        val nearestOrigin = (mCurrentOrigin.x - leftDays * (mWidthPerDay + mColumnGap)).toInt()
        if (nearestOrigin != 0) {
            // Stop current animation.
            mScroller!!.forceFinished(true)
            // Snap to date.
            mScroller!!.startScroll(mCurrentOrigin.x.toInt(), mCurrentOrigin.y.toInt(), -nearestOrigin, 0, (Math.abs(nearestOrigin) / mWidthPerDay * scrollDuration).toInt())
            ViewCompat.postInvalidateOnAnimation(this@WeekView)
        }
        // Reset scrolling and fling direction.
        mCurrentFlingDirection = Direction.NONE
        mCurrentScrollDirection = mCurrentFlingDirection
    }

    /////////////////////////////////////////////////////////////////
    //
    //      Functions related to scrolling.
    //
    /////////////////////////////////////////////////////////////////
    private fun goToNearestOrigin1() {
        var leftDays = (mCurrentOrigin.x / (mWidthPerDay + mColumnGap)).toDouble()
        if (numberOfVisibleDays == 1 && mCurrentScrollDirection == Direction.RIGHT) {
            leftDays = ((mCurrentOrigin.x + mHeaderColumnWidth) / (mWidthPerDay + mColumnGap)).toDouble()
        }
        leftDays = if (mCurrentFlingDirection != Direction.NONE) {
            // snap to nearest day
            Math.round(leftDays).toDouble()
        } else if (mCurrentScrollDirection == Direction.LEFT) {
            // snap to last day
            Math.ceil(leftDays)
        } else if (mCurrentScrollDirection == Direction.RIGHT) {
            // snap to next day
            Math.floor(leftDays)
        } else {
            // snap to nearest day
            Math.round(leftDays).toDouble()
        }
        val nearestOrigin = (mCurrentOrigin.x - leftDays * (mWidthPerDay + mColumnGap)).toInt()
        if (nearestOrigin != 0) {
            // Stop current animation.
            mScroller!!.forceFinished(true)
            // Snap to date.
            mScroller!!.startScroll(mCurrentOrigin.x.toInt(), mCurrentOrigin.y.toInt(), -nearestOrigin, 0, (Math.abs(nearestOrigin) / mWidthPerDay * scrollDuration).toInt())
            ViewCompat.postInvalidateOnAnimation(this@WeekView)
        }
        // Reset scrolling and fling direction.
        mCurrentFlingDirection = Direction.NONE
        mCurrentScrollDirection = mCurrentFlingDirection
    }

    override fun computeScroll() {
        super.computeScroll()
        //        if (mScroller.isFinished()) {
//            if (mCurrentFlingDirection != Direction.NONE) {
//                // Snap to day after fling is finished.
//                Log.e("flingcall","flh"+mScroller.getCurrVelocity());
//               goToNearestOrigin();
//            }
//        } else {
//
//            if (mCurrentFlingDirection != Direction.NONE && forceFinishScroll()) {
//                Log.e("flingcall","finish"+mScroller.getStartX()+","+mMinimumFlingVelocity);
//
//               goToNearestOrigin();
//            }
//            else if (mScroller.computeScrollOffset()) {
//                Log.e("flingcall","main"+mScroller.getStartX()+","+mMinimumFlingVelocity);
//                mCurrentOrigin.y = mScroller.getCurrY();
//                mCurrentOrigin.x = mScroller.getCurrX();
//                ViewCompat.postInvalidateOnAnimation(this);
//            }
//        }
    }

    /**
     * Check if scrolling should be stopped.
     *
     * @return true if scrolling should be stopped before reaching the end of animation.
     */
    private fun forceFinishScroll(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // current velocity only available since api 14
            mScroller!!.currVelocity <= mMinimumFlingVelocity
        } else {
            false
        }
    }

    fun isclosed(): Boolean {
        val mainActivity = mContext as MainActivity
        return mainActivity.isAppBarExpanded()
    }

    /**
     * Show today on the week view.
     */
    fun goToToday() {
        val today = Calendar.getInstance()
        goToDate(today)
    }

    /**
     * Show a specific day on the week view.
     *
     * @param date The date to show.
     */
    fun goToDate(date: Calendar) {
        if (numberOfVisibleDays == 7) {
            val diff = date[Calendar.DAY_OF_WEEK] - firstDayOfWeek
            Log.e("diff", diff.toString() + "")
            if (diff < 0) {
                date.add(Calendar.DAY_OF_MONTH, -(7 - Math.abs(diff)))
            } else {
                date.add(Calendar.DAY_OF_MONTH, -diff)
            }
        }
        mScroller!!.forceFinished(true)
        mCurrentFlingDirection = Direction.NONE
        mCurrentScrollDirection = mCurrentFlingDirection
        date[Calendar.HOUR_OF_DAY] = 0
        date[Calendar.MINUTE] = 0
        date[Calendar.SECOND] = 0
        date[Calendar.MILLISECOND] = 0
        if (mAreDimensionsInvalid) {
            mScrollToDay = date
            return
        }
        mRefreshEvents = true
        val today = Calendar.getInstance()
        today[Calendar.HOUR_OF_DAY] = 0
        today[Calendar.MINUTE] = 0
        today[Calendar.SECOND] = 0
        today[Calendar.MILLISECOND] = 0
        val day = 1000L * 60L * 60L * 24L
        val dateInMillis = date.timeInMillis + date.timeZone.getOffset(date.timeInMillis)
        val todayInMillis = today.timeInMillis + today.timeZone.getOffset(today.timeInMillis)
        val dateDifference = dateInMillis / day - todayInMillis / day
        mCurrentOrigin.x = -dateDifference * (mWidthPerDay + mColumnGap)
        invalidate()
    }
    /////////////////////////////////////////////////////////////////
    //
    //      Public methods.
    //
    /////////////////////////////////////////////////////////////////
    /**
     * Refreshes the view and loads the events again.
     */
    fun notifyDatasetChanged() {
        mRefreshEvents = true
        invalidate()
    }

    /**
     * Vertically scroll to a specific hour in the week view.
     *
     * @param hour The hour to scroll to in 24-hour format. Supported values are 0-24.
     */
    fun goToHour(hour: Double) {
        if (mAreDimensionsInvalid) {
            mScrollToHour = hour
            return
        }
        var verticalOffset = 0
        if (hour > 24) verticalOffset = mHourHeight * 24 else if (hour > 0) verticalOffset = (mHourHeight * hour).toInt()
        if (verticalOffset > mHourHeight * 24 - height + mHeaderHeight + mHeaderRowPadding * 3 + mHeaderMarginBottom) verticalOffset = (mHourHeight * 24 - height + mHeaderHeight + mHeaderRowPadding * 3 + mHeaderMarginBottom).toInt()
        mCurrentOrigin.y = -verticalOffset.toFloat()
        invalidate()
    }

    val firstVisibleHour: Double
        /**
         * Get the first hour that is visible on the screen.
         *
         * @return The first hour that is visible.
         */
        get() = (-mCurrentOrigin.y / mHourHeight).toDouble()

    private enum class Direction {
        NONE, LEFT, RIGHT, VERTICAL
    }

    interface EventClickListener {
        /**
         * Triggered when clicked on one existing event
         *
         * @param event:     event clicked.
         * @param eventRect: view containing the clicked event.
         */
        fun onEventClick(event: WeekViewEvent?, eventRect: RectF?)
    }

    /////////////////////////////////////////////////////////////////
    //
    //      Interfaces.
    //
    /////////////////////////////////////////////////////////////////
    interface EventLongPressListener {
        /**
         * @param event:     event clicked.
         * @param eventRect: view containing the clicked event.
         */
        fun onEventLongPress(event: WeekViewEvent?, eventRect: RectF?)
    }

    interface EmptyViewClickListener {
        /**
         * Triggered when the users clicks on a empty space of the calendar.
         *
         * @param time: [Calendar] object set with the date and time of the clicked position on the view.
         */
        fun onEmptyViewClicked(time: Calendar?)
    }

    interface EmptyViewLongPressListener {
        /**
         * @param time: [Calendar] object set with the date and time of the long pressed position on the view.
         */
        fun onEmptyViewLongPress(time: Calendar?)
    }

    interface ScrollListener {
        /**
         * Called when the first visible day has changed.
         *
         *
         * (this will also be called during the first draw of the weekview)
         *
         * @param newFirstVisibleDay The new first visible day
         * @param oldFirstVisibleDay The old first visible day (is null on the first call).
         */
        fun onFirstVisibleDayChanged(newFirstVisibleDay: Calendar?, oldFirstVisibleDay: Calendar?)
    }

    /**
     * A class to hold reference to the events and their visual representation. An EventRect is
     * actually the rectangle that is drawn on the calendar for a given event. There may be more
     * than one rectangle for a single event (an event that expands more than one day). In that
     * case two instances of the EventRect will be used for a single event. The given event will be
     * stored in "originalEvent". But the event that corresponds to rectangle the rectangle
     * instance will be stored in "event".
     */
    private inner class EventRect
    /**
     * Create a new instance of event rect. An EventRect is actually the rectangle that is drawn
     * on the calendar for a given event. There may be more than one rectangle for a single
     * event (an event that expands more than one day). In that case two instances of the
     * EventRect will be used for a single event. The given event will be stored in
     * "originalEvent". But the event that corresponds to rectangle the rectangle instance will
     * be stored in "event".
     *
     * @param event         Represents the event which this instance of rectangle represents.
     * @param originalEvent The original event that was passed by the user.
     * @param rectF         The rectangle.
     */(var event: WeekViewEvent?, var originalEvent: WeekViewEvent?, var rectF: RectF?) {
        var left = 0f
        var width = 0f
        var top = 0f
        var bottom = 0f
        var noofevent = 0
    }

    companion object {
        @Deprecated("")
        val LENGTH_SHORT = 1

        @Deprecated("")
        val LENGTH_LONG = 2
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        fun RoundedRect(left: Float, top: Float, right: Float, bottom: Float, rx: Float, ry: Float, conformToOriginalPost: Boolean): Path {
            var rx = rx
            var ry = ry
            val path = Path()
            if (rx < 0) rx = 0f
            if (ry < 0) ry = 0f
            val width = right - left
            val height = bottom - top
            if (rx > width / 2) rx = width / 2
            if (ry > height / 2) ry = height / 2
            val widthMinusCorners = width - 2 * rx
            val heightMinusCorners = height - 2 * ry
            path.moveTo(right, top + ry)
            path.arcTo(right - 2 * rx, top, right, top + 2 * ry, 0f, -90f, false) //top-right-corner
            path.rLineTo(-widthMinusCorners, 0f)
            path.arcTo(left, top, left + 2 * rx, top + 2 * ry, 270f, -90f, false) //top-left corner.
            path.rLineTo(0f, heightMinusCorners)
            if (conformToOriginalPost) {
                path.rLineTo(0f, ry)
                path.rLineTo(width, 0f)
                path.rLineTo(0f, -ry)
            } else {
                path.arcTo(left, bottom - 2 * ry, left + 2 * rx, bottom, 180f, -90f, false) //bottom-left corner
                path.rLineTo(widthMinusCorners, 0f)
                path.arcTo(right - 2 * rx, bottom - 2 * ry, right, bottom, 90f, -90f, false) //bottom-right corner
            }
            path.rLineTo(0f, -heightMinusCorners)
            path.close() //Given close, last lineto can be removed.
            return path
        }
    }
}