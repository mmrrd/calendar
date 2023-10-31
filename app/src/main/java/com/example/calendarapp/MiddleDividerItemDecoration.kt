package com.example.calendarapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.example.calendarapp.MiddleDividerItemDecoration
import com.example.calendarapp.R


/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ /**
 * MiddleDividerItemDecoration is a [RecyclerView.ItemDecoration] that can be used as a divider
 * between items of a [LinearLayoutManager]. It supports both [.HORIZONTAL] and
 * [.VERTICAL] orientations.
 * It can also supports [.ALL], included both the horizontal and vertical. Mainly used for GridLayout.
 * <pre>
 * For normal usage with LinearLayout,
 * val mItemDecoration = MiddleDividerItemDecoration(context!!,DividerItemDecoration.VERTICAL)
 * For GridLayoutManager with inner decorations,
 * val mItemDecoration = MiddleDividerItemDecoration(context!!,MiddleDividerItemDecoration.ALL)
 * recyclerView.addItemDecoration(mItemDecoration);
</pre> *  *
 */
class MiddleDividerItemDecoration(context: Context, orientation: Int) : ItemDecoration() {
    private var mDivider: Drawable? = null
    private var mOrientation = 0
    private val mBounds: Rect

    /**
     * Creates a divider [RecyclerView.ItemDecoration] that can be used with a
     * [LinearLayoutManager].
     *
     * @param context     Current context, it will be used to access resources.
     * @param orientation Divider orientation. Should be [.HORIZONTAL] or [.VERTICAL].
     */
    init {
        /**
         * Current orientation. Either [.HORIZONTAL] or [.VERTICAL].
         */
        mBounds = Rect()
        val a = context.obtainStyledAttributes(intArrayOf(android.R.attr.listDivider))
        mDivider = a.getDrawable(0)
        if (mDivider == null) {
            Log.w(MiddleDividerItemDecoration.Companion.TAG, "@android:attr/listDivider was not set in the theme used for this " + "DividerItemDecoration. Please set that attribute all call setDrawable()")
        }
        mDivider!!.colorFilter = PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP)
        a.recycle()
        setOrientation(orientation)
    }

    fun setDividerColor(color: Int) {
        if (mDivider != null) mDivider!!.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }

    /**
     * Sets the orientation for this divider. This should be called if
     * [RecyclerView.LayoutManager] changes orientation.
     *
     * @param orientation [.HORIZONTAL] or [.VERTICAL]
     */
    fun setOrientation(orientation: Int) {
        require(!(orientation != MiddleDividerItemDecoration.Companion.HORIZONTAL && orientation != MiddleDividerItemDecoration.Companion.VERTICAL && orientation != MiddleDividerItemDecoration.Companion.ALL)) { "Invalid orientation. It should be either HORIZONTAL or VERTICAL" }
        mOrientation = orientation
    }

    /**
     * Sets the [Drawable] for this divider.
     *
     * @param drawable Drawable that should be used as a divider.
     */
    fun setDrawable(drawable: Drawable?) {
        requireNotNull(drawable) { "Drawable cannot be null." }
        mDivider = drawable
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.layoutManager == null || mDivider == null) return
        when (mOrientation) {
            MiddleDividerItemDecoration.Companion.ALL -> {
                drawVertical(c, parent)
                drawHorizontal(c, parent)
            }

            MiddleDividerItemDecoration.Companion.VERTICAL -> drawVertical(c, parent)
            else -> drawHorizontal(c, parent)
        }
    }

    private fun drawVertical(canvas: Canvas, parent: RecyclerView) {
        canvas.save()
        val left: Int
        val right: Int
        if (parent.clipToPadding) {
            left = parent.paddingLeft
            right = parent.width - parent.paddingRight
            canvas.clipRect(left, parent.paddingTop, right, parent.height - parent.paddingBottom)
        } else {
            left = 0
            right = parent.width
        }
        val childCount = parent.childCount
        if (parent.layoutManager is GridLayoutManager) {
            var leftItems = childCount % (parent.layoutManager as GridLayoutManager?)!!.spanCount
            if (leftItems == 0) {
                leftItems = (parent.layoutManager as GridLayoutManager?)!!.spanCount
            }
        }
        for (i in 0..5) {
            val child = parent.getChildAt(i * 7) ?: return
            parent.getDecoratedBoundsWithMargins(child, mBounds)
            val bottom = mBounds.top + Math.round(child.translationY) + parent.resources.getDimensionPixelSize(R.dimen.pluseightdp) //+22
            val top = bottom - mDivider!!.intrinsicHeight
            if (i * 7 == 0) {
                mDivider!!.setBounds(left, top, right, bottom)
            } else {
                mDivider!!.setBounds(left, top, right, bottom)
            }
            mDivider!!.draw(canvas)
        }
        canvas.restore()
    }

    private fun drawHorizontal(canvas: Canvas, parent: RecyclerView) {
        canvas.save()
        val top: Int
        val bottom: Int
        if (parent.clipToPadding) {
            top = parent.paddingTop
            bottom = parent.height - parent.paddingBottom
            canvas.clipRect(parent.paddingLeft, top, parent.width - parent.paddingRight, bottom)
        } else {
            top = 0
            bottom = parent.height
        }
        val childCount = parent.childCount
        for (i in 0..6) {
            val child = parent.getChildAt(i)
            parent.layoutManager!!.getDecoratedBoundsWithMargins(child, mBounds)
            val right = mBounds.right + Math.round(child.translationX)
            val left = right - mDivider!!.intrinsicWidth
            mDivider!!.setBounds(left, top, right, bottom)
            mDivider!!.draw(canvas)
        }
        canvas.restore()

//        canvas.save();
//
//        int top;
//        int bottom;
//
//        if (parent.getClipToPadding()) {
//            top = parent.getPaddingTop();
//            bottom = parent.getHeight() - parent.getPaddingBottom();
//            canvas.clipRect(parent.getPaddingLeft(), top, parent.getWidth()-parent.getPaddingRight(), bottom);
//        } else {
//            top = 0;
//            bottom = parent.getHeight();
//        }
//
//        int childCount = parent.getChildCount();
//        if (parent.getLayoutManager() instanceof GridLayoutManager) {
//            //childCount = ((GridLayoutManager)parent.getLayoutManager()).getSpanCount();
//        }
//
//        for (int i=0 ; i<childCount-1 ; i++) {
//            View child = parent.getChildAt(i);
//            if(child == null) return;
//            int right = mBounds.right + Math.round(child.getTranslationX());
//            int left = right - mDivider.getIntrinsicWidth();
//            mDivider.setBounds(left, top, right, bottom);
//            mDivider.draw(canvas);
//        }
//
//        canvas.restore();
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (mDivider == null) {
            outRect[0, 0, 0] = 0
            return
        }
        if (mOrientation == MiddleDividerItemDecoration.Companion.VERTICAL) {
            outRect[0, 0, 0] = mDivider!!.intrinsicHeight
        } else {
            outRect[0, 0, mDivider!!.intrinsicWidth] = 0
        }
    }

    companion object {
        const val HORIZONTAL = LinearLayout.HORIZONTAL
        const val VERTICAL = LinearLayout.VERTICAL
        const val ALL = 2
        private val TAG = MiddleDividerItemDecoration::class.java.simpleName
    }
}