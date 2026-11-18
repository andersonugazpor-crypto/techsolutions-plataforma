package pe.techsolutions.plataforma.reporte;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ReporteFinanciero(
        LocalDateTime fechaGeneracion,
        BigDecimal ingresosTotales,
        BigDecimal descuentosOtorgados,
        BigDecimal ticketPromedio,
        long pedidosProcesados,
        long pedidosPendientes,
        long pedidosCancelados,
        Map<String, BigDecimal> ventasPorPasarela,
        Map<String, BigDecimal> ventasPorCategoria,
        List<LineaTopProducto> topProductos,
        long milisegundosGeneracion) {

    public record LineaTopProducto(String codigo, String nombre, int unidades, BigDecimal importe) {
    }
}
