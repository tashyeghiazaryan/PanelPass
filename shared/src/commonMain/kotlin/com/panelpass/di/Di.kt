package com.panelpass.di

import com.panelpass.domain.auth.AuthRepository
import com.panelpass.domain.auth.GetCurrentUserUseCase
import com.panelpass.domain.auth.SignInUseCase
import com.panelpass.domain.auth.SignOutUseCase
import com.panelpass.domain.billing.BillingRepository
import com.panelpass.domain.billing.GetProductsUseCase
import com.panelpass.domain.billing.GetSubscriptionStateUseCase
import com.panelpass.domain.billing.PurchaseSubscriptionUseCase
import com.panelpass.domain.billing.RestorePurchasesUseCase
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

private val useCasesModule = module {
    singleOf(::SignInUseCase)
    singleOf(::SignOutUseCase)
    singleOf(::GetCurrentUserUseCase)
    singleOf(::GetProductsUseCase)
    singleOf(::PurchaseSubscriptionUseCase)
    singleOf(::RestorePurchasesUseCase)
    singleOf(::GetSubscriptionStateUseCase)
}

/**
 * Call from the app (Android/iOS) after creating platform [AuthRepository] and [BillingRepository].
 */
fun initKoin(
    authRepository: AuthRepository,
    billingRepository: BillingRepository,
    appDeclaration: KoinAppDeclaration = {},
) {
    startKoin {
        appDeclaration()
        modules(
            module {
                single<AuthRepository> { authRepository }
                single<BillingRepository> { billingRepository }
            },
            useCasesModule,
        )
    }
}
