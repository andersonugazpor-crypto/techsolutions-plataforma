package pe.techsolutions.plataforma.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.techsolutions.plataforma.catalogo.ServicioCatalogo;
import pe.techsolutions.plataforma.dominio.modelo.Pedido;
import pe.techsolutions.plataforma.pago.ServicioPagos;
import pe.techsolutions.plataforma.pedido.LineaPedido;
import pe.techsolutions.plataforma.pedido.NuevoPedido;
import pe.techsolutions.plataforma.pedido.ServicioHistorial;
import pe.techsolutions.plataforma.pedido.ServicioPedidos;
import pe.techsolutions.plataforma.pedido.comando.ComandoPedido;
import pe.techsolutions.plataforma.pedido.comando.FabricaComandos;
import pe.techsolutions.plataforma.pedido.comando.InvocadorComandos;
import pe.techsolutions.plataforma.pedido.comando.ResultadoComando;
import pe.techsolutions.plataforma.pedido.memento.HistorialMementos;
import pe.techsolutions.plataforma.seguridad.ProveedorUsuarioActual;
import pe.techsolutions.plataforma.seguridad.UsuarioSesion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * GRASP Controlador: recibe los eventos de la interfaz sobre pedidos y los
 * convierte en comandos que entrega al invocador. No contiene reglas de negocio.
 */
@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    private static final int FILAS_FORMULARIO = 5;
    private static final String MODO_ENCOLAR = "encolar";

    private final ServicioPedidos pedidos;
    private final FabricaComandos fabrica;
    private final InvocadorComandos invocador;
    private final HistorialMementos mementos;
    private final ServicioHistorial historial;
    private final ServicioPagos pagos;
    private final ServicioCatalogo catalogo;
    private final ProveedorUsuarioActual usuarioActual;

    public PedidoController(ServicioPedidos pedidos, FabricaComandos fabrica, InvocadorComandos invocador,
                            HistorialMementos mementos, ServicioHistorial historial, ServicioPagos pagos,
                            ServicioCatalogo catalogo, ProveedorUsuarioActual usuarioActual) {
        this.pedidos = pedidos;
        this.fabrica = fabrica;
        this.invocador = invocador;
        this.mementos = mementos;
        this.historial = historial;
        this.pagos = pagos;
        this.catalogo = catalogo;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pedidos", pedidos.listar());
        model.addAttribute("comandosEnCola", invocador.pendientes().size());
        model.addAttribute("puedeGestionar", Permisos.tiene(usuario(), Permisos.GESTIONAN_PEDIDOS));
        return "pedidos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        Permisos.exigir(usuario(), Permisos.GESTIONAN_PEDIDOS, "crear pedidos");
        model.addAttribute("productos", catalogo.todos());
        model.addAttribute("pasarelas", pagos.pasarelasHabilitadas());
        model.addAttribute("filas", FILAS_FORMULARIO);
        return "pedidos/nuevo";
    }

    @PostMapping
    public String crear(@RequestParam String cliente, @RequestParam String metodoPago,
                        @RequestParam(defaultValue = "") String contactoPago,
                        @RequestParam(name = "productoId", required = false) List<String> productoIds,
                        @RequestParam(name = "cantidad", required = false) List<String> cantidades,
                        RedirectAttributes redirect) {
        Permisos.exigir(usuario(), Permisos.GESTIONAN_PEDIDOS, "crear pedidos");
        NuevoPedido datos = new NuevoPedido(cliente, metodoPago, contactoPago, lineas(productoIds, cantidades));
        ResultadoComando resultado = invocador.ejecutar(fabrica.crear(datos), usuario().username());
        mostrar(resultado, redirect);
        return resultado.exitoso() ? "redirect:/pedidos/" + resultado.pedidoId() : "redirect:/pedidos/nuevo";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        Pedido pedido = pedidos.buscar(id);
        model.addAttribute("pedido", pedido);
        model.addAttribute("nombrePasarela", pagos.nombreDe(pedido.getMetodoPago()));
        model.addAttribute("mementos", mementos.listar(id));
        model.addAttribute("historial", historial.dePedido(id));
        model.addAttribute("puedeGestionar", Permisos.tiene(usuario(), Permisos.GESTIONAN_PEDIDOS));
        return "pedidos/detalle";
    }

    @PostMapping("/{id}/procesar")
    public String procesar(@PathVariable Long id, @RequestParam(defaultValue = "ejecutar") String modo,
                           RedirectAttributes redirect) {
        return despachar(fabrica.procesar(id), modo, redirect);
    }

    @PostMapping("/{id}/descuento")
    public String descuento(@PathVariable Long id, @RequestParam BigDecimal porcentaje,
                            @RequestParam(defaultValue = "ejecutar") String modo, RedirectAttributes redirect) {
        return despachar(fabrica.aplicarDescuento(id, porcentaje), modo, redirect);
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, @RequestParam(defaultValue = "ejecutar") String modo,
                           RedirectAttributes redirect) {
        return despachar(fabrica.cancelar(id), modo, redirect);
    }

    @PostMapping("/{id}/restaurar")
    public String restaurar(@PathVariable Long id, @RequestParam int indice, RedirectAttributes redirect) {
        return despachar(fabrica.restaurar(id, indice), "ejecutar", redirect);
    }

    // ------------------------------------------------------------------ cola
    @GetMapping("/cola")
    public String cola(Model model) {
        model.addAttribute("pendientes", invocador.pendientes());
        model.addAttribute("puedeGestionar", Permisos.tiene(usuario(), Permisos.GESTIONAN_PEDIDOS));
        return "pedidos/cola";
    }

    @PostMapping("/cola/procesar")
    public String procesarCola(RedirectAttributes redirect) {
        Permisos.exigir(usuario(), Permisos.GESTIONAN_PEDIDOS, "procesar la cola de comandos");
        List<ResultadoComando> resultados = invocador.procesarCola();
        redirect.addFlashAttribute("resultadosCola", resultados);
        redirect.addFlashAttribute("exito", resultados.size() + " comando(s) ejecutado(s) en orden de llegada");
        return "redirect:/pedidos/cola";
    }

    @PostMapping("/cola/vaciar")
    public String vaciarCola(RedirectAttributes redirect) {
        Permisos.exigir(usuario(), Permisos.GESTIONAN_PEDIDOS, "vaciar la cola de comandos");
        invocador.vaciarCola();
        redirect.addFlashAttribute("exito", "Cola de comandos vaciada");
        return "redirect:/pedidos/cola";
    }

    // ------------------------------------------------------------- internos
    private String despachar(ComandoPedido comando, String modo, RedirectAttributes redirect) {
        Permisos.exigir(usuario(), Permisos.GESTIONAN_PEDIDOS, "modificar pedidos");
        if (MODO_ENCOLAR.equals(modo)) {
            invocador.encolar(comando, usuario().username());
            redirect.addFlashAttribute("exito", "Comando encolado: " + comando.descripcion());
        } else {
            mostrar(invocador.ejecutar(comando, usuario().username()), redirect);
        }
        return "redirect:/pedidos/" + comando.pedidoId();
    }

    private void mostrar(ResultadoComando resultado, RedirectAttributes redirect) {
        redirect.addFlashAttribute(resultado.exitoso() ? "exito" : "error", resultado.mensaje());
    }

    private UsuarioSesion usuario() {
        return usuarioActual.actual().orElse(null);
    }

    static List<LineaPedido> lineas(List<String> productoIds, List<String> cantidades) {
        List<LineaPedido> lineas = new ArrayList<>();
        if (productoIds == null || cantidades == null) {
            return lineas;
        }
        for (int i = 0; i < Math.min(productoIds.size(), cantidades.size()); i++) {
            String producto = productoIds.get(i);
            String cantidad = cantidades.get(i);
            if (producto == null || producto.isBlank() || cantidad == null || cantidad.isBlank()) {
                continue;
            }
            try {
                lineas.add(new LineaPedido(Long.valueOf(producto.trim()), Integer.parseInt(cantidad.trim())));
            } catch (NumberFormatException e) {
                // fila con datos inválidos: se ignora y el receptor valida que exista al menos una línea
            }
        }
        return lineas;
    }
}
