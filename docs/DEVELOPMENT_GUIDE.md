# Guía de Desarrollo, Flujo Git y Gestión de Versiones — MarketCompare

Esta guía establece el flujo de trabajo estándar para ingenieros y agentes que contribuyen al proyecto MarketCompare.

---

## 1. Estrategia de Ramas (Gitflow Adaptado)

Adoptamos una estrategia estructurada con dos ramas de larga duración y ramas efímeras de trabajo:

```
[main]        ──────────────────────────────●──────────────────────────────● (v1.0.0 Tagged Release)
                                           ▲                              ▲
                                           │ PR de Release                │
[develop]     ──────●──────────────────────●──────────────●───────────────┘
                     \                    /                \             /
[feat/fix]            ●─────────●────────●                  ●───────────●
                      (Ramas de Tarea / Hitos)
```

### Roles de las Ramas:
1. **`main` (Producción / Versiones Estables):**
   - Es la rama de máxima estabilidad y fuente de verdad de las versiones lanzadas.
   - Solo recibe fusiones provenientes de `develop` mediante Pull Request cuando se concreta un hito o versión pública (ej. `v1.0.0`).
   - Cada fusión en `main` debe ir acompañada de un **Git Tag semántico**.
2. **`develop` (Integración Activa):**
   - Es la rama de integración principal en el día a día.
   - Es el destino de todos los Pull Requests de nuevas funcionalidades, refactorizaciones y correcciones.
   - Todo código en `develop` debe compilar limpiamente y pasar las pruebas automáticas.
3. **Ramas de Tarea (`feat/*`, `fix/*`, `docs/*`, `test/*`):**
   - Se crean a partir de `develop`:
     - `feat/MC-014-document-selection`
     - `fix/MC-031-parser-error`
     - `docs/MC-005-architecture-adr`
   - Deben mantenerse pequeñas, atómicas y vinculadas a un ID de tarea del [`BACKLOG.csv`](../BACKLOG.csv).

---

## 2. Convención de Commits (Conventional Commits)

Cada commit debe describir claramente su intención y referenciar las tareas afectadas:
```bash
<tipo>(<ámbito>): <descripción concisa en imperativo> [<ID_TAREA>]
```
**Tipos admitidos:**
- `feat`: Nueva funcionalidad o componente de usuario.
- `fix`: Corrección de un defecto o bug.
- `docs`: Cambios exclusivos en documentación o especificaciones.
- `test`: Adición o refactorización de suites de pruebas.
- `refactor`: Cambio de código que no altera la funcionalidad externa.
- `chore`: Tareas de build, dependencias, CI o configuración de Gradle.

**Ejemplo:** `feat(receipt): implement document picker for Camera and PDF [MC-014]`

---

## 3. Integración Continua (GitHub Actions)

El repositorio cuenta con workflows automatizados en `.github/workflows/`:

1. **`android-ci.yml` (Integración Continua):**
   - Se ejecuta automáticamente ante cada `push` o `pull_request` sobre `main` o `develop`.
   - Verifica el código en Ubuntu con JDK 21.
   - Ejecuta:
     ```bash
     ./gradlew testDebugUnitTest
     ./gradlew assembleDebug
     ```
   - Publica los reportes de pruebas y almacena el APK compilado como artefacto descargable.

2. **`release.yml` (Publicación Automática de Versiones):**
   - Se dispara automáticamente cuando se empuja un Git Tag con el patrón `v*.*.*` (ej. `v1.0.0`).
   - Compila el APK oficial y crea automáticamente una **GitHub Release** con el archivo `MarketCompare-v1.0.0.apk` adjunto y el changelog generado.

---

## 4. Gestión de Versiones y Git Tags (SemVer)

Seguimos **Versionado Semántico (MAJOR.MINOR.PATCH)**:
- `MAJOR` (v**1**.0.0): Cambios de arquitectura mayores o lanzamiento público oficial de versión estable.
- `MINOR` (v1.**1**.0): Nuevas funcionalidades completas (ej. soporte de nuevo formato de exportación, autocompletado avanzado).
- `PATCH` (v1.0.**1**): Correcciones de errores menores o ajustes sin cambio de contrato.

### Pasos para publicar un nuevo Release:
1. Asegurar que todas las tareas del hito estén en `develop` con pruebas en verde.
2. Abrir PR de `develop` hacia `main` y aprobarlo.
3. Crear el tag anotado en local:
   ```bash
   git checkout main
   git pull origin main
   git tag -a v1.0.0 -m "Release v1.0.0 — MVP inicial de MarketCompare"
   git push origin v1.0.0
   ```
4. El workflow de GitHub Actions detectará el tag, compilará el APK y generará la Release en la pestaña *Releases* de GitHub.
