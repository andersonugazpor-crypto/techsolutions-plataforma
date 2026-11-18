package pe.techsolutions.plataforma.pago.sdk;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Simulación del cliente de Plin (Adaptee).
 * Interfaz incompatible: recibe y devuelve mapas clave-valor (estilo JSON).
 */
public class PlinClient {

    public Map<String, String> transfer(Map<String, Object> payload) {
        Map<String, String> respuesta = new HashMap<>();
        Object monto = payload.get("amount");
        Object destino = payload.get("phone");
        if (!(monto instanceof BigDecimal) || ((BigDecimal) monto).signum() <= 0 || destino == null) {
            respuesta.put("status", "REJECTED");
            respuesta.put("detail", "Datos de transferencia inválidos");
            return respuesta;
        }
        respuesta.put("status", "OK");
        respuesta.put("ref", "PLN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return respuesta;
    }
}
