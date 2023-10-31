package com.example.calendarapp

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.calendarapp.MonthFragment.Myadapter.MonthViewHolder
import org.joda.time.LocalDate

/**
 * A simple [Fragment] subclass.
 * Use the [MonthFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MonthFragment : Fragment() {
    private val title: String? = null
    private var dayModels: ArrayList<DayModel>? = null
    private val gridView: RecyclerView? = null
    private var firstday = 0
    private var month = 0
    private var year = 0
    private var singleitemheight = 0
    private var index = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firstday = requireArguments().getInt("firstday")
        month = requireArguments().getInt("month")
        year = requireArguments().getInt("year")
        index = requireArguments().getInt("index", -1)
        singleitemheight = requireArguments().getInt("singleitemheight")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_month, container, false)
        val jCalendarMonthView = view.findViewById<JCalendarMonthView>(R.id.jcalendarmonthview)
        jCalendarMonthView.setDayModels(dayModels, index)


//        RecyclerView gridView = view.findViewById(R.id.recyclerview);
//        ConstraintLayout constraintLayout = view.findViewById(R.id.dd);
//        for (int i = 0; i < constraintLayout.getChildCount(); i++) {
//            TextView textView = (TextView) constraintLayout.getChildAt(i);
//            if (i == index) {
//                textView.setTextColor(getResources().getColor(R.color.selectday));
//            } else {
//                textView.setTextColor(getResources().getColor(R.color.unselectday));
//            }
//        }
//        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 7) {
//
//
//        };
//        gridView.setLayoutManager(gridLayoutManager);
//        MiddleDividerItemDecoration vertecoration = new MiddleDividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
//        //vertecoration.setDrawable(new ColorDrawable(Color.LTGRAY));
//        MiddleDividerItemDecoration hortdecoration = new MiddleDividerItemDecoration(getActivity(), DividerItemDecoration.HORIZONTAL);
//        // hortdecoration.setDrawable(new ColorDrawable(Color.LTGRAY));
//        gridView.addItemDecoration(vertecoration);
//        gridView.addItemDecoration(hortdecoration);
//
//        gridView.setAdapter(new Myadapter());
        return view
    }

    internal inner class Myadapter : RecyclerView.Adapter<MonthViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {

            //   RelativeLayout relativeLayout = new RelativeLayout(getActivity());

            // Defining the RelativeLayout layout parameters.
            // In this case I want to fill its parent
//            RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
//                    RelativeLayout.LayoutParams.MATCH_PARENT,
//                    singleitemheight);
//            relativeLayout.setMinimumHeight(singleitemheight);
//
//            TextView tv = new TextView(getActivity());
//            tv.setText("Test");
//
//            // Defining the layout parameters of the TextView
//            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
//                    RelativeLayout.LayoutParams.WRAP_CONTENT,
//                    RelativeLayout.LayoutParams.WRAP_CONTENT);
//            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
//
//            // Setting the parameters on the TextView
//            tv.setLayoutParams(lp);
//
//            // Adding the TextView to the RelativeLayout as a child
//            relativeLayout.addView(tv);
            var view: View? = null
            view = if (viewType == 0) {
                activity!!.layoutInflater.inflate(R.layout.monthgriditemlspace, parent, false)
            } else {
                activity!!.layoutInflater.inflate(R.layout.monthgriditem, parent, false)
            }
            val layoutParams = view.layoutParams
            layoutParams.height = singleitemheight
            view.layoutParams = layoutParams
            return MonthViewHolder(view)
        }

        override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
            holder.textView.text = dayModels!![position].day.toString() + ""
            if (dayModels!![position].isToday) {
                holder.textView.setBackgroundResource(R.drawable.smallcircle)
                holder.textView.setTextColor(Color.WHITE)
            } else if (dayModels!![position].isenable()) {
                holder.textView.setTextColor(Color.BLACK)
                holder.textView.setBackgroundColor(Color.TRANSPARENT)
            } else {
                holder.textView.setBackgroundColor(Color.TRANSPARENT)
                holder.textView.setTextColor(resources.getColor(R.color.lightblack))
            }
            val dayModeltemp = dayModels!![position]
            val names = dayModels!![position].events
            if (names != null) {
                if (names.size == 1) {
                    holder.event1.visibility = View.VISIBLE
                    holder.event2.visibility = View.GONE
                    holder.event3.visibility = View.GONE
                    holder.event2.text = ""
                    holder.event3.text = ""
                } else if (names.size == 2) {
                    holder.event1.visibility = View.VISIBLE
                    holder.event2.visibility = View.VISIBLE
                    holder.event3.visibility = View.GONE
                    holder.event3.text = ""
                } else {
                    holder.event1.visibility = View.VISIBLE
                    holder.event2.visibility = View.VISIBLE
                    holder.event3.visibility = View.VISIBLE
                }
                for (i in dayModels!![position].events.indices) {
                    if (i == 0) holder.event1.text = names[0] else if (i == 1) holder.event2.text = names[1] else holder.event3.text = names[2]
                }
            } else {
                holder.event1.visibility = View.GONE
                holder.event2.visibility = View.GONE
                holder.event3.visibility = View.GONE
            }
        }

        override fun getItemCount(): Int {
            return 42
        }

        override fun getItemViewType(position: Int): Int {
            return if (position % 7 == 0) 0 else 1
        }

        internal inner class MonthViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView
            val event1: TextView
            val event2: TextView
            val event3: TextView

            init {
                textView = itemView.findViewById(R.id.textView8)
                event1 = itemView.findViewById(R.id.event1)
                event2 = itemView.findViewById(R.id.event2)
                event3 = itemView.findViewById(R.id.event3)
                itemView.setOnClickListener {
                    val mainActivity = activity as MainActivity?
                    if (mainActivity != null) {
                        val dayModel = dayModels!![adapterPosition]
                        mainActivity.selectdateFromMonthPager(dayModel.year, dayModel.month, dayModel.day)
                    }
                }
            }
        }
    }

    companion object {
        @Throws(CloneNotSupportedException::class)
        fun newInstance(month: Int, year: Int, page: Int, dayModels: ArrayList<DayModel>, alleventlist: HashMap<LocalDate, EventInfo?>?, singleitemheight: Int, effectmonthlist: HashMap<LocalDate, EventInfo>): MonthFragment {
            val fragmentFirst = MonthFragment()
            val args = Bundle()
            args.putInt("singleitemheight", singleitemheight)
            args.putInt("firstday", page)
            args.putInt("month", month)
            args.putInt("year", year)
            val prevmonth = LocalDate(year, month, 1)
            val todaydate = LocalDate()
            val adapterdata = ArrayList<DayModel>(43)
            for (effectmonth in effectmonthlist.keys) {
                Log.e("jeffect$effectmonth", effectmonthlist[effectmonth].toString() + "")
            }
            for (i in 0..41) {
                if (i < page) {
                    //prev month
                    val localDate = prevmonth.minusDays(page - i)
                    val dayModel = DayModel()
                    if (localDate.isEqual(todaydate)) {
                        dayModel.isToday = true
                    }
                    dayModel.day = localDate.dayOfMonth
                    dayModel.month = localDate.monthOfYear
                    dayModel.year = localDate.year
                    if (alleventlist!!.containsKey(localDate)) {
                        dayModel.eventInfo = alleventlist[localDate]
                    }
                    if (i == 0) {
                        if (effectmonthlist.containsKey(prevmonth)) {
                            val startdate = LocalDate(effectmonthlist[prevmonth]!!.starttime)
                            if (startdate.isBefore(localDate) || startdate.isEqual(localDate)) {
                                val containevent = HashMap<String, String>()
                                var myinfo = effectmonthlist[prevmonth]
                                containevent[myinfo!!.id.toString() + ""] = "1"
                                var newobj = EventInfo(myinfo)
                                val begning = newobj
                                while (myinfo!!.nextnode != null) {
                                    myinfo = myinfo.nextnode
                                    newobj.nextnode = EventInfo(myinfo)
                                    newobj = newobj.nextnode!!
                                    containevent[myinfo!!.id.toString() + ""] = "1"
                                }
                                val infolist: MutableList<EventInfo> = ArrayList()
                                var originalevent = alleventlist[localDate]
                                while (originalevent != null) {
                                    if (!containevent.containsKey(originalevent.id.toString() + "")) {
                                        infolist.add(originalevent)
                                    }
                                    originalevent = originalevent.nextnode
                                }
                                for (eventInfo in infolist) {
                                    newobj.nextnode = EventInfo(eventInfo)
                                    newobj = newobj.nextnode!!
                                }
                                dayModel.eventInfo = begning
                            }
                        }
                    }
                    dayModel.setIsenable(false)
                    adapterdata.add(dayModel)
                } else if (i >= dayModels.size + page) {
                    //next month
                    val localDate = prevmonth.plusDays(i - page)
                    Log.e("dateelseif", localDate.toString())
                    val dayModel = DayModel()
                    if (localDate.isEqual(todaydate)) {
                        dayModel.isToday = true
                    }
                    dayModel.day = localDate.dayOfMonth
                    dayModel.month = localDate.monthOfYear
                    dayModel.year = localDate.year
                    dayModel.setIsenable(false)
                    if (alleventlist!!.containsKey(localDate)) {
                        val eventInfo = alleventlist[localDate]
                        //                    while(eventInfo.isalreadyset){
//                        eventInfo=eventInfo.nextnode;
//                        if(eventInfo==null)break;
//                    }
                        if (eventInfo != null) dayModel.eventInfo = eventInfo
                        //                    if (alleventlist.get(localDate).isallday){
//                        LocalDate localDate1=new LocalDate(alleventlist.get(localDate).starttime, DateTimeZone.forID(alleventlist.get(localDate).timezone));
//                        LocalDate localDate2=new LocalDate(alleventlist.get(localDate).endtime, DateTimeZone.forID(alleventlist.get(localDate).timezone));
//                        int day = Days.daysBetween(localDate1,localDate2).getDays();
//                        dayModel.setNoofdayevent(day);
//                        Log.e("noofday",dayModel.getEvents()[0]+","+day);
//                    }
                    }
                    adapterdata.add(dayModel)
                } else {
                    //current month
                    val dayModel = dayModels[i - page]
                    dayModel.setIsenable(true)
                    if (dayModel.isToday) {
                        args.putInt("index", i % 7)
                    }
                    val mydate = LocalDate(year, month, dayModel.day)
                    Log.e("dateelse", mydate.toString())
                    if (alleventlist!!.containsKey(mydate)) {
                        dayModel.eventInfo = alleventlist[mydate]
                    }
                    if (i == 0) {
                        if (effectmonthlist.containsKey(prevmonth)) {
                            val startdate = LocalDate(effectmonthlist[prevmonth]!!.starttime)
                            if (startdate.isBefore(mydate) || startdate.isEqual(mydate)) {
                                val containevent = HashMap<String, String>()
                                var myinfo = effectmonthlist[prevmonth]
                                var newobj = EventInfo(myinfo)
                                val begning = newobj
                                containevent[myinfo!!.id.toString() + ""] = "1"
                                while (myinfo!!.nextnode != null) {
                                    myinfo = myinfo.nextnode
                                    newobj.nextnode = EventInfo(myinfo)
                                    newobj = newobj.nextnode!!
                                    if (myinfo != null) {
                                        containevent[myinfo.id.toString() + ""] = "1"
                                    }
                                }
                                val infolist: MutableList<EventInfo> = ArrayList()
                                var originalevent = alleventlist[mydate]
                                while (originalevent != null) {
                                    if (!containevent.containsKey(originalevent.id.toString() + "")) {
                                        infolist.add(originalevent)
                                    }
                                    originalevent = originalevent.nextnode
                                }
                                for (eventInfo in infolist) {
                                    newobj.nextnode = EventInfo(eventInfo)
                                    newobj = newobj.nextnode!!
                                }
                                dayModel.eventInfo = begning
                            }
                        }
                    }
                    adapterdata.add(dayModels[i - page])
                }
            }
            //        if(effectmonthlist.containsKey(prevmonth)){
//
//           EventInfo firstday = adapterdata.get(0).getEventInfo();
//           if(firstday==null){
//               DayModel dayModel=adapterdata.get(0);
//               dayModel.setEventInfo(effectmonthlist.get(prevmonth));
//               adapterdata.set(0,dayModel);
//           }
//           else {
//
//              EventInfo currentmodel=effectmonthlist.get(prevmonth);
//              while (currentmodel.nextnode!=null)currentmodel=currentmodel.nextnode;
//               DayModel dayModel=adapterdata.get(0);
//               currentmodel.nextnode=firstday;
//               dayModel.setEventInfo(currentmodel);
//               adapterdata.set(0,dayModel);
//           }
//        }
            fragmentFirst.dayModels = adapterdata
            fragmentFirst.arguments = args
            return fragmentFirst
        }
    }
}