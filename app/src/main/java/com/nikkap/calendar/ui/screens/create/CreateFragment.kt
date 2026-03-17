package com.nikkap.calendar.ui.screens.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.nikkap.calendar.R
import com.nikkap.calendar.databinding.CreateFragmentBinding
import com.nikkap.calendar.domain.model.Birthday
import com.nikkap.calendar.domain.model.CalendarEntry
import com.nikkap.calendar.domain.model.Event
import com.nikkap.calendar.domain.model.Task
import com.nikkap.calendar.ui.screens.create.birthday.CreateBirthdayFragment
import com.nikkap.calendar.ui.screens.create.event.CreateEventFragment
import com.nikkap.calendar.ui.screens.create.task.CreateTaskFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreateFragment : Fragment(R.layout.create_fragment) {
    private var _binding: CreateFragmentBinding? = null

    private val binding get() = _binding!!

    private val viewModel: CreateViewModel by viewModel()

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
    }

    private fun updateNestedFragment(item: CalendarEntry) {
        val fragment = when (item) {
            is Task -> {
                binding.createEditText.hint = "Add title"
                CreateTaskFragment()
            }

            is Event -> {
                binding.createEditText.hint = "Add title"
                CreateEventFragment()
            }

            is Birthday -> {
                binding.createEditText.hint = "Add name"
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

        }
        binding.createSaveButton.setOnClickListener {
            viewModel.onBirthdayIntent(CreateBirthdayIntent.SaveBirthday)

//            TODO
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

