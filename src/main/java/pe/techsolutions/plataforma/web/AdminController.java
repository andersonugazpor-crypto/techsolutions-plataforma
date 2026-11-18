package pe.techsolutions.plataforma.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.techsolutions.plataforma.dominio.modelo.OperacionNoPermitidaException;
import pe.techsolutions.plataforma.pago.EstadoPasarelas;
import pe.techsolutions.plataforma.pago.ServicioPagos;
import pe.techsolutions.plataforma.precio.CalculadoraPrecios;

import java.math.BigDecimal;
import java.util.List;

/** Panel de configuración del administrador: RF2 (pasarelas) y RF10 (estrategia de precios). */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ServicioPagos pagos;
    private final EstadoPasarelas estadoPasarelas;
    private final CalculadoraPrecios calculadora;

    public AdminController(ServicioPagos pagos, EstadoPasarelas estadoPasarelas, CalculadoraPrecios calculadora) {
        this.pagos = pagos;
        this.estadoPasarelas = estadoPasarelas;
        this.calculadora = calculadora;
    }

    @GetMapping("/configuracion")
    public String configuracion(Model model) {
        List<PasarelaVista> pasarelas = pagos.todasLasPasarelas().stream()
                .map(p -> new PasarelaVista(p.codigo(), p.nombre(), p.tipoContacto(), estadoPasarelas.estaHabilitada(p.codigo())))
                .toList();
        model.addAttribute("pasarelas", pasarelas);
        model.addAttribute("estrategias", calculadora.estrategias());
        model.addAttribute("estrategiaActiva", calculadora.estrategiaActiva());
        model.addAttribute("parametros", calculadora.parametros());
        return "admin/configuracion";
    }

    @PostMapping("/pasarelas/{codigo}")
    public String cambiarPasarela(@PathVariable String codigo, @RequestParam boolean habilitada,
                                  RedirectAttributes redirect) {
        boolean existe = pagos.todasLasPasarelas().stream().anyMatch(p -> p.codigo().equals(codigo));
        if (!existe) {
            throw new OperacionNoPermitidaException("Pasarela desconocida: " + codigo);
        }
        estadoPasarelas.cambiar(codigo, habilitada);
        redirect.addFlashAttribute("exito", "Pasarela " + pagos.nombreDe(codigo) + (habilitada ? " habilitada" : " deshabilitada"));
        return "redirect:/admin/configuracion";
    }

    @PostMapping("/precios")
    public String cambiarEstrategia(@RequestParam String estrategia, @RequestParam BigDecimal descuento,
                                    @RequestParam BigDecimal recargo, RedirectAttributes redirect) {
        try {
            calculadora.cambiarEstrategia(estrategia, descuento, recargo);
            redirect.addFlashAttribute("exito", "Estrategia de precios activa: " + calculadora.estrategiaActiva().nombre());
        } catch (OperacionNoPermitidaException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/configuracion";
    }
}
