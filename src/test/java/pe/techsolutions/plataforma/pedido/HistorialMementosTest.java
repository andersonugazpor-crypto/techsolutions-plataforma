package pe.techsolutions.plataforma.pedido;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.techsolutions.plataforma.dominio.modelo.OperacionNoPermitidaException;
import pe.techsolutions.plataforma.dominio.modelo.Pedido;
import pe.techsolutions.plataforma.pedido.memento.HistorialMementos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Memento: el cuidador conserva un historial acotado")
class HistorialMementosTest {

    @Test
    @DisplayName("Mantiene como máximo N puntos por pedido, descartando los más antiguos")
    void limiteDeHistorial() {
        HistorialMementos historial = new HistorialMementos(3);
        Pedido pedido = new Pedido("Cliente", "YAPE", "987654321");
        pedido.asignarId(7L);
        for (int i = 1; i <= 5; i++) {
            historial.guardar(7L, pedido.crearMemento("Punto " + i));
        }
        assertEquals(3, historial.listar(7L).size());
        assertEquals("Punto 3", historial.listar(7L).get(0).getEtiqueta());
    }

    @Test
    @DisplayName("Pedido sin historial no puede restaurarse")
    void sinHistorial() {
        assertThrows(OperacionNoPermitidaException.class, () -> new HistorialMementos(5).extraerParaRestaurar(1L, 0));
    }
}
