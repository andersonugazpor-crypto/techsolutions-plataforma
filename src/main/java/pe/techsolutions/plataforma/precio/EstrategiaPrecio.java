package pe.techsolutions.plataforma.precio;

import pe.techsolutions.plataforma.dominio.modelo.Producto;

import java.math.BigDecimal;

/**
 * Patrón Strategy — familia de algoritmos de cálculo de precio intercambiables (RF9).
 * GRASP Polimorfismo: cada política es una clase; no hay condicionales por tipo de estrategia.
 */
public interface EstrategiaPrecio {

    String codigo();

    String nombre();

    String descripcion();

    BigDecimal calcular(Producto producto, ParametrosPrecio parametros);
}
