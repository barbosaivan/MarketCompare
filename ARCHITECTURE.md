# Arquitectura objetivo

## Enfoque
Clean Architecture pragmática en un solo módulo de aplicación inicialmente. No dividir en módulos Gradle hasta que haya límites estables y un beneficio de compilación/equipo.

## Capas
- `domain`: entidades y value objects, reglas de comparación, contratos de repositorio y casos de uso.
- `data`: Room, mappers, parser de factura, implementación de repositorios, cliente API.
- `presentation`: pantallas Compose, ViewModels, UI state y navegación.
- `platform/android`: CameraX, SAF/Photo Picker, PdfRenderer, ML Kit, manejo de URI y permisos.

## Flujo de importación
1. `DocumentPicker`/CameraX obtiene URI.
2. `DocumentTextExtractor` determina formato y extrae texto.
3. `ReceiptParser` produce un borrador editable.
4. `ProductMatcher` sugiere productos locales existentes, sin fusionar variantes incompatibles.
5. La pantalla `ReceiptReview` permite seleccionar artículos, corregir nombres/cantidades/precios y confirmar.
6. `SavePurchaseUseCase` guarda recibo y líneas en una transacción Room.
7. `PurchaseHistoryRepository` alimenta historial y comparación local.

## API externa
`ExternalPriceProvider` es un cliente de consulta exclusivamente remota. El endpoint real se implementa solo tras recibir documentación oficial.

Contrato conceptual:
- `searchPrices(query, productAttributes): Result<List<ExternalPriceQuote>>`
- `ExternalPriceQuote` es un objeto de memoria, no una entidad Room.
- No crear DAO, Entity, DataStore key ni caché para `ExternalPriceQuote`.
- Cliente con caché deshabilitada; no persistir respuesta en logs ni analytics.
- Al salir de la pantalla o cancelar la consulta, limpiar el estado de precios externos.
- Mostrar fecha de consulta y procedencia solo si el proveedor los entrega.
- Si no hay conexión, mostrar error/estado no disponible; no mostrar una respuesta anterior como actual.
- Antes de integrar, revisar autenticación, rate limits, términos de uso, licencia de redistribución y restricciones de almacenamiento.

## Modelo de dominio sugerido
- `Product`: identidad normalizada, marca, variedad, cantidad neta y unidad.
- `Store`: establecimiento detectado/confirmado.
- `Receipt`: fecha, tienda, total, origen, moneda, referencia local al documento opcional.
- `ReceiptItem`: descripción OCR original, producto enlazado opcional, cantidad, precio lista, descuento, precio final, confianza y procedencia del dato.
- `ShoppingList` / `ShoppingListItem`: lista y cantidades deseadas.
- `ExternalPriceQuote`: DTO temporal en memoria, nunca persistido.
- `PriceObservation`: derivado solo de compras guardadas; no incluye API externa.

## Trazabilidad de descuentos
Campos recomendados en ReceiptItem:
- `unitListPriceMinor: Long?`
- `quantity: BigDecimal-equivalent representation` (usar representación decimal exacta acordada; no Double para dinero)
- `lineDiscountMinor: Long?`
- `discountType: DiscountType` (`NONE`, `FIXED_AMOUNT`, `PERCENTAGE`, `PROMOTION`, `UNKNOWN`)
- `lineFinalTotalMinor: Long`
- `discountEvidence: String?` (fragmento OCR mínimo necesario)
- `extractionConfidence: Float?`
- `userConfirmed: Boolean`
- `rawDescription: String`

No derivar `unitListPriceMinor` si la factura solo muestra un total final sin suficiente información. Separar descuento explícito de ahorro calculado contra precio externo.

## Normalización y sugerencias
Orden de matching:
1. Alias exacto confirmado por usuario.
2. Código de barras, si existe.
3. Nombre normalizado + marca + tamaño + unidad + presentación.
4. Coincidencia aproximada como sugerencia, nunca fusión automática de variantes dudosas.
5. Confirmación del usuario.
No almacenar la respuesta externa de API en el catálogo local.

## Preparación para nube futura
- IDs internos estables y repositorios desacoplados.
- Campos de creación/modificación y esquema migrable cuando sean necesarios.
- Diseñar un punto de extensión para `SyncRepository`, pero no implementar sincronización todavía.
- No añadir login, backend, Firebase ni nube al MVP sin aprobación.
