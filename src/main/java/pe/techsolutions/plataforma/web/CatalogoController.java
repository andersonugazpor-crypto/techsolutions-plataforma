package pe.techsolutions.plataforma.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.techsolutions.plataforma.catalogo.FiltroCatalogo;
import pe.techsolutions.plataforma.catalogo.ServicioCatalogo;
import pe.techsolutions.plataforma.precio.CalculadoraPrecios;

import java.util.List;

/** RF11/RF12: catálogo paginado y filtrable recorrido con el iterador. */
@Controller
public class CatalogoController {

    private final ServicioCatalogo catalogo;
    private final CalculadoraPrecios calculadora;

    public CatalogoController(ServicioCatalogo catalogo, CalculadoraPrecios calculadora) {
        this.catalogo = catalogo;
        this.calculadora = calculadora;
    }

    @GetMapping("/catalogo")
    public String catalogo(@RequestParam(defaultValue = "") String texto,
                           @RequestParam(defaultValue = "") String categoria,
                           @RequestParam(defaultValue = "false") boolean soloBajo,
                           @RequestParam(defaultValue = "0") int pagina,
                           @RequestParam(defaultValue = "12") int tamano,
                           Model model) {
        FiltroCatalogo filtro = new FiltroCatalogo(texto, categoria, soloBajo);
        model.addAttribute("pagina", catalogo.pagina(filtro, pagina, tamano));
        model.addAttribute("filtro", filtro);
        model.addAttribute("categorias", catalogo.categorias());
        model.addAttribute("tamanos", List.of(8, 12, 24, 48));
        model.addAttribute("estrategia", calculadora.estrategiaActiva());
        return "catalogo";
    }
}
