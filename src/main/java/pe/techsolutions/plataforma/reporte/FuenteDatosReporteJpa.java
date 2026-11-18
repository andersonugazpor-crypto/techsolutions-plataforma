package pe.techsolutions.plataforma.reporte;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.techsolutions.plataforma.dominio.modelo.Pedido;
import pe.techsolutions.plataforma.repositorio.PedidoRepositorio;

import java.util.List;

@Component
public class FuenteDatosReporteJpa implements FuenteDatosReporte {

    private final PedidoRepositorio pedidos;

    public FuenteDatosReporteJpa(PedidoRepositorio pedidos) {
        this.pedidos = pedidos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> pedidos() {
        return pedidos.findAll();
    }
}
