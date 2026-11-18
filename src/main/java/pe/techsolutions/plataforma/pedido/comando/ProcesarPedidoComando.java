package pe.techsolutions.plataforma.pedido.comando;

import pe.techsolutions.plataforma.pedido.ReceptorPedidos;

/** Cobra el pedido con su pasarela y descuenta el stock. */
public class ProcesarPedidoComando implements ComandoPedido {

    private final ReceptorPedidos receptor;
    private final Long pedidoId;

    public ProcesarPedidoComando(ReceptorPedidos receptor, Long pedidoId) {
        this.receptor = receptor;
        this.pedidoId = pedidoId;
    }

    @Override public String nombre() { return "PROCESAR"; }
    @Override public String descripcion() { return "Procesar (cobrar y despachar) el pedido #" + pedidoId; }
    @Override public Long pedidoId() { return pedidoId; }
    @Override public boolean requiereMemento() { return true; }

    @Override
    public ResultadoComando ejecutar() {
        receptor.procesar(pedidoId);
        return ResultadoComando.ok("Pedido #" + pedidoId + " procesado y cobrado", pedidoId);
    }
}
