import Foundation
import StoreKit
import shared

/// Swift implementation of Kotlin `StoreKitBillingProvider`.
final class StoreKitBillingProviderImpl: StoreKitBillingProvider {
    private let productIds: [String] = ["premium_monthly"]

    func fetchProducts(onResult: @escaping ([SubscriptionProduct], KotlinThrowable?) -> Void) {
        Task {
            do {
                let products = try await Product.products(for: productIds)
                let list = products.map { product in
                    SubscriptionProduct(
                        id: product.id,
                        title: product.displayName,
                        description: product.description,
                        price: product.displayPrice,
                        priceAmountMicros: Self.priceToMicros(product.price),
                    )
                }
                onResult(list, nil)
            } catch {
                onResult([], IosErrorsKt.throwable(message: error.localizedDescription))
            }
        }
    }

    func purchase(productId: String, onResult: @escaping (PurchaseResult, KotlinThrowable?) -> Void) {
        Task {
            do {
                guard let product = try await Product.products(for: [productId]).first else {
                    onResult(PurchaseResult.Error(message: "Product not found"), nil)
                    return
                }
                let result = try await product.purchase()
                switch result {
                case .success(let verification):
                    let transaction = try Self.checkVerified(verification)
                    await transaction.finish()
                    onResult(PurchaseResult.Success.shared, nil)
                case .userCancelled:
                    onResult(PurchaseResult.Cancelled.shared, nil)
                case .pending:
                    onResult(PurchaseResult.Cancelled.shared, nil)
                @unknown default:
                    onResult(PurchaseResult.Error(message: "Unknown result"), nil)
                }
            } catch {
                onResult(PurchaseResult.Error(message: error.localizedDescription), nil)
            }
        }
    }

    func restorePurchases(onResult: @escaping (KotlinThrowable?) -> Void) {
        Task {
            do {
                try await AppStore.sync()
                onResult(nil)
            } catch {
                onResult(IosErrorsKt.throwable(message: error.localizedDescription))
            }
        }
    }

    func fetchSubscriptionState(onResult: @escaping (SubscriptionState, KotlinThrowable?) -> Void) {
        Task {
            do {
                var hasActiveSubscription = false
                for await result in Transaction.currentEntitlements {
                    if case .verified = result {
                        hasActiveSubscription = true
                        break
                    }
                }
                let state: SubscriptionState = hasActiveSubscription
                    ? SubscriptionState.Subscribed(
                        productId: "premium_monthly",
                        expiresAtMillis: nil as KotlinLong?,
                    )
                    : SubscriptionState.NotSubscribed.shared
                onResult(state, nil)
            } catch {
                onResult(SubscriptionState.Unknown.shared, IosErrorsKt.throwable(message: error.localizedDescription))
            }
        }
    }

    func isAvailable() -> Bool {
        true
    }

    private static func priceToMicros(_ price: Decimal) -> Int64 {
        let ns = NSDecimalNumber(decimal: price)
        return ns.multiplying(by: NSDecimalNumber(value: 1_000_000)).int64Value
    }

    private static func checkVerified<T>(_ result: VerificationResult<T>) throws -> T {
        switch result {
        case .unverified(_, let error):
            throw error
        case .verified(let value):
            return value
        }
    }
}
