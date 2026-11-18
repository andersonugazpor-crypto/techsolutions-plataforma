package pe.techsolutions.plataforma.inventario;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.techsolutions.plataforma.dominio.modelo.Notificacion;
import pe.techsolutions.plataforma.repositorio.NotificacionRepositorio;
import pe.techsolutions.plataforma.seguridad.UsuarioSesion;

import java.util.List;

/** Bandeja de notificaciones por rol. El administrador ve todas. */
@Service
@Transactional
public class ServicioNotificaciones {

    private final NotificacionRepositorio repositorio;

    public ServicioNotificaciones(NotificacionRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public List<Notificacion> listarPara(UsuarioSesion usuario) {
        return usuario.esAdministrador()
                ? repositorio.findAllByOrderByFechaDesc()
                : repositorio.findByRolDestinoOrderByFechaDesc(usuario.rol());
    }

    @Transactional(readOnly = true)
    public long pendientesPara(UsuarioSesion usuario) {
        return usuario.esAdministrador()
                ? repositorio.countByLeidaFalse()
                : repositorio.countByRolDestinoAndLeidaFalse(usuario.rol());
    }

    public void marcarLeida(Long id, UsuarioSesion usuario) {
        repositorio.findById(id)
                .filter(n -> usuario.esAdministrador() || n.getRolDestino() == usuario.rol())
                .ifPresent(n -> {
                    n.marcarLeida();
                    repositorio.save(n);
                });
    }

    public void marcarTodas(UsuarioSesion usuario) {
        listarPara(usuario).forEach(n -> {
            n.marcarLeida();
            repositorio.save(n);
        });
    }
}
