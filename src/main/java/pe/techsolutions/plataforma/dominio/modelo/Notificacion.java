package pe.techsolutions.plataforma.dominio.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rolDestino;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 500)
    private String mensaje;

    private LocalDateTime fecha;

    private boolean leida;

    protected Notificacion() {
        // requerido por JPA
    }

    public Notificacion(Rol rolDestino, String titulo, String mensaje, LocalDateTime fecha) {
        this.rolDestino = rolDestino;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.fecha = fecha;
    }

    public void marcarLeida() {
        this.leida = true;
    }

    public Long getId() { return id; }
    public Rol getRolDestino() { return rolDestino; }
    public String getTitulo() { return titulo; }
    public String getMensaje() { return mensaje; }
    public LocalDateTime getFecha() { return fecha; }
    public boolean isLeida() { return leida; }
}
