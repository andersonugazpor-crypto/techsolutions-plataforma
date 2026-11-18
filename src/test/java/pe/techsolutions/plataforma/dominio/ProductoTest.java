package pe.techsolutions.plataforma.dominio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.techsolutions.plataforma.dominio.modelo.OperacionNoPermitidaException;
import pe.techsolutions.plataforma.dominio.modelo.Producto;
import pe.techsolutions.plataforma.soporte.Datos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Producto: reglas de stock (Experto en información)")
class ProductoTest {

    @Test
    @DisplayName("Detecta stock bajo el mínimo configurable (RF6)")
    void detectaStockBajo() {
        Producto p = Datos.producto(1, "Atún", "6.20", 16, 15);
        assertFalse(p.estaBajoMinimo());
        p.descontarStock(2);
        assertTrue(p.estaBajoMinimo());
        p.configurarStockMinimo(10);
        assertFalse(p.estaBajoMinimo());
    }

    @Test
    @DisplayName("No permite descontar más que el stock ni mínimos negativos")
    void validaMovimientos() {
        Producto p = Datos.producto(1, "Atún", "6.20", 3, 1);
        assertThrows(OperacionNoPermitidaException.class, () -> p.descontarStock(4));
        assertThrows(OperacionNoPermitidaException.class, () -> p.descontarStock(0));
        assertThrows(OperacionNoPermitidaException.class, () -> p.configurarStockMinimo(-1));
    }

    @Test
    @DisplayName("Una venta devuelta regresa el stock y reduce la demanda")
    void devolverVenta() {
        Producto p = Datos.producto(1, "Atún", "6.20", 10, 1);
        p.descontarStock(4);
        assertEquals(4, p.getUnidadesVendidas());
        p.devolverVenta(4);
        assertEquals(10, p.getStock());
        assertEquals(0, p.getUnidadesVendidas());
    }
}
