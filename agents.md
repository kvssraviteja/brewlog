# Agent Instructions

## Project Context

`brewlog` is a native Android app for logging coffee bags, pour methods, and daily pour-over brews. It helps record, refine, and rediscover great coffee recipes.

The current app uses Kotlin, Jetpack Compose, Material 3, Room, KSP, and Gradle Kotlin DSL. A Gradle Wrapper is included, so prefer `./gradlew` for command-line builds and tests.

## Architecture

- `app/src/main/java/com/kvssrt/brewlog/data` contains Room entities, DAO, database setup, repository logic, draft types, validation, and model helpers.
- `app/src/main/java/com/kvssrt/brewlog/ui` contains Compose screens and local app navigation state.
- `app/src/main/java/com/kvssrt/brewlog/ui/theme` contains the Compose Material theme.
- Keep business validation in repository or data-layer helpers rather than scattering it through composables.
- Keep UI state and navigation simple until a feature creates a clear need for additional structure.

## Working Guidelines

- Prefer small, focused changes over broad rewrites.
- Preserve the package namespace `com.kvssrt.brewlog`.
- Use Jetpack Compose for UI changes.
- Follow existing Kotlin and Compose style before introducing new patterns.
- Avoid adding frameworks or dependencies unless they clearly support the next app feature.
- Update documentation when behavior, setup, or project structure changes.

## Android And Data Practices

- Use Room deliberately for persistent local data.
- Add Room migrations when schema versions change; do not rely on destructive migration for user data.
- Keep coffee-bag, pour-method, and pour-log behavior aligned across repository methods, database entities, and UI flows.
- Keep user-entered text trimmed and validation messages clear.
- Preserve local-first behavior unless a feature explicitly introduces sync or export.

## Repository Practices

- Check the current tree before editing.
- Do not overwrite user changes. If local edits are present, work around them or ask when they affect the task.
- Keep generated artifacts, secrets, local environment files, dependency directories, APKs, and build outputs out of version control.
- Prefer descriptive filenames and straightforward module boundaries as the project grows.

## Verification

- Run the narrowest relevant check for the change.
- Prefer `./gradlew test` for data, validation, and repository logic.
- Use `./gradlew assembleDebug` for Android build verification.
- For documentation-only changes, proofread the edited files.
- If a required toolchain or SDK is unavailable, state that clearly and verify by inspection where possible.

## Near-Term Priorities

- Edit and delete coffee bags and pour logs.
- Export local data.
- Faster repeat-log flows from the last brew for a bag and method.
- Mobile-friendly logging while brewing.
