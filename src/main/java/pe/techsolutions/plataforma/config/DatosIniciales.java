package pe.techsolutions.plataforma.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pe.techsolutions.plataforma.dominio.modelo.ParametroSistema;
import pe.techsolutions.plataforma.dominio.modelo.Producto;
import pe.techsolutions.plataforma.dominio.modelo.Rol;
import pe.techsolutions.plataforma.dominio.modelo.Usuario;
import pe.techsolutions.plataforma.pago.EstadoPasarelas;
import pe.techsolutions.plataforma.pago.RegistroPasarelas;
import pe.techsolutions.plataforma.pedido.LineaPedido;
import pe.techsolutions.plataforma.pedido.NuevoPedido;
import pe.techsolutions.plataforma.pedido.comando.ComandoPedido;
import pe.techsolutions.plataforma.pedido.comando.FabricaComandos;
import pe.techsolutions.plataforma.pedido.comando.InvocadorComandos;
import pe.techsolutions.plataforma.pedido.comando.ResultadoComando;
import pe.techsolutions.plataforma.precio.ConfiguracionPreciosJpa;
import pe.techsolutions.plataforma.repositorio.ConfiguracionPasarelaRepositorio;
import pe.techsolutions.plataforma.repositorio.ParametroSistemaRepositorio;
import pe.techsolutions.plataforma.repositorio.ProductoRepositorio;
import pe.techsolutions.plataforma.repositorio.UsuarioRepositorio;

import java.math.BigDecimal;
import java.util.List;

/**
 * Carga datos de demostración la primera vez que arranca la aplicación
 * (usuarios por rol, productos, configuración y algunos pedidos de ejemplo).
 */
@Component
@Order(1)
public class DatosIniciales implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DatosIniciales.class);
    private static final String SISTEMA = "sistema";

    private final UsuarioRepositorio usuarios;
    private final ProductoRepositorio productos;
    private final ParametroSistemaRepositorio parametros;
    private final ConfiguracionPasarelaRepositorio configPasarelas;
    private final RegistroPasarelas registroPasarelas;
    private final EstadoPasarelas estadoPasarelas;
    private final PasswordEncoder passwordEncoder;
    private final FabricaComandos fabricaComandos;
    private final InvocadorComandos invocador;

    public DatosIniciales(UsuarioRepositorio usuarios, ProductoRepositorio productos, ParametroSistemaRepositorio parametros,
                          ConfiguracionPasarelaRepositorio configPasarelas, RegistroPasarelas registroPasarelas,
                          EstadoPasarelas estadoPasarelas, PasswordEncoder passwordEncoder,
                          FabricaComandos fabricaComandos, InvocadorComandos invocador) {
        this.usuarios = usuarios;
        this.productos = productos;
        this.parametros = parametros;
        this.configPasarelas = configPasarelas;
        this.registroPasarelas = registroPasarelas;
        this.estadoPasarelas = estadoPasarelas;
        this.passwordEncoder = passwordEncoder;
        this.fabricaComandos = fabricaComandos;
        this.invocador = invocador;
    }

    @Override
    public void run(String... args) {
        registrarPasarelasNuevas();
        if (usuarios.count() == 0) {
            crearUsuarios();
        }
        crearParametroSiFalta(ConfiguracionPreciosJpa.CLAVE_ESTRATEGIA, "ESTANDAR");
        crearParametroSiFalta(ConfiguracionPreciosJpa.CLAVE_DESCUENTO, "10");
        crearParametroSiFalta(ConfiguracionPreciosJpa.CLAVE_RECARGO, "15");
        if (productos.count() == 0) {
            crearProductos();
            crearPedidosDeEjemplo();
        }
    }

    /** Toda pasarela con adaptador registrado obtiene su fila de configuración (habilitada por defecto). */
    private void registrarPasarelasNuevas() {
        registroPasarelas.todas().stream()
                .filter(p -> !configPasarelas.existsById(p.codigo()))
                .forEach(p -> estadoPasarelas.cambiar(p.codigo(), true));
    }

    private void crearUsuarios() {
        usuarios.saveAll(List.of(
                usuario("admin", "admin123", "Ana Torres (Administradora)", Rol.ADMINISTRADOR),
                usuario("gerente", "gerente123", "Luis Ramírez (Gerente)", Rol.GERENTE),
                usuario("contador", "contador123", "María Quispe (Contadora)", Rol.CONTADOR),
                usuario("compras", "compras123", "Jorge Huamán (Compras)", Rol.COMPRAS),
                usuario("vendedor", "vendedor123", "Rosa Flores (Vendedora)", Rol.VENDEDOR)));
        LOG.info("Usuarios de demostración creados (admin, gerente, contador, compras, vendedor)");
    }

    private Usuario usuario(String username, String clave, String nombre, Rol rol) {
        return new Usuario(username, passwordEncoder.encode(clave), nombre, rol);
    }

    private void crearParametroSiFalta(String clave, String valor) {
        if (!parametros.existsById(clave)) {
            parametros.save(new ParametroSistema(clave, valor));
        }
    }

    private void crearProductos() {
        Object[][] datos = {
                // codigo, nombre, categoria, precio, stock, minimo, vendidas
                {"ABR-001", "Arroz extra Costeño 5 kg", "Abarrotes", "24.90", 80, 20, 64},
                {"ABR-002", "Aceite vegetal Primor 1 L", "Abarrotes", "11.50", 45, 15, 38},
                {"ABR-003", "Azúcar rubia Cartavio 1 kg", "Abarrotes", "4.60", 120, 30, 25},
                {"ABR-004", "Fideos spaghetti Don Vittorio 500 g", "Abarrotes", "3.80", 90, 25, 18},
                {"ABR-005", "Atún en trozos Florida 170 g", "Abarrotes", "6.20", 16, 15, 52},
                {"ABR-006", "Leche evaporada Gloria 400 g", "Abarrotes", "4.30", 200, 50, 75},
                {"ABR-007", "Menestra lenteja 1 kg", "Abarrotes", "7.90", 35, 10, 6},
                {"BEB-001", "Agua San Luis 2.5 L", "Bebidas", "3.50", 60, 20, 41},
                {"BEB-002", "Inca Kola 1.5 L", "Bebidas", "7.20", 22, 20, 88},
                {"BEB-003", "Café Altomayo 200 g", "Bebidas", "18.90", 25, 8, 12},
                {"BEB-004", "Infusión manzanilla Herbi x25", "Bebidas", "3.20", 40, 10, 9},
                {"BEB-005", "Jugo de naranja Frugos 1 L", "Bebidas", "5.40", 9, 12, 27},
                {"LIM-001", "Detergente Bolívar 2.6 kg", "Limpieza", "32.90", 30, 10, 22},
                {"LIM-002", "Lejía Clorox 1 L", "Limpieza", "4.90", 50, 15, 14},
                {"LIM-003", "Papel higiénico Elite x12", "Limpieza", "19.90", 28, 10, 33},
                {"LIM-004", "Lavavajillas Ayudín 900 g", "Limpieza", "8.70", 11, 10, 19},
                {"LIM-005", "Desinfectante Poett 900 ml", "Limpieza", "6.30", 44, 12, 7},
                {"CUI-001", "Shampoo Head & Shoulders 375 ml", "Cuidado personal", "21.50", 18, 6, 11},
                {"CUI-002", "Jabón Protex x3", "Cuidado personal", "9.90", 52, 15, 30},
                {"CUI-003", "Pasta dental Colgate 90 g", "Cuidado personal", "5.80", 65, 20, 24},
                {"CUI-004", "Desodorante Rexona 150 ml", "Cuidado personal", "14.90", 7, 8, 16},
                {"LIB-001", "Cuaderno A4 cuadriculado 100 h", "Librería", "6.50", 150, 40, 58},
                {"LIB-002", "Lapicero Faber-Castell azul x10", "Librería", "9.00", 80, 20, 21},
                {"LIB-003", "Papel bond A4 Atlas 500 h", "Librería", "17.90", 26, 10, 29},
                {"LIB-004", "Resaltador Stabilo x4", "Librería", "12.50", 14, 6, 5},
                {"TEC-001", "Mouse inalámbrico Logitech M170", "Tecnología", "39.90", 15, 5, 13},
                {"TEC-002", "Teclado USB Teros", "Tecnología", "35.00", 12, 4, 8},
                {"TEC-003", "Memoria USB Kingston 64 GB", "Tecnología", "29.90", 30, 10, 26},
                {"TEC-004", "Audífonos Xiaomi Redmi Buds", "Tecnología", "89.00", 6, 5, 9},
                {"TEC-005", "Cargador USB-C 20 W Samsung", "Tecnología", "59.90", 10, 5, 17},
                {"TEC-006", "Router TP-Link Archer C6", "Tecnología", "179.00", 4, 3, 3},
                {"TEC-007", "Cable HDMI 2 m", "Tecnología", "15.90", 33, 10, 11},
                {"FER-001", "Foco LED Philips 9 W", "Ferretería", "7.50", 70, 20, 36},
                {"FER-002", "Cinta aislante 3M", "Ferretería", "4.20", 45, 10, 10},
                {"FER-003", "Extensión eléctrica 5 m", "Ferretería", "24.00", 13, 5, 6},
                {"FER-004", "Candado Forte 40 mm", "Ferretería", "22.50", 9, 4, 4},
        };
        for (Object[] d : datos) {
            Producto p = new Producto((String) d[0], (String) d[1], (String) d[2], new BigDecimal((String) d[3]),
                    (Integer) d[4], (Integer) d[5]);
            p.asignarUnidadesVendidas((Integer) d[6]);
            productos.save(p);
        }
        LOG.info("{} productos de demostración creados", datos.length);
    }

    private void crearPedidosDeEjemplo() {
        Long arroz = idDe("ABR-001");
        Long leche = idDe("ABR-006");
        Long inca = idDe("BEB-002");
        Long cuaderno = idDe("LIB-001");
        Long mouse = idDe("TEC-001");
        Long detergente = idDe("LIM-001");

        Long p1 = crear(new NuevoPedido("Bodega Don Pepe", "YAPE", "987654321",
                List.of(new LineaPedido(arroz, 5), new LineaPedido(leche, 24))));
        Long p2 = crear(new NuevoPedido("Librería San Marcos", "PAYPAL", "compras@libreriasanmarcos.pe",
                List.of(new LineaPedido(cuaderno, 30), new LineaPedido(mouse, 2))));
        Long p3 = crear(new NuevoPedido("Minimarket Los Olivos", "PLIN", "912345678",
                List.of(new LineaPedido(inca, 12), new LineaPedido(detergente, 3))));
        crear(new NuevoPedido("Restaurante El Sabor Norteño", "YAPE", "955111222",
                List.of(new LineaPedido(arroz, 10), new LineaPedido(inca, 6))));

        ejecutar(fabricaComandos.procesar(p1));
        ejecutar(fabricaComandos.aplicarDescuento(p2, BigDecimal.valueOf(5)));
        ejecutar(fabricaComandos.procesar(p2));
        ejecutar(fabricaComandos.cancelar(p3));
        LOG.info("Pedidos de ejemplo creados");
    }

    private Long crear(NuevoPedido datos) {
        return ejecutar(fabricaComandos.crear(datos)).pedidoId();
    }

    private ResultadoComando ejecutar(ComandoPedido comando) {
        ResultadoComando resultado = invocador.ejecutar(comando, SISTEMA);
        if (!resultado.exitoso()) {
            LOG.warn("Comando de ejemplo no ejecutado: {}", resultado.mensaje());
        }
        return resultado;
    }

    private Long idDe(String codigo) {
        return productos.findAll().stream().filter(p -> p.getCodigo().equals(codigo)).findFirst()
                .map(Producto::getId).orElseThrow();
    }
}
