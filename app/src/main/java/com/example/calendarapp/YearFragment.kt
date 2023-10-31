package com.example.calendarapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
/**
 * A simple [Fragment] subclass.
 * Use the [YearFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class YearFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var year = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            year = requireArguments().getInt(ARG_PARAM1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_year, container, false)
        val yearView = view.findViewById<YearView>(R.id.yearview)
        yearView.updateYearView(year)
        return view
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        // TODO: Rename and change types and number of parameters
        fun newInstance(year: Int): YearFragment {
            val fragment = YearFragment()
            val args = Bundle()
            args.putInt(ARG_PARAM1, year)
            fragment.arguments = args
            return fragment
        }
    }
}