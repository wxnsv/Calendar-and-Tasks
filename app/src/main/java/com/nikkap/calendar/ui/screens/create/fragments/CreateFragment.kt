package com.nikkap.calendar.ui.screens.create.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.nikkap.calendar.R
import com.nikkap.calendar.databinding.CreateFragmentBinding
import com.nikkap.calendar.domain.model.Birthday
import com.nikkap.calendar.domain.model.CalendarEntry
import com.nikkap.calendar.domain.model.Event
import com.nikkap.calendar.domain.model.Task
import com.nikkap.calendar.ui.screens.create.CreateIntent
import com.nikkap.calendar.ui.screens.create.CreateState
import com.nikkap.calendar.ui.screens.create.CreateViewModel
import com.nikkap.calendar.ui.screens.main.MainViewModel
import com.nikkap.calendar.ui.utils.getColorFromAttr
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
            updateNestedFragment(viewModel.state.value.taskDraft)
        }
        binding.createEventButton.setOnClickListener {
            updateNestedFragment(viewModel.state.value.eventDraft)
        }
        binding.createBirthdayButton.setOnClickListener {
            updateNestedFragment(viewModel.state.value.birthdayDraft)
        }
        binding.createEditText.doAfterTextChanged {
            viewModel.onIntent(CreateIntent.UpdateTitle(it.toString()))
        }
        binding.createBackButton.setOnClickListener {
            sharedViewModel.popBackStack()
        }
        binding.createSaveButton.setOnClickListener {
            val result = viewModel.saveItemResult()

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

    private fun doInactive(button: Button) {
        button.isEnabled = false
        button.backgroundTintList =
            ColorStateList.valueOf(
                requireContext().getColorFromAttr(
                    com.google.android.material.R.attr.colorSurfaceVariant
                )
            )
        button.foregroundTintList = ColorStateList.valueOf(
            requireContext().getColorFromAttr(
                com.google.android.material.R.attr.colorOnSurfaceVariant
            )
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
        if (itemId.isNotEmpty() && createType != "TASK") doInactive(binding.createTaskButton)
        if (itemId.isNotEmpty() && createType != "EVENT") doInactive(binding.createEventButton)
        if (itemId.isNotEmpty() && createType != "BIRTHDAY") doInactive(binding.createBirthdayButton)
    }

    private fun updateUi(state: CreateState) {
        if (binding.createEditText.text.toString() != state.title) {
            binding.createEditText.setText(state.title)
        }
    }
}