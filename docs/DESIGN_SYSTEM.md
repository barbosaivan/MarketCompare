# Sistema de Diseño — Material Design 3 — MarketCompare

- **Estado:** DEFINIDO (Gate 1)
- **Tarea asociada:** MC-008
- **Idioma base de recursos:** Español (Colombia) — `es-rCO`

---

## 1. Principios de Interfaz y Calidad Visual

1. **Cero strings hardcodeados:** Todos los textos visibles se definen en recursos XML (`strings.xml`) o en catálogos de recursos de KMP.
2. **Cero colores hardcodeados:** Todos los colores provienen estrictamente de `MaterialTheme.colorScheme` para asegurar compatibilidad total con Modo Claro y Modo Oscuro.
3. **Tipografía Escalable:** Utilización exclusiva de `MaterialTheme.typography` respetando las preferencias de accesibilidad del usuario (escalado de texto en Android).
4. **Accesibilidad (a11y):** Etiquetas de contenido (`contentDescription`) obligatorias en botones de íconos, contraste mínimo WCAG AA (4.5:1) y objetivos táctiles mínimos de 48dp.

---

## 2. Mapa de Navegación y Rutas

La navegación se implementa con Compose Navigation con destinos fuertemente tipados:

```
                      +-------------------+
                      |   HistoryScreen   | (Pantalla Principal / Historial)
                      +---------+---------+
                                |
             +------------------+------------------+
             |                                     |
             v                                     v
+------------------------+             +------------------------+
|  ReceiptCaptureScreen  |             |  ProductDetailScreen   |
| (Cámara / Galería / PDF|             | (Evolución histórica y |
+-----------+------------+             |  comparativa externa)  |
            |                          +------------------------+
            v                                      |
+------------------------+                         v
|  ReceiptReviewScreen   |             +------------------------+
| (Revisión editable de  |             |   ShoppingListScreen   |
|  artículos detectados) |             | (Lista y estimación de |
+-----------+------------+             |  costo con datos propios|
            |                          +------------------------+
      (Confirmar)
            |
            v
     (Retorna a Historial)
```

### Rutas Declaradas:
- `Route.History`: Historial de facturas y resumen de compras del usuario.
- `Route.ReceiptCapture`: Selección de método de captura (Cámara, Galería, PDF).
- `Route.ReceiptReview(draftId: String)`: Pantalla editable de revisión del ticket.
- `Route.ProductDetail(productId: String)`: Historial de precios pagados por ese producto y comparación en tiempo real contra cotización externa.
- `Route.ShoppingList`: Gestión de listas de compras con presupuesto estimado según precios locales históricos.

---

## 3. Catálogo de Componentes Centralizados

- **`AppScaffold`**: Barra superior de la aplicación (`TopAppBar`), navegación inferior y manejo de Snackbars para retroalimentación.
- **`PriceCard`**: Tarjeta destacada que presenta importes con diferenciación cromática según la procedencia.
- **`SourceBadge`**:
  - `SourceBadge.ConfirmedPurchase`: Indica "Compra real en [Tienda]" con badge primario.
  - `SourceBadge.ExternalQuote`: Indica "Referencia externa (En memoria)" con badge neutro y aviso de consulta en vivo.
- **`MoneyText`**: Componente de texto especializado para importes en COP con formato de miles y moneda.
- **`ReceiptItemRow`**: Fila con casilla de verificación, descripción original editable, selector de cantidad y campo de precio/descuento.
- **`ConfidenceIndicator`**: Semáforo visual (Alto: verde, Medio: ámbar, Bajo: rojo) para alertar al usuario cuando un valor OCR deba revisarse con especial cuidado.
- **`EmptyState` / `LoadingState` / `ErrorState`**: Estados estándar y uniformes para todas las pantallas.
