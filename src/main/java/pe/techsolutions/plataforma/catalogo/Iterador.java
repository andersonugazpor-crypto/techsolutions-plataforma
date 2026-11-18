package pe.techsolutions.plataforma.catalogo;

/**
 * Patrón Iterator — interfaz del iterador.
 * Permite recorrer una colección sin conocer cómo está almacenada.
 */
public interface Iterador<T> {

    boolean tieneSiguiente();

    T siguiente();
}
