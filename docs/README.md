# Documentación de MarketCompare

Índice y fuente de verdad documental del proyecto.

## Especificaciones de Arquitectura y Producto
- [`PRODUCT_OVERVIEW.md`](PRODUCT_OVERVIEW.md): propósito, alcance y estado del producto.
- [`ARCHITECTURE.md`](ARCHITECTURE.md): capas, dependencias, flujo OCR y límites arquitectónicos.
- [`FUNCTIONALITY.md`](FUNCTIONALITY.md): funcionalidades, pipeline de recibos, canasta y autocompletado.
- [`DATA_MODEL.md`](DATA_MODEL.md): entidades Room, relaciones, lista de compras y DTOs en memoria.
- [`DESIGN_SYSTEM.md`](DESIGN_SYSTEM.md): Material 3, tokens, colores, accesibilidad y componentes.
- [`PROJECT_CONFIGURATION.md`](PROJECT_CONFIGURATION.md): versiones, Gradle, SDK, dependencias y comandos.
- [`INTEGRATIONS.md`](INTEGRATIONS.md): OCR (ML Kit), PDF, CameraX y API externa.
- [`GLOSSARY.md`](GLOSSARY.md): términos del dominio (Money, Frescura, Variantes, Alias).
- [`DEVELOPMENT_GUIDE.md`](DEVELOPMENT_GUIDE.md): onboarding, Gitflow, Conventional Commits y releases.
- [`adr/`](adr/): Registro de Decisiones Arquitectónicas (ADRs 0001, 0002, 0003).

## Auditorías de Calidad y Reportes (Gate 6)
- [`STATIC_ANALYSIS_AUDIT.md`](STATIC_ANALYSIS_AUDIT.md): auditoría de Android Lint (0 errores, 0 warnings), Compose y dependencias.
- [`TEST_SUITE_REPORT.md`](TEST_SUITE_REPORT.md): certificación de la suite completa de 47 pruebas unitarias (100% éxito).
- [`AUDIT_REPORT.md`](AUDIT_REPORT.md): informe de auditoría sénior independiente de código, arquitectura y privacidad (Veredicto PASS).
- [`QUALITY_AND_TESTING.md`](QUALITY_AND_TESTING.md): estrategia, pirámide de pruebas y criterios de compuerta.
- [`CHANGELOG_TECHNICAL.md`](CHANGELOG_TECHNICAL.md): evolución técnica y registro cronológico de cambios.

## Control de Calidad y Seguimiento
- [`../QUALITY_GATES.md`](../QUALITY_GATES.md): criterios de aceptación formales por compuerta de calidad (Gates 0 a 6).
- [`../DASHBOARD.md`](../DASHBOARD.md): tablero de seguimiento en tiempo real con KPIs, estado de hitos y trazabilidad Git.
- [`../BACKLOG.csv`](../BACKLOG.csv): inventario atómico de tareas con dependencias, estados y hashes de commit.
