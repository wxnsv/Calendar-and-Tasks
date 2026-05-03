package com.nikkap.calendar.ui.screens.create.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nikkap.calendar.R
import com.nikkap.calendar.core.utils.toUiDate
import com.nikkap.calendar.databinding.CreateBirthdayFragmentBinding
import com.nikkap.calendar.ui.screens.create.CreateBirthdayIntent
import com.nikkap.calendar.ui.screens.create.CreateState
import com.nikkap.calendar.ui.screens.create.CreateViewModel
import com.nikkap.calendar.ui.setupSetColorRecyclerView
import com.nikkap.calendar.ui.showDatePicker
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreateBirthdayFragment : Fragment(R.layout.create_birthday_fragment) {
    private var _binding: CreateBirthdayFragmentBinding? = null

    private val binding get() = _binding!!

    private val viewModel: CreateViewModel by viewModel(
        ownerProducer = { requireParentFragment() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSetColorRecyclerView(
            recyclerView = binding.createBirthdaySetColorRv,
            onSet = { viewModel.onBirthdayIntent(CreateBirthdayIntent.UpdateColor(it)) },
            context = requireContext(),
            resources = resources
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
        binding.createBirthdayNotificationButton.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.create_task_event_notification_menu, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                binding.createBirthdayNotificationButton.text = item.title
                true
            }
            popup.show()
        }
        binding.createBirthdayDateButton.setOnClickListener {
            val birthday = viewModel.state.value.birthdayDraft
            showDatePicker(
                onClick = { viewModel.onBirthdayIntent(CreateBirthdayIntent.UpdateDate(it)) },
                fragmentManager = parentFragmentManager,
                birthday.date ?: System.currentTimeMillis()
            )
        }
    }

    private fun updateUi(state: CreateState) {
        val birthday = state.birthdayDraft

        if (binding.createBirthdayDateButton.text != birthday.date.toUiDate() && birthday.date != null) {
            binding.createBirthdayDateButton.text = birthday.date.toUiDate()
        }
    }
}