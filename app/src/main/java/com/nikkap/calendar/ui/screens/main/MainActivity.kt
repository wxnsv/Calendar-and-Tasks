package com.nikkap.calendar.ui.screens.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.nikkap.calendar.R
import com.nikkap.calendar.ui.navigation.NavEvent
import com.nikkap.calendar.ui.navigation.NavigationTarget
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModel()
    private lateinit var navController: NavController
    var showSplash = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen().setKeepOnScreenCondition {
            showSplash
        }


        setContentView(R.layout.activity_main)
        observeNavigation()
        viewModel.checkAuthAndNavigate()
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
                            val resId = when (event.route) {
                                is NavigationTarget.Auth -> R.id.authFragment
                                is NavigationTarget.Create -> R.id.createFragment
                                is NavigationTarget.List -> R.id.listFragment
                            }

                            navController.navigate(resId) {
                                event.popUpTo?.let { popUpTo(it) { inclusive = true } }
                            }
                        }

                        is NavEvent.SetRoot -> {
                            val resId = when (event.route) {
                                is NavigationTarget.Auth -> R.id.authFragment
                                else -> R.id.listFragment
                            }

                            navGraph.setStartDestination(resId)
                            navController.graph = navGraph
                            window.decorView.post {
                                showSplash = false
                            }
                        }

                        is NavEvent.PopBack -> navController.popBackStack()
                    }
                }
            }
        }
    }
}