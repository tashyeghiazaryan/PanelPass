package com.panelpass.platform.ios

import com.panelpass.features.billing.domain.PurchaseResult
import com.panelpass.features.billing.domain.SubscriptionProduct
import com.panelpass.features.billing.domain.SubscriptionState

/**
 * Swift-friendly API for StoreKit (no suspend / Result in the contract).
 */
public interface StoreKitBillingProvider {
    public fun fetchProducts(onResult: (List<SubscriptionProduct>, Throwable?) -> Unit)

    public fun purchase(productId: String, onResult: (PurchaseResult, Throwable?) -> Unit)

    /** Pass null [Throwable] on success. */
    public fun restorePurchases(onResult: (Throwable?) -> Unit)

    public fun fetchSubscriptionState(onResult: (SubscriptionState, Throwable?) -> Unit)

    public fun isAvailable(): Boolean
}
