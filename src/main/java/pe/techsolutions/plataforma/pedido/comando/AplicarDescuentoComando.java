package pe.techsolutions.plataforma.pedido.comando;

import pe.techsolutions.plataforma.pedido.ReceptorPedidos;

import java.math.BigDecimal;

public class AplicarDescuentoComando implements ComandoPedido {

    private final ReceptorPedidos receptor;
    private final Long pedidoId;
    private final BigDecimal porcentaje;

    public AplicarDescuentoComando(ReceptorPedidos receptor, Long pedidoId, BigDecimal porcentaje) {
        this.receptor = receptor;
        this.pedidoId = pedidoId;
        this.porcentaje = porcentaje;
    }

    @Override public String nombre() { return "DESCUENTO"; }

    @Override
    public String descripcion() {
        return "Aplicar " + (porcentaje == null ? "?" : porcentaje.stripTrailingZeros().toPlainString()) + "% de descuento al pedido #" + pedidoId;
    }

    @Override public Long pedidoId() { return pedidoId; }
    @Override public boolean requiereMemento() { return true; }

    @Override
    public ResultadoComando ejecutar() {
        receptor.aplicarDescuento(pedidoId, porcentaje);
        return ResultadoComando.ok("Descuento aplicado al pedido #" + pedidoId, pedidoId);
    }
}
