import SwiftUI
import shared

@main
struct PanelPassApp: App {
    init() {
        let authRepo = AppleAuthRepository()
        let billingRepo = StoreKitBillingRepository()
        DiKt.initKoin(authRepository: authRepo, billingRepository: billingRepo)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
