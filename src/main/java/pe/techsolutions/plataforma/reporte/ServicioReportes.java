package pe.techsolutions.plataforma.reporte;

/**
 * Patrón Proxy — interfaz común (Subject) del objeto real y de su sustituto.
 * El controlador web solo conoce esta interfaz, por lo que recibe el Proxy
 * sin saberlo.
 */
public interface ServicioReportes {

    /** Reporte financiero completo con montos (información sensible). */
    ReporteFinanciero reporteCompleto();

    /** Resumen operativo sin montos, apto para cualquier usuario autenticado. */
    ResumenOperativo resumenOperativo();
}
