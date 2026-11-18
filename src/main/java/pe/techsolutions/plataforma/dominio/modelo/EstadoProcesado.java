package pe.techsolutions.plataforma.dominio.modelo;

import java.math.BigDecimal;

/** Pedido pagado y despachado: solo puede cancelarse (se devuelve el stock). */
final class EstadoProcesado implements EstadoPedido {

    @Override
    public void procesar(Pedido pedido, String codigoTransaccion) {
        throw new OperacionNoPermitidaException("El pedido #" + pedido.getId() + " ya fue procesado");
    }

    @Override
    public void cancelar(Pedido pedido) {
        pedido.cambiarEstado(EstadoPedidoTipo.CANCELADO);
    }

    @Override
    public void aplicarDescuento(Pedido pedido, BigDecimal porcentaje) {
        throw new OperacionNoPermitidaException("No se puede aplicar descuento a un pedido ya procesado");
    }

    @Override public boolean consumeStock() { return true; }
    @Override public boolean puedeProcesar() { return false; }
    @Override public boolean puedeCancelar() { return true; }
    @Override public boolean puedeAplicarDescuento() { return false; }
}
