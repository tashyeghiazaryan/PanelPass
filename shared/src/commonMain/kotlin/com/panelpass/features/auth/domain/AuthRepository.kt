package com.panelpass.features.auth.domain

/**
 * Platform-agnostic authentication contract.
 * iOS: Sign in with Apple + optional email/password (local session until you plug a backend).
 * Android: Google Sign-In + optional email/password.
 */
public interface AuthRepository {
    /**
     * Performs native OAuth sign-in. On iOS: Sign in with Apple. On Android: Sign in with Google.
     */
    public suspend fun signIn(): Result<User>

    /**
     * Sign in with email and password (demo / replace with API).
     */
    public suspend fun signInWithEmail(email: String, password: String): Result<User>

    public suspend fun signOut()

    public suspend fun getCurrentUser(): User?

    public fun isSignInAvailable(): Boolean
}
