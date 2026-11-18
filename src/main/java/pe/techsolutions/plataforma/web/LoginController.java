package pe.techsolutions.plataforma.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.techsolutions.plataforma.seguridad.ProveedorUsuarioActual;
import pe.techsolutions.plataforma.seguridad.ServicioAutenticacion;
import pe.techsolutions.plataforma.seguridad.UsuarioSesion;

import java.util.Optional;

@Controller
public class LoginController {

    private final ServicioAutenticacion autenticacion;

    public LoginController(ServicioAutenticacion autenticacion) {
        this.autenticacion = autenticacion;
    }

    @GetMapping("/login")
    public String formulario(HttpSession sesion) {
        if (sesion.getAttribute(ProveedorUsuarioActual.ATRIBUTO_SESION) != null) {
            return "redirect:/";
        }
        return "login";
    }

    @PostMapping("/login")
    public String ingresar(@RequestParam String username, @RequestParam String password,
                           HttpServletRequest request, Model model) {
        Optional<UsuarioSesion> usuario = autenticacion.autenticar(username, password);
        if (usuario.isEmpty()) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
            model.addAttribute("username", username);
            return "login";
        }
        HttpSession anterior = request.getSession(false);
        if (anterior != null) {
            anterior.invalidate(); // evita fijación de sesión
        }
        request.getSession(true).setAttribute(ProveedorUsuarioActual.ATRIBUTO_SESION, usuario.get());
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String salir(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        return "redirect:/login?salir";
    }

    @GetMapping("/acceso-denegado")
    public String accesoDenegado(Model model) {
        model.addAttribute("titulo", "Acceso denegado");
        model.addAttribute("mensaje", "Esta sección es exclusiva del rol Administrador.");
        return "error-negocio";
    }
}
