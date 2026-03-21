package com.panelpass.platform.ios

import com.panelpass.domain.auth.User

/**
 * Swift-friendly API for Sign in with Apple (no suspend / Result in the contract).
 */
public interface AppleSignInProvider {
    public fun startSignIn(onSuccess: (User) -> Unit, onFailure: (Throwable) -> Unit)

    public fun signOut()

    public fun getCurrentUser(): User?

    public fun isAvailable(): Boolean
}
