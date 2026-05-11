package com.nikkap.calendar.ui.screens.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ListFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = ListAdapter { id, type -> sharedViewModel.onEditListItemClicked(id, type) }

        setupRecyclerView(adapter)
        setupListeners()
        observeState(adapter)
    }

    private fun setupRecyclerView(adapter: ListAdapter) {
        val dividerItemDecoration = DividerItemDecoration(requireContext(), RecyclerView.VERTICAL)

        ContextCompat.getDrawable(requireContext(), R.drawable.list_divider)?.let {
            dividerItemDecoration.setDrawable(it)
        }
        binding.listRV.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(requireView().context)
            addItemDecoration(dividerItemDecoration)
        }
    }

    private fun setupListeners() {
        binding.listSwipeRef.setOnRefreshListener {
            viewModel.refreshData(requireContext())
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
                }
            }
        }
    }
}


