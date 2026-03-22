package com.panelpass.features.billing.usecase

import com.panelpass.features.billing.domain.BillingRepository

public class RestorePurchasesUseCase(
    private val billingRepository: BillingRepository,
) {
    public suspend operator fun invoke(): Result<Unit> = billingRepository.restorePurchases()
}
