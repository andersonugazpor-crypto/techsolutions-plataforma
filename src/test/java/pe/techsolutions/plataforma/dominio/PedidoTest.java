package pe.techsolutions.plataforma.dominio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.techsolutions.plataforma.dominio.modelo.EstadoPedidoTipo;
import pe.techsolutions.plataforma.dominio.modelo.OperacionNoPermitidaException;
import pe.techsolutions.plataforma.dominio.modelo.Pedido;
import pe.techsolutions.plataforma.dominio.modelo.PedidoMemento;
import pe.techsolutions.plataforma.soporte.Datos;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Pedido: patrón State, Memento y cálculo de totales")
class PedidoTest {

    private Pedido pedido;

    @BeforeEach
    void preparar() {
        pedido = new Pedido("Bodega Don Pepe", "YAPE", "987654321");
        pedido.asignarId(1L);
        pedido.agregarItem(Datos.producto(1, "Arroz", "24.90", 50, 10), 2, new BigDecimal("24.90"));
        pedido.agregarItem(Datos.producto(2, "Leche", "4.30", 50, 10), 10, new BigDecimal("4.30"));
    }

    @Test
    @DisplayName("Experto: calcula subtotal, descuento y total")
    void calculaTotales() {
        pedido.aplicarDescuento(new BigDecimal("10"));
        assertEquals("92.80", Datos.dinero(pedido.getSubtotal()));
        assertEquals("9.28", Datos.dinero(pedido.getMontoDescuento()));
        assertEquals("83.52", Datos.dinero(pedido.getTotal()));
        assertEquals(12, pedido.getTotalUnidades());
    }

    @Test
    @DisplayName("State: un pedido pendiente se procesa y luego ya no admite descuento")
    void pendienteAProcesado() {
        assertTrue(pedido.comportamiento().puedeProcesar());
        pedido.procesar("YAPE-1");
        assertEquals(EstadoPedidoTipo.PROCESADO, pedido.getEstado());
        assertEquals("YAPE-1", pedido.getCodigoTransaccion());
        assertTrue(pedido.comportamiento().consumeStock());
        assertThrows(OperacionNoPermitidaException.class, () -> pedido.aplicarDescuento(BigDecimal.ONE));
        assertThrows(OperacionNoPermitidaException.class, () -> pedido.procesar("X"));
    }

    @Test
    @DisplayName("State: un pedido cancelado no admite ninguna operación")
    void canceladoBloqueaOperaciones() {
        pedido.cancelar();
        assertEquals(EstadoPedidoTipo.CANCELADO, pedido.getEstado());
        assertFalse(pedido.comportamiento().puedeProcesar());
        assertFalse(pedido.comportamiento().puedeCancelar());
        assertThrows(OperacionNoPermitidaException.class, () -> pedido.procesar("X"));
        assertThrows(OperacionNoPermitidaException.class, () -> pedido.cancelar());
    }

    @Test
    @DisplayName("El descuento debe estar entre 0 y 50 %")
    void validaRangoDescuento() {
        assertThrows(OperacionNoPermitidaException.class, () -> pedido.aplicarDescuento(new BigDecimal("60")));
        assertThrows(OperacionNoPermitidaException.class, () -> pedido.aplicarDescuento(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Memento: restaura estado, descuento y transacción anteriores")
    void restauraMemento() {
        PedidoMemento antesDeDescuento = pedido.crearMemento("Antes de descuento");
        pedido.aplicarDescuento(new BigDecimal("15"));
        PedidoMemento antesDeProcesar = pedido.crearMemento("Antes de procesar");
        pedido.procesar("TX-9");

        pedido.restaurar(antesDeProcesar);
        assertEquals(EstadoPedidoTipo.PENDIENTE, pedido.getEstado());
        assertEquals("15", pedido.getPorcentajeDescuento().stripTrailingZeros().toPlainString());
        assertEquals(null, pedido.getCodigoTransaccion());

        pedido.restaurar(antesDeDescuento);
        assertEquals(0, pedido.getPorcentajeDescuento().signum());
        assertEquals("Estado Pendiente, descuento 0%", antesDeDescuento.getResumen());
    }

    @Test
    @DisplayName("Memento: no se puede restaurar el memento de otro pedido")
    void rechazaMementoAjeno() {
        Pedido otro = new Pedido("Otro cliente", "PLIN", "912345678");
        otro.asignarId(99L);
        PedidoMemento ajeno = otro.crearMemento("ajeno");
        assertThrows(OperacionNoPermitidaException.class, () -> pedido.restaurar(ajeno));
    }

    @Test
    @DisplayName("Creador: solo se agregan ítems a pedidos pendientes")
    void soloAgregaItemsEnPendiente() {
        pedido.cancelar();
        assertThrows(OperacionNoPermitidaException.class,
                () -> pedido.agregarItem(Datos.producto(3, "Café", "18.90", 5, 1), 1, new BigDecimal("18.90")));
    }
}
