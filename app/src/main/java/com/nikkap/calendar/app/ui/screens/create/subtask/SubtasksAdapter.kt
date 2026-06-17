package com.nikkap.calendar.app.ui.screens.create.subtask

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.nikkap.calendar.app.R
import com.nikkap.calendar.domain.model.Subtask

class SubtaskAdapter(
    private val onChangeTitle: (Subtask) -> Unit,
    private val onDeleteClick: (Subtask) -> Unit,
    private val onCheckedChange: (Subtask, Boolean) -> Unit
) : ListAdapter<Subtask, SubtaskAdapter.SubtaskViewHolder>(SubtaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.create_subtask_item, parent, false)
        return SubtaskViewHolder(view, onChangeTitle, onDeleteClick, onCheckedChange)
    }

    override fun onBindViewHolder(holder: SubtaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SubtaskViewHolder(
        itemView: View,
        private val onChangeTitle: (Subtask) -> Unit,
        private val onDeleteClick: (Subtask) -> Unit,
        private val onCheckedChange: (Subtask, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val checkBox: MaterialCheckBox = itemView.findViewById(R.id.create_subtask_checkbox)
        private val textView: TextInputEditText = itemView.findViewById(R.id.create_subtask_text)
        private val deleteButton: MaterialButton =
            itemView.findViewById(R.id.create_subtask_delete_btn)

        fun bind(subtask: Subtask) {
            textView.onFocusChangeListener = null
            textView.setText(subtask.title)
            textView.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    val newTitle = textView.text.toString()
                    if (newTitle != subtask.title) {
                        onChangeTitle(subtask.copy(title = newTitle))
                    }
                }
            }

            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = subtask.isCompleted

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                onCheckedChange(subtask, isChecked)
            }

            deleteButton.setOnClickListener {
                onDeleteClick(subtask)
            }
        }
    }

    class SubtaskDiffCallback : DiffUtil.ItemCallback<Subtask>() {
        override fun areItemsTheSame(oldItem: Subtask, newItem: Subtask): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Subtask, newItem: Subtask): Boolean {
            return oldItem == newItem
        }
    }
}