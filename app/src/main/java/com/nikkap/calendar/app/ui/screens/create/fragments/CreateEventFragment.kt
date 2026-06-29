package com.nikkap.calendar.app.ui.screens.create.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.nikkap.calendar.app.R
import com.nikkap.calendar.app.databinding.CreateEventFragmentBinding
import com.nikkap.calendar.app.ui.screens.create.CreateEventIntent
import com.nikkap.calendar.app.ui.screens.create.CreateState
import com.nikkap.calendar.app.ui.screens.create.CreateViewModel
import com.nikkap.calendar.app.ui.utils.getColorFromAttr
import com.nikkap.calendar.app.ui.utils.renderCreateDateTime
import com.nikkap.calendar.app.ui.utils.setupSetColorRecyclerView
import com.nikkap.calendar.app.ui.utils.showDatePicker
import com.nikkap.calendar.app.ui.utils.showTimePicker
import com.nikkap.calendar.core.utils.CalendarColors
import com.nikkap.calendar.core.utils.toCalendar
import com.nikkap.calendar.core.utils.toOnlyDateLong
import com.nikkap.calendar.core.utils.toShortUiDate
import com.nikkap.calendar.core.utils.toTimeLong
import com.nikkap.calendar.core.utils.toUiTime
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreateEventFragment : Fragment(R.layout.create_event_fragment) {

    private var _binding: CreateEventFragmentBinding? = null

    private val binding get() = _binding!!

    private val viewModel: CreateViewModel by viewModel(
        ownerProducer = { parentFragment ?: this }
    )

    private val snapHelper = LinearSnapHelper()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeState()
        setupListeners()
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
            val event = viewModel.state.value.eventDraft
            showDatePicker(
                onClick = { viewModel.onEventIntent(CreateEventIntent.UpdateStartDate(it)) },
                fragmentManager = parentFragmentManager,
                if (event.startTimestamp != 0L) event.startTimestamp else System.currentTimeMillis()
            )
        }
        binding.createEventStartTimeButton.setOnClickListener {
            showTimePicker(
                onClick = {
                    viewModel.onEventIntent(CreateEventIntent.UpdateStartTime(it))
                    viewModel.onEventIntent(CreateEventIntent.UpdateStartDate(viewModel.state.value.eventDraft.startTimestamp))
                },
                requireContext(),
                viewModel.state.value.eventStartTime.toCalendar()
            )
        }
        binding.createEventEndTimeButton.setOnClickListener {
            showTimePicker(
                onClick = {
                    viewModel.onEventIntent(CreateEventIntent.UpdateEndTime(it))
                },
                requireContext(),
                viewModel.state.value.eventEndTime.toCalendar()
            )
        }
        binding.createEventEndDateButton.setOnClickListener {
            val event = viewModel.state.value.eventDraft
            showDatePicker(
                onClick = { viewModel.onEventIntent(CreateEventIntent.UpdateEndDate(it)) },
                fragmentManager = parentFragmentManager,
                if (event.endTimestamp != 0L) event.endTimestamp else System.currentTimeMillis()
            )
        }
        setupSetColorRecyclerView(
            recyclerView = binding.createEventSetColorRv,
            initialColorId = CalendarColors.getEventColor(
                viewModel.state.value.eventDraft.colorId
            ).id,
            onSet = {
                viewModel.onEventIntent(CreateEventIntent.UpdateColor(it))
            },
            context = requireContext(),
            resources = resources,
            snapHelper = snapHelper
        )
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    updateUi(state)
                    renderColorPicker(state)
                }
            }
        }
    }


    private fun updateUi(state: CreateState) {
        val event = state.eventDraft
        val currentTimestamp = System.currentTimeMillis()
        val currentDate = currentTimestamp.toOnlyDateLong()
        val currentTime = currentTimestamp.toTimeLong()
        if (binding.createEventStartDateButton.text != event.startTimestamp.toShortUiDate() && event.startTimestamp != 0L) {
            binding.createEventStartDateButton.text = event.startTimestamp.toShortUiDate()
        }
        /**
         * if start date earlier than today do text red
         */
        if (state.eventStartDate < currentDate) {
            binding.createEventStartDateButton.setTextColor(
                ColorStateList.valueOf(
                    requireContext().getColorFromAttr(
                        com.google.android.material.R.attr.colorErrorContainer
                    )
                )
            )
        } else {
            binding.createEventStartDateButton.setTextColor(
                ColorStateList.valueOf(
                    requireContext().getColorFromAttr(
                        com.google.android.material.R.attr.colorOnSurface
                    )
                )
            )
        }
        /**
         * if end date earlier than today do text red
         */
        if (state.eventEndDate < currentDate) {
            binding.createEventEndDateButton.setTextColor(
                ColorStateList.valueOf(
                    requireContext().getColorFromAttr(
                        com.google.android.material.R.attr.colorErrorContainer
                    )
                )
            )
        } else {
            binding.createEventEndDateButton.setTextColor(
                ColorStateList.valueOf(
                    requireContext().getColorFromAttr(
                        com.google.android.material.R.attr.colorOnSurface
                    )
                )
            )
        }

        /**
         * if end time earlier than now do text red
         */
        if (state.eventEndTime < currentTime) {
            binding.createEventEndTimeButton.setTextColor(
                ColorStateList.valueOf(
                    requireContext().getColorFromAttr(
                        com.google.android.material.R.attr.colorErrorContainer
                    )
                )
            )
        } else {
            binding.createEventEndTimeButton.setTextColor(
                ColorStateList.valueOf(
                    requireContext().getColorFromAttr(
                        com.google.android.material.R.attr.colorOnSurface
                    )
                )
            )
        }
        /**
         * if end time earlier than start time in one day do text red
         */
        if (state.eventStartDate == state.eventEndDate && state.eventStartTime > state.eventEndTime) {
            binding.createEventEndTimeButton.setTextColor(
                ColorStateList.valueOf(
                    requireContext().getColorFromAttr(
                        com.google.android.material.R.attr.colorErrorContainer
                    )
                )
            )
        } else {
            binding.createEventEndTimeButton.setTextColor(
                ColorStateList.valueOf(
                    requireContext().getColorFromAttr(
                        com.google.android.material.R.attr.colorOnSurface
                    )
                )
            )
        }
        /**
         * if end date earlier than start date do text red
         */
        if (state.eventStartDate > state.eventEndDate && !state.eventDraft.isAllDay) {
            binding.createEventEndDateButton.setTextColor(
                ColorStateList.valueOf(
                    requireContext().getColorFromAttr(
                        com.google.android.material.R.attr.colorErrorContainer
                    )
                )
            )
        } else {
            binding.createEventEndDateButton.setTextColor(
                ColorStateList.valueOf(
                    requireContext().getColorFromAttr(
                        com.google.android.material.R.attr.colorOnSurface
                    )
                )
            )
        }


        if (binding.createEventStartTimeButton.text != state.eventStartTime.toUiTime()) {
            binding.createEventStartTimeButton.text = state.eventStartTime.toUiTime()
        }
        if (binding.createEventEndTimeButton.text != state.eventEndTime.toUiTime()) {
            binding.createEventEndTimeButton.text = state.eventEndTime.toUiTime()
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
        if (binding.createEventEndDateButton.text != event.endTimestamp.toShortUiDate() && event.endTimestamp != 0L) {
            binding.createEventEndDateButton.text = event.endTimestamp.toShortUiDate()
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

    private fun renderColorPicker(state: CreateState) {
        val allColors = CalendarColors.entries
        val targetPosition = allColors.indexOfFirst { it.id == state.eventDraft.colorId }
        if (targetPosition == -1) return

        val layoutManager =
            binding.createEventSetColorRv.layoutManager as? LinearLayoutManager ?: return

        val centerView = snapHelper.findSnapView(layoutManager)
        val currentCenterPos = centerView?.let { layoutManager.getPosition(it) } ?: -1

        if (currentCenterPos != targetPosition && binding.createEventSetColorRv.scrollState == RecyclerView.SCROLL_STATE_IDLE) {

            binding.createEventSetColorRv.post {
                if (binding.createEventSetColorRv.isAttachedToWindow) {
                    layoutManager.scrollToPositionWithOffset(targetPosition, 0)
                }
            }
        }
    }
}