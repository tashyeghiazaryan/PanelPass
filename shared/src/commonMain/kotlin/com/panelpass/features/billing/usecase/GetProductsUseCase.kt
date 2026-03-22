package com.panelpass.features.billing.usecase

import com.panelpass.features.billing.domain.BillingRepository
import com.panelpass.features.billing.domain.SubscriptionProduct

public class GetProductsUseCase(
    private val billingRepository: BillingRepository,
) {
    public suspend operator fun invoke(): Result<List<SubscriptionProduct>> =
        billingRepository.getProducts()
}
