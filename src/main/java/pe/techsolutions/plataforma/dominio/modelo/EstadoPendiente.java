package pe.techsolutions.plataforma.dominio.modelo;

import java.math.BigDecimal;

/** Pedido recién creado: admite procesar, cancelar y aplicar descuento. */
final class EstadoPendiente implements EstadoPedido {

    @Override
    public void procesar(Pedido pedido, String codigoTransaccion) {
        pedido.fijarTransaccion(codigoTransaccion);
        pedido.cambiarEstado(EstadoPedidoTipo.PROCESADO);
    }

    @Override
    public void cancelar(Pedido pedido) {
        pedido.cambiarEstado(EstadoPedidoTipo.CANCELADO);
    }

    @Override
    public void aplicarDescuento(Pedido pedido, BigDecimal porcentaje) {
        pedido.fijarDescuento(porcentaje);
    }

    @Override public boolean consumeStock() { return false; }
    @Override public boolean puedeProcesar() { return true; }
    @Override public boolean puedeCancelar() { return true; }
    @Override public boolean puedeAplicarDescuento() { return true; }
}
