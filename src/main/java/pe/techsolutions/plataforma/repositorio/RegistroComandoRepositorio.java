package pe.techsolutions.plataforma.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.techsolutions.plataforma.dominio.modelo.RegistroComando;

import java.util.List;

public interface RegistroComandoRepositorio extends JpaRepository<RegistroComando, Long> {

    List<RegistroComando> findByPedidoIdOrderByIdDesc(Long pedidoId);

    List<RegistroComando> findTop30ByOrderByIdDesc();
}
