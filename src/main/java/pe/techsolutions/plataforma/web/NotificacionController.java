package pe.techsolutions.plataforma.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import pe.techsolutions.plataforma.inventario.ServicioNotificaciones;
import pe.techsolutions.plataforma.seguridad.ProveedorUsuarioActual;
import pe.techsolutions.plataforma.seguridad.UsuarioSesion;

@Controller
public class NotificacionController {

    private final ServicioNotificaciones notificaciones;
    private final ProveedorUsuarioActual usuarioActual;

    public NotificacionController(ServicioNotificaciones notificaciones, ProveedorUsuarioActual usuarioActual) {
        this.notificaciones = notificaciones;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping("/notificaciones")
    public String listar(Model model) {
        model.addAttribute("notificaciones", notificaciones.listarPara(usuario()));
        return "notificaciones";
    }

    @PostMapping("/notificaciones/{id}/leer")
    public String leer(@PathVariable Long id) {
        notificaciones.marcarLeida(id, usuario());
        return "redirect:/notificaciones";
    }

    @PostMapping("/notificaciones/leer-todas")
    public String leerTodas() {
        notificaciones.marcarTodas(usuario());
        return "redirect:/notificaciones";
    }

    private UsuarioSesion usuario() {
        return usuarioActual.actual().orElseThrow();
    }
}
