import AuthenticationServices
import Foundation
import shared

/// Swift implementation of Kotlin `AppleSignInProvider`.
final class AppleSignInProviderImpl: NSObject, AppleSignInProvider {
    private var signInSuccess: ((User) -> Void)?
    private var signInFailure: ((KotlinThrowable) -> Void)?

    func startSignIn(onSuccess: @escaping (User) -> Void, onFailure: @escaping (KotlinThrowable) -> Void) {
        signInSuccess = onSuccess
        signInFailure = onFailure
        let request = ASAuthorizationAppleIDProvider().createRequest()
        request.requestedScopes = [.fullName, .email]
        let controller = ASAuthorizationController(authorizationRequests: [request])
        controller.delegate = self
        controller.presentationContextProvider = self
        controller.performRequests()
    }

    func signOut() {}

    func getCurrentUser() -> User? {
        nil
    }

    func isAvailable() -> Bool {
        true
    }
}

extension AppleSignInProviderImpl: ASAuthorizationControllerDelegate {
    func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization authorization: ASAuthorization
    ) {
        defer {
            signInSuccess = nil
            signInFailure = nil
        }
        guard let credential = authorization.credential as? ASAuthorizationAppleIDCredential else {
            signInFailure?(IosErrorsKt.throwable(message: "Invalid credential"))
            return
        }
        let user = User(
            id: credential.user,
            email: credential.email,
            name: credential.fullName.flatMap { [$0.givenName, $0.familyName].compactMap { $0 }.joined(separator: " ") },
        )
        signInSuccess?(user)
    }

    func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError error: Error
    ) {
        defer {
            signInSuccess = nil
            signInFailure = nil
        }
        signInFailure?(IosErrorsKt.throwable(message: error.localizedDescription))
    }
}

extension AppleSignInProviderImpl: ASAuthorizationControllerPresentationContextProviding {
    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        guard let window = UIApplication.shared.connectedScenes
            .compactMap({ $0 as? UIWindowScene })
            .flatMap({ $0.windows })
            .first(where: { $0.isKeyWindow }) else {
            return ASPresentationAnchor()
        }
        return window
    }
}
