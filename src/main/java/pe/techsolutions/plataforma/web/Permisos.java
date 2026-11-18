package pe.techsolutions.plataforma.web;

import pe.techsolutions.plataforma.dominio.modelo.Rol;
import pe.techsolutions.plataforma.seguridad.AccesoDenegadoException;
import pe.techsolutions.plataforma.seguridad.UsuarioSesion;

import java.util.EnumSet;
import java.util.Set;

/** Reglas de permisos de la capa web (qué roles pueden modificar qué). */
public final class Permisos {

    public static final Set<Rol> GESTIONAN_INVENTARIO = EnumSet.of(Rol.ADMINISTRADOR, Rol.GERENTE, Rol.COMPRAS);
    public static final Set<Rol> GESTIONAN_PEDIDOS = EnumSet.of(Rol.ADMINISTRADOR, Rol.GERENTE, Rol.VENDEDOR);
    public static final Set<Rol> VEN_REPORTE_COMPLETO = EnumSet.of(Rol.GERENTE, Rol.CONTADOR);

    private Permisos() {
    }

    public static void exigir(UsuarioSesion usuario, Set<Rol> roles, String accion) {
        if (usuario == null || !roles.contains(usuario.rol())) {
            throw new AccesoDenegadoException("Su rol no tiene permiso para " + accion);
        }
    }

    public static boolean tiene(UsuarioSesion usuario, Set<Rol> roles) {
        return usuario != null && roles.contains(usuario.rol());
    }
}
