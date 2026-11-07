# Arquitectura

Estado: PLANNED hasta que ADR y código la confirmen. Clean Architecture pragmática en un módulo inicial.

- Presentation: Compose, navegación, ViewModels y estados.
- Domain: entidades, value objects, reglas, contratos y casos de uso.
- Data: Room, repositorios, mappers, parser y cliente API.
- Platform Android: CameraX, Photo Picker/SAF, PdfRenderer y ML Kit.

Domain no depende de Android, Compose, Room ni HTTP. UI no ejecuta SQL/OCR. ViewModels coordinan casos de uso. No crear módulos o abstracciones sin beneficio real.

Flujos: documento → extracción → parser → matching → revisión → confirmación → transacción Room; compra local + API → comparación en memoria → UI diferenciada → descartar cotización; búsqueda → alias/catálogo local → ranking por marca, variante, contenido y presentación.
