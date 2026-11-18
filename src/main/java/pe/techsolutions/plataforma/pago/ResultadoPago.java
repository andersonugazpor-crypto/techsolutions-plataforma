package pe.techsolutions.plataforma.pago;

/** Respuesta uniforme de cualquier pasarela, sin importar su API original. */
public record ResultadoPago(boolean aprobado, String codigoTransaccion, String mensaje, String pasarela) {

    public static ResultadoPago aprobado(String pasarela, String codigoTransaccion) {
        return new ResultadoPago(true, codigoTransaccion, "Pago aprobado", pasarela);
    }

    public static ResultadoPago rechazado(String pasarela, String motivo) {
        return new ResultadoPago(false, null, motivo, pasarela);
    }
}
