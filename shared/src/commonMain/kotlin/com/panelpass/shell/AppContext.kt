package com.panelpass.shell

import com.panelpass.features.auth.usecase.GetCurrentUserUseCase
import com.panelpass.features.auth.usecase.SignInUseCase
import com.panelpass.features.auth.usecase.SignInWithEmailUseCase
import com.panelpass.features.auth.usecase.SignOutUseCase
import com.panelpass.features.billing.usecase.GetProductsUseCase
import com.panelpass.features.billing.usecase.GetSubscriptionStateUseCase
import com.panelpass.features.billing.usecase.PurchaseSubscriptionUseCase
import com.panelpass.features.billing.usecase.RestorePurchasesUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Resolves use cases after [com.panelpass.di.initKoin].
 * For new features, add `val myUseCase: MyUseCase by inject()` here or use a feature-scoped holder.
 */
public object AppContext : KoinComponent {
    public val signInUseCase: SignInUseCase by inject()
    public val signInWithEmailUseCase: SignInWithEmailUseCase by inject()
    public val signOutUseCase: SignOutUseCase by inject()
    public val getCurrentUserUseCase: GetCurrentUserUseCase by inject()
    public val getProductsUseCase: GetProductsUseCase by inject()
    public val purchaseSubscriptionUseCase: PurchaseSubscriptionUseCase by inject()
    public val restorePurchasesUseCase: RestorePurchasesUseCase by inject()
    public val getSubscriptionStateUseCase: GetSubscriptionStateUseCase by inject()
}
