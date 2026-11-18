package pe.techsolutions.plataforma.pedido.comando;

import java.time.LocalDateTime;

/** Solicitud en espera dentro de la cola del invocador. */
public record ComandoEncolado(ComandoPedido comando, String usuario, LocalDateTime fechaEncolado) {

    public String getDescripcion() {
        return comando.descripcion();
    }

    public String getUsuario() {
        return usuario;
    }

    public LocalDateTime getFechaEncolado() {
        return fechaEncolado;
    }
}
