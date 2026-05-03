package com.nikkap.calendar.ui.screens.create.fragments

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
                viewModel.onIntent(CreateIntent.UpdateShowFragment(Task()))
                CreateTaskFragment()
            }

            is Event -> {
                binding.createEditText.hint = "Add title"
                viewModel.onIntent(CreateIntent.UpdateShowFragment(Event()))
                CreateEventFragment()
            }

            is Birthday -> {
                binding.createEditText.hint = "Add name"
                viewModel.onIntent(CreateIntent.UpdateShowFragment(Birthday()))
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
            viewModel.onIntent(CreateIntent.UpdateShowFragment(Task()))
        }
        binding.createEventButton.setOnClickListener {
            updateNestedFragment(viewModel.state.value.eventDraft)
            viewModel.onIntent(CreateIntent.UpdateShowFragment(Event()))
        }
        binding.createBirthdayButton.setOnClickListener {
            updateNestedFragment(viewModel.state.value.birthdayDraft)
            viewModel.onIntent(CreateIntent.UpdateShowFragment(Birthday()))
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

    private fun initial() {
        val createType = args.type
        val itemId = args.id
        if (itemId != "") {
            viewModel.onIntent(CreateIntent.UpdateItem(type = createType, id = itemId))
        }
        when (createType) {
            "TASK" -> updateNestedFragment(viewModel.state.value.taskDraft)
            "EVENT" -> updateNestedFragment(viewModel.state.value.eventDraft)
            "BIRTHDAY" -> updateNestedFragment(viewModel.state.value.birthdayDraft)
        }
    }

    private fun updateUi(state: CreateState) {
        val currentFragment = childFragmentManager.findFragmentById(R.id.create_fcv)
        val currentEntry = when (currentFragment) {
            is CreateTaskFragment -> state.taskDraft
            is CreateEventFragment -> state.eventDraft
            else -> state.birthdayDraft
        }
        if (binding.createEditText.text.toString() != state.title) {
            binding.createEditText.setText(state.title)
        }
        if (currentEntry != state.activeType) {
            updateNestedFragment(currentEntry)
        }
    }
}