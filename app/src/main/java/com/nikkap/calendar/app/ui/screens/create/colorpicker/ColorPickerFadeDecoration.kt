package com.nikkap.calendar.app.ui.screens.create.colorpicker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.TypedValue
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class ColorPickerFadeDecoration(context: Context) : RecyclerView.ItemDecoration() {
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

    // For vertical line in center
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {

        val typedValue = TypedValue()
        context.theme.resolveAttribute(
            com.google.android.material.R.attr.colorOnSurfaceVariant,
            typedValue,
            true
        )
        color = typedValue.data

        style = Paint.Style.FILL

        strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            3f,
            context.resources.displayMetrics
        )
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        val centerX = parent.width / 2f

        val topY = 0f
        val bottomY = parent.height.toFloat()

        c.drawLine(centerX, topY, centerX, bottomY, paint)
    }
}