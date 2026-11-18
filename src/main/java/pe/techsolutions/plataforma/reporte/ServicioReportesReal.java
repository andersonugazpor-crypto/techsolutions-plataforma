package pe.techsolutions.plataforma.reporte;

import pe.techsolutions.plataforma.dominio.modelo.EstadoPedidoTipo;
import pe.techsolutions.plataforma.dominio.modelo.ItemPedido;
import pe.techsolutions.plataforma.dominio.modelo.Pedido;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Patrón Proxy — objeto real (RealSubject).
 * Calcula los reportes recorriendo todos los pedidos: es una operación costosa
 * y expone información sensible, por eso nunca se usa directamente sino a través
 * de {@link ProxyServicioReportes}, que además lo crea de forma perezosa.
 * No es un bean de Spring a propósito.
 */
public class ServicioReportesReal implements ServicioReportes {

    private static final int TOP = 5;

    private final FuenteDatosReporte fuente;
    private final Clock reloj;

    public ServicioReportesReal(FuenteDatosReporte fuente, Clock reloj) {
        this.fuente = fuente;
        this.reloj = reloj;
    }

    @Override
    public ReporteFinanciero reporteCompleto() {
        long inicio = System.nanoTime();
        List<Pedido> pedidos = fuente.pedidos();
        List<Pedido> procesados = pedidos.stream().filter(p -> p.getEstado() == EstadoPedidoTipo.PROCESADO).toList();

        BigDecimal ingresos = procesados.stream().map(Pedido::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal descuentos = procesados.stream().map(Pedido::getMontoDescuento).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal ticket = procesados.isEmpty() ? BigDecimal.ZERO
                : ingresos.divide(BigDecimal.valueOf(procesados.size()), 2, RoundingMode.HALF_UP);

        Map<String, BigDecimal> porPasarela = new TreeMap<>();
        Map<String, BigDecimal> porCategoria = new TreeMap<>();
        Map<String, ReporteFinanciero.LineaTopProducto> porProducto = new HashMap<>();
        for (Pedido pedido : procesados) {
            porPasarela.merge(pedido.getMetodoPago(), pedido.getTotal(), BigDecimal::add);
            for (ItemPedido item : pedido.getItems()) {
                porCategoria.merge(item.getProducto().getCategoria(), item.getSubtotal(), BigDecimal::add);
                porProducto.merge(item.getProducto().getCodigo(),
                        new ReporteFinanciero.LineaTopProducto(item.getProducto().getCodigo(), item.getProducto().getNombre(),
                                item.getCantidad(), item.getSubtotal()),
                        (a, b) -> new ReporteFinanciero.LineaTopProducto(a.codigo(), a.nombre(),
                                a.unidades() + b.unidades(), a.importe().add(b.importe())));
            }
        }
        List<ReporteFinanciero.LineaTopProducto> top = porProducto.values().stream()
                .sorted(Comparator.comparing(ReporteFinanciero.LineaTopProducto::importe).reversed())
                .limit(TOP)
                .toList();

        long ms = (System.nanoTime() - inicio) / 1_000_000;
        return new ReporteFinanciero(LocalDateTime.now(reloj), ingresos, descuentos, ticket,
                procesados.size(), contar(pedidos, EstadoPedidoTipo.PENDIENTE), contar(pedidos, EstadoPedidoTipo.CANCELADO),
                new LinkedHashMap<>(porPasarela), new LinkedHashMap<>(porCategoria), top, ms);
    }

    @Override
    public ResumenOperativo resumenOperativo() {
        List<Pedido> pedidos = fuente.pedidos();
        return new ResumenOperativo(LocalDateTime.now(reloj), contar(pedidos, EstadoPedidoTipo.PROCESADO),
                contar(pedidos, EstadoPedidoTipo.PENDIENTE), contar(pedidos, EstadoPedidoTipo.CANCELADO));
    }

    private long contar(List<Pedido> pedidos, EstadoPedidoTipo estado) {
        return pedidos.stream().filter(p -> p.getEstado() == estado).count();
    }
}
