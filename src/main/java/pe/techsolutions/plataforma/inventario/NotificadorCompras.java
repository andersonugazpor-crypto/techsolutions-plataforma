package pe.techsolutions.plataforma.inventario;

import pe.techsolutions.plataforma.dominio.modelo.Rol;

/** Observador concreto: indica al área de Compras cuánto reponer. */
public class NotificadorCompras extends NotificadorPorRol {

    public NotificadorCompras(BandejaNotificaciones bandeja) {
        super(bandeja);
    }

    @Override
    protected Rol rolDestino() {
        return Rol.COMPRAS;
    }

    @Override
    protected String titulo(EventoStockBajo e) {
        return "Reponer: " + e.nombre();
    }

    @Override
    protected String mensaje(EventoStockBajo e) {
        return String.format("Generar orden de compra de %d unidades de %s (%s). Stock actual: %d, mínimo: %d.",
                e.unidadesSugeridas(), e.nombre(), e.codigo(), e.stockActual(), e.stockMinimo());
    }
}
