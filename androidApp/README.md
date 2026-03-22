# Модуль `androidApp`

Собирайте и синхронизируйте проект **из корня репозитория `PanelPass`**, а не из этой папки.

В терминале:

```bash
cd ~/Developer/PanelPass   # родительская папка
./gradlew :androidApp:assembleDebug
```

**Не используйте Gradle 9.x** для этого монорепо: Kotlin Multiplatform + AGP 8.7 рассчитаны на **Gradle 8.x** (в корне зафиксировано **8.11.1**). Старый `androidApp/gradlew` раньше тянул **9.0-milestone** — он обновлён до 8.11.1; при ошибке `DefaultArtifactPublicationSet` проверьте `./gradlew --version` и при необходимости собирайте только из **`PanelPass/gradlew`**.

Здесь **нет** отдельного `settings.gradle.kts`: модуль `:androidApp` подключается только из корневого `PanelPass/settings.gradle.kts` вместе с `:shared` (KMP).

Если видите ошибку `project 'androidApp' not found in root project 'androidApp'` — вы запускали Gradle с неправильного корня; перейдите в `PanelPass` и используйте команду выше.
