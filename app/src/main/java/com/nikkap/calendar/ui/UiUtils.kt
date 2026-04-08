package com.nikkap.calendar.ui

import android.app.TimePickerDialog
import android.content.Context
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.nikkap.calendar.core.utils.CalendarColors
import com.nikkap.calendar.ui.screens.create.colorpicker.ColorPickerAdapter
import com.nikkap.calendar.ui.screens.create.colorpicker.ColorPickerFadeDecoration
import java.util.Calendar

fun showDatePicker(onClick: (Long) -> Unit, fragmentManager: FragmentManager, dateLong: Long) {
    val datePicker = MaterialDatePicker.Builder.datePicker()
        .setTitleText("Set date")
        .setSelection(dateLong)
        .build()

    datePicker.addOnPositiveButtonClickListener { selection ->
        onClick(selection)
    }

    datePicker.show(fragmentManager, "DATE_PICKER")
}

fun showTimePicker(onClick: (Long) -> Unit, context: Context, calendar: Calendar) {
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    val timePickerDialog = TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            val timeAsOffset: Long = (selectedHour * 3600L + selectedMinute * 60L) * 1000L
            onClick(timeAsOffset)
        },
        hour,
        minute,
        true
    )

    timePickerDialog.show()
}

fun setupSetColorRecyclerView(
    recyclerView: RecyclerView,
    onSet: (String) -> Unit,
    context: Context,
    resources: android.content.res.Resources
) {

    val snapHelper = LinearSnapHelper()
    snapHelper.attachToRecyclerView(recyclerView)
    val colorAdapter = ColorPickerAdapter()

    recyclerView.apply {
        adapter = colorAdapter
        layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
    }

    fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    colorAdapter.submitList(CalendarColors.entries)
    val displayMetrics = resources.displayMetrics
    val screenWidth = displayMetrics.widthPixels
    val middlePosition = 6
    recyclerView.scrollToPosition(middlePosition)
    val itemWidth = 60.dpToPx()
    val padding = (screenWidth / 2) - (itemWidth / 2)
    recyclerView.addItemDecoration(ColorPickerFadeDecoration())
    recyclerView.setPadding(padding, 0, padding, 0)
    recyclerView.clipToPadding = false
    recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                val centerView = snapHelper.findSnapView(recyclerView.layoutManager)
                if (centerView != null) {
                    val position = recyclerView.getChildAdapterPosition(centerView)

                    val actualColors = CalendarColors.entries
                    val selectedColor = actualColors[position % actualColors.size]

                    onSet(selectedColor.id)
                }
            }
        }
    })
}

fun renderCreateDateTime(
    dateButton: MaterialButton,
    timeButton: MaterialButton,
    container: LinearLayout,
    isAllDay: Boolean
) {


    val containerHeight = container.height

    if (containerHeight > 0) {
        container.layoutParams.height = containerHeight
        container.requestLayout()
    }

    if (isAllDay) {
        timeButton.animate().alpha(0f).setDuration(150).withEndAction {
            timeButton.visibility = View.GONE
        }

        val startParams = dateButton.layoutParams as LinearLayout.LayoutParams
        startParams.weight = 3f
        dateButton.layoutParams = startParams
    } else {
        timeButton.visibility = View.VISIBLE
        timeButton.alpha = 0f
        timeButton.animate().alpha(1f).setDuration(200).start()

        val startParams = dateButton.layoutParams as LinearLayout.LayoutParams
        startParams.weight = 2f
        dateButton.layoutParams = startParams
    }
}