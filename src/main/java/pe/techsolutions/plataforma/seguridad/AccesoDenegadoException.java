package pe.techsolutions.plataforma.seguridad;

/** El usuario no tiene credenciales válidas o un rol autorizado para el recurso. */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
