# brewlog
Record, refine, and rediscover your perfect coffee brews. Personal coffee brew log

## Android app

This repository contains a native Android app for Brewlog.

### Stack

- Kotlin
- Jetpack Compose
- Material 3
- Room
- Gradle Kotlin DSL

### Requirements

- Android Studio with JDK 17
- Android SDK 36
- Gradle Wrapper included for command-line builds

### Getting started

Open the repository in Android Studio and sync the Gradle project. The app stores coffee bags, pour methods, and daily pour-over logs locally on the device.

Useful command-line checks:

```sh
./gradlew test
./gradlew assembleDebug
```

### Current structure

- `app/src/main/java/com/kvssrt/brewlog/data`: Room entities, DAO, database, repository, and brew log models.
- `app/src/main/java/com/kvssrt/brewlog/ui`: Compose screens for coffee bags, pour methods, and logs.
- `app/src/main/java/com/kvssrt/brewlog/ui/theme`: app color theme.

### Next decisions

- Editing and deleting coffee bags or logs.
- Exporting local data.
- Faster repeat-log flows from the last brew for a bag and method.
