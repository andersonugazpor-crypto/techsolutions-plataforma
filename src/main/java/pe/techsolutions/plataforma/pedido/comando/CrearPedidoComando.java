package pe.techsolutions.plataforma.pedido.comando;

import pe.techsolutions.plataforma.dominio.modelo.Pedido;
import pe.techsolutions.plataforma.pedido.NuevoPedido;
import pe.techsolutions.plataforma.pedido.ReceptorPedidos;

public class CrearPedidoComando implements ComandoPedido {

    private final ReceptorPedidos receptor;
    private final NuevoPedido datos;
    private Long pedidoCreado;

    public CrearPedidoComando(ReceptorPedidos receptor, NuevoPedido datos) {
        this.receptor = receptor;
        this.datos = datos;
    }

    @Override public String nombre() { return "CREAR"; }

    @Override
    public String descripcion() {
        return "Crear pedido para " + datos.cliente() + " (" + datos.lineas().size() + " línea(s), pago " + datos.metodoPago() + ")";
    }

    @Override public Long pedidoId() { return pedidoCreado; }
    @Override public boolean requiereMemento() { return false; }

    @Override
    public ResultadoComando ejecutar() {
        Pedido pedido = receptor.crear(datos);
        pedidoCreado = pedido.getId();
        return ResultadoComando.ok("Pedido #" + pedidoCreado + " creado por S/ " + pedido.getTotal(), pedidoCreado);
    }
}
