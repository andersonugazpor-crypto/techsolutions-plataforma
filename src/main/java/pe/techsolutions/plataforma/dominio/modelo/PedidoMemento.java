package pe.techsolutions.plataforma.dominio.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Patrón Memento: instantánea inmutable del estado interno de un Pedido.
 * Solo el Pedido (originador) puede crearla y leer su contenido: el constructor
 * y los accesores del estado son de paquete, por lo que el cuidador
 * (HistorialMementos) la guarda sin poder inspeccionarla ni modificarla.
 */
public final class PedidoMemento {

    private final Long pedidoId;
    private final EstadoPedidoTipo estado;
    private final BigDecimal porcentajeDescuento;
    private final String codigoTransaccion;
    private final String etiqueta;
    private final LocalDateTime fechaCaptura;

    PedidoMemento(Long pedidoId, EstadoPedidoTipo estado, BigDecimal porcentajeDescuento,
                  String codigoTransaccion, String etiqueta, LocalDateTime fechaCaptura) {
        this.pedidoId = pedidoId;
        this.estado = estado;
        this.porcentajeDescuento = porcentajeDescuento;
        this.codigoTransaccion = codigoTransaccion;
        this.etiqueta = etiqueta;
        this.fechaCaptura = fechaCaptura;
    }

    // --- acceso restringido al originador (mismo paquete) ---
    EstadoPedidoTipo estado() { return estado; }
    BigDecimal porcentajeDescuento() { return porcentajeDescuento; }
    String codigoTransaccion() { return codigoTransaccion; }

    // --- metadatos públicos para mostrar el historial ---
    public Long getPedidoId() { return pedidoId; }
    public String getEtiqueta() { return etiqueta; }
    public LocalDateTime getFechaCaptura() { return fechaCaptura; }

    /** Texto legible del punto de restauración, sin exponer el estado interno como objetos. */
    public String getResumen() {
        return "Estado " + estado.getEtiqueta() + ", descuento " + porcentajeDescuento.stripTrailingZeros().toPlainString() + "%";
    }
}
