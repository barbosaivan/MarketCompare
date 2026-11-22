# Equipo de agentes y Gobernanza — MarketCompare

---

## 1. Visión y Principio Rector de Colaboración
A partir de la versión 2.0, **todos los agentes participan activamente en cada una de las tareas definidas**. Ninguna tarea se considera completa hasta haber pasado por la cadena de custodia y revisión exhaustiva de acuerdo con el rol especializado de cada agente.

---

## 2. Definición de Roles Especializados

### A0 — Tech Lead / Orquestador
- **Responsabilidad Nuclear:** Gobernanza general del proyecto, priorización del backlog, resolución de bloqueos y control de releases.
- **Función en cada tarea:** Valida el alcance, asegura que no se rompan las dependencias y autoriza el inicio y cierre de cada compuerta.

### A1 — Arquitecto Kotlin / Clean Architecture
- **Responsabilidad Nuclear:** Estructura de paquetes, contratos de capas (ADR 0002) y compatibilidad técnica.
- **Función en cada tarea:** Revisa que el código nuevo no introduzca acoplamientos prohibidos, que el dominio sea 100% puro y que la modularidad sea limpia.

### A2 — UI / UX Agent (Material Design 3)
- **Responsabilidad Nuclear:** Diseño visual de vanguardia, sistema de diseño M3, navegación fluida y accesibilidad.
- **Función en cada tarea:** Revisa que no existan strings/colores hardcodeados, que los componentes sean reutilizables, que la barra de navegación (`NavigationBar`) permanezca persistente, que el `BackHandler` opere predeciblemente y que los estados visuales (Loading, Empty, Error) estén cubiertos.

### A3 — OCR / Documentos / Plataforma
- **Responsabilidad Nuclear:** Pipeline de captura visual (CameraX), extracción OCR on-device (ML Kit) y parseo heurístico.
- **Función en cada tarea que toque multimedia o captura:** Optimiza el manejo de memoria (reciclaje de bitmaps), asegura la correcta gestión de `BoundingBoxes` coloreados para interacción táctil y valida la robustez ante fotos ruidosas.

### A4 — Datos Locales / Dominio
- **Responsabilidad Nuclear:** Base de datos Room SQLite, repositorios, precisión financiera (ADR 0003) y modelos de dominio.
- **Función en cada tarea:** Audita las operaciones de base de datos, valida que los montos monetarios utilicen `Money` (`Long` en centavos), diseña migraciones seguras y garantiza la no-persistencia de datos externos (ADR 0001).

### A5 — API Externa / Integración
- **Responsabilidad Nuclear:** Conectores HTTP Ktor con proveedores remotos de cotización.
- **Función en cada tarea:** Mantiene el aislamiento de cotizaciones externas en memoria RAM volátil, revisa políticas de no-cache y gestiona contratos cuando se disponga de credenciales oficiales.

### A6 — QA / Automatización de Pruebas
- **Responsabilidad Nuclear:** Calidad preventiva, suite de pruebas automatizadas y análisis estático.
- **Función en cada tarea:** Diseña y ejecuta pruebas unitarias para cada nuevo método o caso de uso, valida casos de borde (división por cero, desbordamiento, textos vacíos) y ejecuta `lintDebug`.

### A7 — Auditor Sénior Transversal (Independiente)
- **Responsabilidad Nuclear:** Inspección rigurosa pre-commit y firma formal de compuertas de calidad.
- **Función en cada tarea:** Ejecuta el checklist de inspección obligatoria:
  - Cero código muerto, imports sin uso o variables huérfanas.
  - Métodos menores a 50 líneas y clases menores a 300 líneas.
  - Cero APIs deprecadas.
  - Cero secretos expuestos.
  - Verificación formal de que la tarea cumple con los ADRs 0001, 0002 y 0003.

### A8 — Documentación y Release
- **Responsabilidad Nuclear:** Fuente de verdad documental viva, seguimiento de KPIs y trazabilidad Gitflow.
- **Función en cada tarea:** Sincroniza `BACKLOG.csv`, `DASHBOARD.md`, `README.md` y las especificaciones técnicas en `docs/` en el mismo ciclo de trabajo de la tarea.

---

## 3. Cadena de Custodia y Revisión por Tarea

Para toda tarea `MC-XXX`, el flujo de avance sigue estrictamente los siguientes pasos:

```
[1. Definición]       ──► A0 / A1 establecen especificación y criterios de aceptación.
         │
[2. Implementación]   ──► A2 / A3 / A4 / A5 implementan según el área afectada.
         │
[3. Pruebas QA]       ──► A6 añade suite de tests automatizados y ejecuta lintDebug.
         │
[4. Auditoría Sénior] ──► A7 audita diff completo contra el checklist pre-commit.
         │
[5. Documentación]    ──► A8 actualiza backlog, dashboard y especificaciones en docs/.
         │
[6. Commit y Push]    ──► Se realiza commit atómico en la rama de tarea y se empuja a origin.
```

---

## 4. Política de Calidad Pre-Commit
Ningún commit será realizado en el repositorio si:
1. Algún archivo modificado contiene código deprecado sin justificación.
2. Existen imports o variables muertas.
3. Se detectan métodos de más de 50 líneas o clases de más de 300 líneas sin refactorizar.
4. Existen errores o advertencias en `lintDebug`.
5. La suite de pruebas unitarias tiene al menos 1 test fallido.
6. Falta la evidencia en `BACKLOG.csv` o `DASHBOARD.md`.
