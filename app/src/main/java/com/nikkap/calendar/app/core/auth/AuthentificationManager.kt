package com.nikkap.calendar.app.core.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.nikkap.calendar.app.BuildConfig.GOOGLE_CLIENT_ID

class AuthentificationManager(private val context: Context) {
    private val credentialManager: CredentialManager = CredentialManager.create(context)

    private fun getCredentialRequest(): GetCredentialRequest {
        val googleIdOption = GetSignInWithGoogleOption.Builder(GOOGLE_CLIENT_ID)
            .build()

        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    suspend fun authenticate(context: Context): UserInfo? {
        return try {
            val result = credentialManager.getCredential(context, getCredentialRequest())
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)

                UserInfo(
                    email = googleIdToken.id,
                    displayName = googleIdToken.displayName,
                    photoUri = googleIdToken.profilePictureUri.toString()
                )
            } else {
                null
            }
        } catch (_: Exception) {
            null // TODO auth exc
        }
    }

    suspend fun signOutUser() {
        runCatching {
            val credentialManager = CredentialManager.create(context)
            val request = ClearCredentialStateRequest()

            credentialManager.clearCredentialState(request)
        }.onSuccess {
            // TODO()
        }.onFailure { error ->
            // TODO()
        }
    }

}