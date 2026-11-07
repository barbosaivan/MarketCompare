# Calidad y pruebas

Unit tests: parser, normalización, dinero, descuentos, matching y ranking. Integración: Room, migraciones, transacciones, repositorios y cliente API. UI: revisión, comparación, navegación y estados. Manual: cámara, PDF anonimizados, TalkBack, tema oscuro y tamaños.

Casos críticos: PDF corrupto/multipágina, OCR confuso, separadores COP, descuentos, variantes similares, API offline/timeout/cancelación, no persistencia externa y migraciones.

No cerrar con P0/P1. Registrar pruebas no ejecutadas y bloqueos. No declarar tests exitosos sin salida verificable.
