# MarketCompare — Antigravity Agent Pack

Este paquete configura el trabajo asistido por agentes para una aplicación Android inicial con Kotlin Multiplatform + Compose Multiplatform.

## Qué contiene
- `MASTER_PROMPT.md`: instrucción principal para el agente coordinador.
- `PROJECT_RULES.md`: reglas persistentes de arquitectura, calidad, privacidad y UI.
- `ARCHITECTURE.md`: arquitectura objetivo y decisiones de diseño.
- `AGENT_TEAM.md`: perfiles, responsabilidades y límites de cada agente.
- `API_CONTRACT_TEMPLATE.md`: contrato provisional para integrar el proveedor de precios sin persistencia.
- `RECEIPT_PIPELINE.md`: OCR, PDF, parser, revisión y trazabilidad de descuentos.
- `QUALITY_GATES.md`: puertas de calidad y criterios de aceptación.
- `DASHBOARD.md`: dashboard inicial de seguimiento.
- `BACKLOG.csv`: tareas atómicas con dependencias, prioridad y criterios de validación.
- `.agent/rules/marketcompare.md`: regla de proyecto para Antigravity.
- `.agent/skills/marketcompare-orchestrator/SKILL.md`: habilidad de coordinación.
- `.agent/skills/marketcompare-audit/SKILL.md`: habilidad de auditoría sénior.
- `.agent/skills/marketcompare-release-gate/SKILL.md`: habilidad para validar hitos.

## Cómo usarlo
1. Descomprime el paquete en una carpeta de trabajo.
2. Abre esa carpeta como workspace en Antigravity.
3. Lee `MASTER_PROMPT.md` y envíalo al agente principal.
4. Pídele primero solo el diagnóstico del entorno y el plan de ejecución; no debe implementar todo de golpe.
5. Copia o adapta `.agent/rules/marketcompare.md` y las Skills a las ubicaciones de personalización reconocidas por tu versión de Antigravity. Las ubicaciones exactas pueden variar según superficie/versión.
6. Crea/lanza los agentes descritos en `AGENT_TEAM.md`, si tu superficie de Antigravity permite agentes separados. Si solo permite una conversación principal, úsala como coordinador y delega tareas mediante subagentes cuando esté disponible.
7. Mantén `DASHBOARD.md` y `BACKLOG.csv` actualizados después de cada tarea.

## Importante
- Este paquete no crea agentes en tu cuenta ni configura Antigravity automáticamente. Es un kit de instrucciones listo para importar/adaptar.
- No se inventa el endpoint de la API de precios. Debes facilitar documentación, esquema de autenticación, límites, licencia y ejemplos de respuesta antes de integrar el proveedor real.
- No se guardan resultados de precios de la API. Solo se permiten datos temporales en memoria para la sesión de comparación.
- El usuario debe aprobar hitos de producto y cualquier acción destructiva, pago, publicación o cambio de credenciales.

## Documentación viva
La carpeta `docs/` contiene visión del producto, arquitectura, funcionalidades, modelo de datos, sistema de diseño, configuración, integraciones, calidad, ADRs, glosario y guía de desarrollo. Los agentes deben actualizarla junto con el código.
