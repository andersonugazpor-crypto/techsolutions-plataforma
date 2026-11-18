package pe.techsolutions.plataforma.pago;

/**
 * Patrón Adapter — interfaz objetivo (Target).
 * Es lo único que conoce el código cliente para cobrar. Cada pasarela externa
 * se integra con un adaptador que implementa esta interfaz.
 * GRASP Variaciones protegidas: los cambios en las APIs externas quedan
 * encapsulados detrás de este contrato estable.
 */
public interface PasarelaPago {

    /** Identificador único, p. ej. "YAPE". */
    String codigo();

    /** Nombre visible para el usuario. */
    String nombre();

    /** Indica qué dato de contacto requiere (correo o celular), para mostrarlo en la interfaz. */
    String tipoContacto();

    ResultadoPago pagar(SolicitudPago solicitud);
}
