package pe.techsolutions.plataforma.inventario;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.techsolutions.plataforma.dominio.modelo.OperacionNoPermitidaException;
import pe.techsolutions.plataforma.dominio.modelo.Producto;
import pe.techsolutions.plataforma.repositorio.ProductoRepositorio;

import java.util.function.Consumer;

/**
 * Casos de uso de inventario. Cada movimiento se registra en el producto (experto)
 * y luego se informa al sujeto {@link PublicadorStock}, que notifica a los observadores.
 */
@Service
@Transactional
public class ServicioInventario {

    private final ProductoRepositorio productos;
    private final PublicadorStock publicador;

    public ServicioInventario(ProductoRepositorio productos, PublicadorStock publicador) {
        this.productos = productos;
        this.publicador = publicador;
    }

    public void descontarVenta(Long productoId, int cantidad) {
        modificar(productoId, p -> p.descontarStock(cantidad));
    }

    public void devolverVenta(Long productoId, int cantidad) {
        modificar(productoId, p -> p.devolverVenta(cantidad));
    }

    public void registrarEntrada(Long productoId, int cantidad) {
        modificar(productoId, p -> p.reponerStock(cantidad));
    }

    public void registrarSalida(Long productoId, int cantidad) {
        modificar(productoId, p -> p.registrarSalida(cantidad));
    }

    /** RF6: el nivel mínimo es configurable por producto. */
    public void configurarStockMinimo(Long productoId, int minimo) {
        modificar(productoId, p -> p.configurarStockMinimo(minimo));
    }

    @Transactional(readOnly = true)
    public Producto buscar(Long productoId) {
        return productos.findById(productoId)
                .orElseThrow(() -> new OperacionNoPermitidaException("Producto no encontrado: " + productoId));
    }

    @Transactional(readOnly = true)
    public long contarBajoMinimo() {
        return productos.contarBajoMinimo();
    }

    private void modificar(Long productoId, Consumer<Producto> operacion) {
        Producto producto = buscar(productoId);
        int stockAnterior = producto.getStock();
        int minimoAnterior = producto.getStockMinimo();
        operacion.accept(producto);
        productos.save(producto);
        publicador.evaluarCambio(producto, stockAnterior, minimoAnterior);
    }
}
