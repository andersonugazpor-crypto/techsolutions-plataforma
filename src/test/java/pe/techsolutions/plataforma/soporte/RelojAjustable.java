package pe.techsolutions.plataforma.soporte;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

/** Reloj controlable para probar caché y temporadas sin esperar tiempo real. */
public class RelojAjustable extends Clock {

    private Instant ahora;
    private final ZoneId zona;

    public RelojAjustable(Instant inicio) {
        this.ahora = inicio;
        this.zona = ZoneId.of("America/Lima");
    }

    public void avanzar(Duration duracion) {
        ahora = ahora.plus(duracion);
    }

    @Override public ZoneId getZone() { return zona; }
    @Override public Clock withZone(ZoneId zone) { return this; }
    @Override public Instant instant() { return ahora; }
}
