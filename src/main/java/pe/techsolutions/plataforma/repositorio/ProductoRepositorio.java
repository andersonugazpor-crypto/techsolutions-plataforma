package pe.techsolutions.plataforma.repositorio;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.techsolutions.plataforma.dominio.modelo.Producto;

import java.util.List;

public interface ProductoRepositorio extends JpaRepository<Producto, Long> {

    @Query("select p from Producto p "
            + "where (lower(p.nombre) like lower(concat('%', :texto, '%')) or lower(p.codigo) like lower(concat('%', :texto, '%'))) "
            + "and (:categoria = '' or p.categoria = :categoria) "
            + "and (:soloStockBajo = false or p.stock < p.stockMinimo)")
    Page<Producto> buscarConFiltros(@Param("texto") String texto,
                                    @Param("categoria") String categoria,
                                    @Param("soloStockBajo") boolean soloStockBajo,
                                    Pageable pageable);

    @Query("select distinct p.categoria from Producto p order by p.categoria")
    List<String> categorias();

    @Query("select count(p) from Producto p where p.stock < p.stockMinimo")
    long contarBajoMinimo();
}
