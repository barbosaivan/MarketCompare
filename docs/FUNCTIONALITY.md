# Funcionalidades y Flujos de Usuario — MarketCompare

- **Estado:** DEFINIDO (Gate 1)
- **Tarea asociada:** MC-007

---

## 1. Pipeline de Importación y Procesamiento de Facturas

### Fuentes de Entrada Admitidas
1. **Cámara (CameraX):** Captura fotográfica con guía de encuadre en pantalla.
2. **Galería (Photo Picker de Android):** Selección de imágenes existentes (JPG, PNG).
3. **Archivos PDF (Storage Access Framework):** Facturas electrónicas digitales descargadas de supermercados o comercios.

### Estrategia Dual de Extracción de Texto
- **PDF Nativo con Texto Seleccionable:** Se extrae el stream de texto directamente sin rasterizar ni ejecutar OCR, logrando 100% de fidelidad y velocidad inmediata.
- **Imagen / PDF Escaneado:** Si el PDF carece de capas de texto embebido, se renderiza la primera página (o páginas seleccionadas) vía `PdfRenderer` a bitmaps y se procesa mediante **Google ML Kit Text Recognition**.

---

## 2. Contrato y Motor de Parseo (`ReceiptParser`)

El motor de parseo convierte bloques de texto crudo en un borrador de factura estructurado (`ReceiptDraft`):

```kotlin
data class ReceiptDraft(
    val detectedStoreName: String?,
    val detectedDate: LocalDate?,
    val detectedTaxId: String?,
    val rawText: String,
    val items: List<ReceiptItemDraft>,
    val detectedTotal: Money?,
    val overallConfidence: Float
)

data class ReceiptItemDraft(
    val rawDescription: String,
    val quantity: ProductQuantity,
    val unitListPrice: Money?,
    val discount: Money?,
    val discountType: DiscountType,
    val discountEvidence: String?,
    val finalPrice: Money,
    val confidence: Float,
    val isIncluded: Boolean = true // Seleccionado por defecto para guardar
)
```

### Clasificación Heurística de Líneas
1. **Encabezado:** Búsqueda de razones sociales, marcas registradas ("ALMACENES ÉXITO", "OLÍMPICA S.A."), NITs colombianos (`NIT \d{9}-\d`) y marcas temporales.
2. **Líneas de Artículos:** Detección de patrones `[Descripción] [Cantidad] [Precio]`.
3. **Líneas de Descuento:** Detección de palabras clave (`DCTO`, `AHORRO`, `PROMO`, `PUNTOS`) vinculadas a la línea inmediatamente anterior o siguiente.
4. **Líneas de Cierre:** Exclusión de subtotales, IVA/impoconsumo, propinas, medios de pago (efectivo, tarjeta, cambio) para no registrarlos como artículos.

---

## 3. Pantalla de Revisión Editable (`ReceiptReview`)

> **Regla de Oro:** Ningún recibo ni artículo se guarda en la base de datos sin la confirmación explícita del usuario en esta pantalla.

### Capacidades del Usuario en la Revisión:
- **Exclusión selectiva:** Desmarcar líneas que no sean artículos de interés o que el usuario no desee registrar.
- **Edición en línea:** Corregir nombres mal leídos por el OCR, cantidades o importes.
- **Asignación de producto:** Vincular el texto crudo a un producto existente del catálogo o registrarlo como un nuevo producto.
- **Confirmación transaccional:** Al pulsar "Guardar Compra", se ejecuta una transacción atómica que guarda tienda, recibo, líneas y aliases confirmados.

---

## 4. Comparativa de Precios y Cotizaciones

- **Consulta On-Demand:** La comparación contra fuentes externas solo se dispara cuando el usuario lo solicita explícitamente desde el detalle del producto o la lista de compras.
- **Etiquetado Visual Obligatorio:**
  - `Precio de Compra Real`: Verde/azul con ícono de recibo y fecha de la compra.
  - `Precio de Referencia Externo`: Amarillo/gris con etiqueta "Cotización externa temporal" y marca de hora de la consulta.
- **Manejo de Errores y Modo Offline:** Si no hay internet o la API externa falla, se muestra el estado "Servicio externo no disponible" sin afectar en ningún momento los datos históricos locales.
