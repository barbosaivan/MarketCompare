# ADR 0001 — Compras locales y cotizaciones API efímeras

- **Estado:** ACEPTADO (ACCEPTED)
- **Fecha:** 2026-11-07
- **Decisores:** Tech Lead, Arquitectura, Usuario

## Contexto
MarketCompare es una aplicación Android enfocada en la privacidad y utilidad directa para el consumidor. La aplicación funciona sin registro ni cuenta obligatoria (offline-first). El usuario registra sus facturas físicas o digitales para auditar sus gastos reales, mientras que simultáneamente desea comparar precios de referencia ofrecidos por servicios o APIs externas de supermercados.

Existe un requerimiento estricto de diseño y cumplimiento normativo:
1. Las compras reales pagadas por el usuario son de su propiedad y deben persistirse en el dispositivo localmente.
2. Los precios provistos por APIs externas son exclusivamente de consulta y referencia temporal.
3. No se permite persistir respuestas de APIs externas en base de datos local (Room), DataStore, archivos de texto, almacenamiento interno/externo, logs de depuración, analytics ni en la caché persistente HTTP.

## Decisión
1. **Separación estricta de fuentes:** Se definen dos caminos de datos completamente disjuntos a nivel de tipos y ciclo de vida:
   - `PurchaseRecord` / `Receipt`: Datos locales respaldados por base de datos Room. Solo se crean cuando el usuario presiona "Confirmar compra".
   - `ExternalPriceQuote`: Modelos exclusivamente en memoria (in-memory state dentro de Coroutines Flow y ViewModels).
2. **Ciclo de vida efímero:**
   - La respuesta de la API externa solo reside en la memoria volátil mientras la pantalla de comparación esté activa.
   - Al navegar fuera de la pantalla de comparación, cambiar de producto o cancelar la consulta, el estado de cotización externa se purga inmediatamente.
3. **Prohibición de mezcla analítica:** Las cotizaciones externas nunca se computarán en medias históricas de compra, índices de inflación personal ni en el historial de precios pagados.
4. **Resiliencia sin datos falsos:** Si la API externa no está disponible o el dispositivo carece de conexión, la UI mostrará un estado explícito de "Sin conexión / Precios externos no disponibles". Bajo ninguna circunstancia se presentará una cotización antigua en caché haciéndola pasar por precio vigente.

## Consecuencias
- **Positivas:** Cumplimiento total de términos de redistribución de proveedores de precios; garantía absoluta de privacidad; cero riesgo de corrupción de históricos de gasto con estimaciones externas.
- **Negativas / Desafíos:** Requiere conectividad activa para consultar referencias externas y una disciplina rigurosa en el diseño de repositorios para evitar que capas superiores guarden accidentalmente cotizaciones externas.
