package pe.techsolutions.plataforma.dominio.modelo;

/**
 * Se lanza cuando una regla de negocio impide realizar una operación
 * (por ejemplo, procesar un pedido cancelado o descontar más stock del disponible).
 */
public class OperacionNoPermitidaException extends RuntimeException {

    public OperacionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}
