package pe.techsolutions.plataforma.inventario;

import pe.techsolutions.plataforma.dominio.modelo.Producto;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Patrón Observer — sujeto concreto.
 * Mantiene la lista de observadores (relación uno a muchos) y decide cuándo
 * notificar: cuando el stock de un producto cae por debajo de su mínimo (RF5).
 * El sujeto solo conoce la interfaz {@link ObservadorInventario}: se pueden agregar
 * nuevos observadores (correo, WhatsApp, etc.) sin tocar esta clase.
 */
public class PublicadorStock implements SujetoInventario {

    private final List<ObservadorInventario> observadores = new CopyOnWriteArrayList<>();
    private final Clock reloj;

    public PublicadorStock(Clock reloj) {
        this.reloj = reloj;
    }

    @Override
    public void suscribir(ObservadorInventario observador) {
        if (!observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    @Override
    public void desuscribir(ObservadorInventario observador) {
        observadores.remove(observador);
    }

    @Override
    public void notificar(EventoStockBajo evento) {
        observadores.forEach(o -> o.actualizar(evento));
    }

    /**
     * Evalúa un cambio de stock o de mínimo y notifica si corresponde.
     * Se notifica cuando el producto pasa a estar bajo el mínimo, o cuando
     * ya lo estaba y su stock sigue bajando. No se notifica en reposiciones.
     *
     * @return true si se notificó a los observadores
     */
    public boolean evaluarCambio(Producto producto, int stockAnterior, int minimoAnterior) {
        boolean estabaBajo = stockAnterior < minimoAnterior;
        boolean estaBajo = producto.estaBajoMinimo();
        boolean sigueBajando = producto.getStock() < stockAnterior;
        if (estaBajo && (!estabaBajo || sigueBajando)) {
            notificar(new EventoStockBajo(producto.getId(), producto.getCodigo(), producto.getNombre(),
                    producto.getStock(), producto.getStockMinimo(), LocalDateTime.now(reloj)));
            return true;
        }
        return false;
    }

    public int cantidadObservadores() {
        return observadores.size();
    }
}
