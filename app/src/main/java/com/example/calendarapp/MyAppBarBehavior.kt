package com.example.calendarapp

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout



/**
 * Attach this behavior to AppBarLayout to disable the bottom portion of a closed appBar
 * so it cannot be touched to open the appBar. This behavior is helpful if there is some
 * portion of the appBar that displays when the appBar is closed, but should not open the appBar
 * when the appBar is closed.
 */
class MyAppBarBehavior : AppBarLayout.Behavior {
    // Touch above this y-axis value can open the appBar.
    private var mCanOpenBottom = 0
    var isShouldScroll = false
        private set

    // Determines if the appBar can be dragged open or not via direct touch on the appBar.
    private var mCanDrag = true
    private val isPositive = false

    @Suppress("unused")
    constructor() {
        init()
    }

    @Suppress("unused")
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    override fun onStartNestedScroll(parent: CoordinatorLayout, child: AppBarLayout, directTargetChild: View, target: View, nestedScrollAxes: Int, type: Int): Boolean {
        return isShouldScroll
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: AppBarLayout, ev: MotionEvent): Boolean {
        return if (isShouldScroll) {
            super.onTouchEvent(parent, child, ev)
        } else {
            false
        }
    }

    fun setScrollBehavior(shouldScroll: Boolean) {
        isShouldScroll = shouldScroll
    }

    private fun init() {
        setDragCallback(object : DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                return true
            }
        })
    }

    override fun onInterceptTouchEvent(parent: CoordinatorLayout,
                                       child: AppBarLayout,
                                       event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            // If appBar is closed. Only allow scrolling in defined area.
            if (child.top <= -child.totalScrollRange) {
                mCanDrag = event.y < mCanOpenBottom
            }
        }
        return super.onInterceptTouchEvent(parent, child, event)
    }

    fun setCanOpenBottom(bottom: Int) {
        mCanOpenBottom = bottom
    }

    companion object {
        private const val TOP_CHILD_FLING_THRESHOLD = 3
    }
}