package com.panelpass.features.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.panelpass.features.auth.domain.User
import com.panelpass.features.billing.domain.PurchaseResult
import com.panelpass.features.billing.domain.SubscriptionState
import com.panelpass.shell.AppContext
import kotlinx.coroutines.launch

/**
 * Home / shell screen. New features: add navigation to other destinations from [com.panelpass.shell.navigation.AppDestination].
 */
@Composable
internal fun MainScreen() {
    var user by remember { mutableStateOf<User?>(null) }
    var subscriptionState by remember { mutableStateOf<SubscriptionState>(SubscriptionState.Unknown) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var emailText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
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
            OutlinedTextField(
                value = emailText,
                onValueChange = { emailText = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = passwordText,
                onValueChange = { passwordText = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    loading = true
                    scope.launch {
                        val result = AppContext.signInWithEmailUseCase(emailText, passwordText)
                        loading = false
                        result.fold(
                            onSuccess = {
                                user = it
                                message = null
                            },
                            onFailure = { message = it.message },
                        )
                    }
                },
                enabled = !loading && emailText.isNotBlank() && passwordText.length >= 6,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Sign in with email")
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    loading = true
                    scope.launch {
                        val result = AppContext.signInUseCase()
                        loading = false
                        result.fold(
                            onSuccess = {
                                user = it
                                message = null
                            },
                            onFailure = { message = it.message },
                        )
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth(),
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
