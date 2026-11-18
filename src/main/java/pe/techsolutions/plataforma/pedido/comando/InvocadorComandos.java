package pe.techsolutions.plataforma.pedido.comando;

import pe.techsolutions.plataforma.pedido.OrigenMementos;
import pe.techsolutions.plataforma.pedido.memento.HistorialMementos;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Patrón Command — invocador (Invoker). GRASP Fabricación pura.
 * Ejecuta comandos sin saber qué hacen: antes guarda un memento (si el comando
 * lo requiere), después registra el resultado en el historial. También permite
 * encolar solicitudes y procesarlas luego en orden FIFO.
 */
public class InvocadorComandos {

    private final OrigenMementos origenMementos;
    private final HistorialMementos historialMementos;
    private final RegistroHistorial registroHistorial;
    private final Clock reloj;
    private final Queue<ComandoEncolado> cola = new ConcurrentLinkedQueue<>();

    public InvocadorComandos(OrigenMementos origenMementos, HistorialMementos historialMementos,
                             RegistroHistorial registroHistorial, Clock reloj) {
        this.origenMementos = origenMementos;
        this.historialMementos = historialMementos;
        this.registroHistorial = registroHistorial;
        this.reloj = reloj;
    }

    public ResultadoComando ejecutar(ComandoPedido comando, String usuario) {
        boolean mementoGuardado = false;
        ResultadoComando resultado;
        try {
            if (comando.requiereMemento()) {
                historialMementos.guardar(comando.pedidoId(),
                        origenMementos.capturar(comando.pedidoId(), "Antes de " + comando.nombre().toLowerCase()));
                mementoGuardado = true;
            }
            resultado = comando.ejecutar();
        } catch (RuntimeException e) {
            if (mementoGuardado) {
                historialMementos.descartarUltimo(comando.pedidoId());
            }
            resultado = ResultadoComando.fallo(e.getMessage(), comando.pedidoId());
        }
        registroHistorial.registrar(comando, usuario, resultado);
        return resultado;
    }

    public void encolar(ComandoPedido comando, String usuario) {
        cola.add(new ComandoEncolado(comando, usuario, LocalDateTime.now(reloj)));
    }

    /** Ejecuta todos los comandos pendientes en el orden en que se encolaron. */
    public List<ResultadoComando> procesarCola() {
        List<ResultadoComando> resultados = new ArrayList<>();
        ComandoEncolado siguiente;
        while ((siguiente = cola.poll()) != null) {
            resultados.add(ejecutar(siguiente.comando(), siguiente.usuario()));
        }
        return resultados;
    }

    public List<ComandoEncolado> pendientes() {
        return List.copyOf(cola);
    }

    public void vaciarCola() {
        cola.clear();
    }
}
