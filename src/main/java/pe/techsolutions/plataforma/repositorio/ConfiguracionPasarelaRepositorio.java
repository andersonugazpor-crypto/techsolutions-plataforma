package pe.techsolutions.plataforma.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.techsolutions.plataforma.dominio.modelo.ConfiguracionPasarela;

public interface ConfiguracionPasarelaRepositorio extends JpaRepository<ConfiguracionPasarela, String> {
}
