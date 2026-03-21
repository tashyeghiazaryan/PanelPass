package com.panelpass.domain.auth

class SignInUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Result<User> = authRepository.signIn()
}
