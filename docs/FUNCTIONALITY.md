# Funcionalidades y flujos

Estado inicial: PLANNED salvo evidencia de implementación.

Importación: cámara, imagen o PDF; extraer texto y mostrar borrador editable. No guardar sin confirmación.

Revisión: editar, excluir, cambiar cantidad/precio, asociar o crear producto. Conservar descripción OCR original y confianza.

Descuentos: separar precio de lista, descuento, precio final y evidencia. Distinguir explícito, derivado verificable y desconocido. Nunca inferir descuento comparando contra API.

Catálogo: distinguir marca, tipo, contenido neto, unidad y presentación. Yogurt Alpina vaso y yogurt Alpina 1 L no deben fusionarse automáticamente.

Historial: solo compras confirmadas; nunca cotizaciones API.

Comparación: consulta a demanda, temporal en memoria, sin Room/DataStore/archivos/logs/analytics/caché HTTP. No reutilizar cotización vieja como actual.

Lista de mercado: estimar con compras locales y mostrar fecha/falta de datos recientes.
