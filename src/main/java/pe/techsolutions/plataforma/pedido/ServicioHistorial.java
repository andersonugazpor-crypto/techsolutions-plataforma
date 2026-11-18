package pe.techsolutions.plataforma.pedido;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.techsolutions.plataforma.dominio.modelo.RegistroComando;
import pe.techsolutions.plataforma.repositorio.RegistroComandoRepositorio;

import java.util.List;

/** Consultas del historial de comandos (RF7). */
@Service
@Transactional(readOnly = true)
public class ServicioHistorial {

    private final RegistroComandoRepositorio repositorio;

    public ServicioHistorial(RegistroComandoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public List<RegistroComando> dePedido(Long pedidoId) {
        return repositorio.findByPedidoIdOrderByIdDesc(pedidoId);
    }

    public List<RegistroComando> recientes() {
        return repositorio.findTop30ByOrderByIdDesc();
    }
}
