# Modelo de Datos y Trazabilidad de Precios — MarketCompare

- **Estado:** DEFINIDO (Gate 1)
- **Tarea asociada:** MC-006
- **Moneda base:** COP (Pesos Colombianos)
- **Regla monetaria:** Unidades menores enteras (`Long`), prohibición de `Double`/`Float` (ADR 0003).

---

## 1. Value Objects Fundamentales

### `Money`
```kotlin
data class Money(
    val amountMinor: Long, // En COP: centavos o pesos enteros según escala fija (ej: 1250000 = $12.500)
    val currency: Currency = Currency.COP
) : Comparable<Money> {
    operator fun plus(other: Money): Money
    operator fun minus(other: Money): Money
    operator fun times(multiplier: Long): Money
    fun divide(divisor: Long, roundingMode: RoundingMode = RoundingMode.HALF_EVEN): Money
    fun formatDisplay(): String // "$ 12.500"
}
```

### `Quantity` y `UnitType`
```kotlin
enum class UnitType {
    UNIT,       // Unidades / piezas
    GRAM,       // Gramos (g)
    KILOGRAM,   // Kilogramos (kg)
    MILLILITER, // Mililitros (ml)
    LITER       // Litros (l)
}

data class ProductQuantity(
    val value: Long,        // En unidades menores (ej: 750 gramos = 750, 1.5 L = 1500 ml)
    val unit: UnitType
)
```

---

## 2. Entidades de Dominio vs Entidades Room (Persistencia Local)

### Diagrama Entidad-Relación (Base de Datos Local Room)
```
+--------------------+        1:N        +-----------------------+
|       stores       |-------------------|       receipts        |
+--------------------+                   +-----------------------+
| id: String (UUID)  |                   | id: String (UUID)     |
| name: String       |                   | store_id: String (FK) |
| address: String?   |                   | purchase_date: Long   |
| detected_tax_id: Str?                  | total_amount: Long    |
+--------------------+                   | raw_document_uri: Str?|
                                         +-----------+-----------+
                                                     | 1:N
                                                     v
+--------------------+        1:N        +-----------------------+
|      products      |-------------------|     receipt_items     |
+--------------------+                   +-----------------------+
| id: String (UUID)  |                   | id: String (UUID)     |
| canonical_name: Str|                   | receipt_id: String(FK)|
| brand: String?     |                   | product_id: String(FK)|
| category: String?  |                   | raw_description: Str  |
| base_quantity: Long|                   | quantity_value: Long  |
| unit: String       |                   | quantity_unit: String |
| presentation: Str? |                   | list_price_minor: Long|
+---------+----------+                   | discount_minor: Long  |
          | 1:N                          | final_price_minor:Long|
          v                              | discount_type: String |
+--------------------+                   | discount_evidence: Str|
|  product_aliases   |                   | confidence_score: Real|
+--------------------+                   | user_confirmed: Bool  |
| id: String (UUID)  |                   +-----------------------+
| product_id: String |
| alias_text: String |
| store_id: String?  |
+--------------------+
```

### Detalle de Campos y Restricciones:

1. **`stores` (Establecimientos):**
   - Normaliza el nombre del comercio (ej: "Éxito", "Olímpica", "D1", "Carulla").
   - Guarda identificadores fiscales detectados si existieran (ej: NIT) para matching unívoco.

2. **`receipts` (Facturas / Compras):**
   - Transaccional: Una factura solo se guarda cuando el usuario confirma la revisión completa.
   - Contiene la fecha de compra, el total verificado y una referencia URI opcional al documento de origen.

3. **`receipt_items` (Líneas de Compra / Artículos):**
   - `raw_description`: Conserva textualmente lo que leyó el OCR para trazabilidad y auditoría.
   - `list_price_minor`: Precio antes de descuentos.
   - `discount_minor`: Monto descontado explícito.
   - `final_price_minor`: Precio real pagado (`list_price_minor - discount_minor`).
   - `discount_type`: Enum (`NONE`, `EXPLICIT_LINE`, `PROMOTION_2X1`, `LOYALTY_MEMBER`, `UNKNOWN`).
   - `discount_evidence`: Texto OCR mínimo que respalda el descuento (ej: "-$ 2.000 DCTO PUNTOS").
   - `confidence_score`: Valor 0.0 a 1.0 que indica la certidumbre de la extracción automática.

4. **`products` y `product_aliases` (Catálogo Normalizado):**
   - **Diferenciación estricta:** Dos productos con la misma marca pero diferente presentación o contenido neto son entidades distintas (ej: "Yogurt Alpina Fresa 150g Vaso" != "Yogurt Alpina Fresa 1000g Bolsa").
   - `product_aliases`: Asocia textos crudos de factura a productos confirmados por el usuario (ej: "YOG ALP FR 150G" -> ID de "Yogurt Alpina Fresa 150g").

---

## 3. Modelo de Cotización Externa (Efímero en Memoria)

```kotlin
// MODELO EXCLUSIVAMENTE EN MEMORIA RAM
// PROHIBIDO CREAR @Entity o @Dao PARA ESTA CLASE
data class ExternalPriceQuote(
    val quoteId: String,
    val providerName: String,       // Ej: "API Supermercado X"
    val productName: String,
    val brand: String?,
    val matchConfidence: Float,
    val priceMinor: Long,
    val currency: Currency = Currency.COP,
    val quotedAtTimestamp: Long,    // Momento exacto de la consulta
    val sourceUrl: String?
)
```

### Reglas de Trazabilidad y Aislamiento:
- **No persistencia:** `ExternalPriceQuote` nunca se pasa a Room DAOs ni se serializa a disco.
- **Diferenciación visual:** En la interfaz de usuario, los precios locales se presentan con la etiqueta "Tu compra real (dd/mm/aaaa en Tienda X)", mientras que las cotizaciones externas llevan el distintivo "Referencia externa (Tiempo real)".
- **Limpieza de sesión:** Al salir de la pantalla de detalle de producto o lista de mercado, la instancia de `ExternalPriceQuote` queda desreferenciada para recolección de basura.
