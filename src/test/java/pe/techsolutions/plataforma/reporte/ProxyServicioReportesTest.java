package pe.techsolutions.plataforma.reporte;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.techsolutions.plataforma.dominio.modelo.Pedido;
import pe.techsolutions.plataforma.dominio.modelo.Rol;
import pe.techsolutions.plataforma.seguridad.AccesoDenegadoException;
import pe.techsolutions.plataforma.seguridad.UsuarioSesion;
import pe.techsolutions.plataforma.soporte.Datos;
import pe.techsolutions.plataforma.soporte.RelojAjustable;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Proxy: control de acceso, carga perezosa y caché de reportes")
class ProxyServicioReportesTest {

    private UsuarioSesion usuarioActual;
    private boolean credencialesValidas;
    private AtomicInteger instanciasReales;
    private RelojAjustable reloj;
    private ProxyServicioReportes proxy;

    @BeforeEach
    void preparar() {
        credencialesValidas = true;
        instanciasReales = new AtomicInteger();
        reloj = new RelojAjustable(Instant.parse("2026-11-18T15:00:00Z"));

        List<Pedido> pedidos = new ArrayList<>();
        Pedido procesado = new Pedido("Cliente A", "YAPE", "987654321");
        procesado.agregarItem(Datos.producto(1, "Arroz", "20.00", 50, 5), 5, new BigDecimal("20.00"));
        procesado.procesar("TX-1");
        pedidos.add(procesado);
        pedidos.add(new Pedido("Cliente B", "PLIN", "912345678"));

        FuenteDatosReporte fuente = () -> pedidos;
        proxy = new ProxyServicioReportes(
                () -> Optional.ofNullable(usuarioActual),
                u -> credencialesValidas,
                () -> {
                    instanciasReales.incrementAndGet();
                    return new ServicioReportesReal(fuente, reloj);
                },
                reloj, Duration.ofSeconds(60));
    }

    private UsuarioSesion usuario(Rol rol) {
        return new UsuarioSesion(1L, rol.name().toLowerCase(), "Usuario " + rol, rol);
    }

    @Test
    @DisplayName("RF4: Gerente y Contador obtienen el reporte completo")
    void gerenteYContadorAcceden() {
        usuarioActual = usuario(Rol.GERENTE);
        ReporteFinanciero reporte = proxy.reporteCompleto();
        assertEquals("100.00", Datos.dinero(reporte.ingresosTotales()));
        assertEquals(1, reporte.pedidosProcesados());
        assertEquals(1, reporte.pedidosPendientes());

        usuarioActual = usuario(Rol.CONTADOR);
        assertEquals(1, proxy.reporteCompleto().pedidosProcesados());
    }

    @Test
    @DisplayName("RF4: Vendedor, Compras y Administrador reciben acceso denegado")
    void otrosRolesDenegados() {
        for (Rol rol : List.of(Rol.VENDEDOR, Rol.COMPRAS, Rol.ADMINISTRADOR)) {
            usuarioActual = usuario(rol);
            assertThrows(AccesoDenegadoException.class, () -> proxy.reporteCompleto());
        }
        assertTrue(proxy.auditoria().get(0).resultado().startsWith("DENEGADO"));
    }

    @Test
    @DisplayName("RF3: sin sesión o con credenciales inválidas se deniega aunque el rol sea Gerente")
    void validaCredenciales() {
        usuarioActual = null;
        assertThrows(AccesoDenegadoException.class, () -> proxy.reporteCompleto());

        usuarioActual = usuario(Rol.GERENTE);
        credencialesValidas = false;
        assertThrows(AccesoDenegadoException.class, () -> proxy.reporteCompleto());
        assertEquals("DENEGADO (credenciales inválidas)", proxy.auditoria().get(0).resultado());
    }

    @Test
    @DisplayName("Carga perezosa: el objeto real no se crea hasta el primer acceso autorizado")
    void cargaPerezosa() {
        assertFalse(proxy.isObjetoRealCreado());
        usuarioActual = usuario(Rol.VENDEDOR);
        assertThrows(AccesoDenegadoException.class, () -> proxy.reporteCompleto());
        assertEquals(0, instanciasReales.get());

        usuarioActual = usuario(Rol.GERENTE);
        proxy.reporteCompleto();
        proxy.invalidarCache();
        proxy.reporteCompleto();
        assertTrue(proxy.isObjetoRealCreado());
        assertEquals(1, instanciasReales.get());
    }

    @Test
    @DisplayName("Caché: reutiliza el reporte durante el TTL y lo recalcula al vencer")
    void cache() {
        usuarioActual = usuario(Rol.GERENTE);
        ReporteFinanciero primero = proxy.reporteCompleto();
        assertFalse(proxy.isUltimoDesdeCache());

        reloj.avanzar(Duration.ofSeconds(30));
        assertSame(primero, proxy.reporteCompleto());
        assertTrue(proxy.isUltimoDesdeCache());

        reloj.avanzar(Duration.ofSeconds(31));
        assertNotSame(primero, proxy.reporteCompleto());
        assertFalse(proxy.isUltimoDesdeCache());
    }

    @Test
    @DisplayName("El resumen operativo (sin montos) está disponible para cualquier usuario válido")
    void resumenParaTodos() {
        usuarioActual = usuario(Rol.VENDEDOR);
        ResumenOperativo resumen = proxy.resumenOperativo();
        assertEquals(1, resumen.pedidosProcesados());
        assertEquals(1, resumen.pedidosPendientes());
    }
}
