# PanelPass — архитектура и расширение

Цель: слои и границы фич, чтобы без боли добавлять новые модули (экраны, домены, Gradle-модули).

## Слои (KMP `shared`)

| Слой | Пакет | Назначение |
|------|--------|------------|
| **Feature domain** | `com.panelpass.features.<name>.domain` | Контракты репозиториев, модели, без платформы |
| **Feature use cases** | `com.panelpass.features.<name>.usecase` | Один класс = один сценарий (`invoke`) |
| **Shell** | `com.panelpass.shell` | `AppContext`, навигационные типы |
| **Shell navigation** | `com.panelpass.shell.navigation` | `AppDestination` — расширяйте под новые экраны |
| **DI bootstrap** | `com.panelpass.di` | `initKoin` — **не переносить пакет** (стабильный Swift `DiKt`) |
| **DI modules** | `com.panelpass.di.modules` | Koin-модули по фичам + сборка `allApplicationModules` |
| **iOS bridge** | `com.panelpass.ios` | `IosBridge` — **не переносить пакет** (Swift) |
| **Platform iOS** | `com.panelpass.platform.ios` | Обертки над Swift (Apple, StoreKit) |

## Добавить новую фичу в `shared`

1. Создать `shared/src/commonMain/kotlin/com/panelpass/features/<feature>/domain/` — интерфейсы и модели.
2. Создать `.../usecase/` — use case-и с зависимостью только от domain.
3. Добавить `xxxRepositoriesModule` / `xxxUseCasesModule` в `di/modules/KoinFeatureModules.kt`.
4. Добавить модули в `allApplicationModules(...)`.
5. При необходимости расширить `AppContext` или сделать отдельный `FeatureXContext` с `by inject()`.
6. Для iOS: при необходимости новый метод в `IosBridge` или отдельный bridge-объект в `com.panelpass.ios`.

## Android (`androidApp`)

| Область | Пакет |
|---------|--------|
| Реализации SDK | `com.panelpass.platform.auth`, `com.panelpass.platform.billing`, … |
| UI фичи | `com.panelpass.features.<name>.ui` |
| Shell | `com.panelpass.shell` (`ActivityHolder`, позже навигация) |
| Точка входа | `com.panelpass` (`Application`, `MainActivity`) |

Новый экран: `features/<feature>/ui/YourScreen.kt`, навигация — Navigation Compose + `AppDestination` (или локальные routes).

## iOS (`iosApp/PanelPass`)

Рекомендуемая раскладка папок (логически; Xcode groups можно выровнять под неё):

- `Auth/` — провайдеры Sign in with Apple / email  
- `Billing/` — StoreKit  
- `Features/Home/` — `ContentView` и домашний flow  
- `App/` — `PanelPassApp`, координатор навигации (по мере роста)

После изменений в `shared` пересоберите framework (Run Script / Gradle).

## Отдельный Gradle-модуль (позже)

В `settings.gradle.kts` можно добавить, например:

```kotlin
// include(":feature:profile")
```

и вынести KMP-исходники фичи в подпроект с `api(project(":shared"))` или общим `core`-модулем — когда появится потребность в изоляции сборки/тестов.

## Зависимости между слоями

- Use case → только свой `domain` (+ другие domain при кросс-фичах, лучше через события/интерфейсы).
- UI → use cases через `AppContext` / Koin `get()` / viewModel factory.
- Platform (Google, StoreKit) → реализует интерфейсы из `features.*.domain`.
