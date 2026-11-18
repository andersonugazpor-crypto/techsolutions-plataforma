package pe.techsolutions.plataforma.soporte;

import pe.techsolutions.plataforma.dominio.modelo.Producto;

import java.math.BigDecimal;

/** Fábrica de objetos de prueba. */
public final class Datos {

    private Datos() {
    }

    public static Producto producto(long id, String nombre, String precio, int stock, int minimo) {
        Producto p = new Producto("P-" + id, nombre, "General", new BigDecimal(precio), stock, minimo);
        p.asignarId(id);
        return p;
    }

    public static String dinero(BigDecimal monto) {
        return monto.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
