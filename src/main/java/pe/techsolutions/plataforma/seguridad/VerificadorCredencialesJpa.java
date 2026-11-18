package pe.techsolutions.plataforma.seguridad;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.techsolutions.plataforma.repositorio.UsuarioRepositorio;

@Component
public class VerificadorCredencialesJpa implements VerificadorCredenciales {

    private final UsuarioRepositorio usuarios;

    public VerificadorCredencialesJpa(UsuarioRepositorio usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean esValido(UsuarioSesion sesion) {
        return usuarios.findById(sesion.id())
                .filter(u -> u.isActivo())
                .filter(u -> u.getUsername().equals(sesion.username()))
                .filter(u -> u.getRol() == sesion.rol())
                .isPresent();
    }
}
