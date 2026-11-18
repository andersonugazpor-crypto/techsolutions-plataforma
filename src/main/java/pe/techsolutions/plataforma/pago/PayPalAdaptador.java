package pe.techsolutions.plataforma.pago;

import pe.techsolutions.plataforma.pago.sdk.PayPalSdk;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Adapter para PayPal: convierte soles a dólares y traduce la respuesta del SDK.
 */
public class PayPalAdaptador implements PasarelaPago {

    private final PayPalSdk sdk;
    private final BigDecimal tipoCambio;

    public PayPalAdaptador(PayPalSdk sdk, BigDecimal tipoCambio) {
        this.sdk = sdk;
        this.tipoCambio = tipoCambio;
    }

    @Override public String codigo() { return "PAYPAL"; }
    @Override public String nombre() { return "PayPal"; }
    @Override public String tipoContacto() { return "correo electrónico"; }

    @Override
    public ResultadoPago pagar(SolicitudPago solicitud) {
        double montoUsd = solicitud.montoSoles().divide(tipoCambio, 2, RoundingMode.HALF_UP).doubleValue();
        PayPalSdk.PayPalResponse respuesta = sdk.createPayment(solicitud.contacto(), montoUsd, "USD");
        if (respuesta.isApproved()) {
            return ResultadoPago.aprobado(codigo(), respuesta.id());
        }
        return ResultadoPago.rechazado(codigo(), "PayPal rechazó el pago: " + respuesta.errorName());
    }
}
