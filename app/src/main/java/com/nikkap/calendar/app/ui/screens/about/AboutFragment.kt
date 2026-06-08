package com.nikkap.calendar.app.ui.screens.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nikkap.calendar.app.R
import com.nikkap.calendar.app.databinding.AboutFragmentBinding
import com.nikkap.calendar.app.ui.screens.main.MainViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AboutFragment : Fragment(R.layout.about_fragment) {
    private var _binding: AboutFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AboutViewModel by viewModel()
    private val sharedViewModel: MainViewModel by activityViewModels()


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AboutFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeState()
    }

    private fun setupListeners() {
        binding.aboutToolbar.setNavigationOnClickListener {
            sharedViewModel.popBackStack()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.state.collect { state ->
//
//                }
            }
        }
    }
}