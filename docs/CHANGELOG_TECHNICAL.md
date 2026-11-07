# Changelog Técnico — MarketCompare

## [Fase Gate 1: Arquitectura y Modelado] — 2026-11-07

### Añadido
- **ADR 0001 (Aceptado):** Decisión arquitectónica para separar compras locales de cotizaciones API efímeras sin persistencia (`docs/adr/0001-local-first-ephemeral-api.md`).
- **ADR 0002 (Aceptado):** Definición de límites y dependencias de Clean Architecture pragmática en 4 capas (`docs/adr/0002-clean-architecture-layer-boundaries.md`).
- **ADR 0003 (Aceptado):** Prohibición de `Double`/`Float` y especificación del Value Object `Money` en unidades menores (`docs/adr/0003-money-representation-integer-minor-units.md`).
- **Arquitectura completa (`docs/ARCHITECTURE.md`):** Diagramas de flujo de ingesta OCR, flujo de comparación efímera y principios de desacoplamiento.
- **Modelo de datos (`docs/DATA_MODEL.md`):** Definición de entidades Room, esquema relacional, Value Objects (`Money`, `Quantity`) y DTO efímero en memoria `ExternalPriceQuote`.
- **Pipeline de recibos y funcionalidades (`docs/FUNCTIONALITY.md`):** Reglas de clasificación heurística, estados de confianza y contrato de revisión editable.
- **Sistema de diseño (`docs/DESIGN_SYSTEM.md`):** Tokens Material 3, prohibición de strings/colores hardcodeados y mapa de navegación tipado.
- **Estrategia de pruebas (`docs/QUALITY_AND_TESTING.md`):** Especificación de suites unitarias, integración en memoria y fixtures sintéticas.

## [Fase Gate 0: Entorno y Alcance] — 2026-11-07
- Inspección del entorno y toolchain: Android Studio AI-262, OpenJDK 25.0.4 JBR, Git 2.45.1.
- Inicialización del repositorio Git local (`main`) y promoción de la documentación a la raíz.
- Creación de `.gitignore` para Android/KMP.
