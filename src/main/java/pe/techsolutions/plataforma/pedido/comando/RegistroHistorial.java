package pe.techsolutions.plataforma.pedido.comando;

/** Destino donde el invocador deja constancia de cada comando ejecutado (RF7). */
public interface RegistroHistorial {

    void registrar(ComandoPedido comando, String usuario, ResultadoComando resultado);
}
