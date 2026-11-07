# Plantilla de integración del API de precios

## Estado
`BLOCKED — falta documentación del proveedor`

No inventar URL base, endpoints, parámetros, autenticación, campos, moneda ni límites.

## Información que debe aportar el propietario
- URL de documentación oficial.
- Base URL por entorno.
- Método de autenticación y lugar permitido para guardar la credencial.
- Endpoint de búsqueda y ejemplo real de respuesta.
- Campos de producto: nombre, marca, presentación, cantidad, unidad, tienda.
- Precio, moneda, descuentos, fecha de actualización y procedencia.
- Límites de uso, cuota, rate limits y códigos de error.
- Términos de servicio, licencia y reglas de uso/almacenamiento.
- Si permite comparación comercial y presentación de precios al usuario.
- Si la API exige caché; cualquier obligación incompatible con “no persistir” requiere decisión del propietario.

## Contrato interno provisional
`ExternalPriceProvider`
- `suspend fun search(query: ProductSearchQuery): ExternalPriceSearchResult`
- DTOs de red separados de modelos de dominio.
- Respuesta temporal en memoria.
- Sin Room Entity, DAO, DataStore, archivo, historial ni analytics.
- Desactivar caché HTTP; no incluir precios en logs.
- No reutilizar resultados de una consulta anterior al mostrar una nueva comparación.
- Cancelar la llamada al salir de la pantalla cuando proceda.
- Mostrar fecha de consulta y proveedor cuando estén disponibles.

## Seguridad
- No incluir secretos en Git ni hardcodearlos en el APK.
- Si el proveedor requiere secreto privado, evaluar un proxy backend futuro; no simular que una clave embebida en Android es secreta.
- No desplegar proxy ni nube sin aprobación explícita.
