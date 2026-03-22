import AuthenticationServices
import Foundation
import shared

/// Swift implementation of Kotlin `AppleSignInProvider`.
final class AppleSignInProviderImpl: NSObject, AppleSignInProvider {
    private enum Keys {
        static let emailUserId = "panelpass_email_user_id"
        static let emailUserEmail = "panelpass_email_user_email"
        static let emailUserName = "panelpass_email_user_name"
    }

    private let defaults = UserDefaults.standard
    private var signInSuccess: ((User) -> Void)?
    private var signInFailure: ((KotlinThrowable) -> Void)?

    private static let minPasswordLength = 6

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

    func signInWithEmail(
        email: String,
        password: String,
        onSuccess: @escaping (User) -> Void,
        onFailure: @escaping (KotlinThrowable) -> Void,
    ) {
        let trimmed = email.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty, trimmed.contains("@") else {
            onFailure(IosErrorsKt.throwable(message: "Invalid email"))
            return
        }
        guard password.count >= Self.minPasswordLength else {
            onFailure(
                IosErrorsKt.throwable(
                    message: "Password must be at least \(Self.minPasswordLength) characters",
                ),
            )
            return
        }
        let localPart = trimmed.split(separator: "@", maxSplits: 1).first.map(String.init) ?? ""
        let user = User(
            id: "email:\(trimmed.lowercased())",
            email: trimmed,
            name: localPart.isEmpty ? nil : localPart,
        )
        saveEmailSession(user)
        onSuccess(user)
    }

    func signOut() {
        clearEmailSession()
    }

    func getCurrentUser() -> User? {
        loadEmailUser()
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
        clearEmailSession()
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

    private func saveEmailSession(_ user: User) {
        defaults.set(user.id, forKey: Keys.emailUserId)
        defaults.set(user.email, forKey: Keys.emailUserEmail)
        defaults.set(user.name, forKey: Keys.emailUserName)
    }

    private func clearEmailSession() {
        defaults.removeObject(forKey: Keys.emailUserId)
        defaults.removeObject(forKey: Keys.emailUserEmail)
        defaults.removeObject(forKey: Keys.emailUserName)
    }

    private func loadEmailUser() -> User? {
        guard let id = defaults.string(forKey: Keys.emailUserId), !id.isEmpty else { return nil }
        return User(
            id: id,
            email: defaults.string(forKey: Keys.emailUserEmail),
            name: defaults.string(forKey: Keys.emailUserName),
        )
    }
}
