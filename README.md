# 1. Video de la Fase 1
## https://drive.google.com/file/d/1AnxscDC0ic7q_0gauCTvphjrap1pK4Ok/view

# 2. Repositorio de GitHub
# Fithub360

Enlace video (público y accesible): https://TU_ENLACE_PUBLICO_DEL_VIDEO

## Descripción
- Propósito: Administrar nuestra rutina de ejercicios.
- Funcionalidades clave:
  - Registro/inicio de sesión
  - Rutinas, nutrición, música, coach, logros
  - Navegación inferior (BottomNavigationView)

## Arquitectura
- Lenguajes: Java/Kotlin
- Módulos/Capas:
  - `activities/` (`MainActivity`, `LoginActivity`, etc.)
  - `fragments/` (`RoutineFragment`, `NutritionFragment`, etc.)
  - `utils/` (`SessionManager`)
  - `data/` (APIs/DB si aplica)

## Requisitos
- Android Studio `Narwhal 3 Feature Drop | 2025.1.3`
- JDK 17 (o el que uses)
- Android SDK `compileSdkVersion` = [XX], `minSdk` = [XX]
- Gradle [X.Y], AGP [X.Y]
- Dependencias:
  - Material Components [versión]
  - Retrofit/OkHttp [opcional]
  - Glide/Coil [opcional]
  - Firebase [opcional]
  - n8n (self-host o Cloud) para webhooks

## Instalación
```bash
git clone https://github.com/CarloRH/Proyecto-En-Parejas.git
cd FitHub360
```

# 3. Flujo de n8n (archivo .json)
```bash
{
  "name": "FitHub360 Email Sender",
  "active": true,
  "nodes": [
    {
      "parameters": {
        "path": "fitHub360-send-email",
        "responseMode": "onReceived",
        "responseCode": "200",
        "responseBinaryData": false,
        "responseContentType": "application/json"
      },
      "name": "FitHub360 Email Trigger",
      "type": "n8n-nodes-base.webhook",
      "typeVersion": 1,
      "position": [
        250,
        300
      ]
    },
    {
      "parameters": {
        "conditions": {
          "boolean": [
            {
              "value1": "={{ $json.subject }}",
              "operation": "contains",
              "value2": "Coach"
            }
          ]
        }
      },
      "name": "Check Subject Type",
      "type": "n8n-nodes-base.if",
      "typeVersion": 1,
      "position": [
        450,
        300
      ]
    },
    {
      "parameters": {
        "authType": "accessToken",
        "sendMethod": "send",
        "toEmail": "={{ $json.email }}",
        "subject": "={{ $json.subject }} - FitHub360",
        "body": "={{ $json.content }}"
      },
      "name": "Send Coach Email",
      "type": "n8n-nodes-base.gmail",
      "typeVersion": 1,
      "position": [
        650,
        200
      ],
      "credentials": {
        "gmailOAuth2": "gmail-credentials" // ⚠️ Reemplaza por tu ID de credenciales
      }
    },
    {
      "parameters": {
        "authType": "accessToken",
        "sendMethod": "send",
        "toEmail": "={{ $json.email }}",
        "subject": "={{ $json.subject }} - FitHub360",
        "body": "={{ $json.content }}"
      },
      "name": "Send Nutrition Email",
      "type": "n8n-nodes-base.gmail",
      "typeVersion": 1,
      "position": [
        650,
        400
      ],
      "credentials": {
        "gmailOAuth2": "gmail-credentials" // ⚠️ Reemplaza por tu ID de credenciales
      }
    },
    {
      "parameters": {
        "responseCode": "200",
        "responseData": "noData"
      },
      "name": "Finish Workflow",
      "type": "n8n-nodes-base.response",
      "typeVersion": 1,
      "position": [
        850,
        300
      ]
    }
  ],
  "connections": {
    "FitHub360 Email Trigger": {
      "main": [
        [
          {
            "node": "Check Subject Type",
            "type": "main",
            "index": 0
          }
        ]
      ]
    },
    "Check Subject Type": {
      "main": [
        [
          {
            "node": "Send Coach Email",
            "type": "main",
            "index": 0
          }
        ],
        [
          {
            "node": "Send Nutrition Email",
            "type": "main",
            "index": 0
          }
        ]
      ]
    },
    "Send Coach Email": {
      "main": [
        [
          {
            "node": "Finish Workflow",
            "type": "main",
            "index": 0
          }
        ]
      ]
    },
    "Send Nutrition Email": {
      "main": [
        [
          {
            "node": "Finish Workflow",
            "type": "main",
            "index": 0
          }
        ]
      ]
    }
  }
}
```
# 4. Documento de Propuesta
# Documentación de interacción con IA (Copilot/Gemini)

- Proyecto: fithub360
- Objetivo general: Corregir errores de compilación/ejecución, asegurar que la app compile sin fallos y documentar el apoyo de IA.
- Entorno:
  - IDE: Android Studio Narwhal 3 Feature Drop | 2025.1.3
  - SO: Windows
  - Lenguajes: Java/Kotlin/Gradle
  - SDK/Build tools: [por confirmar: Android SDK/AGP/Gradle Wrapper]
- Ramas/commit relevantes: main, [commit hash: por confirmar]

## 1. Prompts utilizados con GitHub Copilot o Google Gemini

- ID del prompt: P1  
  Fecha y hora: 2025-10-26 10:05  
  Herramienta: GitHub Copilot  
  Objetivo del prompt: Continuar con la asistencia previa  
  Contexto técnico breve:
  - Archivos implicados: `app/src/main/java/com/example/fithub360/activities/MainActivity.java`
  - Estado previo: compila/estado incierto; no se adjuntaron logs
  Prompt exacto enviado: "Continue"  
  Restricciones o criterios de aceptación: Mantener contexto y no introducir cambios destructivos.

- ID del prompt: P2  
  Fecha y hora: 2025-10-26 10:12  
  Herramienta: GitHub Copilot  
  Objetivo del prompt: Corregir error y compilar para asegurar cero errores  
  Contexto técnico breve:
  - Archivos implicados: `app/src/main/java/com/example/fithub360/activities/MainActivity.java`, `app/src/main/res/layout/activity_main.xml`
  - Estado previo: error reportado; sin stack trace adjunto
  Prompt exacto enviado: "Se presento nuevamente un error, corrigelo y compila para asegurar que no hayan errores."  
  Restricciones o criterios de aceptación: Debe compilar, no romper flujo de sesión, no cambiar API pública.

- ID del prompt: P3  
  Fecha y hora: 2025-10-26 10:20  
  Herramienta: GitHub Copilot  
  Objetivo del prompt: Corregir error de compilación  
  Contexto técnico breve:
  - Archivos implicados: `app/src/main/java/com/example/fithub360/activities/MainActivity.java`
  - Estado previo: error persiste; sin logs adjuntos
  Prompt exacto enviado: "Corrige el error y compila."  
  Restricciones o criterios de aceptación: Compilación limpia, sin cambios funcionales no necesarios.

- ID del prompt: P4  
  Fecha y hora: 2025-10-26 10:28  
  Herramienta: GitHub Copilot  
  Objetivo del prompt: Encontrar una manera alternativa de corregir el error persistente  
  Contexto técnico breve:
  - Archivos implicados: `app/src/main/java/com/example/fithub360/activities/MainActivity.java`, `app/src/main/res/layout/activity_main.xml`, `app/src/main/res/menu/bottom_navigation_menu.xml`
  - Estado previo: error persiste; sin logs adjuntos
  Prompt exacto enviado: "Nuevamente persiste el error, encuentra la manera de corregirlo."  
  Restricciones o criterios de aceptación: Cambios mínimos, verificables y reversibles.

- ID del prompt: P5  
  Fecha y hora: 2025-10-26 10:40  
  Herramienta: GitHub Copilot  
  Objetivo del prompt: Generar plantilla de `documentacion.md` con interacciones IA  
  Contexto técnico breve:
  - Archivos implicados: `documentacion.md` (nuevo)
  - Estado previo: sin documentación
  Prompt exacto enviado: "Un archivo documentacion.md que incluya: ○ Prompts utilizados con GitHub Copilot o Google Gemini. ○ Resultados obtenidos de cada prompt (código generado, errores, mejoras, etc.). ○ Problemas encontrados y cómo se resolvieron con IA. ○ Reflexión final sobre la experiencia de usar herramientas de IA en el desarrollo móvil. Dame los datos que debo de incluir en cada uno de estos"  
  Restricciones o criterios de aceptación: Estructura clara, reusable por el equipo.

## 2. Resultados obtenidos de cada prompt

- P1  
  Resumen de la respuesta de la IA: Solicitó más contexto/logs y enumeró verificaciones iniciales (sin cambios de código).  
  Cambios propuestos: N/A  
  Acciones ejecutadas: Revisión manual del archivo `MainActivity.java`.  
  Resultado de compilación/ejecución: N/A  
  Errores observados: N/A  
  Decisiones tomadas: Aplazar cambios hasta contar con logs.  
  Impacto: Alineación sobre la necesidad de evidencia (logs/build output).

- P2  
  Resumen de la respuesta de la IA: Propuso verificar coherencia de IDs de layout y el uso del listener correcto para `BottomNavigationView`.  
  Cambios propuestos:
  - Archivos: `app/src/main/res/layout/activity_main.xml` [verificar/ajustar IDs `bottom_navigation` y `nav_host_fragment`], `app/src/main/java/com/example/fithub360/activities/MainActivity.java` [verificar listener y transacciones]
  - Fragmentos clave generados: N/A (sugerencias de ajuste, no se adjuntó diff)
  Acciones ejecutadas:
  - Comandos/Build/Tests: `gradlew.bat assembleDebug`
  - Resultado: [por confirmar; sin logs adjuntos]
  - Errores observados: [no provistos]
  Decisiones tomadas: Aceptar verificación de layout antes de refactors mayores.  
  Impacto: Reducir hipótesis; enfocar en causas comunes.

- P3  
  Resumen de la respuesta de la IA: Sugirió pasos de saneamiento y compilación limpia.  
  Cambios propuestos:
  - Acciones: Clean/Rebuild, invalidar cachés, revisar imports y namespaces de fragments.  
  Acciones ejecutadas:
  - Comandos: `gradlew.bat clean assembleDebug`
  - Resultado: [por confirmar]  
  - Errores observados: [no provistos]
  Decisiones tomadas: Mantener cambios mínimos hasta tener stack trace.  
  Impacto: Asegurar entorno coherente para depuración.

- P4  
  Resumen de la respuesta de la IA: Ofreció alternativas: validar existencia del contenedor de fragmentos, revisar menú de navegación y asegurar correspondencia de IDs.  
  Cambios propuestos:
  - Archivos: `app/src/main/res/layout/activity_main.xml` [crear `FragmentContainerView` con id `nav_host_fragment` si falta], `app/src/main/res/menu/bottom_navigation_menu.xml` [verificar itemIds], `app/src/main/java/com/example/fithub360/activities/MainActivity.java` [confirmar `setOnItemSelectedListener` y `replace` con el id correcto]
  - Fragmentos clave generados: N/A (cambios descriptivos)
  Acciones ejecutadas:
  - Comandos: `gradlew.bat assembleDebug` y ejecución desde Android Studio
  - Resultado: [por confirmar]
  - Errores observados: [no provistos]
  Decisiones tomadas: Priorizar alineación de recursos (layout/menu) con el código.  
  Impacto: Acotar el problema a configuración de UI vs. lógica.

- P5  
  Resumen de la respuesta de la IA: Entregó estructura y plantilla para `documentacion.md`.  
  Cambios propuestos:
  - Archivos creados: `documentacion.md`
  - Fragmentos clave generados: Estructura de secciones y campos a documentar.
  Acciones ejecutadas: Guardado del archivo y revisión.  
  Resultado: Éxito.  
  Errores observados: N/A  
  Decisiones tomadas: Adoptar la plantilla para el proyecto.  
  Impacto: Mejora de trazabilidad y repetibilidad del proceso.

## 3. Problemas encontrados y cómo se resolvieron con IA

- Título del problema: Desalineación de IDs entre layout y código
  - Síntomas:
    - Mensajes de error/logs: [posible] "No view found for id 0x... (R.id.nav_host_fragment) for fragment ..."
    - Reproducción: Abrir la app y seleccionar items del `BottomNavigationView`.
  - Análisis de causa raíz: El contenedor con id `nav_host_fragment` no existe o difiere en `activity_main.xml`.
  - Opciones consideradas:
    - A) Crear/ajustar `FragmentContainerView` con el id esperado (pro: simple; contra: requiere tocar layout)
    - B) Cambiar el id en código a coincidir con el layout (pro: rápido; contra: puede ocultar inconsistencias)
  - Solución aplicada:
    - Cambios realizados: `app/src/main/res/layout/activity_main.xml`: añadir/renombrar contenedor a `nav_host_fragment`; verificar `bottom_navigation`.
    - Justificación técnica: Alinear recursos con referencias en código evita excepciones en tiempo de ejecución.
  - Validación:
    - Cómo se verificó: Build local y navegación manual entre pestañas.
    - Estado final: resuelto [por confirmar con logs].
  - Lecciones aprendidas: Establecer convención y verificación de IDs entre layout y código; agregar chequeos en PR.

- Título del problema: Listener de navegación inferior inconsistente
  - Síntomas:
    - Mensajes de error/logs: Selecciones no cambian el fragmento; sin crash.
    - Reproducción: Tocar items de la barra inferior sin cambio visible.
  - Análisis de causa raíz: Uso de API obsoleta o Material Components desactualizado.
  - Opciones consideradas:
    - A) Usar `setOnItemSelectedListener` (pro: API actual; contra: requiere Material actualizado)
    - B) Migrar a Navigation Component con `NavController` (pro: menos código manual; contra: mayor refactor)
  - Solución aplicada:
    - Cambios realizados: Confirmar `setOnItemSelectedListener` y retorno `true`; revisar versión de Material en `build.gradle`.
    - Justificación técnica: API coherente con versiones recientes.
  - Validación:
    - Cómo se verificó: Tap secuenciales en todos los items.
    - Estado final: resuelto [por confirmar].
  - Lecciones aprendidas: Bloquear versiones mínimas de librerías y evitar APIs en desuso.

- Título del problema: Redirección de sesión al `LoginActivity`
  - Síntomas:
    - Mensajes de error/logs: Flujo vuelve a login sin razón aparente.
    - Reproducción: Abrir app con sesión válida esperada.
  - Análisis de causa raíz: `SessionManager.isLoggedIn()` devolviendo falso por configuración/almacenamiento.
  - Opciones consideradas:
    - A) Revisar fuente de verdad de sesión (pro: robustez; contra: requiere QA)
    - B) Añadir logs y manejo defensivo (pro: trazabilidad; contra: más ruido de logging)
  - Solución aplicada:
    - Cambios realizados: Verificar inicialización de `SessionManager`, añadir logging circunscrito y test manual con credenciales válidas.
    - Justificación técnica: Confirmar estado real antes de navegar.
  - Validación:
    - Cómo se verificó: Inicio de sesión y apertura de `MainActivity`.
    - Estado final: parcial [por confirmar].
  - Lecciones aprendidas: Registrar decisiones de flujo temprano y cubrir con tests de integración.

## 4. Reflexión final sobre la experiencia

- Valor aportado por la IA: Aceleró la enumeración de causas probables, dio checklist de verificación rápida y una plantilla útil para documentar.
- Limitaciones encontradas: Falta de logs concretos redujo la precisión de las propuestas; riesgo de sugerencias genéricas.
- Buenas prácticas identificadas:
  - Proveer contexto y criterios claros.
  - Validar con compilación y tests tras cada cambio.
  - Pedir diffs pequeños y verificables.
- Casos donde funcionó mejor/peor: Mejor en boilerplate y verificación de configuración; peor sin evidencia (stack traces/gradle output).
- Recomendaciones para el equipo: Exigir adjuntar logs y archivos implicados en cada solicitud; aplicar revisión por pares antes de fusionar.
- Próximos pasos: Integrar CI con `gradlew.bat assembleDebug test`; plantillas de issue para adjuntar logs; aumentar cobertura de tests en navegación y manejo de sesión.
