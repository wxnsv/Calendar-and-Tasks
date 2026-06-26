package com.nikkap.calendar.app.ui.screens.auth

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nikkap.calendar.app.core.auth.AuthorizationManager
import com.nikkap.calendar.app.core.auth.AuthorizationManagerResult
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
        if (result.resultCode == RESULT_OK) viewModel.handleLauncherResult(result.data)
        if (result.resultCode == RESULT_CANCELED) viewModel.handleLauncherResult(result.data)
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
                    if (!viewModel.state.value.isLoading) {
                        viewModel.startAuth(
                            requireActivity()
                        ) {
                            authorizationManager.getAuthResult(
                                viewModel.state.value.requiredScopes
                            ) { managerResult ->
                                when (managerResult) {
                                    is AuthorizationManagerResult.InvalidCache -> {
                                        viewModel.invalidateCache()
                                    }

                                    is AuthorizationManagerResult.NeedResolution -> {
                                        viewModel.onAuthIntentReady(
                                            managerResult.intentSender,
                                            authLauncher
                                        )
                                    }

                                    is AuthorizationManagerResult.Success -> sharedViewModel.authorizeSuccess(
                                        viewModel.state.value.photoUri
                                    )
                                }
                            }
                        }
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
                val state by viewModel.state.collectAsStateWithLifecycle()
                LaunchedEffect(state.isAllGranted) {
                    if (state.isAllGranted) sharedViewModel.authorizeSuccess(viewModel.state.value.photoUri)
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (!state.isAllGranted && !state.isFirstLaunch) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Permissions Required",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "To enable all features, please grant the missing permissions in the next step.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
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

    @Preview
    @Composable
    private fun Preview() {
        CalendarTheme {
            LoginScreen { }
        }
    }
}