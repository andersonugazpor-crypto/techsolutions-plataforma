package pe.techsolutions.plataforma.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.techsolutions.plataforma.catalogo.CatalogoProductos;
import pe.techsolutions.plataforma.catalogo.FuenteProductos;
import pe.techsolutions.plataforma.inventario.BandejaNotificaciones;
import pe.techsolutions.plataforma.inventario.NotificadorCompras;
import pe.techsolutions.plataforma.inventario.NotificadorGerencia;
import pe.techsolutions.plataforma.inventario.ObservadorInventario;
import pe.techsolutions.plataforma.inventario.PublicadorStock;
import pe.techsolutions.plataforma.inventario.RegistroLogStock;
import pe.techsolutions.plataforma.pago.EstadoPasarelas;
import pe.techsolutions.plataforma.pago.PasarelaPago;
import pe.techsolutions.plataforma.pago.PayPalAdaptador;
import pe.techsolutions.plataforma.pago.PlinAdaptador;
import pe.techsolutions.plataforma.pago.RegistroPasarelas;
import pe.techsolutions.plataforma.pago.ServicioPagos;
import pe.techsolutions.plataforma.pago.YapeAdaptador;
import pe.techsolutions.plataforma.pago.sdk.PayPalSdk;
import pe.techsolutions.plataforma.pago.sdk.PlinClient;
import pe.techsolutions.plataforma.pago.sdk.YapeApi;
import pe.techsolutions.plataforma.pedido.ReceptorPedidos;
import pe.techsolutions.plataforma.pedido.comando.FabricaComandos;
import pe.techsolutions.plataforma.pedido.comando.InvocadorComandos;
import pe.techsolutions.plataforma.pedido.comando.RegistroHistorial;
import pe.techsolutions.plataforma.pedido.memento.HistorialMementos;
import pe.techsolutions.plataforma.precio.CalculadoraPrecios;
import pe.techsolutions.plataforma.precio.ConfiguracionPrecios;
import pe.techsolutions.plataforma.precio.EstrategiaPrecio;
import pe.techsolutions.plataforma.precio.PrecioDescuentoPorcentual;
import pe.techsolutions.plataforma.precio.PrecioDinamico;
import pe.techsolutions.plataforma.precio.PrecioEstandar;
import pe.techsolutions.plataforma.reporte.FuenteDatosReporte;
import pe.techsolutions.plataforma.reporte.ProxyServicioReportes;
import pe.techsolutions.plataforma.reporte.ServicioReportesReal;
import pe.techsolutions.plataforma.seguridad.ProveedorUsuarioActual;
import pe.techsolutions.plataforma.seguridad.VerificadorCredenciales;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.util.List;

/**
 * Ensamblado de los patrones. Las clases de patrones son Java puro (sin anotaciones
 * de Spring), lo que facilita sus pruebas unitarias; aquí se conectan entre sí.
 */
@Configuration
public class PatronesConfig {

    @Bean
    public Clock reloj() {
        return Clock.system(ZoneId.of("America/Lima"));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ------------------------------------------------------------- Adapter
    @Bean
    public PasarelaPago payPal(@Value("${techsol.pagos.tipo-cambio-usd:3.75}") BigDecimal tipoCambio) {
        return new PayPalAdaptador(new PayPalSdk(), tipoCambio);
    }

    @Bean
    public PasarelaPago yape() {
        return new YapeAdaptador(new YapeApi());
    }

    @Bean
    public PasarelaPago plin() {
        return new PlinAdaptador(new PlinClient());
    }

    @Bean
    public RegistroPasarelas registroPasarelas(List<PasarelaPago> pasarelas) {
        return new RegistroPasarelas(pasarelas);
    }

    @Bean
    public ServicioPagos servicioPagos(RegistroPasarelas registro, EstadoPasarelas estadoPasarelas) {
        return new ServicioPagos(registro, estadoPasarelas);
    }

    // --------------------------------------------------------------- Proxy
    @Bean
    public ProxyServicioReportes servicioReportes(ProveedorUsuarioActual usuarioActual, VerificadorCredenciales verificador,
                                                  FuenteDatosReporte fuente, Clock reloj,
                                                  @Value("${techsol.reportes.cache-segundos:60}") long ttlSegundos) {
        return new ProxyServicioReportes(usuarioActual, verificador,
                () -> new ServicioReportesReal(fuente, reloj),   // carga perezosa: se crea en el primer acceso autorizado
                reloj, Duration.ofSeconds(ttlSegundos));
    }

    // ------------------------------------------------------------ Observer
    @Bean
    public ObservadorInventario notificadorGerencia(BandejaNotificaciones bandeja) {
        return new NotificadorGerencia(bandeja);
    }

    @Bean
    public ObservadorInventario notificadorCompras(BandejaNotificaciones bandeja) {
        return new NotificadorCompras(bandeja);
    }

    @Bean
    public ObservadorInventario registroLogStock() {
        return new RegistroLogStock();
    }

    @Bean
    public PublicadorStock publicadorStock(Clock reloj, List<ObservadorInventario> observadores) {
        PublicadorStock publicador = new PublicadorStock(reloj);
        observadores.forEach(publicador::suscribir);
        return publicador;
    }

    // ------------------------------------------------------------ Strategy
    @Bean
    public EstrategiaPrecio precioEstandar() {
        return new PrecioEstandar();
    }

    @Bean
    public EstrategiaPrecio precioDescuento() {
        return new PrecioDescuentoPorcentual();
    }

    @Bean
    public EstrategiaPrecio precioDinamico() {
        return new PrecioDinamico();
    }

    @Bean
    public CalculadoraPrecios calculadoraPrecios(List<EstrategiaPrecio> estrategias, ConfiguracionPrecios configuracion) {
        return new CalculadoraPrecios(estrategias, configuracion);
    }

    // ------------------------------------------------------------ Iterator
    @Bean
    public CatalogoProductos catalogoProductos(FuenteProductos fuente) {
        return new CatalogoProductos(fuente);
    }

    // ---------------------------------------------------- Command + Memento
    @Bean
    public HistorialMementos historialMementos(@Value("${techsol.pedidos.max-mementos:20}") int maximo) {
        return new HistorialMementos(maximo);
    }

    @Bean
    public InvocadorComandos invocadorComandos(ReceptorPedidos receptor, HistorialMementos historial,
                                               RegistroHistorial registro, Clock reloj) {
        return new InvocadorComandos(receptor, historial, registro, reloj);
    }

    @Bean
    public FabricaComandos fabricaComandos(ReceptorPedidos receptor, HistorialMementos historial) {
        return new FabricaComandos(receptor, historial);
    }
}
