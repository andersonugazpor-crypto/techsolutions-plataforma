package pe.techsolutions.plataforma.inventario;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.techsolutions.plataforma.dominio.modelo.Producto;
import pe.techsolutions.plataforma.dominio.modelo.Rol;
import pe.techsolutions.plataforma.soporte.Datos;
import pe.techsolutions.plataforma.soporte.RelojAjustable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Observer: notificaciones de stock bajo a Gerencia y Compras")
class PublicadorStockTest {

    /** Bandeja en memoria que captura los mensajes enviados por los observadores. */
    static class BandejaEnMemoria implements BandejaNotificaciones {
        final List<String> enviados = new ArrayList<>();
        @Override public void enviar(Rol rol, String titulo, String mensaje) { enviados.add(rol + "|" + titulo + "|" + mensaje); }
    }

    private BandejaEnMemoria bandeja;
    private PublicadorStock publicador;

    @BeforeEach
    void preparar() {
        bandeja = new BandejaEnMemoria();
        publicador = new PublicadorStock(new RelojAjustable(Instant.parse("2026-11-18T15:00:00Z")));
        publicador.suscribir(new NotificadorGerencia(bandeja));
        publicador.suscribir(new NotificadorCompras(bandeja));
    }

    private boolean vender(Producto p, int cantidad) {
        int stock = p.getStock();
        int minimo = p.getStockMinimo();
        p.descontarStock(cantidad);
        return publicador.evaluarCambio(p, stock, minimo);
    }

    @Test
    @DisplayName("RF5: al caer bajo el mínimo se notifica a Gerente y a Compras (uno a muchos)")
    void notificaAlCruzarMinimo() {
        Producto atun = Datos.producto(1, "Atún Florida", "6.20", 16, 15);
        assertTrue(vender(atun, 3));
        assertEquals(2, bandeja.enviados.size());
        assertTrue(bandeja.enviados.get(0).startsWith("GERENTE|Stock bajo: Atún Florida"));
        assertTrue(bandeja.enviados.get(1).startsWith("COMPRAS|Reponer: Atún Florida"));
        assertTrue(bandeja.enviados.get(1).contains("17 unidades")); // 15*2 - 13
    }

    @Test
    @DisplayName("No notifica si el stock sigue por encima del mínimo")
    void noNotificaSobreMinimo() {
        Producto arroz = Datos.producto(1, "Arroz", "24.90", 80, 20);
        assertFalse(vender(arroz, 10));
        assertTrue(bandeja.enviados.isEmpty());
    }

    @Test
    @DisplayName("No notifica en reposiciones aunque el producto siga bajo el mínimo")
    void noNotificaEnReposicion() {
        Producto jugo = Datos.producto(1, "Jugo", "5.40", 5, 12);
        int stock = jugo.getStock();
        jugo.reponerStock(3);
        assertFalse(publicador.evaluarCambio(jugo, stock, 12));
    }

    @Test
    @DisplayName("RF6: subir el mínimo por encima del stock también dispara la alerta")
    void notificaAlSubirMinimo() {
        Producto cafe = Datos.producto(1, "Café", "18.90", 25, 8);
        cafe.configurarStockMinimo(30);
        assertTrue(publicador.evaluarCambio(cafe, 25, 8));
    }

    @Test
    @DisplayName("Un observador desuscrito deja de recibir avisos")
    void desuscribir() {
        List<EventoStockBajo> recibidos = new ArrayList<>();
        ObservadorInventario espia = recibidos::add;
        publicador.suscribir(espia);
        assertEquals(3, publicador.cantidadObservadores());
        publicador.desuscribir(espia);
        vender(Datos.producto(1, "Atún", "6.20", 16, 15), 3);
        assertTrue(recibidos.isEmpty());
        assertEquals(2, bandeja.enviados.size());
    }
}
