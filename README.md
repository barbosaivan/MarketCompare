# MarketCompare 🛒📊

[![Android CI](https://github.com/barbosaivan/MarketCompare/actions/workflows/android-ci.yml/badge.svg)](https://github.com/barbosaivan/MarketCompare/actions/workflows/android-ci.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Android Gradle Plugin](https://img.shields.io/badge/AGP-9.2.1-green.svg?logo=android)](https://developer.android.com/build)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2026.05.01-4285F4.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material%20Design-3-7C4DFF.svg)](https://m3.material.io/)
[![Quality Gate](https://img.shields.io/badge/Quality%20Gate-Gate%206%20Certified-success.svg)](QUALITY_GATES.md)
[![Android Lint](https://img.shields.io/badge/Android%20Lint-0%20errors%20%7C%200%20warnings-brightgreen.svg)](docs/STATIC_ANALYSIS_AUDIT.md)
[![Unit Tests](https://img.shields.io/badge/Unit%20Tests-47%2F47%20Passed-success.svg)](docs/TEST_SUITE_REPORT.md)
[![Audit](https://img.shields.io/badge/Senior%20Audit-PASS-success.svg)](docs/AUDIT_REPORT.md)

**MarketCompare** es una aplicación Android moderna, accesible e intuitiva orientada al consumidor inteligente. Permite registrar y auditar compras reales de supermercado a partir de facturas físicas y electrónicas (mediante cámara, galería y PDFs), manteniendo un historial de precios 100% privado y local, y facilitando la comparación de precios de referencia en tiempo real sin almacenar cotizaciones externas.

---

## 🚀 Estado del Proyecto y Capacidades (Gate 6 Certificado)

El proyecto ha superado con éxito las compuertas de calidad **Gate 0**, **Gate 1**, **Gate 2**, **Gate 3**, **Gate 5** y **Gate 6 (Calidad Final y Auditoría Sénior)**:

1. **Importación Multicanal de Facturas:**
   - **Cámara en vivo:** Captura de fotos de tickets con CameraX y URI seguro vía `FileProvider`.
   - **Galería del dispositivo:** Selección rápida con Android Photo Picker nativo.
   - **Facturas electrónicas (PDF):** Lectura directa de streams de texto para facturas DIAN y renderizado rasterizado con `PdfRenderer` + OCR para documentos escaneados.
2. **Extracción y Parseo Inteligente (ML Kit & Heurística):**
   - Extracción de texto OCR de alto rendimiento con **Google ML Kit Text Recognition**.
   - Motor heurístico especializado en facturas colombianas (`ReceiptParser`) con soporte para comercios como **Éxito, Carulla, D1, Ara, Jumbo, Olímpica, Metro, Makro, PriceSmart**, entre otros.
   - Extracción automática de NIT, fecha de compra, desglose de líneas de productos (cantidad, descripción, unidad de medida: kg, g, ml, l) y líneas explícitas de descuento promocional.
   - Filtrado riguroso de pie de página fiscal (IVA, ICA, CUFE, resoluciones DIAN, medios de pago y cambio).
3. **Revisión Editable y Auditoría de Descuentos (MC-028):**
   - Pantalla interactiva (`ReceiptReviewScreen`) previa a la persistencia con cálculo de ahorro total.
   - Clasificación de procedencia de descuentos (`DiscountClassifier`): Fidelización/Puntos, Promociones 2x1, Descuentos directos en factura, Calculados o Desconocidos.
   - Diálogo de auditoría interactiva (`DiscountAuditDialog`) para verificar o ajustar la evidencia OCR y corregir falsos positivos recalculando el precio exacto.
4. **Normalización y Autocompletado de Catálogo (MC-026 & MC-027):**
   - Extracción de marca, empaque, volumen/peso y huella digital única de variante (`variantFingerprint`).
   - Autocompletado ponderado (`ProductAutocompleteEngine`) en campo de texto con prevención explícita ante fusiones accidentales de presentaciones diferentes.
5. **Lista de Compras y Presupuesto de Canasta Estimada (MC-029):**
   - Planificación de compras (`ShoppingListScreen`) con presupuesto estimado en tiempo real calculado a partir del historial local de compras.
   - Distinción visual y analítica de frescura: precios confirmados recientes (<= 30 días con comercio y fecha), desactualizados (> 30 días) y sin precio histórico registrado.
6. **Persistencia Local-First (Room):**
   - Base de datos relacional local en SQLite (`MarketCompareDatabase`) para compras confirmadas y lista de compras.
   - Aritmética financiera exacta: prohibición estricta de `Double`/`Float` para importes monetarios mediante el Value Object `Money` (en unidades menores / enteros exactos).
7. **Historial de Compras en Vivo:**
   - Vista de compras anteriores (`HistoryScreen`) con fecha formateada, establecimiento, cantidad de artículos y total pagado.

---

## 🏛️ Arquitectura del Proyecto

MarketCompare implementa una **Clean Architecture** pragmática dividida en 4 capas desacopladas:

```
[Presentation]  --> Compose UI (M3), ViewModels, UiState inmutable, Compose Navigation
      │
      ▼
   [Domain]     --> Modelos puros (Receipt, Money, Product, ReceiptDraft), Parser heurístico
      ▲
      │ (Implementa)
    [Data]      --> Room Database (DAOs, Entities), PurchaseRepositoryImpl, Cliente Ktor
      ▲
      │ (Provee streams crudos)
   [Platform]   --> CameraX, Photo Picker, ML Kit OCR, PdfRenderer, SAF
```

---

## 🛠️ Requisitos y Entorno de Desarrollo

- **Sistema Operativo:** Windows, macOS o Linux
- **IDE:** Android Studio (Ladybug / Meerkat o superior)
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
*El APK generado se ubica en: `app/build/outputs/apk/debug/app-debug.apk` (~65.5 MB).*

### Ejecutar Pruebas Unitarias:
```bash
./gradlew testDebugUnitTest
```
*Ejecuta la suite completa de 47 pruebas unitarias (`BasketEstimatorTest`, `DiscountClassifierTest`, `ReceiptParserTest`, `ProductAutocompleteEngineTest`, `ProductNormalizerTest`, `PurchaseRepositoryTest`, `MoneyTest`), con reportes HTML en: `app/build/reports/tests/testDebugUnitTest/index.html`.*

### Ejecutar Análisis Estático (Android Lint):
```bash
./gradlew lintDebug
```
*Verifica la conformidad del código contra `app/lint.xml`. Baseline actual: **0 errores, 0 warnings**.*

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

La carpeta [`docs/`](docs/) contiene las especificaciones detalladas, decisiones arquitectónicas e informes de auditoría:

| Documento | Descripción |
|---|---|
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | Capas, flujo de ingesta OCR, canasta y principios de desacoplamiento. |
| [`docs/DATA_MODEL.md`](docs/DATA_MODEL.md) | Modelo relacional Room, lista de compras, Value Objects y DTOs en memoria. |
| [`docs/FUNCTIONALITY.md`](docs/FUNCTIONALITY.md) | Pipeline de procesamiento de recibos, autocompletado y flujos de usuario. |
| [`docs/DESIGN_SYSTEM.md`](docs/DESIGN_SYSTEM.md) | Tokens Material 3, tipografía y catálogo de componentes. |
| [`docs/PROJECT_CONFIGURATION.md`](docs/PROJECT_CONFIGURATION.md) | Toolchain verificado, SDK y versiones fijadas. |
| [`docs/QUALITY_AND_TESTING.md`](docs/QUALITY_AND_TESTING.md) | Estrategia de calidad, pirámide de pruebas y criterios de compuerta. |
| [`docs/DEVELOPMENT_GUIDE.md`](docs/DEVELOPMENT_GUIDE.md) | Guía de desarrollo, Conventional Commits y releases. |
| [`docs/STATIC_ANALYSIS_AUDIT.md`](docs/STATIC_ANALYSIS_AUDIT.md) | Reporte formal de Android Lint (0 errores, 0 advertencias) y dependencias. |
| [`docs/TEST_SUITE_REPORT.md`](docs/TEST_SUITE_REPORT.md) | Certificación de la suite completa de 47 pruebas unitarias (100% éxito). |
| [`docs/AUDIT_REPORT.md`](docs/AUDIT_REPORT.md) | Informe de auditoría sénior independiente de código, arquitectura y privacidad (Veredicto PASS). |
| [`docs/CHANGELOG_TECHNICAL.md`](docs/CHANGELOG_TECHNICAL.md) | Historial técnico de cambios por compuerta de calidad. |
| [`docs/adr/`](docs/adr/) | Registro de Decisiones Arquitectónicas (ADRs 0001, 0002, 0003). |
| [`DASHBOARD.md`](DASHBOARD.md) | Tablero de seguimiento de hitos y KPIs en tiempo real. |
| [`BACKLOG.csv`](BACKLOG.csv) | Trazabilidad atómica de tareas por hito con dependencias y estados Git. |
| [`QUALITY_GATES.md`](QUALITY_GATES.md) | Criterios de aceptación por compuerta de calidad (Gates 0 a 6). |
