# DayPilot — Test Supabase (Flutter)

Proyecto mínimo e independiente que valida la conexión entre Flutter y Supabase antes de integrarla en `Incremento-Flutter-TestFinal`. Equivalente en Flutter de la rama `Test-Supabase-Android`.

## Por qué existe

- Confirmar que `supabase_flutter` se conecta correctamente al mismo proyecto de Supabase que ya usa la app Android.
- Validar login, SELECT e INSERT contra el esquema real (`Informacion-Supabase`, `01_schema_v004.sql`).
- Fijar qué versiones de Flutter/Dart/supabase_flutter funcionan juntas.

> **Conclusión:** la conexión funciona. Verificado tanto desde la UI (`flutter build linux`) como desde un script Dart puro (`tool/verify_connection.dart`) contra la base de datos real.

## Versiones validadas

| Componente | Versión |
|---|---|
| Flutter | 3.44.4 (stable) |
| Dart | 3.12.2 |
| supabase_flutter | 2.15.4 |
| supabase (core, usado por el script de verificación) | 2.13.4 |

## Qué se probó y funcionó

Con los usuarios de la seed (`ana.garcia@daypilot.test` / `password123`):

- ✅ Login con email/contraseña.
- ✅ SELECT de `tasks` (respeta RLS, solo devuelve las propias).
- ✅ INSERT de una tarea nueva asociada al `user_id` del usuario logueado.

## Cómo ejecutarlo

Copia `env.json.example` a `env.json` (ignorado por git) y rellena tus credenciales reales de Supabase. Sin este paso la app arranca con `SUPABASE_URL`/`SUPABASE_KEY` vacíos y no conecta.

```bash
cp env.json.example env.json   # solo la primera vez, luego edita env.json
flutter run -d linux
```

(cambia `-d linux` por `-d chrome`, `-d <dispositivo-android>`, etc. según dónde quieras probarlo)

O el script de verificación sin UI (tiene las credenciales de prueba embebidas, no depende de env.json):

```bash
dart run tool/verify_connection.dart
```

## Descargas

Cada tag `vX.Y.Z` dispara `.github/workflows/release.yml`, que compila la app para Linux, Windows, macOS, Android e iOS y las publica todas juntas en un mismo [GitHub Release](https://github.com/MarTonPerCar/DayPilot/releases). Flutter no genera un binario universal — cada plataforma necesita su propio build — pero al usar releases todas las versiones quedan en el mismo sitio, con la última siempre arriba.

| Plataforma | Archivo | Instalación |
|---|---|---|
| Linux | `test_supabase_flutter-linux.tar.gz` | Descomprimir y ejecutar `test_supabase_flutter` |
| Windows | `test_supabase_flutter-windows.zip` | Descomprimir y ejecutar `test_supabase_flutter.exe` |
| macOS | `test_supabase_flutter-macos.zip` | Descomprimir y abrir `test_supabase_flutter.app` (clic derecho → Abrir, al no estar notarizado) |
| Android | `test_supabase_flutter-android.apk` | Instalar el APK (permitir orígenes desconocidos) |
| iOS | `test_supabase_flutter-ios-unsigned.zip` | **Sin firmar**: no se puede instalar directamente en un iPhone. Sirve para Simulator o para volver a firmar con tu propia cuenta de Apple Developer en Xcode |

Estos builds usan las credenciales de ejemplo de `env.json.example` (clave publishable/anon de Supabase, pensada para exponerse en cliente y protegida por RLS), no tus credenciales locales de `env.json`.

