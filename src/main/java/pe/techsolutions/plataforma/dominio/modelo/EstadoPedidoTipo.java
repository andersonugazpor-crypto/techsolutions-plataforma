package pe.techsolutions.plataforma.dominio.modelo;

/**
 * Valor persistible del estado. Cada constante entrega el objeto State
 * que implementa su comportamiento.
 */
public enum EstadoPedidoTipo {
    PENDIENTE("Pendiente", "pendiente", new EstadoPendiente()),
    PROCESADO("Procesado", "procesado", new EstadoProcesado()),
    CANCELADO("Cancelado", "cancelado", new EstadoCancelado());

    private final String etiqueta;
    private final String claseCss;
    private final EstadoPedido comportamiento;

    EstadoPedidoTipo(String etiqueta, String claseCss, EstadoPedido comportamiento) {
        this.etiqueta = etiqueta;
        this.claseCss = claseCss;
        this.comportamiento = comportamiento;
    }

    public String getEtiqueta() { return etiqueta; }
    public String getClaseCss() { return claseCss; }
    public EstadoPedido comportamiento() { return comportamiento; }
}
