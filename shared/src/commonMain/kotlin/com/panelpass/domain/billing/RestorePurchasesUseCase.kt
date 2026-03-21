package com.panelpass.domain.billing

class RestorePurchasesUseCase(
    private val billingRepository: BillingRepository,
) {
    suspend operator fun invoke(): Result<Unit> = billingRepository.restorePurchases()
}
