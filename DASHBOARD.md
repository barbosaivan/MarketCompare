# MarketCompare — Dashboard de proyecto

> Estado actual: Gate 2 (Esqueleto Ejecutable) completado. El proyecto compila y empaqueta APK de depuración con éxito, y la suite de pruebas unitarias pasa con código de salida 0.

## Resumen
- Fase actual: `GATE 2 — Esqueleto ejecutable (Completada)` -> Listo para `GATE 3 — Compras locales y OCR`
- Estado global: `IN PROGRESS`
- Rama activa: `feat/MC-010-project-scaffold`
- Bloqueos conocidos: API externa de precios (`MC-003` - BLOCKED por falta de proveedor documentado).
- Plataforma: Android (Target/Compile SDK 37, Min SDK 26, Kotlin 2.2.10, AGP 9.2.1, Compose 2026.05.01, Room 2.8.4).
- Persistencia: Room local exclusiva para compras confirmadas (`MarketCompareDatabase`).
- Precios externos: Consulta efímera en memoria (no persistente en Room, archivos, logs ni caché).
- Responsable de integración: Tech Lead.
- Auditor final: Auditor sénior independiente.

## KPIs
| Indicador | Valor actual |
|---|---:|
| Tareas totales | 38 |
| DONE con evidencia | 12 (MC-001, MC-002, MC-004, MC-005, MC-006, MC-007, MC-008, MC-009, MC-010, MC-011, MC-012, MC-013) |
| En revisión | 0 |
| Bloqueadas | 1 (MC-003 - API externa sin proveedor oficial) |
| Defectos P0/P1 abiertos | 0 |
| Última compilación | `assembleDebug` EXITOSA (APK generado 64.7 MB) |
| Última suite de tests | `testDebugUnitTest` EXITOSA (MoneyTest, PurchaseRepositoryTest) |
| Hito Gate 0 (Entorno y alcance) | APROBADO (2026-11-07) |
| Hito Gate 1 (Arquitectura y modelado) | COMPLETADO (2026-11-07) |
| Hito Gate 2 (Esqueleto ejecutable) | COMPLETADO (2026-11-07) |

## Hitos
| Hito | Estado | Aprobación |
|---|---|---|
| G0 Entorno y alcance | DONE | Usuario (Aprobado 2026-11-07) |
| G1 Arquitectura | DONE | Usuario + Tech Lead |
| G2 Esqueleto ejecutable | DONE | Auditor sénior + usuario |
| G3 Compras locales y OCR | TODO | Auditor sénior + usuario |
| G4 API temporal | BLOCKED | Documentación API + auditor sénior |
| G5 Autocompletado y descuentos | TODO | Auditor sénior |
| G6 Calidad final | TODO | Auditor sénior + usuario |

## Control de Versiones y Git
| Campo | Estado |
|---|---|
| Rama activa | `feat/MC-010-project-scaffold` |
| Estado de repositorio | Conectado y sincronizado con GitHub |
| Remoto configurado | `origin` (`https://github.com/barbosaivan/MarketCompare.git`) |
| Push remoto | `PUSHED` (`main`, `docs/MC-005-architecture-adr`, `feat/MC-010-project-scaffold`) |
| Pull Request | Listo para PR de Gate 2 |

## Registro de cambios (Changelog)
- **2026-11-07 17:50:**
  - `MC-010` (G2): Creado proyecto ejecutable con Gradle 9.4.1, AGP 9.2.1, Kotlin 2.2.10 y Version Catalog (`gradle/libs.versions.toml`).
  - `MC-011` (G2): Configurado tema Material 3 (`MarketCompareTheme`), esquema claro/oscuro y recursos centralizados en `strings.xml`. Cero cadenas o colores hardcodeados.
  - `MC-012` (G2): Definida navegación tipada (`Screen`), `MainActivity` y `HistoryScreen` con estados vacío y poblado.
  - `MC-013` (G2): Implementada base de datos Room (`MarketCompareDatabase`), entidades (`StoreEntity`, `ReceiptEntity`, `ReceiptItemEntity`, `ProductEntity`, `ProductAliasEntity`), DAOs y `PurchaseRepositoryImpl`.
  - Verificación: `assembleDebug` completado con éxito y pruebas unitarias (`MoneyTest`, `PurchaseRepositoryTest`) aprobadas con 100% de éxito.
