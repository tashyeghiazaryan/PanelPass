package com.panelpass.features.auth.usecase

import com.panelpass.features.auth.domain.AuthRepository
import com.panelpass.features.auth.domain.User

public class GetCurrentUserUseCase(
    private val authRepository: AuthRepository,
) {
    public suspend operator fun invoke(): User? = authRepository.getCurrentUser()
}
