package pe.techsolutions.plataforma.pago;

import java.math.BigDecimal;

/**
 * Datos de cobro en el formato propio de la plataforma (soles, BigDecimal).
 *
 * @param referencia identificador del pedido
 * @param montoSoles importe a cobrar en soles
 * @param cliente    nombre del cliente
 * @param contacto   correo o celular según la pasarela
 */
public record SolicitudPago(String referencia, BigDecimal montoSoles, String cliente, String contacto) {
}
