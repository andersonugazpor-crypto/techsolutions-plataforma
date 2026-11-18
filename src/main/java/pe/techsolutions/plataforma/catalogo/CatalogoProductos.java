package pe.techsolutions.plataforma.catalogo;

import pe.techsolutions.plataforma.dominio.modelo.Producto;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Patrón Iterator — agregado. Es la única puerta de entrada al catálogo:
 * entrega iteradores y páginas, nunca la colección interna.
 */
public class CatalogoProductos {

    public static final int TAMANO_LOTE_POR_DEFECTO = 25;
    public static final int TAMANO_PAGINA_MAXIMO = 100;

    private final FuenteProductos fuente;

    public CatalogoProductos(FuenteProductos fuente) {
        this.fuente = fuente;
    }

    /** Iterador para recorrer todo el catálogo filtrado, cargando lotes bajo demanda. */
    public Iterador<Producto> crearIterador(FiltroCatalogo filtro) {
        return new IteradorProductos(fuente, filtro, TAMANO_LOTE_POR_DEFECTO, 0, Integer.MAX_VALUE);
    }

    /** Iterador que recorre únicamente una página. */
    public Iterador<Producto> crearIteradorDePagina(FiltroCatalogo filtro, int pagina, int tamano) {
        return new IteradorProductos(fuente, filtro, tamano, pagina, 1);
    }

    /** Arma una página transformando cada producto con {@code aVista} (p. ej. a un DTO con precio calculado). */
    public <T> PaginaCatalogo<T> pagina(FiltroCatalogo filtro, int pagina, int tamano, Function<Producto, T> aVista) {
        int tamanoValido = Math.min(Math.max(1, tamano), TAMANO_PAGINA_MAXIMO);
        long total = fuente.contar(filtro);
        int ultimaPagina = (int) Math.max(0, (total - 1) / tamanoValido);
        int paginaValida = Math.min(Math.max(0, pagina), ultimaPagina);

        List<T> elementos = new ArrayList<>();
        Iterador<Producto> iterador = crearIteradorDePagina(filtro, paginaValida, tamanoValido);
        while (iterador.tieneSiguiente()) {
            elementos.add(aVista.apply(iterador.siguiente()));
        }
        return new PaginaCatalogo<>(elementos, paginaValida, tamanoValido, total);
    }
}
