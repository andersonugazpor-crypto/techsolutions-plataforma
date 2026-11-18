package pe.techsolutions.plataforma.dominio.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Parámetros clave-valor de la configuración del sistema (p. ej. estrategia de precios activa). */
@Entity
@Table(name = "parametros_sistema")
public class ParametroSistema {

    @Id
    @Column(length = 60)
    private String clave;

    @Column(nullable = false, length = 120)
    private String valor;

    protected ParametroSistema() {
        // requerido por JPA
    }

    public ParametroSistema(String clave, String valor) {
        this.clave = clave;
        this.valor = valor;
    }

    public String getClave() { return clave; }
    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
}
