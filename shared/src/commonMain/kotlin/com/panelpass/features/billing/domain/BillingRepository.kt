package com.panelpass.features.billing.domain

/**
 * Platform-agnostic in-app subscription contract.
 */
public interface BillingRepository {
    public suspend fun getProducts(): Result<List<SubscriptionProduct>>

    public suspend fun purchase(productId: String): Result<PurchaseResult>

    public suspend fun restorePurchases(): Result<Unit>

    public suspend fun getSubscriptionState(): SubscriptionState

    public fun isBillingAvailable(): Boolean
}

@kotlinx.serialization.Serializable
public data class SubscriptionProduct(
    val id: String,
    val title: String,
    val description: String,
    val price: String,
    val priceAmountMicros: Long,
)

@kotlinx.serialization.Serializable
public sealed class PurchaseResult {
    @kotlinx.serialization.Serializable
    public data object Success : PurchaseResult()

    @kotlinx.serialization.Serializable
    public data object Cancelled : PurchaseResult()

    @kotlinx.serialization.Serializable
    public data class Error(val message: String) : PurchaseResult()
}
