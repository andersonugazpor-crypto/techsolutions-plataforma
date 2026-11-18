package pe.techsolutions.plataforma.dominio.modelo;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pedido de venta.
 * - State: delega procesar/cancelar/aplicarDescuento en {@link EstadoPedido}.
 * - Memento (originador): crea y restaura instantáneas {@link PedidoMemento}.
 * - GRASP Creador: crea sus propios {@link ItemPedido}.
 * - GRASP Experto: calcula subtotal, descuento y total con la información que posee.
 */
@Entity
@Table(name = "pedidos")
public class Pedido {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);
    private static final BigDecimal DESCUENTO_MAXIMO = BigDecimal.valueOf(50);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String cliente;

    /** Código de la pasarela de pago elegida (PAYPAL, YAPE, PLIN, ...). */
    @Column(nullable = false, length = 20)
    private String metodoPago;

    /** Dato de contacto para la pasarela: correo (PayPal) o celular (Yape/Plin). */
    @Column(length = 120)
    private String contactoPago;

    private LocalDateTime fechaCreacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedidoTipo estado = EstadoPedidoTipo.PENDIENTE;

    @Column(precision = 5, scale = 2)
    private BigDecimal porcentajeDescuento = BigDecimal.ZERO;

    @Column(length = 60)
    private String codigoTransaccion;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ItemPedido> items = new ArrayList<>();

    protected Pedido() {
        // requerido por JPA
    }

    public Pedido(String cliente, String metodoPago, String contactoPago) {
        if (cliente == null || cliente.isBlank()) {
            throw new OperacionNoPermitidaException("El cliente es obligatorio");
        }
        this.cliente = cliente.trim();
        this.metodoPago = metodoPago;
        this.contactoPago = contactoPago == null ? null : contactoPago.trim();
        this.fechaCreacion = LocalDateTime.now();
    }

    // ----------------------------------------------------------------- Creador
    public ItemPedido agregarItem(Producto producto, int cantidad, BigDecimal precioUnitario) {
        if (estado != EstadoPedidoTipo.PENDIENTE) {
            throw new OperacionNoPermitidaException("Solo se pueden agregar productos a pedidos pendientes");
        }
        if (cantidad <= 0) {
            throw new OperacionNoPermitidaException("La cantidad debe ser mayor que cero");
        }
        ItemPedido item = new ItemPedido(this, producto, cantidad, precioUnitario);
        items.add(item);
        return item;
    }

    // ----------------------------------------------------------------- Experto
    public BigDecimal getSubtotal() {
        return items.stream().map(ItemPedido::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getMontoDescuento() {
        return getSubtotal().multiply(porcentajeDescuento).divide(CIEN, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotal() {
        return getSubtotal().subtract(getMontoDescuento());
    }

    public int getTotalUnidades() {
        return items.stream().mapToInt(ItemPedido::getCantidad).sum();
    }

    // ------------------------------------------------------------------- State
    public EstadoPedido comportamiento() {
        return estado.comportamiento();
    }

    public void procesar(String codigoTransaccion) {
        comportamiento().procesar(this, codigoTransaccion);
    }

    public void cancelar() {
        comportamiento().cancelar(this);
    }

    public void aplicarDescuento(BigDecimal porcentaje) {
        comportamiento().aplicarDescuento(this, porcentaje);
    }

    /** Usados únicamente por las clases State del mismo paquete. */
    void cambiarEstado(EstadoPedidoTipo nuevoEstado) {
        this.estado = nuevoEstado;
    }

    void fijarDescuento(BigDecimal porcentaje) {
        if (porcentaje == null || porcentaje.signum() <= 0 || porcentaje.compareTo(DESCUENTO_MAXIMO) > 0) {
            throw new OperacionNoPermitidaException("El descuento debe estar entre 0 y " + DESCUENTO_MAXIMO + "%");
        }
        this.porcentajeDescuento = porcentaje;
    }

    void fijarTransaccion(String codigoTransaccion) {
        this.codigoTransaccion = codigoTransaccion;
    }

    // ----------------------------------------------------------------- Memento
    public PedidoMemento crearMemento(String etiqueta) {
        return new PedidoMemento(id, estado, porcentajeDescuento, codigoTransaccion, etiqueta, LocalDateTime.now());
    }

    public void restaurar(PedidoMemento memento) {
        if (memento.getPedidoId() != null && !memento.getPedidoId().equals(id)) {
            throw new OperacionNoPermitidaException("El memento no pertenece al pedido #" + id);
        }
        this.estado = memento.estado();
        this.porcentajeDescuento = memento.porcentajeDescuento();
        this.codigoTransaccion = memento.codigoTransaccion();
    }

    // ---------------------------------------------------------------- getters
    public Long getId() { return id; }
    public String getCliente() { return cliente; }
    public String getMetodoPago() { return metodoPago; }
    public String getContactoPago() { return contactoPago; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public EstadoPedidoTipo getEstado() { return estado; }
    public BigDecimal getPorcentajeDescuento() { return porcentajeDescuento; }
    public String getCodigoTransaccion() { return codigoTransaccion; }
    public List<ItemPedido> getItems() { return Collections.unmodifiableList(items); }

    /** Solo para pruebas unitarias sin base de datos. */
    public void asignarId(Long id) { this.id = id; }
}
