# Pipeline de facturas

## Entradas
- Fotografía tomada con CameraX.
- Imagen importada por Photo Picker.
- PDF con texto.
- PDF escaneado.

## Etapas
1. Validar tipo MIME, tamaño y acceso a URI.
2. Extraer texto directamente de PDF cuando sea viable.
3. Si es imagen o PDF escaneado, ejecutar ML Kit Text Recognition.
4. Conservar líneas, bloques y posiciones relevantes para asociar descripciones e importes.
5. Preprocesar texto: normalización de espacios, caracteres OCR confundibles y separadores numéricos sin destruir texto original.
6. Detectar encabezado: tienda, fecha, número de factura si existe.
7. Analizar líneas candidatas de artículos.
8. Clasificar líneas no comerciales: subtotal, impuestos, total, pagos, cambio, medios de pago, mensajes promocionales.
9. Interpretar cantidades, precio de lista, descuentos y total de línea cuando haya evidencia.
10. Asociar productos existentes por alias y atributos.
11. Mostrar revisión editable.
12. Guardar solo tras confirmación; recibo y artículos en transacción.
13. Registrar métricas técnicas agregadas sin texto de factura ni datos sensibles.

## Revisión de usuario
- Mostrar cada ítem candidato con checkbox seleccionado por defecto solo si cumple las reglas de confianza aprobadas.
- Nombre detectado y descripción original accesible.
- Cantidad, precio unitario, descuento, precio final y estado de confianza.
- Permitir editar, excluir, vincular a producto existente o crear producto.
- Mostrar advertencia para datos dudosos.
- No bloquear guardar todos los ítems porque uno sea ambiguo; permitir excluir o corregir el ítem.
- Validar suma de líneas frente al total del recibo, pero no asumir que la suma debe coincidir si hay depósitos, redondeos, impuestos o conceptos no asociados.

## Descuentos
- `EXPLICIT`: descuento impreso claramente.
- `DERIVED`: derivado por cálculo solo si los datos de origen son suficientes y se etiqueta como calculado.
- `UNKNOWN`: no se puede determinar con seguridad.
- Nunca etiquetar como “descuento” una diferencia entre API y factura.
- Conservar precio final pagado por separado del precio de lista.

## Pruebas
- Variantes de separadores COP: `4.250`, `4,250`, `4.250,00` según factura.
- Artículos con cantidades y precios unitarios.
- Descuentos por línea y descuentos globales.
- Dos artículos con nombres similares.
- OCR que confunde `0/O`, `1/I`, `5/S`.
- Facturas largas, rotadas, borrosas, corruptas o multipágina.
- Totales, impuestos, medios de pago y cambio no deben convertirse en artículos.
