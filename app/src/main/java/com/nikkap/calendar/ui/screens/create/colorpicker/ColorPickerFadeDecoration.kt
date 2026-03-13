package com.nikkap.calendar.ui.screens.create.colorpicker

import android.graphics.Canvas
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class ColorPickerFadeDecoration : RecyclerView.ItemDecoration() {
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val centerX = parent.width / 2f
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val childCenterX = (child.left + child.right) / 2f
            val distanceFromCenter = abs(centerX - childCenterX)

            val alpha = 1f - 1f.coerceAtMost(distanceFromCenter / centerX) * 0.7f
            child.alpha = alpha

            val scale = 1f - 1f.coerceAtMost(distanceFromCenter / centerX) * 0.3f
            child.scaleX = scale
            child.scaleY = scale
        }
    }
}