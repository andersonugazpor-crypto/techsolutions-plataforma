# Guía para armar el prototipo interactivo en Figma

El prototipo de baja fidelidad navegable está en `docs/prototipo/index.html` (abrir en el navegador; los enlaces y botones navegan entre pantallas). Las capturas de cada pantalla están en `docs/img/wireframes/`. Esta guía indica cómo llevarlo a Figma para el entregable.

## 1. Preparación del archivo

1. Crear un archivo nuevo en Figma: **TechSolutions – Plataforma de Gestión**.
2. Crear dos páginas: **Wireframes** (baja fidelidad, en grises) y **Prototipo** (alta fidelidad, con la paleta de la app).
3. Tamaño de frame: **Desktop 1280 × 800**.
4. Opcional y rápido: arrastrar las imágenes de `docs/img/wireframes/*.png` a la página *Wireframes*, una por frame, como referencia, y redibujar encima.

## 2. Estilos (página Prototipo)

| Estilo | Valor |
|---|---|
| Azul primario | `#1D4ED8` |
| Azul oscuro (barra lateral) | `#1E3A8A` |
| Texto | `#0F172A` · texto secundario `#64748B` |
| Bordes | `#E2E8F0` · fondo `#F8FAFC` |
| Éxito / Error / Aviso | `#15803D` / `#B91C1C` / `#B45309` |
| Tipografía | Segoe UI o Inter · títulos 24 px semibold · cuerpo 15 px |
| Radios | tarjetas 10 px · botones 8 px |

## 3. Componentes reutilizables

Crear como componentes (con variantes):

- **Sidebar** — variante por ítem activo (Inicio, Catálogo, Pedidos, Cola, Inventario, Notificaciones, Reportes, Configuración).
- **Botón** — Primario / Secundario / Peligro / Éxito · estado Normal / Deshabilitado.
- **Badge de estado** — Pendiente (ámbar) / Procesado (verde) / Cancelado (rojo) / Info.
- **Tarjeta KPI**, **Tarjeta de producto**, **Fila de tabla**, **Campo de texto**, **Select**, **Alerta** (éxito, error, info, aviso).

## 4. Pantallas (frames)

| # | Frame | Contenido clave | Requerimiento |
|---|---|---|---|
| 1 | Login | Usuario, contraseña, tabla de usuarios demo | — |
| 2 | Inicio | 4 KPI, estrategia activa, pasarelas habilitadas, cola, actividad reciente | — |
| 3 | Catálogo | Filtros (texto, categoría, stock bajo, tamaño), grilla de productos, paginación | RF11, RF12 |
| 4 | Inventario | Tabla con stock, estado, stock mínimo editable, entrada/salida | RF5, RF6 |
| 5 | Pedidos | Lista con estado y total, acceso a cola | RF7 |
| 6 | Nuevo pedido | Cliente, pasarela (solo habilitadas), contacto, líneas de productos, total estimado | RF1, RF9 |
| 7 | Detalle de pedido | Detalle, acciones Ejecutar/Encolar, puntos de restauración, historial | RF7, RF8 |
| 8 | Cola de comandos | Pendientes FIFO, Ejecutar cola, Vaciar | RF7 |
| 9 | Reportes (Gerente) | KPI financieros, barras por pasarela y categoría, top productos, auditoría | RF3, RF4 |
| 10 | Reportes (Vendedor) | Acceso restringido + resumen sin montos | RF3, RF4 |
| 11 | Notificaciones | Alertas por rol, marcar leída | RF5 |
| 12 | Configuración | Habilitar/deshabilitar pasarelas, elegir estrategia y parámetros | RF2, RF10 |

## 5. Interacciones (modo Prototype)

Usar **On click → Navigate to**, transición *Instant* o *Smart animate*:

| Desde | Elemento | Hacia |
|---|---|---|
| Login | Ingresar | Inicio |
| Sidebar (en todos los frames) | Cada ítem | Frame correspondiente |
| Sidebar | Cerrar sesión | Login |
| Inicio | KPI “Productos bajo mínimo” | Inventario |
| Inicio / Pedidos | + Nuevo pedido | Nuevo pedido |
| Nuevo pedido | Crear pedido | Detalle de pedido |
| Pedidos | Ver detalle | Detalle de pedido |
| Detalle | Encolar | Cola de comandos |
| Inventario | − Salida | Notificaciones (muestra la alerta generada) |
| Reportes (Gerente) | “Ver cómo lo ve un vendedor” | Reportes (Vendedor) |

**Flujos sugeridos (Flow starting points):**

1. *Venta completa:* Login → Inicio → Nuevo pedido → Detalle → Cola de comandos.
2. *Alerta de stock:* Login → Inventario → Notificaciones.
3. *Acceso a reportes:* Login → Reportes (Gerente) → Reportes (Vendedor).
4. *Configuración:* Login → Configuración → Nuevo pedido.

## 6. Compartir

*Share* → **Anyone with the link · can view** → copiar el enlace del modo *Present* (prototipo) y el del archivo, y pegarlos en el documento de sustentación y en este README.
