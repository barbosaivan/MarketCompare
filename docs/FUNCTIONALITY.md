# Funcionalidades y Flujos de Usuario — MarketCompare

- **Estado:** `IMPLEMENTED` (Gates 1, 2, 3, 5, 6 completados; Gate 4 `BLOCKED` a la espera de credenciales de API externa).
- **Última actualización:** 2026-11-08

---

## 1. Pipeline de Importación y Procesamiento de Facturas

### Fuentes de Entrada Admitidas
1. **Cámara (CameraX):** Captura fotográfica con guía de encuadre en pantalla y autoenfoque.
2. **Galería (Photo Picker de Android):** Selección de imágenes existentes (JPG, PNG) con permisos mínimos del sistema.
3. **Archivos PDF (Storage Access Framework):** Facturas electrónicas digitales descargadas de supermercados o comercios.

### Estrategia Dual de Extracción de Texto
- **PDF Nativo con Texto Seleccionable:** Se extrae el stream de texto directamente mediante la heurística de streams sin rasterizar ni ejecutar OCR, logrando 100% de fidelidad y velocidad inmediata.
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
1. **Encabezado:** Búsqueda de razones sociales, marcas registradas ("ALMACENES ÉXITO", "OLÍMPICA S.A.", "D1", "JUMBO"), NITs colombianos (`NIT \d{9}-\d`) y marcas temporales.
2. **Líneas de Artículos:** Detección de patrones `[Descripción] [Cantidad] [Precio]`.
3. **Líneas de Descuento:** Detección de palabras clave (`DCTO`, `AHORRO`, `PROMO`, `PUNTOS`) vinculadas a la línea inmediatamente anterior o siguiente.
4. **Líneas de Cierre:** Exclusión de subtotales, IVA/impoconsumo, propinas, medios de pago (efectivo, tarjeta, cambio) para no registrarlos como artículos.

---

## 3. Pantalla de Revisión Editable (`ReceiptReview`)

> **Regla de Oro:** Ningún recibo ni artículo se guarda en la base de datos sin la confirmación explícita del usuario en esta pantalla.

### Capacidades del Usuario en la Revisión:
- **Exclusión selectiva:** Desmarcar líneas que no sean artículos de interés o que el usuario no desee registrar.
- **Edición en línea:** Corregir nombres mal leídos por el OCR, cantidades o importes monetarios.
- **Asignación de producto:** Vincular el texto crudo a un producto existente del catálogo o registrarlo como un nuevo producto.
- **Auditoría interactiva de descuentos:** Pulsar sobre cualquier ítem con descuento para abrir el `DiscountAuditDialog`, revisar el tipo de promoción detectada, inspeccionar la evidencia OCR cruda o ajustar manualmente el monto ahorrado.
- **Resumen financiero en tiempo real:** Barra superior con total de la compra y ahorro total acumulado calculado al vuelo.
- **Confirmación transaccional:** Al pulsar "Guardar Compra", se ejecuta una transacción atómica en Room que guarda tienda, recibo, líneas y aliases confirmados.

---

## 4. Normalización y Desambiguación de Productos (`ProductNormalizer`)

Para evitar la fragmentación del catálogo debido a variaciones tipográficas en los tickets de diferentes tiendas, `ProductNormalizer` realiza:

1. **Extracción Semántica:**
   - **Marca:** Identificación de marcas comerciales reconocidas (ej: Alpina, Colanta, Corona, Diana, Coca-Cola).
   - **Contenido Neto y Unidad:** Extracción precisa de magnitudes (ej: 1000g, 1L, 250ml, 6 unidades) convirtiendo a unidades estándar (G, KG, ML, L, UNIT, PACK).
   - **Presentación:** Reconocimiento del tipo de empaque (Bolsa, Botella, Vaso, Caja, Tetrapack, Lata, Paquete).
2. **Generación de Fingerprint de Variante (`variantFingerprint`):**
   - Cadena canónica que identifica unívocamente la variante física del producto: `marca|nombre_base|cantidad|unidad|presentacion`.
3. **Compatibilidad Estricta de Variantes (`areVariantsCompatible`):**
   - Valida que dos ítems correspondan a la misma versión física antes de sugerir autocompletados o unificar alias.
   - Dos productos con distinta presentación o contenido neto son **incompatibles** (ej: "Yogurt 150g Vaso" != "Yogurt 1000g Bolsa").

---

## 5. Motor de Autocompletado y Asignación de Alias (`ProductAutocompleteEngine`)

El motor de autocompletado asiste al usuario al editar la descripción de un producto en la pantalla de revisión:

1. **Ranking Ponderado de Sugerencias:**
   - Búsqueda en el catálogo existente y en la tabla de `product_aliases`.
   - Coincidencia exacta de prefijo (puntuación máxima).
   - Contención léxica de palabras clave.
   - Ponderación positiva por compatibilidad de variantes y frecuencia de compra previa.
2. **Prevención de Colisión de Variantes (Diálogo de Advertencia):**
   - Si el usuario selecciona un producto del catálogo cuyo fingerprint difiere significativamente del ítem actual (por ejemplo, distinto tamaño o empaque), el sistema despliega un diálogo de advertencia: *"¿Deseas vincular este ítem a una variante diferente?"*, evitando contaminar el catálogo por toques accidentales.

---

## 6. Clasificación de Descuentos (`DiscountClassifier`)

Permite rastrear de dónde proviene cada beneficio económico y auditar la efectividad de promociones de supermercado:

- **Categorías Soportadas:**
  - `LOYALTY_MEMBER`: Descuentos por membresía o puntos de fidelización (ej: "PUNTOS COLOMBIA", "CLIENTE FRECUENTE").
  - `PROMOTION_2X1`: Promociones por volumen o combos (ej: "LLEVE 2 PAGUE 1", "SEGUNDO AL 50%").
  - `EXPLICIT_LINE`: Descuento individual explícito impreso bajo el artículo.
  - `DERIVED`: Diferencia calculada entre precio de lista anunciado e importe final cobrado.
  - `UNKNOWN`: Deducción no clasificable con la heurística actual.
- **Trazabilidad y Evidencia OCR:**
  - Cada descuento conserva el fragmento de texto exacto del OCR (`discountEvidence`) para auditoría posterior del consumidor.

---

## 7. Lista de Compras y Estimación de Canasta (`ShoppingList` & `BasketEstimator`)

Permite al usuario planificar sus compras en el supermercado y conocer de antemano el costo estimado de su canasta familiar:

1. **Gestión de Artículos:**
   - Adición ágil de productos con nombre, cantidad y unidad.
   - Casilla de verificación interactiva (`Checkbox`) para marcar artículos adquiridos en el pasillo.
2. **Estimación Presupuestal en Tiempo Real (`BasketEstimator`):**
   - Para cada ítem de la lista, el estimador consulta el historial de compras locales y toma la observación de precio más reciente.
   - Multiplica el precio unitario histórico por la cantidad solicitada.
   - Suma el costo total estimado de la canasta, mostrando el desglose en el encabezado de la pantalla.
3. **Semáforo de Frescura de Precios (`PriceFreshness`):**
   - Cada artículo y el resumen general clasifican los precios según su vigencia:
     - 🟢 **`RECENT` (Actualizado):** Precio observado en los últimos 30 días. Alta confianza de estimación.
     - 🟡 **`STALE` (Desactualizado):** Precio observado hace más de 30 días. El precio en góndola puede haber variado por inflación o cambios de temporada.
     - ⚪ **`UNKNOWN` (Desconocido):** Producto nuevo que nunca ha sido comprado antes. No suma al total estimado pero alerta al usuario para prestar atención al precio.

---

## 8. Navegación Unificada de la Aplicación

La aplicación provee una barra de navegación inferior (`NavigationBar`) persistente basada en Material 3:
- **Historial:** Consulta cronológica de recibos guardados, gastos por tienda y detalle de líneas.
- **Escanear:** Acceso directo a la captura de tickets (cámara, galería, PDF) y al flujo de revisión.
- **Lista de Compras:** Planificador de canasta, estimación presupuestal y control de ítems pendientes.

---

## 9. Comparativa de Precios y Cotizaciones Externas

- **Consulta On-Demand:** La comparación contra fuentes externas solo se dispara cuando el usuario lo solicita explícitamente desde el detalle del producto o la lista de compras.
- **Etiquetado Visual Obligatorio:**
  - `Precio de Compra Real`: Verde/azul con ícono de recibo y fecha de la compra.
  - `Precio de Referencia Externo`: Amarillo/gris con etiqueta "Cotización externa temporal" y marca de hora de la consulta.
- **Manejo de Errores y Modo Offline:** Si no hay internet o la API externa falla, se muestra el estado "Servicio externo no disponible" sin afectar en ningún momento los datos históricos locales.
