package pe.techsolutions.plataforma.seguridad;

import pe.techsolutions.plataforma.dominio.modelo.Rol;

import java.io.Serializable;

/** Datos mínimos del usuario autenticado que se guardan en la sesión HTTP. */
public record UsuarioSesion(Long id, String username, String nombreCompleto, Rol rol) implements Serializable {

    public boolean tieneRol(Rol otro) {
        return rol == otro;
    }

    public boolean esAdministrador() {
        return rol == Rol.ADMINISTRADOR;
    }
}
