package pe.techsolutions.plataforma.pago.sdk;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Simulación de la API de Yape (Adaptee).
 * Interfaz incompatible: monto en céntimos (long), identifica al cliente por celular
 * y responde con un código numérico (0 = éxito).
 */
public class YapeApi {

    public static final int OK = 0;
    public static final int ERROR_CELULAR = 101;
    public static final int ERROR_LIMITE = 102;

    /** Límite diario de Yape simulado: S/ 2,000.00 */
    private static final long LIMITE_CENTIMOS = 200_000L;

    private final AtomicLong secuencia = new AtomicLong(700000);
    private String ultimoNumeroOperacion;

    public int enviarCobro(String numeroCelular, long montoEnCentimos, String concepto) {
        if (numeroCelular == null || !numeroCelular.matches("9\\d{8}")) {
            return ERROR_CELULAR;
        }
        if (montoEnCentimos <= 0 || montoEnCentimos > LIMITE_CENTIMOS) {
            return ERROR_LIMITE;
        }
        ultimoNumeroOperacion = String.valueOf(secuencia.incrementAndGet());
        return OK;
    }

    public String obtenerNumeroOperacion() {
        return ultimoNumeroOperacion;
    }
}
