package pe.techsolutions.plataforma.inventario;

import pe.techsolutions.plataforma.dominio.modelo.Rol;

/** Observador concreto: alerta a Gerencia para la toma de decisiones. */
public class NotificadorGerencia extends NotificadorPorRol {

    public NotificadorGerencia(BandejaNotificaciones bandeja) {
        super(bandeja);
    }

    @Override
    protected Rol rolDestino() {
        return Rol.GERENTE;
    }

    @Override
    protected String mensaje(EventoStockBajo e) {
        return String.format("El producto %s (%s) tiene %d unidades y su mínimo es %d. Riesgo de pérdida de ventas.",
                e.nombre(), e.codigo(), e.stockActual(), e.stockMinimo());
    }
}
