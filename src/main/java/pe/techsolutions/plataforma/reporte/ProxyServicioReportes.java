package pe.techsolutions.plataforma.reporte;

import pe.techsolutions.plataforma.dominio.modelo.Rol;
import pe.techsolutions.plataforma.seguridad.AccesoDenegadoException;
import pe.techsolutions.plataforma.seguridad.ProveedorUsuarioActual;
import pe.techsolutions.plataforma.seguridad.UsuarioSesion;
import pe.techsolutions.plataforma.seguridad.VerificadorCredenciales;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Patrón Proxy — sustituto inteligente de {@link ServicioReportesReal}.
 * <ul>
 *   <li><b>Proxy de protección</b> (RF3, RF4): valida credenciales vigentes y que el rol sea
 *       Gerente o Contador antes de entregar el reporte completo.</li>
 *   <li><b>Proxy virtual</b>: el objeto real se crea de forma perezosa en el primer acceso autorizado.</li>
 *   <li><b>Proxy de caché</b>: reutiliza el último reporte durante un tiempo de vida (TTL).</li>
 *   <li><b>Auditoría</b>: registra cada intento de acceso.</li>
 * </ul>
 * Implementa la misma interfaz que el objeto real, por lo que el cliente no nota la diferencia.
 */
public class ProxyServicioReportes implements ServicioReportes, AuditoriaReportes {

    private static final Set<Rol> ROLES_REPORTE_COMPLETO = EnumSet.of(Rol.GERENTE, Rol.CONTADOR);
    private static final int MAX_AUDITORIA = 50;

    private final ProveedorUsuarioActual usuarioActual;
    private final VerificadorCredenciales verificador;
    private final Supplier<ServicioReportes> fabricaReal;
    private final Clock reloj;
    private final Duration ttlCache;

    private volatile ServicioReportes real;
    private ReporteFinanciero reporteEnCache;
    private Instant cacheVigenteHasta = Instant.MIN;
    private boolean ultimoDesdeCache;
    private final Deque<RegistroAcceso> auditoria = new ArrayDeque<>();

    public ProxyServicioReportes(ProveedorUsuarioActual usuarioActual, VerificadorCredenciales verificador,
                                 Supplier<ServicioReportes> fabricaReal, Clock reloj, Duration ttlCache) {
        this.usuarioActual = usuarioActual;
        this.verificador = verificador;
        this.fabricaReal = fabricaReal;
        this.reloj = reloj;
        this.ttlCache = ttlCache;
    }

    @Override
    public synchronized ReporteFinanciero reporteCompleto() {
        UsuarioSesion usuario = autenticar("Reporte completo");
        if (!ROLES_REPORTE_COMPLETO.contains(usuario.rol())) {
            auditar(usuario, "Reporte completo", "DENEGADO (rol " + usuario.rol().getEtiqueta() + ")");
            throw new AccesoDenegadoException("Solo los roles Gerente y Contador pueden ver el reporte financiero completo");
        }
        Instant ahora = reloj.instant();
        if (reporteEnCache != null && ahora.isBefore(cacheVigenteHasta)) {
            ultimoDesdeCache = true;
            auditar(usuario, "Reporte completo", "PERMITIDO (desde caché)");
            return reporteEnCache;
        }
        reporteEnCache = objetoReal().reporteCompleto();
        cacheVigenteHasta = ahora.plus(ttlCache);
        ultimoDesdeCache = false;
        auditar(usuario, "Reporte completo", "PERMITIDO (generado)");
        return reporteEnCache;
    }

    @Override
    public synchronized ResumenOperativo resumenOperativo() {
        UsuarioSesion usuario = autenticar("Resumen operativo");
        auditar(usuario, "Resumen operativo", "PERMITIDO");
        return objetoReal().resumenOperativo();
    }

    /** Fuerza que el próximo reporte se recalcule. */
    @Override
    public synchronized void invalidarCache() {
        reporteEnCache = null;
        cacheVigenteHasta = Instant.MIN;
    }

    @Override
    public synchronized boolean isUltimoDesdeCache() {
        return ultimoDesdeCache;
    }

    @Override
    public boolean isObjetoRealCreado() {
        return real != null;
    }

    @Override
    public long getSegundosTtl() {
        return ttlCache.getSeconds();
    }

    @Override
    public synchronized List<RegistroAcceso> auditoria() {
        return List.copyOf(auditoria);
    }

    // ------------------------------------------------------------ internos

    private UsuarioSesion autenticar(String operacion) {
        Optional<UsuarioSesion> sesion = usuarioActual.actual();
        if (sesion.isEmpty()) {
            auditar(null, operacion, "DENEGADO (sin sesión)");
            throw new AccesoDenegadoException("Debe iniciar sesión para consultar reportes");
        }
        UsuarioSesion usuario = sesion.get();
        if (!verificador.esValido(usuario)) {
            auditar(usuario, operacion, "DENEGADO (credenciales inválidas)");
            throw new AccesoDenegadoException("Sus credenciales ya no son válidas. Inicie sesión nuevamente");
        }
        return usuario;
    }

    /** Carga perezosa del objeto real (doble verificación). */
    private ServicioReportes objetoReal() {
        ServicioReportes instancia = real;
        if (instancia == null) {
            synchronized (this) {
                instancia = real;
                if (instancia == null) {
                    instancia = fabricaReal.get();
                    real = instancia;
                }
            }
        }
        return instancia;
    }

    private void auditar(UsuarioSesion usuario, String operacion, String resultado) {
        if (auditoria.size() >= MAX_AUDITORIA) {
            auditoria.removeLast();
        }
        auditoria.addFirst(new RegistroAcceso(LocalDateTime.now(reloj),
                usuario == null ? "anónimo" : usuario.username(),
                usuario == null ? "-" : usuario.rol().getEtiqueta(),
                operacion, resultado));
    }
}
