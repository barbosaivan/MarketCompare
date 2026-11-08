# Visión del Producto — MarketCompare

- **Estado Actual:** `IMPLEMENTED` (Gates 0, 1, 2, 3, 5, 6 completados; Gate 4 `BLOCKED` a la espera de credenciales oficiales de API externa).
- **Enfoque de Privacidad:** 100% Local-First / Offline-First (ADR 0001, ADR 0002, ADR 0003).

---

## 1. Propósito y Propuesta de Valor
MarketCompare es una aplicación Android nativa diseñada para que los consumidores colombianos tomen el control de sus gastos de supermercado. Permite registrar precios observados en compras reales mediante la digitalización asistida por OCR de facturas físicas y electrónicas (PDF), gestionar listas de compras con presupuestos estimados y comparar en tiempo real contra cotizaciones de referencia externas sin comprometer la privacidad del usuario.

## 2. Principios Rectores
1. **Local-First & Offline-First:** Toda la información de compras, facturas, historial y listas reside exclusivamente en la base de datos local SQLite (Room) en el dispositivo. No requiere registro ni login ni envía datos de recibos a servidores externos.
2. **Revisión Humana Obligatoria:** Ningún recibo ni ítem se almacena de forma automática o desatendida. El usuario siempre revisa, edita y confirma las extracciones en la pantalla `ReceiptReviewScreen`.
3. **Cotizaciones Externas Efímeras (ADR 0001):** Las cotizaciones obtenidas de APIs externas se consumen bajo demanda, residen exclusivamente en memoria RAM (StateFlow de ViewModel) y jamás se persisten en base de datos, caché de disco o archivos.
4. **Precisión Financiera Exacta (ADR 0003):** Se prohíbe el uso de tipos de coma flotante (`Double`/`Float`) para montos monetarios. Todas las operaciones usan enteros `Long` en unidades menores (centavos) mediante el Value Object `Money`.
5. **Diferenciación Estricta de Variantes:** Dos productos con la misma marca pero diferente presentación o contenido neto son tratados como entidades distintas para evitar comparaciones engañosas.

---

## 3. Alcance Implementado en el MVP (v0.5.0)
- **Ingesta Multicanal de Facturas:**
  - Captura directa con cámara mediante CameraX con guía de encuadre.
  - Selección de imágenes desde galería (Photo Picker del sistema).
  - Carga de facturas electrónicas PDF (Storage Access Framework) con extracción dual (texto nativo para PDFs digitales y rasterizado `PdfRenderer` + ML Kit para PDFs escaneados).
- **Motor Heurístico de Parseo OCR (`ReceiptParser`):** Detección de comercio, fecha, líneas de productos, importes, cantidades y cálculo de confianza.
- **Normalización y Desambiguación (`ProductNormalizer`):** Reconocimiento de marcas colombianas, presentaciones, unidades y generación de `variantFingerprint`.
- **Autocompletado Inteligente (`ProductAutocompleteEngine`):** Sugerencias ponderadas y diálogo de confirmación ante colisión de variantes incompatibles.
- **Auditoría de Descuentos (`DiscountClassifier` & `DiscountAuditDialog`):** Identificación de descuentos (`LOYALTY_MEMBER`, `PROMOTION_2X1`, `EXPLICIT_LINE`, `DERIVED`), preservación de evidencia OCR y diálogo interactivo de inspección.
- **Historial de Compras (`PurchaseHistoryScreen`):** Lista cronológica de recibos con detalle expandible de ítems y totales.
- **Lista de Compras y Estimación de Canasta (`ShoppingListScreen` & `BasketEstimator`):** Artículos pendientes con checkbox, cálculo en tiempo real de presupuesto total estimado basado en compras locales previas, y clasificación de frescura de precios (`RECENT`, `STALE`, `UNKNOWN`).
- **Navegación Unificada Material 3:** Barra inferior (`NavigationBar`) con accesos a Historial, Escáner y Lista de Compras.

---

## 4. Estado de Integración de API Externa (Gate 4)
- **Estado:** `BLOCKED`.
- Se gestionó solicitud formal de credenciales (`ClaveAPI`) y documentación técnica ante el proveedor oficial (`losprecios.co`). La arquitectura (ADR 0001) y los contratos de repositorio (`ExternalPriceQuote`, `ComparePricesUseCase`) están totalmente preparados para integrarse de inmediato en cuanto se otorgue el acceso oficial.

---

## 5. Fuera de Alcance para el MVP
- Cuentas de usuario, autenticación y login en la nube.
- Sincronización multi-dispositivo en backend propio.
- Persistencia o agregación masiva de cotizaciones externas.
