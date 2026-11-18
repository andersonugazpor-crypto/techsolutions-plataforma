package pe.techsolutions.plataforma.catalogo;

import pe.techsolutions.plataforma.dominio.modelo.Producto;

import java.math.BigDecimal;

/** Datos de un producto para mostrar en pantalla (no expone la entidad). */
public class ProductoVista {

    private final Long id;
    private final String codigo;
    private final String nombre;
    private final String categoria;
    private final BigDecimal precioBase;
    private final BigDecimal precioFinal;
    private final int stock;
    private final int stockMinimo;
    private final boolean bajoMinimo;

    public ProductoVista(Producto p, BigDecimal precioFinal) {
        this.id = p.getId();
        this.codigo = p.getCodigo();
        this.nombre = p.getNombre();
        this.categoria = p.getCategoria();
        this.precioBase = p.getPrecioBase();
        this.precioFinal = precioFinal;
        this.stock = p.getStock();
        this.stockMinimo = p.getStockMinimo();
        this.bajoMinimo = p.estaBajoMinimo();
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public BigDecimal getPrecioBase() { return precioBase; }
    public BigDecimal getPrecioFinal() { return precioFinal; }
    public int getStock() { return stock; }
    public int getStockMinimo() { return stockMinimo; }
    public boolean isBajoMinimo() { return bajoMinimo; }
    public boolean isPrecioModificado() { return precioFinal.compareTo(precioBase) != 0; }
}
