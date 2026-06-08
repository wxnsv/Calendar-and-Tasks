package com.nikkap.calendar.app.ui.utils

import android.app.TimePickerDialog
import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import androidx.annotation.AttrRes
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.core.view.isNotEmpty
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.nikkap.calendar.app.ui.screens.create.colorpicker.ColorPickerAdapter
import com.nikkap.calendar.app.ui.screens.create.colorpicker.ColorPickerFadeDecoration
import com.nikkap.calendar.core.utils.CalendarColors
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
    initialColorId: Int? = null,
    onSet: (Int) -> Unit,
    context: Context,
    resources: Resources,
    snapHelper: LinearSnapHelper
) {
    val allColors = CalendarColors.entries

    if (recyclerView.tag == null) {
        snapHelper.attachToRecyclerView(recyclerView)

        val colorAdapter = ColorPickerAdapter()
        recyclerView.apply {
            adapter = colorAdapter
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)

            if (itemDecorationCount == 0) {
                addItemDecoration(ColorPickerFadeDecoration(context))
            }

            val density = resources.displayMetrics.density
            val itemWidth = (40 * density).toInt()
            val padding = (resources.displayMetrics.widthPixels / 2) - (itemWidth / 2)

            setPadding(padding, 0, padding, 0)
            clipToPadding = false

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val lm = layoutManager as? LinearLayoutManager ?: return
                        val centerView = snapHelper.findSnapView(lm)

                        centerView?.let { view ->
                            val position = getChildAdapterPosition(view)
                            if (position != RecyclerView.NO_POSITION) {
                                onSet(allColors[position].id)
                            }
                        }
                    }
                }
            })
        }
        colorAdapter.submitList(allColors)
        recyclerView.tag = "READY"
    }

    if (initialColorId != null) {
        val targetPosition = allColors.indexOfFirst { it.id == initialColorId }.let {
            if (it != -1) it else 6
        }

        recyclerView.post {
            val lm = recyclerView.layoutManager as? LinearLayoutManager ?: return@post

            lm.scrollToPositionWithOffset(targetPosition, 0)
        }
    }
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

inline fun RecyclerView.onFirstDraw(crossinline action: () -> Unit) {
    viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            if (isNotEmpty()) {
                viewTreeObserver.removeOnPreDrawListener(this)
                action()
            }
            return true
        }
    })
}

fun Context.getColorFromAttr(@AttrRes attrColor: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrColor, typedValue, true)
    return typedValue.data
}

fun String?.toColor(): Color {
    if (this.isNullOrEmpty()) return Color.Gray

    return try {
        val hexToParse = if (this.startsWith("#")) this else "#$this"

        Color(hexToParse.toColorInt())
    } catch (_: Exception) {
        Color.Black
    }
}

