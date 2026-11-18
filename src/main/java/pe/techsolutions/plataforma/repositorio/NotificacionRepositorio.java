package pe.techsolutions.plataforma.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.techsolutions.plataforma.dominio.modelo.Notificacion;
import pe.techsolutions.plataforma.dominio.modelo.Rol;

import java.util.List;

public interface NotificacionRepositorio extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByRolDestinoOrderByFechaDesc(Rol rol);

    List<Notificacion> findAllByOrderByFechaDesc();

    long countByRolDestinoAndLeidaFalse(Rol rol);

    long countByLeidaFalse();
}
