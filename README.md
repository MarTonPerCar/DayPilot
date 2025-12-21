# DayPilot

---

## Introducción
**DayPilot** es una aplicación Android enfocada en la organización y en la motivación diaria. Combina un sistema de gestión clasico, como un calendario, tareas o la gestión de hábitos, con un sistema de basado en competición sana con una base de puntos, buscando que el usuario mantenga constancia y desarrolle una rutina.

---

## Objetivos
- Ofrecer una app **simple y rápida** para el día a día.
- Integrar un sistema de **seguimiento y motivación** mediante actividades y obteción de puntos.
- Utilización de una base de datos (Firebase) para la creación de un usuario y guardado de los datos sobre 30 dias 
- Diseñar el sistema en una estructura facil e intuitiba para todos los usuarios 
---

## Diseño
El diseño está planteado en una estructura de 3 secciones divididas por importancias:

- **NavigationBar inferior persistente**: La cual permite moverse entre secciones secundarias en 1 toque, englobando el perfil, las configuraciones y el añadido de amigos.

Dentro de las secciones del menu principal
- **Botones pequeños puestos abajo**: Apartados de importancia media, tales como el apartado de rivalidad y de creación de tareas.
- **Botones grandes puestos arriba**: Los apartados más importantes de la aplicación, teniendo el calendario que permite ver las tareas y todos los apartados respecto a los habitos

En resumen: el diseño prioriza **rapidez**, **claridad** y **mínima fricción** al navegar.

---

## Arquitectura
La arquitectura se basa en separar responsabilidades para que el proyecto sea manejable. Se apoya en dos ideas principales:

### 1) Persistencia de datos: Local + Firebase
La app usa **dos capas de datos**, cada una con un objetivo claro:

- **Local (almacenamiento en el dispositivo)**
    - Guardar datos necesarios para inicio de secciones y configuraciones.
    - Mejorar rendimiento: lecturas rápidas y funcionamiento estable.

- **Firebase (cloud / sincronización)**
    - Guardar datos asociados a la cuenta del usuario.
    - Mantener el progreso persistente entre dispositivos.
    - Gestionar autenticación e información de perfil.
    - Sincronizar puntos/logros o datos relevantes cuando corresponde.
    - Permitir el sistema de amigos

Esto permite una experiencia fluida (local) y a la vez segura y persistente (Firebase). 

### 2) Organización de interfaz: Activity + Screen + módulos de apoyo
La UI se organiza de forma que sea fácil construir pantallas complejas sin crear “pantallas gigantes” imposibles de mantener:

- **Activity (contenedor / entrada de sección)**
    - Punto de entrada a una sección concreta.
    - Configura navegación, dependencias básicas, y decide qué pantalla se muestra.
    - Actúa como “marco” de la funcionalidad.

- **Screen (UI principal en Compose)**
    - Es el componente que dibuja la interfaz y gestiona estados visuales.
    - Aquí vive la lógica de presentación: estados, eventos de UI, composición.

- **Componentes / secciones auxiliares (extras de Screen)**
    - Subcomponentes reutilizables: tarjetas, listas, formularios, diálogos, etc.
    - Evita duplicación y mantiene el código limpio.

**Por qué esta decisión:**
- Separar Activity (estructura) y Screen (UI) ayuda a mantener el proyecto ordenado.
- Los componentes auxiliares permiten reutilizar y escalar funcionalidades sin duplicar código.
- Facilita testear, depurar y evolucionar cada parte por separado.

---

## Funcionalidades

- **Autenticación de usuario** (Firebase Auth): registro, inicio de sesión, recuperación de contraseña y cierre de sesión.
- **Perfil de usuario**: Muestra varios apartados basicos (nombre, correo, fecha de creación, etc.), así como una tarjeta que muestra la información de los puntos obtenidos.
- **Seguimiento de actividad**: registro local de pasos y cálculo de progreso/puntos.
- **Sistema de puntos / progresión**: conversión de hitos de actividad en recompensas en forma de puntos.
- **Rivalidad con amigos**: utilización de los puntos para competir con los amigos 
- **Configuración**: modo oscuro, notificaciones, idioma (no implementado aunque muestra la opción).
- **Navegación por secciones**: menú inferior fijo para moverse entre apartados principales.
- **Base para ampliación**: estructura preparada para añadir hábitos, tareas o incluso secciones enteras.

---

## CI/CD: APK automático + Releases en GitHub
Este repositorio incluye un workflow de GitHub Actions que:
1. Compila la app en cada push a `master` y en cada Pull Request.
2. Sube el APK generado como **artifact**.
3. Si el push es un **tag v*** (por ejemplo `v1.0.0`), además crea un **GitHub Release** y adjunta el APK.

### Workflow usado
El archivo de workflow compila un **Debug APK**, lo renombra a `DayPilot.apk` y lo publica.

### Secret necesario (google-services.json)
Para evitar subir credenciales al repositorio, `google-services.json` **no se versiona**. En su lugar se almacena como un **Secret** de GitHub en Base64

---

## Versión final 

Aquí se da un enlace con la aplicación para poder ser descargada con la ultima versión

[⬇️ Descargar DayPilot](https://github.com/MarTonPerCar/DayPilot/releases/latest/download/DayPilot.apk)