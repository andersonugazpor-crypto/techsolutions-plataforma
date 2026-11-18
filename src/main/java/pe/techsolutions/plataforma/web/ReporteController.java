package pe.techsolutions.plataforma.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.techsolutions.plataforma.reporte.AuditoriaReportes;
import pe.techsolutions.plataforma.reporte.ReporteFinanciero;
import pe.techsolutions.plataforma.reporte.ServicioReportes;
import pe.techsolutions.plataforma.seguridad.AccesoDenegadoException;
import pe.techsolutions.plataforma.seguridad.ProveedorUsuarioActual;
import pe.techsolutions.plataforma.seguridad.UsuarioSesion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cliente del Proxy: pide los reportes a {@link ServicioReportes} sin saber
 * que en realidad habla con el Proxy, que decide si permite el acceso.
 */
@Controller
public class ReporteController {

    private final ServicioReportes servicioReportes;
    private final AuditoriaReportes auditoria;
    private final ProveedorUsuarioActual usuarioActual;

    public ReporteController(ServicioReportes servicioReportes, AuditoriaReportes auditoria,
                             ProveedorUsuarioActual usuarioActual) {
        this.servicioReportes = servicioReportes;
        this.auditoria = auditoria;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping("/reportes")
    public String reportes(Model model) {
        try {
            ReporteFinanciero reporte = servicioReportes.reporteCompleto();
            model.addAttribute("reporte", reporte);
            model.addAttribute("desdeCache", auditoria.isUltimoDesdeCache());
            model.addAttribute("barrasPasarela", porcentajes(reporte.ventasPorPasarela()));
            model.addAttribute("barrasCategoria", porcentajes(reporte.ventasPorCategoria()));
        } catch (AccesoDenegadoException e) {
            model.addAttribute("denegado", e.getMessage());
            model.addAttribute("resumen", servicioReportes.resumenOperativo());
        }
        UsuarioSesion usuario = usuarioActual.actual().orElse(null);
        model.addAttribute("verAuditoria", usuario != null
                && (usuario.esAdministrador() || Permisos.tiene(usuario, Permisos.VEN_REPORTE_COMPLETO)));
        model.addAttribute("auditoria", auditoria.auditoria());
        model.addAttribute("ttl", auditoria.getSegundosTtl());
        model.addAttribute("objetoRealCreado", auditoria.isObjetoRealCreado());
        return "reportes";
    }

    @PostMapping("/reportes/regenerar")
    public String regenerar(RedirectAttributes redirect) {
        Permisos.exigir(usuarioActual.actual().orElse(null), Permisos.VEN_REPORTE_COMPLETO, "regenerar reportes");
        auditoria.invalidarCache();
        redirect.addFlashAttribute("exito", "Caché invalidada: el reporte se recalculó con los datos actuales");
        return "redirect:/reportes";
    }

    /** Convierte montos en porcentajes respecto al mayor, para dibujar barras horizontales. */
    static Map<String, Integer> porcentajes(Map<String, BigDecimal> montos) {
        BigDecimal maximo = montos.values().stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        Map<String, Integer> resultado = new LinkedHashMap<>();
        montos.forEach((clave, valor) -> resultado.put(clave, maximo.signum() == 0 ? 0
                : valor.multiply(BigDecimal.valueOf(100)).divide(maximo, 0, RoundingMode.HALF_UP).intValue()));
        return resultado;
    }
}
