package pe.techsolutions.plataforma.pedido.comando;

import pe.techsolutions.plataforma.dominio.modelo.PedidoMemento;
import pe.techsolutions.plataforma.pedido.ReceptorPedidos;
import pe.techsolutions.plataforma.pedido.memento.HistorialMementos;

/** RF8: revierte el pedido al estado guardado en un memento. */
public class RestaurarPedidoComando implements ComandoPedido {

    private final ReceptorPedidos receptor;
    private final HistorialMementos historial;
    private final Long pedidoId;
    private final int indice;

    public RestaurarPedidoComando(ReceptorPedidos receptor, HistorialMementos historial, Long pedidoId, int indice) {
        this.receptor = receptor;
        this.historial = historial;
        this.pedidoId = pedidoId;
        this.indice = indice;
    }

    @Override public String nombre() { return "RESTAURAR"; }
    @Override public String descripcion() { return "Restaurar el pedido #" + pedidoId + " al punto " + (indice + 1); }
    @Override public Long pedidoId() { return pedidoId; }
    @Override public boolean requiereMemento() { return false; }

    @Override
    public ResultadoComando ejecutar() {
        PedidoMemento memento = historial.extraerParaRestaurar(pedidoId, indice);
        try {
            receptor.restaurar(pedidoId, memento);
        } catch (RuntimeException e) {
            historial.reinsertar(pedidoId, memento);
            throw e;
        }
        return ResultadoComando.ok("Pedido #" + pedidoId + " restaurado: " + memento.getEtiqueta()
                + " (" + memento.getResumen() + ")", pedidoId);
    }
}
