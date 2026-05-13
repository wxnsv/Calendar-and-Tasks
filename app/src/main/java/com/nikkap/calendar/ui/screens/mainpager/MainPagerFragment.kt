package com.nikkap.calendar.ui.screens.mainpager

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButtonToggleGroup
import com.nikkap.calendar.R
import com.nikkap.calendar.databinding.MainPagerFragmentBinding
import com.nikkap.calendar.ui.screens.main.MainViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.abs

class MainPagerFragment : Fragment(R.layout.main_pager_fragment) {

    private var _binding: MainPagerFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainPagerViewModel by viewModel()
    private val sharedViewModel: MainViewModel by activityViewModels()
    private lateinit var viewPager: ViewPager2

    private var lastState: Boolean? = null

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainPagerFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPager(view)
        observeState()
        observeMainState()
        setupListeners()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    renderMenu(state.isMenuExpanded)
                }
            }
        }
    }

    private fun observeMainState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.state.collect { state ->
                    if (state.isLoading && !state.userState.isFirstLaunch) {
                        val pos = if (state.userState.isListScreenLast) 0 else 1
                        async { viewPager.setCurrentItem(pos, false) }.await()

                        setupTransition()
                    }
                }
            }
        }
    }

    private fun setupTransition() {
        viewPager.post {
            val transformer = CompositePageTransformer().apply {
                addTransformer(MarginPageTransformer(40))
                addTransformer { page, position ->
                    page.alpha = 0.5f + (1 - abs(position)) * 0.5f
                }
            }
            viewPager.setPageTransformer(transformer)

            viewPager.requestTransform()
        }
    }

    private fun setupPager(view: View) {
        viewPager = binding.mainViewPager
        viewPager.adapter = MainPagerAdapter(this)
        viewPager.offscreenPageLimit = 1
        val toggleGroup = view.findViewById<MaterialButtonToggleGroup>(R.id.mainToggleGroup)

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val pos = if (checkedId == R.id.btnView) 0 else 1
                if (viewPager.currentItem != pos) {
                    viewPager.setCurrentItem(pos, true)
                }
                viewModel.switchScreen(pos)
            }
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val buttonId = if (position == 0) R.id.btnView else R.id.btnCompose
                if (toggleGroup.checkedButtonId != buttonId) {
                    toggleGroup.check(buttonId)
                }
            }
        })


    }

    private fun setupListeners() {
        binding.scrim.setOnClickListener { viewModel.toggleMenu() }

        binding.createTask.setOnClickListener {
            sharedViewModel.toCreateTask()
            viewModel.toggleMenu()
        }
        binding.createEvent.setOnClickListener {
            sharedViewModel.toCreateEvent()
            viewModel.toggleMenu()
        }
        binding.createBirthday.setOnClickListener {
            sharedViewModel.toCreateBirthday()
            viewModel.toggleMenu()
        }
        binding.createItemButton.setOnClickListener {
            viewModel.toggleMenu()
        }
    }

    private fun renderMenu(isExpanded: Boolean) {
        if (lastState == isExpanded) return

        if (isExpanded) {
            binding.createItemButton.animate()
                .rotation(90f)
                .setDuration(50)
                .withEndAction {
                    binding.createItemButton.setIconResource(R.drawable.close)
                    binding.createItemButton.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.red)
                    )
                    binding.createItemButton.animate()
                        .rotation(180f)
                        .setDuration(50)
                        .start()
                }
                .start()

            binding.scrim.visibility = View.VISIBLE
            binding.scrim.animate().alpha(1f).setDuration(300).start()

            expandGroup(binding.createTask, 0L)
            expandGroup(binding.createEvent, 50L)
            expandGroup(binding.createBirthday, 100L)
        } else {
            binding.createItemButton.animate()
                .rotation(90f)
                .setDuration(50)
                .withEndAction {
                    _binding?.createItemButton?.setIconResource(R.drawable.add)
                    _binding?.createItemButton?.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.purple_700)
                    )
                    _binding?.createItemButton?.animate()?.apply {
                        rotation(180f)
                        alpha(1f)
                        setDuration(50)
                        start()
                    }

                    _binding?.scrim?.animate()?.apply {
                        alpha(0f)
                        setDuration(300)
                        withEndAction {
                            _binding?.scrim?.visibility = View.GONE
                        }.start()
                    }

                    collapseGroup(_binding?.createTask)
                    collapseGroup(_binding?.createBirthday)
                    collapseGroup(_binding?.createEvent)

                }
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

    private fun collapseGroup(view: View?) {
        view?.animate()?.apply {
            alpha(0f)
            translationY(100f)
            setDuration(300)
            withEndAction { view.visibility = View.GONE }
            start()
        }
    }
}