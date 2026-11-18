package pe.techsolutions.plataforma.web;

/** Fila del panel de configuración de pasarelas. */
public class PasarelaVista {

    private final String codigo;
    private final String nombre;
    private final String tipoContacto;
    private final boolean habilitada;

    public PasarelaVista(String codigo, String nombre, String tipoContacto, boolean habilitada) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.tipoContacto = tipoContacto;
        this.habilitada = habilitada;
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getTipoContacto() { return tipoContacto; }
    public boolean isHabilitada() { return habilitada; }
}
