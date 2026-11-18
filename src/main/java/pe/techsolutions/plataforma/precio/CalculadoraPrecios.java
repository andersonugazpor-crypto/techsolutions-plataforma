package pe.techsolutions.plataforma.precio;

import pe.techsolutions.plataforma.dominio.modelo.OperacionNoPermitidaException;
import pe.techsolutions.plataforma.dominio.modelo.Producto;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Patrón Strategy — contexto. Obtiene la estrategia activa desde la configuración
 * y le delega el cálculo. Cambiar de política no requiere modificar esta clase:
 * basta con registrar otra implementación de {@link EstrategiaPrecio}.
 */
public class CalculadoraPrecios {

    private static final BigDecimal CINCUENTA = BigDecimal.valueOf(50);
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private final Map<String, EstrategiaPrecio> estrategias = new LinkedHashMap<>();
    private final ConfiguracionPrecios configuracion;

    public CalculadoraPrecios(List<EstrategiaPrecio> disponibles, ConfiguracionPrecios configuracion) {
        disponibles.forEach(e -> estrategias.put(e.codigo(), e));
        this.configuracion = configuracion;
    }

    public BigDecimal precioDe(Producto producto) {
        return estrategiaActiva().calcular(producto, configuracion.parametros());
    }

    public EstrategiaPrecio estrategiaActiva() {
        return estrategias.getOrDefault(configuracion.codigoEstrategiaActiva(), estrategias.values().iterator().next());
    }

    public ParametrosPrecio parametros() {
        return configuracion.parametros();
    }

    public Collection<EstrategiaPrecio> estrategias() {
        return Collections.unmodifiableCollection(estrategias.values());
    }

    /** RF10: el administrador selecciona la estrategia y sus parámetros. */
    public void cambiarEstrategia(String codigo, BigDecimal porcentajeDescuento, BigDecimal recargoTemporadaAlta) {
        if (!estrategias.containsKey(codigo)) {
            throw new OperacionNoPermitidaException("Estrategia de precios desconocida: " + codigo);
        }
        validarRango(porcentajeDescuento, CINCUENTA, "El descuento");
        validarRango(recargoTemporadaAlta, CIEN, "El recargo de temporada");
        configuracion.actualizar(codigo, porcentajeDescuento, recargoTemporadaAlta);
    }

    private void validarRango(BigDecimal valor, BigDecimal maximo, String nombre) {
        if (valor == null || valor.signum() < 0 || valor.compareTo(maximo) > 0) {
            throw new OperacionNoPermitidaException(nombre + " debe estar entre 0 y " + maximo + "%");
        }
    }
}
