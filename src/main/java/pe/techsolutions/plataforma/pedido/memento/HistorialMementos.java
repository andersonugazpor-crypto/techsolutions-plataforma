package pe.techsolutions.plataforma.pedido.memento;

import pe.techsolutions.plataforma.dominio.modelo.OperacionNoPermitidaException;
import pe.techsolutions.plataforma.dominio.modelo.PedidoMemento;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Patrón Memento — cuidador (Caretaker).
 * Guarda las instantáneas de cada pedido en orden cronológico sin conocer ni
 * alterar su contenido. Restaurar a un punto descarta ese punto y los posteriores.
 */
public class HistorialMementos {

    private final int maximoPorPedido;
    private final Map<Long, List<PedidoMemento>> historial = new ConcurrentHashMap<>();

    public HistorialMementos(int maximoPorPedido) {
        this.maximoPorPedido = maximoPorPedido;
    }

    public void guardar(Long pedidoId, PedidoMemento memento) {
        List<PedidoMemento> lista = historial.computeIfAbsent(pedidoId, id -> new ArrayList<>());
        synchronized (lista) {
            lista.add(memento);
            if (lista.size() > maximoPorPedido) {
                lista.remove(0);
            }
        }
    }

    /** Elimina el último memento (se usa cuando el comando que lo originó falló). */
    public void descartarUltimo(Long pedidoId) {
        List<PedidoMemento> lista = historial.get(pedidoId);
        if (lista == null) {
            return;
        }
        synchronized (lista) {
            if (!lista.isEmpty()) {
                lista.remove(lista.size() - 1);
            }
        }
    }

    /** Puntos de restauración del pedido, del más antiguo (índice 0) al más reciente. */
    public List<PedidoMemento> listar(Long pedidoId) {
        List<PedidoMemento> lista = historial.get(pedidoId);
        if (lista == null) {
            return List.of();
        }
        synchronized (lista) {
            return List.copyOf(lista);
        }
    }

    /** Devuelve el memento del índice indicado y quita del historial ese punto y los posteriores. */
    public PedidoMemento extraerParaRestaurar(Long pedidoId, int indice) {
        List<PedidoMemento> lista = historial.get(pedidoId);
        if (lista == null) {
            throw new OperacionNoPermitidaException("El pedido #" + pedidoId + " no tiene puntos de restauración");
        }
        synchronized (lista) {
            if (indice < 0 || indice >= lista.size()) {
                throw new OperacionNoPermitidaException("Punto de restauración inexistente");
            }
            PedidoMemento memento = lista.get(indice);
            lista.subList(indice, lista.size()).clear();
            return memento;
        }
    }

    /** Vuelve a colocar un memento extraído si la restauración no pudo completarse. */
    public void reinsertar(Long pedidoId, PedidoMemento memento) {
        guardar(pedidoId, memento);
    }
}
