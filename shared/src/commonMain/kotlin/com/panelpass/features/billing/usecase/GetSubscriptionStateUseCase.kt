package com.panelpass.features.billing.usecase

import com.panelpass.features.billing.domain.BillingRepository
import com.panelpass.features.billing.domain.SubscriptionState

public class GetSubscriptionStateUseCase(
    private val billingRepository: BillingRepository,
) {
    public suspend operator fun invoke(): SubscriptionState = billingRepository.getSubscriptionState()
}
