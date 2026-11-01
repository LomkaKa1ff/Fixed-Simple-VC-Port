# Fixed Voice Port — проект для IntelliJ IDEA

Это версия исходников с обычной сборкой Gradle/ModDevGradle. Python для сборки не нужен.
Версия 1.1.0 добавляет поле порта голосового чата в меню «Открыть для сети».

## 1. Распаковка

Распакуй архив, например в `C:\JavaProjects\fixed-voice-port`.
В этой папке должны лежать `build.gradle`, `settings.gradle`, `gradlew.bat`, `gradle.properties` и `src`.
Открывай эту папку целиком, а не папку `src`.

## 2. Открытие в IDEA

Нажми Open на стартовом экране IDEA либо File → Open и выбери папку проекта.
Если IDEA спросит, открыть ли проект как Gradle, согласись. Разреши импорт проекта, если доверяешь содержимому архива.
Если автоматический импорт не начался, щёлкни по `build.gradle` правой кнопкой и выбери Link Gradle Project / Import Gradle Project.

## 3. JDK 21

Открой File → Project Structure → Project → SDK.
Выбери JDK 21. Если его нет, выбери Download JDK → Version 21 → любой доступный полноценный JDK (например Eclipse Temurin или Microsoft OpenJDK).
Language level: SDK default.
JDK нужен отдельно от Java, на которой запускается сама IDEA.

Затем File → Settings → Build, Execution, Deployment → Build Tools → Gradle:

- Distribution: Wrapper (`gradle-wrapper.properties`).
- Gradle JVM: выбранный JDK 21.
- Build and run using: Gradle.

Нажми Apply, затем Reload All Gradle Projects в правой панели Gradle.
Первый импорт скачивает Gradle, NeoForge, Minecraft и Simple Voice Chat, поэтому требует интернета и времени.

## 4. Сборка

В правой панели Gradle открой Tasks → build → build и запусти двойным щелчком.
Либо в терминале проекта выполни:

```powershell
.\gradlew.bat build
```

Ожидаемый результат — BUILD SUCCESSFUL.
Готовый мод: `build/libs/fixed-voice-port-neoforge-1.21.1-1.1.0.jar`.
Архив с суффиксом `-sources.jar` содержит исходники; его не нужно класть в mods.
В терминале Java берётся из окружения системы. Если терминал сообщает, что JAVA_HOME/JDK не найден, запускай задачу из панели Gradle IDEA либо настрой Java 21 в PATH/JAVA_HOME.

## 5. Запуск из IDEA

В панели Gradle найди задачу `runClient` (при необходимости используй поиск задач) и запусти её.
Можно также выполнить `.\gradlew.bat runClient`.
Игра запускается в отдельной папке `run`, без твоей основной сборки Prism.
Simple Voice Chat 2.6.22 скачивается автоматически как зависимость проекта.
Создай тестовый мир и открой его для сети. Для нового конфига голосовой порт по умолчанию — 24454. Существующий конфиг (например 54563) сохраняется. В меню LAN под игровым портом есть отдельное поле порта голосового чата. Выбранный порт применяется и сохраняется при нажатии «Открыть мир для сети». «Отмена» ничего не сохраняет.
Конфиг тестового запуска: `run/config/fixed-voice-port.properties`.
Игровые логи: `run/logs/latest.log`.
Для проверки с другом используй собранный JAR в обычном авторизованном клиенте Prism.

## 6. Что редактировать

| Файл | Назначение |
|---|---|
| `src/main/java/dev/fixedvoiceport/FixedVoicePort.java` | Регистрация мода |
| `src/main/java/dev/fixedvoiceport/PortConfig.java` | Чтение конфига, порт 24454 по умолчанию |
| `src/main/java/dev/fixedvoiceport/mixin/VoiceServerMixin.java` | Изменение поведения Simple Voice Chat |
| `src/main/resources/fixed_voice_port.mixins.json` | Регистрация Mixin |
| `src/main/resources/META-INF/neoforge.mods.toml` | Метаданные и требования к версиям |
| `gradle.properties` | Версия нашего мода и NeoForge |
| `build.gradle` | Зависимости, Java 21, сборка и запуск |
| `settings.gradle` | Имя проекта и настройки плагинов Gradle |
| `gradlew.bat`, `gradlew`, `gradle/wrapper/*` | Gradle Wrapper; запускает нужную версию Gradle |

Чтобы выпустить обновление, измени `mod_version=1.1.0` в `gradle.properties`, например на `1.1.1`.
Версия попадёт в имя JAR и метаданные автоматически.
Simple Voice Chat ограничен версией `1.21.1-2.6.22`: изменение зависимости без проверки Mixin может сломать аддон.
Существующий пользовательский конфиг имеет приоритет над значением по умолчанию в Java.

## 7. Если создавать самостоятельно с нуля

1. Возьми официальный MDK для **1.21.1 ModDevGradle**, а не Fabric/Forge/другой версии игры.
2. Импортируй его в IDEA и настрой JDK 21.
3. Удали Java-классы примера ExampleMod и его метаданные из `src/main/templates`, чтобы не получить два файла neoforge.mods.toml.
4. Перенеси `src` нашего проекта.
5. Настрой `build.gradle` и `gradle.properties` по файлам из этого архива. В данной версии метаданные находятся в `src/main/resources`, а шаблонная задача MDK не используется.
6. Оставь Gradle Wrapper и `settings.gradle` из совместимого MDK. В готовом архиве они уже есть.
7. Собери через Gradle `build`, проверь JAR в игре.

## Установка готового мода и обновление

Готовый JAR находится в папке `dist`. Закрой Minecraft и замени старый JAR Fixed Voice Port на `fixed-voice-port-neoforge-1.21.1-1.1.0.jar`.
Оставь Simple Voice Chat 2.6.22. Другу этот аддон не нужен.
Открой одиночный мир → «Открыть для сети». Под игровым портом появится «Порт голосового чата» (Voice Chat Port на английском).
Для новой установки предлагается UDP 24454. Если конфиг уже содержит 54563, это число будет показано вместо 24454.
Выбранный голосовой порт должен быть проброшен на роутере и разрешён в брандмауэре как UDP; игровой порт используется отдельно как TCP.
Поле принимает только цифры и значения 1–65535. Если UDP-порт занят или конфиг невозможно сохранить, экран покажет ошибку и не продолжит открытие LAN.
При ошибке привязки UDP прежний сокет остаётся открытым. При ошибке записи конфига новый сокет может быть уже применён; можно повторить попытку или выйти из мира.
Пустое поле не выбирает случайный порт. При изменении размера окна введённое значение сохраняется.

## Проверки этой версии проекта

- Java-код скомпилирован JDK 21 против реальных Minecraft 1.21.1, NeoForge 21.1.265 и Simple Voice Chat 2.6.22.
- Проверены существование поля portEdit, сигнатуры init/render и оба вызова Button.builder в байткоде ShareToLanScreen 1.21.1.
- Проверены новый default 24454, сохранение существующего 54563, запись/чтение выбранного порта, границы и отбрасывание недопустимых значений.
- Проверены ZIP/JAR, метаданные, JSON переводов, версия 1.1.0 и совместимость Mixin JAVA_21.
- Gradle загрузил зависимости, но createMinecraftArtifacts завершился ошибкой внутри NeoForm Runtime (NoSuchElementException в NeoFormEngine) в среде подготовки. Готовый JAR собран напрямую javac, без обхода проверок версий.
- Полный запуск Minecraft, применение Mixin и визуальная проверка нового поля в игре здесь не выполнялись. При вылете пришли logs/latest.log.

Официальная документация: https://docs.neoforged.net/docs/1.21.1/gettingstarted/

Gradle Wrapper и шаблон конфигурации взяты из https://github.com/NeoForgeMDKs/MDK-1.21.1-ModDevGradle
Gradle Wrapper содержит собственные уведомления и распространяется по Apache License 2.0.
Код аддона: MIT, см. LICENSE.
