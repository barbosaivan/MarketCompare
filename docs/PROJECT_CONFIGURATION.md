# Configuración del proyecto — MarketCompare

Registro verificado del entorno y decisiones técnicas base.

## Entorno de desarrollo verificado (Gate 0)

- **Sistema Operativo:** Windows 11 Home / Pro (x64)
- **Control de Versiones:** Git 2.45.1.windows.1
- **IDE:** Android Studio (`C:\Program Files\Android\Android Studio2`)
  - **Build:** `AI-262.10968.63.2622.16840690`
- **JDK de compilación (JBR):** OpenJDK 25.0.4 (`C:\Program Files\Android\Android Studio2\jbr`)
  - Build: `build 25.0.4+-16339643-b508.27, mixed mode`
- **Android SDK:** Ubicación estándar `%LOCALAPPDATA%\Android\Sdk` (gestionado vía `local.properties`).
- **Gradle:** Se utilizará Gradle Wrapper (`gradlew`) en la raíz del proyecto.
- **Moneda base:** Peso Colombiano (COP). Cantidades monetarias representadas en enteros (unidades menores / centavos) o tipos decimales exactos, prohibido `Double` para cálculos de dinero.

## Tecnologías y librerías objetivo (MVP)

- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose Multiplatform + Material Design 3
- **Persistencia local:** Room compatible con KMP (SQLite) para compras confirmadas
- **Cámara & Entrada de documentos:** CameraX + Activity Result Contracts (SAF / Photo Picker)
- **OCR:** Google ML Kit Text Recognition (Android)
- **Manejo de PDF:** Extracción directa de texto; fallback a `PdfRenderer` + ML Kit para documentos escaneados
- **Cliente HTTP (Precios externos):** Ktor Client (diseñado estrictamente en memoria, caché deshabilitada, sin persistencia en Room ni disco)

## Estado de integraciones externas

- **API de precios de supermercados:** Estado **BLOQUEADO (`BLOCKED`)**. No se implementará hasta contar con documentación oficial del proveedor, endpoints y política de autenticación y límites.
