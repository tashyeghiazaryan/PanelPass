import SwiftUI
import shared

@main
struct PanelPassApp: App {
    init() {
        let apple = AppleSignInProviderImpl()
        let authRepo = IosAuthRepository(apple: apple)
        let billingProvider = StoreKitBillingProviderImpl()
        let billingRepo = IosBillingRepository(store: billingProvider)
        DiKt.doInitKoin(
            authRepository: authRepo,
            billingRepository: billingRepo,
            appDeclaration: { _ in },
        )
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
