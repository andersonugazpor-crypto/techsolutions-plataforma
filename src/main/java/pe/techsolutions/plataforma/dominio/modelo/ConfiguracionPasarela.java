package pe.techsolutions.plataforma.dominio.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** RF2: estado habilitado/deshabilitado de cada pasarela de pago. */
@Entity
@Table(name = "config_pasarelas")
public class ConfiguracionPasarela {

    @Id
    @Column(length = 20)
    private String codigo;

    private boolean habilitada;

    protected ConfiguracionPasarela() {
        // requerido por JPA
    }

    public ConfiguracionPasarela(String codigo, boolean habilitada) {
        this.codigo = codigo;
        this.habilitada = habilitada;
    }

    public String getCodigo() { return codigo; }
    public boolean isHabilitada() { return habilitada; }
    public void setHabilitada(boolean habilitada) { this.habilitada = habilitada; }
}
