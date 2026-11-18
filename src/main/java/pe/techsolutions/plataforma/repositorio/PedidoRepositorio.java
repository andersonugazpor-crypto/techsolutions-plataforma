package pe.techsolutions.plataforma.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.techsolutions.plataforma.dominio.modelo.EstadoPedidoTipo;
import pe.techsolutions.plataforma.dominio.modelo.Pedido;

import java.util.List;

public interface PedidoRepositorio extends JpaRepository<Pedido, Long> {

    List<Pedido> findAllByOrderByIdDesc();

    long countByEstado(EstadoPedidoTipo estado);
}
