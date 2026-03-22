# PanelPass

Cross-platform mobile app (iOS and Android) with Kotlin Multiplatform: Apple / Google Sign In, **email + password** (local demo session), and in-app subscriptions (StoreKit 2 / Play Billing).

> **Email login** is implemented for UX/testing: validation + local persistence only. For production, wire `AuthRepository.signInWithEmail` to your backend or Firebase Auth.

## Structure

Подробно: **[ARCHITECTURE.md](./ARCHITECTURE.md)** (слои, как добавить фичу, Gradle-модули).

- **shared/** — KMP: `features/<name>/{domain,usecase}`, `shell` (AppContext, navigation), `di` + `di.modules` (Koin), `ios` (IosBridge), `platform/ios` (обёртки под Swift)
- **androidApp/** — `platform/*` (Google / Play Billing), `features/*/ui` (Compose), `shell`, `com.panelpass` (Application / Activity)
- **iosApp/** — Xcode-проект, SwiftUI, провайдеры в `PanelPass/`

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
