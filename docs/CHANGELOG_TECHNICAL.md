# Changelog Técnico — MarketCompare

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/) y siguiendo convenciones semánticas.

---

## [Gate 6: Calidad Final y Cierre] — 2026-11-08

### Añadido
- **Suite de Pruebas Automatizadas Completa (MC-031):** 47 pruebas unitarias aprobadas al 100% en ~3.8s (`MoneyTest`, `PurchaseRepositoryTest`, `ProductNormalizerTest`, `ProductAutocompleteEngineTest`, `DiscountClassifierTest`, `ReceiptParserTest`, `BasketEstimatorTest`).
- **Certificación de Análisis Estático Android Lint (MC-030):** 0 errores, 0 warnings. Creación de configuración en `app/lint.xml`.
- **Auditoría Sénior Independiente (MC-032):** Veredicto `PASS`, 0 defectos bloqueantes, verificación de 0 secretos y 100% adherencia a ADRs 0001, 0002 y 0003.
- **Documentación Completa y Sincronizada (MC-033 / MC-038):**
  - Publicación de reportes de auditoría: `docs/STATIC_ANALYSIS_AUDIT.md`, `docs/TEST_SUITE_REPORT.md`, `docs/AUDIT_REPORT.md`.
  - Actualización exhaustiva de especificaciones: `docs/README.md`, `docs/DATA_MODEL.md`, `docs/PRODUCT_OVERVIEW.md`, `docs/FUNCTIONALITY.md`, `docs/ARCHITECTURE.md`, `docs/QUALITY_AND_TESTING.md`, `docs/CHANGELOG_TECHNICAL.md`, `docs/GLOSSARY.md`, `docs/INTEGRATIONS.md`.

### Optimizado
- Orden canónico de parámetros `modifier` en componentes Jetpack Compose.
- Eliminación de recursos redundantes y colores huérfanos.
- Soporte para íconos monocromáticos adaptativos (`<monochrome>`) en Material You.

---

## [Gate 5: Normalización, Descuentos y Canasta] — 2026-11-08

### Añadido
- **Normalización de Productos y Variantes (MC-026):** Implementación de `ProductNormalizer.kt` con extracción de marcas colombianas, presentaciones, cantidades estándar, `variantFingerprint` canónico y validación estricta de compatibilidad `areVariantsCompatible`.
- **Motor de Autocompletado y Asignación de Alias (MC-027):** Implementación de `ProductAutocompleteEngine.kt` con ranking ponderado, autocompletado en edición de ítems y diálogo de advertencia ante colisiones de variantes.
- **Clasificador y Auditoría de Descuentos (MC-028):** Implementación de `DiscountClassifier.kt` con soporte para `LOYALTY_MEMBER`, `PROMOTION_2X1`, `EXPLICIT_LINE`, `DERIVED` y `UNKNOWN`. Inclusión de evidencia OCR y diálogo interactivo `DiscountAuditDialog`.
- **Lista de Compras y Estimador de Canasta (MC-029):**
  - Entidad `ShoppingListItemEntity` y `ShoppingListDao` en Room con operaciones reactivas (`Flow`).
  - `BasketEstimator.kt` con cálculo dinámico de presupuesto estimado.
  - Clasificación de frescura de precios: `RECENT` (<= 30 días), `STALE` (> 30 días), `UNKNOWN`.
  - Pantalla `ShoppingListScreen.kt` y barra de navegación inferior `NavigationBar` en Material 3.

---

## [Gate 4: API Temporal de Precios] — 2026-11-08 (En Espera)

### Estado
- Tareas `MC-003` y `MC-022` bloqueadas formalmente a la espera de credenciales oficiales (`ClaveAPI`) de `losprecios.co`.
- La arquitectura en memoria (ADR 0001) y los contratos de repositorio (`ExternalPriceQuote`) se encuentran preparados para integración inmediata en cuanto se reciban los accesos.

---

## [Gate 3: Compras Locales y Pipeline OCR] — 2026-11-07

### Añadido
- **Pipeline de Captura Multicanal:** Integración de CameraX con guía de encuadre, Android Photo Picker para imágenes locales y Storage Access Framework para documentos PDF.
- **Extracción de Texto Dual:** Stream nativo para PDFs digitales y rasterizado `PdfRenderer` + Google ML Kit Text Recognition para imágenes y PDFs escaneados.
- **Motor Heurístico de Parseo (`ReceiptParser`):** Detección de comercios, fechas, líneas de artículos, cantidades, precios y niveles de certidumbre.
- **Pantalla de Revisión Editable (`ReceiptReviewScreen`):** Flujo de inspección, edición, exclusión de líneas y confirmación explícita previa a guardar.
- **Persistencia Transaccional:** `PurchaseRepositoryImpl` con transacciones atómicas en Room SQLite.
- **Pantalla de Historial de Compras (`PurchaseHistoryScreen`):** Visualización cronológica de facturas y desglose de ítems.

---

## [Gate 2: Esqueleto Ejecutable y Base de Persistencia] — 2026-11-07

### Añadido
- **Configuración de Room DB:** `AppDatabase` v1 con `StoreEntity`, `ProductEntity`, `ProductAliasEntity`, `ReceiptEntity`, `ReceiptItemEntity`.
- **TypeConverters:** Soporte para Value Object `Money`, `LocalDate` y enums.
- **Integración Material 3:** Tema tipográfico, paleta de colores dinámica con soporte de tema claro y oscuro, prohibición de colores y textos hardcodeados.
- **Navegación Tipada:** Rutas de navegación seguras en Jetpack Compose.
- **CI/CD Básico:** Pipeline de GitHub Actions para validación de compilación y pruebas unitarias.

---

## [Gate 1: Arquitectura y Modelado] — 2026-11-07

### Añadido
- **ADR 0001 (Aceptado):** Decisión arquitectónica para separar compras locales de cotizaciones API efímeras sin persistencia (`docs/adr/0001-local-first-ephemeral-api.md`).
- **ADR 0002 (Aceptado):** Definición de límites y dependencias de Clean Architecture pragmática en 4 capas (`docs/adr/0002-clean-architecture-layer-boundaries.md`).
- **ADR 0003 (Aceptado):** Prohibición de `Double`/`Float` y especificación del Value Object `Money` en unidades menores (`docs/adr/0003-money-representation-integer-minor-units.md`).
- **Especificaciones Técnicas:** `docs/ARCHITECTURE.md`, `docs/DATA_MODEL.md`, `docs/FUNCTIONALITY.md`, `docs/DESIGN_SYSTEM.md`, `docs/QUALITY_AND_TESTING.md`.

---

## [Gate 0: Entorno y Alcance] — 2026-11-07

### Añadido
- Inspección del entorno y toolchain: Android Studio AI-262, OpenJDK 25.0.4 JBR, Git 2.45.1.
- Inicialización del repositorio Git local (`main`), estructura de ramas Gitflow (`develop`).
- Configuración de `.gitignore` para Android y Compose.
- Definición de compuertas de calidad formales (`QUALITY_GATES.md`), inventario atómico (`BACKLOG.csv`) y tablero de control (`DASHBOARD.md`).
