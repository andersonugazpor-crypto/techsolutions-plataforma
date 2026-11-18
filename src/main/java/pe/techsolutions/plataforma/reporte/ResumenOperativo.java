package pe.techsolutions.plataforma.reporte;

import java.time.LocalDateTime;

public record ResumenOperativo(LocalDateTime fechaGeneracion, long pedidosProcesados,
                               long pedidosPendientes, long pedidosCancelados) {
}
