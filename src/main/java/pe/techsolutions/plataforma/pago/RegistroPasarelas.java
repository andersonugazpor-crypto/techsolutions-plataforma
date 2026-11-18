package pe.techsolutions.plataforma.pago;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * GRASP Fabricación pura: catálogo de adaptadores disponibles.
 * Spring inyecta todos los beans {@link PasarelaPago}; agregar una pasarela nueva
 * solo requiere crear su adaptador, sin modificar esta clase ni a sus clientes.
 */
public class RegistroPasarelas {

    private final Map<String, PasarelaPago> pasarelas = new LinkedHashMap<>();

    public RegistroPasarelas(List<PasarelaPago> disponibles) {
        disponibles.forEach(p -> pasarelas.put(p.codigo(), p));
    }

    public Optional<PasarelaPago> buscar(String codigo) {
        return Optional.ofNullable(pasarelas.get(codigo));
    }

    public Collection<PasarelaPago> todas() {
        return Collections.unmodifiableCollection(pasarelas.values());
    }
}
