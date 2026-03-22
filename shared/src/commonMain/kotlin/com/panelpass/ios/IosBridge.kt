package com.panelpass.ios

import com.panelpass.features.auth.domain.User
import com.panelpass.features.billing.domain.PurchaseResult
import com.panelpass.features.billing.domain.SubscriptionState
import com.panelpass.shell.AppContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Bridge for iOS (Swift) to call shared use cases with callbacks.
 * Package stays `com.panelpass.ios` for stable Swift interop.
 */
public object IosBridge {

    private val scope = CoroutineScope(Dispatchers.Default)

    public fun getCurrentUser(callback: (User?) -> Unit) {
        scope.launch {
            val user = AppContext.getCurrentUserUseCase()
            callback(user)
        }
    }

    public fun getSubscriptionState(callback: (SubscriptionState) -> Unit) {
        scope.launch {
            val state = AppContext.getSubscriptionStateUseCase()
            callback(state)
        }
    }

    public fun signIn(callback: (User?, Throwable?) -> Unit) {
        scope.launch {
            val result = AppContext.signInUseCase()
            result.fold(
                onSuccess = { callback(it, null) },
                onFailure = { callback(null, it) },
            )
        }
    }

    public fun signInWithEmail(
        email: String,
        password: String,
        callback: (User?, Throwable?) -> Unit,
    ) {
        scope.launch {
            val result = AppContext.signInWithEmailUseCase(email, password)
            result.fold(
                onSuccess = { callback(it, null) },
                onFailure = { callback(null, it) },
            )
        }
    }

    public fun purchase(productId: String, callback: (PurchaseResult?, Throwable?) -> Unit) {
        scope.launch {
            val result = AppContext.purchaseSubscriptionUseCase(productId)
            result.fold(
                onSuccess = { callback(it, null) },
                onFailure = { callback(null, it) },
            )
        }
    }

    public fun restorePurchases(callback: (Throwable?) -> Unit) {
        scope.launch {
            val result = AppContext.restorePurchasesUseCase()
            callback(result.exceptionOrNull())
        }
    }
}
