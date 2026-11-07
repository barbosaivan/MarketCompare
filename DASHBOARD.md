# MarketCompare — Dashboard de proyecto

> Estado actual: Gate 3 (Compras locales y Pipeline OCR) completado. El pipeline de importación multicanal (Cámara, Galería, PDF), extracción ML Kit, parseo heurístico de facturas colombianas, pantalla de revisión editable y persistencia transaccional en Room están 100% operativos. La suite de pruebas unitarias (`ReceiptParserTest`, `MoneyTest`, `PurchaseRepositoryTest`) pasa al 100% y el APK de depuración se ensambla limpiamente.

## Resumen
- Fase actual: `GATE 3 — Compras locales y OCR (Completada)` -> Listo para `GATE 4 — API temporal` (Actualmente bloqueada por proveedor externo).
- Estado global: `IN PROGRESS`
- Rama de integración activa: `develop`
- Rama de hito: `feat/MC-014-receipt-pipeline`
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
| DONE con evidencia | 23 (MC-001, MC-002, MC-004 a MC-021, MC-035, MC-036, MC-037) |
| En revisión | 0 |
| Bloqueadas | 1 (MC-003 - API externa sin proveedor oficial) |
| Defectos P0/P1 abiertos | 0 |
| Última compilación | `assembleDebug` EXITOSA (APK generado 65.5 MB) |
| Última suite de tests | `testDebugUnitTest` EXITOSA (10/10 tests pasados: ReceiptParserTest, MoneyTest, PurchaseRepositoryTest) |
| Hito Gate 0 (Entorno y alcance) | APROBADO (2026-11-07) |
| Hito Gate 1 (Arquitectura y modelado) | COMPLETADO (2026-11-07) |
| Hito Gate 2 (Esqueleto ejecutable) | COMPLETADO (2026-11-07) |
| Hito Gate 3 (Compras locales y OCR) | COMPLETADO (2026-11-07) |

## Hitos
| Hito | Estado | Aprobación |
|---|---|---|
| G0 Entorno y alcance | DONE | Usuario (Aprobado 2026-11-07) |
| G1 Arquitectura | DONE | Usuario + Tech Lead |
| G2 Esqueleto ejecutable | DONE | Auditor sénior + usuario |
| G3 Compras locales y OCR | DONE | Tech Lead + QA |
| G4 API temporal | BLOCKED | Documentación API + auditor sénior |
| G5 Autocompletado y descuentos | TODO | Auditor sénior |
| G6 Calidad final | TODO | Auditor sénior + usuario |

## Control de Versiones, Git y CI/CD
| Campo | Estado |
|---|---|
| Rama base de releases | `main` (protegida para versiones estables con tags) |
| Rama base de desarrollo | `develop` (integración activa y destino de PRs) |
| Rama de trabajo activa | `feat/MC-014-receipt-pipeline` |
| Estado de repositorio | Conectado y sincronizado con GitHub |
| Remoto configurado | `origin` (`https://github.com/barbosaivan/MarketCompare.git`) |
| Integración Continua (CI) | GitHub Actions configurado (`.github/workflows/android-ci.yml`) |
| Automatización de Releases | GitHub Actions configurado (`.github/workflows/release.yml`) |
| Push remoto | `PUSHED` (`main`, `develop`, `feat/MC-014-receipt-pipeline`) |

## Registro de cambios (Changelog)
- **2026-11-07 18:25:**
  - `MC-014` (G3): Implementada selección multicanal de facturas (CameraX con archivo temporal y `FileProvider`, Photo Picker de galería y OpenDocument SAF para PDFs).
  - `MC-015` & `MC-016` (G3): Implementado `DocumentTextExtractor` combinando Google ML Kit Text Recognition, `PdfRenderer` para PDFs escaneados y lectura de streams para PDFs con texto embebido.
  - `MC-017` (G3): Implementado motor de parseo heurístico `ReceiptParser` para facturas colombianas (reconocimiento de comercios Éxito, D1, Ara, Carulla, Olímpica, Jumbo, etc., extracción de NIT, fechas, líneas de artículos con unidades y empaques, detección de descuentos promocionales y descarte de pie fiscal/medios de pago).
  - `MC-018` (G3): Desarrollada pantalla `ReceiptReviewScreen` interactiva: edición en vivo de tienda, inclusión/exclusión de artículos, modificación de descripciones y recálculo automático de total antes del guardado.
  - `MC-019` & `MC-020` (G3): Guardado transaccional en Room (`saveConfirmedPurchase`) enlazando tienda, recibo y artículos con confirmación explícita del usuario.
  - `MC-021` (G3): Aritmética financiera estricta en enteros menores (`Money`) en todo el flujo de ingesta y revisión.
  - Verificación: 10/10 pruebas unitarias exitosas (`ReceiptParserTest`, `MoneyTest`, `PurchaseRepositoryTest`) y `assembleDebug` completado generando `app-debug.apk` (65.5 MB).
