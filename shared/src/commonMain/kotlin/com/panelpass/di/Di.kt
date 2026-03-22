package com.panelpass.di

import com.panelpass.di.modules.allApplicationModules
import com.panelpass.features.auth.domain.AuthRepository
import com.panelpass.features.billing.domain.BillingRepository
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/**
 * Application DI entry — **keep this package** so Swift keeps a stable `DiKt` symbol.
 *
 * New features: extend [com.panelpass.di.modules.allApplicationModules].
 */
public fun initKoin(
    authRepository: AuthRepository,
    billingRepository: BillingRepository,
    appDeclaration: KoinAppDeclaration = {},
) {
    startKoin {
        appDeclaration()
        modules(allApplicationModules(authRepository, billingRepository))
    }
}
