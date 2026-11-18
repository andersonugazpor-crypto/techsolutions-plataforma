package pe.techsolutions.plataforma.pedido;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.techsolutions.plataforma.dominio.modelo.EstadoPedidoTipo;
import pe.techsolutions.plataforma.dominio.modelo.OperacionNoPermitidaException;
import pe.techsolutions.plataforma.dominio.modelo.Pedido;
import pe.techsolutions.plataforma.dominio.modelo.PedidoMemento;
import pe.techsolutions.plataforma.dominio.modelo.Producto;
import pe.techsolutions.plataforma.pedido.comando.ComandoPedido;
import pe.techsolutions.plataforma.pedido.comando.FabricaComandos;
import pe.techsolutions.plataforma.pedido.comando.InvocadorComandos;
import pe.techsolutions.plataforma.pedido.comando.RegistroHistorial;
import pe.techsolutions.plataforma.pedido.comando.ResultadoComando;
import pe.techsolutions.plataforma.pedido.memento.HistorialMementos;
import pe.techsolutions.plataforma.soporte.Datos;
import pe.techsolutions.plataforma.soporte.RelojAjustable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Command + Memento: ejecución, cola, historial y restauración de pedidos")
class InvocadorComandosTest {

    /** Receptor en memoria: mismo contrato que ServicioPedidos, sin base de datos ni pagos reales. */
    static class ReceptorEnMemoria implements ReceptorPedidos {
        final Map<Long, Pedido> pedidos = new HashMap<>();
        final Map<Long, Producto> productos = new HashMap<>();
        long secuencia = 0;

        @Override
        public Pedido crear(NuevoPedido datos) {
            Pedido pedido = new Pedido(datos.cliente(), datos.metodoPago(), datos.contactoPago());
            pedido.asignarId(++secuencia);
            datos.lineas().forEach(l -> {
                Producto p = productos.get(l.productoId());
                pedido.agregarItem(p, l.cantidad(), p.getPrecioBase());
            });
            pedidos.put(pedido.getId(), pedido);
            return pedido;
        }

        @Override
        public void procesar(Long id) {
            Pedido pedido = pedidos.get(id);
            boolean antes = pedido.comportamiento().consumeStock();
            pedido.procesar("TX-" + id);
            sincronizar(pedido, antes);
        }

        @Override
        public void aplicarDescuento(Long id, BigDecimal porcentaje) {
            pedidos.get(id).aplicarDescuento(porcentaje);
        }

        @Override
        public void cancelar(Long id) {
            Pedido pedido = pedidos.get(id);
            boolean antes = pedido.comportamiento().consumeStock();
            pedido.cancelar();
            sincronizar(pedido, antes);
        }

        @Override
        public void restaurar(Long id, PedidoMemento memento) {
            Pedido pedido = pedidos.get(id);
            boolean antes = pedido.comportamiento().consumeStock();
            pedido.restaurar(memento);
            sincronizar(pedido, antes);
        }

        @Override
        public PedidoMemento capturar(Long id, String etiqueta) {
            return pedidos.get(id).crearMemento(etiqueta);
        }

        private void sincronizar(Pedido pedido, boolean antes) {
            boolean ahora = pedido.comportamiento().consumeStock();
            if (antes == ahora) {
                return;
            }
            pedido.getItems().forEach(i -> {
                if (ahora) {
                    i.getProducto().descontarStock(i.getCantidad());
                } else {
                    i.getProducto().devolverVenta(i.getCantidad());
                }
            });
        }
    }

    private ReceptorEnMemoria receptor;
    private HistorialMementos mementos;
    private List<String> historial;
    private InvocadorComandos invocador;
    private FabricaComandos fabrica;
    private Producto arroz;

    @BeforeEach
    void preparar() {
        receptor = new ReceptorEnMemoria();
        arroz = Datos.producto(1, "Arroz", "25.00", 40, 10);
        receptor.productos.put(1L, arroz);
        mementos = new HistorialMementos(20);
        historial = new ArrayList<>();
        RegistroHistorial registro = (comando, usuario, r) ->
                historial.add(comando.nombre() + ":" + (r.exitoso() ? "OK" : "FALLO") + ":" + usuario);
        invocador = new InvocadorComandos(receptor, mementos, registro, new RelojAjustable(Instant.parse("2026-11-18T15:00:00Z")));
        fabrica = new FabricaComandos(receptor, mementos);
    }

    private Long crearPedido(int cantidad) {
        NuevoPedido datos = new NuevoPedido("Bodega", "YAPE", "987654321", List.of(new LineaPedido(1L, cantidad)));
        return invocador.ejecutar(fabrica.crear(datos), "vendedor").pedidoId();
    }

    @Test
    @DisplayName("RF7: cada comando se ejecuta y se registra en el historial")
    void registraHistorial() {
        Long id = crearPedido(4);
        invocador.ejecutar(fabrica.aplicarDescuento(id, BigDecimal.TEN), "vendedor");
        invocador.ejecutar(fabrica.procesar(id), "gerente");

        assertEquals(List.of("CREAR:OK:vendedor", "DESCUENTO:OK:vendedor", "PROCESAR:OK:gerente"), historial);
        assertEquals(EstadoPedidoTipo.PROCESADO, receptor.pedidos.get(id).getEstado());
        assertEquals(36, arroz.getStock());
    }

    @Test
    @DisplayName("El invocador guarda un memento antes de cada comando que modifica el pedido")
    void guardaMementos() {
        Long id = crearPedido(2);
        invocador.ejecutar(fabrica.aplicarDescuento(id, new BigDecimal("5")), "vendedor");
        invocador.ejecutar(fabrica.procesar(id), "vendedor");
        List<PedidoMemento> puntos = mementos.listar(id);
        assertEquals(2, puntos.size());
        assertEquals("Antes de descuento", puntos.get(0).getEtiqueta());
        assertEquals("Antes de procesar", puntos.get(1).getEtiqueta());
    }

    @Test
    @DisplayName("Un comando que falla queda registrado como fallido y no deja memento")
    void comandoFallido() {
        Long id = crearPedido(2);
        invocador.ejecutar(fabrica.cancelar(id), "vendedor");
        ResultadoComando resultado = invocador.ejecutar(fabrica.procesar(id), "vendedor");

        assertFalse(resultado.exitoso());
        assertTrue(resultado.mensaje().contains("cancelado"));
        assertEquals("PROCESAR:FALLO:vendedor", historial.get(historial.size() - 1));
        assertEquals(1, mementos.listar(id).size()); // solo el de "antes de cancelar"
    }

    @Test
    @DisplayName("Cola: los comandos encolados se ejecutan después y en orden FIFO")
    void colaFifo() {
        Long id = crearPedido(5);
        invocador.encolar(fabrica.aplicarDescuento(id, new BigDecimal("10")), "vendedor");
        invocador.encolar(fabrica.procesar(id), "vendedor");
        assertEquals(2, invocador.pendientes().size());
        assertEquals(EstadoPedidoTipo.PENDIENTE, receptor.pedidos.get(id).getEstado()); // aún no se ejecutan

        List<ResultadoComando> resultados = invocador.procesarCola();
        assertEquals(2, resultados.size());
        assertTrue(resultados.get(0).exitoso() && resultados.get(1).exitoso());
        assertEquals(0, invocador.pendientes().size());
        // El orden importa: si se procesara antes del descuento, el descuento fallaría.
        assertEquals("112.50", Datos.dinero(receptor.pedidos.get(id).getTotal()));
    }

    @Test
    @DisplayName("RF8: restaurar un memento revierte el pedido y devuelve el stock")
    void restaurarRevierteStock() {
        Long id = crearPedido(6);
        invocador.ejecutar(fabrica.procesar(id), "vendedor");
        assertEquals(34, arroz.getStock());

        ResultadoComando r = invocador.ejecutar(fabrica.restaurar(id, 0), "gerente");
        assertTrue(r.exitoso());
        assertEquals(EstadoPedidoTipo.PENDIENTE, receptor.pedidos.get(id).getEstado());
        assertEquals(40, arroz.getStock());
        assertTrue(mementos.listar(id).isEmpty()); // se consumió el punto restaurado
    }

    @Test
    @DisplayName("RF8: restaurar desde un pedido cancelado a uno procesado vuelve a descontar el stock")
    void restaurarCancelacion() {
        Long id = crearPedido(3);
        invocador.ejecutar(fabrica.procesar(id), "vendedor");
        invocador.ejecutar(fabrica.cancelar(id), "vendedor");
        assertEquals(40, arroz.getStock());

        invocador.ejecutar(fabrica.restaurar(id, 1), "vendedor"); // "Antes de cancelar"
        assertEquals(EstadoPedidoTipo.PROCESADO, receptor.pedidos.get(id).getEstado());
        assertEquals(37, arroz.getStock());
        assertEquals(1, mementos.listar(id).size());
    }

    @Test
    @DisplayName("Restaurar un punto inexistente falla sin alterar el pedido")
    void restaurarInexistente() {
        Long id = crearPedido(1);
        ResultadoComando r = invocador.ejecutar(fabrica.restaurar(id, 5), "vendedor");
        assertFalse(r.exitoso());
        assertEquals(EstadoPedidoTipo.PENDIENTE, receptor.pedidos.get(id).getEstado());
    }

    @Test
    @DisplayName("Todos los comandos comparten la misma interfaz genérica")
    void interfazGenerica() {
        Long id = crearPedido(1);
        List<ComandoPedido> comandos = List.of(fabrica.aplicarDescuento(id, BigDecimal.ONE), fabrica.procesar(id));
        comandos.forEach(c -> assertTrue(invocador.ejecutar(c, "x").exitoso()));
        assertTrue(comandos.get(1).descripcion().contains("#" + id));
    }
}
