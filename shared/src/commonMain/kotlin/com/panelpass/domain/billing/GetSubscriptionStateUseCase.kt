package com.panelpass.domain.billing

class GetSubscriptionStateUseCase(
    private val billingRepository: BillingRepository,
) {
    suspend operator fun invoke(): SubscriptionState = billingRepository.getSubscriptionState()
}
