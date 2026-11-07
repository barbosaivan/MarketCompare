# MarketCompare — Dashboard de proyecto

> Estado actual: Gate 0 aprobado. Entorno diagnosticado, documentación promovida a la raíz y workspace preparado para Gate 1 (Arquitectura y Modelado).

## Resumen
- Fase actual: `GATE 1 — Arquitectura y Contratos`
- Estado global: `IN PROGRESS`
- Hito anterior: `GATE 0 — Entorno y alcance` (Aprobado por el usuario el 2026-11-07)
- Bloqueos conocidos: API externa de precios (`MC-003` - BLOCKED por falta de documentación oficial del proveedor).
- Plataforma: Android inicial (KMP / Compose Multiplatform).
- Persistencia: Room local exclusiva para compras confirmadas.
- Precios externos: Consulta efímera en memoria (no persistente en Room, archivos, logs ni caché).
- Responsable de integración: Tech Lead.
- Auditor final: Auditor sénior independiente.

## KPIs
| Indicador | Valor actual |
|---|---:|
| Tareas totales | 38 |
| DONE con evidencia | 3 (MC-001, MC-002, MC-004) |
| En revisión | 0 |
| Bloqueadas | 1 (MC-003 - API externa sin proveedor oficial) |
| Defectos P0/P1 abiertos | 0 |
| Última compilación | Pendiente creación de esqueleto (Gate 2) |
| Última suite de tests | Pendiente |
| Gate 0 (Entorno y alcance) | APROBADO |

## Hitos
| Hito | Estado | Aprobación |
|---|---|---|
| G0 Entorno y alcance | DONE | Usuario (Aprobado 2026-11-07) |
| G1 Arquitectura | IN_PROGRESS | Usuario |
| G2 Esqueleto ejecutable | TODO | Auditor sénior + usuario |
| G3 Compras locales y OCR | TODO | Auditor sénior + usuario |
| G4 API temporal | BLOCKED | Documentación API + auditor sénior |
| G5 Autocompletado y descuentos | TODO | Auditor sénior |
| G6 Calidad final | TODO | Auditor sénior + usuario |

## Control de Versiones y Git
| Campo | Estado |
|---|---|
| Rama principal | `main` |
| Estado de repositorio | Inicializado localmente (`git init -b main`) |
| Commits locales | Listos para commit inicial de infraestructura |
| Push remoto | `PUSH_BLOCKED` (sin remoto configurado aún) |
| Pull Request | No aplica (preparación local de repositorio base) |
| Revisión sénior | Lista para Gate 1 |

## Documentación viva
| Documento | Estado | Responsable |
|---|---|---|
| docs/PRODUCT_OVERVIEW.md | PLANNED | Tech Lead |
| docs/ARCHITECTURE.md | IN_PROGRESS | Arquitectura |
| docs/FUNCTIONALITY.md | PLANNED | OCR/UI/Datos |
| docs/DATA_MODEL.md | PLANNED | Datos |
| docs/DESIGN_SYSTEM.md | PLANNED | UI/UX |
| docs/PROJECT_CONFIGURATION.md | VERIFIED | Build/Documentación |
| docs/INTEGRATIONS.md | API bloqueada | Integraciones |
| docs/QUALITY_AND_TESTING.md | PLANNED | QA |
| docs/CHANGELOG_TECHNICAL.md | Actualizado | Documentación |
| docs/GLOSSARY.md | Inicializado | Documentación |
| docs/DEVELOPMENT_GUIDE.md | Inicializado | Documentación |
| docs/adr/ | ADR 0001 propuesta | Arquitectura |

## Registro de cambios (Changelog)
- **2026-11-07 16:28:**
  - `MC-001` (G0): Inspeccionado workspace y estado de Git.
  - `MC-002` (G0): Registrado toolchain (Android Studio AI-262, OpenJDK 25.0.4 JBR, Git 2.45.1).
  - `MC-004` (G0): Usuario aprueba alcance MVP y reglas de calidad. Gate 0 completado.
  - Promovidos todos los archivos de arquitectura, reglas, backlog y docs a la raíz del proyecto.
  - Creado `.gitignore` para Android/KMP/Gradle/IDE.
