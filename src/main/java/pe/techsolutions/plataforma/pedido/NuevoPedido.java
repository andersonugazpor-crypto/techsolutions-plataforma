package pe.techsolutions.plataforma.pedido;

import java.util.List;

/** Datos capturados en el formulario para crear un pedido. */
public record NuevoPedido(String cliente, String metodoPago, String contactoPago, List<LineaPedido> lineas) {
}
