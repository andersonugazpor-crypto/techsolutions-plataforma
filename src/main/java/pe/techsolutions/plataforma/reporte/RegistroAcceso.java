package pe.techsolutions.plataforma.reporte;

import java.time.LocalDateTime;

/** Entrada de auditoría generada por el Proxy en cada intento de acceso. */
public record RegistroAcceso(LocalDateTime fecha, String usuario, String rol, String operacion, String resultado) {
}
