package com.panelpass.domain.billing

import kotlinx.serialization.Serializable

@Serializable
sealed class SubscriptionState {
    @Serializable
    data object Unknown : SubscriptionState()

    @Serializable
    data object NotSubscribed : SubscriptionState()

    @Serializable
    data class Subscribed(
        val productId: String,
        val expiresAtMillis: Long?,
    ) : SubscriptionState()
}
