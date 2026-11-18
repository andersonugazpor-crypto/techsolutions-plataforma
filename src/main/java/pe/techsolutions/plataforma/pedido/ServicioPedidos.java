package pe.techsolutions.plataforma.pedido;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.techsolutions.plataforma.dominio.modelo.EstadoPedidoTipo;
import pe.techsolutions.plataforma.dominio.modelo.ItemPedido;
import pe.techsolutions.plataforma.dominio.modelo.OperacionNoPermitidaException;
import pe.techsolutions.plataforma.dominio.modelo.Pedido;
import pe.techsolutions.plataforma.dominio.modelo.PedidoMemento;
import pe.techsolutions.plataforma.dominio.modelo.Producto;
import pe.techsolutions.plataforma.inventario.ServicioInventario;
import pe.techsolutions.plataforma.pago.ResultadoPago;
import pe.techsolutions.plataforma.pago.ServicioPagos;
import pe.techsolutions.plataforma.pago.SolicitudPago;
import pe.techsolutions.plataforma.precio.CalculadoraPrecios;
import pe.techsolutions.plataforma.repositorio.PedidoRepositorio;
import pe.techsolutions.plataforma.repositorio.ProductoRepositorio;

import java.math.BigDecimal;
import java.util.List;

/**
 * Receptor concreto de los comandos de pedido.
 * Coordina colaboradores (pagos, inventario, precios) sin contener sus reglas:
 * las reglas de transición están en los objetos State del Pedido.
 */
@Service
@Transactional
public class ServicioPedidos implements ReceptorPedidos {

    private final PedidoRepositorio pedidos;
    private final ProductoRepositorio productos;
    private final ServicioInventario inventario;
    private final ServicioPagos pagos;
    private final CalculadoraPrecios calculadora;

    public ServicioPedidos(PedidoRepositorio pedidos, ProductoRepositorio productos, ServicioInventario inventario,
                           ServicioPagos pagos, CalculadoraPrecios calculadora) {
        this.pedidos = pedidos;
        this.productos = productos;
        this.inventario = inventario;
        this.pagos = pagos;
        this.calculadora = calculadora;
    }

    @Override
    public Pedido crear(NuevoPedido datos) {
        pagos.validarDisponible(datos.metodoPago());
        List<LineaPedido> lineas = datos.lineas().stream().filter(l -> l.productoId() != null && l.cantidad() > 0).toList();
        if (lineas.isEmpty()) {
            throw new OperacionNoPermitidaException("El pedido debe tener al menos un producto con cantidad mayor que cero");
        }
        Pedido pedido = new Pedido(datos.cliente(), datos.metodoPago(), datos.contactoPago());
        for (LineaPedido linea : lineas) {
            Producto producto = productos.findById(linea.productoId())
                    .orElseThrow(() -> new OperacionNoPermitidaException("Producto no encontrado: " + linea.productoId()));
            pedido.agregarItem(producto, linea.cantidad(), calculadora.precioDe(producto));
        }
        return pedidos.save(pedido);
    }

    @Override
    public void procesar(Long pedidoId) {
        Pedido pedido = buscar(pedidoId);
        if (!pedido.comportamiento().puedeProcesar()) {
            pedido.procesar(null); // el estado lanza el mensaje de negocio adecuado
        }
        pedido.getItems().stream()
                .filter(i -> !i.getProducto().tieneDisponible(i.getCantidad()))
                .findFirst()
                .ifPresent(i -> {
                    throw new OperacionNoPermitidaException("Stock insuficiente de " + i.getProducto().getNombre()
                            + " (disponible " + i.getProducto().getStock() + ")");
                });
        ResultadoPago pago = pagos.cobrar(pedido.getMetodoPago(),
                new SolicitudPago("PED-" + pedido.getId(), pedido.getTotal(), pedido.getCliente(), pedido.getContactoPago()));
        if (!pago.aprobado()) {
            throw new OperacionNoPermitidaException(pago.mensaje());
        }
        boolean consumiaAntes = pedido.comportamiento().consumeStock();
        pedido.procesar(pago.codigoTransaccion());
        sincronizarStock(pedido, consumiaAntes);
        pedidos.save(pedido);
    }

    @Override
    public void aplicarDescuento(Long pedidoId, BigDecimal porcentaje) {
        Pedido pedido = buscar(pedidoId);
        pedido.aplicarDescuento(porcentaje);
        pedidos.save(pedido);
    }

    @Override
    public void cancelar(Long pedidoId) {
        Pedido pedido = buscar(pedidoId);
        boolean consumiaAntes = pedido.comportamiento().consumeStock();
        pedido.cancelar();
        sincronizarStock(pedido, consumiaAntes);
        pedidos.save(pedido);
    }

    @Override
    public void restaurar(Long pedidoId, PedidoMemento memento) {
        Pedido pedido = buscar(pedidoId);
        boolean consumiaAntes = pedido.comportamiento().consumeStock();
        pedido.restaurar(memento);
        sincronizarStock(pedido, consumiaAntes);
        pedidos.save(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoMemento capturar(Long pedidoId, String etiqueta) {
        return buscar(pedidoId).crearMemento(etiqueta);
    }

    @Transactional(readOnly = true)
    public Pedido buscar(Long pedidoId) {
        return pedidos.findById(pedidoId)
                .orElseThrow(() -> new OperacionNoPermitidaException("Pedido no encontrado: " + pedidoId));
    }

    @Transactional(readOnly = true)
    public long contarPorEstado(EstadoPedidoTipo estado) {
        return pedidos.countByEstado(estado);
    }

    @Transactional(readOnly = true)
    public List<Pedido> listar() {
        return pedidos.findAllByOrderByIdDesc();
    }

    /**
     * Mantiene el inventario coherente con el estado del pedido: si el nuevo estado
     * consume stock y el anterior no, se descuenta; en el caso inverso, se devuelve.
     */
    private void sincronizarStock(Pedido pedido, boolean consumiaAntes) {
        boolean consumeAhora = pedido.comportamiento().consumeStock();
        if (consumeAhora == consumiaAntes) {
            return;
        }
        for (ItemPedido item : pedido.getItems()) {
            if (consumeAhora) {
                inventario.descontarVenta(item.getProducto().getId(), item.getCantidad());
            } else {
                inventario.devolverVenta(item.getProducto().getId(), item.getCantidad());
            }
        }
    }
}
