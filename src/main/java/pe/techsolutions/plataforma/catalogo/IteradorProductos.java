package pe.techsolutions.plataforma.catalogo;

import pe.techsolutions.plataforma.dominio.modelo.Producto;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Patrón Iterator — iterador concreto con carga perezosa por lotes.
 * Solo trae de la fuente el siguiente lote cuando el anterior se agotó, por lo que
 * recorrer un catálogo grande no exige cargarlo completo en memoria.
 */
public class IteradorProductos implements Iterador<Producto> {

    private final FuenteProductos fuente;
    private final FiltroCatalogo filtro;
    private final int tamanoLote;
    private final int maximoLotes;

    private int siguienteLote;
    private int lotesLeidos;
    private boolean agotado;
    private List<Producto> buffer = Collections.emptyList();
    private int indice;

    /**
     * @param loteInicial primer lote a leer (base 0)
     * @param maximoLotes cantidad máxima de lotes a recorrer ({@link Integer#MAX_VALUE} = todos)
     */
    public IteradorProductos(FuenteProductos fuente, FiltroCatalogo filtro, int tamanoLote, int loteInicial, int maximoLotes) {
        if (tamanoLote <= 0) {
            throw new IllegalArgumentException("El tamaño de lote debe ser positivo");
        }
        this.fuente = fuente;
        this.filtro = filtro;
        this.tamanoLote = tamanoLote;
        this.siguienteLote = Math.max(0, loteInicial);
        this.maximoLotes = maximoLotes;
    }

    @Override
    public boolean tieneSiguiente() {
        if (indice < buffer.size()) {
            return true;
        }
        if (agotado || lotesLeidos >= maximoLotes) {
            return false;
        }
        cargarSiguienteLote();
        return indice < buffer.size();
    }

    @Override
    public Producto siguiente() {
        if (!tieneSiguiente()) {
            throw new NoSuchElementException("No hay más productos en el catálogo");
        }
        return buffer.get(indice++);
    }

    public int lotesLeidos() {
        return lotesLeidos;
    }

    private void cargarSiguienteLote() {
        buffer = fuente.lote(filtro, siguienteLote++, tamanoLote);
        indice = 0;
        lotesLeidos++;
        if (buffer.size() < tamanoLote) {
            agotado = true;
        }
    }
}
