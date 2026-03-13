package com.nikkap.calendar.ui.screens.create.colorpicker

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nikkap.calendar.core.utils.CalendarColors
import com.nikkap.calendar.databinding.ItemColorPickerBinding

class ColorPickerAdapter :
    ListAdapter<CalendarColors, ColorPickerAdapter.ViewHolder>(DiffCallback) {

    private var selectedColorId: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemColorPickerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position % CalendarColors.entries.size)
        with(holder.binding) {
            createItemColor.backgroundTintList = ColorStateList.valueOf(item.hex.toColorInt())

            ivSelected.isVisible = item.id == selectedColorId
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<CalendarColors>() {
        override fun areItemsTheSame(oldItem: CalendarColors, newItem: CalendarColors): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CalendarColors, newItem: CalendarColors): Boolean {
            return oldItem == newItem
        }
    }

    class ViewHolder(val binding: ItemColorPickerBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int {
        return CalendarColors.entries.size
    }
}