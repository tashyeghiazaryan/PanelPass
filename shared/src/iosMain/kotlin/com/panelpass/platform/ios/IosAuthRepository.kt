package com.panelpass.platform.ios

import com.panelpass.features.auth.domain.AuthRepository
import com.panelpass.features.auth.domain.User
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

    override suspend fun signInWithEmail(email: String, password: String): Result<User> =
        suspendCancellableCoroutine { cont ->
            apple.signInWithEmail(
                email = email,
                password = password,
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
