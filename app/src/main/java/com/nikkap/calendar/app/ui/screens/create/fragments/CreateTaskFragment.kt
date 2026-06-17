package com.nikkap.calendar.app.ui.screens.create.fragments

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListPopupWindow
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nikkap.calendar.app.R
import com.nikkap.calendar.app.databinding.CreateTaskFragmentBinding
import com.nikkap.calendar.app.ui.screens.create.CreateState
import com.nikkap.calendar.app.ui.screens.create.CreateTaskIntent
import com.nikkap.calendar.app.ui.screens.create.CreateViewModel
import com.nikkap.calendar.app.ui.screens.create.subtask.SubtaskAdapter
import com.nikkap.calendar.app.ui.screens.create.subtask.SubtasksAnimator
import com.nikkap.calendar.app.ui.utils.showDatePicker
import com.nikkap.calendar.core.utils.toShortUiDate
import com.nikkap.calendar.domain.model.TaskList
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.UUID

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
        val adapter = SubtaskAdapter(
            onDeleteClick = { subtask ->
                viewModel.onTaskIntent(
                    CreateTaskIntent.DeleteSubtask(
                        subtask.id
                    )
                )
            },
            onCheckedChange = { subtask, isChecked ->
                viewModel.onTaskIntent(
                    CreateTaskIntent.UpdateCompleteSubtask(
                        subtask.id,
                        isChecked
                    )
                )
            },
            onChangeTitle = { subtask ->
                viewModel.onTaskIntent(
                    CreateTaskIntent.UpdateTitleSubtask(
                        subtask.id,
                        subtask.title!!
                    )
                )
            }
        )
        observeState(adapter)
        setupListeners()
        binding.createTaskSubtasksRv.adapter = adapter
    }

    private fun setupListeners() {
        binding.createTaskDescriptionEditText.doAfterTextChanged {
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
        binding.createTaskAddSubtaskBtn.setOnClickListener {
            val text = binding.createTaskAddSubtaskEt.text.toString().trim()
            if (viewModel.checkSubtaskBeforeSave(text).isSuccess) {
                viewModel.onTaskIntent(CreateTaskIntent.AddSubtask(text))
                binding.createTaskAddSubtaskEt.text?.clear()
            }
        }
        binding.createTaskSubtasksRv.layoutManager = LinearLayoutManager(requireContext())
        binding.createTaskSubtasksRv.itemAnimator = SubtasksAnimator()
        binding.createTaskAddSubtaskEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                val text = binding.createTaskAddSubtaskEt.text.toString().trim()

                if (viewModel.checkSubtaskBeforeSave(text).isSuccess) {
                    viewModel.onTaskIntent(CreateTaskIntent.AddSubtask(text))
                    binding.createTaskAddSubtaskEt.text?.clear()
                }

                true
            } else {
                false
            }
        }
        binding.createTaskAddSubtaskEt.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                val text = binding.createTaskAddSubtaskEt.text.toString().trim()

                if (viewModel.checkSubtaskBeforeSave(text).isSuccess) {
                    viewModel.onTaskIntent(CreateTaskIntent.AddSubtask(text))
                    binding.createTaskAddSubtaskEt.text?.clear()
                }
                true
            } else false
        }
    }


    private fun observeState(adapter: SubtaskAdapter) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    updateUi(state)
                    adapter.submitList(state.subtasks)
                }
            }
        }
    }

    private fun updateUi(state: CreateState) {
        val task = state.taskDraft
        if (binding.createTaskDescriptionEditText.text.toString() != task.notes) {
            binding.createTaskDescriptionEditText.setText(task.notes)
        }

        if (binding.createTaskSetListButton.text.toString() != state.selectedTaskList?.title) {
            binding.createTaskSetListButton.text = state.selectedTaskList?.title
        }

        if (state.taskDraft.deadline != null && binding.createTaskDeadlineButton.text != state.taskDraft.deadline.toShortUiDate()) {
            binding.createTaskDeadlineButton.text = state.taskDraft.deadline.toShortUiDate()
        }

    }

    private fun showTaskListMenu(anchor: View, taskLists: List<TaskList>) {
        setTaskListPopup = ListPopupWindow(requireContext()).apply {
            val showTaskList =
                (taskLists + TaskList(id = "", "Create new task list")).toMutableList()
            setAdapter(
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    showTaskList.map { it.title }
                ))
            anchorView = anchor

            isModal = true

            setOnItemClickListener { _, _, position, _ ->
                if (position == taskLists.size) {
                    showCreateTaskListDialog()
                }
                val selectedList = showTaskList[position]

                dismiss()

                viewModel.onTaskIntent(CreateTaskIntent.UpdateList(selectedList))
            }
        }
        setTaskListPopup?.show()
    }

    private fun showCreateTaskListDialog() {
        val input = EditText(requireContext())
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("New task list")
            .setView(input)
            .setPositiveButton("Create") { _, _ -> }
            .setNegativeButton("Cancel", null)
            .show()

        val createButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        createButton.isEnabled = false

        input.doOnTextChanged { text, _, _, _ ->
            createButton.isEnabled = !text.isNullOrBlank()
        }

        createButton.setOnClickListener {
            val title = input.text.toString()
            viewModel.onTaskIntent(
                CreateTaskIntent.UpdateList(
                    TaskList(
                        id = UUID.randomUUID().toString().replace("-", ""),
                        title = title
                    )
                )
            )
            dialog.dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        setTaskListPopup?.dismiss()
        setTaskListPopup = null

    }
}