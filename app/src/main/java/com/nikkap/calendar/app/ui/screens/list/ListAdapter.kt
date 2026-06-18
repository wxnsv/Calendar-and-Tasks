package com.nikkap.calendar.app.ui.screens.list

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nikkap.calendar.app.R
import com.nikkap.calendar.core.utils.CalendarColors
import com.nikkap.calendar.core.utils.toListUiDate
import com.nikkap.calendar.core.utils.toUiDate
import com.nikkap.calendar.domain.model.Birthday
import com.nikkap.calendar.domain.model.Event
import com.nikkap.calendar.domain.model.Subtask
import com.nikkap.calendar.domain.model.Task

class ListAdapter(
    private val onItemClick: (String, String) -> Unit,
    private val onTaskComplete: (String, String) -> Unit,
    private val onDeleteClick: (ListItem) -> Unit
) : ListAdapter<ListItem, RecyclerView.ViewHolder>(ListDiffCallback()) {

    companion object {
        private const val TYPE_TASK = 0
        private const val TYPE_EVENT = 1
        private const val TYPE_BIRTHDAY = 2
        private const val TYPE_SUBTASK = 3
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ListItem.TaskItem -> TYPE_TASK
            is ListItem.EventItem -> TYPE_EVENT
            is ListItem.BirthdayItem -> TYPE_BIRTHDAY
            is ListItem.SubtaskItem -> TYPE_SUBTASK
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TASK -> {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
                TaskViewHolder(view)
            }

            TYPE_EVENT -> {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
                EventViewHolder(view)
            }

            TYPE_BIRTHDAY -> {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
                BirthdayViewHolder(view)
            }

            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_subtask, parent, false)
                SubtaskViewHolder(view)
            }
        }
    }

    class TaskViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.itemText)
        val itemType: TextView = view.findViewById(R.id.list_item_type_tv)
        val itemIcon: ImageView = view.findViewById(R.id.list_item_type_icon)
        val itemTime: TextView = view.findViewById(R.id.item_timestamp)
        val itemCheckBox: CheckBox = view.findViewById(R.id.list_item_checkbox)


        fun bind(
            task: Task, onClick: () -> Unit, onTaskComplete: () -> Unit, onDelete: () -> Unit
        ) {
            itemTime.text = task.deadline?.toListUiDate() ?: ""
            title.text = task.title
            itemType.text = "Task"
            itemIcon.setImageResource(R.drawable.task)
            itemIcon.imageTintList = ColorStateList.valueOf(
                "#3F51B5".toColorInt()
            )
            itemCheckBox.isChecked = task.isCompleted
            itemView.setOnClickListener {
                onClick()
            }
            itemCheckBox.setOnClickListener { onTaskComplete() }

            view.setOnLongClickListener { view ->
                val popup = androidx.appcompat.widget.PopupMenu(view.context, view)

                popup.menuInflater.inflate(R.menu.list_item_menu, popup.menu)

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_edit -> {
                            onClick()
                            true
                        }

                        R.id.action_delete -> {
                            onDelete()
                            true
                        }

                        else -> false
                    }
                }
                popup.show()

                true
            }
        }
    }

    // EVENT
    class EventViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val eventTitle: TextView = view.findViewById(R.id.itemText)
        val eventTime: TextView = view.findViewById(R.id.item_timestamp)
        val itemType: TextView = view.findViewById(R.id.list_item_type_tv)
        val itemIcon: ImageView = view.findViewById(R.id.list_item_type_icon)
        val itemCheckBox: CheckBox = view.findViewById(R.id.list_item_checkbox)

        fun bind(event: Event, onClick: () -> Unit, onDelete: () -> Unit) {
            val color = CalendarColors.getBirthdayColor(event.colorId).hex.toColorInt()
            eventTitle.text = event.summary
            eventTime.text = event.startTimestamp.toListUiDate(event.isAllDay)
            itemType.text = "Event"
            itemIcon.setImageResource(R.drawable.event)
            itemView.setOnClickListener {
                onClick()
            }
            itemIcon.imageTintList = ColorStateList.valueOf(color)
            itemCheckBox.visibility = View.GONE

            view.setOnLongClickListener { view ->
                val popup = androidx.appcompat.widget.PopupMenu(view.context, view)

                popup.menuInflater.inflate(R.menu.list_item_menu, popup.menu)

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_edit -> {
                            onClick()
                            true
                        }

                        R.id.action_delete -> {
                            onDelete()
                            true
                        }

                        else -> false
                    }
                }
                popup.show()

                true
            }
        }
    }

    class BirthdayViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val birthdayName: TextView = view.findViewById(R.id.itemText)
        val birthdayDate: TextView = view.findViewById(R.id.item_timestamp)
        val itemType: TextView = view.findViewById(R.id.list_item_type_tv)
        val itemIcon: ImageView = view.findViewById(R.id.list_item_type_icon)
        val itemCheckBox: CheckBox = view.findViewById(R.id.list_item_checkbox)

        fun bind(birthday: Birthday, onClick: () -> Unit, onDelete: () -> Unit) {
            val color = CalendarColors.getBirthdayColor(birthday.colorId).hex.toColorInt()
            birthdayName.text = birthday.name
            birthdayDate.text = birthday.date.toUiDate()
            itemType.text = "Birthday"
            itemIcon.setImageResource(R.drawable.birthday)
            itemView.setOnClickListener {
                onClick()
            }
            itemIcon.imageTintList = ColorStateList.valueOf(color)
            itemCheckBox.visibility = View.GONE

            view.setOnLongClickListener { view ->
                val popup = androidx.appcompat.widget.PopupMenu(view.context, view)

                popup.menuInflater.inflate(R.menu.list_item_menu, popup.menu)

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_edit -> {
                            onClick()
                            true
                        }

                        R.id.action_delete -> {
                            onDelete()
                            true
                        }

                        else -> false
                    }
                }
                popup.show()

                true
            }
        }
    }

    class SubtaskViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val subtaskTitle: TextView = view.findViewById(R.id.list_subtask_title)
        val itemCheckBox: CheckBox = view.findViewById(R.id.list_subtask_checkbox)
        fun bind(subtask: Subtask, onSubtaskComplete: () -> Unit, onDelete: () -> Unit) {
            subtaskTitle.text = subtask.title
            itemCheckBox.setOnClickListener {
                onSubtaskComplete()
            }

            view.setOnLongClickListener { view ->
                val popup = androidx.appcompat.widget.PopupMenu(view.context, view)

                popup.menuInflater.inflate(R.menu.list_subtask_menu, popup.menu)

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {

                        R.id.action_subtask_delete -> {
                            onDelete()
                            true
                        }

                        else -> false
                    }
                }
                popup.show()

                true
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ListItem.TaskItem -> {
                (holder as TaskViewHolder).bind(
                    item.task,
                    { onItemClick(item.task.id!!, "TASK") },
                    { onTaskComplete(item.task.id!!, "TASK") },
                    { onDeleteClick(item) })
            }

            is ListItem.EventItem -> {
                (holder as EventViewHolder).bind(
                    item.event,
                    { onItemClick(item.event.id!!, "EVENT") },
                    { onDeleteClick(item) })
            }

            is ListItem.BirthdayItem -> {
                (holder as BirthdayViewHolder).bind(
                    item.birthday,
                    { onItemClick(item.birthday.id!!, "BIRTHDAY") },
                    { onDeleteClick(item) })
            }

            is ListItem.SubtaskItem -> {
                (holder as SubtaskViewHolder).bind(
                    item.subtask,
                    { onTaskComplete(item.subtask.id, "SUBTASK") },
                    { onDeleteClick(item) })
            }
        }
    }

    class ListDiffCallback : DiffUtil.ItemCallback<ListItem>() {
        override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return oldItem.id == newItem.id && oldItem::class == newItem::class
        }

        override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return oldItem == newItem
        }
    }
}