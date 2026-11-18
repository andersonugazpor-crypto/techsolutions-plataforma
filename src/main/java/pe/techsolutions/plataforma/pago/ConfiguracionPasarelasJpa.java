package pe.techsolutions.plataforma.pago;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.techsolutions.plataforma.dominio.modelo.ConfiguracionPasarela;
import pe.techsolutions.plataforma.repositorio.ConfiguracionPasarelaRepositorio;

/** Implementación de {@link EstadoPasarelas} persistida en la tabla config_pasarelas. */
@Component
public class ConfiguracionPasarelasJpa implements EstadoPasarelas {

    private final ConfiguracionPasarelaRepositorio repositorio;

    public ConfiguracionPasarelasJpa(ConfiguracionPasarelaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaHabilitada(String codigo) {
        return repositorio.findById(codigo).map(ConfiguracionPasarela::isHabilitada).orElse(false);
    }

    @Override
    @Transactional
    public void cambiar(String codigo, boolean habilitada) {
        ConfiguracionPasarela config = repositorio.findById(codigo)
                .orElseGet(() -> new ConfiguracionPasarela(codigo, habilitada));
        config.setHabilitada(habilitada);
        repositorio.save(config);
    }
}
