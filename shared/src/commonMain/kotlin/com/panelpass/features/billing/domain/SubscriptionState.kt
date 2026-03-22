package com.panelpass.features.billing.domain

import kotlinx.serialization.Serializable

@Serializable
public sealed class SubscriptionState {
    @Serializable
    public data object Unknown : SubscriptionState()

    @Serializable
    public data object NotSubscribed : SubscriptionState()

    @Serializable
    public data class Subscribed(
        val productId: String,
        val expiresAtMillis: Long?,
    ) : SubscriptionState()
}
