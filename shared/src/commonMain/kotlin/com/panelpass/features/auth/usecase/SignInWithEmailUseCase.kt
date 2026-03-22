package com.panelpass.features.auth.usecase

import com.panelpass.features.auth.domain.AuthRepository
import com.panelpass.features.auth.domain.User

public class SignInWithEmailUseCase(
    private val authRepository: AuthRepository,
) {
    public suspend operator fun invoke(email: String, password: String): Result<User> =
        authRepository.signInWithEmail(email, password)
}
