# Checklist de Lanzamiento a Google Play Console

Última actualización: 12/09/2025

Este documento te guía para preparar y subir la App a Google Play Console.

## 1) Versionado
- Incrementa `versionCode` y ajusta `versionName` en `app/build.gradle.kts`.
- Hecho: `versionCode=2`, `versionName=1.0.1`.

## 2) Firma (Signing)
- Crea o usa una keystore propia (NO usar debug para producción):
  - PowerShell:
    - `keytool -genkeypair -v -keystore keystore/release.keystore -alias release_key -keyalg RSA -keysize 2048 -validity 10000`
- Crea `key.properties` en la raíz del repo (no lo comitees):
  - `storeFile=keystore/release.keystore`
  - `storePassword=...`
  - `keyAlias=release_key`
  - `keyPassword=...`
- El build ya está preparado para leer `key.properties` y firmar la variante `release`.

## 3) Generar App Bundle (AAB)
- Windows PowerShell:
  - `./gradlew.bat :app:bundleRelease`
- Salida esperada: `app/build/outputs/bundle/release/app-release.aab`.

## 4) Políticas y Ficha de Play Store
- Política de Privacidad: `PRIVACY_POLICY_ES.md`. Sube este contenido a una URL pública (GitHub Pages, Gist, o enlace RAW del repo) y pega el link en Play Console.
- Textos de la ficha: revisa `store/playstore_listing_es.md`.
- Capturas: `store/screenshots/`.
- Icono: `@mipmap/ic_launcher` (ya incluido).

## 5) Declaraciones en Play Console
- Clasificación de contenido (cuestionario IARC).
- Seguridad de datos (Data safety): datos locales; sin envío a servidores; sin analítica de terceros.
- Audiencia: adultos.
- Permisos: "Alarms & reminders" (usa `SCHEDULE_EXACT_ALARM`) — explica que es para recordatorios locales.

## 6) Pruebas internas/abiertas
- Crea una pista interna para probar el AAB.
- Invita testers y verifica instalación en dispositivos reales.

## 7) Lanzamiento en producción
- Sube el AAB firmado.
- Completa notas de la versión (en español).
- Revisa advertencias de Play Console y publica.

## 8) Recomendaciones
- Considera cambiar `applicationId` a un dominio propio antes del primer envío (p.ej., `com.caxulex.laboratoriodeldolor`). Una vez publicada, no podrás cambiarlo.
- La app empaqueta solo recursos `es`. Si agregas más idiomas, ajusta `resourceConfigurations`.
