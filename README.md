# Laboratorio del dolor

This Android app uses Kotlin + Jetpack Compose + Room.

Quick developer notes to keep the project scalable and maintainable:

- Keep UI strings in `res/values/strings.xml` and add localized files like `values-es/strings.xml` for Spanish.
- Use ViewModels for UI state and expose immutable flows (StateFlow) for composables to collect.
- Persist data via Room. When changing the schema, increment the database `version` and provide a migration or use `fallbackToDestructiveMigration()` only for development.
- Add unit tests for ViewModel logic (fast, no Android framework). Add instrumented tests for UI flows if needed.
- CI recommendations: Run `./gradlew assembleDebug test lint` on each PR.

How to run locally (Windows PowerShell):

```powershell
# build and install
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug

# run local unit tests
.\gradlew.bat test

# run lint
.\gradlew.bat lint
```

Recommended next improvements:
- Add proper migrations (avoid destructive migration in production).
- Improve accessibility: content descriptions, TalkBack labels for interactive points.
- Add instrumented UI tests (Espresso/Compose Test) for the pain tracker save/delete flow.
- Add an Undo snackbar after deletion.

