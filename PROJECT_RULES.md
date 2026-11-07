# Reglas permanentes del proyecto

## Kotlin y arquitectura
- Kotlin idiomático, null-safety y funciones pequeñas con nombres precisos.
- Preferir composición sobre herencia.
- Separar presentación, dominio y datos.
- El dominio no depende de Android, Room, Compose ni el cliente HTTP.
- Composables renderizan estado y emiten eventos; no ejecutan consultas SQL ni llamadas de red.
- ViewModels coordinan estado de pantalla y casos de uso; no contienen parsers grandes ni reglas monetarias.
- Repository abstrae fuentes; `LocalPurchaseRepository` y `ExternalPriceRepository` nunca deben fusionarse en un único almacén ambiguo.
- Inyectar dependencias por constructor.
- No crear interfaces para cada clase sin un motivo real de sustitución/testabilidad.
- Evitar singletons mutables globales.
- No añadir frameworks por moda: justificar cada dependencia.

## UI y Material Design 3
- Todo texto visible usa recursos localizados; idioma inicial español (Colombia), preparado para ampliar idiomas.
- Todo color viene de `MaterialTheme.colorScheme`, roles semánticos o tokens de tema.
- Tipografía desde `MaterialTheme.typography`; formas/espaciado desde tokens centrales.
- Usar componentes compartidos para botones, campos, estados vacíos, errores, tarjetas de precio y etiquetas de fuente.
- Soportar tema claro/oscuro, contraste adecuado, tamaños de fuente y accesibilidad.
- Usar estados de carga, error, vacío y contenido.
- No usar emojis como sustitutos de iconografía de producto.
- Evitar `Modifier` duplicados, recomposiciones evitables y estado derivado almacenado innecesariamente.
- Formularios y listas deben ser usables con TalkBack y escalado de texto.
- No fijar tamaños de pantalla como si todos los dispositivos fueran iguales.

## Datos y precios
- Diferenciar `PurchasePrice` de `ExternalReferencePrice` en nombres, tipos y UI.
- La respuesta API es efímera y no persistente.
- Prohibido persistir la respuesta API en Room, DataStore, archivos, preferencias, logs, analytics o caché HTTP.
- No persistir búsquedas API ni precios API en historial.
- La comparación externa se calcula en memoria a partir de los datos propios guardados y la respuesta recién recibida.
- Si la API requiere cachear datos por contrato o condiciones del proveedor, bloquear la integración y solicitar decisión al propietario.
- Conservar moneda, fecha/hora de observación, fuente y unidad de comparación cuando el proveedor las entregue.
- No comparar productos de tamaños/unidades incompatibles sin normalizar precio por unidad.
- No inferir descuento si no hay evidencia en el documento.
- Dinero en `Long`/tipo de valor monetario o decimal exacto, nunca `Double` para importes.
- Transacciones de base de datos para guardar una factura y sus artículos de forma consistente.
- Deduplicación de factura debe ser conservadora y no descartar compras legítimas.

## OCR y documentos
- No asumir que cada línea de OCR es un producto.
- Ignorar o clasificar subtotales, impuestos, totales, pagos, cambio y mensajes promocionales.
- Mantener texto bruto por artículo para corregir el parser.
- PDF con texto: extraer texto directamente; PDF escaneado: renderizar páginas y aplicar OCR.
- Limitar tamaño y páginas para evitar OOM; cancelar operaciones largas y liberar bitmaps/recursos.
- Nunca declarar que el OCR es correcto sin una etapa de revisión o confianza calibrada.
- Probar facturas de varios formatos con datos anonimizados.

## Calidad y seguridad
- Sin APIs deprecated, código muerto, imports sin uso, dependencias sin uso o warnings evitables.
- Sin strings/colores hardcodeados en UI.
- Sin secretos en repositorio; configurar API keys de acuerdo con el proveedor y el modelo de seguridad de Android.
- No registrar datos de facturas ni respuestas de precios en logs.
- No ejecutar comandos destructivos, publicar, firmar ni subir artefactos sin autorización.
- Ejecutar tests, lint, compilación y revisión de diff antes de cerrar tarea.
- Documentar toda excepción a estas reglas.

## Git y colaboración
- Inspeccionar estado, rama, remotos y convenciones antes de editar.
- Una rama por tarea coherente; no trabajar directamente en ramas protegidas sin permiso.
- Commits pequeños, atómicos, descriptivos y vinculados al ID del backlog.
- Revisar diff, tests y análisis antes de crear commits.
- No incluir secretos, datos personales, facturas reales ni precios API efímeros en Git.
- No hacer force push, rebase destructivo de historia compartida, reset destructivo ni borrar ramas remotas sin aprobación explícita.
- Abrir PR cuando existan permisos y herramienta disponible; incluir resumen, pruebas, riesgos y checklist.
- No autoaprobar ni fusionar PR protegido. Requerir revisión independiente y aprobación de acuerdo con la política del repo.
- Si faltan permisos/herramientas, registrar el bloqueo y proporcionar instrucciones manuales exactas; nunca fingir acciones remotas.
- Nunca solicitar secretos para pegarlos en el chat. Usar el mecanismo de autenticación seguro recomendado por el proveedor.
- Mantener estados de tarea y estados de Git/PR por separado en el dashboard.

## Documentación viva y onboarding
`docs/README.md` es el índice oficial. Leer docs antes de implementar y actualizarlos en el mismo PR. README raíz debe permitir a una persona nueva comprender y ejecutar el proyecto con pasos verificados. No presentar como existente algo planeado. Registrar decisiones relevantes mediante ADR. Documentación contradictoria bloquea el cierre.
