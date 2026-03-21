package com.panelpass.domain.billing

class GetProductsUseCase(
    private val billingRepository: BillingRepository,
) {
    suspend operator fun invoke(): Result<List<SubscriptionProduct>> =
        billingRepository.getProducts()
}
