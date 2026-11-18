package pe.techsolutions.plataforma.dominio.modelo;

/**
 * Roles de usuario de la plataforma.
 */
public enum Rol {
    ADMINISTRADOR("Administrador"),
    GERENTE("Gerente"),
    CONTADOR("Contador"),
    COMPRAS("Compras"),
    VENDEDOR("Vendedor");

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
