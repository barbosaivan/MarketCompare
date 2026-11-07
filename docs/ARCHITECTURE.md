# Arquitectura del Sistema — MarketCompare

- **Estado:** DEFINIDO Y APROBADO (Gate 1)
- **Responsable:** Tech Lead & Equipo de Arquitectura
- **Última actualización:** 2026-11-07

## 1. Visión General y Principios
MarketCompare implementa una Clean Architecture pragmática en un único módulo Android para el MVP, diseñada para ser extensible a Kotlin Multiplatform (KMP).

### Principios Fundamentales
1. **Local-First & Offline-First:** Los datos de compras confirmadas se almacenan localmente en SQLite mediante Room.
2. **Precios Externos Efímeros:** Las cotizaciones de APIs externas nunca se persisten en base de datos, caché persistente ni archivos. Residen únicamente en el flujo en memoria del ViewModel mientras la pantalla esté activa.
3. **Independencia del Dominio:** La lógica de negocio no tiene dependencias de Android SDK ni de librerías de UI o persistencia.
4. **Precisión Matemática:** Prohibición estricta de `Double`/`Float` para importes de dinero (ADR 0003).

## 2. Mapa de Capas y Responsabilidades

```
+-------------------------------------------------------------------+
|                        PRESENTATION LAYER                         |
|  - Jetpack Compose UI (Material Design 3)                         |
|  - ViewModels (Coroutines, StateFlow, UiState inmutable)          |
|  - Navigation (Type-safe Compose Destinations)                   |
|  - Localized String Resources & Formateadores                    |
+---------------------------------+---------------------------------+
                                  | Observa UiState / Envía Intentos
                                  v
+-------------------------------------------------------------------+
|                           DOMAIN LAYER                            |
|  - Modelos / Entidades: Receipt, ReceiptItem, Product, Store      |
|  - Value Objects: Money (minor units), UnitPrice, Confidence      |
|  - Casos de Uso: ParseReceiptDraft, SaveConfirmedReceipt,        |
|                  CompareProductPrices, EstimateShoppingList       |
|  - Contratos de Repositorio (Interfaces puras)                   |
+---------------------------------+---------------------------------+
                                  ^ Implementa interfaces
                                  |
+---------------------------------+---------------------------------+
|                            DATA LAYER                             |
|  - Room Local DB (DAOs, Entities, TypeConverters, Migrations)     |
|  - Motor de Parseo de Recibos (Clasificación de líneas, OCR draft)|
|  - Implementaciones de Repositorios                               |
|  - Cliente HTTP Ktor (Caché desactivada, solo en memoria)        |
+---------------------------------+---------------------------------+
                                  ^
                                  | Provee datos crudos
+---------------------------------+---------------------------------+
|                     PLATFORM / ANDROID LAYER                      |
|  - CameraX (Captura optimizada para texto)                        |
|  - Photo Picker / SAF Document Picker                             |
|  - ML Kit Text Recognition (OCR de imágenes)                      |
|  - PdfRenderer (Fallback rasterizado para PDFs escaneados)        |
|  - Extracción de texto plano para PDFs digitales                  |
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
              │
   [Usuario pulsa "Confirmar"]
              │
              ▼
   [SaveConfirmedReceiptUseCase]
              │
              ▼ (Transacción atómica en Room)
   [ReceiptDao + ReceiptItemDao + CatalogDao]
```

### B. Flujo de Comparación de Precios de Referencia (API Externa)
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
- Inyección de dependencias modular y ligera (Manual Dependency Injection o Service Locator simple para el MVP; sin sobrecarga de frameworks como Hilt o Koin hasta que el volumen de módulos lo amerite).
