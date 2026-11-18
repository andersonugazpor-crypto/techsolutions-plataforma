package pe.techsolutions.plataforma.pago;

/**
 * RF2: consulta y cambio del estado habilitado de cada pasarela.
 * Se implementa sobre la base de datos ({@code ConfiguracionPasarelasJpa}).
 */
public interface EstadoPasarelas {

    boolean estaHabilitada(String codigo);

    void cambiar(String codigo, boolean habilitada);
}
