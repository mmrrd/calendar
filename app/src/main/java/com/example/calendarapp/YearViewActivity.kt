package com.example.calendarapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener


class YearViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_year_view)
        val viewPager = findViewById<ViewPager>(R.id.yearviewpager)
        val yeartextView = findViewById<TextView>(R.id.yeartextview)
        viewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                yeartextView.text = (2010 + position).toString() + ""
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        viewPager.adapter = YearPageAdapter(supportFragmentManager)
    }

    internal inner class YearPageAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return YearFragment.Companion.newInstance(2010 + position)
        }

        override fun getCount(): Int {
            return 30
        }
    }
}