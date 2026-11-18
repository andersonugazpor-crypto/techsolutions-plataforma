package pe.techsolutions.plataforma.reporte;

import java.util.List;

/** Información de control que expone el Proxy de reportes (auditoría y estado de la caché). */
public interface AuditoriaReportes {

    List<RegistroAcceso> auditoria();

    boolean isUltimoDesdeCache();

    boolean isObjetoRealCreado();

    long getSegundosTtl();

    void invalidarCache();
}
