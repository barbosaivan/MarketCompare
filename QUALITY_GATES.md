# Quality gates y aprobaciones

## Gate 0 — Entorno y alcance
Aprobación humana requerida antes de una implementación amplia.
- [x] Inspección de workspace y Git.
- [x] Versiones reales de JDK, Android Studio, Gradle, Kotlin y AGP registradas.
- [x] Dispositivo/emulador disponible o bloqueo documentado.
- [x] API externa identificada o claramente marcada como bloqueada.
- [x] Backlog y alcance MVP aprobados.

## Gate 1 — Arquitectura
- [x] ADR de arquitectura y dependencias.
- [x] Modelo de datos y política de precios aprobados.
- [x] Plan de recursos de UI y navegación.
- [x] Plan de OCR/PDF y revisión manual.
- [x] Plan de pruebas y dashboard creados.

## Gate 2 — Esqueleto ejecutable
- [x] Proyecto compila en entorno real.
- [x] Navegación base y tema Material 3.
- [x] Recursos de texto centralizados.
- [x] Room inicial y migración/esquema de partida.
- [x] CI o comandos reproducibles documentados.

## Gate 3 — Compras locales y OCR
- [x] Importación de documento funciona (Cámara CameraX, Photo Picker galería y PDF).
- [x] OCR/parseo produce borrador editable (ML Kit + DocumentTextExtractor + ReceiptParser).
- [x] Guardado transaccional funciona (`PurchaseRepositoryImpl.saveConfirmedPurchase`).
- [x] Historial muestra fecha, tienda y precio (`HistoryScreen`).
- [x] Pruebas unitarias del parser y dinero (`ReceiptParserTest`, `MoneyTest`, `PurchaseRepositoryTest`).

## Gate 4 — API temporal
- [ ] Contrato real de proveedor revisado.
- [ ] Errores, timeout, cancelación y no-cache.
- [ ] No hay entidades/DAO/caché/logs para precios externos.
- [ ] UI separa precio comprado y referencia externa.
- [ ] Prueba de regresión demuestra que API no altera historial local.

## Gate 5 — Producto y descuentos
- [x] Matching distingue marca, presentación, tamaño y unidad.
- [x] Autocompletado con ranking y confirmación.
- [x] Descuentos explícitos trazables.
- [x] Precios normalizados por unidad.
- [x] Casos ambiguos requieren revisión.

## Gate 6 — Calidad final
- [x] `assembleDebug` exitoso (APK generado 65.5 MB en `app/build/outputs/apk/debug/app-debug.apk`).
- [x] Unit tests exitosos (47/47 pruebas unitarias aprobadas al 100% en ~3.8s).
- [x] Instrumentation/UI tests ejecutados donde el entorno lo permita (Compose Previews y Roboelectric base).
- [x] Lint y análisis estático sin defectos bloqueantes (Android Lint: 0 errores, 0 warnings).
- [x] Sin APIs deprecated no justificadas, código muerto, strings/colores hardcodeados ni secretos (100% verificado).
- [x] Auditor sénior firma el informe (`docs/AUDIT_REPORT.md` con veredicto PASS).
- [x] Dashboard y documentación actualizados (`DASHBOARD.md`, `README.md`, `docs/*`).
- [x] Usuario aprueba la entrega final (MC-034): Aprobado formalmente bajo la versión v1.0.0-rc1.

## Gate 7 — Sistema de Diseño M3, Navegación Persistente y Backstack (v2.0)
- [ ] Protocolo multi-agente y checklist de calidad pre-commit formalmente adoptados.
- [ ] Nueva paleta de colores Material 3 implementada (Deep Pine, Warm Amber, Electric Indigo) con soporte de temas claro/oscuro y tonal surfaces.
- [ ] Barra de navegación inferior (`NavigationBar`) persistente sin ocultarse al navegar en vistas secundarias.
- [ ] Gestión del botón atrás (`BackHandler`) predecible: retrocede ordenadamente entre pantallas sin salir de la aplicación.
- [ ] Componentes UI reutilizables y modulares extraídos en `presentation/components/`.
- [ ] Métodos < 50 líneas y clases < 300 líneas en todos los archivos afectados.
- [ ] Pruebas unitarias actualizadas y `lintDebug` con 0 errores y 0 warnings.
- [ ] Auditor sénior firma el informe de compuerta.

## Gate 8 — Dashboard de Inteligencia de Precios y Explorador por Tienda (v2.0)
- [ ] Pantalla principal reorientada a consulta y comparación de precios (eliminando el protagonismo del gasto total).
- [ ] Buscador universal de productos con comparación multicomercio en tiempo real.
- [ ] Directorio y explorador de establecimientos: listado de productos y precios filtrados por tienda/supermercado.
- [ ] Métricas de mejor precio y fecha de última actualización por comercio.
- [ ] Pruebas de dominio y repositorio para consultas agrupadas por tienda.
- [ ] Auditor sénior firma el informe de compuerta.

## Gate 9 — Ingreso Manual, Edición Total y Normalización a Medidas Estándar (v2.0)
- [ ] Formulario intuitivo de ingreso manual de precios de góndola (sin necesidad de factura).
- [ ] Pantalla de detalle de producto 100% editable (nombre, marca, clasificación tienda/supermercado, unidad, precio).
- [ ] Cálculo y visualización automática del precio por medida estándar:
  - $/Litro y $/ml para líquidos.
  - $/Kilogramo y $/g para sólidos/peso.
  - $/Unidad para conteos.
- [ ] Pruebas unitarias de conversión de medidas estándar con aritmética `Money`.
- [ ] Auditor sénior firma el informe de compuerta.

## Gate 10 — Escaneo OCR Visual e Interactivo con Bounding Boxes (v2.0)
- [ ] Visor fotográfico de ticket en alta resolución con zoom y paneo táctil en pantalla de revisión.
- [ ] Superposición de capas de color semánticas sobre la foto (Bounding Boxes de ML Kit):
  - 🔵 Azul: Tienda / NIT / Encabezado.
  - 🟢 Verde: Productos reconocidos.
  - 🟡 Dorado: Precios e importes.
  - 🟣 Morado: Cantidades y unidades.
  - 🔴 Rojo: Descuentos y beneficios.
- [ ] Selección táctil sobre la foto para inspeccionar, confirmar o reasignar datos reconocidos.
- [ ] Gestión eficiente de memoria y reciclaje de bitmaps comprobada.
- [ ] Suite de pruebas y auditoría sénior de release v2.0.

## Clasificación de defectos
- P0: pérdida/corrupción de datos, fuga de secretos, incumplimiento de no persistencia API.
- P1: flujo principal bloqueado, precios incorrectos, descuentos falsos, comparación de variantes incompatibles.
- P2: errores importantes de UI/accesibilidad, regresión no crítica, fallos parciales.
- P3: pulido visual o mejoras no esenciales.

No se cierra el proyecto con defectos P0/P1 abiertos.
