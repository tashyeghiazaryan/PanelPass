import AuthenticationServices
import Foundation
import shared

/// iOS implementation of AuthRepository using Sign in with Apple.
final class AppleAuthRepository: NSObject, AuthRepository {
    func signIn(completionHandler: @escaping (User?, Error?) -> Void) {
        let request = ASAuthorizationAppleIDProvider().createRequest()
        request.requestedScopes = [.fullName, .email]
        let controller = ASAuthorizationController(authorizationRequests: [request])
        controller.delegate = self
        controller.presentationContextProvider = self
        self.completionHandler = completionHandler
        controller.performRequests()
    }

    private var completionHandler: ((User?, Error?) -> Void)?

    func signOut(completionHandler: @escaping (KotlinUnit?, Error?) -> Void) {
        completionHandler(KotlinUnit(), nil)
    }

    func getCurrentUser(completionHandler: @escaping (User?, Error?) -> Void) {
        completionHandler(nil, nil)
    }

    func isSignInAvailable() -> Bool {
        true
    }
}

extension AppleAuthRepository: ASAuthorizationControllerDelegate {
    func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization authorization: ASAuthorization
    ) {
        guard let credential = authorization.credential as? ASAuthorizationAppleIDCredential else {
            completionHandler?(nil, NSError(domain: "AppleAuth", code: -1, userInfo: [NSLocalizedDescriptionKey: "Invalid credential"]))
            completionHandler = nil
            return
        }
        let user = User(
            id: credential.user,
            email: credential.email,
            name: credential.fullName.flatMap { [$0.givenName, $0.familyName].compactMap { $0 }.joined(separator: " ") }
        )
        completionHandler?(user, nil)
        completionHandler = nil
    }

    func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError error: Error
    ) {
        completionHandler?(nil, error)
        completionHandler = nil
    }
}

extension AppleAuthRepository: ASAuthorizationControllerPresentationContextProviding {
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
