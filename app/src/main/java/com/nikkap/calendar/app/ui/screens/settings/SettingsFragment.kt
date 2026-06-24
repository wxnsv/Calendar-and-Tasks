package com.nikkap.calendar.app.ui.screens.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nikkap.calendar.app.R
import com.nikkap.calendar.app.databinding.SettingsFragmentBinding
import com.nikkap.calendar.app.ui.screens.main.MainViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class SettingsFragment : Fragment(R.layout.settings_fragment) {
    private var _binding: SettingsFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModel()
    private val sharedViewModel: MainViewModel by activityViewModels()


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SettingsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        setupListeners()
        observeState()
    }

    private fun setupListeners() {
        binding.settingsAboutButton.setOnClickListener {
            sharedViewModel.toAboutScreen()
        }
        binding.settingsToolbar.setNavigationOnClickListener {
            sharedViewModel.popBackStack()
        }
        binding.settingsThemeButton.setOnClickListener {
            showSelectionDialog(
                title = "Set theme",
                listeners = listOf(
                    Pair(
                        "System",
                        { viewModel.onIntent(SettingsIntent.UpdateTheme(isLight = null)) }),
                    Pair(
                        "Light",
                        { viewModel.onIntent(SettingsIntent.UpdateTheme(isLight = true)) }),
                    Pair(
                        "Dark",
                        { viewModel.onIntent(SettingsIntent.UpdateTheme(isLight = false)) }
                    )),
                isFirst = viewModel.state.value.isLightTheme)
        }
        binding.settingsStartScreenButton.setOnClickListener {
            showSelectionDialog(
                title = "Set start screen",
                listeners = listOf(
                    Pair(
                        "Last opened",
                        { viewModel.onIntent(SettingsIntent.UpdateStartScreen(isList = null)) }
                    ),
                    Pair(
                        "List",
                        { viewModel.onIntent(SettingsIntent.UpdateStartScreen(isList = true)) }
                    ),
                    Pair(
                        "Split",
                        { viewModel.onIntent(SettingsIntent.UpdateStartScreen(isList = false)) }
                    )
                ),
                isFirst = viewModel.state.value.isListStartScreen
            )
        }
        binding.settingsFirstWeekDayButton.setOnClickListener {
            showSelectionDialog(
                title = "Set first day of week",
                listeners = listOf(
                    Pair(
                        "System",
                        { viewModel.onIntent(SettingsIntent.UpdateFirstDayOfWeek(isMonday = null)) }
                    ),
                    Pair(
                        "Monday",
                        { viewModel.onIntent(SettingsIntent.UpdateFirstDayOfWeek(isMonday = true)) }
                    ),
                    Pair(
                        "Sunday",
                        { viewModel.onIntent(SettingsIntent.UpdateFirstDayOfWeek(isMonday = false)) }
                    ),
                ),
                isFirst = viewModel.state.value.isMondayFirstDayOfWeek
            )
        }
        binding.settingsLogoutButton.setOnClickListener {
            sharedViewModel.logout()
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

    private fun updateUi(state: SettingsState) {
        if (binding.settingsUserTV.text != state.userName) {
            binding.settingsUserTV.text = state.userName
        }
        if (binding.settingsEmailTV.text != state.userEmail) {
            binding.settingsEmailTV.text = state.userEmail
        }
        val startScreen = when (state.isListStartScreen) {
            true -> "List"
            false -> "Split"
            null -> "Last opened"
        }
        if (binding.settingsStartScreenTV.text != startScreen) {
            binding.settingsStartScreenTV.text = startScreen
        }
        val theme = when (state.isLightTheme) {
            true -> "Light"
            false -> "Dark"
            null -> "System"
        }
        if (binding.settingsThemeTV.text != theme) {
            binding.settingsThemeTV.text = theme
        }
        val firstDayOfWeek = when (state.isMondayFirstDayOfWeek) {
            true -> "Monday"
            false -> "Sunday"
            null -> "System"
        }
        if (binding.settingsFirstWeekDayTV.text != firstDayOfWeek) {
            binding.settingsFirstWeekDayTV.text = firstDayOfWeek
        }
        if (!state.userImagePath.isNullOrBlank()) setupPhoto(state)
    }

    private fun setupPhoto(state: SettingsState) {
        val photo = File(state.userImagePath ?: "")
        if (photo.exists()) {
            binding.settingsUserImage.load(photo) {
                crossfade(true)
                placeholder(R.drawable.avatar_placeholder)
                error(R.drawable.avatar_placeholder)
                listener(
                    onSuccess = { _, _ ->
                        startPostponedEnterTransition()
                    },
                    onError = { _, _ ->
                        startPostponedEnterTransition()
                    })
            }
        }
    }

    private fun showSelectionDialog(
        title: String,
        listeners: List<Pair<String, () -> Unit>>,
        isFirst: Boolean?
    ) {

        var checkedItem = when (isFirst) {
            null -> 0
            true -> 1
            false -> 2
        }
        val options = listeners.map { it.first }.toTypedArray()
        val onClickOptions = listeners.map { it.second }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setSingleChoiceItems(options, checkedItem) { _, which ->
                checkedItem = which
            }

            .setPositiveButton("Apply") { dialog, _ ->
                onClickOptions[checkedItem]()
                dialog.dismiss()
            }

            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
