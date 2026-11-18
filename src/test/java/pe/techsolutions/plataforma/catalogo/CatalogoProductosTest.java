package pe.techsolutions.plataforma.catalogo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.techsolutions.plataforma.dominio.modelo.Producto;
import pe.techsolutions.plataforma.soporte.Datos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Iterator: recorrido del catálogo con paginación y filtros")
class CatalogoProductosTest {

    /** Fuente en memoria que cuenta cuántos lotes se le pidieron. */
    static class FuenteEnMemoria implements FuenteProductos {
        final List<Producto> productos = new ArrayList<>();
        int lotesPedidos;

        @Override
        public List<Producto> lote(FiltroCatalogo filtro, int numeroLote, int tamanoLote) {
            lotesPedidos++;
            List<Producto> filtrados = filtrar(filtro);
            int desde = numeroLote * tamanoLote;
            if (desde >= filtrados.size()) {
                return List.of();
            }
            return filtrados.subList(desde, Math.min(desde + tamanoLote, filtrados.size()));
        }

        @Override
        public long contar(FiltroCatalogo filtro) {
            return filtrar(filtro).size();
        }

        private List<Producto> filtrar(FiltroCatalogo f) {
            return productos.stream()
                    .filter(p -> p.getNombre().toLowerCase().contains(f.texto().toLowerCase()))
                    .filter(p -> f.categoria().isEmpty() || p.getCategoria().equals(f.categoria()))
                    .filter(p -> !f.soloStockBajo() || p.estaBajoMinimo())
                    .sorted(Comparator.comparing(Producto::getNombre))
                    .toList();
        }
    }

    private FuenteEnMemoria fuente;
    private CatalogoProductos catalogo;

    @BeforeEach
    void preparar() {
        fuente = new FuenteEnMemoria();
        for (int i = 1; i <= 60; i++) {
            int stock = i % 10 == 0 ? 1 : 50;  // 6 productos con stock bajo
            fuente.productos.add(Datos.producto(i, String.format("Producto %02d", i), "10.00", stock, 5));
        }
        catalogo = new CatalogoProductos(fuente);
    }

    @Test
    @DisplayName("RF11: recorre todo el catálogo cargando lotes bajo demanda")
    void recorreTodoPorLotes() {
        Iterador<Producto> it = catalogo.crearIterador(FiltroCatalogo.sinFiltro());
        assertEquals(0, fuente.lotesPedidos);         // perezoso: aún no carga nada
        int contados = 0;
        while (it.tieneSiguiente()) {
            it.siguiente();
            contados++;
        }
        assertEquals(60, contados);
        assertEquals(3, fuente.lotesPedidos);         // 25 + 25 + 10
        assertThrows(NoSuchElementException.class, it::siguiente);
    }

    @Test
    @DisplayName("RF11: devuelve la página pedida con sus metadatos")
    void paginacion() {
        PaginaCatalogo<String> pagina = catalogo.pagina(FiltroCatalogo.sinFiltro(), 1, 12, Producto::getNombre);
        assertEquals(12, pagina.getElementos().size());
        assertEquals("Producto 13", pagina.getElementos().get(0));
        assertEquals(5, pagina.getTotalPaginas());
        assertEquals(13, pagina.getDesde());
        assertEquals(24, pagina.getHasta());
        assertTrue(pagina.isTieneAnterior());
        assertTrue(pagina.isTieneSiguiente());
    }

    @Test
    @DisplayName("Ajusta páginas fuera de rango a la última página válida")
    void paginaFueraDeRango() {
        PaginaCatalogo<String> pagina = catalogo.pagina(FiltroCatalogo.sinFiltro(), 99, 25, Producto::getNombre);
        assertEquals(2, pagina.getPagina());
        assertEquals(10, pagina.getElementos().size());
        assertFalse(pagina.isTieneSiguiente());
    }

    @Test
    @DisplayName("RF11: aplica filtros de texto y stock bajo")
    void filtros() {
        PaginaCatalogo<String> bajos = catalogo.pagina(new FiltroCatalogo("", "", true), 0, 50, Producto::getNombre);
        assertEquals(6, bajos.getTotalElementos());
        PaginaCatalogo<String> texto = catalogo.pagina(new FiltroCatalogo("producto 0", null, false), 0, 50, Producto::getNombre);
        assertEquals(9, texto.getTotalElementos());
    }

    @Test
    @DisplayName("RF12: la página expone una lista inmodificable, no la colección interna")
    void noExponeColeccion() {
        PaginaCatalogo<String> pagina = catalogo.pagina(FiltroCatalogo.sinFiltro(), 0, 5, Producto::getNombre);
        assertThrows(UnsupportedOperationException.class, () -> pagina.getElementos().add("intruso"));
    }

    @Test
    @DisplayName("Catálogo vacío: una página sin elementos")
    void catalogoVacio() {
        PaginaCatalogo<String> pagina = new CatalogoProductos(new FuenteEnMemoria())
                .pagina(FiltroCatalogo.sinFiltro(), 0, 10, Producto::getNombre);
        assertEquals(0, pagina.getTotalElementos());
        assertEquals(1, pagina.getTotalPaginas());
        assertEquals(0, pagina.getDesde());
    }
}
