package pe.techsolutions.plataforma.precio;

import pe.techsolutions.plataforma.dominio.modelo.Producto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PrecioDescuentoPorcentual implements EstrategiaPrecio {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    @Override public String codigo() { return "DESCUENTO"; }
    @Override public String nombre() { return "Descuento porcentual"; }
    @Override public String descripcion() { return "Aplica el porcentaje de descuento configurado a todo el catálogo (promociones)."; }

    @Override
    public BigDecimal calcular(Producto producto, ParametrosPrecio parametros) {
        BigDecimal factor = CIEN.subtract(parametros.porcentajeDescuento()).divide(CIEN, 4, RoundingMode.HALF_UP);
        return producto.getPrecioBase().multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }
}
