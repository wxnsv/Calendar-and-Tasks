package com.nikkap.calendar.ui.screens.create.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isGone
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nikkap.calendar.R
import com.nikkap.calendar.core.utils.toDate
import com.nikkap.calendar.core.utils.toTime
import com.nikkap.calendar.databinding.CreateEventFragmentBinding
import com.nikkap.calendar.ui.renderCreateDateTime
import com.nikkap.calendar.ui.screens.create.CreateEventIntent
import com.nikkap.calendar.ui.screens.create.CreateState
import com.nikkap.calendar.ui.screens.create.CreateViewModel
import com.nikkap.calendar.ui.setupSetColorRecyclerView
import com.nikkap.calendar.ui.showDatePicker
import com.nikkap.calendar.ui.showTimePicker
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreateEventFragment : Fragment(R.layout.create_event_fragment) {

    private var _binding: CreateEventFragmentBinding? = null

    private val binding get() = _binding!!

    private val viewModel: CreateViewModel by viewModel(
        ownerProducer = { requireParentFragment() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeState()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CreateEventFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupListeners() {
        binding.createEventRepeatButton.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.create_task_event_notification_menu, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                binding.createEventRepeatButton.text = item.title
                true
            }
            popup.show()
        }
        binding.createEventDescriptionEditText.doAfterTextChanged {
            viewModel.onEventIntent(CreateEventIntent.UpdateDescription(it.toString()))
        }
        binding.createEventAllDayButton.setOnClickListener {
            val isChecked = binding.createEventAllDayButton.isChecked
            viewModel.onEventIntent(CreateEventIntent.UpdateIsAllDay(isAllDay = isChecked))
            renderCreateDateTime(
                dateButton = binding.createEventStartDateButton,
                timeButton = binding.createEventStartTimeButton,
                container = binding.createEventStartDateContainer,
                isAllDay = isChecked
            )
            renderCreateDateTime(
                dateButton = binding.createEventEndDateButton,
                timeButton = binding.createEventEndTimeButton,
                container = binding.createEventEndDateContainer,
                isAllDay = isChecked
            )
        }
        binding.createEventStartDateButton.setOnClickListener {
            showDatePicker(
                onClick = { viewModel.onEventIntent(CreateEventIntent.UpdateStartDate(it)) },
                fragmentManager = parentFragmentManager
            )
        }
        binding.createEventStartTimeButton.setOnClickListener {
            showTimePicker(
                onClick = {
                    viewModel.onEventIntent(CreateEventIntent.UpdateStartTime(it))
                },
                requireContext(),
            )
        }
        binding.createEventEndTimeButton.setOnClickListener {
            showTimePicker(
                onClick = {
                    viewModel.onEventIntent(CreateEventIntent.UpdateEndTime(it))
                },
                requireContext(),
            )
        }
        binding.createEventEndDateButton.setOnClickListener {
            showDatePicker(
                onClick = { viewModel.onEventIntent(CreateEventIntent.UpdateEndDate(it)) },
                fragmentManager = parentFragmentManager
            )
        }
        setupSetColorRecyclerView(
            recyclerView = binding.createEventSetColorRv,
            onSet = { viewModel.onEventIntent(CreateEventIntent.UpdateColor(it)) },
            context = requireContext(),
            resources = resources
        )
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
        val event = state.eventDraft
        if (binding.createEventStartDateButton.text != event.startTimestamp.toDate() && event.startTimestamp != 0L) {
            binding.createEventStartDateButton.text = event.startTimestamp.toDate()
        }
        if (binding.createEventStartTimeButton.text != state.eventStartTime.toTime()) {
            binding.createEventStartTimeButton.text = state.eventStartTime.toTime()
        }
        if (binding.createEventEndTimeButton.text != state.eventEndTime.toTime()) {
            binding.createEventEndTimeButton.text = state.eventEndTime.toTime()
        }
        if (binding.createEventStartTimeButton.isGone != event.isAllDay) {
            binding.createEventStartTimeButton.isGone = event.isAllDay
            binding.createEventStartTimeButton.jumpDrawablesToCurrentState()
            renderCreateDateTime(
                dateButton = binding.createEventStartDateButton,
                timeButton = binding.createEventStartTimeButton,
                container = binding.createEventStartDateContainer,
                isAllDay = event.isAllDay
            )
        }
        if (binding.createEventEndTimeButton.isGone != event.isAllDay) {
            binding.createEventEndTimeButton.isGone = event.isAllDay
            binding.createEventEndTimeButton.jumpDrawablesToCurrentState()
            renderCreateDateTime(
                dateButton = binding.createEventEndDateButton,
                timeButton = binding.createEventEndTimeButton,
                container = binding.createEventEndDateContainer,
                isAllDay = event.isAllDay
            )
        }
        if (binding.createEventEndDateButton.text != event.endTimestamp.toDate() && event.endTimestamp != 0L) {
            binding.createEventEndDateButton.text = event.endTimestamp.toDate()
        }
        if (binding.createEventDescriptionEditText.text.toString() != event.description) {
            binding.createEventDescriptionEditText.setText(event.description)
        }
        if (binding.createEventAllDayButton.isChecked != event.isAllDay) {
            binding.createEventAllDayButton.isChecked = event.isAllDay
            binding.createEventAllDayButton.jumpDrawablesToCurrentState()
            renderCreateDateTime(
                dateButton = binding.createEventStartDateButton,
                timeButton = binding.createEventStartTimeButton,
                container = binding.createEventStartDateContainer,
                isAllDay = event.isAllDay
            )
            renderCreateDateTime(
                dateButton = binding.createEventEndDateButton,
                timeButton = binding.createEventEndTimeButton,
                container = binding.createEventEndDateContainer,
                isAllDay = event.isAllDay
            )
        }

    }
}