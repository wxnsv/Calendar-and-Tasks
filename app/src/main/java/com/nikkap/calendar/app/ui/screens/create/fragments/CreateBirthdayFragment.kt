package com.nikkap.calendar.app.ui.screens.create.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearSnapHelper
import com.nikkap.calendar.app.R
import com.nikkap.calendar.app.databinding.CreateBirthdayFragmentBinding
import com.nikkap.calendar.app.ui.screens.create.CreateBirthdayIntent
import com.nikkap.calendar.app.ui.screens.create.CreateState
import com.nikkap.calendar.app.ui.screens.create.CreateViewModel
import com.nikkap.calendar.app.ui.utils.setupSetColorRecyclerView
import com.nikkap.calendar.app.ui.utils.showDatePicker
import com.nikkap.calendar.core.utils.CalendarColors
import com.nikkap.calendar.core.utils.toShortUiDate
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreateBirthdayFragment : Fragment(R.layout.create_birthday_fragment) {
    private var _binding: CreateBirthdayFragmentBinding? = null

    private val binding get() = _binding!!

    private val viewModel: CreateViewModel by viewModel(
        ownerProducer = { requireParentFragment() }
    )

    private val snapHelper = LinearSnapHelper()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSetColorRecyclerView(
            recyclerView = binding.createBirthdaySetColorRv,
            onSet = { viewModel.onBirthdayIntent(CreateBirthdayIntent.UpdateColor(it)) },
            context = requireContext(),
            resources = resources,
            snapHelper = snapHelper,
            initialColorId = CalendarColors.getBirthdayColor(
                viewModel.state.value.birthdayDraft.colorId
            ).id
        )
        setupListeners()
        observeState()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CreateBirthdayFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

    private fun setupListeners() {
        binding.createBirthdayDateButton.setOnClickListener {
            val state = viewModel.state.value
            showDatePicker(
                onClick = { viewModel.onBirthdayIntent(CreateBirthdayIntent.UpdateDate(it)) },
                fragmentManager = parentFragmentManager,
                state.birthdayDate
            )
        }
    }

    private fun updateUi(state: CreateState) {
        if (binding.createBirthdayDateButton.text != state.birthdayDate.toShortUiDate()) {
            binding.createBirthdayDateButton.text = state.birthdayDate.toShortUiDate()
        }
    }
}