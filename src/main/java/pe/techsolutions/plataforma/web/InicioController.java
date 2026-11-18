package pe.techsolutions.plataforma.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pe.techsolutions.plataforma.dominio.modelo.EstadoPedidoTipo;
import pe.techsolutions.plataforma.inventario.ServicioInventario;
import pe.techsolutions.plataforma.pago.ServicioPagos;
import pe.techsolutions.plataforma.pedido.ServicioHistorial;
import pe.techsolutions.plataforma.pedido.ServicioPedidos;
import pe.techsolutions.plataforma.pedido.comando.InvocadorComandos;
import pe.techsolutions.plataforma.precio.CalculadoraPrecios;

/** Panel de inicio con indicadores de cada módulo. */
@Controller
public class InicioController {

    private final ServicioPedidos pedidos;
    private final ServicioInventario inventario;
    private final ServicioPagos pagos;
    private final CalculadoraPrecios calculadora;
    private final ServicioHistorial historial;
    private final InvocadorComandos invocador;

    public InicioController(ServicioPedidos pedidos, ServicioInventario inventario, ServicioPagos pagos,
                            CalculadoraPrecios calculadora, ServicioHistorial historial, InvocadorComandos invocador) {
        this.pedidos = pedidos;
        this.inventario = inventario;
        this.pagos = pagos;
        this.calculadora = calculadora;
        this.historial = historial;
        this.invocador = invocador;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("pedidosPendientes", pedidos.contarPorEstado(EstadoPedidoTipo.PENDIENTE));
        model.addAttribute("pedidosProcesados", pedidos.contarPorEstado(EstadoPedidoTipo.PROCESADO));
        model.addAttribute("productosBajoMinimo", inventario.contarBajoMinimo());
        model.addAttribute("estrategia", calculadora.estrategiaActiva());
        model.addAttribute("pasarelas", pagos.pasarelasHabilitadas());
        model.addAttribute("comandosEnCola", invocador.pendientes().size());
        model.addAttribute("actividad", historial.recientes().stream().limit(8).toList());
        return "inicio";
    }
}
