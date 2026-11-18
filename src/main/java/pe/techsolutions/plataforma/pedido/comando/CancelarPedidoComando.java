package pe.techsolutions.plataforma.pedido.comando;

import pe.techsolutions.plataforma.pedido.ReceptorPedidos;

public class CancelarPedidoComando implements ComandoPedido {

    private final ReceptorPedidos receptor;
    private final Long pedidoId;

    public CancelarPedidoComando(ReceptorPedidos receptor, Long pedidoId) {
        this.receptor = receptor;
        this.pedidoId = pedidoId;
    }

    @Override public String nombre() { return "CANCELAR"; }
    @Override public String descripcion() { return "Cancelar el pedido #" + pedidoId; }
    @Override public Long pedidoId() { return pedidoId; }
    @Override public boolean requiereMemento() { return true; }

    @Override
    public ResultadoComando ejecutar() {
        receptor.cancelar(pedidoId);
        return ResultadoComando.ok("Pedido #" + pedidoId + " cancelado", pedidoId);
    }
}
