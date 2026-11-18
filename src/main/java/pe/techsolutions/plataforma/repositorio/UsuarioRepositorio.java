package pe.techsolutions.plataforma.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.techsolutions.plataforma.dominio.modelo.Usuario;

import java.util.Optional;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);
}
