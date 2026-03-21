package com.panelpass.domain.billing

class PurchaseSubscriptionUseCase(
    private val billingRepository: BillingRepository,
) {
    suspend operator fun invoke(productId: String): Result<PurchaseResult> =
        billingRepository.purchase(productId)
}
