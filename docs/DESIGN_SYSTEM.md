# Sistema de Diseño — Material Design 3 — MarketCompare

- **Estado:** `IMPLEMENTED` (Gate 7 — Modern M3 Palette & Tokens)
- **Tarea asociada:** MC-008, MC-040
- **Idioma base de recursos:** Español (Colombia) — `es-rCO`

---

## 1. Principios de Interfaz y Calidad Visual

1. **Cero strings hardcodeados:** Todos los textos visibles se definen en recursos XML (`strings.xml`) respetando la internacionalización.
2. **Cero colores hardcodeados:** Todos los colores provienen estrictamente de `MaterialTheme.colorScheme` y `MaterialTheme.semanticColors` para asegurar soporte completo de modo claro, modo oscuro y Material You (Dynamic Color).
3. **Tipografía Escalable:** Utilización exclusiva de `MaterialTheme.typography` respetando las preferencias de accesibilidad del usuario (escalado de fuentes del sistema).
4. **Accesibilidad (a11y):** Contraste mínimo WCAG AA (4.5:1), objetivos táctiles mínimos de 48dp y `contentDescription` obligatorio.
5. **Navegación Persistente y Backstack Natural:** Barra de navegación inferior (`NavigationBar`) fija en flujos primarios y gestión fluida del botón atrás (`BackHandler`).

---

## 2. Paleta de Colores Vanguardista (Material Design 3)

Inspirada en **Inteligencia Financiera, Ahorro Inteligente y Claridad Analítica**:

### A. Roles Cromáticos Principales
| Rol Semántico M3 | Token Light | Hex Light | Token Dark | Hex Dark | Intención de Diseño |
| :--- | :--- | :---: | :--- | :---: | :--- |
| **Primary** | `PrimaryLight` | `#006A60` | `PrimaryDark` | `#53DBC9` | **Deep Pine / Teal:** Identidad de marca, estabilidad económica y confianza. |
| **Primary Container** | `PrimaryContainerLight` | `#73F8E6` | `PrimaryContainerDark` | `#005048` | Contenedores destacados, botones de acción positiva y chips seleccionados. |
| **Secondary** | `SecondaryLight` | `#7C5800` | `SecondaryDark` | `#FBBB42` | **Warm Amber:** Indicador de ahorro, promociones 2x1 y descuentos. |
| **Secondary Container**| `SecondaryContainerLight` | `#FFDF9E` | `SecondaryContainerDark`| `#5E4200` | Badges de beneficios de fidelización y alertas de oportunidad de precio. |
| **Tertiary** | `TertiaryLight` | `#4355B9` | `TertiaryDark` | `#B9C3FF` | **Electric Indigo:** Comparativa multicomercio, tiendas y analítica. |
| **Tertiary Container** | `TertiaryContainerLight` | `#DEE0FF` | `TertiaryContainerDark` | `#2A3C9F` | Contenedores de catálogo por tienda y filtros de comercio. |
| **Error** | `ErrorLight` | `#BA1A1A` | `ErrorDark` | `#FFB4AB` | Alertas críticas, inconsistencias o fallas de captura. |

### B. Superficies Tonales M3 (Elevaciones Suaves)
Se implementa el conjunto completo de tonal surface containers para eliminar sombras agresivas:
- `surfaceContainerLowest`, `surfaceContainerLow`, `surfaceContainer`, `surfaceContainerHigh`, `surfaceContainerHighest`.

### C. Tokens Semánticos (`MaterialTheme.semanticColors`)
Tokens dedicados para el visor OCR interactivo y el semáforo de frescura:
- **`ocrStoreBlue`:** Identificación de comercio y NIT (`#1565C0` Light / `#90CAF9` Dark).
- **`ocrProductGreen`:** Nombres y descripciones de productos (`#2E7D32` Light / `#81C784` Dark).
- **`ocrPriceGold`:** Precios e importes cobrados (`#C67C00` Light / `#FFD54F` Dark).
- **`ocrQuantityPurple`:** Cantidades, empaques y pesos (`#6A1B9A` Light / `#CE93D8` Dark).
- **`ocrDiscountRed`:** Descuentos y beneficios (`#C62828` Light / `#E57373` Dark).
- **`freshnessRecent`:** Precios vigentes <= 30 días (`#2E7D32` Light / `#81C784` Dark).
- **`freshnessStale`:** Precios desactualizados > 30 días (`#E65100` Light / `#FFB74D` Dark).
- **`freshnessUnknown`:** Precios sin observación previa (`#616161` Light / `#BDBDBD` Dark).

---

## 3. Mapa de Navegación y Rutas (v2.0)

```
                       +-------------------------------+
                       |   RootAppScaffold (Persistent)|
                       +---------------+---------------+
                                       |
          +----------------------------+----------------------------+
          |                            |                            |
          v                            v                            v
+--------------------+       +--------------------+       +--------------------+
|  HomePriceExplorer |       |   ReceiptCapture   |       |    ShoppingList    |
| (Buscador universal|       | (Cámara/Galería/PDF|       | (Canasta estimada  |
|  tiendas y precios)|       |  y Visor OCR)      |       |  y compras activas)|
+---------+----------+       +---------+----------+       +--------------------+
          |                            |
          v                            v
+--------------------+       +--------------------+
| StoreCatalogScreen |       | ReceiptReviewScreen|
| (Catálogo filtrado |       | (Visor con Bounding|
|  por tienda/super) |       |  Boxes coloreados) |
+--------------------+       +--------------------+
```

---

## 4. Catálogo de Componentes Centralizados

- **`NavigationBar` Persistente:** Mantiene visibles las 3 pestañas principales en el Scaffold raíz.
- **`PriceCard`:** Tarjeta con tipografía numérica destacada y badge de frescura.
- **`StandardUnitBadge`:** Muestra la tasa normalizada obligatoria ($\text{COP/kg}$, $\text{COP/L}$, $\text{COP/und}$).
- **`ConfidenceIndicator`:** Semáforo visual del OCR.
- **`DiscountAuditDialog`:** Diálogo interactivo de auditoría de ahorro.
- **`BoundingBoxOverlay`:** Capa de renderizado visual sobre la fotografía del ticket con colores semánticos.
