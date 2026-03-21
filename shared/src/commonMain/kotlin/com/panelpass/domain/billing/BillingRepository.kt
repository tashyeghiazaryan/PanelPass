package com.panelpass.domain.billing

/**
 * Platform-agnostic in-app subscription contract.
 * iOS: StoreKit 2. Android: Google Play Billing.
 */
interface BillingRepository {
    /**
     * Fetches available subscription products (e.g. monthly premium).
     */
    suspend fun getProducts(): Result<List<SubscriptionProduct>>

    /**
     * Starts purchase flow for the given product.
     */
    suspend fun purchase(productId: String): Result<PurchaseResult>

    /**
     * Restores previous purchases (e.g. after reinstall).
     */
    suspend fun restorePurchases(): Result<Unit>

    /**
     * Current subscription state for the user.
     */
    suspend fun getSubscriptionState(): SubscriptionState

    /**
     * Whether in-app billing is available on this device.
     */
    fun isBillingAvailable(): Boolean
}

@kotlinx.serialization.Serializable
data class SubscriptionProduct(
    val id: String,
    val title: String,
    val description: String,
    val price: String,
    val priceAmountMicros: Long,
)

@kotlinx.serialization.Serializable
sealed class PurchaseResult {
    @kotlinx.serialization.Serializable
    data object Success : PurchaseResult()

    @kotlinx.serialization.Serializable
    data object Cancelled : PurchaseResult()

    @kotlinx.serialization.Serializable
    data class Error(val message: String) : PurchaseResult()
}
