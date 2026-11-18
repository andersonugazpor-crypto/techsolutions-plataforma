package pe.techsolutions.plataforma.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.techsolutions.plataforma.dominio.modelo.ParametroSistema;

public interface ParametroSistemaRepositorio extends JpaRepository<ParametroSistema, String> {
}
