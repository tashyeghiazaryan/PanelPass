package com.panelpass

import com.panelpass.domain.auth.GetCurrentUserUseCase
import com.panelpass.domain.auth.SignInUseCase
import com.panelpass.domain.auth.SignOutUseCase
import com.panelpass.domain.billing.GetProductsUseCase
import com.panelpass.domain.billing.GetSubscriptionStateUseCase
import com.panelpass.domain.billing.PurchaseSubscriptionUseCase
import com.panelpass.domain.billing.RestorePurchasesUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Entry point to resolve use cases from shared code after [com.panelpass.di.initKoin] has been called.
 */
object AppContext : KoinComponent {
    val signInUseCase: SignInUseCase by inject()
    val signOutUseCase: SignOutUseCase by inject()
    val getCurrentUserUseCase: GetCurrentUserUseCase by inject()
    val getProductsUseCase: GetProductsUseCase by inject()
    val purchaseSubscriptionUseCase: PurchaseSubscriptionUseCase by inject()
    val restorePurchasesUseCase: RestorePurchasesUseCase by inject()
    val getSubscriptionStateUseCase: GetSubscriptionStateUseCase by inject()
}
