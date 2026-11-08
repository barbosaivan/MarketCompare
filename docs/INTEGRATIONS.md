# Integraciones — MarketCompare

- **Estado:** `IMPLEMENTED` para Cámara, Galería, PDF y ML Kit; `BLOCKED` para API Externa de Precios.
- **Última actualización:** 2026-11-08

---

## 1. Cámara y Captura Fotográfica (CameraX)
- **Tecnología:** Jetpack CameraX (`camera-camera2`, `camera-lifecycle`, `camera-view`).
- **Configuración:** Modo de análisis optimizado para texto, enfoque táctil, guía visual de encuadre en pantalla.
- **Permisos:** Solicitud en tiempo de ejecución de `android.permission.CAMERA` gestionada mediante Compose Permissions API.
- **Privacidad:** La captura se procesa en búfer de memoria local y nunca se sube a servicios en la nube.

---

## 2. Galería y Sistema de Archivos (Photo Picker y Storage Access Framework)
- **Photo Picker de Android:**
  - Uso de `ActivityResultContracts.PickVisualMedia` para imágenes individuales (JPG, PNG).
  - Permite acceso seguro a la foto seleccionada por el usuario sin requerir permisos de lectura amplia de almacenamiento (`READ_MEDIA_IMAGES` / `READ_EXTERNAL_STORAGE`).
- **Storage Access Framework (SAF):**
  - Uso de `ActivityResultContracts.OpenDocument` para archivos PDF (`application/pdf`).
  - Resolución segura de streams mediante `ContentResolver.openInputStream(uri)`.

---

## 3. Reconocimiento Óptico de Caracteres (Google ML Kit Text Recognition)
- **Librería:** `com.google.android.gms:play-services-mlkit-text-recognition:19.0.1`.
- **Ejecución:** Completamente **on-device** (local en el dispositivo), sin llamadas de red ni telemetría de imágenes.
- **Pipeline:**
  - Recepción de `InputImage` generado a partir de bitmap capturado o renderizado.
  - Extracción de bloques de texto, líneas y rectángulos delimitadores (`BoundingBox`).
  - Entrega de texto plano al motor `ReceiptParser`.

---

## 4. Procesamiento de Facturas Electrónicas PDF
- **Estrategia Dual de Extracción:**
  - **Fase 1 (Nativa):** Se inspeccionan los streams internos del archivo PDF buscando bloques de texto plano digital.
  - **Fase 2 (Rasterizado Fallback):** Si el documento contiene imágenes escaneadas sin capas de texto vectorial, se utiliza la API nativa de Android `PdfRenderer` para generar bitmaps de alta resolución a 300 DPI de las páginas relevantes y se derivan al motor de OCR de ML Kit.

---

## 5. API Externa de Precios de Referencia
- **Estado Actual:** `BLOCKED` (En espera de credenciales oficiales y documentación de `losprecios.co`).
- **Política de Privacidad y Efimeridad (ADR 0001):**
  - Los endpoints de cotización externa se consultan bajo demanda exclusiva del usuario.
  - Las respuestas recibidas residen temporalmente en memoria RAM (`ExternalPriceQuote`) para visualización comparativa en UI.
  - **Prohibición estricta:** Ningún dato devuelto por la API externa puede persistirse en Room DB, DataStore, almacenamiento interno, caché persistente o archivos de registro.
  - En caso de indisponibilidad de red o caída del servicio externo, la aplicación opera al 100% en modo local sin degradar las funcionalidades de compras ni la lista de canasta.
