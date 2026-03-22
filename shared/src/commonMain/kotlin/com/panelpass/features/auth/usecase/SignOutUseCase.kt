package com.panelpass.features.auth.usecase

import com.panelpass.features.auth.domain.AuthRepository

public class SignOutUseCase(
    private val authRepository: AuthRepository,
) {
    public suspend operator fun invoke() = authRepository.signOut()
}
