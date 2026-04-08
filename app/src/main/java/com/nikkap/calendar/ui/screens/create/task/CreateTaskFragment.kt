package com.nikkap.calendar.ui.screens.create.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import androidx.appcompat.widget.PopupMenu
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nikkap.calendar.R
import com.nikkap.calendar.core.utils.toListDate
import com.nikkap.calendar.databinding.CreateTaskFragmentBinding
import com.nikkap.calendar.domain.model.TaskList
import com.nikkap.calendar.ui.screens.create.CreateState
import com.nikkap.calendar.ui.screens.create.CreateTaskIntent
import com.nikkap.calendar.ui.screens.create.CreateViewModel
import com.nikkap.calendar.ui.showDatePicker
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreateTaskFragment : Fragment(R.layout.create_task_fragment) {

    private var _binding: CreateTaskFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreateViewModel by viewModel(ownerProducer = { requireParentFragment() })
    private var setTaskListPopup: ListPopupWindow? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CreateTaskFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeState()
        setupListeners()
    }

    private fun setupListeners() {
        binding.createTaskRepeatButton.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.create_task_event_notification_menu, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                binding.createTaskRepeatButton.text = item.title
                viewModel.onTaskIntent(CreateTaskIntent.UpdateRepeat(item.title.toString()))
                true
            }
            popup.show()
        }
        binding.createTaskDetailsEditText.doAfterTextChanged {
            viewModel.onTaskIntent(CreateTaskIntent.UpdateDescription(it.toString()))
        }
        binding.createTaskDeadlineButton.setOnClickListener {
            val task = viewModel.state.value.taskDraft
            showDatePicker(
                onClick = { viewModel.onTaskIntent(CreateTaskIntent.UpdateDeadline(it)) },
                fragmentManager = parentFragmentManager,
                task.deadline ?: System.currentTimeMillis()
            )
        }
        binding.createTaskSetListButton.setOnClickListener { view ->
            val state = viewModel.state.value
            if (setTaskListPopup?.isShowing == true) {
                setTaskListPopup?.dismiss()
            } else {
                showTaskListMenu(view, state.taskLists)
            }
        }
    }


    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    updateUi(state)
                }
            }
        }
    }

    private fun updateUi(state: CreateState) {
        val task = state.taskDraft
        if (binding.createTaskDetailsEditText.text.toString() != task.notes) {
            binding.createTaskDetailsEditText.setText(task.notes)
        }

        if (binding.createTaskSetListButton.text.toString() != state.selectedTaskList?.title) {
            binding.createTaskSetListButton.text = state.selectedTaskList?.title
        }

        if (binding.createTaskDeadlineButton.text != state.taskDraft.deadline.toListDate() && state.taskDraft.deadline != null) {
            binding.createTaskDeadlineButton.text = state.taskDraft.deadline.toListDate()
        }

    }

    private fun showTaskListMenu(anchor: View, taskLists: List<TaskList>) {
        setTaskListPopup = ListPopupWindow(requireContext()).apply {
            setAdapter(
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    taskLists.map { it.title })
            )
            anchorView = anchor

            isModal = true

            setOnItemClickListener { _, _, position, _ ->
                val selectedList = taskLists[position]

                dismiss()

                viewModel.onTaskIntent(CreateTaskIntent.UpdateList(selectedList))
            }
        }
        setTaskListPopup?.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        setTaskListPopup?.dismiss()
        setTaskListPopup = null

    }
}