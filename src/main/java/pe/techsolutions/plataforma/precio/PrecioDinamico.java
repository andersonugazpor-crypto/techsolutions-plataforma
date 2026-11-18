package pe.techsolutions.plataforma.precio;

import pe.techsolutions.plataforma.dominio.modelo.Producto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.util.EnumSet;
import java.util.Set;

/**
 * Precio ajustado por demanda y temporada:
 * <ul>
 *   <li>Demanda alta (≥ 50 unidades vendidas): +10 %; demanda media (≥ 20): +5 %.</li>
 *   <li>Escasez (stock en o bajo el mínimo): +5 %.</li>
 *   <li>Temporada alta en Perú (julio – Fiestas Patrias, diciembre – Navidad): + recargo configurable.</li>
 *   <li>Temporada baja (febrero y marzo): −5 %.</li>
 * </ul>
 */
public class PrecioDinamico implements EstrategiaPrecio {

    private static final Set<Month> TEMPORADA_ALTA = EnumSet.of(Month.JULY, Month.DECEMBER);
    private static final Set<Month> TEMPORADA_BAJA = EnumSet.of(Month.FEBRUARY, Month.MARCH);
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    @Override public String codigo() { return "DINAMICO"; }
    @Override public String nombre() { return "Precio dinámico"; }
    @Override public String descripcion() { return "Ajusta el precio según la demanda del producto y la temporada del año."; }

    @Override
    public BigDecimal calcular(Producto producto, ParametrosPrecio parametros) {
        BigDecimal ajuste = ajustePorDemanda(producto)
                .add(ajustePorEscasez(producto))
                .add(ajustePorTemporada(parametros));
        BigDecimal factor = CIEN.add(ajuste).divide(CIEN, 4, RoundingMode.HALF_UP);
        return producto.getPrecioBase().multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    BigDecimal ajustePorDemanda(Producto producto) {
        int vendidas = producto.getUnidadesVendidas();
        if (vendidas >= 50) {
            return BigDecimal.TEN;
        }
        return vendidas >= 20 ? BigDecimal.valueOf(5) : BigDecimal.ZERO;
    }

    BigDecimal ajustePorEscasez(Producto producto) {
        return producto.getStock() <= producto.getStockMinimo() ? BigDecimal.valueOf(5) : BigDecimal.ZERO;
    }

    BigDecimal ajustePorTemporada(ParametrosPrecio parametros) {
        Month mes = parametros.fecha().getMonth();
        if (TEMPORADA_ALTA.contains(mes)) {
            return parametros.recargoTemporadaAlta();
        }
        return TEMPORADA_BAJA.contains(mes) ? BigDecimal.valueOf(-5) : BigDecimal.ZERO;
    }
}
