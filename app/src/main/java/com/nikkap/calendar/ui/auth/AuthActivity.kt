package com.nikkap.calendar.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.nikkap.calendar.core.auth.AuthManager
import com.nikkap.calendar.ui.main.MainActivity
import com.nikkap.calendar.ui.theme.CalendarTheme
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AuthActivity : ComponentActivity() {
    private val viewModel: AuthViewModel by viewModel()
    private val authManager by lazy { AuthManager(this) }
    private val authLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.handleAuthResult(result.data)
        } else Log.d("Auth", "${result.resultCode}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splashScreen.setKeepOnScreenCondition {
            viewModel.state.value !is AuthState.NavigateToMain
        }

        viewModel.checkAuth()

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is AuthState.Authenticated -> viewModel.checkData()
                    is AuthState.Unauthenticated -> showLoginScreen()
                    is AuthState.NavigateToMain -> navigateToMain()
                    else -> Unit
                }
            }
        }
    }

    private fun showLoginScreen() {
        setContent {
            CalendarTheme {
                LoginScreen(onLoginClick = {
                    authManager.getAuthIntent { intentSender ->
                        viewModel.onAuthIntentReady(intentSender, authLauncher)
                    }
                })
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

@Composable
fun LoginScreen(onLoginClick: () -> Unit) {
    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    onLoginClick()
                }) {
                Text("Start Authorize")
            }
        }
    }
}