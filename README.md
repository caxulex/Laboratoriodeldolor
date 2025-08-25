# Laboratorio del Dolor

This Android app uses Kotlin + Jetpack Compose + Room.

Quick developer notes to keep the project scalable and maintainable:

- Keep UI strings in `res/values/strings.xml` and add localized files like `values-es/strings.xml` for Spanish.
- Use ViewModels for UI state and expose immutable flows (StateFlow) for Composables to collect.
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
- Add an Undo Snackbar after deletion.

## Hackathon: Instrucciones y Demo (Español)

Descripción corta:

Laboratorio del Dolor es una app móvil para registrar estado de ánimo y puntos de dolor, recibir recomendaciones personalizadas y realizar ejercicios dirigidos.

Descripción larga:

La aplicación permite a los usuarios anotar su estado de ánimo en una escala de 5 niveles, marcar puntos de dolor en una silueta corporal (frente/espalda y por género), y guardar sesiones de ejercicio. Un motor de recomendaciones proporciona sugerencias basadas en reglas (ahora definidas de forma data-driven) que analizan historiales de ánimo y la localización del dolor. La app incluye persistencia segura mediante Room con migraciones y una UI moderna en Jetpack Compose.

Características clave:

- Registro de ánimo en escala de 5 niveles y notas asociadas.
- Registro de puntos de dolor sobre silueta (frente/espalda) y agrupación por sesión.
- Motor de recomendaciones basado en reglas data-driven (fácilmente extensible).
- Rastreo de ejercicios y racha diaria con opción "Deshacer" tras marcar ejercicios como completados.
- Persistencia con Room y DAOs separados (`PainLogDao`, `PainPointDao`, `ExerciseDao`).
- UI en Jetpack Compose con componentes reutilizables (LottieSaveButton, MoodEmojiButton) y tokens de diseño.

Cómo probar (flujo principal):

1. Abrir la app y en la pantalla principal seleccionar un estado de ánimo (por ejemplo: triste).
2. Navegar al rastreador de dolor, seleccionar "Espalda" y marcar puntos altos en la zona superior.
3. Volver a la pantalla de Recomendaciones: la app debería sugerir una rutina para la parte superior de la espalda.
4. En la pantalla principal, marcar ejercicios como completados; aparecerá un snackbar con la opción "Deshacer". Pulsar "Deshacer" eliminará el último registro de ejercicio.
5. Revisar historial y recomendaciones para validar persistencia y reglas.

Notas para el envío al hackathon:

- Incluya este README en la raíz del repositorio.
- Adjunte un breve video de demostración de 90 segundos siguiendo el guion en `demo_script_90s.txt`.
- Asegúrese de que los tests unitarios pasan (`.
	.\gradlew.bat test`) y, si es posible, ejecute los tests de instrumentación en un emulador para validar migrations (`.\gradlew.bat connectedAndroidTest`).

## Pre-release checklist (ejecutar en este orden)

1. Firmado (keystore): cree un keystore de lanzamiento y coloque sus credenciales en `key.properties` (vea `key.properties.template`). No comitear `key.properties`. Hay instrucciones en `app/README-signing.md`.
2. Minify / R8: la build release tiene `isMinifyEnabled = true`. Genere `:app:bundleRelease` localmente y corrija reglas ProGuard si alguna librería falla.
3. Iconos adaptativos: genere `ic_launcher_foreground` / `ic_launcher_background` y mipmaps para todas densidades.
4. Activos de la tienda: prepare capturas (teléfono/tablet), feature graphic (1024x500) y texto de la ficha de Play Store.
5. Política de privacidad: hospede la política y actualice `R.string.about_privacy_url` con la URL pública. Hay una plantilla en `PRIVACY_POLICY_TEMPLATE.md`.
6. Target API: revise `compileSdk` / `targetSdk` y actualice al SDK requerido por Google Play si fuera necesario.
7. Generar AAB firmado y subir a la pista interna para pruebas: `.\gradlew.bat :app:bundleRelease`.

Opciones que puedo realizar por usted:

- Agregar una GitHub Action para generar AAB firmado en CI (usa secrets, no se incluyen claves aquí).
- Crear un conjunto de iconos adaptativos de ejemplo (placeholder) en `app/src/main/res/mipmap-*/`.
- Redactar la ficha de Play Store (texto en español) y ejemplos de capturas.



