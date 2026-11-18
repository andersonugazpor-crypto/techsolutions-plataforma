package pe.techsolutions.plataforma.dominio.modelo;

import java.math.BigDecimal;

/** Pedido cancelado: no admite operaciones (solo puede revertirse con un memento). */
final class EstadoCancelado implements EstadoPedido {

    @Override
    public void procesar(Pedido pedido, String codigoTransaccion) {
        throw new OperacionNoPermitidaException("No se puede procesar un pedido cancelado");
    }

    @Override
    public void cancelar(Pedido pedido) {
        throw new OperacionNoPermitidaException("El pedido #" + pedido.getId() + " ya está cancelado");
    }

    @Override
    public void aplicarDescuento(Pedido pedido, BigDecimal porcentaje) {
        throw new OperacionNoPermitidaException("No se puede aplicar descuento a un pedido cancelado");
    }

    @Override public boolean consumeStock() { return false; }
    @Override public boolean puedeProcesar() { return false; }
    @Override public boolean puedeCancelar() { return false; }
    @Override public boolean puedeAplicarDescuento() { return false; }
}
