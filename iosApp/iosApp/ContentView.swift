import SwiftUI
import shared

struct ContentView: View {
    @State private var user: User? = nil
    @State private var isSubscribed = false
    @State private var loading = false
    @State private var message: String? = nil

    var body: some View {
        VStack(spacing: 16) {
            Text(user != nil ? "Signed in: \(user?.name ?? user?.email ?? user?.id ?? "")" : "Not signed in")
                .padding()

            if user == nil {
                Button("Sign in with Apple") {
                    performSignIn()
                }
                .disabled(loading)
            } else {
                Button("Purchase subscription") {
                    performPurchase()
                }
                .disabled(loading)

                Button("Restore purchases") {
                    performRestore()
                }
                .disabled(loading)
            }

            if isSubscribed {
                Text("Premium active")
            }

            if loading {
                ProgressView()
            }

            if let message = message {
                Text(message)
                    .padding()
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .onAppear {
            loadState()
        }
    }

    private func loadState() {
        IosBridge.shared.getCurrentUser { u in
            DispatchQueue.main.async { user = u }
        }
        IosBridge.shared.getSubscriptionState { state in
            DispatchQueue.main.async {
                if state is SubscriptionState.Subscribed {
                    isSubscribed = true
                } else {
                    isSubscribed = false
                }
            }
        }
    }

    private func performSignIn() {
        loading = true
        IosBridge.shared.signIn { u, error in
            DispatchQueue.main.async {
                loading = false
                if let u = u {
                    user = u
                    message = nil
                } else {
                    message = error?.localizedDescription ?? "Sign in failed"
                }
            }
        }
    }

    private func performPurchase() {
        loading = true
        IosBridge.shared.purchase(productId: "premium_monthly") { result, error in
            DispatchQueue.main.async {
                loading = false
                if let result = result {
                    if result is PurchaseResult.Success {
                        message = "Subscription purchased"
                        loadState()
                    } else if result is PurchaseResult.Cancelled {
                        message = "Cancelled"
                    } else if let err = result as? PurchaseResult.Error {
                        message = err.message
                    }
                } else {
                    message = error?.localizedDescription ?? "Purchase failed"
                }
            }
        }
    }

    private func performRestore() {
        loading = true
        IosBridge.shared.restorePurchases { _ in
            DispatchQueue.main.async {
                loading = false
                loadState()
                message = "Restore completed"
            }
        }
    }
}
