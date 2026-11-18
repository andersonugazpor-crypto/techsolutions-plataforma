# Pruebas de usabilidad

Se combinan dos técnicas:

1. **Evaluación heurística** del prototipo (10 heurísticas de Nielsen), hecha por el equipo antes de construir las pantallas. Los riesgos detectados y la solución aplicada en la app están en la sección 1.
2. **Prueba con usuarios** (5 participantes, tareas guiadas). El protocolo está en la sección 2; los resultados se registran en la sección 3.

## 1. Evaluación heurística: riesgos detectados y mejoras aplicadas

| # | Heurística | Riesgo detectado en el prototipo | Mejora implementada | Dónde |
|---|---|---|---|---|
| 1 | Visibilidad del estado del sistema | El usuario no sabía si un comando se ejecutó, se encoló o falló | Mensajes de éxito/error después de cada acción; badge de estado del pedido; contador de la cola | `fragments/layout.html`, `pedidos/detalle.html` |
| 2 | Visibilidad del estado del sistema | No se distinguía si el reporte estaba actualizado | Fecha de generación, etiqueta “Servido desde caché / Recién calculado” y botón *Regenerar* | `reportes.html` |
| 3 | Prevención de errores | Se podía intentar procesar o descontar un pedido cancelado | Botones deshabilitados según el estado (patrón State) + validación en el servidor | `pedidos/detalle.html`, `Estado*` |
| 4 | Prevención de errores | Cancelar o restaurar por error | Diálogo de confirmación antes de cancelar y antes de restaurar | `pedidos/detalle.html` |
| 5 | Prevención de errores | El campo “contacto” no indicaba qué dato pedía cada pasarela | La etiqueta y el ejemplo cambian según la pasarela (correo para PayPal, celular para Yape/Plin) | `pedidos/nuevo.html` |
| 6 | Control y libertad del usuario | No existía forma de deshacer una operación crítica | Puntos de restauración (Memento) listados con fecha y resumen | `pedidos/detalle.html` |
| 7 | Reconocer antes que recordar | Al registrar movimientos en inventario se perdían los filtros y la página | Los filtros y la página se conservan tras cada acción | `InventarioController` |
| 8 | Reconocer antes que recordar | Al crear un pedido no se veía el precio ni el stock | Cada opción muestra precio vigente y stock; total estimado en vivo | `pedidos/nuevo.html` |
| 9 | Flexibilidad y eficiencia | Catálogos grandes lentos de recorrer | Paginación con tamaño configurable, búsqueda por nombre/código, filtro por categoría y stock bajo | `catalogo.html` |
| 10 | Diseño estético y minimalista | Demasiada información en el panel | Inicio con 4 indicadores, accesos directos y actividad reciente | `inicio.html` |
| 11 | Ayuda a reconocer y recuperarse de errores | Mensajes técnicos ante pagos rechazados | Los adaptadores traducen los códigos de cada API a mensajes claros (p. ej. “Número de celular Yape inválido”) | `YapeAdaptador`, `PayPalAdaptador`, `PlinAdaptador` |
| 12 | Consistencia y estándares | Acceso denegado mostraba una página en blanco | Vista de aviso uniforme y, en reportes, resumen operativo sin montos | `error-negocio.html`, `reportes.html` |

## 2. Protocolo de prueba con usuarios

- **Participantes:** 5 (compañeros o personas que trabajen en una bodega/minimarket).
- **Duración:** 10–15 minutos por participante; pensar en voz alta.
- **Métricas:** tarea completada (sí/no), tiempo, errores, satisfacción (1–5).

| Tarea | Usuario a usar | Criterio de éxito |
|---|---|---|
| T1. Buscar los productos de “Bebidas” con stock bajo | `vendedor` | Aplica ambos filtros y encuentra el producto |
| T2. Crear un pedido de 3 latas de atún pagado con Yape | `vendedor` | Pedido creado con contacto válido |
| T3. Aplicar 5 % de descuento y procesar el pedido | `vendedor` | Pedido en estado *Procesado* |
| T4. Deshacer el procesamiento del pedido | `vendedor` | Pedido de nuevo en *Pendiente* |
| T5. Revisar la alerta de stock generada | `compras` | Encuentra la notificación de reposición |
| T6. Deshabilitar Plin y activar el precio con descuento | `admin` | Ambos cambios guardados |
| T7. Consultar el reporte financiero | `vendedor` y luego `gerente` | Entiende por qué el vendedor no lo ve |

## 3. Resultados (completar después de las pruebas)

| Participante | T1 | T2 | T3 | T4 | T5 | T6 | T7 | Satisfacción (1–5) | Comentarios |
|---|---|---|---|---|---|---|---|---|---|
| P1 | | | | | | | | | |
| P2 | | | | | | | | | |
| P3 | | | | | | | | | |
| P4 | | | | | | | | | |
| P5 | | | | | | | | | |

**Problemas observados y solución aplicada:**

| Problema observado | Participantes afectados | Solución aplicada | Commit |
|---|---|---|---|
| | | | |
