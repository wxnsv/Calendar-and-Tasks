package com.nikkap.calendar.app.ui.screens.auth

import android.app.Activity.RESULT_OK
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
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
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.nikkap.calendar.app.core.auth.AuthorizationManager
import com.nikkap.calendar.app.ui.screens.main.MainViewModel
import com.nikkap.calendar.app.ui.theme.CalendarTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream

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
                        sharedViewModel.authorizeSuccess(requireContext())
                        lifecycleScope.launch {
                            saveUserPhoto(requireContext(), viewModel.photoUri.value)
                        }
                    }
                }
            )
            sharedViewModel.setIsAuthReadyTrue()
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

    suspend fun saveUserPhoto(context: Context, url: String) {
        withContext(Dispatchers.IO) {
            try {

                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .allowHardware(false)
                    .build()

                val result = loader.execute(request)

                if (result is SuccessResult) {

                    val bitmap = (result.drawable as BitmapDrawable).bitmap

                    val fileName = "user_avatar.jpg"
                    val file = File(context.filesDir, fileName)

                    FileOutputStream(file).use { outStream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outStream)
                    }

                    viewModel.saveUserPhotoPath(file.absolutePath)
                }
                null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}