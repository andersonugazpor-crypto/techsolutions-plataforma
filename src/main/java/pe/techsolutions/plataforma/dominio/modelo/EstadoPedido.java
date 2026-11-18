package pe.techsolutions.plataforma.dominio.modelo;

import java.math.BigDecimal;

/**
 * Patrón State: comportamiento del pedido según su estado interno.
 * El Pedido delega cada operación en su estado actual, de modo que
 * el mismo mensaje (procesar, cancelar, aplicar descuento) produce un
 * resultado distinto sin usar condicionales en el Pedido.
 */
public interface EstadoPedido {

    void procesar(Pedido pedido, String codigoTransaccion);

    void cancelar(Pedido pedido);

    void aplicarDescuento(Pedido pedido, BigDecimal porcentaje);

    /** Indica si en este estado las unidades del pedido están descontadas del inventario. */
    boolean consumeStock();

    boolean puedeProcesar();

    boolean puedeCancelar();

    boolean puedeAplicarDescuento();
}
