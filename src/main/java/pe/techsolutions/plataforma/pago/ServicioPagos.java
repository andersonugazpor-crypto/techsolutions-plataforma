package pe.techsolutions.plataforma.pago;

import pe.techsolutions.plataforma.dominio.modelo.OperacionNoPermitidaException;

import java.util.List;

/**
 * Cliente del patrón Adapter: cobra a través de {@link PasarelaPago} sin conocer
 * PayPal, Yape ni Plin. Respeta la configuración de pasarelas habilitadas (RF2).
 */
public class ServicioPagos {

    private final RegistroPasarelas registro;
    private final EstadoPasarelas estadoPasarelas;

    public ServicioPagos(RegistroPasarelas registro, EstadoPasarelas estadoPasarelas) {
        this.registro = registro;
        this.estadoPasarelas = estadoPasarelas;
    }

    public List<PasarelaPago> pasarelasHabilitadas() {
        return registro.todas().stream().filter(p -> estadoPasarelas.estaHabilitada(p.codigo())).toList();
    }

    public List<PasarelaPago> todasLasPasarelas() {
        return List.copyOf(registro.todas());
    }

    public void validarDisponible(String codigoPasarela) {
        obtenerHabilitada(codigoPasarela);
    }

    public ResultadoPago cobrar(String codigoPasarela, SolicitudPago solicitud) {
        return obtenerHabilitada(codigoPasarela).pagar(solicitud);
    }

    public String nombreDe(String codigoPasarela) {
        return registro.buscar(codigoPasarela).map(PasarelaPago::nombre).orElse(codigoPasarela);
    }

    private PasarelaPago obtenerHabilitada(String codigo) {
        PasarelaPago pasarela = registro.buscar(codigo)
                .orElseThrow(() -> new OperacionNoPermitidaException("Pasarela de pago desconocida: " + codigo));
        if (!estadoPasarelas.estaHabilitada(codigo)) {
            throw new OperacionNoPermitidaException("La pasarela " + pasarela.nombre() + " está deshabilitada");
        }
        return pasarela;
    }
}
