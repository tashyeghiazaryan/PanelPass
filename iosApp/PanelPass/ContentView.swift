import SwiftUI
import shared

struct ContentView: View {
    @State private var user: User? = nil
    @State private var isSubscribed = false
    @State private var loading = false
    @State private var message: String? = nil
    @State private var emailText = ""
    @State private var passwordText = ""
    @State private var showEmailSignInFields = false

    private var canSignInWithEmail: Bool {
        !emailText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty && passwordText.count >= 6
    }

    var body: some View {
        VStack(spacing: 16) {
            Text(user != nil ? "Signed in: \(user?.name ?? user?.email ?? user?.id ?? "")" : "Not signed in")
                .padding()

            if user == nil {
                Button("Sign in with Apple") {
                    performSignIn()
                }
                .disabled(loading)

                Button("Sign in with EMail") {
                    withAnimation(.easeInOut(duration: 0.2)) {
                        showEmailSignInFields = true
                    }
                }
                .disabled(loading)

                if showEmailSignInFields {
                    VStack(spacing: 12) {
                        TextField("Email", text: $emailText)
                            .textContentType(.emailAddress)
                            .keyboardType(.emailAddress)
                            .autocapitalization(.none)
                            .disableAutocorrection(true)
                            .padding(.horizontal, 24)
                            .textFieldStyle(.roundedBorder)
                        SecureField("Password (min 6 characters)", text: $passwordText)
                            .textContentType(.password)
                            .padding(.horizontal, 24)
                            .textFieldStyle(.roundedBorder)
                        Button("Sign in") {
                            performEmailSignIn()
                        }
                        .disabled(loading || !canSignInWithEmail)
                        Button("Cancel") {
                            withAnimation(.easeInOut(duration: 0.2)) {
                                showEmailSignInFields = false
                                emailText = ""
                                passwordText = ""
                                message = nil
                            }
                        }
                        .disabled(loading)
                    }
                    .padding(.top, 8)
                    .transition(.opacity.combined(with: .move(edge: .top)))
                }
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
                isSubscribed = state is SubscriptionState.Subscribed
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
                    showEmailSignInFields = false
                } else {
                    message = error?.message ?? "Sign in failed"
                }
            }
        }
    }

    private func performEmailSignIn() {
        loading = true
        IosBridge.shared.signInWithEmail(email: emailText, password: passwordText) { u, error in
            DispatchQueue.main.async {
                loading = false
                if let u = u {
                    user = u
                    message = nil
                    showEmailSignInFields = false
                } else {
                    message = error?.message ?? "Sign in failed"
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
                    message = error?.message ?? "Purchase failed"
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
