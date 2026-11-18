package pe.techsolutions.plataforma.catalogo;

/** Criterios de búsqueda del catálogo (RF11). */
public record FiltroCatalogo(String texto, String categoria, boolean soloStockBajo) {

    public FiltroCatalogo {
        texto = texto == null ? "" : texto.trim();
        categoria = categoria == null ? "" : categoria.trim();
    }

    public static FiltroCatalogo sinFiltro() {
        return new FiltroCatalogo("", "", false);
    }
}
