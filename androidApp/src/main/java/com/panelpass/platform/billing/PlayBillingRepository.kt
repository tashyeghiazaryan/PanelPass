package com.panelpass.platform.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.panelpass.features.billing.domain.BillingRepository
import com.panelpass.features.billing.domain.PurchaseResult
import com.panelpass.features.billing.domain.SubscriptionProduct
import com.panelpass.features.billing.domain.SubscriptionState
import com.panelpass.shell.ActivityHolder
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Android implementation of [BillingRepository] using Google Play Billing Library.
 */
internal class PlayBillingRepository(
    private val context: Context,
) : BillingRepository, PurchasesUpdatedListener {

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    private var connected = false
    private val _subscriptionState = MutableStateFlow<SubscriptionState>(SubscriptionState.Unknown)

    private val purchaseResultChannel = Channel<PurchaseResult>(Channel.RENDEZVOUS)

    private val productIds = listOf("premium_monthly")

    init {
        connectBilling()
    }

    private fun connectBilling() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                connected = result.responseCode == BillingClient.BillingResponseCode.OK
                if (connected) queryPurchases()
            }

            override fun onBillingServiceDisconnected() {
                connected = false
            }
        })
    }

    override suspend fun getProducts(): Result<List<SubscriptionProduct>> = suspendCancellableCoroutine { cont ->
        if (!connected) {
            cont.resume(Result.failure(IllegalStateException("Billing not connected")))
            return@suspendCancellableCoroutine
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                productIds.map { id ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(id)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                },
            )
            .build()
        billingClient.queryProductDetailsAsync(params) { result, productDetailsList ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList != null) {
                val products = productDetailsList.map { it.toSubscriptionProduct() }
                cont.resume(Result.success(products))
            } else {
                cont.resume(Result.failure(RuntimeException("Query products failed: ${result.debugMessage}")))
            }
        }
    }

    override suspend fun purchase(productId: String): Result<PurchaseResult> {
        val activity = ActivityHolder.get()
            ?: return Result.failure(IllegalStateException("No Activity for purchase flow"))
        return purchaseInternal(activity, productId)
    }

    private suspend fun purchaseInternal(activity: Activity, productId: String): Result<PurchaseResult> {
        if (!connected) return Result.failure(IllegalStateException("Billing not connected"))
        val productDetailsResult = suspendCancellableCoroutine<ProductDetails?> { cont ->
            val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build(),
            )
            billingClient.queryProductDetailsAsync(
                QueryProductDetailsParams.newBuilder().setProductList(productList).build(),
            ) { result, productDetailsList ->
                if (result.responseCode != BillingClient.BillingResponseCode.OK || productDetailsList.isNullOrEmpty()) {
                    cont.resume(null)
                    return@queryProductDetailsAsync
                }
                cont.resume(productDetailsList.first())
            }
        } ?: return Result.success(PurchaseResult.Error("Product not found"))
        val offerToken = productDetailsResult.subscriptionOfferDetails?.firstOrNull()?.offerToken
            ?: return Result.success(PurchaseResult.Error("No offer token"))
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetailsResult)
                        .setOfferToken(offerToken)
                        .build(),
                ),
            )
            .build()
        val billingResult = billingClient.launchBillingFlow(activity, flowParams)
        return if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            Result.success(purchaseResultChannel.receive())
        } else {
            Result.success(PurchaseResult.Error(billingResult.debugMessage))
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        acknowledgeIfNeeded(purchase)
                        purchaseResultChannel.trySend(PurchaseResult.Success)
                    }
                }
                queryPurchases()
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> purchaseResultChannel.trySend(PurchaseResult.Cancelled)
            else -> purchaseResultChannel.trySend(PurchaseResult.Error(result.debugMessage))
        }
    }

    private fun acknowledgeIfNeeded(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            billingClient.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build(),
            ) { }
        }
    }

    override suspend fun restorePurchases(): Result<Unit> {
        queryPurchases()
        return Result.success(Unit)
    }

    private fun queryPurchases() {
        if (!connected) return
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(),
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK && !purchases.isNullOrEmpty()) {
                val active = purchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                val first = purchases.firstOrNull { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                _subscriptionState.value = if (active && first != null) {
                    SubscriptionState.Subscribed(
                        productId = first.products.firstOrNull() ?: "",
                        expiresAtMillis = null,
                    )
                } else {
                    SubscriptionState.NotSubscribed
                }
            } else {
                _subscriptionState.value = SubscriptionState.NotSubscribed
            }
        }
    }

    override suspend fun getSubscriptionState(): SubscriptionState = _subscriptionState.value

    override fun isBillingAvailable(): Boolean = connected

    private fun ProductDetails.toSubscriptionProduct(): SubscriptionProduct {
        val subOffer = subscriptionOfferDetails?.firstOrNull()
        val price = subOffer?.pricingPhases?.pricingPhaseList?.firstOrNull()
        return SubscriptionProduct(
            id = productId,
            title = title,
            description = description,
            price = price?.formattedPrice ?: "",
            priceAmountMicros = price?.priceAmountMicros ?: 0L,
        )
    }
}
