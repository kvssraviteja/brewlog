# brewlog
Record, refine, and rediscover your perfect coffee brews. Personal coffee brew log

## Android app

This repository contains a native Android app for Brewlog.

### Stack

- Kotlin
- Jetpack Compose
- Material 3
- ExifInterface
- Room
- Gradle Kotlin DSL

### Requirements

- Android Studio with JDK 17
- Android SDK 36
- Gradle Wrapper included for command-line builds

### Getting started

Open the repository in Android Studio and sync the Gradle project. The app stores coffee bags, brew segments, and brew logs locally on the device.

Current app flow:

- Add coffee bags with roaster notes, roast date, bean details, and an optional bag image.
- Organize logs inside each bag by brew segment, such as `Pour over - B75` or `Espresso - Espresso machine`.
- Add, edit, and delete brew logs with recipe, water, grinder, grind size, tasting notes, next improvements, rating, and timing details.
- Delete brew segments and their logs when a brew setup is no longer useful.

Useful command-line checks:

```sh
./gradlew test
./gradlew assembleDebug
```

### Current structure

- `app/src/main/java/com/kvssrt/brewlog/data`: Room entities, DAO, database, repository, image storage, and brew log models.
- `app/src/main/java/com/kvssrt/brewlog/ui`: Compose screens for coffee bags, brew segments, and logs.
- `app/src/main/java/com/kvssrt/brewlog/ui/theme`: app color theme.

### Next decisions

- Deleting coffee bags.
- Exporting local data.
- Faster repeat-log flows from the last brew for a bag and segment.
- Espresso-specific fields after testing the generic espresso segment flow.
