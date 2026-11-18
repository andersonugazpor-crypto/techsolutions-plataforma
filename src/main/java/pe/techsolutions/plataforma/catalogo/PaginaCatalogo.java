package pe.techsolutions.plataforma.catalogo;

import java.util.Collections;
import java.util.List;

/** Resultado de una página del catálogo, listo para la vista (RF12). */
public class PaginaCatalogo<T> {

    private final List<T> elementos;
    private final int pagina;
    private final int tamano;
    private final long totalElementos;
    private final int totalPaginas;

    public PaginaCatalogo(List<T> elementos, int pagina, int tamano, long totalElementos) {
        this.elementos = Collections.unmodifiableList(elementos);
        this.pagina = pagina;
        this.tamano = tamano;
        this.totalElementos = totalElementos;
        this.totalPaginas = (int) Math.max(1, (totalElementos + tamano - 1) / tamano);
    }

    public List<T> getElementos() { return elementos; }
    public int getPagina() { return pagina; }
    public int getNumeroVisible() { return pagina + 1; }
    public int getTamano() { return tamano; }
    public long getTotalElementos() { return totalElementos; }
    public int getTotalPaginas() { return totalPaginas; }
    public boolean isTieneAnterior() { return pagina > 0; }
    public boolean isTieneSiguiente() { return pagina + 1 < totalPaginas; }
    public long getDesde() { return totalElementos == 0 ? 0 : (long) pagina * tamano + 1; }
    public long getHasta() { return (long) pagina * tamano + elementos.size(); }
}
