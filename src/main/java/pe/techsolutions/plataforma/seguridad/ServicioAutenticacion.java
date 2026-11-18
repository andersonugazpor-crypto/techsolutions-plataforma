package pe.techsolutions.plataforma.seguridad;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.techsolutions.plataforma.repositorio.UsuarioRepositorio;

import java.util.Optional;

/** Valida usuario y contraseña (hash BCrypt) y construye el usuario de sesión. */
@Service
public class ServicioAutenticacion {

    private final UsuarioRepositorio usuarios;
    private final PasswordEncoder passwordEncoder;

    public ServicioAutenticacion(UsuarioRepositorio usuarios, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Optional<UsuarioSesion> autenticar(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }
        return usuarios.findByUsername(username.trim().toLowerCase())
                .filter(u -> u.isActivo())
                .filter(u -> passwordEncoder.matches(password, u.getPasswordHash()))
                .map(u -> new UsuarioSesion(u.getId(), u.getUsername(), u.getNombreCompleto(), u.getRol()));
    }
}
