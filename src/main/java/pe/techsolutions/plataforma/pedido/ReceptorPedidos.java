package pe.techsolutions.plataforma.pedido;

import pe.techsolutions.plataforma.dominio.modelo.Pedido;
import pe.techsolutions.plataforma.dominio.modelo.PedidoMemento;

import java.math.BigDecimal;

/**
 * Patrón Command — receptor (Receiver). Sabe realizar las operaciones reales
 * sobre los pedidos; los comandos solo le delegan.
 */
public interface ReceptorPedidos extends OrigenMementos {

    Pedido crear(NuevoPedido datos);

    void procesar(Long pedidoId);

    void aplicarDescuento(Long pedidoId, BigDecimal porcentaje);

    void cancelar(Long pedidoId);

    void restaurar(Long pedidoId, PedidoMemento memento);
}
