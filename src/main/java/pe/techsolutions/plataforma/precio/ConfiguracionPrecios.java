package pe.techsolutions.plataforma.precio;

import java.math.BigDecimal;

/** RF10: estrategia activa y parámetros elegidos por el administrador. */
public interface ConfiguracionPrecios {

    String codigoEstrategiaActiva();

    ParametrosPrecio parametros();

    void actualizar(String codigoEstrategia, BigDecimal porcentajeDescuento, BigDecimal recargoTemporadaAlta);
}
