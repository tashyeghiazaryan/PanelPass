package com.panelpass.platform.ios

import com.panelpass.domain.auth.AuthRepository
import com.panelpass.domain.auth.User
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

public class IosAuthRepository(
    private val apple: AppleSignInProvider,
) : AuthRepository {

    override suspend fun signIn(): Result<User> = suspendCancellableCoroutine { cont ->
        apple.startSignIn(
            onSuccess = { cont.resume(Result.success(it)) },
            onFailure = { cont.resume(Result.failure(it)) },
        )
    }

    override suspend fun signOut() {
        apple.signOut()
    }

    override suspend fun getCurrentUser(): User? = apple.getCurrentUser()

    override fun isSignInAvailable(): Boolean = apple.isAvailable()
}
