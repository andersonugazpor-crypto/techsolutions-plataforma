package pe.techsolutions.plataforma.inventario;

import org.springframework.stereotype.Component;
import pe.techsolutions.plataforma.dominio.modelo.Notificacion;
import pe.techsolutions.plataforma.dominio.modelo.Rol;
import pe.techsolutions.plataforma.repositorio.NotificacionRepositorio;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
public class BandejaNotificacionesJpa implements BandejaNotificaciones {

    private final NotificacionRepositorio repositorio;
    private final Clock reloj;

    public BandejaNotificacionesJpa(NotificacionRepositorio repositorio, Clock reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    @Override
    public void enviar(Rol rolDestino, String titulo, String mensaje) {
        repositorio.save(new Notificacion(rolDestino, titulo, mensaje, LocalDateTime.now(reloj)));
    }
}
