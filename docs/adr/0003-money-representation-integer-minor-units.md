# ADR 0003 — Representación exacta del dinero en unidades menores (enteros)

- **Estado:** ACEPTADO (ACCEPTED)
- **Fecha:** 2026-11-07
- **Decisores:** Tech Lead, Datos, Usuario

## Contexto
El uso de números en coma flotante binaria (`Double` o `Float`) en aplicaciones financieras o de comercio introduce errores de redondeo acumulativos (por ejemplo `0.1 + 0.2 = 0.30000000000000004`). Estos errores corrompen la suma de subtotales, impuestos, descuentos y comparativas de precios por unidad en facturas.

En Colombia (moneda COP), los importes en tickets comerciales se expresan en pesos enteros, pero en transacciones estándar y cálculos de descuentos o precios unitarios normalizados (p. ej. $/gramo o $/mililitro) se requiere precisión absoluta sin redondeo errático.

## Decisión
1. **Prohibición de `Double` y `Float` para dinero:** Queda terminantemente prohibido utilizar tipos de coma flotante binaria (`Double`, `Float`) para almacenar, computar o comparar importes monetarios o subtotales de facturas.
2. **Value Object `Money`:**
   - Se crea el value object inmutable `Money(val amountMinor: Long, val currency: Currency = Currency.COP)`.
   - `amountMinor` representa el valor en unidades menores (para COP, centavos base donde 1 peso = 100 centavos, o 1 peso base según la precisión fijada del catálogo, manteniéndose uniforme en todo el sistema como `Long`).
   - Las operaciones aritméticas (`plus`, `minus`, `times`, `div`) devuelven nuevas instancias de `Money` con redondeo bancario explícito (`RoundingMode.HALF_EVEN`).
3. **Cantidades y Proporciones:**
   - La cantidad de artículos (`Quantity`) se representará mediante un tipo decimal exacto o ratio entero (`BigDecimal` o numerador/denominador enteros) para manejar fracciones como `0.750 kg` de carne o fruta a granel.
4. **Formateo para UI:**
   - La UI solo recibe cadenas ya formateadas con separadores colombianos oficiales (p. ej. `$ 12.500 COP` mediante un formateador centralizado en el sistema de diseño).

## Consecuencias
- **Positivas:** Cero discrepancias en balances y sumas de recibos; trazabilidad matemática auditable; compatibilidad nativa con bases de datos SQL (almacenado como `INTEGER`/`BIGINT` en SQLite/Room).
- **Negativas:** Exige conversión al mostrar o parsear entradas de texto manuales.

