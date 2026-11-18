package pe.techsolutions.plataforma.pago;

import pe.techsolutions.plataforma.pago.sdk.PlinClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapter para Plin: arma el mapa que espera el cliente y lee su respuesta.
 */
public class PlinAdaptador implements PasarelaPago {

    private final PlinClient cliente;

    public PlinAdaptador(PlinClient cliente) {
        this.cliente = cliente;
    }

    @Override public String codigo() { return "PLIN"; }
    @Override public String nombre() { return "Plin"; }
    @Override public String tipoContacto() { return "celular"; }

    @Override
    public ResultadoPago pagar(SolicitudPago solicitud) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("amount", solicitud.montoSoles());
        payload.put("phone", solicitud.contacto());
        payload.put("description", "Pedido " + solicitud.referencia());
        Map<String, String> respuesta = cliente.transfer(payload);
        if ("OK".equals(respuesta.get("status"))) {
            return ResultadoPago.aprobado(codigo(), respuesta.get("ref"));
        }
        return ResultadoPago.rechazado(codigo(), "Plin: " + respuesta.getOrDefault("detail", "rechazado"));
    }
}
