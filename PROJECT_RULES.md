# Reglas permanentes del proyecto — MarketCompare

---

## 1. Protocolo Multi-Agente y Cadena de Revisión Obligatoria
A partir de la versión 2.0, **toda tarea técnica o funcional debe transitar obligatoriamente por una revisión exhaustiva de cada rol de agente** antes de ser integrada:

1. **Tech Lead / Arquitectura (A0/A1):** Valida viabilidad técnica, límites de Clean Architecture (ADR 0002), modularidad y ausencia de sobreingeniería.
2. **Datos Locales / Dominio (A4):** Verifica diseño relacional Room, DAOs, transacciones atómicas, Value Object `Money` (ADR 0003), migraciones y garantía de no-persistencia externa (ADR 0001).
3. **OCR / Plataforma (A3):** Supervisa el ciclo de vida de bitmaps, liberación de memoria en `PdfRenderer`/ML Kit, gestión de CameraX y permisos del sistema.
4. **UI / UX Material Design 3 (A2):** Garantiza diseño vanguardista, navegación persistente, inmutabilidad de UiState, cero hardcoded text/colors y extracción de componentes reutilizables.
5. **QA / Automatización (A6):** Diseña pruebas unitarias/instrumentadas para casos típicos y de borde (overflows, divisas, textos ruidosos).
6. **Auditor Sénior Transversal (A7):** Ejecuta la inspección de código estricta previa a commit (checklist de calidad pre-commit).
7. **Documentación / Release (A8):** Sincroniza en tiempo real `BACKLOG.csv`, `DASHBOARD.md`, changelog técnico y especificaciones vivas en `docs/`.

---

## 2. Protocolo de Inspección Pre-Commit Obligatorio
Antes de cada commit en cualquier rama de tarea, el Auditor Sénior y los implementadores deben certificar:
- **Código muerto y limpieza:** Cero imports sin uso, variables huérfanas o funciones inaccesibles.
- **Code smells y granularidad:** Clases con menos de 300 líneas de código y métodos/Composables con menos de 50 líneas (obligación de dividir en sub-composables especializados).
- **APIs obsoletas o deprecadas:** Cero uso de APIs deprecated de Android SDK o Jetpack Compose sin justificación arquitectónica documentada.
- **Componentes y vistas reutilizables:** Prohibición de duplicar widgets, tarjetas o campos; extracción inmediata al paquete de componentes compartidos (`presentation/components/`).
- **Compose Lint y rendimiento:** Parámetro `modifier: Modifier = Modifier` como primer parámetro opcional, lambdas finales fuera de paréntesis, y estabilidad en recomposiciones (`@Immutable` / `@Stable`).
- **Análisis estático:** Ejecución de `./gradlew lintDebug` certificando **0 errores y 0 warnings**.

---

## 3. UI, Experiencia de Usuario y Material Design 3
- **Identidad Visual Moderna:** Paleta de colores atractiva y sofisticada inspirada en finanzas inteligentes (Deep Pine / Vibrant Teal, Warm Amber para ahorros/descuentos, y Electric Indigo para comparativas), aprovechando las tonalidades de M3 (`surfaceContainer`, `surfaceContainerHigh`) en tema claro y oscuro.
- **Navegación Persistente:** La barra de navegación inferior (`NavigationBar`) debe permanecer visible durante los flujos primarios y secundarios de catálogo, evitando que una vista hija oculte el menú principal de navegación.
- **Gestión Predecible del Backstack (`BackHandler`):** El botón nativo de "Atrás" debe navegar fluidamente a la pantalla previa o cerrar diálogos/hojas modales en lugar de salir abruptamente de la aplicación.
- **Todo texto visible:** Debe residir exclusivamente en `res/values/strings.xml` (español de Colombia, preparado para internacionalización).
- **Todo color:** Debe provenir estrictamente de `MaterialTheme.colorScheme` o tokens semánticos definidos.
- **Tipografía y espaciado:** Uso de `MaterialTheme.typography` y rejilla de espaciado estándar (4dp, 8dp, 16dp, 24dp).
- **Estados de pantalla completos:** Manejo explícito de carga (`Loading`), vacío (`Empty`), error (`Error`), éxito (`Success`) y cancelación (`Cancelled`).

---

## 4. Dominio, Datos y Precios de Referencia
- **Enfoque en Precios y No en Gastos:** La aplicación es una herramienta de **inteligencia de precios de góndola**, no un gestor contable de presupuesto o gastos totales. Las pantallas principales deben empoderar al usuario para encontrar dónde está más barato un producto y cómo varían los precios.
- **Ingreso Manual de Productos:** La app debe permitir registrar productos y precios tanto por escaneo OCR como de forma **manual** directa (para compras sin ticket o precios vistos en góndola).
- **Ficha de Producto 100% Editable:** Toda observación de precio y producto debe ser editable: corregir nombre comercial, marca, clasificación del establecimiento (*Tienda de barrio* vs *Supermercado de cadena*), unidad y precio.
- **Listado y Filtro por Tienda:** El usuario debe poder explorar los productos y precios filtrados por tienda o supermercado específico.
- **Normalización a Medidas Estándar:** Además del precio del empaque, el sistema debe calcular y mostrar siempre el **precio por unidad base estándar**:
  - Líquidos: **Precio por Litro ($/L)** y $/ml.
  - Sólidos / Peso: **Precio por Kilogramo ($/kg)** y $/g.
  - Conteo / Unidades: **Precio por Unidad ($/und)**.
- **Aritmética Financiera Exacta (ADR 0003):** Prohibido el uso de `Double` o `Float` para dinero; uso exclusivo de `Long` en centavos con el Value Object inmutable `Money`.
- **Precios Externos Efímeros (ADR 0001):** Cotizaciones API en memoria RAM (`StateFlow`), jamás persistidas en SQLite Room, DataStore, archivos, preferencias, logs o caché.
- **Transacciones Atómicas:** Toda persistencia local de factura o compra con múltiples ítems se ejecuta en una sola transacción Room con rollback automático ante fallo.

---

## 5. OCR, Bounding Boxes e Interacción Visual
- **Visor Visual Interactivo:** La pantalla de revisión de facturas debe exhibir la fotografía o página PDF capturada con capacidad de paneo y zoom.
- **Capas Semánticas con Bounding Boxes Coloreados:**
  - 🔵 **Azul:** Nombre del comercio, NIT y metadatos de encabezado.
  - 🟢 **Verde:** Nombres y descripciones de productos reconocidos.
  - 🟡 **Dorado:** Precios unitarios e importes finales.
  - 🟣 **Morado:** Cantidades, pesos y unidades de empaque.
  - 🔴 **Rojo / Naranja:** Descuentos y beneficios de fidelización.
- **Interacción Táctil:** El usuario debe poder pulsar cualquier caja delimitadora en la foto para inspeccionar, confirmar o corregir el dato extraído.
- **Liberación de Memoria:** Reciclaje explícito de bitmaps y cierre de descriptores de archivos PDF tras el procesamiento para evitar OOM (Out Of Memory).

---

## 6. Git, Calidad y Trazabilidad
- **Gitflow Estricto:** Rama `main` para releases estables con tags semánticos (ej. `v1.0.0-rc1`); rama `develop` para integración continua; ramas temáticas `feat/*`, `qa/*`, `audit/*`, `docs/*`.
- **Fusiones con `--no-ff`:** Preservar siempre la historia de ramas temáticas al fusionar en `develop`.
- **Conventional Commits:** `<tipo>(<ámbito>): <descripción imperativa> [<ID_TAREA>]`.
- **Cero Secretos:** Cero credenciales, tokens o datos fiscales reales en el repositorio.
- **Quality Gates:** No avanzar a un nuevo hito con defectos P0 o P1 abiertos ni sin la firma formal del Auditor Sénior y la aprobación del usuario.
