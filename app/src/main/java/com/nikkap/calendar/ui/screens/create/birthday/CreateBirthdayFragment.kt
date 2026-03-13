package com.nikkap.calendar.ui.screens.create.birthday

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.nikkap.calendar.R
import com.nikkap.calendar.databinding.CreateBirthdayFragmentBinding
import com.nikkap.calendar.ui.screens.create.CreateBirthdayIntent
import com.nikkap.calendar.ui.screens.create.CreateState
import com.nikkap.calendar.ui.screens.create.CreateViewModel
import com.nikkap.calendar.ui.setupSetColorRecyclerView
import com.nikkap.calendar.ui.showDatePicker
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
            showDatePicker(
                onClick = { viewModel.onBirthdayIntent(CreateBirthdayIntent.UpdateDate(it)) },
                fragmentManager = parentFragmentManager
            )
        }
    }

    private fun updateUi(state: CreateState) {
        val birthday = state.birthdayDraft

//        if (binding.createBirthdayDateButton.text != birthday.date)
    }
}