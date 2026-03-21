# PanelPass

Cross-platform mobile app (iOS and Android) with Kotlin Multiplatform: Apple / Google Sign In and in-app subscriptions (StoreKit 2 / Play Billing).

## Structure

- **shared/** — KMP shared (domain, use cases, DI, IosBridge)
- **androidApp/** — Android (Compose, Google Sign In, Play Billing), package `com.panelpass`
- **iosApp/** — iOS app
  - **PanelPass.xcodeproj** — Xcode project (откройте этот файл в Xcode)
  - **PanelPass/** — исходники Swift (SwiftUI, Sign in with Apple, StoreKit 2), Info.plist, entitlements
- **shared/src/iosMain/** — `IosAuthRepository` / `IosBillingRepository` и провайдеры `AppleSignInProvider` / `StoreKitBillingProvider` с колбэками (Swift не реализует Kotlin `suspend` + `Result` напрямую)

## Открытие проекта

- **Android Studio / Cursor:** откройте папку **PanelPass** (`~/Developer/PanelPass`).
- **Xcode:** откройте файл **`iosApp/PanelPass.xcodeproj`**. При первой сборке Run Script автоматически соберёт shared-фреймворк (нужны **Java** в PATH и рабочий **`./gradlew`** в корне PanelPass).

### Если Xcode пишет `gradlew not found or not executable`

В корне проекта должны быть **`gradlew`** (исполняемый) и **`gradle/wrapper/gradle-wrapper.jar`**. Из терминала:

```bash
cd ~/Developer/PanelPass
chmod +x gradlew
# если нет jar — скопируйте из другого проекта или выполните:
# gradle wrapper --gradle-version 8.11.1
```

Проверка: `./gradlew :shared:tasks --no-daemon` (должно завершиться без ошибки).

## Build

### Android

```bash
./gradlew :androidApp:assembleDebug
```

Укажите `default_web_client_id` в `androidApp/src/main/res/values/strings.xml` (Web client ID из Google Cloud Console).

### iOS

В Xcode выберите схему **PanelPass** и устройство/симулятор, нажмите **Run**. Либо из терминала:

```bash
./gradlew :shared:linkReleaseFrameworkIosArm64   # устройство
./gradlew :shared:linkReleaseFrameworkIosSimulatorArm64   # симулятор
```

В Xcode уже настроены: Run Script (сборка shared), линковка и Embed `shared.framework`, **Sign in with Apple** в entitlements.

## Configuration

- **Google (Android):** OAuth 2.0 Web client ID в `strings.xml`; подписка (например `premium_monthly`) в Play Console.
- **Apple (iOS):** в идентификаторе приложения включите Sign in with Apple; при необходимости — In-App Purchase. Подписку создайте в App Store Connect.

Корень проекта: **PanelPass** (например `~/Developer/PanelPass`).
