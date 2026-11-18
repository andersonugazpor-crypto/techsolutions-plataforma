package pe.techsolutions.plataforma.inventario;

/** Patrón Observer — interfaz del sujeto observable. */
public interface SujetoInventario {

    void suscribir(ObservadorInventario observador);

    void desuscribir(ObservadorInventario observador);

    void notificar(EventoStockBajo evento);
}
