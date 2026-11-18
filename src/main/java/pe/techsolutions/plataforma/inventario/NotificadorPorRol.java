package pe.techsolutions.plataforma.inventario;

import pe.techsolutions.plataforma.dominio.modelo.Rol;

/**
 * Observador concreto base: envía una notificación a la bandeja de un rol.
 * Las subclases definen el rol destino y el contenido (método plantilla).
 */
public abstract class NotificadorPorRol implements ObservadorInventario {

    private final BandejaNotificaciones bandeja;

    protected NotificadorPorRol(BandejaNotificaciones bandeja) {
        this.bandeja = bandeja;
    }

    @Override
    public final void actualizar(EventoStockBajo evento) {
        bandeja.enviar(rolDestino(), titulo(evento), mensaje(evento));
    }

    protected abstract Rol rolDestino();

    protected String titulo(EventoStockBajo evento) {
        return "Stock bajo: " + evento.nombre();
    }

    protected abstract String mensaje(EventoStockBajo evento);
}
