package com.nikkap.calendar.app.ui.screens.create.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.google.android.material.button.MaterialButton
import com.nikkap.calendar.app.R
import com.nikkap.calendar.app.databinding.CreateFragmentBinding
import com.nikkap.calendar.app.ui.screens.create.CreateIntent
import com.nikkap.calendar.app.ui.screens.create.CreateState
import com.nikkap.calendar.app.ui.screens.create.CreateViewModel
import com.nikkap.calendar.app.ui.screens.main.MainViewModel
import com.nikkap.calendar.app.ui.utils.getColorFromAttr
import com.nikkap.calendar.domain.model.Birthday
import com.nikkap.calendar.domain.model.CalendarEntry
import com.nikkap.calendar.domain.model.Event
import com.nikkap.calendar.domain.model.Task
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreateFragment : Fragment(R.layout.create_fragment) {
    private var _binding: CreateFragmentBinding? = null

    private val binding get() = _binding!!

    private val viewModel: CreateViewModel by viewModel()

    private val sharedViewModel: MainViewModel by activityViewModels()

    private val args: CreateFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CreateFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initial()
        setupListeners()
        observeState()
        observeErrors()
    }

    private fun observeErrors() {
        lifecycleScope.launch {
            viewModel.errorEvents.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { errorMessage ->

                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateNestedFragment(item: CalendarEntry) {
        val activeType = viewModel.state.value.activeType
        val fragment = when (item) {
            is Task -> {
                binding.createEditText.hint = "Add title"
                viewModel.onIntent(CreateIntent.UpdateShowFragment("TASK"))
                CreateTaskFragment()
            }

            is Event -> {
                binding.createEditText.hint = "Add title"
                viewModel.onIntent(CreateIntent.UpdateShowFragment("EVENT"))
                CreateEventFragment()
            }

            is Birthday -> {
                binding.createEditText.hint = "Add name"
                viewModel.onIntent(CreateIntent.UpdateShowFragment("BIRTHDAY"))
                CreateBirthdayFragment()
            }

            else -> null
        }

        fragment.let {
            childFragmentManager.beginTransaction()
                .replace(R.id.create_fcv, it!!)
                .commit()
        }
    }

    private fun setupListeners() {
        binding.createTaskButton.setOnClickListener {
            if (viewModel.state.value.activeType != "TASK")
            updateNestedFragment(viewModel.state.value.taskDraft)
        }
        binding.createEventButton.setOnClickListener {
            if (viewModel.state.value.activeType != "EVENT")
            updateNestedFragment(viewModel.state.value.eventDraft)
        }
        binding.createBirthdayButton.setOnClickListener {
            if (viewModel.state.value.activeType != "BIRTHDAY")
            updateNestedFragment(viewModel.state.value.birthdayDraft)
        }
        binding.createEditText.doAfterTextChanged {
            viewModel.onIntent(CreateIntent.UpdateTitle(it.toString()))
        }
        binding.createBackButton.setOnClickListener {
            sharedViewModel.popBackStack()
        }
        binding.createSaveButton.setOnClickListener {
            val result = viewModel.checkItemAndSave()

            if (result.isSuccess) sharedViewModel.popBackStack()
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

    private fun doButtonInactive(button: MaterialButton) {
        button.isEnabled = false
        button.setTextColor(
            ColorStateList.valueOf(
                requireContext().getColorFromAttr(
                    com.google.android.material.R.attr.colorOutlineVariant
                )
            )
        )
        button.iconTint = ColorStateList.valueOf(
            requireContext().getColorFromAttr(
                com.google.android.material.R.attr.colorOutlineVariant
            )
        )
        button.backgroundTintList = ColorStateList.valueOf(
            requireContext().getColor(R.color.trans)
        )
    }

    private fun doButtonFocused(button: MaterialButton) {
        button.setTextColor(
            ColorStateList.valueOf(
                requireContext().getColorFromAttr(
                    com.google.android.material.R.attr.colorOnPrimary
                )
            )
        )
        button.iconTint = ColorStateList.valueOf(
            requireContext().getColorFromAttr(
                com.google.android.material.R.attr.colorOnPrimary
            )
        )
        button.backgroundTintList = ColorStateList.valueOf(
            requireContext().getColorFromAttr(
                androidx.appcompat.R.attr.colorPrimary
            )
        )
    }

    private fun doButtonUnFocused(button: MaterialButton) {
        button.setTextColor(
            ColorStateList.valueOf(
                requireContext().getColorFromAttr(
                    com.google.android.material.R.attr.colorOnSecondaryContainer
                )
            )
        )
        button.iconTint = ColorStateList.valueOf(
            requireContext().getColorFromAttr(
                com.google.android.material.R.attr.colorOnSecondaryContainer
            )
        )
        button.backgroundTintList = ColorStateList.valueOf(
            requireContext().getColor(R.color.trans)
        )
    }


    private fun initial() {
        viewModel.onIntent(CreateIntent.UpdateShowFragment(args.type))
        val createType = args.type
        viewModel.onIntent(CreateIntent.UpdateShowFragment(createType))
        val itemId = args.id
        viewModel.onIntent(CreateIntent.UpdateItem(type = createType, itemId))
        when (createType) {
            "TASK" -> updateNestedFragment(viewModel.state.value.taskDraft)
            "EVENT" -> updateNestedFragment(viewModel.state.value.eventDraft)
            "BIRTHDAY" -> updateNestedFragment(viewModel.state.value.birthdayDraft)
        }
        if (itemId.isNotEmpty() && createType != "TASK") doButtonInactive(binding.createTaskButton)
        if (itemId.isNotEmpty() && createType != "EVENT") doButtonInactive(binding.createEventButton)
        if (itemId.isNotEmpty() && createType != "BIRTHDAY") doButtonInactive(binding.createBirthdayButton)
    }

    private fun updateUi(state: CreateState) {
        if (binding.createEditText.text.toString() != state.title) {
            binding.createEditText.setText(state.title)
        }
        if (state.activeType != "TASK" && !state.isEditing) {
            doButtonUnFocused(binding.createTaskButton)
        } else if (!state.isEditing || state.activeType == "TASK") {
            doButtonFocused(binding.createTaskButton)
        } else doButtonInactive(binding.createTaskButton)
        if (state.activeType != "EVENT" && !state.isEditing) {
            doButtonUnFocused(binding.createEventButton)
        } else if (!state.isEditing || state.activeType == "EVENT") {
            doButtonFocused(binding.createEventButton)
        } else doButtonInactive(binding.createEventButton)
        if (state.activeType != "BIRTHDAY" && !state.isEditing) {
            doButtonUnFocused(binding.createBirthdayButton)
        } else if (!state.isEditing || state.activeType == "BIRTHDAY") {
            doButtonFocused(binding.createBirthdayButton)
        } else doButtonInactive(binding.createBirthdayButton)
    }
}