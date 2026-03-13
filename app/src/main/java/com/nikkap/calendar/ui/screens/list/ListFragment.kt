package com.nikkap.calendar.ui.screens.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nikkap.calendar.R
import com.nikkap.calendar.databinding.ListFragmentBinding
import com.nikkap.calendar.ui.screens.main.MainViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ListFragment : Fragment(R.layout.list_fragment) {
    private var _binding: ListFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ListViewModel by viewModel()
    private val sharedViewModel: MainViewModel by activityViewModels()

//    private var adapter: ListAdapter? = null

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private var lastState: Boolean? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ListFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = ListAdapter { id, type -> sharedViewModel.onListItemClicked(id, type) }
        val dividerItemDecoration = DividerItemDecoration(requireContext(), RecyclerView.VERTICAL)

        ContextCompat.getDrawable(requireContext(), R.drawable.list_divider)?.let {
            dividerItemDecoration.setDrawable(it)
        }
        binding.listRV.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(requireView().context)
            addItemDecoration(dividerItemDecoration)
        }
        setupRecyclerView()
//        lifecycleScope.launch { viewModel.syncAll() }
        setupListeners()
        observeState(adapter)
    }

    private fun setupRecyclerView() {

    }

    private fun setupListeners() {
        binding.listSwipeRef.setOnRefreshListener {
            viewModel.refreshData()
        }

        binding.scrim.setOnClickListener { viewModel.toggleMenu() }

        binding.createTask.setOnClickListener {
            viewModel.toggleMenu()
            sharedViewModel.onTaskClicked()
        }
        binding.createEvent.setOnClickListener {
            viewModel.toggleMenu()
            sharedViewModel.onEventClicked()
        }
        binding.createBirthday.setOnClickListener {
            viewModel.toggleMenu()
            sharedViewModel.onBirthdayClicked()
        }
        binding.createItemButton.setOnClickListener {
            viewModel.toggleMenu()
        }
    }

    private fun observeState(adapter: ListAdapter) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.listSwipeRef.isRefreshing = state.isLoading
                    adapter.updateList(state.items) // fun updateUi
                    state.errorMessage?.let { message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                    }
                    renderMenu(state.isMenuExpanded)
                }
            }
        }
    }

    private fun renderMenu(isExpanded: Boolean) {
        if (lastState == isExpanded) return

        if (isExpanded) {
            binding.scrim.visibility = View.VISIBLE
            binding.scrim.animate().alpha(1f).setDuration(300).start()

            expandGroup(binding.createTask, 0L)
            expandGroup(binding.createEvent, 50L)
            expandGroup(binding.createBirthday, 100L)
        } else {
            _binding?.scrim?.animate()?.alpha(0f)?.setDuration(300)?.withEndAction {
                _binding?.scrim?.visibility = View.GONE
            }?.start()

            collapseGroup(binding.createTask)
            collapseGroup(binding.createBirthday)
            collapseGroup(binding.createEvent)

        }
    }

    private fun expandGroup(view: View, delay: Long) {
        view.visibility = View.VISIBLE
        view.bringToFront()
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(delay)
            .setInterpolator(OvershootInterpolator())
            .setDuration(300)
            .start()
    }

    private fun collapseGroup(view: View) {
        view.animate()
            .alpha(0f)
            .translationY(100f)
            .setDuration(300)
            .withEndAction { view.visibility = View.GONE }
            .start()
    }
}