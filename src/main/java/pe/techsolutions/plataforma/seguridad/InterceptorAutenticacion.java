package pe.techsolutions.plataforma.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Exige sesión iniciada en toda la aplicación y rol Administrador en /admin/**.
 * El acceso a reportes NO se controla aquí: lo controla el Proxy de reportes.
 */
@Component
public class InterceptorAutenticacion implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession sesion = request.getSession(false);
        Object valor = sesion == null ? null : sesion.getAttribute(ProveedorUsuarioActual.ATRIBUTO_SESION);
        if (!(valor instanceof UsuarioSesion usuario)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        if (ruta.startsWith("/admin") && !usuario.esAdministrador()) {
            response.sendRedirect(request.getContextPath() + "/acceso-denegado");
            return false;
        }
        return true;
    }
}
