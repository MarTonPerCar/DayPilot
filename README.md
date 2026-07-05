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

```bash
flutter run -d linux \
  --dart-define=SUPABASE_URL=https://xxxx.supabase.co \
  --dart-define=SUPABASE_KEY=sb_publishable_xxxx
```

O el script de verificación sin UI:

```bash
dart run tool/verify_connection.dart
```

## Base de datos

El esquema real vive en la rama `Informacion-Supabase` (no en este repo). Ver esa rama para las migraciones (`01_schema_v004.sql`, `02_drop_all.sql`, `03_seed.sql`).

## Siguiente paso

Los resultados de esta rama alimentan `Incremento-Flutter-TestFinal`: repositorios reales de Supabase, uno por dominio, sustituyendo a los fakes de `Test-Funcional-Flutter`.
