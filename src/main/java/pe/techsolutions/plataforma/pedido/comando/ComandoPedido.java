package pe.techsolutions.plataforma.pedido.comando;

/**
 * Patrón Command — interfaz del comando (RF7).
 * Cada acción sobre un pedido se encapsula como un objeto con todo lo necesario
 * para ejecutarse más tarde: así puede encolarse, registrarse en el historial
 * y asociarse a un punto de restauración (memento).
 */
public interface ComandoPedido {

    /** Nombre corto de la acción, p. ej. "PROCESAR". */
    String nombre();

    /** Descripción legible para el historial y la cola. */
    String descripcion();

    /** Pedido afectado; puede ser null antes de crear el pedido. */
    Long pedidoId();

    /** Si es true, el invocador guarda un memento del pedido antes de ejecutar. */
    boolean requiereMemento();

    ResultadoComando ejecutar();
}
