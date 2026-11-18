package pe.techsolutions.plataforma.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.techsolutions.plataforma.catalogo.FiltroCatalogo;
import pe.techsolutions.plataforma.catalogo.ServicioCatalogo;
import pe.techsolutions.plataforma.dominio.modelo.OperacionNoPermitidaException;
import pe.techsolutions.plataforma.inventario.ServicioInventario;
import pe.techsolutions.plataforma.seguridad.ProveedorUsuarioActual;
import pe.techsolutions.plataforma.seguridad.UsuarioSesion;

/** RF5/RF6: gestión de stock y del nivel mínimo por producto. */
@Controller
public class InventarioController {

    private final ServicioInventario inventario;
    private final ServicioCatalogo catalogo;
    private final ProveedorUsuarioActual usuarioActual;

    public InventarioController(ServicioInventario inventario, ServicioCatalogo catalogo,
                                ProveedorUsuarioActual usuarioActual) {
        this.inventario = inventario;
        this.catalogo = catalogo;
        this.usuarioActual = usuarioActual;
    }

    private UsuarioSesion usuario() {
        return usuarioActual.actual().orElse(null);
    }

    @GetMapping("/inventario")
    public String inventario(@RequestParam(defaultValue = "") String texto,
                             @RequestParam(defaultValue = "") String categoria,
                             @RequestParam(defaultValue = "false") boolean soloBajo,
                             @RequestParam(defaultValue = "0") int pagina,
                             Model model) {
        FiltroCatalogo filtro = new FiltroCatalogo(texto, categoria, soloBajo);
        model.addAttribute("pagina", catalogo.pagina(filtro, pagina, 15));
        model.addAttribute("filtro", filtro);
        model.addAttribute("categorias", catalogo.categorias());
        model.addAttribute("puedeEditar", Permisos.tiene(usuario(), Permisos.GESTIONAN_INVENTARIO));
        model.addAttribute("bajoMinimo", inventario.contarBajoMinimo());
        return "inventario";
    }

    @PostMapping("/inventario/{id}/minimo")
    public String configurarMinimo(@PathVariable Long id, @RequestParam int minimo,
                                   @RequestParam(defaultValue = "") String texto, @RequestParam(defaultValue = "") String categoria,
                                   @RequestParam(defaultValue = "false") boolean soloBajo, @RequestParam(defaultValue = "0") int pagina,
                                   RedirectAttributes redirect) {
        Permisos.exigir(usuario(), Permisos.GESTIONAN_INVENTARIO, "configurar el stock mínimo");
        return ejecutar(() -> inventario.configurarStockMinimo(id, minimo),
                "Stock mínimo actualizado a " + minimo, redirect, texto, categoria, soloBajo, pagina);
    }

    @PostMapping("/inventario/{id}/entrada")
    public String entrada(@PathVariable Long id, @RequestParam int cantidad,
                          @RequestParam(defaultValue = "") String texto, @RequestParam(defaultValue = "") String categoria,
                          @RequestParam(defaultValue = "false") boolean soloBajo, @RequestParam(defaultValue = "0") int pagina,
                          RedirectAttributes redirect) {
        Permisos.exigir(usuario(), Permisos.GESTIONAN_INVENTARIO, "registrar entradas de stock");
        return ejecutar(() -> inventario.registrarEntrada(id, cantidad),
                "Entrada de " + cantidad + " unidad(es) registrada", redirect, texto, categoria, soloBajo, pagina);
    }

    @PostMapping("/inventario/{id}/salida")
    public String salida(@PathVariable Long id, @RequestParam int cantidad,
                         @RequestParam(defaultValue = "") String texto, @RequestParam(defaultValue = "") String categoria,
                         @RequestParam(defaultValue = "false") boolean soloBajo, @RequestParam(defaultValue = "0") int pagina,
                         RedirectAttributes redirect) {
        Permisos.exigir(usuario(), Permisos.GESTIONAN_INVENTARIO, "registrar salidas de stock");
        return ejecutar(() -> inventario.registrarSalida(id, cantidad),
                "Salida de " + cantidad + " unidad(es) registrada", redirect, texto, categoria, soloBajo, pagina);
    }

    private String ejecutar(Runnable accion, String exito, RedirectAttributes redirect,
                            String texto, String categoria, boolean soloBajo, int pagina) {
        try {
            accion.run();
            redirect.addFlashAttribute("exito", exito);
        } catch (OperacionNoPermitidaException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        redirect.addAttribute("texto", texto);
        redirect.addAttribute("categoria", categoria);
        redirect.addAttribute("soloBajo", soloBajo);
        redirect.addAttribute("pagina", pagina);
        return "redirect:/inventario";
    }
}
