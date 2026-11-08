# Arquitectura del Sistema — MarketCompare

- **Estado:** `IMPLEMENTED` (Gates 0, 1, 2, 3, 5, 6 completados; Gate 4 `BLOCKED` a la espera de credenciales de API externa)
- **Responsable:** Tech Lead & Equipo de Arquitectura
- **Última actualización:** 2026-11-08

## 1. Visión General y Principios
MarketCompare implementa una Clean Architecture pragmática en un único módulo Android para el MVP, diseñada para ser extensible a Kotlin Multiplatform (KMP).

### Principios Fundamentales
1. **Local-First & Offline-First:** Los datos de compras confirmadas y la lista de compras se almacenan localmente en SQLite mediante Room.
2. **Precios Externos Efímeros:** Las cotizaciones de APIs externas nunca se persisten en base de datos, caché persistente ni archivos. Residen únicamente en el flujo en memoria del ViewModel mientras la pantalla esté activa (ADR 0001).
3. **Independencia del Dominio:** La lógica de negocio no tiene dependencias de Android SDK ni de librerías de UI o persistencia (ADR 0002).
4. **Precisión Matemática:** Prohibición estricta de `Double`/`Float` para importes de dinero (ADR 0003).

## 2. Mapa de Capas y Responsabilidades

```
+-------------------------------------------------------------------+
|                        PRESENTATION LAYER                         |
|  - Jetpack Compose UI (Material Design 3, colorScheme, No Hardcode)|
|  - Barra de Navegación Inferior (Historial, Escáner, Lista)       |
|  - Pantallas: PurchaseHistory, ReceiptReview, ShoppingList        |
|  - Diálogos: DiscountAuditDialog, Anti-Collision Warning Dialog   |
|  - ViewModels (Coroutines, StateFlow, UiState inmutable)          |
|  - Navigation (Type-safe Compose Destinations)                   |
|  - Localized String Resources & Formateadores Moneda/Fecha       |
+---------------------------------+---------------------------------+
                                  | Observa UiState / Envía Intentos
                                  v
+-------------------------------------------------------------------+
|                           DOMAIN LAYER                            |
|  - Modelos / Entidades: Receipt, ReceiptItem, Product, Store,     |
|                         ShoppingListItem, BasketEstimate          |
|  - Value Objects: Money (minor units Long), ProductQuantity       |
|  - Enums de Dominio: DiscountType, UnitType, PriceFreshness       |
|  - Motores de Dominio:                                            |
|    * ProductNormalizer (Marcas, presentaciones, fingerprints)     |
|    * ProductAutocompleteEngine (Ranking y compatibilidad)        |
|    * DiscountClassifier (Categorización y evidencia de ahorro)    |
|    * BasketEstimator (Cálculo de presupuesto y frescura)          |
|  - Contratos de Repositorio (Interfaces puras):                  |
|    * PurchaseRepository, ShoppingListRepository                   |
+---------------------------------+---------------------------------+
                                  ^ Implementa interfaces
                                  |
+---------------------------------+---------------------------------+
|                            DATA LAYER                             |
|  - Room Local DB (AppDatabase v1, TypeConverters, DAOs):          |
|    * ReceiptDao, ReceiptItemDao, StoreDao, ProductDao,            |
|    * ProductAliasDao, ShoppingListDao                             |
|  - Entidades de Persistencia: Room Entities relacionales          |
|  - Implementación Repositorio: PurchaseRepositoryImpl             |
|  - Motor de Parseo de Recibos (Clasificación de líneas, OCR draft)|
|  - Cliente HTTP Ktor (Caché desactivada, solo en memoria)        |
+---------------------------------+---------------------------------+
                                  ^
                                  | Provee datos crudos
+---------------------------------+---------------------------------+
|                     PLATFORM / ANDROID LAYER                      |
|  - CameraX (Captura fotográfica optimizada para texto)            |
|  - Photo Picker (Selección de imágenes sin permisos invasivos)    |
|  - Storage Access Framework (Importación de archivos PDF)         |
|  - ML Kit Text Recognition (OCR local on-device para imágenes)    |
|  - PdfRenderer (Fallback rasterizado para PDFs escaneados)        |
|  - Extracción directa de texto para facturas PDF digitales        |
+-------------------------------------------------------------------+
```

## 3. Flujos Críticos de la Aplicación

### A. Flujo de Ingesta y Confirmación de Recibos (Pipeline OCR)
```
[Cámara / Archivo PDF / Galería]
              │
              ▼
   [Extracción de Texto Crudo]
    ├── PDF Nativo: Extracción de streams de texto
    └── Imagen / PDF Escaneado: ML Kit Text Recognition
              │
              ▼
    [ReceiptParser Engine]
    ├── Identifica encabezado (Tienda, NIT/RUT, Fecha)
    ├── Clasifica líneas: Ítems vs Descuentos vs Totales/Impuestos
    ├── Extrae: descripción cruda, cantidad, precio lista, descuento
    └── Asigna puntuación de confianza heurística
              │
              ▼
  [Pantalla de Revisión Editable] ◄─── (El usuario edita, añade o excluye)
    ├── Autocompletado con ProductAutocompleteEngine
    ├── Validación de variantes con ProductNormalizer
    └── Inspección de beneficios en DiscountAuditDialog
              │
   [Usuario pulsa "Guardar Compra"]
              │
              ▼
   [PurchaseRepositoryImpl.saveReceiptWithItems]
              │
              ▼ (Transacción atómica en Room)
   [ReceiptDao + ReceiptItemDao + ProductDao + ProductAliasDao]
```

### B. Flujo de Lista de Compras y Estimación de Canasta
```
[Usuario accede a ShoppingListScreen]
              │
              ▼
   [ShoppingListDao.getAllItemsFlow()]
              │
              ▼
   [ShoppingListViewModel]
              │
              ▼ (Consulta histórico de precios de cada artículo)
   [PurchaseRepository.getLatestPriceForProduct()]
              │
              ▼
   [BasketEstimator.estimateBasket()]
    ├── Multiplica precio unitario histórico por cantidad
    ├── Clasifica frescura (RECENT <= 30d, STALE > 30d, UNKNOWN)
    └── Calcula total estimado, ítems con precio y faltantes
              │
              ▼
   [Renderizado Reactivo en ShoppingListScreen]
   - Encabezado con presupuesto estimado y badge de frescura
   - Lista interactiva con checkboxes para tachar artículos comprados
```

### C. Flujo de Comparación de Precios de Referencia (API Externa)
```
[Usuario visualiza detalle de producto / lista]
              │
              ▼
   [ComparePricesUseCase]
              │
    ┌─────────┴────────────────────────┐
    ▼                                  ▼
[Historial Local Propio]     [ExternalPriceProvider (Ktor)]
(Room DB: Solo compras reales)   (Consulta remota en tiempo real)
    │                                  │
    │                                  ▼
    │                        [ExternalPriceQuote]
    │                        (DTO temporal en memoria RAM)
    │                                  │
    └─────────┬────────────────────────┘
              ▼
  [ViewModel une en UI State]
  - Precios locales vs Referencia externa claramente etiquetados
  - Se descarta la referencia externa al salir de la pantalla
```

## 4. Gestión de Dependencias
- Inyección de dependencias modular y ligera (Manual Dependency Injection vía Singleton Provider y Factory en Compose para el MVP; sin sobrecarga innecesaria de Hilt o Koin hasta requerir múltiples módulos dinámicos).
