# MarketCompare — Dashboard de proyecto

> Estado actual: Gate 5 (Normalización, descuentos y canasta estimada) completado. Autocompletado inteligente de catálogo con protección de variantes incompatibles, clasificación de procedencia de descuentos con auditoría en recibos, y lista de compras con presupuesto estimado y detección de precios desactualizados (> 30 días) 100% implementados y verificados. 45/45 pruebas unitarias exitosas y APK de depuración generado limpiamente.

## Resumen
- Fase actual: `GATE 5 — Normalización, Descuentos y Canasta (COMPLETADO)` (Gate 4 permanece BLOQUEADO a la espera de credenciales/clave API oficial de `losprecios.co`).
- Estado global: `IN PROGRESS`
- Rama de integración activa: `develop`
- Rama de hito: `feat/MC-029-shopping-list-basket`
- Bloqueos conocidos: API externa de precios (`MC-003` - BLOCKED por falta de proveedor documentado).
- Plataforma: Android (Target/Compile SDK 37, Min SDK 26, Kotlin 2.2.10, AGP 9.2.1, Compose 2026.05.01, Room 2.8.4, ML Kit 16.0.1, CameraX 1.5.0-alpha06).
- Persistencia: Room local exclusiva para compras confirmadas (`MarketCompareDatabase`).
- Precios externos: Consulta efímera en memoria (no persistente en Room, archivos, logs ni caché).
- Responsable de integración: Tech Lead.
- Auditor final: Auditor sénior independiente.

## KPIs
| Indicador | Valor actual |
|---|---:|
| Tareas totales | 38 |
| DONE con evidencia | 30 (MC-001, MC-002, MC-004 a MC-021, MC-026 a MC-032, MC-035, MC-036, MC-037) |
| En revisión | 0 |
| Bloqueadas | 1 (MC-003 - API externa sin proveedor oficial) |
| Defectos P0/P1 abiertos | 0 |
| Última compilación | `assembleDebug` EXITOSA (APK generado 65.5 MB) |
| Última suite de tests | `testDebugUnitTest` EXITOSA (47/47 tests pasados: MoneyTest (10), PurchaseRepositoryTest (9), BasketEstimatorTest (7), ProductNormalizerTest (7), ProductAutocompleteEngineTest (5), DiscountClassifierTest (5), ReceiptParserTest (4)) |
| Hito Gate 0 (Entorno y alcance) | APROBADO (2026-11-07) |
| Hito Gate 1 (Arquitectura y modelado) | COMPLETADO (2026-11-07) |
| Hito Gate 2 (Esqueleto ejecutable) | COMPLETADO (2026-11-07) |
| Hito Gate 3 (Compras locales y OCR) | COMPLETADO (2026-11-07) |
| Hito Gate 5 (Producto y descuentos) | COMPLETADO (2026-11-07) |
| Hito Gate 6 (Calidad final) | IN PROGRESS (MC-030, MC-031 y MC-032 completadas) |

## Hitos
| Hito | Estado | Aprobación |
|---|---|---|
| G0 Entorno y alcance | DONE | Usuario (Aprobado 2026-11-07) |
| G1 Arquitectura | DONE | Usuario + Tech Lead |
| G2 Esqueleto ejecutable | DONE | Auditor sénior + usuario |
| G3 Compras locales y OCR | DONE | Tech Lead + QA |
| G4 API temporal | BLOCKED | Documentación API + auditor sénior |
| G5 Autocompletado y descuentos | DONE | Auditor sénior |
| G6 Calidad final | IN PROGRESS | Auditor sénior + usuario |

## Control de Versiones, Git y CI/CD
| Campo | Estado |
|---|---|
| Rama base de releases | `main` (protegida para versiones estables con tags) |
| Rama base de desarrollo | `develop` (integración activa y destino de PRs) |
| Rama de trabajo activa | `audit/MC-032-senior-code-privacy-audit` |
| Estado de repositorio | Conectado y sincronizado con GitHub |
| Remoto configurado | `origin` (`https://github.com/barbosaivan/MarketCompare.git`) |
| Integración Continua (CI) | GitHub Actions configurado (`.github/workflows/android-ci.yml`) |
| Automatización de Releases | GitHub Actions configurado (`.github/workflows/release.yml`) |
| Push remoto | `PUSHED` (`main`, `develop`, `feat/MC-014-receipt-pipeline`, `feat/MC-026-product-normalization`, `feat/MC-027-product-autocomplete`, `feat/MC-028-discount-provenance`, `feat/MC-029-shopping-list-basket`, `qa/MC-030-static-analysis-audit`, `qa/MC-031-automated-test-suite`, `audit/MC-032-senior-code-privacy-audit`) |

## Registro de cambios (Changelog)
- **2026-11-08 14:10:**
  - `MC-032` (G6): Auditoría sénior independiente de código, arquitectura y privacidad completada con veredicto **PASS** ([`docs/AUDIT_REPORT.md`](docs/AUDIT_REPORT.md)).
  - Verificación formal: 0 defectos P0, 0 defectos P1, 0 secretos expuestos, 100% cumplimiento de ADR 0001 (privacidad y no-persistencia de cotizaciones externas) y ADR 0003 (aritmética financiera exacta sin punto flotante).
- **2026-11-08 13:58:**
  - `MC-031` (G6): Ejecución y certificación de la suite completa de pruebas automatizadas (`docs/TEST_SUITE_REPORT.md`).
  - Cobertura expandida a 47 pruebas unitarias sin fallos ni errores (100% de éxito en ~3.8s).
  - Casos de borde validados: operaciones entre divisas distintas, divisiones por cero, formateo monetario COP, eliminación y consulta histórica de observaciones de precios en el repositorio de compras.
  - Verificación: `testDebugUnitTest` (47/47 pasados) y `assembleDebug` completado en 2s.
- **2026-11-08 13:50:**
  - `MC-030` (G6): Auditoría completa de análisis estático y dependencias (`docs/STATIC_ANALYSIS_AUDIT.md`).
  - Android Lint ejecutado y depurado al 100%: **0 errores y 0 advertencias**.
  - Corrección de directrices Compose en `ProductAutocompleteField` (parámetro `modifier` ordenado como primer opcional).
  - Sustitución de plataforma `Bitmap.createBitmap` por extensión KTX en `DocumentTextExtractor`.
  - Incorporada capa `<monochrome>` en íconos adaptativos launcher para compatibilidad con Android 13+.
  - Eliminado código muerto XML de colores (`colors.xml`) en favor del sistema de diseño Compose M3.
  - Creado `app/lint.xml` con exclusiones controladas y justificadas.
  - Verificación: 45/45 pruebas unitarias exitosas y `assembleDebug` limpio (13s).
- **2026-11-07 22:40:**
  - `MC-029` (G5): Implementada lista de compras y canasta estimada (`ShoppingListScreen`, `BasketEstimator`, `ShoppingListDao`).
  - Estimación de presupuesto en tiempo real basada exclusivamente en compras locales confirmadas de la base de datos Room.
  - Distinción rigurosa de frescura de precios: confirmados recientes (<= 30 días con comercio y fecha), desactualizados (> 30 días advertidos explícitamente en UI y cálculos) y sin historial previo.
  - Soporte para agregar artículos con autocompletado desde el catálogo, marcar como comprados/tachados, y limpiar lista.
  - Navegación principal mediante `NavigationBar` (Historial, Escaneo, Lista de Compras).
  - Verificación: 45/45 pruebas unitarias exitosas (+7 tests en `BasketEstimatorTest` y +3 en `PurchaseRepositoryTest`) y `assembleDebug` limpio sin deprecaciones.
- **2026-11-07 22:15:**
  - `MC-028` (G5): Implementada clasificación de procedencia de descuentos (`DiscountClassifier`: `LOYALTY_MEMBER`, `PROMOTION_2X1`, `EXPLICIT_LINE`, `DERIVED`, `UNKNOWN`).
  - Integrada evidencia textual OCR en el modelo y UI (`discountEvidence`) con auditoría interactiva (`DiscountAuditDialog`).
  - En la pantalla de revisión (`ReceiptReviewScreen`), el usuario puede ver la procedencia de cada descuento detectado, modificar su tipo o importe, o remover falsos positivos recalculando el precio final con exactitud (`Money`).
  - Mostrado el ahorro total acumulado en descuentos en la barra inferior de confirmación.
  - Verificación: 29/29 pruebas unitarias exitosas (+5 tests en `DiscountClassifierTest` y +1 en `ReceiptParserTest`) y `assembleDebug` exitoso.
- **2026-11-07 21:45:**
  - `MC-027` (G5): Implementado motor de autocompletado con ranking ponderado (`ProductAutocompleteEngine`: alias exacto > prefijo canónico > tokens > propuesta heurística).
  - Implementada protección contra fusión silenciosa de variantes incompatibles: la UI (`ProductAutocompleteField`) detecta discrepancias de peso o presentación y muestra un diálogo de confirmación explícito antes de vincular.
  - Integrado el campo de autocompletado en `ReceiptReviewScreen` para vinculación directa de ítems de factura con el catálogo de productos local.
  - Verificación: 24/24 pruebas unitarias exitosas (+5 tests en `ProductAutocompleteEngineTest`) y compilación `assembleDebug` limpia sin advertencias de deprecación.
- **2026-11-07 18:55:**
  - `MC-026` (G5): Implementado `ProductNormalizer` para extracción inteligente de marca (marcas colombianas comunes), presentación (Bolsa, Vaso, Botella, Lata, Caja, etc.), cantidad base y unidad (`kg`, `g`, `ml`, `l`, `und`).
  - Implementada clave única `variantFingerprint` y regla estricta `areVariantsCompatible` para garantizar que presentaciones o tamaños distintos nunca se fusionen como el mismo producto.
  - Implementada búsqueda y registro de alias en `PurchaseRepositoryImpl` y `ProductDao`.
  - Verificación: 19/19 pruebas unitarias exitosas (+7 tests en `ProductNormalizerTest` y +2 en `PurchaseRepositoryTest`) y `assembleDebug` exitoso.
- **2026-11-07 18:25:**
  - `MC-014` (G3): Implementada selección multicanal de facturas (CameraX con archivo temporal y `FileProvider`, Photo Picker de galería y OpenDocument SAF para PDFs).
  - `MC-015` & `MC-016` (G3): Implementado `DocumentTextExtractor` combinando Google ML Kit Text Recognition, `PdfRenderer` para PDFs escaneados y lectura de streams para PDFs con texto embebido.
  - `MC-017` (G3): Implementado motor de parseo heurístico `ReceiptParser` para facturas colombianas (reconocimiento de comercios Éxito, D1, Ara, Carulla, Olímpica, Jumbo, etc., extracción de NIT, fechas, líneas de artículos con unidades y empaques, detección de descuentos promocionales y descarte de pie fiscal/medios de pago).
  - `MC-018` (G3): Desarrollada pantalla `ReceiptReviewScreen` interactiva: edición en vivo de tienda, inclusión/exclusión de artículos, modificación de descripciones y recálculo automático de total antes del guardado.
  - `MC-019` & `MC-020` (G3): Guardado transaccional en Room (`saveConfirmedPurchase`) enlazando tienda, recibo y artículos con confirmación explícita del usuario.
  - `MC-021` (G3): Aritmética financiera estricta en enteros menores (`Money`) en todo el flujo de ingesta y revisión.
  - Verificación: 10/10 pruebas unitarias exitosas (`ReceiptParserTest`, `MoneyTest`, `PurchaseRepositoryTest`) y `assembleDebug` completado generando `app-debug.apk` (65.5 MB).
