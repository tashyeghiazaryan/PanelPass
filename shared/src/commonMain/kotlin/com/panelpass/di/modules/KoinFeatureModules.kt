package com.panelpass.di.modules

import com.panelpass.features.auth.domain.AuthRepository
import com.panelpass.features.auth.usecase.GetCurrentUserUseCase
import com.panelpass.features.auth.usecase.SignInUseCase
import com.panelpass.features.auth.usecase.SignInWithEmailUseCase
import com.panelpass.features.auth.usecase.SignOutUseCase
import com.panelpass.features.billing.domain.BillingRepository
import com.panelpass.features.billing.usecase.GetProductsUseCase
import com.panelpass.features.billing.usecase.GetSubscriptionStateUseCase
import com.panelpass.features.billing.usecase.PurchaseSubscriptionUseCase
import com.panelpass.features.billing.usecase.RestorePurchasesUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Platform [AuthRepository] provided by Android/iOS app — register here when adding auth backends.
 */
public fun authRepositoriesModule(authRepository: AuthRepository): Module = module {
    single<AuthRepository> { authRepository }
}

/** Use cases for the auth feature — grows with new auth flows. */
public val authUseCasesModule: Module = module {
    singleOf(::SignInUseCase)
    singleOf(::SignInWithEmailUseCase)
    singleOf(::SignOutUseCase)
    singleOf(::GetCurrentUserUseCase)
}

public fun billingRepositoriesModule(billingRepository: BillingRepository): Module = module {
    single<BillingRepository> { billingRepository }
}

/** Use cases for subscriptions / IAP. */
public val billingUseCasesModule: Module = module {
    singleOf(::GetProductsUseCase)
    singleOf(::PurchaseSubscriptionUseCase)
    singleOf(::RestorePurchasesUseCase)
    singleOf(::GetSubscriptionStateUseCase)
}

/**
 * All Koin modules in dependency order. When you add a new feature module:
 * 1. Create `features/<name>/` with domain + usecase packages
 * 2. Add `xxxRepositoriesModule` / `xxxUseCasesModule` here
 * 3. Append to [allApplicationModules]
 */
public fun allApplicationModules(
    authRepository: AuthRepository,
    billingRepository: BillingRepository,
): List<Module> = listOf(
    authRepositoriesModule(authRepository),
    billingRepositoriesModule(billingRepository),
    authUseCasesModule,
    billingUseCasesModule,
)
