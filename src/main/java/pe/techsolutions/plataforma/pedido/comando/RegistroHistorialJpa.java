package pe.techsolutions.plataforma.pedido.comando;

import org.springframework.stereotype.Component;
import pe.techsolutions.plataforma.dominio.modelo.RegistroComando;
import pe.techsolutions.plataforma.repositorio.RegistroComandoRepositorio;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
public class RegistroHistorialJpa implements RegistroHistorial {

    private final RegistroComandoRepositorio repositorio;
    private final Clock reloj;

    public RegistroHistorialJpa(RegistroComandoRepositorio repositorio, Clock reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    @Override
    public void registrar(ComandoPedido comando, String usuario, ResultadoComando resultado) {
        String mensaje = resultado.mensaje();
        if (mensaje != null && mensaje.length() > 300) {
            mensaje = mensaje.substring(0, 300);
        }
        repositorio.save(new RegistroComando(resultado.pedidoId() != null ? resultado.pedidoId() : comando.pedidoId(),
                comando.nombre(), comando.descripcion(), usuario, resultado.exitoso(), mensaje, LocalDateTime.now(reloj)));
    }
}
