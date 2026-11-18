package pe.techsolutions.plataforma.precio;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.techsolutions.plataforma.dominio.modelo.ParametroSistema;
import pe.techsolutions.plataforma.repositorio.ParametroSistemaRepositorio;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

@Component
public class ConfiguracionPreciosJpa implements ConfiguracionPrecios {

    public static final String CLAVE_ESTRATEGIA = "precio.estrategia";
    public static final String CLAVE_DESCUENTO = "precio.descuento";
    public static final String CLAVE_RECARGO = "precio.recargoTemporada";

    private final ParametroSistemaRepositorio parametros;
    private final Clock reloj;

    public ConfiguracionPreciosJpa(ParametroSistemaRepositorio parametros, Clock reloj) {
        this.parametros = parametros;
        this.reloj = reloj;
    }

    @Override
    @Transactional(readOnly = true)
    public String codigoEstrategiaActiva() {
        return leer(CLAVE_ESTRATEGIA, "ESTANDAR");
    }

    @Override
    @Transactional(readOnly = true)
    public ParametrosPrecio parametros() {
        return new ParametrosPrecio(new BigDecimal(leer(CLAVE_DESCUENTO, "10")),
                new BigDecimal(leer(CLAVE_RECARGO, "15")), LocalDate.now(reloj));
    }

    @Override
    @Transactional
    public void actualizar(String codigoEstrategia, BigDecimal porcentajeDescuento, BigDecimal recargoTemporadaAlta) {
        guardar(CLAVE_ESTRATEGIA, codigoEstrategia);
        guardar(CLAVE_DESCUENTO, porcentajeDescuento.toPlainString());
        guardar(CLAVE_RECARGO, recargoTemporadaAlta.toPlainString());
    }

    private String leer(String clave, String porDefecto) {
        return parametros.findById(clave).map(ParametroSistema::getValor).orElse(porDefecto);
    }

    private void guardar(String clave, String valor) {
        ParametroSistema parametro = parametros.findById(clave).orElseGet(() -> new ParametroSistema(clave, valor));
        parametro.setValor(valor);
        parametros.save(parametro);
    }
}
