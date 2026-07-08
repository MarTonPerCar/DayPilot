# Incremento 1

## Índice

1. [Funcionalidades implementadas](#1-funcionalidades-implementadas)
2. [Deuda técnica resuelta](#2-deuda-técnica-resuelta)
3. [Funcionalidades en desarrollo](#3-funcionalidades-en-desarrollo)
4. [Final del incremento](#4-final-del-incremento)

## 1. Funcionalidades implementadas

### Gestión de tareas y calendario

El usuario puede crear, editar y eliminar tareas asignándoles título, dificultad, categoría, duración estimada y días asignados. Las tareas se presentan en un listado visual con filtros por dificultad y categoría, y pueden ordenarse por estado, fecha, nombre, duración o proximidad. Complementando el listado, existe un calendario mensual donde se visualizan todas las tareas de forma organizada por día, permitiendo consultar sus detalles, marcarlas como completadas y añadir nuevas directamente desde la vista de calendario.

### Sistema de puntos

El sistema de puntos actúa como motor de motivación transversal a toda la aplicación. El usuario obtiene 2 puntos por cada tarea completada, independientemente de su dificultad, incentivando la organización como hábito. Por el contador de pasos se pueden obtener hasta 6 puntos diarios, conseguidos progresivamente al alcanzar el 50%, 75% y 100% de la meta diaria establecida. Para equilibrar la competencia entre usuarios que empezaron en momentos distintos, los puntos se mantienen activos durante una ventana de exactamente 30 días, de modo que el ranking refleja el esfuerzo reciente y no la antigüedad en la aplicación.

### Hábitos

La sección de hábitos agrupa tres subsecciones diferenciadas que cubren aspectos distintos del bienestar diario.

El **contador de pasos** permite al usuario establecer una meta diaria configurable. El progreso se guarda en local y se sincroniza con la base de datos al alcanzar cada uno de los hitos establecidos (50%, 75%, 100%). La meta es modificable pero solo aplicable al día siguiente, evitando que el usuario la ajuste para obtener puntos de forma artificial.

Los **recordatorios** permiten configurar alarmas para días y horas específicas, con opción de repetición diaria o semanal y aviso anticipado configurable antes de la hora señalada.

La **salud tecnológica** permite establecer restricciones de uso sobre aplicaciones concretas del dispositivo. Cuando el usuario supera la cuota diaria, el sistema de accesibilidad bloquea la app mostrando una pantalla de aviso. Las aplicaciones pueden restringirse de forma individual o agrupadas, con cuotas independientes por grupo. Las restricciones eliminadas no se borran de inmediato sino que entran en un estado de eliminación diferida, aplicándose al día siguiente para no interrumpir el uso en curso.

### Social y rivalidad

La vertiente social se articula en torno a un sistema de amistades y competencia por puntos. El usuario puede enviar y aceptar solicitudes de amistad, y una vez conectados acceder a la sección de rivalidad, donde se muestra un ranking de puntos de los últimos 30 días entre todos sus amigos. El ranking se presenta con un pódium para los tres primeros puestos y un listado para el resto. Desde el perfil de cada amigo es posible consultar su progreso individual, viendo sus puntos y el origen de los mismos.

### Perfil y configuración

El perfil del usuario centraliza su información personal junto con un desglose del progreso de puntos de los últimos 30 días, mostrando de qué actividades provienen. La configuración ofrece opciones de modo oscuro, activación de notificaciones, idioma y región, y selección de tema de color de la interfaz.

### Infraestructura

La aplicación cuenta con un pipeline CI/CD mediante GitHub Actions que automatiza la construcción de APKs y la publicación de releases. Se emplea persistencia local para mantener la sesión, las preferencias del usuario y la caché de datos sin depender de conexión a red.

---

## 2. Deuda técnica resuelta

El análisis del código de la primera iteración identificó un conjunto de problemas estructurales que se abordan de forma prioritaria antes de incorporar nuevas funcionalidades. Estos no son errores puntuales sino decisiones de diseño acumuladas que dificultaban el mantenimiento y la expansión del proyecto.

### Arquitectura uniforme MVVM

La primera iteración mezclaba dos enfoques sin consistencia: algunos módulos usaban ViewModel con StateFlow correctamente, mientras que otros realizaban llamadas a repositorios directamente desde las vistas mediante corrutinas. Esta inconsistencia se resuelve adoptando el patrón MVVM de forma uniforme en toda la aplicación. Cada pantalla tiene ahora su ViewModel correspondiente y ninguna vista contiene lógica de negocio ni acceso directo a datos.

### Inyección de dependencias

Los repositorios se instanciaban directamente en cada pantalla sin ningún sistema centralizado. Esto impedía compartir instancias entre pantallas y hacía el testing inviable. Se introduce un contenedor de dependencias que centraliza la creación y el ciclo de vida de los repositorios, permitiendo su reutilización y sustitución para pruebas.

### Navegación centralizada

La navegación entre pantallas se realizaba mediante Intents con strings hardcodeados como claves de extras, sin un grafo de navegación centralizado. Se reemplaza por Navigation Component con rutas tipadas, gestión automática de la pila de retroceso y paso de argumentos con seguridad de tipos.

### Lógica de hitos de pasos duplicada

La detección de si el usuario alcanzaba el 50%, 75% o 100% de su meta de pasos estaba duplicada en dos componentes distintos, lo que podía provocar en determinadas condiciones que el usuario recibiese puntos dobles por el mismo hito. Esta lógica se centraliza en un único punto, garantizando que cada hito se registra exactamente una vez.

### Persistencia local unificada

Coexistían tres sistemas de persistencia local simultáneos: SharedPreferences para preferencias generales, DataStore en algunos módulos y una caché adicional dentro del módulo de pasos. Además, los almacenes de restricciones de salud tecnológica y recordatorios usaban serialización manual con strings delimitados por el carácter `|`, un sistema frágil ante cualquier cambio de modelo. Todo se unifica bajo DataStore con serialización JSON tipada.

### Gestión de sesión unificada

Coexistían dos sistemas de sesión en paralelo sin garantía de sincronización entre ambos, con un componente adicional que intentaba reconciliarlos en cada arranque de la aplicación. Se unifica la gestión de sesión en un único punto coherente con el SDK de autenticación utilizado.

### Datos de usuario desactualizados en amistades

El nombre y el username del amigo se almacenaban duplicados dentro del registro de la relación de amistad. Si un usuario modificaba su nombre, los registros de amistad quedaban desactualizados sin ningún mecanismo de propagación. Se refactoriza para que las relaciones referencien al usuario por identificador, obteniendo siempre el nombre del registro actualizado.

---

## 3. Funcionalidades en desarrollo

### Cronómetros y temporizadores

Se incorpora un módulo de cronómetros accesible desde la sección de hábitos que permite al usuario realizar actividades temporizadas de forma guiada. Las modalidades disponibles son el método Pomodoro — con fases de trabajo de 25 minutos y descanso de 5 minutos, número de sesiones configurable y posibilidad de saltar fase — y modos predefinidos para entrenamiento, meditación y cocina. Existe también un modo personalizado con duración libre entre 5 y 180 minutos. La finalización de cada sesión otorga puntos al usuario, añadiendo una nueva fuente al sistema de puntuación.

### Tareas recurrentes

La gestión de tareas se amplía con soporte para recurrencia. Al crear o editar una tarea, el usuario puede indicar que se repita automáticamente cada cierto número de días, evitando la creación manual repetitiva. Esto resulta especialmente útil para hábitos periódicos como repasar apuntes o realizar ejercicio en días concretos de la semana.

### Resumen diario al abrir la aplicación

Al acceder a la pantalla principal se mostrará una tarjeta de resumen diario con las tareas pendientes del día, el progreso de pasos respecto a la meta y la posición actual en el ranking de amigos. Esta tarjeta proporciona de un vistazo el estado del día sin necesidad de navegar entre secciones.

### Sistema de niveles

Se implementará un sistema de niveles basado en los puntos acumulados históricamente, de forma independiente al ciclo de 30 días que rige el ranking de rivalidad. Cada nivel representa un umbral de puntos totales y se mostrará en el perfil y en la tabla de rivalidad junto al nombre del usuario, proporcionando una sensación de progresión a largo plazo que complementa la competencia periódica con amigos.

### Rachas diarias

Se añadirán rachas diarias visibles en el perfil, reflejando cuántos días consecutivos el usuario ha cumplido al menos una de sus metas. La racha se mostrará también en el pódium de rivalidad como indicador secundario junto a los puntos, añadiendo un elemento de constancia a la competencia.

### Reacciones al progreso de amigos

Los usuarios podrán reaccionar con un emoji al resumen semanal de sus amigos. Cada usuario puede reaccionar una vez por resumen; la reacción llega al destinatario como notificación y se muestra en su pantalla de perfil.

---

## 4. Final del incremento

**Estado:** Completado.

El incremento se cerró con todas las funcionalidades planificadas implementadas y revisadas. A continuación se resume qué cambios tuvieron que realizarse durante el proceso de testing y revisión final:

### Cambios introducidos durante el testing

**Bloqueo real de apps (AccessibilityService)**
El planteamiento inicial contemplaba notificaciones periódicas cuando el usuario superaba el límite. Durante el testing se comprobó que este enfoque no era efectivo: las notificaciones eran ignorables y no cambiaban el comportamiento del usuario. Se sustituyó por un bloqueo efectivo mediante `DayPilotAccessibilityService` que lanza una pantalla de bloqueo (`TechHealthBlockActivity`) a pantalla completa, sin posibilidad de usar el botón de atrás y que obliga al usuario a volver al inicio del sistema.

**Pasarela de permisos a pantalla completa**
La pantalla de salud tecnológica mostraba un aviso inline cuando los permisos no estaban concedidos, pero el usuario podía ignorarlo y seguir viendo el resto de la pantalla. Se rediseñó como una pasarela que ocupa toda la pantalla con instrucciones paso a paso (incluyendo texto en negrita y los pasos exactos para cada permiso) y no permite avanzar hasta que ambos permisos están concedidos.

**Eliminación del sistema de notificaciones de Tech Health**
Las notificaciones periódicas de salud tecnológica ("llevas X minutos en esta app") se eliminaron completamente. El bloqueo activo por AccessibilityService hace innecesario avisar al usuario: o no ha llegado al límite y puede usar la app, o lo ha superado y la app se cierra. Las notificaciones en este contexto solo generaban ruido.

**Corrección de bugs identificados en revisión de código**

| Severidad | Descripción |
|---|---|
| Alta | `SupabaseTaskRepository`: `updateTask`, `toggleTask` y `deleteTask` no invalidaban `SessionCache.tasks`, devolviendo datos obsoletos tras cualquier modificación |
| Alta | `ProfileViewModel`: `isUploadingAvatar` quedaba `true` permanentemente si la recarga de perfil fallaba silenciosamente tras una subida exitosa de foto |
| Media | `AppLimitFormCard`: `name.first()` lanzaba excepción con nombres de app vacíos |
| Media | `DayPilotAccessibilityService`: el repositorio se instanciaba en cada evento de accesibilidad (hilo principal), con riesgo de ANR en cambios de app rápidos |
| Media | `TechHealthBlockActivity`: siempre renderizaba el tema SAGE_GREEN ignorando la preferencia del usuario |

### Rama de continuación

Esta rama (`Incremento-Android`) queda como base para el siguiente incremento.

## Convenciones de esta rama

- Commits por bloque funcional, no por archivo
- Las pantallas no conocen los repositorios — solo hablan con el ViewModel
- `SessionCache` es la única fuente de verdad en memoria; la BD es la fuente de verdad persistente

---

## Descargas

- [⬇️ Descargar DayPilot (Incremento-Android)](https://github.com/MarTonPerCar/DayPilot/releases/download/incremento-android-v1.0.1/DayPilot-Incremento-Android.apk)
