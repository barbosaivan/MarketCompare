# MarketCompare 🛒📊

[![Android CI](https://github.com/barbosaivan/MarketCompare/actions/workflows/android-ci.yml/badge.svg)](https://github.com/barbosaivan/MarketCompare/actions/workflows/android-ci.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Android Gradle Plugin](https://img.shields.io/badge/AGP-9.2.1-green.svg?logo=android)](https://developer.android.com/build)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2026.05.01-4285F4.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material%20Design-3-7C4DFF.svg)](https://m3.material.io/)

**MarketCompare** es una aplicación Android moderna, accesible e intuitiva orientada al consumidor inteligente. Permite registrar y auditar compras reales de supermercado a partir de facturas físicas y electrónicas (mediante cámara, galería y PDFs), manteniendo un historial de precios 100% privado y local, y facilitando la comparación de precios de referencia en tiempo real sin almacenar cotizaciones externas.

---

## 🚀 Características Principales

1. **Importación Multicanal de Facturas:**
   - Captura en vivo con cámara integrada vía **CameraX**.
   - Selección de imágenes de tickets desde galería vía Android **Photo Picker**.
   - Importación de facturas electrónicas en **PDF** (extracción directa de streams de texto y fallback rasterizado con `PdfRenderer` + OCR).
2. **Reconocimiento y Parseo Inteligente (ML Kit):**
   - Extracción de texto con **Google ML Kit Text Recognition**.
   - Motor heurístico (`ReceiptParser`) que separa encabezados, líneas de artículos, descuentos y totales/impuestos.
3. **Revisión Editable Obligatoria:**
   - Pantalla interactiva previa al guardado: el usuario puede corregir descripciones, cantidades y precios, o excluir artículos antes de confirmar.
   - **Regla de oro:** Cero guardado automático sin aprobación explícita del usuario.
4. **Persistencia Local-First (Room):**
   - Base de datos relacional local en SQLite para compras confirmadas.
   - Aritmética financiera exacta: prohibición estricta de `Double`/`Float` para importes monetarios mediante el Value Object `Money` (en unidades menores / enteros).
5. **Comparativa de Precios Efímera (ADR 0001):**
   - Las cotizaciones externas son temporales y residen exclusivamente en memoria volátil durante la visualización.
   - No se almacenan en Room, DataStore, caché persistente HTTP ni logs, protegiendo los términos de licencia y la fidelidad de los datos propios.

---

## 🏛️ Arquitectura del Proyecto

MarketCompare implementa una **Clean Architecture** pragmática dividida en 4 capas desacopladas:

```
[Presentation]  --> Compose UI (M3), ViewModels, UiState inmutable, Compose Navigation
      │
      ▼
   [Domain]     --> Modelos puros (Receipt, Money, Product), Casos de Uso, Interfaces
      ▲
      │ (Implementa)
    [Data]      --> Room Database (DAOs, Entities), Repositorios, Parser, Cliente Ktor
      ▲
      │ (Provee streams crudos)
   [Platform]   --> CameraX, Photo Picker, ML Kit OCR, PdfRenderer, SAF
```

---

## 🛠️ Requisitos y Entorno de Desarrollo

- **Sistema Operativo:** Windows, macOS o Linux
- **IDE:** Android Studio (Canary / Ladybug / Meerkat o superior)
- **JDK:** OpenJDK 21 / 25 (utiliza por defecto el JBR de Android Studio)
- **Gradle:** 9.4.1 (vía Gradle Wrapper `./gradlew`)
- **Android SDK:**
  - `compileSdk`: **37**
  - `targetSdk`: **37**
  - `minSdk`: **26** (Android 8.0 Oreo)

---

## 💻 Comandos de Compilación y Verificación

### Compilar el APK de Depuración:
```bash
./gradlew assembleDebug
```
*El APK generado se ubica en: `app/build/outputs/apk/debug/app-debug.apk`.*

### Ejecutar Pruebas Unitarias:
```bash
./gradlew testDebugUnitTest
```
*Genera reportes HTML en: `app/build/reports/tests/testDebugUnitTest/index.html`.*

---

## 🌿 Flujo de Ramas y Releases (Gitflow)

- **`main`**: Rama protegida para versiones estables listas para producción. Cada release cuenta con un **Git Tag semántico** (ej. `v1.0.0`).
- **`develop`**: Rama base de integración continua activa. Destino de los Pull Requests.
- **Ramas de Tarea (`feat/*`, `fix/*`, `docs/*`)**: Ramas de corta duración asociadas a tareas de [`BACKLOG.csv`](BACKLOG.csv).
- **Integración Continua (CI/CD):**
  - [`.github/workflows/android-ci.yml`](.github/workflows/android-ci.yml): Compilación y ejecución de tests automática en cada `push`/`PR`.
  - [`.github/workflows/release.yml`](.github/workflows/release.yml): Generación automática de Releases en GitHub y empaquetado de APKs ante nuevos tags `v*.*.*`.

---

## 📚 Documentación Técnica Viva

La carpeta [`docs/`](docs/) contiene las especificaciones detalladas y decisiones arquitectónicas:

| Documento | Descripción |
|---|---|
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | Capas, flujo de ingesta OCR y principios de desacoplamiento. |
| [`docs/DATA_MODEL.md`](docs/DATA_MODEL.md) | Modelo relacional Room, Value Objects y DTOs en memoria. |
| [`docs/FUNCTIONALITY.md`](docs/FUNCTIONALITY.md) | Pipeline de procesamiento de recibos y flujos de usuario. |
| [`docs/DESIGN_SYSTEM.md`](docs/DESIGN_SYSTEM.md) | Tokens Material 3, tipografía y catálogo de componentes. |
| [`docs/PROJECT_CONFIGURATION.md`](docs/PROJECT_CONFIGURATION.md) | Toolchain verificado, SDK y versiones fijadas. |
| [`docs/QUALITY_AND_TESTING.md`](docs/QUALITY_AND_TESTING.md) | Estrategia de pruebas unitarias, integración y fixtures sintéticas. |
| [`docs/DEVELOPMENT_GUIDE.md`](docs/DEVELOPMENT_GUIDE.md) | Guía de desarrollo, Conventional Commits y releases. |
| [`docs/adr/`](docs/adr/) | Registro de Decisiones Arquitectónicas (ADRs 0001, 0002, 0003). |
| [`DASHBOARD.md`](DASHBOARD.md) | Tablero de seguimiento de hitos y KPIs en tiempo real. |
| [`BACKLOG.csv`](BACKLOG.csv) | Trazabilidad atómica de tareas por hito con dependencias y estados Git. |
