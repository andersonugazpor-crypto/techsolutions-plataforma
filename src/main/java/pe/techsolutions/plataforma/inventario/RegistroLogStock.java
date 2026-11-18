package pe.techsolutions.plataforma.inventario;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Observador concreto: deja constancia en el log de la aplicación. */
public class RegistroLogStock implements ObservadorInventario {

    private static final Logger LOG = LoggerFactory.getLogger(RegistroLogStock.class);

    @Override
    public void actualizar(EventoStockBajo e) {
        LOG.warn("[STOCK BAJO] {} ({}) stock={} minimo={}", e.nombre(), e.codigo(), e.stockActual(), e.stockMinimo());
    }
}
