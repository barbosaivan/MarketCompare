# MarketCompare — Dashboard de proyecto

> Estado actual: Gate 1 (Arquitectura y Modelado) completado en rama `docs/MC-005-architecture-adr`. Especificaciones, contratos de datos, ADRs y estrategias de testing finalizadas.

## Resumen
- Fase actual: `GATE 1 — Arquitectura y Modelado (Completada)` -> Listo para `GATE 2 — Esqueleto ejecutable`
- Estado global: `IN PROGRESS`
- Rama actual: `docs/MC-005-architecture-adr`
- Bloqueos conocidos: API externa de precios (`MC-003` - BLOCKED por falta de proveedor documentado).
- Plataforma: Android inicial (preparado para KMP / Compose Multiplatform).
- Persistencia: Room local exclusiva para compras confirmadas.
- Precios externos: Consulta efímera en memoria (no persistente en Room, archivos, logs ni caché).
- Responsable de integración: Tech Lead.
- Auditor final: Auditor sénior independiente.

## KPIs
| Indicador | Valor actual |
|---|---:|
| Tareas totales | 38 |
| DONE con evidencia | 8 (MC-001, MC-002, MC-004, MC-005, MC-006, MC-007, MC-008, MC-009) |
| En revisión | 0 |
| Bloqueadas | 1 (MC-003 - API externa sin proveedor oficial) |
| Defectos P0/P1 abiertos | 0 |
| Hito Gate 0 (Entorno y alcance) | APROBADO (2026-11-07) |
| Hito Gate 1 (Arquitectura y modelado) | COMPLETADO (2026-11-07) |

## Hitos
| Hito | Estado | Aprobación |
|---|---|---|
| G0 Entorno y alcance | DONE | Usuario (Aprobado 2026-11-07) |
| G1 Arquitectura | DONE | Usuario + Tech Lead |
| G2 Esqueleto ejecutable | TODO | Auditor sénior + usuario |
| G3 Compras locales y OCR | TODO | Auditor sénior + usuario |
| G4 API temporal | BLOCKED | Documentación API + auditor sénior |
| G5 Autocompletado y descuentos | TODO | Auditor sénior |
| G6 Calidad final | TODO | Auditor sénior + usuario |

## Control de Versiones y Git
| Campo | Estado |
|---|---|
| Rama activa | `docs/MC-005-architecture-adr` |
| Estado de repositorio | Local (100% en disco local, sin remotos configurados) |
| Commits locales | Listos para commit de Gate 1 |
| Push remoto | `PUSH_BLOCKED` (sin remoto configurado aún) |
| Pull Request | No aplica (preparación local de repositorio base) |

## Documentación viva
| Documento | Estado | Responsable |
|---|---|---|
| docs/PRODUCT_OVERVIEW.md | PLANNED | Tech Lead |
| docs/ARCHITECTURE.md | IMPLEMENTED (G1) | Arquitectura |
| docs/FUNCTIONALITY.md | IMPLEMENTED (G1) | OCR/UI/Datos |
| docs/DATA_MODEL.md | IMPLEMENTED (G1) | Datos |
| docs/DESIGN_SYSTEM.md | IMPLEMENTED (G1) | UI/UX |
| docs/PROJECT_CONFIGURATION.md | VERIFIED (G0) | Build/Documentación |
| docs/INTEGRATIONS.md | API bloqueada | Integraciones |
| docs/QUALITY_AND_TESTING.md | IMPLEMENTED (G1) | QA |
| docs/CHANGELOG_TECHNICAL.md | ACTUALIZADO | Documentación |
| docs/GLOSSARY.md | Inicializado | Documentación |
| docs/DEVELOPMENT_GUIDE.md | Inicializado | Documentación |
| docs/adr/ | ADR 0001, 0002, 0003 Aceptados | Arquitectura |

## Registro de cambios (Changelog)
- **2026-11-07 17:13:**
  - `MC-005` (G1): Redactados y aprobados ADR 0001 (API efímera), ADR 0002 (Límites de capas) y ADR 0003 (Manejo exacto de dinero en unidades menores). Actualizado `docs/ARCHITECTURE.md`.
  - `MC-006` (G1): Diseñado modelo relacional y de dominio (`docs/DATA_MODEL.md`). Definido DTO en memoria `ExternalPriceQuote`.
  - `MC-007` (G1): Definido contrato del motor de parseo y pipeline OCR/PDF (`docs/FUNCTIONALITY.md`).
  - `MC-008` (G1): Especificados tokens de diseño Material 3 y mapa de navegación tipado (`docs/DESIGN_SYSTEM.md`).
  - `MC-009` (G1): Formalizada estrategia de pruebas unitarias, integración y fixtures sintéticas (`docs/QUALITY_AND_TESTING.md`).
