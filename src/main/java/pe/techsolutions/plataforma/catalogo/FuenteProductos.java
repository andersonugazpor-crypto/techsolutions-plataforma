package pe.techsolutions.plataforma.catalogo;

import pe.techsolutions.plataforma.dominio.modelo.Producto;

import java.util.List;

/**
 * Origen paginable de productos (GRASP Indirección). El iterador pide lotes
 * a esta interfaz, así la colección real (tabla en BD, lista en memoria) queda oculta.
 */
public interface FuenteProductos {

    /** Devuelve el lote {@code numeroLote} (base 0) ordenado por nombre. */
    List<Producto> lote(FiltroCatalogo filtro, int numeroLote, int tamanoLote);

    long contar(FiltroCatalogo filtro);
}
