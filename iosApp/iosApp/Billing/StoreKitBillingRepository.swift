import Foundation
import StoreKit
import shared

/// iOS implementation of BillingRepository using StoreKit 2.
final class StoreKitBillingRepository: BillingRepository {
    private let productIds: [String] = ["premium_monthly"]

    func getProducts(completionHandler: @escaping ([SubscriptionProduct]?, Error?) -> Void) {
        Task {
            do {
                let products = try await Product.products(for: productIds)
                let list = products.map { product in
                    SubscriptionProduct(
                        id: product.id,
                        title: product.displayName,
                        description: product.description,
                        price: product.displayPrice,
                        priceAmountMicros: Int64(product.price * 1_000_000)
                    )
                }
                completionHandler(list, nil)
            } catch {
                completionHandler(nil, error)
            }
        }
    }

    func purchase(productId: String, completionHandler: @escaping (PurchaseResult?, Error?) -> Void) {
        Task {
            do {
                guard let product = try await Product.products(for: [productId]).first else {
                    completionHandler(PurchaseResult.Error(message: "Product not found"), nil)
                    return
                }
                let result = try await product.purchase()
                switch result {
                case .success(let verification):
                    let transaction = try checkVerified(verification)
                    await transaction.finish()
                    completionHandler(PurchaseResult.Success(), nil)
                case .userCancelled:
                    completionHandler(PurchaseResult.Cancelled(), nil)
                case .pending:
                    completionHandler(PurchaseResult.Cancelled(), nil)
                @unknown default:
                    completionHandler(PurchaseResult.Error(message: "Unknown result"), nil)
                }
            } catch {
                completionHandler(PurchaseResult.Error(message: error.localizedDescription), nil)
            }
        }
    }

    func restorePurchases(completionHandler: @escaping (KotlinUnit?, Error?) -> Void) {
        Task {
            do {
                try await AppStore.sync()
                completionHandler(KotlinUnit(), nil)
            } catch {
                completionHandler(nil, error)
            }
        }
    }

    func getSubscriptionState(completionHandler: @escaping (SubscriptionState?, Error?) -> Void) {
        Task {
            do {
                var hasActiveSubscription = false
                for await result in Transaction.currentEntitlements {
                    if case .verified(_) = result {
                        hasActiveSubscription = true
                        break
                    }
                }
                let state: SubscriptionState = hasActiveSubscription
                    ? SubscriptionState.Subscribed(
                        productId: "premium_monthly",
                        expiresAtMillis: nil as KotlinLong?,
                    )
                    : SubscriptionState.NotSubscribed()
                completionHandler(state, nil)
            } catch {
                completionHandler(SubscriptionState.Unknown(), error)
            }
        }
    }

    func isBillingAvailable() -> Bool {
        true
    }

    private func checkVerified<T>(_ result: VerificationResult<T>) throws -> T {
        switch result {
        case .unverified(_, let error):
            throw error
        case .verified(let value):
            return value
        }
    }
}
