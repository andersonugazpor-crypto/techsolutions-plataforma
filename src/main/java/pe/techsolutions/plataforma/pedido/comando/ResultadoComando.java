package pe.techsolutions.plataforma.pedido.comando;

public record ResultadoComando(boolean exitoso, String mensaje, Long pedidoId) {

    public static ResultadoComando ok(String mensaje, Long pedidoId) {
        return new ResultadoComando(true, mensaje, pedidoId);
    }

    public static ResultadoComando fallo(String mensaje, Long pedidoId) {
        return new ResultadoComando(false, mensaje, pedidoId);
    }
}
