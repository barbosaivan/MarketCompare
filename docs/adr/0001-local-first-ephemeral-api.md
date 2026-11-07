# ADR 0001 — Compras locales y cotizaciones API efímeras

- Estado: PROPOSED — requiere aprobación de arquitectura.
- Fecha: 2026-11-07.

Contexto: la aplicación funciona sin registro. Las compras se conservan localmente y los precios API no deben almacenarse en el dispositivo.

Decisión propuesta: Room guarda compras confirmadas y datos propios. Cotizaciones externas son DTO temporales en memoria; no se guardan en Room, DataStore, archivos, caché HTTP, logs, analytics ni historial.

Consecuencias: fuentes separadas; sin conexión no se presenta cotización anterior como actual; revisar términos del proveedor para confirmar que no exige persistencia.
