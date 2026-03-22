package com.panelpass.features.auth.domain

import kotlinx.serialization.Serializable

/**
 * Authenticated user data returned from platform sign-in.
 */
@Serializable
public data class User(
    val id: String,
    val email: String?,
    val name: String?,
)
