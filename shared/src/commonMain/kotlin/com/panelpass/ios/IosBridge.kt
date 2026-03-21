package com.panelpass.ios

import com.panelpass.AppContext
import com.panelpass.domain.auth.User
import com.panelpass.domain.billing.PurchaseResult
import com.panelpass.domain.billing.SubscriptionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Bridge for iOS (Swift) to call shared use cases with callbacks.
 */
object IosBridge {

    private val scope = CoroutineScope(Dispatchers.Default)

    fun getCurrentUser(callback: (User?) -> Unit) {
        scope.launch {
            val user = AppContext.getCurrentUserUseCase()
            callback(user)
        }
    }

    fun getSubscriptionState(callback: (SubscriptionState) -> Unit) {
        scope.launch {
            val state = AppContext.getSubscriptionStateUseCase()
            callback(state)
        }
    }

    fun signIn(callback: (User?, Throwable?) -> Unit) {
        scope.launch {
            val result = AppContext.signInUseCase()
            result.fold(
                onSuccess = { callback(it, null) },
                onFailure = { callback(null, it) },
            )
        }
    }

    fun purchase(productId: String, callback: (PurchaseResult?, Throwable?) -> Unit) {
        scope.launch {
            val result = AppContext.purchaseSubscriptionUseCase(productId)
            result.fold(
                onSuccess = { callback(it, null) },
                onFailure = { callback(null, it) },
            )
        }
    }

    fun restorePurchases(callback: (Throwable?) -> Unit) {
        scope.launch {
            val result = AppContext.restorePurchasesUseCase()
            callback(result.exceptionOrNull())
        }
    }
}
