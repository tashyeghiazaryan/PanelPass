package com.panelpass.platform.ios

import com.panelpass.features.auth.domain.User

/**
 * Swift-friendly API for Sign in with Apple (no suspend / Result in the contract).
 */
public interface AppleSignInProvider {
    public fun startSignIn(onSuccess: (User) -> Unit, onFailure: (Throwable) -> Unit)

    /**
     * Local demo: validates email/password and persists session in UserDefaults.
     */
    public fun signInWithEmail(
        email: String,
        password: String,
        onSuccess: (User) -> Unit,
        onFailure: (Throwable) -> Unit,
    )

    public fun signOut()

    public fun getCurrentUser(): User?

    public fun isAvailable(): Boolean
}
