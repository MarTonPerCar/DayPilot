# DayPilot

Aplicación de productividad (tareas, pasos, temporizadores Pomodoro, límites de uso de apps, ranking social), con Supabase como backend. Construida originalmente para Android con Kotlin y Jetpack Compose, y más tarde adaptada a Flutter para dar soporte multiplataforma de escritorio.

Los usuarios registran tareas diarias, pasos y límites de uso de apps, y compiten con sus amigos en un ranking de puntos de 30 días. La app incluye temporizadores (Pomodoro y personalizados), recordatorios, un sistema de notificaciones en tiempo real y un resumen semanal de progreso.

## Ramas

**Android (Kotlin, Jetpack Compose):**

| Rama | Propósito |
|---|---|
| `Incremento-Android` | Rama principal de desarrollo — base para futuros incrementos |
| `Incremento-Android-TestFinal` | Rama de trabajo/testing activa, fusionada periódicamente en `Incremento-Android` |
| `Informacion-Supabase` | Archivos de migración SQL (esquema, datos semilla, script de borrado) |
| `Version-Original` | Código original antes del incremento de Android |
| `Test-Diseno-Android` | Rama de pruebas de solo sistema de diseño |
| `Test-Funcional-Android` | Rama de pruebas funcionales |
| `Test-Supabase-Android` | Prueba de concepto independiente que valida la conectividad con Supabase desde Android |

**Flutter (Dart, multiplataforma):**

| Rama | Propósito |
|---|---|
| `Incremento-Flutter` | Incremento final — arquitectura real con Riverpod y backend real de Supabase |
| `Incremento-Flutter-TestFinal` | Rama de trabajo/testing activa, fusionada periódicamente en `Incremento-Flutter` |
| `Test-Diseño-Flutter` | Base de solo sistema de diseño (temas, componentes, datos de prueba) |
| `Test-Supabase-Flutter` | Prueba de concepto independiente que valida `supabase_flutter` |

## Tecnologías

| | Android | Flutter |
|---|---|---|
| **Lenguaje** | Kotlin | Dart |
| **UI** | Jetpack Compose + Material 3 | Widgets de Flutter + Material 3 |
| **Gestión de estado** | MVVM con patrón repositorio | Riverpod |
| **Backend** | Supabase (PostgreSQL, Auth, Storage, Realtime) | Supabase (PostgreSQL, Auth, Storage, Realtime) |
| **CI/CD** | GitHub Actions | GitHub Actions |

## Dependencias

Listado de las dependencias declaradas en `Incremento-Android` (`gradle/libs.versions.toml` + `app/build.gradle.kts`) y `Incremento-Flutter` (`pubspec.yaml`), las ramas de producción de cada plataforma.

### Android (`Incremento-Android`)

| Dependencia | Versión | Uso |
|---|---|---|
| `androidx.core:core-ktx` | 1.17.0 | Extensiones Kotlin para las APIs core de AndroidX |
| `androidx.lifecycle:lifecycle-runtime-ktx` | 2.10.0 | Coroutines conscientes del ciclo de vida |
| `androidx.activity:activity-compose` | 1.13.0 | Integración de Compose con `Activity` |
| `androidx.compose:compose-bom` | 2026.06.01 | BOM de versiones de Jetpack Compose |
| `androidx.compose.ui:ui` / `ui-graphics` / `ui-tooling-preview` | via BOM | Toolkit base de Jetpack Compose |
| `androidx.compose.material3:material3` | via BOM | Componentes Material 3 |
| `androidx.compose.animation:animation` | 1.11.4 | Animaciones de Compose |
| `androidx.compose.material:material-icons-extended` | via BOM | Set extendido de iconos Material |
| `androidx.navigation:navigation-compose` | 2.9.8 | Navegación entre pantallas en Compose |
| `androidx.work:work-runtime-ktx` | 2.11.2 | Tareas periódicas en segundo plano (WorkManager) |
| `io.github.jan-tennert.supabase:bom` | 3.3.0 | BOM de versiones del SDK de Supabase |
| `supabase:postgrest-kt` / `auth-kt` / `realtime-kt` / `storage-kt` | via BOM | Cliente de Supabase (BD, Auth, Realtime, Storage) |
| `io.ktor:ktor-client-okhttp` | 3.3.0 | Motor HTTP para el cliente Ktor de Supabase |
| `io.coil-kt.coil3:coil-compose` / `coil-network-okhttp` | 3.5.0 | Carga asíncrona de imágenes |
| `com.github.yalantis:ucrop` | 2.2.9 | Recorte de imágenes (foto de perfil) |
| `junit:junit` | 4.13.2 | Framework de tests unitarios |
| `io.mockk:mockk` | 1.14.11 | Mocking en Kotlin |
| `kotlinx-coroutines-test` | 1.11.0 | Utilidades de test para coroutines |
| `androidx.test:core` | 1.7.0 | Utilidades de test de AndroidX (`ApplicationProvider`, etc.) |
| `androidx.arch.core:core-testing` | 2.2.0 | `InstantTaskExecutorRule` para código dirigido por lifecycle/LiveData |
| `org.robolectric:robolectric` | 4.16.1 | Simulación del framework Android en JVM |
| `io.ktor:ktor-client-mock` | 3.3.0 | Mocking HTTP para el cliente Ktor en tests |
| `androidx.test.ext:junit` / `espresso-core` | 1.3.0 / 3.7.0 | Tests instrumentados (`androidTest`) |

### Flutter (`Incremento-Flutter`)

| Dependencia | Versión | Uso |
|---|---|---|
| `flutter_localizations` / `intl` | SDK / 0.20.2 | Localización (es/en/de) |
| `cupertino_icons` | ^1.0.8 | Set de iconos estilo iOS |
| `window_manager` | ^0.5.2 | Gestión de la ventana en escritorio (tamaño, posición) |
| `tray_manager` | ^0.5.3 | Icono en la bandeja del sistema |
| `screen_retriever` | ^0.2.2 | Información de la pantalla para posicionar la ventana |
| `supabase_flutter` | ^2.15.4 | Cliente de Supabase (BD, Auth, Storage, Realtime) |
| `flutter_riverpod` | ^3.3.2 | Gestión de estado |
| `uuid` | ^4.5.3 | Generación de identificadores únicos |
| `shared_preferences` | ^2.5.5 | Persistencia local clave-valor |
| `image_picker` | ^1.2.3 | Selección de foto de perfil |
| `launch_at_startup` | ^0.5.1 | Autoarranque en escritorio |
| `win32` / `ffi` | ^5.15.0 / ^2.2.0 | Bindings nativos de Windows |
| `local_notifier` | ^0.1.6 | Notificaciones locales en escritorio |
| `flutter_test` | SDK | Framework de tests |
| `flutter_lints` | ^6.0.0 | Reglas de lint recomendadas |
| `flutter_launcher_icons` | ^0.14.4 | Generación del icono de la app |
| `mocktail` | ^1.0.4 | Mocking en Dart |
| `http` | ^1.6.0 | Cliente HTTP (usado en tests) |

## Descargas

**Android:**
- [⬇️ Descargar DayPilot (Version-Original)](https://github.com/MarTonPerCar/DayPilot/releases/download/v0.1.1/DayPilot.apk)
- [⬇️ Descargar DayPilot (Incremento-Android)](https://github.com/MarTonPerCar/DayPilot/releases/download/incremento-android-latest/DayPilot-Incremento-Android.apk)

**Flutter (Windows/Linux):**
- [⬇️ Descargar DayPilot para Windows](https://github.com/MarTonPerCar/DayPilot/releases/download/incremento-flutter-latest/DayPilot-Setup.exe) — ejecuta el instalador tras descargarlo
- Linux, Debian/Ubuntu y derivadas (.deb):
  ```bash
  wget -O DayPilot-Setup.deb https://github.com/MarTonPerCar/DayPilot/releases/download/incremento-flutter-latest/DayPilot-Setup.deb && sudo apt install ./DayPilot-Setup.deb
  ```
- Linux, cualquier otra distribución (AppImage — no requiere instalación ni permisos de root):
  ```bash
  wget -O DayPilot.AppImage https://github.com/MarTonPerCar/DayPilot/releases/download/incremento-flutter-latest/DayPilot-x86_64.AppImage && chmod +x DayPilot.AppImage && ./DayPilot.AppImage
  ```

**iOS / macOS:** todavía no disponible — desarrollo futuro. Distribuir una build sin firmar no es viable en ninguna de las dos plataformas: macOS Gatekeeper bloquea las apps sin firmar para usuarios normales, y iOS no permite instalar un `.ipa` sin firmar fuera del despliegue propio de Xcode a dispositivo. Ambas necesitan una cuenta de Apple Developer para firmar y notarizar/distribuir correctamente.

## Páginas de autenticación (`docs/`)

Páginas estáticas a las que Supabase Auth redirige para la confirmación de email y el restablecimiento de contraseña (`docs/confirm.html`, `docs/reset-password.html`), además de las plantillas HTML de email configuradas en el panel de Supabase (`docs/emails/`). Se despliegan mediante GitHub Pages a través de `.github/workflows/deploy-pages.yml`, que inyecta `SUPABASE_URL`/`SUPABASE_KEY` desde los secrets del repositorio en `reset-password.html` en el momento del despliegue en lugar de comitearlos — requiere que el origen de Pages del repositorio esté configurado como "GitHub Actions" (Settings → Pages).
