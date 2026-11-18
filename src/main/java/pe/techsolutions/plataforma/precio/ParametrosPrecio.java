package pe.techsolutions.plataforma.precio;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Parámetros configurables por el administrador que usan las estrategias.
 *
 * @param porcentajeDescuento  descuento general para la estrategia de descuento porcentual
 * @param recargoTemporadaAlta recargo (%) que aplica el precio dinámico en temporada alta
 * @param fecha                fecha de referencia para la temporada
 */
public record ParametrosPrecio(BigDecimal porcentajeDescuento, BigDecimal recargoTemporadaAlta, LocalDate fecha) {
}
