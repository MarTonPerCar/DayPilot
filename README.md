# DayPilot — Rama `Test-Funcional-Android`

> **Estado:** En desarrollo activo  
> **Base:** `Test-Diseno-Android` (fase de diseño UI completada)  
> **Objetivo:** Implementar la lógica funcional completa de la aplicación

---

## ¿Qué es esta rama?

`desarrollo` es la rama donde DayPilot deja de ser una maqueta y se convierte en una aplicación real. Parte de `Test-Diseno-Android`, que dejó una UI completamente diseñada, 19 pantallas con navegación funcional y todos los modelos de datos definidos, pero con estado simulado mediante datos hardcodeados en el NavGraph.

En esta rama se implementa la arquitectura MVVM completa, con repositorios que en una primera fase devuelven datos falsos pero fluyen por la estructura correcta, de modo que cuando llegue la integración con Supabase, solo cambie la implementación del repositorio — sin tocar ViewModels ni pantallas.

---

## Punto de partida

La rama de diseño entregó:

- 19 pantallas Compose completamente diseñadas y navegables
- Todos los modelos de datos de UI (`TaskModels`, `ReminderModels`, `TechHealthModels`, etc.)
- Navegación completa con `NavGraph` y `BottomBar`
- Cadenas de texto externalizadas en ES / EN / DE
- Estado mutable centralizado en el NavGraph (patrón temporal, listo para migrar)

Las pantallas son **stateless**: reciben datos como parámetros y emiten eventos hacia arriba. Esto hace que la migración a MVVM sea quirúrgica — solo cambia la fuente de los datos, no el interior de las pantallas.

---

## Arquitectura objetivo

```
UI (Compose Screen)
      ↓  collectAsStateWithLifecycle()
ViewModel  (StateFlow<UiState>)
      ↓  suspend fun / Flow
Repository (interfaz)
      ↓
FakeRepository          →   en esta rama, datos mock coherentes
RealRepository          →   en la siguiente rama, llamadas a Supabase
```

El contrato entre ViewModel y repositorio es una **interfaz Kotlin**. La implementación concreta se puede intercambiar sin tocar nada por encima. Esta es la razón de separar las dos fases: la arquitectura queda validada con datos falsos antes de introducir la complejidad del backend.

---

## Plan de desarrollo

### Fase 1 — Arquitectura MVVM con repositorios falsos

Migrar la gestión de estado del NavGraph a ViewModels, conectados a repositorios que devuelven datos mock pero a través de la estructura correcta.

**Pasos:**

1. Añadir dependencias `lifecycle-viewmodel-compose` y `lifecycle-runtime-compose`
2. Definir interfaces de repositorio por dominio
3. Implementar `FakeRepository` por dominio con datos hardcodeados coherentes
4. Crear un ViewModel por dominio con `StateFlow` y una `UiState` como `data class`
5. Conectar cada pantalla mediante `collectAsStateWithLifecycle()`
6. El NavGraph pasa a gestionar solo navegación, sin ningún `var x by remember`

**Estructura objetivo:**

```
data/
├── repository/
│   ├── TaskRepository.kt           ← interfaz
│   ├── ReminderRepository.kt       ← interfaz
│   ├── TechHealthRepository.kt     ← interfaz
│   ├── ProgressRepository.kt       ← interfaz
│   ├── FriendRepository.kt         ← interfaz
│   └── fake/
│       ├── FakeTaskRepository.kt
│       ├── FakeReminderRepository.kt
│       ├── FakeTechHealthRepository.kt
│       ├── FakeProgressRepository.kt
│       └── FakeFriendRepository.kt
... (StepsRepository, RankingRepository, SettingsRepository, UserRepository y
     NotificationRepository, con sus respectivas Fake*, siguen el mismo patrón)
viewmodel/
├── calendar/
│   ├── CalendarViewModel.kt
│   └── CalendarUiState.kt
├── habits/
│   ├── HabitsViewModel.kt
│   └── HabitsUiState.kt
├── progress/
│   ├── ProgressViewModel.kt
│   └── ProgressUiState.kt
├── rivalry/
│   ├── RivalryViewModel.kt
│   └── RivalryUiState.kt
... (un ViewModel por pantalla o dominio funcional)
presentation/
├── calendar/CalendarScreen.kt      ← ya existe, solo cambia la firma
├── habits/HabitsScreen.kt
... (una carpeta por dominio, con las pantallas ya existentes)
```

**Criterio de finalización:** ningún estado de negocio en el NavGraph. Toda la app navega, responde a interacciones y persiste cambios durante la sesión a través de ViewModels y repositorios falsos.

---

### Fase 2 — Integración con Supabase (rama futura)

Una vez validada la arquitectura, se reemplaza cada `FakeRepository` por su implementación real que habla con Supabase. Los ViewModels y las pantallas no necesitan cambios.

---

## Features pendientes de la fase de diseño

Tres funcionalidades que quedaron sin implementar en la UI y que se resolverán en esta rama:

| Feature | Dónde | Momento |
|---|---|---|
| Gráfico de progreso real (30 días) | `ProgressScreen` | Al tener datos reales en Fase 2 |
| Notificaciones locales de recordatorios | `RemindersScreen` | Fase 1 (no requiere backend) |
| Control de idioma en tiempo de ejecución | `SettingsScreen` | Fase 1 (no requiere backend) |

---

## Lo que queda fuera de esta rama

- **Persistencia real en la nube** — Supabase va en la rama siguiente
- **Puerto iOS** — Incremento 3
- **Optimización y polish** — Incremento 4
- **Tareas recurrentes, hábitos personalizados, niveles de usuario** — segunda iteración funcional

---

## Convenciones de esta rama

- Un commit por feature completa, no por archivo
- Mensajes de commit en inglés, en una sola línea, sin cuerpo
- Nunca romper la navegación existente — los cambios son aditivos
- Cada ViewModel tiene su `UiState` (`data class`) definida antes de conectarlo a la pantalla
- Las pantallas no conocen los repositorios — solo hablan con el ViewModel

---

## Cómo se verá esta rama al terminar

Cuando `desarrollo` esté completa, DayPilot será una aplicación Android con arquitectura MVVM lista para producción, completamente navegable y funcional durante una sesión, donde todas las interacciones (crear tarea, activar recordatorio, cambiar límite de app) tienen efecto real en la UI aunque los datos no persistan entre arranques. El siguiente paso será sustituir los repositorios falsos por Supabase.
