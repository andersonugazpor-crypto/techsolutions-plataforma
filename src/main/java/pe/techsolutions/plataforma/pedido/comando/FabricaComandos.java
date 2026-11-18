package pe.techsolutions.plataforma.pedido.comando;

import pe.techsolutions.plataforma.pedido.NuevoPedido;
import pe.techsolutions.plataforma.pedido.ReceptorPedidos;
import pe.techsolutions.plataforma.pedido.memento.HistorialMementos;

import java.math.BigDecimal;

/**
 * GRASP Creador: construye los comandos concretos inyectándoles su receptor,
 * para que la capa web no dependa de las clases concretas ni del receptor.
 */
public class FabricaComandos {

    private final ReceptorPedidos receptor;
    private final HistorialMementos historialMementos;

    public FabricaComandos(ReceptorPedidos receptor, HistorialMementos historialMementos) {
        this.receptor = receptor;
        this.historialMementos = historialMementos;
    }

    public ComandoPedido crear(NuevoPedido datos) {
        return new CrearPedidoComando(receptor, datos);
    }

    public ComandoPedido procesar(Long pedidoId) {
        return new ProcesarPedidoComando(receptor, pedidoId);
    }

    public ComandoPedido aplicarDescuento(Long pedidoId, BigDecimal porcentaje) {
        return new AplicarDescuentoComando(receptor, pedidoId, porcentaje);
    }

    public ComandoPedido cancelar(Long pedidoId) {
        return new CancelarPedidoComando(receptor, pedidoId);
    }

    public ComandoPedido restaurar(Long pedidoId, int indice) {
        return new RestaurarPedidoComando(receptor, historialMementos, pedidoId, indice);
    }
}
