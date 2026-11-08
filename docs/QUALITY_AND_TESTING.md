# Estrategia de Calidad y Suite de Pruebas — MarketCompare

- **Estado:** `CERTIFIED` (Gate 6 completado)
- **Última Certificación:** 2026-11-08
- **Regla de integridad:** No declarar pruebas aprobadas sin ejecución verificable y logs limpios.

---

## 1. Pirámide de Pruebas y Certificación Formal

```
                   / \
                  /   \
                 / UI  \        (Compose Previews / Navegación M3)
                / Robo- \
               / electric\
              /-----------\
             / Integración \    (Room DB in-memory, Transacciones atómicas)
            /---------------\
           / Pruebas Unitarias\ (47 tests: Money, Parser, Dominio, Canasta)
          /---------------------\
```

### Resultados de Ejecución Automatizada (Suite Completa)
- **Total de pruebas unitarias:** **47**
- **Pruebas aprobadas:** **47 (100%)**
- **Pruebas fallidas:** **0**
- **Tiempo medio de ejecución:** ~3.8 segundos.
- **Comando de verificación:** `./gradlew testDebugUnitTest`

---

## 2. Detalle de Suites de Pruebas Unitarias

| Suite de Prueba | Capa / Módulo | N° Tests | Cobertura Clave y Escenarios Críticos |
| :--- | :--- | :---: | :--- |
| **`MoneyTest`** | Domain / Value Object | 10 | Aritmética exacta en centavos `Long`, redondeo bancario `HALF_EVEN`, formateo en moneda colombiana COP, prevención de desbordamientos. |
| **`PurchaseRepositoryTest`** | Data / Room in-memory | 6 | Transacciones atómicas de compra, persistencia de recibos e ítems, recuperación cronológica y consulta de precios históricos por producto. |
| **`ProductNormalizerTest`** | Domain / Heurística | 10 | Extracción de marcas comerciales colombianas, cantidades y unidades (`G`, `KG`, `ML`, `L`, `UNIT`, `PACK`), presentaciones físicas, fingerprints canónicos y compatibilidad estricta de variantes. |
| **`ProductAutocompleteEngineTest`** | Domain / Search | 7 | Ranking de autocompletado ponderado, prioridad a coincidencias exactas y de prefijo, penalización y detección de colisión de variantes. |
| **`DiscountClassifierTest`** | Domain / Heurística | 6 | Clasificación de promociones (`LOYALTY_MEMBER`, `PROMOTION_2X1`, `EXPLICIT_LINE`, `DERIVED`, `UNKNOWN`), cálculo de ahorros y conservación de evidencia OCR. |
| **`ReceiptParserTest`** | Data / Parser | 4 | Clasificación de encabezados de tickets reales (Éxito, D1, Olímpica, Carulla), extracción de NITs, discriminación de líneas de cierre (IVA, subtotales, propinas) y cálculo de confianza. |
| **`BasketEstimatorTest`** | Domain / Estimación | 4 | Estimación de presupuesto para canasta de compras, ponderación por cantidad, y clasificación del semáforo de frescura (`RECENT`, `STALE`, `UNKNOWN`). |

---

## 3. Análisis Estático y Salud de Código (Android Lint)

- **Comando:** `./gradlew lintDebug`
- **Resultado:** **0 errores, 0 warnings** (Clean baseline).
- **Inspecciones Validadas:**
  - Cero cadenas de texto hardcodeadas en Compose (`HardcodedText` / `strings.xml` al 100%).
  - Cero colores hardcodeados en UI (`MaterialTheme.colorScheme` consistente).
  - Parámetros `modifier` ordenados según directrices canónicas de Jetpack Compose.
  - Íconos adaptativos con capa monocromática (`<monochrome>`) para Material You en Android 13+.
  - Reglas de proyecto centralizadas en [`app/lint.xml`](../app/lint.xml).

---

## 4. Auditoría Sénior Independiente de Código y Privacidad

- **Veredicto:** `PASS` (Aprobado sin objeciones).
- **Defectos Bloqueantes (P0 / P1):** 0 encontrados.
- **Cumplimiento de ADRs:**
  - **ADR 0001:** 100% verificado. Cero persistencia de cotizaciones externas en Room, DataStore, archivos o logs.
  - **ADR 0002:** 100% verificado. Fronteras estrictas de Clean Architecture entre Presentación, Dominio, Datos y Plataforma.
  - **ADR 0003:** 100% verificado. Cero tipos `Double` o `Float` para montos monetarios.
- **Credenciales y Secretos:** Cero llaves de API, tokens o credenciales expuestas en el repositorio.

---

## 5. Reportes de Calidad Relacionados
- [`TEST_SUITE_REPORT.md`](TEST_SUITE_REPORT.md): Reporte detallado de ejecución de la suite de pruebas.
- [`STATIC_ANALYSIS_AUDIT.md`](STATIC_ANALYSIS_AUDIT.md): Informe de auditoría de análisis estático y dependencias.
- [`AUDIT_REPORT.md`](AUDIT_REPORT.md): Reporte integral de auditoría sénior de arquitectura, seguridad y UI.
