package com.nikkap.calendar.core.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

class AuthentificationManager(private val context: Context) {
    private val credentialManager: CredentialManager = CredentialManager.create(context)

    private fun getCredentialRequest(): GetCredentialRequest {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId("")
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()

        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    suspend fun authenticate(): UserInfo? {
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
                    photoUri = googleIdToken.profilePictureUri
                )
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}