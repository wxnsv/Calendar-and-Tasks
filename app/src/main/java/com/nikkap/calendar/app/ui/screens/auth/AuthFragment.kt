package com.nikkap.calendar.app.ui.screens.auth

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.nikkap.calendar.app.core.auth.AuthorizationManager
import com.nikkap.calendar.app.ui.screens.main.MainViewModel
import com.nikkap.calendar.app.ui.theme.CalendarTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class AuthFragment : Fragment() {
    private val viewModel: AuthViewModel by viewModel()
    private val authorizationManager by lazy { AuthorizationManager(requireContext()) }
    private val sharedViewModel: MainViewModel by activityViewModels()
    private val authLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.handleAuthResult(result.data)
        }
//        else TODO
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ShowLoginScreen()
            }
        }
    }

    @Composable
    private fun ShowLoginScreen() {
        CalendarTheme {
            LoginScreen(
                onLoginClick = {
                    viewModel.startAuth {
                        authorizationManager.getAuthIntent { intentSender ->
                            viewModel.onAuthIntentReady(intentSender, authLauncher)
                        }
                        sharedViewModel.authorizeSuccess(viewModel.photoUri.value)

                    }
                }
            )
            sharedViewModel.setIsAuthReady()
        }
    }

    @Composable
    private fun LoginScreen(onLoginClick: () -> Unit) {
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
}