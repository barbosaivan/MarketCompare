# Modelo de datos

Estado: PLANNED; sincronizar con entidades Room y migraciones reales. Moneda inicial COP. Dinero como entero en unidades menores o decimal exacto, nunca Double.

Entidades previstas:
- Product: nombre normalizado, marca, variedad, contenido neto, unidad, presentación, código opcional.
- ProductAlias: alias confirmado, procedencia y vínculo a producto.
- Store: nombre normalizado y visible.
- Receipt: fecha, tienda, total, moneda y origen.
- ReceiptItem: descripción OCR original, producto opcional, cantidad, precio lista, descuento, precio final, confianza y confirmación.
- ShoppingList y ShoppingListItem.

ExternalPriceQuote es DTO temporal en memoria, nunca entidad Room. No crear DAO, DataStore key, archivo, historial ni caché.

Cada precio propio debe rastrearse a factura, fecha, establecimiento, producto/presentación, cantidad, precio final y evidencia del descuento. Guardar recibo y líneas en una transacción. Conservar texto original.
