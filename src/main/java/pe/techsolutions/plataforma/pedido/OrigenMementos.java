package pe.techsolutions.plataforma.pedido;

import pe.techsolutions.plataforma.dominio.modelo.PedidoMemento;

/** Permite al invocador pedir una instantánea de un pedido antes de modificarlo. */
public interface OrigenMementos {

    PedidoMemento capturar(Long pedidoId, String etiqueta);
}
