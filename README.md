# DayPilot — Test de Diseño Android

Esta rama es el espacio de prototipado y pruebas visuales de la interfaz de DayPilot en Android. Aquí se desarrollan y validan componentes de UI con Jetpack Compose antes de integrarlos en la rama principal.

---

## Objetivo

Probar el sistema de diseño de la aplicación de forma aislada: paletas de color, tipografía, componentes reutilizables y pantallas completas, sin depender del backend ni de datos reales.

---

## Sistema de diseño

### Temas de color
La app cuenta con 5 temas predefinidos, cada uno con versión clara y oscura:
- **Verde salvia** (default)
- 4 temas adicionales por definir

El tema se aplica de forma reactiva mediante `StateFlow` a nivel de aplicación, sin necesidad de reiniciar la app.

### Tipografía
Escala tipográfica completa definida en Jetpack Compose Material 3, cubriendo todos los niveles: `displayLarge` hasta `labelSmall`.

---

## Pantallas y componentes a prototipar

### Navegación
- Bottom navigation bar con 4 tabs: Inicio, Amigos, Notificaciones, Perfil

### Inicio
- Card de resumen diario (pasos, tareas, puntos, racha, posición en ranking)
- Menú de acceso rápido con iconos

### Calendario
- Vista mensual con tareas por día
- Detalle de tarea (crear / editar / completar)
- Filtros por categoría y dificultad

### Hábitos
- Card de pasos con barra de progreso y hitos
- Card de salud tecnológica
- Sección de cronómetros (Pomodoro, Entreno, Meditación, Cocina, Personalizable)
- Card de recordatorios

### Amigos
- Lista de amigos con racha y resumen desplegable
- Tab de solicitudes pendientes
- Buscador por nombre o correo
- Ranking de amigos

### Notificaciones
- Historial de notificaciones con categorías
- Configuración por categoría

### Perfil
- Info personal, nivel, puntos históricos, racha
- Edición de nombre, username y foto

### Configuración
- Selector de tema de color
- Toggle de modo oscuro
- Selector de idioma (ES / EN / DE)
- Selector de región

---

## Estructura del proyecto de pruebas
app/
├── ui/
│   ├── theme/
│   │   ├── Color.kt        ← paletas de los 5 temas
│   │   ├── Typography.kt   ← escala tipográfica completa
│   │   └── Theme.kt        ← aplicación reactiva del tema
│   ├── components/         ← componentes reutilizables
│   └── screens/            ← prototipos de pantallas completas
└── MainActivity.kt         ← entrada con navegación de prueba
---

## Estado

- [ ] Sistema de temas (5 paletas, claro/oscuro)
- [ ] Escala tipográfica
- [ ] Bottom navigation bar
- [ ] Card resumen diario
- [ ] Pantalla Inicio
- [ ] Pantalla Calendario
- [ ] Pantalla Hábitos + Cronómetros
- [ ] Pantalla Amigos + Ranking
- [ ] Pantalla Perfil
- [ ] Pantalla Configuración
