package com.example.calendarapp

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Point
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.CalendarContract
import android.text.Spannable
import android.text.SpannableString
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AbsListView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.example.calendarapp.Scrollparallax.ScrollParallaxImageView
import com.example.calendarapp.Scrollparallax.VerticalMovingStyle
import com.example.calendarapp.eventCal.CalendarHelper
import com.example.calendarapp.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter
import com.example.calendarapp.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration
import com.example.calendarapp.weekview.DateTimeInterpreter
import com.example.calendarapp.weekview.MonthLoader
import com.example.calendarapp.weekview.WeekView
import com.example.calendarapp.weekview.WeekViewEvent
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.navigation.NavigationView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.joda.time.DateTimeZone
import org.joda.time.Days
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MainActivity : AppCompatActivity(), MyRecyclerView.AppBarTracking,
        WeekView.EventClickListener, MonthLoader.MonthChangeListener, WeekView.EmptyViewClickListener,
        WeekView.EventLongPressListener, WeekView.EmptyViewLongPressListener,
        WeekView.ScrollListener {

    var lasttime: Long = 0
    var mycolor = 0
    lateinit var mNestedView: MyRecyclerView
    lateinit var weekviewcontainer: View
    var mWeekView: WeekView? = null
    private val daysList = arrayOf("", "Monday", "Tuesday", "Wednesday",
            "Thursday", "Friday", "Saturday", "Sunday")
    private var myshadow: View? = null
    private lateinit var monthviewpager: ViewPager
    private lateinit var yearviewpager: ViewPager
    private var alleventlist: HashMap<LocalDate, EventInfo?>? = null
    private lateinit var montheventlist: HashMap<LocalDate, EventInfo>
    private lateinit var drawerLayout: DrawerLayout
    private var mAppBarOffset = 0
    private var mAppBarIdle = true
    private var mAppBarMaxOffset = 0
    private var shadow: View? = null
    private var lastselectedid = R.id.threeday
    private lateinit var mAppBar: AppBarLayout
    private var mIsExpanded = false
    private lateinit var redlay: View
    private lateinit var mArrowImageView: ImageView
    private lateinit var monthname: TextView
    private lateinit var toolbar: Toolbar
    private var lastchangeindex = -1
    private var isappbarclosed = true
    private var month = 0
    private var expandedfirst = 0
    private var roundrect: View? = null
    private var eventnametextview: TextView? = null
    private var eventrangetextview: TextView? = null
    private var holidaytextview: TextView? = null
    private var eventfixstextview: TextView? = null
    private var calendaricon: ImageView? = null
    private var eventview: View? = null
    private var fullview: View? = null
    private lateinit var calendarView: GooglecalenderView
    private var eventalllist: ArrayList<EventModel>? = null
    private var isgivepermission = false
    private var indextrack: HashMap<LocalDate, Int>? = null
    private lateinit var closebtn: ImageButton
    private var dupindextrack: HashMap<LocalDate, Int>? = null
    private val `var` = arrayOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
    private val monthresource = intArrayOf(
            R.drawable.bkg_01_jan,
            R.drawable.bkg_02_feb,
            R.drawable.bkg_03_mar,
            R.drawable.bkg_04_apr,
            R.drawable.bkg_05_may,
            R.drawable.bkg_06_jun,
            R.drawable.bkg_07_jul,
            R.drawable.bkg_08_aug,
            R.drawable.bkg_09_sep,
            R.drawable.bkg_10_oct,
            R.drawable.bkg_11_nov,
            R.drawable.bkg_12_dec
    )

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            drawerLayout!!.openDrawer(Gravity.LEFT)
            return true
        }
        if (item.itemId == R.id.action_favorite) {
            val localDate = LocalDate.now()
            if (yearviewpager!!.visibility == View.VISIBLE && yearviewpager!!.adapter != null) {
                yearviewpager!!.setCurrentItem(localDate.year % 2000, false)
            } else {
                if (monthviewpager!!.visibility == View.VISIBLE && monthviewpager!!.adapter != null) {
                    monthviewpager!!.setCurrentItem(calendarView!!.calculateCurrentMonth(localDate), false)
                }
                if (weekviewcontainer!!.visibility == View.VISIBLE) {
                    val todaydate = Calendar.getInstance()
                    todaydate[Calendar.DAY_OF_MONTH] = localDate.dayOfMonth
                    todaydate[Calendar.MONTH] = localDate.monthOfYear - 1
                    todaydate[Calendar.YEAR] = localDate.year
                    mWeekView!!.goToDate(todaydate)
                }
                val linearLayoutManager = mNestedView!!.layoutManager as LinearLayoutManager?
                mNestedView!!.stopScroll()
                if (indextrack!!.containsKey(LocalDate(localDate.year, localDate.monthOfYear, localDate.dayOfMonth))) {
                    val `val` = indextrack!![LocalDate(localDate.year, localDate.monthOfYear, localDate.dayOfMonth)]
                    if (isAppBarExpanded()) {
                        calendarView!!.setCurrentmonth(LocalDate())
                        expandedfirst = `val`!!
                        topspace = 20
                        linearLayoutManager!!.scrollToPositionWithOffset(`val`, 20)
                        EventBus.getDefault().post(MonthChange(localDate, 0))
                        month = localDate.dayOfMonth
                        lastdate = localDate
                    } else {
                        expandedfirst = `val`!!
                        topspace = 20
                        linearLayoutManager!!.scrollToPositionWithOffset(`val`, 20)
                        EventBus.getDefault().post(MonthChange(localDate, 0))
                        month = localDate.dayOfMonth
                        lastdate = localDate
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    val statusBarHeight: Int
        get() {
            var result = 0
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = resources.getDimensionPixelSize(resourceId)
            }
            return result
        }

    private fun applyFontToMenuItem(mi: MenuItem) {
        val font = ResourcesCompat.getFont(this, R.font.googlesansmed)
        val mNewTitle = SpannableString(mi.title)
        mNewTitle.setSpan(CustomTypefaceSpan("", font), 0, mNewTitle.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        mi.title = mNewTitle
    }

    fun getnavigationHeight(): Int {
        val resources = resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }

    private fun setMargins(view: View, left: Int, top: Int, right: Int, bottom: Int, width: Int, height: Int) {
        if (view.layoutParams is ViewGroup.MarginLayoutParams) {
            view.layoutParams = CoordinatorLayout.LayoutParams(width, height)
            val p = view.layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(left, top, right, bottom)
            view.requestLayout()
        }
    }

    fun closebtnClick() {
        closebtn!!.visibility = View.GONE
        eventnametextview!!.visibility = View.GONE
        roundrect!!.visibility = View.GONE
        eventrangetextview!!.visibility = View.GONE
        calendaricon!!.visibility = View.GONE
        holidaytextview!!.visibility = View.GONE
        eventfixstextview!!.visibility = View.GONE
        val animwidth = ValueAnimator.ofInt(devicewidth, eventview!!.width)
        animwidth.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            val layoutParams = redlay!!.layoutParams
            layoutParams.width = `val`
            redlay!!.layoutParams = layoutParams
        }
        animwidth.duration = 300
        val animheight = ValueAnimator.ofInt(deviceHeight, 0)
        animheight.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            val layoutParams = redlay!!.layoutParams
            layoutParams.height = `val`
            redlay!!.layoutParams = layoutParams
            if (redlay!!.translationZ != 0f && valueAnimator.animatedFraction > 0.7) {
                val shape = GradientDrawable()
                shape.cornerRadius = resources.getDimensionPixelSize(R.dimen.fourdp).toFloat()
                shape.setColor(mycolor)
                redlay!!.background = shape
                redlay!!.translationZ = 0f
                shadow!!.visibility = View.GONE
            }
        }
        animheight.duration = 300
        val animx = ValueAnimator.ofFloat(0f, eventview!!.left.toFloat())
        animx.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Float
            redlay!!.translationX = `val`
        }
        animx.duration = 300
        val animy = ValueAnimator.ofFloat(0f, (fullview!!.top + toolbar!!.height).toFloat())
        animy.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Float
            redlay!!.translationY = `val`
        }
        animy.duration = 300
        animwidth.start()
        animheight.start()
        animy.start()
        animx.start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mWeekView = findViewById<View>(R.id.weekView) as WeekView
        weekviewcontainer = findViewById(R.id.weekViewcontainer)
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        val m = navigationView.menu
        for (i in 0 until m.size()) {
            val mi = m.getItem(i)

            //for aapplying a font to subMenu ...
            val subMenu = mi.subMenu
            if (subMenu != null && subMenu.size() > 0) {
                for (j in 0 until subMenu.size()) {
                    val subMenuItem = subMenu.getItem(j)
                    applyFontToMenuItem(subMenuItem)
                }
            }

            //the method we have create in activity
            applyFontToMenuItem(mi)
        }
        navigationView.setNavigationItemSelectedListener { item ->
            Log.e("itemselect", "itemselect")
            if (item.itemId == R.id.Day) {
                weekviewcontainer.setVisibility(View.VISIBLE)
                monthviewpager!!.visibility = View.GONE
                yearviewpager!!.visibility = View.GONE
                mNestedView!!.visibility = View.GONE
                mWeekView!!.numberOfVisibleDays = 1
                mWeekView!!.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, resources.displayMetrics).toInt()
                mWeekView!!.eventTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, resources.displayMetrics).toInt()
                mWeekView!!.allDayEventHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 26f, resources.displayMetrics).toInt()
                val todaydate = Calendar.getInstance()
                todaydate[Calendar.DAY_OF_MONTH] = lastdate.dayOfMonth
                todaydate[Calendar.MONTH] = lastdate.monthOfYear - 1
                todaydate[Calendar.YEAR] = lastdate.year
                mWeekView!!.goToDate(todaydate)
                val layoutParams = mAppBar!!.layoutParams as CoordinatorLayout.LayoutParams
                (layoutParams.behavior as MyAppBarBehavior?)!!.setScrollBehavior(true)
                mAppBar!!.elevation = 0f
                mArrowImageView!!.visibility = View.VISIBLE
                drawerLayout.closeDrawer(Gravity.LEFT)
            } else if (item.itemId == R.id.Week) {
                weekviewcontainer.setVisibility(View.VISIBLE)
                monthviewpager!!.visibility = View.GONE
                yearviewpager!!.visibility = View.GONE
                mNestedView!!.visibility = View.GONE
                mWeekView!!.numberOfVisibleDays = 7
                mWeekView!!.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, resources.displayMetrics).toInt()
                mWeekView!!.eventTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, resources.displayMetrics).toInt()
                mWeekView!!.allDayEventHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, resources.displayMetrics).toInt()
                val todaydate = Calendar.getInstance()
                todaydate[Calendar.DAY_OF_MONTH] = lastdate.dayOfMonth
                todaydate[Calendar.MONTH] = lastdate.monthOfYear - 1
                todaydate[Calendar.YEAR] = lastdate.year
                mWeekView!!.goToDate(todaydate)
                val layoutParams = mAppBar!!.layoutParams as CoordinatorLayout.LayoutParams
                (layoutParams.behavior as MyAppBarBehavior?)!!.setScrollBehavior(true)
                mAppBar!!.elevation = 0f
                mArrowImageView!!.visibility = View.VISIBLE
                drawerLayout.closeDrawer(Gravity.LEFT)
            } else if (item.itemId == R.id.threeday) {
                weekviewcontainer.setVisibility(View.VISIBLE)
                monthviewpager!!.visibility = View.GONE
                yearviewpager!!.visibility = View.GONE
                mNestedView!!.visibility = View.GONE
                mWeekView!!.numberOfVisibleDays = 3
                mWeekView!!.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, resources.displayMetrics).toInt()
                mWeekView!!.eventTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, resources.displayMetrics).toInt()
                mWeekView!!.allDayEventHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, resources.displayMetrics).toInt()
                val todaydate = Calendar.getInstance()
                todaydate[Calendar.DAY_OF_MONTH] = lastdate.dayOfMonth
                todaydate[Calendar.MONTH] = lastdate.monthOfYear - 1
                todaydate[Calendar.YEAR] = lastdate.year
                mWeekView!!.goToDate(todaydate)
                val layoutParams = mAppBar!!.layoutParams as CoordinatorLayout.LayoutParams
                (layoutParams.behavior as MyAppBarBehavior?)!!.setScrollBehavior(true)
                mAppBar!!.elevation = 0f
                mArrowImageView!!.visibility = View.VISIBLE
                drawerLayout.closeDrawer(Gravity.LEFT)
            } else if (item.itemId == R.id.monthviewitem) {
                mAppBar!!.setExpanded(false, false)
                mNestedView!!.visibility = View.GONE
                weekviewcontainer.setVisibility(View.GONE)
                yearviewpager!!.visibility = View.GONE
                monthviewpager!!.visibility = View.VISIBLE
                val layoutParams = mAppBar!!.layoutParams as CoordinatorLayout.LayoutParams
                (layoutParams.behavior as MyAppBarBehavior?)!!.setScrollBehavior(false)
                mAppBar!!.elevation = 0f
                mArrowImageView!!.visibility = View.INVISIBLE
                drawerLayout.closeDrawer(Gravity.LEFT)
                monthname!!.text = lastdate.toString("MMM")
                monthviewpager!!.setCurrentItem(calendarView!!.calculateCurrentMonth(lastdate), true)
            } else if (item.itemId == R.id.yearviewitem) {
                mAppBar!!.setExpanded(false, false)
                mNestedView!!.visibility = View.GONE
                weekviewcontainer.setVisibility(View.GONE)
                yearviewpager!!.visibility = View.VISIBLE
                monthviewpager!!.visibility = View.GONE
                val layoutParams = mAppBar!!.layoutParams as CoordinatorLayout.LayoutParams
                (layoutParams.behavior as MyAppBarBehavior?)!!.setScrollBehavior(false)
                mAppBar!!.elevation = 0f
                mArrowImageView!!.visibility = View.INVISIBLE
                drawerLayout.closeDrawer(Gravity.LEFT)
                monthname!!.text = lastdate.year.toString() + ""
                yearviewpager!!.setCurrentItem(lastdate.year % 2000, false)
            } else if (item.itemId == R.id.licenceviewitem) {
                val last = lastselectedid
                Handler().postDelayed({
                    navigationView.setCheckedItem(last)
                    drawerLayout.closeDrawer(Gravity.LEFT)
                }, 1000)
                val intent = Intent(this@MainActivity, PrivacyActivity::class.java)
                startActivity(intent)
            } else {
                val localDate = LocalDate()
                val yearstr = if (lastdate.year == localDate.year) "" else lastdate.year.toString() + ""
                monthname!!.text = lastdate.toString("MMMM") + " " + yearstr
                calendarView!!.setCurrentmonth(lastdate)
                calendarView!!.adjustheight()
                mIsExpanded = false
                mAppBar!!.setExpanded(false, false)
                EventBus.getDefault().post(MessageEvent(lastdate))
                monthviewpager!!.visibility = View.GONE
                yearviewpager!!.visibility = View.GONE
                weekviewcontainer.setVisibility(View.GONE)
                mNestedView!!.visibility = View.VISIBLE
                val layoutParams = mAppBar!!.layoutParams as CoordinatorLayout.LayoutParams
                (layoutParams.behavior as MyAppBarBehavior?)!!.setScrollBehavior(true)
                mAppBar!!.elevation = 20f
                mArrowImageView!!.visibility = View.VISIBLE
                drawerLayout.closeDrawer(Gravity.LEFT)
            }
            if (item.itemId != R.id.licenceviewitem) lastselectedid = item.itemId
            item.isChecked = true
            true
        }
        eventalllist = ArrayList()
        indextrack = HashMap()
        dupindextrack = HashMap()
        mAppBar = findViewById(R.id.app_bar)
        redlay = findViewById(R.id.redlay)
        redlay.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        shadow = findViewById(R.id.shadow)
        closebtn = findViewById(R.id.closebtn)
        closebtn.setOnClickListener(View.OnClickListener { closebtnClick() })
        roundrect = findViewById(R.id.roundrect)
        eventnametextview = findViewById(R.id.textView12)
        eventrangetextview = findViewById(R.id.textView13)
        calendaricon = findViewById(R.id.imageView2)
        holidaytextview = findViewById(R.id.textView14)
        eventfixstextview = findViewById(R.id.textView014)
        calendarView = findViewById(R.id.calander)
        calendarView.setPadding(0, statusBarHeight, 0, 0)
        mNestedView = findViewById(R.id.nestedView)
        monthviewpager = findViewById(R.id.monthviewpager)
        monthviewpager.setOffscreenPageLimit(1)
        monthviewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i1: Int) {}
            override fun onPageSelected(i: Int) {
                if (monthviewpager.getVisibility() == View.GONE) return
                if (isAppBarClosed()) {
                    Log.e("selected", i.toString() + "")
                    val localDate = LocalDate()
                    val monthPageAdapter = monthviewpager.getAdapter() as MonthPageAdapter?
                    val monthModel = monthPageAdapter!!.monthModels[i]
                    val year = if (monthModel.year == localDate.year) "" else monthModel.year.toString() + ""
                    monthname!!.text = monthModel.monthnamestr + " " + year
                    lastdate = LocalDate(monthModel.year, monthModel.month, 1)
                    // EventBus.getDefault().post(new MessageEvent(new LocalDate(monthModel.getYear(),monthModel.getMonth(),1)));
                    // if (monthChangeListner!=null)monthChangeListner.onmonthChange(myPagerAdapter.monthModels.get(position));
                } else {
                    // calendarView.setCurrentmonth(i);
                }
            }

            override fun onPageScrollStateChanged(i: Int) {}
        })
        yearviewpager = findViewById(R.id.yearviewpager)
        yearviewpager.setOffscreenPageLimit(1)
        yearviewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i1: Int) {}
            override fun onPageSelected(i: Int) {
                if (yearviewpager.getVisibility() == View.GONE) return
                if (isAppBarClosed()) {
                    Log.e("selected", i.toString() + "")
                    monthname!!.text = (2000 + i).toString() + ""
                    // EventBus.getDefault().post(new MessageEvent(new LocalDate(monthModel.getYear(),monthModel.getMonth(),1)));
                    // if (monthChangeListner!=null)monthChangeListner.onmonthChange(myPagerAdapter.monthModels.get(position));
                } else {
                    // calendarView.setCurrentmonth(i);
                }
            }

            override fun onPageScrollStateChanged(i: Int) {}
        })
        //  setMargins(mNestedView,0,0,0,getnavigationHeight());
        mNestedView.setAppBarTracking(this)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        mNestedView.setLayoutManager(linearLayoutManager)
        val dateAdapter = DateAdapter()
        mNestedView.setAdapter(dateAdapter)
        val headersDecor = StickyRecyclerHeadersDecoration(dateAdapter as StickyRecyclerHeadersAdapter<*>)
        mNestedView.addItemDecoration(headersDecor)
        EventBus.getDefault().register(this)
        monthname = findViewById(R.id.monthname)
        calendarView.setMonthChangeListner(object : MonthChangeListner {
            override fun onmonthChange(monthModel: MonthModel) {
                /**
                 * call when Googlecalendarview is open  scroll viewpager available inside GoogleCalendar
                 */
                /**
                 * call when Googlecalendarview is open  scroll viewpager available inside GoogleCalendar
                 */
                /**
                 * call when Googlecalendarview is open  scroll viewpager available inside GoogleCalendar
                 */

                /**
                 * call when Googlecalendarview is open  scroll viewpager available inside GoogleCalendar
                 */
                /**
                 * call when Googlecalendarview is open  scroll viewpager available inside GoogleCalendar
                 */
                /**
                 * call when Googlecalendarview is open  scroll viewpager available inside GoogleCalendar
                 */
                /**
                 * call when Googlecalendarview is open  scroll viewpager available inside GoogleCalendar
                 */
                /**
                 * call when Googlecalendarview is open  scroll viewpager available inside GoogleCalendar
                 */
                val localDate = LocalDate()
                val year = if (monthModel.year == localDate.year) "" else monthModel.year.toString() + ""
                monthname.setText(monthModel.monthnamestr + " " + year)
                if (weekviewcontainer.getVisibility() == View.VISIBLE) {
                    val todaydate = Calendar.getInstance()
                    todaydate[Calendar.DAY_OF_MONTH] = 1
                    todaydate[Calendar.MONTH] = monthModel.month - 1
                    todaydate[Calendar.YEAR] = monthModel.year
                    mWeekView!!.goToDate(todaydate)
                }
            }
        })
        if (ActivityCompat.checkSelfPermission(this,
                        arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR).toString()) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR), 200)
            }
        } else {
            isgivepermission = true
            val mintime = LocalDate().minusYears(5)
            val maxtime = LocalDate().plusYears(5)
            alleventlist = Utility.readCalendarEvent(this, mintime, maxtime)
            montheventlist = HashMap()
            for (localDate in alleventlist!!.keys) {
                var eventInfo = alleventlist!![localDate]
                while (eventInfo != null) {
                    if (eventInfo.noofdayevent > 1) {
                        var nextmonth = localDate!!.plusMonths(1).withDayOfMonth(1)
                        val enddate = LocalDate(eventInfo.endtime)
                        while (enddate.isAfter(nextmonth)) {
                            if (montheventlist!!.containsKey(nextmonth)) {
                                var firstday = nextmonth.dayOfMonth().withMinimumValue().dayOfWeek().get()
                                if (firstday == 7) firstday = 0
                                val noofdays = Days.daysBetween(nextmonth, enddate).days + firstday
                                val newobj = EventInfo()
                                newobj.title = eventInfo.title
                                newobj.timezone = eventInfo.timezone
                                newobj.isallday = eventInfo.isallday
                                newobj.eventcolor = eventInfo.eventcolor
                                newobj.endtime = eventInfo.endtime
                                newobj.accountname = eventInfo.accountname
                                newobj.isalreadyset = true
                                newobj.starttime = eventInfo.starttime
                                newobj.noofdayevent = noofdays
                                newobj.id = eventInfo.id
                                val beginnode = montheventlist!![nextmonth]
                                if (beginnode != null) {
                                    newobj.nextnode = beginnode
                                }
                                montheventlist!![nextmonth] = newobj
                            } else {
                                var firstday = nextmonth.dayOfMonth().withMinimumValue().dayOfWeek().get()
                                if (firstday == 7) firstday = 0
                                val noofdays = Days.daysBetween(nextmonth, enddate).days + firstday
                                val newobj = EventInfo()
                                newobj.title = eventInfo.title
                                newobj.timezone = eventInfo.timezone
                                newobj.accountname = eventInfo.accountname
                                newobj.isallday = eventInfo.isallday
                                newobj.eventcolor = eventInfo.eventcolor
                                newobj.endtime = eventInfo.endtime
                                newobj.isalreadyset = true
                                newobj.starttime = eventInfo.starttime
                                newobj.noofdayevent = noofdays
                                newobj.id = eventInfo.id
                                montheventlist!![nextmonth] = newobj
                            }
                            Log.e("nextmonth", nextmonth.toString())
                            Log.e("jdata" + localDate.toString() + "," + eventInfo.noofdayevent, eventInfo.title + "," + LocalDate(eventInfo.starttime) + "," + LocalDate(eventInfo.endtime))
                            nextmonth = nextmonth.plusMonths(1).withDayOfMonth(1)
                        }
                    }
                    eventInfo = eventInfo.nextnode
                }
            }
            calendarView.init(alleventlist, mintime, maxtime)
            calendarView.setCurrentmonth(LocalDate())
            calendarView.adjustheight()
            mIsExpanded = false
            mAppBar.setExpanded(false, false)
        }
        toolbar = findViewById(R.id.toolbar)
        toolbar.setPadding(0, statusBarHeight, 0, 0)
        //        expandCollapse = findViewById(R.id.expandCollapseButton);
        mArrowImageView = findViewById(R.id.arrowImageView)
        if (monthviewpager.getVisibility() == View.VISIBLE || yearviewpager.getVisibility() == View.VISIBLE) {
            val layoutParams = mAppBar.getLayoutParams() as CoordinatorLayout.LayoutParams
            (layoutParams.behavior as MyAppBarBehavior?)!!.setScrollBehavior(false)
            mAppBar.setElevation(0f)
            mArrowImageView.setVisibility(View.INVISIBLE)
        } else {
            val layoutParams = mAppBar.getLayoutParams() as CoordinatorLayout.LayoutParams
            (layoutParams.behavior as MyAppBarBehavior?)!!.setScrollBehavior(true)
            mAppBar.setElevation(20f)
            mArrowImageView.setVisibility(View.VISIBLE)
        }
        mNestedView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var llm = mNestedView.getLayoutManager() as LinearLayoutManager?
            var dateAdapter = mNestedView.getAdapter() as DateAdapter?
            var mydy = 0
            private val offset = 0
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (mAppBarOffset != 0 && isappbarclosed && newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    calendarView.setCurrentmonth(dateAdapter.geteventallList()!![expandedfirst].localDate)
                    calendarView.adjustheight()
                    mIsExpanded = false
                    mAppBar.setExpanded(false, false)
                    Log.e("callme", "statechange")
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (isappbarclosed) {
                    var pos = llm!!.findFirstVisibleItemPosition()
                    val view = llm!!.findViewByPosition(pos)
                    var currentmonth = dateAdapter.geteventallList()!![pos].localDate.monthOfYear
                    if (dateAdapter.geteventallList()!![pos].type == 1) {
                        if (dy > 0 && Math.abs(view!!.top) > 100) {
                            if (month != currentmonth) EventBus.getDefault().post(MonthChange(dateAdapter.geteventallList()!![pos].localDate, dy))
                            month = currentmonth
                            lastdate = dateAdapter.geteventallList()!![pos].localDate
                            expandedfirst = pos
                        } else if (dy < 0 && Math.abs(view!!.top) < 100 && pos - 1 > 0) {
                            pos--
                            currentmonth = dateAdapter.geteventallList()!![pos].localDate.monthOfYear
                            if (month != currentmonth) EventBus.getDefault().post(MonthChange(dateAdapter.geteventallList()!![pos].localDate, dy))
                            month = currentmonth
                            lastdate = dateAdapter.geteventallList()!![pos].localDate.dayOfMonth().withMaximumValue()
                            expandedfirst = pos
                        }
                        //                       if (dy>=0){
//                           if (Math.abs(view.getTop())>100){
//                               offset=0;
//                               mydy=dy;
//                              // calendarView.setCurrentmonth(dateAdapter.geteventallList().get(pos).getLocalDate());
//                               if (month!=currentmonth)EventBus.getDefault().post(new MonthChange(dateAdapter.geteventallList().get(pos).getLocalDate()));
//                               month=currentmonth;
//
//                           }
//                           else {
//                               if (pos-1>0)firstitem=pos-1;
//                               lastdate=lastdate.minusDays(1);
//                           }
//                       }
//                       else if (dy<0){
//                            Log.e("viewtop",view.getTop()+"");
//                           if (Math.abs(view.getTop())<10){
//                               offset=0;
//                               mydy=dy;
//                              // calendarView.setCurrentmonth(dateAdapter.geteventallList().get(pos).getLocalDate());
//                               if (month!=currentmonth)EventBus.getDefault().post(new MonthChange(dateAdapter.geteventallList().get(pos).getLocalDate()));
//                               month=currentmonth;
//                           }
//                           else {
//                               if (pos+1<dateAdapter.getItemCount())firstitem=pos+1;
//
//                               lastdate=lastdate.plusDays(1);
//                           }
//                       }
                    } else {
                        lastdate = dateAdapter.geteventallList()!![pos].localDate
                        expandedfirst = pos
                    }
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })
        setSupportActionBar(toolbar)
        val ab = supportActionBar
        if (ab != null) {
            supportActionBar!!.setDisplayShowTitleEnabled(false)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)
        }
        mAppBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (mAppBarOffset != verticalOffset) {
                mAppBarOffset = verticalOffset
                mAppBarMaxOffset = -mAppBar.getTotalScrollRange()
                //calendarView.setTranslationY(mAppBarOffset);
                //calendarView.setLayoutParams(new CollapsingToolbarLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,500));
                val totalScrollRange = appBarLayout.totalScrollRange
                val progress = (-verticalOffset).toFloat() / totalScrollRange.toFloat()
                if ((monthviewpager.getVisibility() == View.GONE || yearviewpager.getVisibility() == View.GONE) && mNestedView.getVisibility() == View.VISIBLE) mAppBar.setElevation(20 + 20 * Math.abs(1 - progress))
                if (weekviewcontainer.getVisibility() == View.VISIBLE) {
                    mAppBar.setElevation(20 - 20 * Math.abs(progress))
                }
                if (Math.abs(progress) > 0.45) {
                    val params = myshadow!!.layoutParams
                    params.height = (resources.getDimensionPixelSize(R.dimen.fourdp) * Math.abs(progress)).toInt()
                    myshadow!!.layoutParams = params
                }
                mArrowImageView.setRotation(progress * 180)
                mIsExpanded = verticalOffset == 0
                mAppBarIdle = mAppBarOffset >= 0 || mAppBarOffset <= mAppBarMaxOffset
                val alpha = -verticalOffset.toFloat() / totalScrollRange
                if (mAppBarOffset == -appBarLayout.totalScrollRange) {
                    isappbarclosed = true
                    setExpandAndCollapseEnabled(false)
                } else {
                    setExpandAndCollapseEnabled(true)
                }
                if (mAppBarOffset == 0) {
                    expandedfirst = linearLayoutManager.findFirstVisibleItemPosition()
                    if (mNestedView.getVisibility() == View.VISIBLE) {
                        topspace = linearLayoutManager.findViewByPosition(linearLayoutManager.findFirstVisibleItemPosition())!!.top //uncomment jigs 28 feb
                    }
                    if (isappbarclosed) {
                        isappbarclosed = false
                        mNestedView.stopScroll()

                        //linearLayoutManager.scrollToPositionWithOffset(expandedfirst,0);
                        calendarView.setCurrentmonth(lastdate)
                    }
                }
            }
        })
        findViewById<View>(R.id.backsupport).setOnClickListener(
                View.OnClickListener { //
                    if (monthviewpager.getVisibility() == View.VISIBLE || yearviewpager.getVisibility() == View.VISIBLE) return@OnClickListener
                    mIsExpanded = !mIsExpanded
                    mNestedView.stopScroll()
                    mAppBar.setExpanded(mIsExpanded, true)
                })

        /////////////////weekview implemention/////
        myshadow = findViewById(R.id.myshadow)
        mWeekView!!.setshadow(myshadow)
        mWeekView!!.setfont(ResourcesCompat.getFont(this, R.font.googlesans_regular), 0)
        mWeekView!!.setfont(ResourcesCompat.getFont(this, R.font.googlesansmed), 1)

        // Show a toast message about the touched event.
        mWeekView!!.setOnEventClickListener(this)

        mWeekView!!.setOnEmptyViewClickListener(this)
        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        mWeekView!!.setMonthChangeListener(this)

        // Set long press listener for events.
        mWeekView!!.eventLongPressListener = this

        // Set long press listener for empty view
        mWeekView!!.emptyViewLongPressListener = this
        mWeekView!!.scrollListener = this

        // Set up a date time interpreter to interpret how the date and time will be formatted in
        // the week view. This is optional.
        setupDateTimeInterpreter(false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val mintime = LocalDate().minusYears(5)
            val maxtime = LocalDate().plusYears(5)
            alleventlist = Utility.readCalendarEvent(this, mintime, maxtime)
            montheventlist = HashMap()
            for (localDate in alleventlist!!.keys) {
                var eventInfo = alleventlist!![localDate]
                while (eventInfo != null) {
                    if (eventInfo.noofdayevent > 1) {
                        var nextmonth = localDate!!.plusMonths(1).withDayOfMonth(1)
                        val enddate = LocalDate(eventInfo.endtime)
                        while (enddate.isAfter(nextmonth)) {
                            if (montheventlist!!.containsKey(nextmonth)) {
                                var firstday = nextmonth.dayOfMonth().withMinimumValue().dayOfWeek().get()
                                if (firstday == 7) firstday = 0
                                val noofdays = Days.daysBetween(nextmonth, enddate).days + firstday
                                val newobj = EventInfo()
                                newobj.title = eventInfo.title
                                newobj.timezone = eventInfo.timezone
                                newobj.isallday = eventInfo.isallday
                                newobj.eventcolor = eventInfo.eventcolor
                                newobj.endtime = eventInfo.endtime
                                newobj.isalreadyset = true
                                newobj.starttime = eventInfo.starttime
                                newobj.noofdayevent = noofdays
                                newobj.id = eventInfo.id
                                val beginnode = montheventlist[nextmonth]
                                if (beginnode != null) {
                                    newobj.nextnode = beginnode
                                }
                                montheventlist[nextmonth] = newobj
                            } else {
                                var firstday = nextmonth.dayOfMonth().withMinimumValue().dayOfWeek().get()
                                if (firstday == 7) firstday = 0
                                val noofdays = Days.daysBetween(nextmonth, enddate).days + firstday
                                val newobj = EventInfo()
                                newobj.title = eventInfo.title
                                newobj.timezone = eventInfo.timezone
                                newobj.isallday = eventInfo.isallday
                                newobj.eventcolor = eventInfo.eventcolor
                                newobj.endtime = eventInfo.endtime
                                newobj.isalreadyset = true
                                newobj.starttime = eventInfo.starttime
                                newobj.noofdayevent = noofdays
                                newobj.id = eventInfo.id
                                montheventlist[nextmonth] = newobj
                            }
                            Log.e("nextmonth", nextmonth.toString())
                            Log.e("jdata" + localDate.toString() + "," + eventInfo.noofdayevent, eventInfo.title + "," + LocalDate(eventInfo.starttime) + "," + LocalDate(eventInfo.endtime))
                            nextmonth = nextmonth.plusMonths(1).withDayOfMonth(1)
                        }
                    }
                    eventInfo = eventInfo.nextnode
                }
            }
            calendarView.init(alleventlist, mintime, maxtime)
            Handler().postDelayed({
                isgivepermission = true
                lastdate = LocalDate()
                calendarView.setCurrentmonth(LocalDate())
                calendarView.adjustheight()
                mIsExpanded = false
                mAppBar.setExpanded(false, false)
                mWeekView!!.notifyDatasetChanged()
                //                    LinearLayoutManager linearLayoutManager= (LinearLayoutManager) mNestedView.getLayoutManager();
//                    if (indextrack.containsKey(new LocalDate())){
//                        smoothScroller.setTargetPosition(indextrack.get(new LocalDate()));
//                        linearLayoutManager.scrollToPositionWithOffset(indextrack.get(new LocalDate()),0);
//                    }
//                    else {
//                        for (int i=0;i<eventalllist.size();i++){
//                            if (eventalllist.get(i).getLocalDate().getMonthOfYear()==new LocalDate().getMonthOfYear()&&eventalllist.get(i).getLocalDate().getYear()==new LocalDate().getYear()){
//                                linearLayoutManager.scrollToPositionWithOffset(i,0);
//                                break;
//                            }
//                        }
//                    }
            }, 10)
        }
    }

    /**
     * this call when user is scrolling on mNestedView(recyclerview) and month will change
     * or when toolbar top side current date button selected
     */
    @Subscribe
    fun onEvent(event: MonthChange) {
        if (!isAppBarExpanded()) {
            val localDate = LocalDate()
            val year = if (event.message.year == localDate.year) "" else event.message.year.toString() + ""
            monthname.text = event.message.toString("MMMM") + " " + year
            val diff = System.currentTimeMillis() - lasttime
            val check = diff > 600
            if (check && event.mdy > 0) {
                monthname.translationY = 35f
                mArrowImageView.translationY = 35f
                lasttime = System.currentTimeMillis()
                monthname.animate().translationY(0f).setDuration(200).start()
                mArrowImageView.animate().translationY(0f).setDuration(200).start()
            } else if (check && event.mdy < 0) {
                monthname.translationY = -35f
                mArrowImageView.translationY = -35f
                lasttime = System.currentTimeMillis()
                monthname.animate().translationY(0f).setDuration(200).start()
                mArrowImageView.animate().translationY(0f).setDuration(200).start()
            }
        }
    }

    /**
     * call when Googlecalendarview is open and tap on any date or scroll viewpager available inside GoogleCalendar
     */
    @Subscribe
    fun onEvent(event: MessageEvent) {
        val previous = lastchangeindex
        if (previous != -1) {
            var totalremove = 0
            for (k in 1..3) {
                if (eventalllist!![previous].eventname == "dupli" || eventalllist!![previous].eventname == "click") {
                    totalremove++
                    val eventModel = eventalllist!!.removeAt(previous)
                }
            }
            indextrack!!.clear()
            indextrack!!.putAll(dupindextrack!!)
            mNestedView.adapter!!.notifyDataSetChanged()
        }
        val linearLayoutManager = mNestedView.layoutManager as LinearLayoutManager?
        if (indextrack!!.containsKey(event.message)) {
            val index = indextrack!![event.message]!!
            val type = eventalllist!![index].type
            if (type == 0 || type == 2) {
                lastdate = event.message
                expandedfirst = index
                topspace = 20
                linearLayoutManager!!.scrollToPositionWithOffset(expandedfirst, 20)
                lastchangeindex = -1
            } else {
                lastdate = event.message
                var ind = indextrack!![event.message]
                ind = ind!! + 1
                for (i in ind!! until eventalllist!!.size) {
                    if (event.message.isBefore(eventalllist!![i].localDate)) {
                        ind = i
                        break
                    }
                }
                lastchangeindex = ind
                val typeselect = if (eventalllist!![ind + 1].type == 200) 200 else 100
                if (!eventalllist!![ind - 1].eventname.startsWith("dup")) {
                    eventalllist!!.add(ind, EventModel("dupli", event.message, typeselect))
                    ind = ind + 1
                }
                expandedfirst = ind
                eventalllist!!.add(ind, EventModel("click", event.message, 1000))
                ind = ind + 1
                if (!eventalllist!![ind].eventname.startsWith("dup")) {
                    eventalllist!!.add(ind, EventModel("dupli", event.message, typeselect))
                }
                mNestedView!!.adapter!!.notifyDataSetChanged()
                topspace = 20
                linearLayoutManager!!.scrollToPositionWithOffset(expandedfirst, 20)
                for (i in lastchangeindex until eventalllist!!.size) {
                    if (!eventalllist!![i].eventname.startsWith("dup")) indextrack!![eventalllist!![i].localDate] = i
                }
            }
        } else {
            var ind = indextrack!![event.message.dayOfWeek().withMinimumValue().minusDays(1)]
            ind = ind!! + 1
            for (i in ind!! until eventalllist!!.size) {
                if (event.message.isBefore(eventalllist!![i].localDate)) {
                    ind = i
                    break
                }
            }
            lastchangeindex = ind
            val typeselect = if (eventalllist!![ind + 1].type == 200) 200 else 100
            if (!eventalllist!![ind - 1].eventname.startsWith("dup")) {
                eventalllist!!.add(ind, EventModel("dupli", event.message, typeselect))
                ind = ind + 1
            }
            expandedfirst = ind
            eventalllist!!.add(ind, EventModel("click", event.message, 1000))
            ind = ind + 1
            if (!eventalllist!![ind].eventname.startsWith("dup")) {
                eventalllist!!.add(ind, EventModel("dupli", event.message, typeselect))
            }
            mNestedView!!.adapter!!.notifyDataSetChanged()
            topspace = 20
            linearLayoutManager!!.scrollToPositionWithOffset(expandedfirst, 20)
            for (i in lastchangeindex until eventalllist!!.size) {
                if (!eventalllist!![i].eventname.startsWith("dup")) indextrack!![eventalllist!![i].localDate] = i
            }
        }
    }

    private val deviceHeight: Int
        private get() {
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getRealSize(size)
            val height1 = size.y
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            val height = displayMetrics.heightPixels
            val width = displayMetrics.widthPixels
            return height1
        }
    private val devicewidth: Int
        private get() {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            val height = displayMetrics.heightPixels
            return displayMetrics.widthPixels
        }

    override fun onBackPressed() {
        if (closebtn.visibility == View.VISIBLE) {
            closebtnClick()
        } else if (mIsExpanded) {
            mIsExpanded = false
            mNestedView.stopScroll()
            mAppBar.setExpanded(false, true)
        } else if (mNestedView.visibility == View.VISIBLE) {
            monthviewpager.setCurrentItem(calendarView.calculateCurrentMonth(lastdate), false)
            mNestedView.visibility = View.GONE
            monthviewpager.visibility = View.VISIBLE
            val layoutParams = mAppBar.layoutParams as CoordinatorLayout.LayoutParams
            (layoutParams.behavior as MyAppBarBehavior?)!!.setScrollBehavior(false)
            mAppBar.elevation = 0f
            mArrowImageView.visibility = View.INVISIBLE
        } else {
            EventBus.getDefault().unregister(this)
            super.onBackPressed()
            finish()
        }
    }

    /**
     * call only one time after googlecalendarview init() method is done
     */
    @Subscribe
    fun onEvent(event: AddEvent) {
        eventalllist = event.arrayList
        val tv = TypedValue()
        if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            val actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
            val monthheight = deviceHeight - actionBarHeight - getnavigationHeight() - statusBarHeight
            val recyheight = monthheight - resources.getDimensionPixelSize(R.dimen.monthtopspace)
            val singleitem = (recyheight - 18) / 6

            //monthviewpager.setAdapter(new MonthViewPagerAdapter(MainActivity.this,event.getMonthModels(),singleitem));
            monthviewpager.adapter = MonthPageAdapter(supportFragmentManager, event.monthModels, singleitem)
            monthviewpager.setCurrentItem(calendarView.calculateCurrentMonth(LocalDate.now()), false)
            yearviewpager.adapter = YearPageAdapter(supportFragmentManager)
            yearviewpager.setCurrentItem(LocalDate.now().year % 2000, false)
        }
        indextrack = event.indextracker
        for ((key, value) in indextrack!!) {
            dupindextrack!![key] = value
        }
        if (mNestedView.isAttachedToWindow) {
            mNestedView.adapter!!.notifyDataSetChanged()
        }
        Handler().postDelayed({
            val localDate = LocalDate()
            val linearLayoutManager = mNestedView.layoutManager as LinearLayoutManager?
            if (indextrack!!.containsKey(LocalDate.now())) {
                val `val` = indextrack!!.get(LocalDate.now())
                expandedfirst = `val`!!
                topspace = 20
                linearLayoutManager!!.scrollToPositionWithOffset(expandedfirst, 20)
                EventBus.getDefault().post(MonthChange(localDate, 0))
                month = localDate.dayOfMonth
                lastdate = localDate
            }
        }, 100)
    }

    private fun setExpandAndCollapseEnabled(enabled: Boolean) {
        if (mNestedView.isNestedScrollingEnabled != enabled) {
            ViewCompat.setNestedScrollingEnabled(mNestedView, enabled)
        }
    }

    override fun isAppBarClosed(): Boolean {
        return isappbarclosed
    }

    override fun appbaroffset(): Int {
        return expandedfirst
    }

    fun selectdateFromMonthPager(year: Int, month: Int, day: Int) {
        lastdate = LocalDate(year, month, day)
        val localDate = LocalDate()
        val yearstr = if (lastdate.year == localDate.year) "" else lastdate.year.toString() + ""
        monthname.text = lastdate.toString("MMMM") + " " + yearstr
        calendarView.setCurrentmonth(lastdate)
        calendarView.adjustheight()
        mIsExpanded = false
        mAppBar.setExpanded(false, false)
        EventBus.getDefault().post(MessageEvent(LocalDate(year, month, day)))
        monthviewpager.visibility = View.GONE
        yearviewpager.visibility = View.GONE
        mNestedView.visibility = View.VISIBLE
        val layoutParams = mAppBar.layoutParams as CoordinatorLayout.LayoutParams
        (layoutParams.behavior as MyAppBarBehavior?)!!.setScrollBehavior(true)
        mAppBar.elevation = 20f
        mArrowImageView.visibility = View.VISIBLE
    }

    override fun isAppBarExpanded(): Boolean {
        return mAppBarOffset == 0
    }

    override fun isAppBarIdle(): Boolean {
        return mAppBarIdle
    }

    ///////////////////////////////////weekview implemention///////////////////////////////////////
    /* Function to reverse the linked list */
    fun reverse(node: EventInfo?): EventInfo? {
        var node = node
        var prev: EventInfo? = null
        var current = node
        var next: EventInfo? = null
        while (current != null) {
            next = current.nextnode
            current.nextnode = prev!!
            prev = current
            current = next
        }
        node = prev
        return node
    }

    override fun onMonthChange(newYear: Int, newMonth: Int): List<WeekViewEvent> {
        if (!isgivepermission) return ArrayList()
        val jmontheventlist = HashMap(montheventlist)
        val initial = LocalDate(newYear, newMonth, 1)
        val length = initial.dayOfMonth().maximumValue
        val events: MutableList<WeekViewEvent> = ArrayList()
        for (i in 1..length) {
            val localDate = LocalDate(newYear, newMonth, i)
            if (alleventlist!!.containsKey(localDate) || jmontheventlist.containsKey(localDate)) {
                var eventInfo: EventInfo? = null
                if (alleventlist!!.containsKey(localDate)) {
                    eventInfo = alleventlist!![localDate]
                }
                if (i == 1) {
                    if (jmontheventlist.containsKey(localDate)) {
                        val containevent = HashMap<String, String>()
                        var movecheck = jmontheventlist[localDate]
                        var newobj = EventInfo(movecheck)
                        eventInfo = newobj
                        containevent[movecheck!!.id.toString() + ""] = "1"
                        while (movecheck!!.nextnode != null) {
                            movecheck = movecheck.nextnode
                            newobj.nextnode = EventInfo(movecheck)
                            newobj = newobj.nextnode!!
                            if (movecheck != null) {
                                containevent[movecheck.id.toString() + ""] = "1"
                            }
                        }
                        val infolist: MutableList<EventInfo> = ArrayList()
                        var originalevent = alleventlist!![localDate]
                        while (originalevent != null) {
                            if (!containevent.containsKey(originalevent.id.toString() + "")) {
                                infolist.add(originalevent)
                            }
                            originalevent = originalevent.nextnode
                        }
                        for (eventInfo1 in infolist) {
                            newobj.nextnode = EventInfo(eventInfo1)
                            newobj = newobj.nextnode!!
                        }
                        //  eventInfo=reverse(eventInfo);
                        Log.e("jeventinfo", eventInfo.title + "" + localDate)
                    }
                }
                while (eventInfo != null) {
                    val startTime = Calendar.getInstance(TimeZone.getTimeZone(eventInfo.timezone))
                    if (eventInfo.isalreadyset) {
                        startTime.timeInMillis = localDate.toDateTimeAtStartOfDay(DateTimeZone.forTimeZone(startTime.timeZone)).millis
                    } else {
                        startTime.timeInMillis = eventInfo.starttime
                    }
                    val endTime = Calendar.getInstance(TimeZone.getTimeZone(eventInfo.timezone)) as Calendar
                    endTime.timeInMillis = eventInfo.endtime
                    val enddate = LocalDate(endTime)
                    val maxdate = LocalDate(newYear, newMonth, length)
                    if (enddate.isAfter(maxdate)) {
                        val localDateTime = LocalDateTime(newYear, newMonth, length, 23, 59, 59)
                        val f = if (eventInfo.isallday) 0 else 1000
                        endTime.timeInMillis = localDateTime.toDateTime().millis + 1000
                    }
                    Log.e("title:" + eventInfo.title, LocalDate(eventInfo.starttime).toString())
                    val dau = Days.daysBetween(LocalDate(eventInfo.endtime), LocalDate(eventInfo.starttime)).days
                    val event = WeekViewEvent(eventInfo.id.toLong(), eventInfo.title, startTime, endTime, eventInfo.accountname)
                    event.myday = eventInfo.noofdayevent
                    event.isAllDay = eventInfo.isallday
                    event.color = eventInfo.eventcolor
                    //                    if (eventInfo.isallday)event.setColor(getResources().getColor(R.color.event_color_04));
//                    else event.setColor(getResources().getColor(R.color.event_color_02));
                    events.add(event)
                    eventInfo = eventInfo.nextnode
                }
            }
        }
        return events
    }

    private fun setupDateTimeInterpreter(shortDate: Boolean) {
        mWeekView!!.dateTimeInterpreter = object : DateTimeInterpreter {
            override fun interpretday(date: Calendar): String {
                val weekdayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())
                var weekday = weekdayNameFormat.format(date.time)
                val format = SimpleDateFormat(" M/d", Locale.getDefault())

                // All android api level do not have a standard way of getting the first letter of
                // the week day name. Hence we get the first char programmatically.
                // Details: http://stackoverflow.com/questions/16959502/get-one-letter-abbreviation-of-week-day-of-a-date-in-java#answer-16959657
                if (mWeekView!!.numberOfVisibleDays == 7) weekday = weekday[0].toString()
                return weekday.uppercase(Locale.getDefault())
            }

            override fun interpretDate(date: Calendar): String {
                val dayOfMonth = date[Calendar.DAY_OF_MONTH]
                return dayOfMonth.toString() + ""
            }

            override fun interpretTime(hour: Int): String {
                return if (hour > 11) (hour - 12).toString() + " PM" else if (hour == 0) "12 AM" else "$hour AM"
            }
        }
    }

    protected fun getEventTitle(time: Calendar?): String {
        return String.format("Event of %02d:%02d %s/%d", time!![Calendar.HOUR_OF_DAY], time[Calendar.MINUTE], time[Calendar.MONTH] + 1, time[Calendar.DAY_OF_MONTH])
    }

    override fun onEventClick(event: WeekViewEvent?, eventRect: RectF?) {
        if (isAppBarExpanded()) {
            mIsExpanded = !mIsExpanded
            mNestedView.stopScroll()
            mAppBar.setExpanded(mIsExpanded, true)
            return
        }
        eventnametextview!!.text = event!!.name
        if (event.isAllDay == false) {
            val start = LocalDateTime(event.startTime.timeInMillis, DateTimeZone.forTimeZone(event.startTime.timeZone))
            val end = LocalDateTime(event.endTime.timeInMillis, DateTimeZone.forTimeZone(event.endTime.timeZone))
            val sf = if (start.toString("a") == end.toString("a")) "" else "a"
            val rangetext = daysList[start.dayOfWeek] + ", " + start.toString("d MMM") + " · " + start.toString("h:mm $sf") + " - " + end.toString("h:mm a")
            eventrangetextview!!.text = rangetext
        } else if (event.isIsmoreday) {
            val localDate = LocalDate(event.actualstart!!.timeInMillis, DateTimeZone.forTimeZone(event.startTime.timeZone))
            val todaydate = LocalDate.now()
            val nextday = localDate.plusDays((event.noofday - 1).toInt())
            if (localDate.year == todaydate.year) {
                val rangetext = daysList[localDate.dayOfWeek] + ", " + localDate.toString("d MMM") + " - " + daysList[nextday.dayOfWeek] + ", " + nextday.toString("d MMM")
                eventrangetextview!!.text = rangetext
            } else {
                val rangetext = daysList[localDate.dayOfWeek] + ", " + localDate.toString("d MMM, YYYY") + " - " + daysList[nextday.dayOfWeek] + ", " + nextday.toString("d MMM, YYYY")
                eventrangetextview!!.text = rangetext
            }
        } else {
            val localDate = LocalDate(event.startTime.timeInMillis)
            val todaydate = LocalDate.now()
            if (localDate.year == todaydate.year) {
                val rangetext = daysList[localDate.dayOfWeek] + ", " + localDate.toString("d MMM")
                eventrangetextview!!.text = rangetext
            } else {
                val rangetext = daysList[localDate.dayOfWeek] + ", " + localDate.toString("d MMM, YYYY")
                eventrangetextview!!.text = rangetext
            }
        }
        holidaytextview!!.text = event.accountname
        closebtn.visibility = View.VISIBLE
        eventnametextview!!.visibility = View.GONE
        roundrect!!.visibility = View.GONE
        eventrangetextview!!.visibility = View.GONE
        calendaricon!!.visibility = View.GONE
        holidaytextview!!.visibility = View.GONE
        eventfixstextview!!.visibility = View.GONE
        val view = View(this)
        val layoutParams1 = ViewGroup.LayoutParams(eventRect!!.width().toInt(), eventRect.height().toInt())
        view.left = eventRect.left.toInt()
        view.top = eventRect.top.toInt()
        view.right = eventRect.right.toInt()
        view.bottom = eventRect.bottom.toInt()
        view.layoutParams = layoutParams1
        redlay.visibility = View.VISIBLE
        val layoutParams = redlay.layoutParams
        layoutParams.height = eventRect.height().toInt()
        layoutParams.width = eventRect.width().toInt()
        redlay.layoutParams = layoutParams
        redlay.translationX = eventRect.left
        redlay.translationY = eventRect.top + toolbar.height
        if (event.color != 0) {
            val shape = GradientDrawable()
            shape.cornerRadius = resources.getDimensionPixelSize(R.dimen.fourdp).toFloat()
            mycolor = event.color
            shape.setColor(mycolor)
            redlay.background = shape
            roundrect!!.background = shape
        } else {
            val shape = GradientDrawable()
            shape.cornerRadius = resources.getDimensionPixelSize(R.dimen.fourdp).toFloat()
            mycolor = Color.parseColor("#009688")
            shape.setColor(mycolor)
            redlay.background = shape
            roundrect!!.background = shape
        }

        //  GradientDrawable drawable = (GradientDrawable) holder.eventtextview.getBackground();

//               if (eventalllist.get(position).getType()==0)drawable.setColor(eventalllist.get(position).getColor());
//               else drawable.setColor(Color.BLACK);
        redlay.translationZ = 0f
        val animwidth = ValueAnimator.ofInt(redlay.width, devicewidth)
        animwidth.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            val layoutParams = redlay.layoutParams
            layoutParams.width = `val`
            redlay.layoutParams = layoutParams
        }
        animwidth.duration = 300
        val animheight = ValueAnimator.ofInt(redlay.height, deviceHeight)
        animheight.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            val layoutParams = redlay.layoutParams
            layoutParams.height = `val`
            redlay.layoutParams = layoutParams
            if (redlay.translationZ == 0f && valueAnimator.animatedFraction > 0.2) {
                redlay.setBackgroundColor(Color.WHITE)
                shadow!!.visibility = View.VISIBLE
                redlay.translationZ = resources.getDimensionPixelSize(R.dimen.tendp).toFloat()
            }
        }
        animheight.duration = 300
        val animx = ValueAnimator.ofFloat(redlay.translationX, 0f)
        animx.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Float
            redlay.translationX = `val`
        }
        animx.duration = 300
        val animy = ValueAnimator.ofFloat(redlay.translationY, 0f)
        animy.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Float
            redlay.translationY = `val`
        }
        animy.duration = 300
        animheight.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                Handler().postDelayed({
                    closebtn.visibility = View.VISIBLE
                    eventnametextview!!.visibility = View.VISIBLE
                    roundrect!!.visibility = View.VISIBLE
                    eventrangetextview!!.visibility = View.VISIBLE
                    calendaricon!!.visibility = View.VISIBLE
                    holidaytextview!!.visibility = View.VISIBLE
                    eventfixstextview!!.visibility = View.VISIBLE
                }, 150)
            }
        })
        animwidth.start()
        animheight.start()
        animy.start()
        animx.start()
        eventview = view
        fullview = view
    }

    override fun onEventLongPress(event: WeekViewEvent?, eventRect: RectF?) {
        Toast.makeText(this, "Long pressed event: " + event!!.name, Toast.LENGTH_SHORT).show()
    }

    override fun onEmptyViewLongPress(time: Calendar?) {
        Toast.makeText(this, "Empty view long pressed: " + getEventTitle(time), Toast.LENGTH_SHORT).show()
    }

    override fun onFirstVisibleDayChanged(newFirstVisibleDay: Calendar?, oldFirstVisibleDay: Calendar?) {
        if (weekviewcontainer!!.visibility == View.GONE || !isgivepermission) return
        if (isAppBarClosed()) {
            val localDate = LocalDate(newFirstVisibleDay!![Calendar.YEAR], newFirstVisibleDay[Calendar.MONTH] + 1, newFirstVisibleDay[Calendar.DAY_OF_MONTH])
            lastdate = localDate
            val year = if (localDate.year == LocalDate.now().year) "" else localDate.year.toString() + ""
            if (monthname!!.text != localDate.toString("MMM") + " " + year) {
                lastdate = localDate
                calendarView!!.setCurrentmonth(localDate)
                calendarView!!.adjustheight()
                mIsExpanded = false
                mAppBar!!.setExpanded(false, false)
                monthname!!.text = localDate.toString("MMM") + " " + year
            }

            // EventBus.getDefault().post(new MessageEvent(new LocalDate(monthModel.getYear(),monthModel.getMonth(),1)));
            // if (monthChangeListner!=null)monthChangeListner.onmonthChange(myPagerAdapter.monthModels.get(position));
        } else {
            // calendarView.setCurrentmonth(i);
        }
    }

    internal inner class MonthPageAdapter//            for (int position=0;position<monthModels.size();position++){
//                firstFragments.add(MonthFragment.newInstance(monthModels.get(position).getMonth(), monthModels.get(position).getYear(), monthModels.get(position).getFirstday(), monthModels.get(position).getDayModelArrayList(), alleventlist, singleitemheight));
//            }
    // private ArrayList<MonthFragment> firstFragments=new ArrayList<>();
    (fragmentManager: FragmentManager?, //        public ArrayList<MonthFragment> getFirstFragments() {
            //            return firstFragments;
            //        }
     val monthModels: ArrayList<MonthModel>, private val singleitemheight: Int) : FragmentStatePagerAdapter(fragmentManager!!) {
        // Returns total number of pages
        override fun getCount(): Int {
            return monthModels.size
        }

        // Returns the fragment to display for that page
        override fun getItem(position: Int): Fragment {
            return MonthFragment.Companion.newInstance(monthModels[position].month, monthModels[position].year, monthModels[position].firstday, monthModels[position].dayModelArrayList, alleventlist, singleitemheight, montheventlist)
        }
    }

    internal inner class YearPageAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return YearFragment.Companion.newInstance(2000 + position)
        }

        override fun getCount(): Int {
            return 30
        }
    }

    inner class DateAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder?> {
        var today = LocalDate.now()
        fun geteventallList(): ArrayList<EventModel>? {
            return eventalllist
        }

        override fun getItemViewType(position: Int): Int {
            if (position > 1 && eventalllist!![position].type == 0 && getHeaderId(position) == getHeaderId(position - 1)) return 5
            if (position > 1 && eventalllist!![position].type == 3 && eventalllist!![position - 1].type == 1) return 7
            return if (position + 1 < eventalllist!!.size && eventalllist!![position].type == 3 && (eventalllist!![position + 1].type == 1 || eventalllist!![position + 1].type == 0)) 6 else eventalllist!![position].type
        }

        fun getHeaderItemViewType(position: Int): Int {
            return eventalllist!![position].type
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == 0) {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.view_item, parent, false)
                ItemViewHolder(view)
            } else if (viewType == 5) {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.viewitemlessspace, parent, false)
                ItemViewHolder(view)
            } else if (viewType == 100) {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.extraspace, parent, false)
                object : RecyclerView.ViewHolder(view) {}
            } else if (viewType == 200) {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.liitlespace, parent, false)
                object : RecyclerView.ViewHolder(view) {}
            } else if (viewType == 1) {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.viewlast, parent, false)
                EndViewHolder(view)
            } else if (viewType == 2) {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.noplanlay, parent, false)
                NoplanViewHolder(view)
            } else if (viewType == 1000) {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.noplanlittlespace, parent, false)
                NoplanViewHolder(view)
            } else if (viewType == 6) {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.rangelayextrabottomspace, parent, false)
                RangeViewHolder(view)
            } else if (viewType == 7) {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.rangelayextratopspace, parent, false)
                RangeViewHolder(view)
            } else {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.rangelay, parent, false)
                RangeViewHolder(view)
            }
        }

        override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
            val viewtype = getItemViewType(position)
            if (viewtype == 0 || viewtype == 5) {
                val holder = viewHolder as ItemViewHolder
                val shape = GradientDrawable()
                shape.cornerRadius = resources.getDimensionPixelSize(R.dimen.fourdp).toFloat()
                shape.setColor(eventalllist!![position].color)
                //  GradientDrawable drawable = (GradientDrawable) holder.eventtextview.getBackground();

//               if (eventalllist.get(position).getType()==0)drawable.setColor(eventalllist.get(position).getColor());
//               else drawable.setColor(Color.BLACK);
                holder.eventtextview.background = shape
                holder.eventtextview.text = eventalllist!![position].eventname
                if (position + 1 < eventalllist!!.size && eventalllist!![position].localDate == today && (eventalllist!![position + 1].localDate != today || eventalllist!![position + 1].type == 100 || eventalllist!![position + 1].type == 200)) {
                    holder.circle.visibility = View.VISIBLE
                    holder.line.visibility = View.VISIBLE
                } else {
                    holder.circle.visibility = View.GONE
                    holder.line.visibility = View.GONE
                }
            } else if (viewtype == 1) {
                val holder = viewHolder as EndViewHolder
                holder.eventimageview.setImageResource(monthresource[eventalllist!![position].localDate.monthOfYear - 1])
                holder.monthname.text = eventalllist!![position].localDate.toString("MMMM YYYY")
            } else if (viewtype == 2 || viewtype == 100 || viewtype == 200 || viewtype == 1000) {
            } else {
                val holder = viewHolder as RangeViewHolder
                holder.rangetextview.text = eventalllist!![position].eventname.replace("tojigs".toRegex(), "")
            }
        }

        override fun getHeaderId(position: Int): Long {
            if (eventalllist!![position].type == 1) return position.toLong() else if (eventalllist!![position].type == 3) return position.toLong() else if (eventalllist!![position].type == 100) return position.toLong() else if (eventalllist!![position].type == 200) return position.toLong()
            val localDate = eventalllist!![position].localDate
            val uniquestr = "" + localDate.dayOfMonth + localDate.monthOfYear + localDate.year
            return uniquestr.toLong()
        }

        override fun onCreateHeaderViewHolder(parent: ViewGroup, position: Int): RecyclerView.ViewHolder {
            val viewtype = getHeaderItemViewType(position)
            return if (viewtype == 2) {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.todayheader, parent, false)
                object : RecyclerView.ViewHolder(view) {}
            } else if (viewtype == 0 && eventalllist!![position].localDate == today) {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.todayheader, parent, false)
                object : RecyclerView.ViewHolder(view) {}
            } else if (viewtype == 1 || viewtype == 3 || viewtype == 100 || viewtype == 200) {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.empty, parent, false)
                object : RecyclerView.ViewHolder(view) {}
            } else {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.headerview, parent, false)
                object : RecyclerView.ViewHolder(view) {}
            }
        }

        override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
            val viewtype = getHeaderItemViewType(position)
            if (viewtype == 0 || viewtype == 2 || viewtype == 1000) {
                val vartextView = holder!!.itemView.findViewById<TextView>(R.id.textView9)
                val datetextView = holder.itemView.findViewById<TextView>(R.id.textView10)
                vartextView.text = `var`[eventalllist!![position].localDate.dayOfWeek - 1]
                datetextView.text = eventalllist!![position].localDate.dayOfMonth.toString() + ""
                holder.itemView.tag = position
            } else {
            }
        }

        override fun getItemCount(): Int {
            return eventalllist!!.size
        }

        internal inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var eventtextview: TextView
            var circle: View
            var line: View

            init {
                eventtextview = itemView.findViewById(R.id.view_item_textview)
                eventtextview.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View) {
                        if (isAppBarExpanded()) {
                            mIsExpanded = !mIsExpanded
                            mNestedView!!.stopScroll()
                            mAppBar!!.setExpanded(mIsExpanded, true)
                            return
                        }
                        var eventInfo = alleventlist!![eventalllist!![adapterPosition].localDate]
                        val sfs = eventalllist!![adapterPosition].eventname
                        while (eventInfo != null && !sfs.startsWith(eventInfo.title!!)) {
                            eventInfo = eventInfo.nextnode
                        }
                        eventnametextview!!.text = eventInfo!!.title
                        if (eventInfo.isallday == false) {
                            val start = LocalDateTime(eventInfo.starttime, DateTimeZone.forID(eventInfo.timezone))
                            val end = LocalDateTime(eventInfo.endtime, DateTimeZone.forID(eventInfo.timezone))
                            val sf = if (start.toString("a") == end.toString("a")) "" else "a"
                            val rangetext = daysList[start.dayOfWeek] + ", " + start.toString("d MMM") + " · " + start.toString("h:mm $sf") + " - " + end.toString("h:mm a")
                            eventrangetextview!!.text = rangetext
                        } else if (eventInfo.noofdayevent > 1) {
                            val localDate = LocalDate(eventInfo.starttime, DateTimeZone.forID(eventInfo.timezone))
                            val todaydate = LocalDate.now()
                            val nextday = localDate.plusDays(eventInfo.noofdayevent - 1)
                            if (localDate.year == todaydate.year) {
                                val rangetext = daysList[localDate.dayOfWeek] + ", " + localDate.toString("d MMM") + " - " + daysList[nextday.dayOfWeek] + ", " + nextday.toString("d MMM")
                                eventrangetextview!!.text = rangetext
                            } else {
                                val rangetext = daysList[localDate.dayOfWeek] + ", " + localDate.toString("d MMM, YYYY") + " - " + daysList[nextday.dayOfWeek] + ", " + nextday.toString("d MMM, YYYY")
                                eventrangetextview!!.text = rangetext
                            }
                        } else {
                            val localDate = LocalDate(eventInfo.starttime)
                            val todaydate = LocalDate.now()
                            if (localDate.year == todaydate.year) {
                                val rangetext = daysList[localDate.dayOfWeek] + ", " + localDate.toString("d MMM")
                                eventrangetextview!!.text = rangetext
                            } else {
                                val rangetext = daysList[localDate.dayOfWeek] + ", " + localDate.toString("d MMM, YYYY")
                                eventrangetextview!!.text = rangetext
                            }
                        }
                        holidaytextview!!.text = eventInfo.accountname
                        closebtn!!.visibility = View.VISIBLE
                        eventnametextview!!.visibility = View.GONE
                        roundrect!!.visibility = View.GONE
                        eventrangetextview!!.visibility = View.GONE
                        calendaricon!!.visibility = View.GONE
                        holidaytextview!!.visibility = View.GONE
                        eventfixstextview!!.visibility = View.GONE
                        val view = mNestedView!!.layoutManager!!.findViewByPosition(adapterPosition)
                        val layoutParams = redlay!!.layoutParams
                        layoutParams.height = v.height
                        layoutParams.width = v.width
                        redlay!!.layoutParams = layoutParams
                        redlay!!.translationX = v.left.toFloat()
                        redlay!!.translationY = (view!!.top + toolbar!!.height).toFloat()
                        redlay!!.translationZ = 0f
                        val shape = GradientDrawable()
                        shape.cornerRadius = resources.getDimensionPixelSize(R.dimen.fourdp).toFloat()
                        mycolor = eventalllist!![adapterPosition].color
                        shape.setColor(mycolor)
                        redlay!!.background = shape
                        roundrect!!.background = shape
                        val animwidth = ValueAnimator.ofInt(redlay!!.width, devicewidth)
                        animwidth.addUpdateListener { valueAnimator ->
                            val `val` = valueAnimator.animatedValue as Int
                            val layoutParams = redlay!!.layoutParams
                            layoutParams.width = `val`
                            redlay!!.layoutParams = layoutParams
                        }
                        animwidth.duration = 300
                        val animheight = ValueAnimator.ofInt(redlay!!.height, deviceHeight)
                        animheight.addUpdateListener { valueAnimator ->
                            val `val` = valueAnimator.animatedValue as Int
                            val layoutParams = redlay!!.layoutParams
                            layoutParams.height = `val`
                            redlay!!.layoutParams = layoutParams
                            if (redlay!!.translationZ == 0f && valueAnimator.animatedFraction > 0.15) {
                                redlay!!.setBackgroundColor(Color.WHITE)
                                shadow!!.visibility = View.VISIBLE
                                redlay!!.translationZ = resources.getDimensionPixelSize(R.dimen.tendp).toFloat()
                            }
                        }
                        animheight.duration = 300
                        val animx = ValueAnimator.ofFloat(redlay!!.translationX, 0f)
                        animx.addUpdateListener { valueAnimator ->
                            val `val` = valueAnimator.animatedValue as Float
                            redlay!!.translationX = `val`
                        }
                        animx.duration = 300
                        val animy = ValueAnimator.ofFloat(redlay!!.translationY, 0f)
                        animy.addUpdateListener { valueAnimator ->
                            val `val` = valueAnimator.animatedValue as Float
                            redlay!!.translationY = `val`
                        }
                        animy.duration = 300
                        animheight.addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                super.onAnimationEnd(animation)
                                Handler().postDelayed({
                                    closebtn!!.visibility = View.VISIBLE
                                    eventnametextview!!.visibility = View.VISIBLE
                                    roundrect!!.visibility = View.VISIBLE
                                    eventrangetextview!!.visibility = View.VISIBLE
                                    calendaricon!!.visibility = View.VISIBLE
                                    holidaytextview!!.visibility = View.VISIBLE
                                    eventfixstextview!!.visibility = View.VISIBLE
                                }, 150)
                            }
                        })
                        animwidth.start()
                        animheight.start()
                        animy.start()
                        animx.start()
                        eventview = v
                        fullview = view
                    }
                })
                circle = itemView.findViewById(R.id.circle)
                line = itemView.findViewById(R.id.line)
            }
        }

        internal inner class EndViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var eventimageview: ScrollParallaxImageView
            var monthname: TextView

            init {
                eventimageview = itemView.findViewById(R.id.imageView)
                eventimageview.setParallaxStyles(VerticalMovingStyle())
                monthname = itemView.findViewById(R.id.textView11)
            }
        }

        internal inner class NoplanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var noplantextview: TextView

            init {
                noplantextview = itemView.findViewById(R.id.view_noplan_textview)
            }
        }

        internal inner class RangeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var rangetextview: TextView

            init {
                rangetextview = itemView.findViewById(R.id.view_range_textview)
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        var lastdate = LocalDate.now()
        var topspace = 0
        fun setTransparent(activity: Activity) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                return
            }
            transparentStatusBar(activity)
            setRootView(activity)
        }

        private fun setRootView(activity: Activity) {
            val parent = activity.findViewById<View>(android.R.id.content) as ViewGroup
            var i = 0
            val count = parent.childCount
            while (i < count) {
                val childView = parent.getChildAt(i)
                if (childView is ViewGroup) {
                    childView.setFitsSystemWindows(false)
                    childView.clipToPadding = true
                }
                i++
            }
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        private fun transparentStatusBar(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                //activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                activity.window.statusBarColor = Color.TRANSPARENT
                activity.window.navigationBarColor = Color.parseColor("#f1f3f5")
            } else {
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            }
        }

        fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
            val win = activity.window
            val winParams = win.attributes
            if (on) {
                winParams.flags = winParams.flags or bits
            } else {
                winParams.flags = winParams.flags and bits.inv()
            }
            win.attributes = winParams
        }
    }

    override fun onEmptyViewClicked(time: Calendar?) {

        val myDate = "2023/11/03 18:10:45"
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        val date: Date = sdf.parse(myDate)
        val millis: Long = date.time

        val myDate1 = "2023/11/03 19:10:45"
        val sdf1 = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        val date1: Date = sdf1.parse(myDate1)
        val millis1: Long = date1.time


        val eventTitle = "My Event"
        val eventDescription = "This is my event."
        val eventStartDateTime = millis

        val eventEndDateTime = millis1
        val eventAllDay = false
        val eventStatus = CalendarContract.Events.STATUS_CONFIRMED
        val eventTimezone = DateTimeZone.getDefault().toString()

        Toast.makeText(this, CalendarHelper.addEventToGoogleCalendar(this, eventTitle,
                eventDescription, eventStartDateTime, eventEndDateTime, eventAllDay,
                eventStatus, eventTimezone).toString() + "", Toast.LENGTH_LONG).show()


        calendarView.invalidate()
        monthviewpager.invalidate()
        mWeekView?.invalidate()
    }
}