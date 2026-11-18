package pe.techsolutions.plataforma.reporte;

import pe.techsolutions.plataforma.dominio.modelo.Pedido;

import java.util.List;

/** Origen de los pedidos usados para calcular reportes (desacopla el reporte de JPA). */
public interface FuenteDatosReporte {

    List<Pedido> pedidos();
}
