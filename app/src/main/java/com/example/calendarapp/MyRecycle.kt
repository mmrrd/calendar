package com.example.calendarapp

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.calendarapp.MyRecycle

class MyRecycle : RecyclerView {
    private var linearLayoutManager: LinearLayoutManager? = null
    private var appBarTracking: MyRecycle.AppBarTracking? = null

    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context!!, attrs, defStyle)

    override fun setLayoutManager(layout: LayoutManager?) {
        super.setLayoutManager(layout)
        linearLayoutManager = layout as LinearLayoutManager?
    }

    fun setAppbartrackListner(appbarListner: MyRecycle.AppBarTracking?) {
        appBarTracking = appbarListner
    }

    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?): Boolean {
        return super.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)
    }

    override fun fling(velocityX: Int, velocityY: Int): Boolean {
        return super.fling(velocityX, velocityY)
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
    }

    interface AppBarTracking {
        val isAppBarIdle: Boolean
        val isAppBarExpanded: Boolean
    }
}