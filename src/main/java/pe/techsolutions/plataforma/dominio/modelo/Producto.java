package pe.techsolutions.plataforma.dominio.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Producto del inventario.
 * GRASP Experto en información: conoce su stock y su mínimo, por lo que
 * él mismo decide si está bajo el mínimo y valida los movimientos de stock.
 */
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 60)
    private String categoria;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioBase;

    private int stock;

    /** RF6: nivel mínimo configurable por producto. */
    private int stockMinimo;

    /** Unidades vendidas acumuladas: indicador de demanda usado por el precio dinámico. */
    private int unidadesVendidas;

    protected Producto() {
        // requerido por JPA
    }

    public Producto(String codigo, String nombre, String categoria, BigDecimal precioBase, int stock, int stockMinimo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precioBase = precioBase;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
    }

    public boolean estaBajoMinimo() {
        return stock < stockMinimo;
    }

    public void descontarStock(int cantidad) {
        validarCantidad(cantidad);
        if (cantidad > stock) {
            throw new OperacionNoPermitidaException(
                    "Stock insuficiente para " + nombre + ": disponible " + stock + ", solicitado " + cantidad);
        }
        stock -= cantidad;
        unidadesVendidas += cantidad;
    }

    public void reponerStock(int cantidad) {
        validarCantidad(cantidad);
        stock += cantidad;
    }

    /** Devuelve unidades al stock por una venta revertida (cancelación o restauración). */
    public void devolverVenta(int cantidad) {
        validarCantidad(cantidad);
        stock += cantidad;
        unidadesVendidas = Math.max(0, unidadesVendidas - cantidad);
    }

    /** Salida de stock que no es una venta (merma, ajuste por inventario físico). */
    public void registrarSalida(int cantidad) {
        validarCantidad(cantidad);
        if (cantidad > stock) {
            throw new OperacionNoPermitidaException("No se puede retirar más del stock disponible (" + stock + ")");
        }
        stock -= cantidad;
    }

    public void configurarStockMinimo(int nuevoMinimo) {
        if (nuevoMinimo < 0) {
            throw new OperacionNoPermitidaException("El stock mínimo no puede ser negativo");
        }
        this.stockMinimo = nuevoMinimo;
    }

    public boolean tieneDisponible(int cantidad) {
        return cantidad <= stock;
    }

    private void validarCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new OperacionNoPermitidaException("La cantidad debe ser mayor que cero");
        }
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public BigDecimal getPrecioBase() { return precioBase; }
    public int getStock() { return stock; }
    public int getStockMinimo() { return stockMinimo; }
    public int getUnidadesVendidas() { return unidadesVendidas; }

    /** Solo para pruebas y datos de ejemplo. */
    public void asignarId(Long id) { this.id = id; }

    /** Solo para datos de ejemplo: fija la demanda histórica. */
    public void asignarUnidadesVendidas(int unidadesVendidas) { this.unidadesVendidas = unidadesVendidas; }
}
