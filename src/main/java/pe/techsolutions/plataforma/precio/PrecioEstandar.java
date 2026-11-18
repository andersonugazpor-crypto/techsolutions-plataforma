package pe.techsolutions.plataforma.precio;

import pe.techsolutions.plataforma.dominio.modelo.Producto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PrecioEstandar implements EstrategiaPrecio {

    @Override public String codigo() { return "ESTANDAR"; }
    @Override public String nombre() { return "Precio estándar"; }
    @Override public String descripcion() { return "Se vende al precio base de lista."; }

    @Override
    public BigDecimal calcular(Producto producto, ParametrosPrecio parametros) {
        return producto.getPrecioBase().setScale(2, RoundingMode.HALF_UP);
    }
}
