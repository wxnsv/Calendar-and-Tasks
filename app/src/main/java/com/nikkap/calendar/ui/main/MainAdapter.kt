package com.nikkap.calendar.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nikkap.calendar.R
import com.nikkap.calendar.domain.model.Event
import com.nikkap.calendar.domain.model.Task

class MainAdapter(private var items: List<ListItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_TASK = 0
        private const val TYPE_EVENT = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ListItem.TaskItem -> TYPE_TASK
            is ListItem.EventItem -> TYPE_EVENT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TASK -> {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
                TaskViewHolder(view)
            }

            else -> {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
                EventViewHolder(view)
            }
        }
    }

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.itemText)
        val time: TextView = view.findViewById(R.id.item_timestamp)
        val checkBox: CheckBox = view.findViewById(R.id.checkBox)

        fun bind(task: Task) {
            title.text = task.title
//            time.text = task.timestamp
//            checkBox.isChecked = task.isDone
        }
    }

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eventTitle: TextView = view.findViewById(R.id.itemText)
        val eventLocation: TextView = view.findViewById(R.id.item_timestamp)

        fun bind(event: Event) {
            eventTitle.text = event.summary
            eventLocation.text = event.startDate
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ListItem.TaskItem -> (holder as TaskViewHolder).bind(item.task)
            is ListItem.EventItem -> (holder as EventViewHolder).bind(item.event)
        }
    }

    override fun getItemCount() = items.size

    fun updateList(newList: List<ListItem>) {
        this.items = newList
        notifyDataSetChanged()
    }
}