package pe.techsolutions.plataforma.seguridad;

import java.util.Optional;

/** Indirección para obtener el usuario autenticado sin acoplarse a la sesión HTTP. */
public interface ProveedorUsuarioActual {

    String ATRIBUTO_SESION = "usuarioSesion";

    Optional<UsuarioSesion> actual();
}
