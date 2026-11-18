package pe.techsolutions.plataforma.inventario;

import java.time.LocalDateTime;

/** Información que el sujeto envía a los observadores cuando un producto cae bajo su mínimo. */
public record EventoStockBajo(Long productoId, String codigo, String nombre, int stockActual,
                              int stockMinimo, LocalDateTime fecha) {

    /** Cantidad sugerida para volver al doble del mínimo. */
    public int unidadesSugeridas() {
        return Math.max(stockMinimo * 2 - stockActual, stockMinimo);
    }
}
