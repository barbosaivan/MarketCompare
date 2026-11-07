# ADR 0002 — Límites de capas en Clean Architecture pragmática

- **Estado:** ACEPTADO (ACCEPTED)
- **Fecha:** 2026-11-07
- **Decisores:** Tech Lead, Arquitectura, Usuario

## Contexto
MarketCompare debe mantener una arquitectura limpia y desacoplada que permita evolucionar la aplicación hacia Kotlin Multiplatform (KMP) y futuros backends/sincronizaciones sin reescribir la lógica de negocio ni mezclar responsabilidades de presentación, procesamiento de documentos o persistencia.
Al mismo tiempo, para el MVP se debe evitar la sobreingeniería (como micro-módulos Gradle excesivos o dependencias innecesarias de inyección de código complejas como Hilt si la escala inicial no lo justifica).

## Decisión
Se implementa una arquitectura **Clean Architecture en un solo módulo de aplicación**, dividida en 4 capas estrictas mediante paquetes:

1. **`domain` (Capa de Dominio):**
   - **Contenido:** Entidades de negocio (`Receipt`, `ReceiptItem`, `Product`, `ProductVariant`, `Store`, `ShoppingList`), Value Objects (`Money`, `UnitPrice`, `ConfidenceScore`), interfaces de repositorios (`PurchaseRepository`, `ProductCatalogRepository`, `ExternalPriceProvider`), y Casos de Uso (`SaveConfirmedReceiptUseCase`, `ComparePricesUseCase`, `EstimateShoppingListUseCase`).
   - **Regla estricta:** Cero dependencias del framework de Android, Room, Ktor, Jetpack Compose o ML Kit. Solo Kotlin puro y corrutinas estándar.

2. **`data` (Capa de Datos):**
   - **Contenido:**
     - `local`: Base de datos Room KMP, DAOs, entidades Room (`ReceiptEntity`, `ItemEntity`, etc.), conversores y migraciones.
     - `remote`: Cliente HTTP Ktor (configurado con caché en memoria deshabilitada/estricta), DTOs serializables de cotizaciones.
     - `parser`: Motor clasificador de líneas y extracción heurística de tickets.
     - `repository`: Implementaciones de las interfaces de dominio (`PurchaseRepositoryImpl`, `ExternalPriceRepositoryImpl`).
   - **Regla:** Mappers explícitos entre entidades Room/DTOs y modelos de dominio.

3. **`presentation` (Capa de Presentación):**
   - **Contenido:** Vistas Jetpack Compose, ViewModels, estados de UI inmutables (`StateFlow<UiState>`), navegación, recursos de texto y sistema de diseño Material 3.
   - **Regla:** Los composables nunca invocan DAOs, repositorios ni APIs directamente. Solo observan el estado emitido por el ViewModel e informan eventos de usuario.

4. **`platform/android` (Capa de Infraestructura / Plataforma):**
   - **Contenido:** Integraciones con APIs del sistema operativo Android: CameraX para captura, contratos SAF/Photo Picker, Google ML Kit Text Recognition y `PdfRenderer`.
   - **Regla:** Expone adaptadores hacia la capa `data`/`domain` sin filtrar objetos dependientes de Android (como `Bitmap` o `Context`) hacia la lógica central del negocio.

```
       +-----------------------+
       |     presentation      | (Compose UI, ViewModels, UI States)
       +-----------+-----------+
                   |
                   v
       +-----------------------+
       |        domain         | <---+ (Pure Kotlin: Use Cases, Models, Contracts)
       +-----------+-----------+     |
                   ^                 |
                   |                 |
       +-----------+-----------+     |
       |         data          |-----+ (Room, Ktor HTTP, Parsers, Repositories)
       +-----------+-----------+
                   ^
                   |
       +-----------+-----------+
       |   platform/android    | (CameraX, ML Kit, PdfRenderer, SAF)
       +-----------------------+
```

## Consecuencias
- **Positivas:** Capa de dominio altamente testeable con JUnit estándar sin emuladores; componentes de UI desacoplados; fácil migración de persistencia o red sin tocar las reglas de negocio.
- **Negativas:** Requiere mapeos manuales entre modelos de entidad Room, modelos de dominio y modelos de UI.

