package com.panelpass.domain.auth

/**
 * Platform-agnostic authentication contract.
 * iOS implements with Apple Sign In; Android with Google Sign In.
 */
interface AuthRepository {
    /**
     * Performs native sign-in. On iOS: Sign in with Apple. On Android: Sign in with Google.
     * @return [User] with id, email, and name on success
     */
    suspend fun signIn(): Result<User>

    /**
     * Signs out the current user.
     */
    suspend fun signOut()

    /**
     * Returns the currently signed-in user, or null.
     */
    suspend fun getCurrentUser(): User?

    /**
     * Whether the platform supports sign-in (e.g. Google Play Services / Apple available).
     */
    fun isSignInAvailable(): Boolean
}
