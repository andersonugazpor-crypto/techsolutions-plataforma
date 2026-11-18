package pe.techsolutions.plataforma.seguridad;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/** Lee el usuario autenticado desde la sesión HTTP de la petición en curso. */
@Component
public class SesionHttpUsuarioActual implements ProveedorUsuarioActual {

    @Override
    public Optional<UsuarioSesion> actual() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes atributos)) {
            return Optional.empty();
        }
        HttpSession sesion = atributos.getRequest().getSession(false);
        if (sesion == null) {
            return Optional.empty();
        }
        Object valor = sesion.getAttribute(ATRIBUTO_SESION);
        return valor instanceof UsuarioSesion usuario ? Optional.of(usuario) : Optional.empty();
    }
}
