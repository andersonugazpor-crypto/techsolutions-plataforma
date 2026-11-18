package pe.techsolutions.plataforma.catalogo;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.techsolutions.plataforma.dominio.modelo.Producto;
import pe.techsolutions.plataforma.repositorio.ProductoRepositorio;

import java.util.List;

/** Lee los lotes desde la base de datos con consultas paginadas (no carga toda la tabla). */
@Component
public class FuenteProductosJpa implements FuenteProductos {

    private final ProductoRepositorio repositorio;

    public FuenteProductosJpa(ProductoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> lote(FiltroCatalogo filtro, int numeroLote, int tamanoLote) {
        PageRequest pagina = PageRequest.of(numeroLote, tamanoLote, Sort.by("nombre").ascending());
        return repositorio.buscarConFiltros(filtro.texto(), filtro.categoria(), filtro.soloStockBajo(), pagina).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public long contar(FiltroCatalogo filtro) {
        return repositorio.buscarConFiltros(filtro.texto(), filtro.categoria(), filtro.soloStockBajo(),
                PageRequest.of(0, 1)).getTotalElements();
    }
}
