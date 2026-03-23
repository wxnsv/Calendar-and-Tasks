package com.nikkap.calendar.core.auth

import android.net.Uri

data class UserInfo(
    val email: String,
    val displayName: String?,
    val photoUri: Uri?,
)
