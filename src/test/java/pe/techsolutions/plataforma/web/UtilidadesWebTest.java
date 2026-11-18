package pe.techsolutions.plataforma.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.techsolutions.plataforma.pedido.LineaPedido;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Capa web: conversión de formularios y datos de gráficos")
class UtilidadesWebTest {

    @Test
    @DisplayName("Ignora filas vacías o inválidas del formulario de pedido")
    void lineasDelFormulario() {
        List<LineaPedido> lineas = PedidoController.lineas(
                Arrays.asList("1", "", "3", "x", "5"),
                Arrays.asList("2", "4", "", "1", "7"));
        assertEquals(List.of(new LineaPedido(1L, 2), new LineaPedido(5L, 7)), lineas);
        assertEquals(0, PedidoController.lineas(null, null).size());
    }

    @Test
    @DisplayName("Calcula el ancho relativo de cada barra del reporte")
    void porcentajesDeBarras() {
        Map<String, BigDecimal> montos = new LinkedHashMap<>();
        montos.put("YAPE", new BigDecimal("200"));
        montos.put("PLIN", new BigDecimal("50"));
        Map<String, Integer> barras = ReporteController.porcentajes(montos);
        assertEquals(100, barras.get("YAPE"));
        assertEquals(25, barras.get("PLIN"));
    }
}
