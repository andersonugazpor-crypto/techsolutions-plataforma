package pe.techsolutions.plataforma.catalogo;

import org.springframework.stereotype.Service;
import pe.techsolutions.plataforma.dominio.modelo.Producto;
import pe.techsolutions.plataforma.precio.CalculadoraPrecios;
import pe.techsolutions.plataforma.repositorio.ProductoRepositorio;

import java.util.ArrayList;
import java.util.List;

/** Fachada de catálogo para la capa web: combina el iterador con la estrategia de precios. */
@Service
public class ServicioCatalogo {

    private final CatalogoProductos catalogo;
    private final CalculadoraPrecios calculadora;
    private final ProductoRepositorio productos;

    public ServicioCatalogo(CatalogoProductos catalogo, CalculadoraPrecios calculadora, ProductoRepositorio productos) {
        this.catalogo = catalogo;
        this.calculadora = calculadora;
        this.productos = productos;
    }

    public PaginaCatalogo<ProductoVista> pagina(FiltroCatalogo filtro, int pagina, int tamano) {
        return catalogo.pagina(filtro, pagina, tamano, p -> new ProductoVista(p, calculadora.precioDe(p)));
    }

    /** Recorre todo el catálogo con el iterador (usado para armar listas de selección). */
    public List<ProductoVista> todos() {
        List<ProductoVista> lista = new ArrayList<>();
        Iterador<Producto> it = catalogo.crearIterador(FiltroCatalogo.sinFiltro());
        while (it.tieneSiguiente()) {
            Producto p = it.siguiente();
            lista.add(new ProductoVista(p, calculadora.precioDe(p)));
        }
        return lista;
    }

    public List<String> categorias() {
        return productos.categorias();
    }
}
