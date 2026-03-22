package com.panelpass.features.billing.usecase

import com.panelpass.features.billing.domain.BillingRepository
import com.panelpass.features.billing.domain.PurchaseResult

public class PurchaseSubscriptionUseCase(
    private val billingRepository: BillingRepository,
) {
    public suspend operator fun invoke(productId: String): Result<PurchaseResult> =
        billingRepository.purchase(productId)
}
