# Estrategia de Calidad y Suite de Pruebas — MarketCompare

- **Estado:** DEFINIDO (Gate 1)
- **Tarea asociada:** MC-009
- **Regla de integridad:** No declarar pruebas aprobadas sin ejecución verificable y logs limpios.

---

## 1. Pirámide de Pruebas

```
                   / \
                  /   \
                 / UI  \        (Pruebas de flujo Compose / Navegación)
                / Robo- \
               / electric\
              /-----------\
             / Integración \    (Room in-memory, Repositorios, Ktor mock)
            /---------------\
           / Pruebas Unitarias\ (Parser, Money, Normalización, Dominio)
          /---------------------\
```

---

## 2. Cobertura por Capas y Casos Críticos

### A. Pruebas Unitarias (Pure Kotlin — JUnit 4/5)
- **`MoneyTest`:**
  - Operaciones aritméticas sin pérdida de precisión ni desbordamiento de enteros.
  - Redondeo bancario en división (`RoundingMode.HALF_EVEN`).
  - Formateo correcto en pesos colombianos (separador de miles con punto, ej: `$ 15.000`).
- **`ReceiptParserTest`:**
  - Clasificación de líneas de tickets con formatos comunes colombianos (Éxito, D1, Olímpica, Jumbo).
  - Aislamiento de líneas de IVA, propinas, subtotales y formas de pago.
  - Asociación de descuentos explícitos a sus ítems precedentes.
  - Manejo de entradas con baja calidad o caracteres ilegibles.
- **`ProductNormalizationTest`:**
  - Distinción de variantes (marca + contenido neto + unidad + presentación).
  - Prohibición de unificación errónea (ej: "Leche Entera 1L" != "Leche Deslactosada 1L").
- **`EphemeralQuoteLifecycleTest`:**
  - Validación de que `ExternalPriceQuote` nunca se almacena en Room DAOs.
  - Verificación de que la consulta externa no altera la media ni el histórico de precios locales.

### B. Pruebas de Integración (Room in-memory)
- **`ReceiptTransactionTest`:**
  - Inserción atómica de `Receipt` con sus múltiples `ReceiptItem`. Si falla una línea, toda la transacción hace rollback.
  - Cascada o integridad referencial ante eliminación de facturas.
- **`ProductAliasDaoTest`:**
  - Búsqueda eficiente de productos por alias OCR previos.

---

## 3. Fixtures Anónimas de Prueba (Tickets Sintéticos)

Se definen fixtures sintéticas en texto plano y archivos JSON de prueba sin contener datos personales ni fiscales reales:

1. **`fixture_supermarket_standard.txt`:** Ticket estándar con 5 artículos, 1 con descuento del 20%, subtotal, IVA y total exacto.
2. **`fixture_promotional_discount.txt`:** Ticket con promoción tipo "2x1" o descuento por puntos cliente frecuente.
3. **`fixture_noisy_ocr.txt`:** Ticket con caracteres OCR erráticos (p. ej. `0` leído como `O`, puntos decimales borrosos) para probar la heurística de confianza y revisión.
4. **`fixture_digital_invoice.txt`:** Extracción limpia de PDF digital estructurado.
