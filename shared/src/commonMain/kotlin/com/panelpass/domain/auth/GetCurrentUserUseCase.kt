package com.panelpass.domain.auth

class GetCurrentUserUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): User? = authRepository.getCurrentUser()
}
