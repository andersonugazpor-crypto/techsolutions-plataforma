# TechSolutions · Plataforma de Gestión Empresarial

Aplicación web para pymes peruanas que integra **ventas, inventario, pagos y reportes financieros**, construida con **Java 17 + Spring Boot 3 + Thymeleaf + Spring Data JPA** y diseñada aplicando patrones de diseño estructurales, de comportamiento y GRASP.

> Evaluación final · Patrones de Diseño de Software · IDAT

| Requerimiento | Patrón | Dónde está |
|---|---|---|
| RF1–RF2 Pasarelas PayPal, Yape, Plin y su habilitación | **Adapter** | `pago/` |
| RF3–RF4 Acceso protegido a reportes financieros | **Proxy** (protección + virtual + caché) | `reporte/` |
| RF5–RF6 Alertas de stock bajo, mínimo configurable | **Observer** | `inventario/` |
| RF7 Acciones de pedido como comandos con historial y cola | **Command** | `pedido/comando/` |
| RF8 Revertir un pedido a un estado anterior | **Memento** | `dominio/modelo/PedidoMemento`, `pedido/memento/` |
| RF9–RF10 Estrategias de precio seleccionables | **Strategy** | `precio/` |
| RF11–RF12 Catálogo paginado y filtrable | **Iterator** | `catalogo/` |
| Comportamiento según el estado del pedido | **State** (adicional) | `dominio/modelo/Estado*` |

---

## 1. Requisitos

| Herramienta | Versión |
|---|---|
| JDK | 17 o superior (probado con 21) |
| Maven | 3.9+ (o el que trae tu IDE) |
| IDE | VS Code (Extension Pack for Java + Spring Boot Extension Pack) o IntelliJ IDEA |
| MySQL (opcional) | 8.x — por defecto se usa H2 en memoria, sin instalar nada |

## 2. Instalación

```bash
git clone https://github.com/<usuario>/techsolutions-plataforma.git
cd techsolutions-plataforma
mvn clean install        # descarga dependencias, compila y ejecuta las pruebas
```

## 3. Ejecución

```bash
mvn spring-boot:run
```

Abrir **http://localhost:8080** e iniciar sesión con uno de los usuarios de demostración:

| Usuario | Contraseña | Rol | Qué puede hacer |
|---|---|---|---|
| `admin` | `admin123` | Administrador | Configurar pasarelas y estrategia de precios, gestionar todo |
| `gerente` | `gerente123` | Gerente | Reporte financiero completo, alertas de stock, pedidos |
| `contador` | `contador123` | Contador | Reporte financiero completo |
| `compras` | `compras123` | Compras | Inventario y alertas de reposición |
| `vendedor` | `vendedor123` | Vendedor | Crear y operar pedidos (sin acceso a reportes completos) |

Al arrancar se cargan 36 productos, 5 usuarios y 4 pedidos de ejemplo. La consola de H2 está en `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:techsolutions`, usuario `sa`, sin contraseña).

**En VS Code:** abrir la carpeta, esperar a que cargue el proyecto Maven y ejecutar `PlataformaApplication` con el botón *Run* (o desde *Spring Boot Dashboard*).

### Con MySQL (opcional)

1. Crear un usuario con permisos o usar `root`; la base `techsolutions` se crea sola.
2. Ajustar usuario y contraseña en `src/main/resources/application-mysql.properties`.
3. Ejecutar:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

## 4. Pruebas

```bash
mvn test
```

| Clase de prueba | Qué verifica |
|---|---|
| `pago/AdaptadoresPagoTest` | Interfaz uniforme de las 3 pasarelas, conversión PEN→USD, códigos de Yape, pasarela deshabilitada, extensión sin modificar código |
| `reporte/ProxyServicioReportesTest` | Acceso solo Gerente/Contador, credenciales inválidas, carga perezosa, caché con TTL, auditoría |
| `inventario/PublicadorStockTest` | Notificación uno-a-muchos al cruzar el mínimo, sin avisos en reposición, desuscripción |
| `pedido/InvocadorComandosTest` | Historial, mementos automáticos, cola FIFO, comando fallido, restauración con ajuste de stock |
| `pedido/HistorialMementosTest` | Límite del historial de mementos |
| `precio/EstrategiasPrecioTest` | Precio estándar, descuento, dinámico (demanda, escasez, temporada) y cambio de estrategia |
| `catalogo/CatalogoProductosTest` | Recorrido perezoso por lotes, paginación, filtros, colección no expuesta |
| `dominio/PedidoTest`, `dominio/ProductoTest` | Transiciones de estado, totales, memento, reglas de stock |
| `web/UtilidadesWebTest` | Conversión del formulario de pedido y barras del reporte |
| `PlataformaIntegracionTest` | Levanta Spring completo: flujo crear→procesar→restaurar, login, Proxy en la web, permisos de admin y render de todas las pantallas |

## 5. Guion de demostración (5 minutos)

1. **Adapter / RF2** — `admin` → *Configuración* → deshabilitar **Plin** → *Nuevo pedido*: Plin ya no aparece.
2. **Strategy / RF10** — en *Configuración* elegir **Descuento porcentual 10 %** → *Catálogo*: precios rebajados con el precio base tachado.
3. **Command** — `vendedor` → crear un pedido de 3 *Atún en trozos* con Yape (celular `987654321`) → en el detalle pulsar **Encolar** en *Aplicar descuento* y en *Procesar* → *Cola de comandos* → **Ejecutar cola**.
4. **Observer / RF5** — el atún baja de 16 a 13 (mínimo 15): entrar como `gerente` o `compras` → *Notificaciones* muestra la alerta.
5. **Memento / RF8** — en el pedido, *Puntos de restauración* → **Restaurar** “Antes de procesar”: vuelve a *Pendiente* y el stock regresa a 16.
6. **Proxy / RF3-RF4** — como `vendedor`, *Reportes* muestra *Acceso restringido*; como `gerente` muestra el reporte; recargar la página: “Servido desde caché”; la tabla de auditoría registra ambos intentos.
7. **Iterator / RF11** — *Catálogo*: buscar “arroz”, filtrar por categoría, cambiar tamaño de página y navegar.

## 6. Estructura del repositorio

```
├── pom.xml
├── README.md
├── docs/
│   ├── uml/                 Diagramas en Mermaid (.mmd) por patrón
│   ├── img/                 Diagramas renderizados (.png) y wireframes
│   ├── prototipo/           Prototipo navegable de baja fidelidad (abrir index.html)
│   ├── GUIA_FIGMA.md        Pantallas, componentes y flujos para armar el prototipo en Figma
│   └── PRUEBAS_USABILIDAD.md  Evaluación heurística y protocolo de pruebas con usuarios
└── src/
    ├── main/java/pe/techsolutions/plataforma/
    │   ├── config/          Ensamblado de patrones (PatronesConfig), web y datos iniciales
    │   ├── dominio/modelo/  Entidades JPA, State del pedido y Memento
    │   ├── repositorio/     Spring Data JPA
    │   ├── pago/            Adapter (+ sdk/ con las APIs externas simuladas)
    │   ├── reporte/         Proxy
    │   ├── inventario/      Observer y notificaciones
    │   ├── pedido/          Receptor, comando/ (Command) y memento/ (cuidador)
    │   ├── precio/          Strategy
    │   ├── catalogo/        Iterator
    │   ├── seguridad/       Sesión, credenciales e interceptor
    │   └── web/             Controladores MVC (GRASP Controlador)
    ├── main/resources/      application*.properties, templates/ (Thymeleaf), static/css
    └── test/java/           Pruebas unitarias por patrón + integración
```

## 7. Decisiones de diseño (GRASP)

- **Controlador:** los `*Controller` solo traducen eventos HTTP a llamadas de servicio o comandos; `PedidoController` entrega todo al `InvocadorComandos`.
- **Experto en información:** `Pedido` calcula sus totales; `Producto` decide si está bajo el mínimo y valida sus movimientos.
- **Creador:** `Pedido` crea sus `ItemPedido`; `FabricaComandos` crea los comandos con su receptor.
- **Alta cohesión / bajo acoplamiento:** cada paquete agrupa un patrón; las clases de patrones son Java puro y se conectan en `PatronesConfig`.
- **Polimorfismo:** estrategias de precio, estados del pedido, pasarelas y comandos se eligen por tipo, sin `if`/`switch` sobre códigos.
- **Fabricación pura:** `RegistroPasarelas`, `InvocadorComandos`, `HistorialMementos`, `PublicadorStock`.
- **Indirección y variaciones protegidas:** `FuenteProductos`, `FuenteDatosReporte`, `EstadoPasarelas`, `ConfiguracionPrecios` y `BandejaNotificaciones` aíslan la lógica de JPA; `PasarelaPago` aísla las APIs externas. Para agregar una pasarela nueva basta con registrar un adaptador (`@Bean`): el resto del sistema no cambia.

## 8. Notas

- Las pasarelas son **simulaciones** de las APIs externas (no realizan cobros reales). Reglas simuladas: PayPal exige correo válido; Yape y Plin exigen celular de 9 dígitos que empiece en 9; Yape rechaza montos mayores a S/ 2 000.
- Los mementos se guardan en memoria (se pierden al reiniciar); el historial de comandos sí se persiste en la base de datos.
- Con H2 en memoria los datos se reinician en cada arranque.
