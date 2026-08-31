# DayPilot — Especificaciones técnicas y estudio de costes

---

## 1. Hardware de desarrollo y validación

| Dispositivo | Especificaciones | Uso |
|---|---|---|
| Móvil Android | Oppo A78, ColorOS 15.0 | Pruebas funcionales, de estrés y aleatorias — Android |
| PC Windows (equipo 1) | Ryzen 5 5600, RTX 3060, 16 GB RAM | Validación Flutter — Windows |
| PC Windows (equipo 2) | Core i5-12400F, RTX 3060 Ti, 16 GB RAM | Validación Flutter — Windows (equipo prestado, ver §4.1) |
| PC Linux | GTX 1050, 16 GB RAM | Desarrollo y validación Flutter — Linux |

Máquina de desarrollo: Linux, exclusivamente. No se dispuso de máquina macOS.

Pruebas de estrés y aleatorias: ~15 minutos por configuración.

---

## 2. Especificaciones mínimas y compatibilidad

### 2.1 Android

| Requisito | Valor |
|---|---|
| Versión mínima | Android 8.0 Oreo (API 26) |
| Versión de compilación | API 36 |
| Permisos sensibles | Acceso a estadísticas de uso, Servicio de accesibilidad (opcionales, solo para Salud Tecnológica) |
| Conectividad | Internet obligatoria |

### 2.2 Escritorio

| Requisito | Valor |
|---|---|
| Sistemas soportados | Windows 10/11, Linux (`.deb`, `AppImage`) |
| macOS | No disponible |
| Empaquetado | Inno Setup (Windows), `.deb` / `AppImage` (Linux) |

### 2.3 Conectividad

Sin persistencia offline-first real en ninguna plataforma. Flutter incluye `ConnectivityService` / `offline_notifier` (detección de estado, sin cola de sincronización).

---

## 3. Stack tecnológico

### 3.1 Android

| Componente | Versión |
|---|---|
| Lenguaje | Kotlin 2.2.21 |
| UI | Jetpack Compose (Material 3) |
| Arquitectura | MVVM + Repository |
| JDK | Java 17 |
| Cliente Supabase | `supabase-kt` 3.3.0 |
| Cliente HTTP | `ktor-client-android` 3.3.0 |
| Persistencia local | DataStore / SharedPreferences |
| Sistema | WorkManager, `AccessibilityService`, `BroadcastReceiver` |

### 3.2 Flutter

| Componente | Versión |
|---|---|
| Framework | Flutter 3.44.4 |
| Lenguaje | Dart 3.12.2 |
| Estado | Riverpod (`Notifier` / `NotifierProvider`) |
| Cliente Supabase | `supabase_flutter` 2.15.4 |
| Escritorio | `window_manager`, `tray_manager`, `local_notifier` |
| Plataformas | Windows, Linux |

### 3.3 Backend — Supabase

| Elemento | Cantidad |
|---|---:|
| Tablas | 16 |
| Áreas funcionales | 6 |
| Funciones PL/pgSQL | 12 |
| Triggers | 8 |
| Vistas (`security_invoker`) | 2 |
| Políticas RLS | 31 |
| Tareas `pg_cron` | 4 |

### 3.4 Compatibilidad de versiones

`supabase-kt` 3.3.0 requiere Kotlin 2.2 o superior (dependencia de `kotlinx-serialization` 1.9.0). Kotlin 2.0.x / 2.1.x: error interno del compilador K2.

---

## 4. Estudio de costes del proyecto

*Estimación con fines ilustrativos. Ver §5 para el origen de cada cifra.*

### 4.1 Coste de hardware

| Elemento | Especificación | Coste | Incluido |
|---|---|---:|:---:|
| Móvil | Oppo A78 | 220 € | Sí |
| PC Linux | GTX 1050, 16 GB | 650 € | Sí |
| PC Windows | Ryzen 5 5600 + RTX 3060, 16 GB | 1.150 € | Sí |
| **Subtotal** | | **2.020 €** | |
| PC Windows (2º equipo) | i5-12400F + RTX 3060 Ti, 16 GB | 1.400 € | No — prestado |

### 4.2 Coste de desarrollo

Tarifa: **20 €/hora** (coste empresa).

**Por fase**

| Fase | Horas | Coste |
|---|---:|---:|
| Planificación Inicial | 30 h | 600 € |
| Incremento 1 — Reingeniería arquitectónica | 80 h | 1.600 € |
| Incremento 2 — Integración cloud y sincronización | 80 h | 1.600 € |
| Incremento 3 — Versión multiplataforma | 60 h | 1.200 € |
| Incremento 4 — Optimización, calidad y validación técnica | 30 h | 600 € |
| Documentación y presentación | 20 h | 400 € |
| **Total** | **300 h** | **6.000 €** |

**Por tipo de tarea**

| Tipo de tarea | Horas | Coste | % |
|---|---:|---:|---:|
| Análisis | 50 h | 1.000 € | 16,7 % |
| Diseño | 45 h | 900 € | 15,0 % |
| Preparación del entorno | 5 h | 100 € | 1,7 % |
| Implementación | 155 h | 3.100 € | 51,7 % |
| Pruebas | 25 h | 500 € | 8,3 % |
| Documentación y presentación | 20 h | 400 € | 6,7 % |
| **Total** | **300 h** | **6.000 €** | **100 %** |

### 4.3 Coste total

| Escenario | Total |
|---|---:|
| Coste estimado | **8.020 €** |
| Coste estimado + 2º PC Windows | **9.420 €** |

---
**Tarifa de desarrollo (20 €/h)**: a partir de salario junior-mid de desarrollo móvil en España 2026 (25.000-30.000 €/año brutos), + ~30,7 % de carga social, ÷ ~1.800 h/año.
