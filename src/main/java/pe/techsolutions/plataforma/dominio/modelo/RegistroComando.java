package pe.techsolutions.plataforma.dominio.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** RF7: entrada persistente del historial de comandos ejecutados sobre pedidos. */
@Entity
@Table(name = "historial_comandos")
public class RegistroComando {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long pedidoId;

    @Column(nullable = false, length = 40)
    private String comando;

    @Column(nullable = false, length = 250)
    private String descripcion;

    @Column(length = 50)
    private String usuario;

    private boolean exitoso;

    @Column(length = 300)
    private String mensaje;

    private LocalDateTime fecha;

    protected RegistroComando() {
        // requerido por JPA
    }

    public RegistroComando(Long pedidoId, String comando, String descripcion, String usuario,
                           boolean exitoso, String mensaje, LocalDateTime fecha) {
        this.pedidoId = pedidoId;
        this.comando = comando;
        this.descripcion = descripcion;
        this.usuario = usuario;
        this.exitoso = exitoso;
        this.mensaje = mensaje;
        this.fecha = fecha;
    }

    public Long getId() { return id; }
    public Long getPedidoId() { return pedidoId; }
    public String getComando() { return comando; }
    public String getDescripcion() { return descripcion; }
    public String getUsuario() { return usuario; }
    public boolean isExitoso() { return exitoso; }
    public String getMensaje() { return mensaje; }
    public LocalDateTime getFecha() { return fecha; }
}
