package com.example.calendarapp

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.calendarapp.GooglecalenderView.Dayadapter.DayViewHolder
import org.greenrobot.eventbus.EventBus
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.Months
import java.util.Arrays
import java.util.Calendar
import java.util.Locale
import java.util.TreeMap

class GooglecalenderView : LinearLayout {
    private var context: Context
    private lateinit var viewPager: ViewPager2
    private var monthChangeListner: MonthChangeListner? = null
    var currentmonth = 0
        private set
    private var mindate: LocalDate? = null
    private var maxdate: LocalDate? = null
    private var eventuser = HashMap<LocalDate?, EventInfo?>()
    private val mDefaultEventColor = Color.parseColor("#9fc6e7")

    constructor(context: Context) : super(context) {
        LayoutInflater.from(context).inflate(R.layout.viewpagerlay, this)
        this.context = context
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        LayoutInflater.from(context).inflate(R.layout.viewpagerlay, this)
        this.context = context
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.viewpagerlay, this)
        this.context = context
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        LayoutInflater.from(context).inflate(R.layout.viewpagerlay, this)
        this.context = context
    }

    fun setMonthChangeListner(monthChangeListner: MonthChangeListner?) {
        this.monthChangeListner = monthChangeListner
    }

    fun calculateCurrentMonth(currentmonthda: LocalDate?): Int {
        if (currentmonthda == null || mindate == null) return 0
        val mindateobj = mindate!!.toDateTimeAtStartOfDay().dayOfMonth().withMinimumValue().toLocalDate()
        val current = currentmonthda.dayOfMonth().withMaximumValue()
        return Months.monthsBetween(mindateobj, current).months
    }

    fun setCurrentmonth(currentmonthda: LocalDate?) {
        if (mindate == null) return
        currentmonth = calculateCurrentMonth(currentmonthda)
        if (viewPager!!.currentItem != currentmonth) {
            viewPager!!.setCurrentItem(currentmonth, false)
            //  viewPager.getAdapter().notifyDataSetChanged();
        }
        updategrid()
    }

    fun setCurrentmonth(position: Int) {
        currentmonth = position
        if (viewPager!!.currentItem != currentmonth) {
            viewPager!!.setCurrentItem(currentmonth, false)
            //  viewPager.getAdapter().notifyDataSetChanged();
        }
    }

    fun init(eventhashmap: HashMap<LocalDate, EventInfo?>?, mindate: LocalDate, maxdate: LocalDate) {
        eventuser = HashMap(eventhashmap)
        viewPager = findViewById(R.id.viewpager)
        this.mindate = mindate
        this.maxdate = maxdate
        var mindateobj = mindate.toDateTimeAtStartOfDay()
        val maxdateobj = maxdate.toDateTimeAtStartOfDay()
        val months = Months.monthsBetween(mindateobj, maxdateobj).months
        val arrayList = ArrayList<MonthModel>()
        val eventhash = HashMap<LocalDate, EventInfo?>()
        for (i in 0..months) {
            var firstday = mindateobj.dayOfMonth().withMinimumValue().dayOfWeek().get()
            if (firstday == 7) firstday = 0
            val lastday = mindateobj.dayOfMonth().withMaximumValue().dayOfWeek().get()
            val month = MonthModel()
            month.monthnamestr = mindateobj.toString("MMMM")
            month.month = mindateobj.monthOfYear
            month.noofday = mindateobj.dayOfMonth().maximumValue
            month.year = mindateobj.year
            month.firstday = firstday
            val currentyear = LocalDate().year
            val dayModelArrayList = ArrayList<DayModel>()
            var startday = mindateobj.dayOfMonth().withMinimumValue().withTimeAtStartOfDay()
            var minweek = startday.dayOfWeek().withMinimumValue().toLocalDate().minusDays(1)
            while (minweek.compareTo(startday.dayOfMonth().withMaximumValue().toLocalDate()) < 0) {
                if (minweek.monthOfYear == minweek.plusDays(6).monthOfYear) {
                    val lastpattern = if (minweek.year == currentyear) "d MMM" else "d MMM YYYY"
                    val s = arrayOf("tojigs" + minweek.toString("d").uppercase(Locale.getDefault()) + " - " + minweek.plusDays(6).toString(lastpattern).uppercase(Locale.getDefault()))
                    if (!eventhash.containsKey(minweek)) eventhash[minweek] = EventInfo(s)
                    minweek = minweek.plusWeeks(1)
                } else {
                    val lastpattern = if (minweek.year == currentyear) "d MMM" else "d MMM YYYY"
                    val s = arrayOf("tojigs" + minweek.toString("d MMM").uppercase(Locale.getDefault()) + " - " + minweek.plusDays(6).toString(lastpattern).uppercase(Locale.getDefault()))
                    if (!eventhash.containsKey(minweek)) eventhash[minweek] = EventInfo(s)
                    minweek = minweek.plusWeeks(1)
                }
            }
            for (j in 1..month.noofday) {
                val dayModel = DayModel()
                dayModel.day = startday.dayOfMonth
                dayModel.month = startday.monthOfYear
                dayModel.year = startday.year
                if (eventuser.containsKey(startday.toLocalDate())) {
                    if (eventhash.containsKey(startday.toLocalDate())) {
                        val eventInfo = eventhash[startday.toLocalDate()]
                        var list = Arrays.asList(*eventInfo!!.eventtitles)
                        list = ArrayList(list)
                        for (s in eventuser[startday.toLocalDate()]!!.eventtitles) {
                            list.add(s)
                        }
                        val mStringArray = arrayOfNulls<String>(list.size)
                        val s = list.toArray<String?>(mStringArray)
                        eventInfo.eventtitles = s
                        eventhash[startday.toLocalDate()] = eventInfo
                    } else {
                        eventhash[startday.toLocalDate()] = eventuser[startday.toLocalDate()]
                    }
                    // dayModel.setEvents(eventuser.get(startday.toLocalDate()));
                    dayModel.eventlist = true
                }
                if (startday.toLocalDate() == LocalDate()) {
                    dayModel.isToday = true
                    currentmonth = i
                } else {
                    dayModel.isToday = false
                }
                dayModelArrayList.add(dayModel)
                if (j == 1) {
                    var eventInfo1: EventInfo? = EventInfo()
                    var s = arrayOf<String>("start")
                    eventInfo1!!.eventtitles = s
                    //                  if (eventhash.containsKey(startday.dayOfWeek().withMinimumValue().toLocalDate())&&eventhash.get(startday.dayOfWeek().withMinimumValue().toLocalDate())[0].contains("tojigs")){
//                     Log.e("remove",startday.dayOfWeek().withMinimumValue().toLocalDate()+"->"+Arrays.asList(eventhash.get(startday.dayOfWeek().withMinimumValue().toLocalDate())));
//                    eventhash.remove(startday.dayOfWeek().withMinimumValue().toLocalDate());

//                  }
                    if (eventhash.containsKey(startday.toLocalDate())) {
                        val eventInfo = eventhash[startday.toLocalDate()]
                        var list = Arrays.asList(*eventInfo!!.eventtitles)
                        list = ArrayList(list)
                        list.add(0, "start")
                        val mStringArray = arrayOfNulls<String>(list.size)
                        s = list.toArray(mStringArray)
                        eventInfo.eventtitles = s
                        eventInfo1 = eventInfo
                    }
                    eventhash[startday.toLocalDate()] = eventInfo1
                }
                //              if (j==month.getNoofday()&&i!=months){
//                  Log.e("endcount",startday.toLocalDate().toString());
//                  Log.e("end",eventhash.containsKey(startday.toLocalDate())+"");
//                  String s[]={"end"};
//                  eventhash.put(startday.toLocalDate(),s);
//              }
                startday = startday.plusDays(1)
            }
            month.dayModelArrayList = dayModelArrayList
            arrayList.add(month)
            mindateobj = mindateobj.plusMonths(1)
        }
        val myPagerAdapter = MonthPagerAdapter(context, arrayList)
        viewPager.adapter = myPagerAdapter
        //       viewPager.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
//           @Override
//           public void onViewAttachedToWindow(View view) {
//               viewPager.setCurrentItem(currentmonth);
//
//           }
//
//           @Override
//           public void onViewDetachedFromWindow(View view) {
//
//           }
//       });
        viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val mainActivity = context as MainActivity
                currentmonth = position
                if (!mainActivity.isAppBarClosed()) {
                    adjustheight()
                    if (mainActivity.mNestedView!!.visibility == VISIBLE) EventBus.getDefault().post(MessageEvent(LocalDate(myPagerAdapter.monthModels[position].year, myPagerAdapter.monthModels[position].month, 1))) else {
                        MainActivity.Companion.lastdate = LocalDate(myPagerAdapter.monthModels[position].year, myPagerAdapter.monthModels[position].month, 1)
                    }
                    updategrid()
                    //     myPagerAdapter.getFirstFragments().get(position).updategrid();
                    // myPagerAdapter.notifyDataSetChanged();
                    if (monthChangeListner != null) monthChangeListner!!.onmonthChange(myPagerAdapter.monthModels[position])
                }
            }
        })
        //        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//                MainActivity mainActivity= (MainActivity) context;
//                currentmonth=position;
//                if (!mainActivity.isAppBarClosed()){
//                    Log.e("onPageSelected","Googlecalendaraview");
//                    adjustheight();
//                    EventBus.getDefault().post(new MessageEvent(new LocalDate(myPagerAdapter.monthModels.get(position).getYear(),myPagerAdapter.monthModels.get(position).getMonth(),1)));
//                    myPagerAdapter.getFirstFragments().get(position).updategrid();
//                    if (monthChangeListner!=null)monthChangeListner.onmonthChange(myPagerAdapter.monthModels.get(position));
//
//
//                }
////                if (myPagerAdapter.getFirstFragments().get(position).isVisible()){
////                    myPagerAdapter.getFirstFragments().get(position).updategrid(arrayList.get(position).getDayModelArrayList());
////                }
//
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//
//            }
//        });
        val todaydate = LocalDate.now()
        if (!eventhash.containsKey(todaydate)) {
            eventhash[todaydate] = EventInfo(arrayOf("todaydate"))
        } else {
            var list = Arrays.asList(*eventhash[todaydate]!!.eventtitles)
            list = ArrayList(list)
            val b = true

            //list.add("todaydate");
            val mStringArray = arrayOfNulls<String>(list.size)
            val eventInfo = eventhash[todaydate]
            eventInfo!!.eventtitles = list.toArray(mStringArray)
            eventhash[todaydate] = eventInfo
        }
        val treeMap: Map<LocalDate, EventInfo?> = TreeMap(eventhash)
        val indextrack = HashMap<LocalDate, Int>()
        var i = 0
        val eventModelslist = ArrayList<EventModel>()
        for ((key, tempinfo) in treeMap) {
            for (s in tempinfo!!.eventtitles) {
                if (s == null) continue
                var type = 0
                if (s.startsWith("todaydate")) type = 2 else if (s == "start") type = 1 else if (s.startsWith("tojigs")) type = 3
                if (type == 2 && eventModelslist[eventModelslist.size - 1].type == 0 && eventModelslist[eventModelslist.size - 1].localDate == key) {
                } else {
                    if (type == 0 && eventModelslist.size > 0 && eventModelslist[eventModelslist.size - 1].type == 0 && eventModelslist[eventModelslist.size - 1].localDate != key) {
                        eventModelslist.add(EventModel("dup", key, 100))
                        // if (!indextrack.containsKey(localDateStringEntry.getKey()))indextrack.put(localDateStringEntry.getKey(),i);
                        i++
                    } else if (type == 3 && eventModelslist.size > 0 && eventModelslist[eventModelslist.size - 1].type == 0) {
                        eventModelslist.add(EventModel("dup", eventModelslist[eventModelslist.size - 1].localDate, 100))
                        //   if (!indextrack.containsKey(localDateStringEntry.getKey()))indextrack.put(localDateStringEntry.getKey(),i);
                        i++
                    } else if (type == 1 && eventModelslist.size > 0 && eventModelslist[eventModelslist.size - 1].type == 0) {
                        eventModelslist.add(EventModel("dup", eventModelslist[eventModelslist.size - 1].localDate, 200))
                        // if (!indextrack.containsKey(localDateStringEntry.getKey()))indextrack.put(localDateStringEntry.getKey(),i);
                        i++
                    } else if (type == 0 && eventModelslist.size > 0 && eventModelslist[eventModelslist.size - 1].type == 1) {
                        eventModelslist.add(EventModel("dup", key, 200))
                        //if (!indextrack.containsKey(localDateStringEntry.getKey()))indextrack.put(localDateStringEntry.getKey(),i);
                        i++
                    } else if (type == 2 && eventModelslist.size > 0 && eventModelslist[eventModelslist.size - 1].type == 0) {
                        eventModelslist.add(EventModel("dup", eventModelslist[eventModelslist.size - 1].localDate, 100))
                        //  if (!indextrack.containsKey(localDateStringEntry.getKey()))indextrack.put(localDateStringEntry.getKey(),i);
                        i++
                    }
                    var ss = s
                    var color = mDefaultEventColor
                    if (type == 0) {
                        var myinfo = eventhashmap!![key]
                        while (myinfo != null && myinfo.title != s) {
                            myinfo = myinfo.nextnode
                        }
                        color = if (myinfo!!.eventcolor == 0) mDefaultEventColor else myinfo.eventcolor
                        if (myinfo.noofdayevent > 1) {
                            ss = ss + " (" + key.toString("d MMMM") + "-" + key.plusDays(myinfo.noofdayevent - 1).toString("d MMMM") + ")"
                        } else if (myinfo.isallday == false) {
                            val start = LocalDateTime(myinfo.starttime, DateTimeZone.forID(myinfo.timezone))
                            val end = LocalDateTime(myinfo.endtime, DateTimeZone.forID(myinfo.timezone))
                            val sf = if (start.toString("a") == end.toString("a")) "" else "a"
                            ss = ss + " (" + start.toString("h:mm $sf") + "-" + end.toString("h:mm a") + ")"
                        }
                    }
                    //                    if (noofday>1&&type==0){
//                        LocalDate as=localDateStringEntry.getKey();
//                        for (int jj=0;jj<noofday;jj++){
//                            eventModelslist.add(new EventModel(s, as.plusDays(jj), type));
//                            indextrack.put(as.plusDays(jj).plusDays(1), i);
//                            i++;
//                            eventModelslist.add(new EventModel("dup", eventModelslist.get(eventModelslist.size() - 1).getLocalDate(), 200));
//                            // if (!indextrack.containsKey(localDateStringEntry.getKey()))indextrack.put(localDateStringEntry.getKey(),i);
//                            i++;
//                        }
//                    }
//                    else {
                    val mModel = EventModel(ss, key, type)
                    mModel.color = color
                    eventModelslist.add(mModel)
                    indextrack[key] = i
                    i++
                    //                  }
                }


//               if (type==2){
//                   if (eventModelslist.get(eventModelslist.size()-1).getType()!=0){
//                       eventModelslist.add(new EventModel(s,localDateStringEntry.getKey(),type));
//                       if (!indextrack.containsKey(localDateStringEntry.getKey()))indextrack.put(localDateStringEntry.getKey(),i);
//                       i++;
//                   }
//               }
//               else {
//                   eventModelslist.add(new EventModel(s,localDateStringEntry.getKey(),type));
//                   if (!indextrack.containsKey(localDateStringEntry.getKey()))indextrack.put(localDateStringEntry.getKey(),i);
//                   i++;
//               }
            }
        }
        EventBus.getDefault().post(AddEvent(eventModelslist, indextrack, arrayList))
    }

    fun updategrid() {
        val myPagerAdapter = viewPager!!.adapter as MonthPagerAdapter?
        if (myPagerAdapter != null) {
            val position = viewPager!!.currentItem
            // myPagerAdapter.getFirstFragments().get(position).updategrid();
            val recyclerView = viewPager!!.getChildAt(0) as RecyclerView
            val monthViewHolder = recyclerView.findViewHolderForAdapterPosition(position) as MonthPagerAdapter.MonthViewHolder?
            if (monthViewHolder != null && monthViewHolder.jCalendarMonthTopView != null) {
                monthViewHolder.jCalendarMonthTopView!!.requestLayout()
                monthViewHolder.jCalendarMonthTopView!!.invalidate()
            }
        }
    }

    fun adjustheight() {
        if (mindate == null) return
        val myPagerAdapter = viewPager!!.adapter as MonthPagerAdapter?
        if (myPagerAdapter != null) {
            val position = viewPager!!.currentItem
            val size = myPagerAdapter.monthModels[position].dayModelArrayList.size + myPagerAdapter.monthModels[position].firstday
            val numbercolumn = if (size % 7 == 0) size / 7 else size / 7 + 1
            val params = layoutParams
            val setheight = 75 + context.resources.getDimensionPixelSize(R.dimen.itemheight) * numbercolumn + context.resources.getDimensionPixelSize(R.dimen.tendp) + statusBarHeight
            if (params.height == setheight) return
            params.height = setheight
            // params.height=0;//jigs change
            layoutParams = params
        }
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

    //    public class MyPagerAdapter extends FragmentStatePagerAdapter {
    //        private ArrayList<MonthModel> monthModels;
    //        private ArrayList<FirstFragment> firstFragments = new ArrayList<>();
    //
    //        public MyPagerAdapter(FragmentManager fragmentManager, ArrayList<MonthModel> monthModels) {
    //
    //            super(fragmentManager);
    //            this.monthModels = monthModels;
    //            for (int i = 0; i < monthModels.size(); i++) {
    //                firstFragments.add(FirstFragment.newInstance(monthModels.get(i).getMonth(), monthModels.get(i).getYear(), monthModels.get(i).getFirstday(), monthModels.get(i).getDayModelArrayList()));
    //            }
    //        }
    //
    //        public ArrayList<MonthModel> getMonthModels() {
    //            return monthModels;
    //        }
    //
    //        public ArrayList<FirstFragment> getFirstFragments() {
    //            return firstFragments;
    //        }
    //
    //        // Returns total number of pages
    //        @Override
    //        public int getCount() {
    //            return monthModels.size();
    //        }
    //
    //
    //        // Returns the fragment to display for that page
    //        @Override
    //        public Fragment getItem(int position) {
    //
    //            return firstFragments.get(position);
    //
    //        }
    //    }
    internal inner class MonthPagerAdapter(private val context: Context, val monthModels: ArrayList<MonthModel>) : RecyclerView.Adapter<MonthPagerAdapter.MonthViewHolder>() {
        private val mInflater: LayoutInflater

        init {
            mInflater = LayoutInflater.from(context)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
            val view = mInflater.inflate(R.layout.fraglay, parent, false)
            return MonthViewHolder(view)
        }

        override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
            val monthtemp = monthModels[position]
            holder.jCalendarMonthTopView!!.initdata(monthtemp.dayModelArrayList, monthtemp.firstday, monthtemp.month, monthtemp.year)
            //            Dayadapter dayadapter = new Dayadapter(context, monthtemp.getDayModelArrayList(), monthtemp.getFirstday(), monthtemp.getMonth(), monthtemp.getYear());
//            holder.gridview.setAdapter(dayadapter);
//            dayadapter.notifyDataSetChanged();
        }

        override fun getItemCount(): Int {
            return monthModels.size
        }

        internal inner class MonthViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            // RecyclerView gridview;
            var jCalendarMonthTopView: JCalendarMonthTopView?

            init {
                jCalendarMonthTopView = itemView.findViewById(R.id.jcalendarmonthview)
            }
        }
    }

    internal inner class Dayadapter(context: Context?, private val dayModels: ArrayList<DayModel>, private val firstday: Int, private val month: Int, private val year: Int) : RecyclerView.Adapter<DayViewHolder>() {
        private val mInflater: LayoutInflater

        init {
            mInflater = LayoutInflater.from(context)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
            val view = mInflater.inflate(R.layout.gridlay, parent, false)
            return DayViewHolder(view)
        }

        override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
            var position = position
            if (position >= firstday) {
                position = position - firstday
                val dayModel = dayModels[position]
                val selected = if (dayModel.day == MainActivity.Companion.lastdate.getDayOfMonth() && dayModel.month == MainActivity.Companion.lastdate.getMonthOfYear() && dayModel.year == MainActivity.Companion.lastdate.getYear()) true else false
                if (dayModel.isToday) {
                    holder.textView.setBackgroundResource(R.drawable.circle)
                    holder.textView.setTextColor(Color.WHITE)
                } else if (selected) {
                    holder.textView.setBackgroundResource(R.drawable.selectedback)
                    holder.textView.setTextColor(Color.rgb(91, 128, 231))
                } else {
                    holder.textView.setBackgroundColor(Color.TRANSPARENT)
                    holder.textView.setTextColor(Color.rgb(80, 80, 80))
                }
                holder.textView.text = dayModels[position].day.toString() + ""
                if (dayModel.eventlist && !selected) {
                    holder.eventview.visibility = VISIBLE
                } else {
                    holder.eventview.visibility = GONE
                }
            } else {
                holder.textView.setBackgroundColor(Color.TRANSPARENT)
                holder.textView.text = ""
                holder.eventview.visibility = GONE
            }
        }

        override fun getItemCount(): Int {
            return dayModels.size + firstday
        }

        internal inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView
            val eventview: View

            init {
                textView = itemView.findViewById(R.id.textView8)
                eventview = itemView.findViewById(R.id.eventview)
                itemView.setOnClickListener {
                    if (adapterPosition >= firstday) {
                        for (dayModel in dayModels) {
                            dayModel.isSelected = false
                        }
                        MainActivity.Companion.lastdate = LocalDate(year, month, dayModels[adapterPosition - firstday].day)
                        val mainActivity = context as MainActivity
                        if (mainActivity.mNestedView!!.visibility == VISIBLE) EventBus.getDefault().post(MessageEvent(LocalDate(year, month, dayModels[adapterPosition - firstday].day)))
                        // dayModels.get(getAdapterPosition()-firstday).setSelected(true);
                        if (mainActivity.weekviewcontainer!!.visibility == VISIBLE) {
                            val todaydate = Calendar.getInstance()
                            todaydate[Calendar.DAY_OF_MONTH] = MainActivity.Companion.lastdate.getDayOfMonth()
                            todaydate[Calendar.MONTH] = MainActivity.Companion.lastdate.getMonthOfYear() - 1
                            todaydate[Calendar.YEAR] = MainActivity.Companion.lastdate.getYear()
                            mainActivity.mWeekView!!.goToDate(todaydate)
                        }
                        notifyDataSetChanged()
                    }
                }
            }
        }
    }
}