package com.nikkap.calendar.ui.screens.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.nikkap.calendar.R
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModel()
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        splashScreen.setKeepOnScreenCondition {
            viewModel.navigationEvent is NavEvent.ToList
        }
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController
        viewModel.checkAuthAndNavigate()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvent.collect { event ->
                    when (event) {
                        is NavEvent.ToList -> navController.navigate(R.id.listFragment)
                        is NavEvent.ToAuth -> navController.navigate(R.id.authFragment)
                        is NavEvent.ToCreate -> {
                            val bundle = bundleOf("type" to event.type, "id" to event.itemId)
                            navController.navigate(R.id.createFragment, bundle)
                        }

                        is NavEvent.Start -> {}
                    }
                }
            }
        }
    }
}