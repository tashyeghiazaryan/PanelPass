package com.panelpass.domain.auth

import kotlinx.serialization.Serializable

/**
 * Authenticated user data returned from platform sign-in (Apple on iOS, Google on Android).
 */
@Serializable
data class User(
    val id: String,
    val email: String?,
    val name: String?,
)
