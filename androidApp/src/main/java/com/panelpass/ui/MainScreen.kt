package com.panelpass.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.panelpass.AppContext
import com.panelpass.domain.auth.User
import com.panelpass.domain.billing.PurchaseResult
import com.panelpass.domain.billing.SubscriptionState
import kotlinx.coroutines.launch

@androidx.compose.runtime.Composable
internal fun MainScreen() {
    var user by remember { mutableStateOf<User?>(null) }
    var subscriptionState by remember { mutableStateOf<SubscriptionState>(SubscriptionState.Unknown) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        user = AppContext.getCurrentUserUseCase()
        subscriptionState = AppContext.getSubscriptionStateUseCase()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = if (user != null) "Signed in: ${user?.name ?: user?.email ?: user?.id}" else "Not signed in",
        )
        Spacer(Modifier.height(16.dp))

        if (user == null) {
            Button(
                onClick = {
                    loading = true
                    scope.launch {
                        val result = AppContext.signInUseCase()
                        loading = false
                        result.fold(
                            onSuccess = { user = it },
                            onFailure = { message = it.message },
                        )
                    }
                },
                enabled = !loading,
            ) {
                Text("Sign in with Google")
            }
        } else {
            Button(
                onClick = {
                    loading = true
                    scope.launch {
                        val products = AppContext.getProductsUseCase().getOrNull()
                        val productId = products?.firstOrNull()?.id ?: "premium_monthly"
                        val result = AppContext.purchaseSubscriptionUseCase(productId)
                        loading = false
                        when (result.getOrNull()) {
                            PurchaseResult.Success -> {
                                message = "Subscription purchased"
                                subscriptionState = AppContext.getSubscriptionStateUseCase()
                            }
                            PurchaseResult.Cancelled -> message = "Cancelled"
                            is PurchaseResult.Error -> message = (result.getOrNull() as PurchaseResult.Error).message
                            null -> message = result.exceptionOrNull()?.message
                        }
                    }
                },
                enabled = !loading,
            ) {
                Text("Purchase subscription")
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    loading = true
                    scope.launch {
                        AppContext.restorePurchasesUseCase()
                        subscriptionState = AppContext.getSubscriptionStateUseCase()
                        loading = false
                        message = "Restore completed"
                    }
                },
                enabled = !loading,
            ) {
                Text("Restore purchases")
            }
        }

        if (subscriptionState is SubscriptionState.Subscribed) {
            Spacer(Modifier.height(8.dp))
            Text("Premium active")
        }
        if (loading) {
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator()
        }
        message?.let { Text(it, modifier = Modifier.padding(top = 8.dp)) }
    }
}
