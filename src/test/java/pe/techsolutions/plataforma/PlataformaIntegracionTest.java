package pe.techsolutions.plataforma;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import pe.techsolutions.plataforma.dominio.modelo.EstadoPedidoTipo;
import pe.techsolutions.plataforma.dominio.modelo.Producto;
import pe.techsolutions.plataforma.dominio.modelo.Rol;
import pe.techsolutions.plataforma.dominio.modelo.Usuario;
import pe.techsolutions.plataforma.pedido.LineaPedido;
import pe.techsolutions.plataforma.pedido.NuevoPedido;
import pe.techsolutions.plataforma.pedido.ServicioPedidos;
import pe.techsolutions.plataforma.pedido.comando.FabricaComandos;
import pe.techsolutions.plataforma.pedido.comando.InvocadorComandos;
import pe.techsolutions.plataforma.pedido.comando.ResultadoComando;
import pe.techsolutions.plataforma.pedido.memento.HistorialMementos;
import pe.techsolutions.plataforma.repositorio.NotificacionRepositorio;
import pe.techsolutions.plataforma.repositorio.ProductoRepositorio;
import pe.techsolutions.plataforma.repositorio.UsuarioRepositorio;
import pe.techsolutions.plataforma.seguridad.ProveedorUsuarioActual;
import pe.techsolutions.plataforma.seguridad.UsuarioSesion;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integración: levanta la aplicación completa con H2 y los datos de demostración.
 * Verifica el flujo real entre patrones y que todas las pantallas se rendericen.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PlataformaIntegracionTest {

    @Autowired private MockMvc mvc;
    @Autowired private FabricaComandos fabrica;
    @Autowired private InvocadorComandos invocador;
    @Autowired private ServicioPedidos pedidos;
    @Autowired private HistorialMementos mementos;
    @Autowired private ProductoRepositorio productos;
    @Autowired private NotificacionRepositorio notificaciones;
    @Autowired private UsuarioRepositorio usuarios;

    private Producto producto(String codigo) {
        return productos.findAll().stream().filter(p -> p.getCodigo().equals(codigo)).findFirst().orElseThrow();
    }

    private MockHttpSession sesionDe(String username) {
        Usuario u = usuarios.findByUsername(username).orElseThrow();
        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute(ProveedorUsuarioActual.ATRIBUTO_SESION,
                new UsuarioSesion(u.getId(), u.getUsername(), u.getNombreCompleto(), u.getRol()));
        return sesion;
    }

    @Test
    @DisplayName("Flujo completo: crear → procesar (Adapter + Observer) → restaurar (Memento)")
    void flujoPedidoCompleto() {
        Producto atun = producto("ABR-005");               // stock 16, mínimo 15
        long alertasGerente = notificaciones.countByRolDestinoAndLeidaFalse(Rol.GERENTE);
        long alertasCompras = notificaciones.countByRolDestinoAndLeidaFalse(Rol.COMPRAS);

        ResultadoComando creado = invocador.ejecutar(fabrica.crear(new NuevoPedido("Cliente prueba", "YAPE", "987000111",
                List.of(new LineaPedido(atun.getId(), 3)))), "test");
        assertTrue(creado.exitoso(), creado.mensaje());
        Long id = creado.pedidoId();

        ResultadoComando procesado = invocador.ejecutar(fabrica.procesar(id), "test");
        assertTrue(procesado.exitoso(), procesado.mensaje());
        assertEquals(EstadoPedidoTipo.PROCESADO, pedidos.buscar(id).getEstado());
        assertEquals(13, producto("ABR-005").getStock());
        assertEquals(alertasGerente + 1, notificaciones.countByRolDestinoAndLeidaFalse(Rol.GERENTE));
        assertEquals(alertasCompras + 1, notificaciones.countByRolDestinoAndLeidaFalse(Rol.COMPRAS));

        assertEquals(1, mementos.listar(id).size());
        ResultadoComando restaurado = invocador.ejecutar(fabrica.restaurar(id, 0), "test");
        assertTrue(restaurado.exitoso(), restaurado.mensaje());
        assertEquals(EstadoPedidoTipo.PENDIENTE, pedidos.buscar(id).getEstado());
        assertEquals(16, producto("ABR-005").getStock());
    }

    @Test
    @DisplayName("Sin sesión se redirige al login")
    void exigeLogin() throws Exception {
        mvc.perform(get("/catalogo")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/login")).andExpect(status().isOk()).andExpect(content().string(containsString("Ingresar")));
    }

    @Test
    @DisplayName("Login con credenciales válidas e inválidas")
    void login() throws Exception {
        mvc.perform(post("/login").param("username", "gerente").param("password", "gerente123"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"));
        mvc.perform(post("/login").param("username", "gerente").param("password", "mala"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("incorrectos")));
    }

    @Test
    @DisplayName("Proxy en la web: el vendedor ve acceso restringido y el gerente el reporte completo")
    void reportesSegunRol() throws Exception {
        mvc.perform(get("/reportes").session(sesionDe("vendedor")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Acceso restringido")));
        mvc.perform(get("/reportes").session(sesionDe("gerente")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Ticket promedio")));
    }

    @Test
    @DisplayName("Solo el administrador entra a la configuración")
    void configuracionSoloAdmin() throws Exception {
        mvc.perform(get("/admin/configuracion").session(sesionDe("vendedor")))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/acceso-denegado"));
        mvc.perform(get("/admin/configuracion").session(sesionDe("admin")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Estrategia de precios")));
    }

    @Test
    @DisplayName("Todas las pantallas principales se renderizan sin errores")
    void pantallasRenderizan() throws Exception {
        MockHttpSession admin = sesionDe("admin");
        for (String ruta : List.of("/", "/catalogo", "/catalogo?texto=arroz&soloBajo=true", "/inventario",
                "/pedidos", "/pedidos/nuevo", "/pedidos/1", "/pedidos/cola", "/notificaciones", "/reportes")) {
            mvc.perform(get(ruta).session(admin)).andExpect(status().isOk());
        }
    }
}
