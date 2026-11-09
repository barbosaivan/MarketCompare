# MarketCompare — Dashboard de proyecto

> Estado actual: Fase v2.0 en ejecución. Gate 7 y Gate 8 completados y aprobados. Gate 9 (Ingreso Manual, Edición Total y Normalización a Medidas Estándar) en progreso, completada tarea MC-049.

## Resumen
- Fase actual: `FASE v2.0 — Mejoras de Usabilidad, Arquitectura M3 y Catálogo`
- Compuerta activa: `GATE 9 — Ingreso Manual, Edición Total y Normalización a Medidas Estándar (IN PROGRESS)`
- Estado global: `IN_PROGRESS`
- Rama de integración activa: `develop`
- Rama de trabajo activa: `feat/MC-049-manual-price-entry`
- Rama de release estable: `main` (Tag `v1.0.0-rc1`)
- Bloqueos conocidos: API externa de precios (`MC-003` / `MC-022` - BLOCKED por falta de clave API del proveedor oficial).
- Plataforma: Android (Target/Compile SDK 37, Min SDK 26, Kotlin 2.2.10, AGP 9.2.1, Compose 2026.05.01, Room 2.8.4, ML Kit 16.0.1, CameraX 1.5.0-alpha06).
- Persistencia: Room local exclusiva para compras confirmadas y lista de compras (`MarketCompareDatabase`).
- Precios externos: Consulta efímera en memoria (no persistente en Room, archivos, logs ni caché - ADR 0001).
- Responsable de integración: Tech Lead.
- Auditor final: Auditor sénior independiente.

## KPIs
| Indicador | Valor actual |
|---|---:|
| Tareas totales | 58 |
| DONE con evidencia | 43 (MC-001, MC-002, MC-004 a MC-021, MC-026 a MC-049) |
| En progreso | 0 |
| Pendientes (TODO) | 14 (MC-050 a MC-058) |
| Bloqueadas | 1 (MC-003 / MC-022 - API externa a la espera de credenciales oficiales) |
| Defectos P0/P1 abiertos | 0 |
| Última compilación | `assembleDebug` EXITOSA (APK generado 65.5 MB) |
| Último análisis estático | `lintDebug` EXITOSO (**0 errores, 0 warnings**) |
| Última suite de tests | `testDebugUnitTest` EXITOSA (**73/73 tests pasados**) |
| Hito Gate 6 (Calidad final y release v1.0.0-rc1) | APROBADO (2026-11-08) |
| Hito Gate 7 (Diseño M3, Navegación y Backstack) | COMPLETADO (2026-11-08) |
| Hito Gate 8 (Dashboard de Precios y Tiendas) | DONE / APROBADO (2026-11-08) |
| Hito Gate 9 (Ingreso Manual y Medidas Estándar) | IN_PROGRESS (MC-049 completada) |
| Hito Gate 10 (OCR Visual con Bounding Boxes) | PENDING |

## Hitos
| Hito | Estado | Aprobación |
|---|---|---|
| G0 Entorno y alcance | DONE | Usuario (Aprobado 2026-11-07) |
| G1 Arquitectura | DONE | Usuario + Tech Lead |
| G2 Esqueleto ejecutable | DONE | Auditor sénior + usuario |
| G3 Compras locales y OCR | DONE | Tech Lead + QA |
| G4 API temporal | BLOCKED | Documentación API + auditor sénior |
| G5 Autocompletado y descuentos | DONE | Auditor sénior |
| G6 Calidad final y release | DONE | Auditor sénior + Usuario (Aprobado v1.0.0-rc1) |
| G7 Sistema M3, Navegación y Backstack | DONE | Auditor sénior + Tech Lead (2026-11-08) |
| G8 Dashboard de Precios y Tiendas | DONE | Auditor sénior + Tech Lead (2026-11-08) |
| G9 Ingreso Manual y Medidas Estándar | IN_PROGRESS | Auditor sénior + Tech Lead |
| G10 OCR Visual y Bounding Boxes | TODO | Auditor sénior + Usuario |

## Control de Versiones, Git y CI/CD
| Campo | Estado |
|---|---|
| Rama base de releases | `main` (protegida para versiones estables con tags) |
| Rama base de desarrollo | `develop` (integración activa y destino de PRs) |
| Rama de trabajo activa | `feat/MC-039-v2-roadmap-and-governance` |
| Estado de repositorio | Conectado y sincronizado con GitHub |
| Remoto configurado | `origin` (`https://github.com/barbosaivan/MarketCompare.git`) |
| Integración Continua (CI) | GitHub Actions EXITOSO (Run 42552956093 - 100% verde en `develop`) |
| Automatización de Releases | GitHub Actions configurado (`.github/workflows/release.yml`) |
| Push remoto | `PUSHED` (`main`, `develop`, `feat/*`, `qa/*`, `audit/*`, `docs/*`) |

## Registro de cambios (Changelog)
- **2026-11-08 22:15:**
  - `MC-049` (G9): Implementación del flujo de ingreso manual de precios de góndola sin necesidad de factura física.
  - Componente modal M3 `ManualPriceEntrySheet` con captura de establecimiento (chips de tiendas frecuentes y texto libre), producto, marca, precio, cantidad base, selector de unidades de medida (`und`, `g`, `kg`, `ml`, `L`), presentación y selector de promociones/descuentos.
  - Creación de `ManualPriceRecorder` y método `recordManualPriceObservation` en el repositorio para registrar observaciones manuales en Room resolviendo unicidad de comercios y productos.
  - Desacople de DAOs Room en archivos individuales (`StoreDao.kt`, `ReceiptDao.kt`, `ProductDao.kt`, `ShoppingListDao.kt`) garantizando cumplimiento estricto del límite de 300 líneas.
  - Suite unitaria `ManualPriceRecorderTest` (4 pruebas añadidas, expandiendo la suite global a **73/73 pruebas exitosas**).
  - Verificación: 73/73 tests pasados al 100% y `lintDebug` aprobado con 0 errores y 0 warnings.
- **2026-11-08 21:50:**
  - `CI/CD Fix`: Corrección integral del pipeline de GitHub Actions (`android-ci.yml` y `release.yml`).
  - Causa raíz identificada: `org.gradle.java.home` con ruta de Windows (`C:\Program Files\Android\Android Studio2\jbr`) en `gradle.properties` provocaba `IllegalArgumentException` inmediata en runners Linux (Ubuntu).
  - Solución aplicada:
    1. Desacople de la ruta hardcodeada de Windows del repositorio hacia la configuración global del desarrollador (`~/.gradle/gradle.properties`).
    2. Agregado paso defensivo `sed -i '/org.gradle.java.home/d' gradle.properties` en los workflows para garantizar resiliencia en CI.
    3. Actualización de `actions/setup-java@v4` a `actions/setup-java@v5` (resolviendo advertencias de deprecación).
    4. Actualización de `gradle/actions/setup-gradle@v3` a `gradle/actions/setup-gradle@v4` (resolviendo fallo de caché 400 y advertencias de Node 20).
    5. Configuración de `if-no-files-found: ignore` en `actions/upload-artifact@v4`.
  - Verificación en vivo: Ejecución GitHub Actions `Run 42552956093` completada con veredicto **SUCCESS** (pruebas unitarias 69/69 y generación de APK aprobadas al 100%).
- **2026-11-08 21:30:**
  - `MC-048` (G8): Creación de pruebas unitarias (`PriceExplorerGroupingTest.kt`), auditoría sénior independiente y certificación de Gate 8.
  - Validación de suite unitaria completa con **69/69 pruebas exitosas** y análisis estático con `lintDebug` en **0 errores y 0 warnings**.
  - Publicación del informe de auditoría independiente [`docs/AUDIT_REPORT_GATE_8.md`](docs/AUDIT_REPORT_GATE_8.md) con veredicto **PASS**.
  - **Hito Gate 8 (Dashboard de Inteligencia de Precios y Explorador por Tienda) 100% completado y certificado**.
- **2026-11-08 21:00:**
  - `MC-047` (G8): Implementación de queries agregadas y analíticas por tienda en Room DAOs y repositorio (`getStoreStatistics`, `observePriceTimeline`, `observePriceObservationsBetween`).
  - Creación de suite unitaria `StoreCatalogRepositoryTest.kt` (5 pruebas).
- **2026-11-08 20:30:**
  - `MC-046` (G8): Implementación del directorio de comercios y explorador de catálogo filtrado por establecimiento (`StoreCatalogScreen`), con resolución de unicidad por `ProductCatalogResolver`.
  - Navegación bidireccional conectada desde chips en Home y ruta parametrizada `store_catalog/{storeName}`.
- **2026-11-08 19:40:**
  - `MC-045` (G8): Implementación del buscador universal de precios multicomercio y ModalBottomSheet `ProductCrossStoreComparisonSheet`.
  - Extracción del motor puro de dominio `CrossStoreComparisonCalculator` con cálculo de dispersión, ranking de menor a mayor precio y ahorro relativo respecto al comercio más costoso.
  - Creación de suite unitaria `CrossStoreComparisonCalculatorTest.kt` (4 pruebas).
- **2026-11-08 18:30:**
  - `MC-044` (G8): Rediseño integral de la pantalla principal de registro de gastos a explorador de inteligencia de precios (`HomePriceExplorerScreen`).
  - Incorporación de barra de búsqueda universal, chips de filtrado por comercio, tarjetas KPI de monitoreo y feed cronológico de observaciones de precios con badges de frescura.
- **2026-11-08 17:15:**
  - `MC-043` (G7): Refactorización y extracción exhaustiva de componentes en la capa de presentación.
  - Creación de componentes reutilizables: `DiscountAuditDialog`, `EditableReceiptItemCard`, `ReceiptReviewBottomBar`, `BasketSummaryCard`, `ShoppingListItemRow` y `ShoppingListAddCard`.
  - Reducción drástica del tamaño de archivos: `ReceiptReviewScreen.kt` pasó de 573 a 153 líneas; `ShoppingListScreen.kt` pasó de 442 a 133 líneas. Todos los archivos de la capa de UI se encuentran ahora estrictamente bajo el límite de 260 líneas (< 300 líneas) y métodos < 50 líneas.
  - **Hito Gate 7 completado al 100%** satisfaciendo todos sus criterios de calidad y usabilidad.
  - Verificación: 53/53 pruebas unitarias exitosas y `lintDebug` aprobado con 0 errores y 0 warnings.
- **2026-11-08 16:50:**
  - `MC-042` (G7): Implementación del gestor de historial de navegación `AppNavigator` y del manejador de botón atrás nativo `BackHandler` en Compose.
  - La aplicación gestiona una pila de navegación histórica ordenada: presionar "Atrás" o el botón de retroceso de la barra superior regresa ordenadamente a la pantalla previa (por ejemplo, de Revisión a Escaneo y de Escaneo a Historial) en lugar de cerrar la app.
  - Creación de suite unitaria `AppNavigatorTest` (6 pruebas añadidas, expandiendo la suite global a **53/53 pruebas exitosas**).
  - Verificación: 53/53 pruebas unitarias exitosas y `lintDebug` aprobado con 0 errores y 0 warnings.
- **2026-11-08 16:30:**
  - `MC-041` (G7): Extracción del componente modular `AppBottomNavigationBar` y reestructuración de la jerarquía del Scaffold raíz en `MainActivity.kt`.
  - La barra de navegación inferior permanece ahora 100% visible y accesible en todas las vistas (Historial, Escaneo, Revisión y Lista de Compras), eliminando el ocultamiento del menú durante la navegación.
  - El ítem "Escanear" se resalta dinámicamente tanto en la captura como en la revisión de tickets.
  - Verificación: 47/47 pruebas unitarias exitosas y `lintDebug` aprobado con 0 errores y 0 warnings.
- **2026-11-08 15:50:**
  - `MC-040` (G7): Implementación de la paleta moderna Material 3 en `Color.kt` y `Theme.kt` (Deep Pine como primario, Warm Amber como secundario, Electric Indigo como terciario).
  - Incorporados tokens de elevación tonal M3 (`surfaceContainerLowest` hasta `surfaceContainerHighest`) para tema claro y tema oscuro.
  - Creado proveedor semántico `MaterialTheme.semanticColors` (`LocalMarketCompareSemanticColors`) para categorías OCR y semáforo de frescura sin hardcodear colores en componentes.
  - Verificación: 47/47 pruebas unitarias exitosas y `lintDebug` aprobado con 0 errores y 0 advertencias.
- **2026-11-08 15:40:**
  - Inicio de la **Fase v2.0**: Incorporación de los 14 nuevos requerimientos acordados con el usuario.
  - `MC-039` (G7): Formalización del protocolo de revisión multi-agente en 7 etapas y adopción de la política de calidad pre-commit obligatoria en `PROJECT_RULES.md` y `AGENT_TEAM.md`.
  - Definición y registro en `QUALITY_GATES.md` y `BACKLOG.csv` de las compuertas Gate 7, Gate 8, Gate 9 y Gate 10 (tareas MC-039 a MC-058).
- **2026-11-08 14:40:**
  - `MC-034` (G6): Aprobación formal de entrega y release por parte del usuario bajo la versión `v1.0.0-rc1`.
  - Gate 6 cerrado formalmente con 100% de criterios aprobados.
  - Fusión de versión estable a `main` y etiquetado Git con tag semántico `v1.0.0-rc1`.
- **2026-11-08 14:30:**
  - `MC-033` & `MC-038` (G6): Cierre y sincronización documental completa de todas las especificaciones técnicas del proyecto.
  - Actualización de `docs/README.md`, `docs/DATA_MODEL.md`, `docs/PRODUCT_OVERVIEW.md`, `docs/FUNCTIONALITY.md`, `docs/ARCHITECTURE.md`, `docs/QUALITY_AND_TESTING.md`, `docs/CHANGELOG_TECHNICAL.md`, `docs/GLOSSARY.md`, `docs/INTEGRATIONS.md`, y raíz `README.md`.
  - Auditoría sénior de consistencia documental (`MC-038`): Verificación exhaustiva de alineación entre código fuente, tests (47/47), reportes de lint (0/0) y documentación de arquitectura.
  - Hito Gate 6 cerrado con éxito. Proyecto preparado para `MC-034` (Aprobación final de release por parte del usuario).
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
