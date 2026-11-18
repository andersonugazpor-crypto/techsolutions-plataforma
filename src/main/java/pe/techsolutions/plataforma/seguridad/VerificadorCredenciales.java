package pe.techsolutions.plataforma.seguridad;

/**
 * Comprueba que las credenciales de una sesión siguen siendo válidas
 * (el usuario existe, está activo y mantiene el mismo rol).
 */
public interface VerificadorCredenciales {

    boolean esValido(UsuarioSesion usuario);
}
