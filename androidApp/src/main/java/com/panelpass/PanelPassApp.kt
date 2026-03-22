package com.panelpass

import android.app.Application
import android.content.Intent
import com.panelpass.platform.auth.GoogleAuthRepository
import com.panelpass.platform.billing.PlayBillingRepository
import com.panelpass.di.initKoin

class PanelPassApp : Application() {
    private lateinit var authRepo: GoogleAuthRepository

    override fun onCreate() {
        super.onCreate()
        authRepo = GoogleAuthRepository(this)
        val billingRepo = PlayBillingRepository(this)
        initKoin(authRepository = authRepo, billingRepository = billingRepo)
    }

    fun deliverSignInResult(data: Intent?) {
        if (::authRepo.isInitialized) authRepo.deliverSignInResult(data)
    }
}
