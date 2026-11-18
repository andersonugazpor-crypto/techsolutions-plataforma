package pe.techsolutions.plataforma.pago;

import pe.techsolutions.plataforma.pago.sdk.YapeApi;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Adapter para Yape: convierte soles a céntimos e interpreta los códigos numéricos.
 */
public class YapeAdaptador implements PasarelaPago {

    private static final Map<Integer, String> ERRORES = Map.of(
            YapeApi.ERROR_CELULAR, "Número de celular Yape inválido (9 dígitos, empieza en 9)",
            YapeApi.ERROR_LIMITE, "Monto fuera del límite permitido por Yape");

    private final YapeApi api;

    public YapeAdaptador(YapeApi api) {
        this.api = api;
    }

    @Override public String codigo() { return "YAPE"; }
    @Override public String nombre() { return "Yape"; }
    @Override public String tipoContacto() { return "celular"; }

    @Override
    public ResultadoPago pagar(SolicitudPago solicitud) {
        long centimos = solicitud.montoSoles().multiply(BigDecimal.valueOf(100)).longValue();
        int codigo = api.enviarCobro(solicitud.contacto(), centimos, "Pedido " + solicitud.referencia());
        if (codigo == YapeApi.OK) {
            return ResultadoPago.aprobado(codigo(), "YAPE-" + api.obtenerNumeroOperacion());
        }
        return ResultadoPago.rechazado(codigo(), ERRORES.getOrDefault(codigo, "Error Yape " + codigo));
    }
}
