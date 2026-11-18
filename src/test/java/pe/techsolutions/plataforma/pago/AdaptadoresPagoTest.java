package pe.techsolutions.plataforma.pago;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.techsolutions.plataforma.dominio.modelo.OperacionNoPermitidaException;
import pe.techsolutions.plataforma.pago.sdk.PayPalSdk;
import pe.techsolutions.plataforma.pago.sdk.PlinClient;
import pe.techsolutions.plataforma.pago.sdk.YapeApi;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Adapter: pasarelas de pago PayPal, Yape y Plin")
class AdaptadoresPagoTest {

    /** Estado de pasarelas en memoria para no depender de la base de datos. */
    static class EstadoEnMemoria implements EstadoPasarelas {
        final Map<String, Boolean> estados = new HashMap<>();
        @Override public boolean estaHabilitada(String codigo) { return estados.getOrDefault(codigo, false); }
        @Override public void cambiar(String codigo, boolean habilitada) { estados.put(codigo, habilitada); }
    }

    private EstadoEnMemoria estado;
    private ServicioPagos servicio;

    @BeforeEach
    void preparar() {
        estado = new EstadoEnMemoria();
        List<PasarelaPago> adaptadores = List.of(
                new PayPalAdaptador(new PayPalSdk(), new BigDecimal("3.75")),
                new YapeAdaptador(new YapeApi()),
                new PlinAdaptador(new PlinClient()));
        adaptadores.forEach(p -> estado.cambiar(p.codigo(), true));
        servicio = new ServicioPagos(new RegistroPasarelas(adaptadores), estado);
    }

    private SolicitudPago solicitud(String monto, String contacto) {
        return new SolicitudPago("PED-1", new BigDecimal(monto), "Cliente", contacto);
    }

    @Test
    @DisplayName("Los tres adaptadores aprueban con la misma interfaz y respuesta uniforme")
    void interfazUniforme() {
        ResultadoPago paypal = servicio.cobrar("PAYPAL", solicitud("150.00", "cliente@correo.pe"));
        ResultadoPago yape = servicio.cobrar("YAPE", solicitud("150.00", "987654321"));
        ResultadoPago plin = servicio.cobrar("PLIN", solicitud("150.00", "912345678"));

        assertTrue(paypal.aprobado());
        assertTrue(paypal.codigoTransaccion().startsWith("PAYID-"));
        assertTrue(yape.aprobado());
        assertTrue(yape.codigoTransaccion().startsWith("YAPE-"));
        assertTrue(plin.aprobado());
        assertTrue(plin.codigoTransaccion().startsWith("PLN-"));
    }

    @Test
    @DisplayName("PayPal: el adaptador convierte soles a dólares antes de llamar al SDK")
    void payPalConvierteMoneda() {
        double[] montoRecibido = new double[1];
        String[] moneda = new String[1];
        PayPalSdk sdkEspia = new PayPalSdk() {
            @Override
            public PayPalResponse createPayment(String email, double amount, String currencyCode) {
                montoRecibido[0] = amount;
                moneda[0] = currencyCode;
                return super.createPayment(email, amount, currencyCode);
            }
        };
        new PayPalAdaptador(sdkEspia, new BigDecimal("3.75")).pagar(solicitud("375.00", "a@b.pe"));
        assertEquals(100.0, montoRecibido[0], 0.001);
        assertEquals("USD", moneda[0]);
    }

    @Test
    @DisplayName("Yape: traduce sus códigos numéricos de error a mensajes")
    void yapeTraduceErrores() {
        ResultadoPago celularInvalido = servicio.cobrar("YAPE", solicitud("50.00", "12345"));
        ResultadoPago excedeLimite = servicio.cobrar("YAPE", solicitud("2500.00", "987654321"));
        assertFalse(celularInvalido.aprobado());
        assertTrue(celularInvalido.mensaje().contains("celular"));
        assertFalse(excedeLimite.aprobado());
        assertTrue(excedeLimite.mensaje().contains("límite"));
    }

    @Test
    @DisplayName("RF2: una pasarela deshabilitada no aparece ni puede cobrar")
    void pasarelaDeshabilitada() {
        estado.cambiar("PLIN", false);
        assertEquals(2, servicio.pasarelasHabilitadas().size());
        assertThrows(OperacionNoPermitidaException.class, () -> servicio.cobrar("PLIN", solicitud("10.00", "912345678")));
        assertThrows(OperacionNoPermitidaException.class, () -> servicio.validarDisponible("PLIN"));
    }

    @Test
    @DisplayName("Pasarela desconocida produce error de negocio")
    void pasarelaDesconocida() {
        assertThrows(OperacionNoPermitidaException.class, () -> servicio.cobrar("BITCOIN", solicitud("10.00", "x")));
    }

    @Test
    @DisplayName("Extensibilidad: una pasarela nueva se integra sin modificar ServicioPagos")
    void nuevaPasarelaSinCambios() {
        PasarelaPago tunki = new PasarelaPago() {
            @Override public String codigo() { return "TUNKI"; }
            @Override public String nombre() { return "Tunki"; }
            @Override public String tipoContacto() { return "celular"; }
            @Override public ResultadoPago pagar(SolicitudPago s) { return ResultadoPago.aprobado(codigo(), "TNK-1"); }
        };
        EstadoEnMemoria estados = new EstadoEnMemoria();
        estados.cambiar("TUNKI", true);
        ServicioPagos conTunki = new ServicioPagos(new RegistroPasarelas(List.of(tunki)), estados);
        assertEquals("TNK-1", conTunki.cobrar("TUNKI", solicitud("20.00", "999888777")).codigoTransaccion());
    }
}
