# PanelPass

Cross-platform mobile app (iOS and Android) with Kotlin Multiplatform: Apple / Google Sign In, **email + password** (local demo session), and in-app subscriptions (StoreKit 2 / Play Billing).

> **Email login** is implemented for UX/testing: validation + local persistence only. For production, wire `AuthRepository.signInWithEmail` to your backend or Firebase Auth.

## Structure

Подробно: **[ARCHITECTURE.md](./ARCHITECTURE.md)** (слои, как добавить фичу, Gradle-модули).

- **shared/** — KMP: `features/<name>/{domain,usecase}`, `shell` (AppContext, navigation), `di` + `di.modules` (Koin), `ios` (IosBridge), `platform/ios` (обёртки под Swift)
- **androidApp/** — `platform/*` (Google / Play Billing), `features/*/ui` (Compose), `shell`, `com.panelpass` (Application / Activity)
- **iosApp/** — Xcode-проект, SwiftUI, провайдеры в `PanelPass/`

## Открытие проекта

- **Android Studio / Cursor:** откройте папку **PanelPass** (`~/Developer/PanelPass`) — **корень репозитория**, где лежат `settings.gradle.kts` и `gradlew`. Не открывайте только подпапку `androidApp`: иначе Gradle может не подхватить общие настройки и не найти плагин Android (`com.android.application`).
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

**Сборка debug APK:**

```bash
cd ~/Developer/PanelPass
./gradlew :androidApp:assembleDebug
```

Готовый APK лежит в `androidApp/build/outputs/apk/debug/` (имя файла зависит от модуля, чаще `androidApp-debug.apk`).

**Запуск на эмуляторе или устройстве (установка + запуск приложения):**

1. Включите эмулятор в Android Studio или подключите телефон с **USB-отладкой**.
2. Из корня проекта:
   ```bash
   ./gradlew :androidApp:installDebug
   ```
3. Либо в **Android Studio**: откройте папку **PanelPass**, выберите конфигурацию **androidApp** → **Run** ▶.

**Требования:** JDK **17**, установленный **Android SDK** (через Android Studio SDK Manager), устройство не ниже **minSdk 26** (см. `gradle/libs.versions.toml`).

**Google Sign-In:** укажите `default_web_client_id` в `androidApp/src/main/res/values/strings.xml` (Web client ID из Google Cloud Console). Без него сборка обычно проходит, но вход через Google может не работать.

Если `./gradlew` не исполняется: `chmod +x gradlew` в корне проекта.

**Если `installDebug` падает с `No connected devices!`:** Gradle не видит ни эмулятора, ни телефона. Сделайте одно из:

1. Запустите **AVD** в Android Studio (*Device Manager* → Play на эмуляторе) или подключите телефон с включённой **отладкой по USB**.
2. Проверьте: `adb devices` — в списке должно быть устройство со статусом `device` (не `unauthorized`: тогда подтвердите диалог на телефоне).
3. Пока нет устройства — соберите APK без установки: **`./gradlew :androidApp:assembleDebug`**, затем установите файл из `androidApp/build/outputs/apk/debug/`, например: `adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk` (точное имя смотрите в этой папке).

Сообщение **`Unable to strip ... libandroidx.graphics.path.so`** при упаковке обычно **не критично**: библиотека кладётся в APK как есть, на запуск не влияет.

**Синхронизация: `Plugin com.android.application was not found` / искали только в Gradle Plugin Portal:**  
Плагин Android публикуется в **Google Maven**, не в центральном портале плагинов. В корневом `settings.gradle.kts` уже заданы `google()`, зеркало `dl.google.com/.../maven2` и `resolutionStrategy` для AGP. Убедитесь, что открыт **корень PanelPass**, выполните **File → Invalidate Caches** при необходимости и проверьте доступ в интернет / прокси к `*.google.com`.

**Ошибка `project 'androidApp' not found in root project 'androidApp'`:**  
Gradle запущен с **корнем `androidApp`** (часто из‑за открытой в IDE только папки `androidApp`). В таком режиме нет подпроекта `:androidApp` — сам корень уже называется `androidApp`. **Решение:** в терминале выполните `cd` в **`~/Developer/PanelPass`** (родительская папка, где лежат `settings.gradle.kts` и `gradlew`) и снова запустите `./gradlew :androidApp:assembleDebug`. В Android Studio: **File → Open** → выберите **PanelPass**, не подпапку `androidApp`. Подсказка также в `androidApp/README.md`.

**Ошибка `DefaultArtifactPublicationSet` / сборка падает на `shared/build.gradle.kts`:**  
Чаще всего запущен **Gradle 9.x** (например milestone), он несовместим с текущими **KMP + Android Gradle Plugin**. Используйте **`./gradlew` из корня `PanelPass`** (там **Gradle 8.11.1**). Команда проверки: `./gradlew --version`. В Android Studio: **Settings → Build → Gradle → Gradle JDK** и использование wrapper из проекта.

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
