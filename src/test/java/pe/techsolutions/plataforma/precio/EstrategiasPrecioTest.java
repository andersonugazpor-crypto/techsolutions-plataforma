package pe.techsolutions.plataforma.precio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.techsolutions.plataforma.dominio.modelo.OperacionNoPermitidaException;
import pe.techsolutions.plataforma.dominio.modelo.Producto;
import pe.techsolutions.plataforma.soporte.Datos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Strategy: políticas de precios intercambiables")
class EstrategiasPrecioTest {

    /** Configuración en memoria (sustituye a la tabla de parámetros). */
    static class ConfiguracionEnMemoria implements ConfiguracionPrecios {
        String codigo = "ESTANDAR";
        BigDecimal descuento = BigDecimal.TEN;
        BigDecimal recargo = new BigDecimal("15");
        LocalDate fecha = LocalDate.of(2026, 11, 18);

        @Override public String codigoEstrategiaActiva() { return codigo; }
        @Override public ParametrosPrecio parametros() { return new ParametrosPrecio(descuento, recargo, fecha); }
        @Override public void actualizar(String c, BigDecimal d, BigDecimal r) { codigo = c; descuento = d; recargo = r; }
    }

    private ConfiguracionEnMemoria config;
    private CalculadoraPrecios calculadora;
    private Producto producto;

    @BeforeEach
    void preparar() {
        config = new ConfiguracionEnMemoria();
        calculadora = new CalculadoraPrecios(List.of(new PrecioEstandar(), new PrecioDescuentoPorcentual(), new PrecioDinamico()), config);
        producto = Datos.producto(1, "Detergente", "100.00", 50, 10);
    }

    @Test
    @DisplayName("RF9: precio estándar = precio base")
    void estandar() {
        assertEquals("100.00", Datos.dinero(calculadora.precioDe(producto)));
    }

    @Test
    @DisplayName("RF10: al cambiar la estrategia, el mismo contexto calcula distinto (sin if)")
    void cambiarEstrategia() {
        calculadora.cambiarEstrategia("DESCUENTO", new BigDecimal("20"), new BigDecimal("15"));
        assertEquals("80.00", Datos.dinero(calculadora.precioDe(producto)));
        assertEquals("Descuento porcentual", calculadora.estrategiaActiva().nombre());
    }

    @Test
    @DisplayName("Precio dinámico: demanda alta y temporada alta (diciembre)")
    void dinamicoTemporadaAlta() {
        producto.asignarUnidadesVendidas(60);         // +10 %
        config.fecha = LocalDate.of(2026, 12, 10);   // +15 %
        config.codigo = "DINAMICO";
        assertEquals("125.00", Datos.dinero(calculadora.precioDe(producto)));
    }

    @Test
    @DisplayName("Precio dinámico: escasez suma y temporada baja (marzo) resta")
    void dinamicoEscasezTemporadaBaja() {
        Producto escaso = Datos.producto(2, "Router", "200.00", 3, 3); // +5 % escasez
        config.fecha = LocalDate.of(2026, 3, 1);                      // -5 %
        config.codigo = "DINAMICO";
        assertEquals("200.00", Datos.dinero(calculadora.precioDe(escaso)));
    }

    @Test
    @DisplayName("Precio dinámico: demanda media sin temporada")
    void dinamicoDemandaMedia() {
        producto.asignarUnidadesVendidas(25);  // +5 %
        config.codigo = "DINAMICO";
        assertEquals("105.00", Datos.dinero(calculadora.precioDe(producto)));
    }

    @Test
    @DisplayName("Rechaza estrategias inexistentes y parámetros fuera de rango")
    void validaciones() {
        assertThrows(OperacionNoPermitidaException.class,
                () -> calculadora.cambiarEstrategia("GRATIS", BigDecimal.ONE, BigDecimal.ONE));
        assertThrows(OperacionNoPermitidaException.class,
                () -> calculadora.cambiarEstrategia("DESCUENTO", new BigDecimal("70"), BigDecimal.ONE));
    }
}
