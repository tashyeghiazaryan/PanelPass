package com.panelpass.platform.ios

import com.panelpass.domain.billing.BillingRepository
import com.panelpass.domain.billing.PurchaseResult
import com.panelpass.domain.billing.SubscriptionProduct
import com.panelpass.domain.billing.SubscriptionState
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

public class IosBillingRepository(
    private val store: StoreKitBillingProvider,
) : BillingRepository {

    override suspend fun getProducts(): Result<List<SubscriptionProduct>> =
        suspendCancellableCoroutine { cont ->
            store.fetchProducts { list, err ->
                if (err != null) {
                    cont.resume(Result.failure(err))
                } else {
                    cont.resume(Result.success(list))
                }
            }
        }

    override suspend fun purchase(productId: String): Result<PurchaseResult> =
        suspendCancellableCoroutine { cont ->
            store.purchase(productId) { result, err ->
                if (err != null) {
                    cont.resume(Result.failure(err))
                } else {
                    cont.resume(Result.success(result))
                }
            }
        }

    override suspend fun restorePurchases(): Result<Unit> =
        suspendCancellableCoroutine { cont ->
            store.restorePurchases { err ->
                if (err != null) {
                    cont.resume(Result.failure(err))
                } else {
                    cont.resume(Result.success(Unit))
                }
            }
        }

    override suspend fun getSubscriptionState(): SubscriptionState =
        suspendCancellableCoroutine { cont ->
            store.fetchSubscriptionState { state, err ->
                cont.resume(if (err != null) SubscriptionState.Unknown else state)
            }
        }

    override fun isBillingAvailable(): Boolean = store.isAvailable()
}
