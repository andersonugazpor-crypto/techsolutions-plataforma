package pe.techsolutions.plataforma.web;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;
import pe.techsolutions.plataforma.dominio.modelo.OperacionNoPermitidaException;
import pe.techsolutions.plataforma.inventario.ServicioNotificaciones;
import pe.techsolutions.plataforma.seguridad.AccesoDenegadoException;
import pe.techsolutions.plataforma.seguridad.ProveedorUsuarioActual;
import pe.techsolutions.plataforma.seguridad.UsuarioSesion;

/** Datos comunes a todas las vistas y manejo centralizado de errores de negocio. */
@ControllerAdvice
public class ModeloGlobal {

    private final ProveedorUsuarioActual usuarioActual;
    private final ServicioNotificaciones notificaciones;

    public ModeloGlobal(ProveedorUsuarioActual usuarioActual, ServicioNotificaciones notificaciones) {
        this.usuarioActual = usuarioActual;
        this.notificaciones = notificaciones;
    }

    @ModelAttribute("usuario")
    public UsuarioSesion usuario() {
        return usuarioActual.actual().orElse(null);
    }

    @ModelAttribute("notificacionesPendientes")
    public long notificacionesPendientes() {
        return usuarioActual.actual().map(notificaciones::pendientesPara).orElse(0L);
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String accesoDenegado(AccesoDenegadoException e, Model model) {
        return vistaError(model, "Acceso denegado", e.getMessage());
    }

    @ExceptionHandler(OperacionNoPermitidaException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String operacionNoPermitida(OperacionNoPermitidaException e, Model model) {
        return vistaError(model, "Operación no permitida", e.getMessage());
    }

    private String vistaError(Model model, String titulo, String mensaje) {
        model.addAttribute("usuario", usuario());
        model.addAttribute("notificacionesPendientes", notificacionesPendientes());
        model.addAttribute("titulo", titulo);
        model.addAttribute("mensaje", mensaje);
        return "error-negocio";
    }
}
