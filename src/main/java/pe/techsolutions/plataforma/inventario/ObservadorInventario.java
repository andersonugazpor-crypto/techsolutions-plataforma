package pe.techsolutions.plataforma.inventario;

/** Patrón Observer — interfaz del observador. */
public interface ObservadorInventario {

    void actualizar(EventoStockBajo evento);
}
