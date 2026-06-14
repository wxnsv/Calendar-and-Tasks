package com.nikkap.calendar.app.ui.screens.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import com.nikkap.calendar.app.R
import com.nikkap.calendar.app.ui.navigation.NavEvent
import com.nikkap.calendar.app.ui.navigation.NavigationTarget
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModel()
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            viewModel.state.first { it.isPrefsLoaded }
            val userState = viewModel.state.value.userState
            val currentTheme = if (userState.isSystemTheme) null else userState.isLightTheme
            val targetMode = when (currentTheme) {
                true -> AppCompatDelegate.MODE_NIGHT_NO
                false -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            if (AppCompatDelegate.getDefaultNightMode() != targetMode) {
                AppCompatDelegate.setDefaultNightMode(targetMode)
            }
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        observeNavigation()
        viewModel.checkAuthAndNavigate()
        observeState()
        installSplashScreen().setKeepOnScreenCondition {
            !viewModel.state.value.isScreensReady
        }
    }

    private fun observeNavigation() {

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvent.collect { event ->

                    when (event) {
                        is NavEvent.NavigateTo -> {
                            val options = navOptions {

                                if (event.route is NavigationTarget.Create && event.route.itemId.isNotBlank()) {
                                    anim {
                                        enter = R.anim.slide_out_bottom
                                        exit = R.anim.slide_in_top
                                        popEnter = R.anim.slide_in_bottom
                                        popExit = R.anim.slide_out_top
                                    }
                                } else anim {
                                    enter = R.anim.slide_in_right
                                    exit = R.anim.slide_out_left
                                    popEnter = R.anim.slide_in_left
                                    popExit = R.anim.slide_out_right
                                }

                                if (event.route is NavigationTarget.Pager) {
                                    popUpTo(R.id.authFragment) {
                                        inclusive = true
                                    }
                                }

                                launchSingleTop = true
                            }
                            val resId = when (event.route) {
                                is NavigationTarget.Auth -> R.id.authFragment
                                is NavigationTarget.Create -> R.id.createFragment
                                is NavigationTarget.Pager -> R.id.mainPagerFragment
                                is NavigationTarget.About -> R.id.action_settingsFragment_to_aboutFragment
                                is NavigationTarget.Settings -> R.id.action_mainPager_to_settingsFragment
                            }
                            var bundle: Bundle? = null
                            if (event.route is NavigationTarget.Create) {
                                bundle = Bundle().apply {
                                    putString("type", event.route.type)
                                    putString("id", event.route.itemId)
                                }
                            }
                            navController.navigate(resId, bundle, options)

                        }

                        is NavEvent.SetRoot -> {
                            val resId = when (event.route) {
                                is NavigationTarget.Auth -> R.id.authFragment
                                else -> R.id.mainPagerFragment
                            }

                            navGraph.setStartDestination(resId)
                            navController.graph = navGraph
                        }

                        is NavEvent.PopBack -> navController.popBackStack()
                    }
                }
            }
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect {
                    val userState = viewModel.state.value.userState
                    updateTheme(
                        if (userState.isSystemTheme) null
                        else userState.isLightTheme
                    )
                }
            }
        }
    }

    private fun updateTheme(isLight: Boolean?) {
        val targetMode = when (isLight) {
            true -> AppCompatDelegate.MODE_NIGHT_NO
            false -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        if (AppCompatDelegate.getDefaultNightMode() != targetMode) {
            AppCompatDelegate.setDefaultNightMode(targetMode)
        }
    }
}