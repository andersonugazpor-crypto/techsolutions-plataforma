package pe.techsolutions.plataforma.inventario;

import pe.techsolutions.plataforma.dominio.modelo.Rol;

/** Destino de las notificaciones internas por rol (implementado con JPA). */
public interface BandejaNotificaciones {

    void enviar(Rol rolDestino, String titulo, String mensaje);
}
