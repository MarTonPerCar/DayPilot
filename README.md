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

### test-supabase-flutter-v0.1.0

- [Linux](https://github.com/MarTonPerCar/DayPilot/releases/download/test-supabase-flutter-v0.1.0/test_supabase_flutter-linux.tar.gz)
- [Windows](https://github.com/MarTonPerCar/DayPilot/releases/download/test-supabase-flutter-v0.1.0/test_supabase_flutter-windows.zip)
- [macOS](https://github.com/MarTonPerCar/DayPilot/releases/download/test-supabase-flutter-v0.1.0/test_supabase_flutter-macos.zip)
- [Android](https://github.com/MarTonPerCar/DayPilot/releases/download/test-supabase-flutter-v0.1.0/test_supabase_flutter-android.apk)

