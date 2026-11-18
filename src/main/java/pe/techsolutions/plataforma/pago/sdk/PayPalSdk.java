package pe.techsolutions.plataforma.pago.sdk;

import java.util.UUID;

/**
 * Simulación del SDK de PayPal (clase "Adaptada" / Adaptee).
 * Su interfaz es incompatible con la de la plataforma: trabaja en dólares (double),
 * identifica al pagador por correo y devuelve su propio objeto de respuesta.
 * No se modifica: el Adaptador es quien traduce.
 */
public class PayPalSdk {

    public PayPalResponse createPayment(String payerEmail, double amount, String currencyCode) {
        if (amount <= 0 || payerEmail == null || !payerEmail.contains("@")) {
            return new PayPalResponse(null, "failed", "INVALID_REQUEST");
        }
        return new PayPalResponse("PAYID-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase(), "approved", null);
    }

    public record PayPalResponse(String id, String state, String errorName) {
        public boolean isApproved() {
            return "approved".equals(state);
        }
    }
}
