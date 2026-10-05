package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.UsuarioCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.AsignaturaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.EstudianteGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.IJwtServicio;
import com.unicauca.cfiet.solicitudes.aplicacion.output.RolGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SesionGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.UsuarioGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EstadoAsignaturaMatriculadaConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Asignatura;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AsignaturaMatriculada;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Estudiante;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Rol;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoUsuario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorEntidadExisteExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorEntidadNoExisteExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorMalFormatoExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorNoInformacionExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstudianteCUImplAdaptadorTest {

    private static final String TOKEN = "token";
    private static final String UUID_USUARIO = "uuid-usuario-creado";
    private static final String UUID_ESTUDIANTE = "uuid-estudiante";

    @Mock
    private UsuarioCUIntPuerto usuarioCU;

    @Mock
    private EstudianteGatewayIntPuerto gateway;

    @Mock
    private AsignaturaGatewayIntPuerto asignaturaGateway;

    @Mock
    private RolGatewayIntPuerto rolGateway;

    @Mock
    private UsuarioGatewayIntPuerto usuarioGateway;

    @Mock
    private SesionGatewayIntPuerto sesionGateway;

    @Mock
    private IJwtServicio jwtServicio;

    @Mock
    private LogCUIntPuerto log;

    private EstudianteCUImplAdaptador casoDeUso;

    private final Rol rolEstudiante = Rol.builder().uuidRol("rol-est").nombre("Estudiante").estado(true).build();
    private final Rol rolDecano = Rol.builder().uuidRol("rol-dec").nombre("Decano").estado(true).build();
    private final TipoUsuario tipoEstudiante = TipoUsuario.builder().uuidTipoUsuario("tipo-est").nombre("Estudiante").build();

    @BeforeEach
    void setUp() {
        casoDeUso = new EstudianteCUImplAdaptador(usuarioCU, gateway, asignaturaGateway, rolGateway,
                usuarioGateway, sesionGateway, jwtServicio, new ExcepcionesFormateadorImplAdaptador(), log);
    }

    private AsignaturaMatriculada materia(String codigo, String nombre, String grupo) {
        return AsignaturaMatriculada.builder()
                .asignatura(Asignatura.builder().codigoAsignatura(codigo).nombreAsignatura(nombre).build())
                .grupo(grupo)
                .build();
    }

    private AsignaturaMatriculada matriculada(String uuid, String codigo, String grupo, String estado) {
        return AsignaturaMatriculada.builder()
                .uuidAsignaturaMatriculada(uuid)
                .asignatura(existente("asig-" + codigo, codigo, "Materia " + codigo))
                .grupo(grupo)
                .estado(estado)
                .build();
    }

    private Estudiante estudiante(String codigoEstudiantil, AsignaturaMatriculada... materias) {
        Usuario usuario = new Usuario();
        usuario.setNombres("Ana");
        usuario.setApellidos("Perez");
        usuario.setNumeroDocumento("D" + codigoEstudiantil);
        usuario.setCorreoElectronico("e" + codigoEstudiantil + "@unicauca.edu.co");
        usuario.setUsername("u" + codigoEstudiantil);
        usuario.setRoles(new ArrayList<>(List.of(rolDecano)));
        return Estudiante.builder()
                .usuario(usuario)
                .codigoEstudiantil(codigoEstudiantil)
                .programaAcademico("Ingenieria de Sistemas")
                .semestre("5")
                .facultad("FIET")
                .asignaturasMatriculadas(new ArrayList<>(List.of(materias)))
                .build();
    }

    private Estudiante registrado(String codigoEstudiantil, AsignaturaMatriculada... materias) {
        Estudiante estudiante = estudiante(codigoEstudiantil, materias);
        estudiante.setUuidUsuario(UUID_ESTUDIANTE);
        estudiante.getUsuario().setUuidUsuario(UUID_ESTUDIANTE);
        return estudiante;
    }

    private Asignatura existente(String uuid, String codigo, String nombre) {
        return Asignatura.builder().uuidAsignatura(uuid).codigoAsignatura(codigo).nombreAsignatura(nombre).build();
    }

    private void prepararCreacion() {
        when(gateway.existePorCodigoEstudiantil(anyString())).thenReturn(false);
        when(rolGateway.getRoles()).thenReturn(List.of(rolDecano, rolEstudiante));
        when(usuarioGateway.getTipoUsuarioPorNombre("Estudiante")).thenReturn(tipoEstudiante);
        when(usuarioCU.crearUsuario(any(Usuario.class), eq("ESTUDIANTE"), eq(TOKEN))).thenAnswer(invocacion -> {
            Usuario usuario = invocacion.getArgument(0);
            usuario.setUuidUsuario(UUID_USUARIO);
            return usuario;
        });
        when(gateway.guardar(any(Estudiante.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    private void verificarQueNoSeCreoNada() {
        verifyNoInteractions(usuarioCU);
        verify(asignaturaGateway, never()).guardar(any());
        verify(gateway, never()).guardar(any());
        verifyNoInteractions(log);
    }

    @Test
    void crearEstudianteCreaUsuarioAntesDeGuardarConRolYTipoEstudianteYMateriasActivas() {
        prepararCreacion();
        when(asignaturaGateway.getPorCodigo("IS101")).thenReturn(existente("asig-1", "IS101", "Introduccion"));

        Estudiante creado = casoDeUso.crearEstudiante(estudiante("1001", materia("IS101", "Introduccion", "A")), TOKEN);

        InOrder orden = inOrder(usuarioCU, gateway);
        orden.verify(usuarioCU).crearUsuario(any(Usuario.class), eq("ESTUDIANTE"), eq(TOKEN));
        orden.verify(gateway).guardar(any(Estudiante.class));

        ArgumentCaptor<Usuario> captorUsuario = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioCU).crearUsuario(captorUsuario.capture(), eq("ESTUDIANTE"), eq(TOKEN));
        assertEquals(1, captorUsuario.getValue().getRoles().size());
        assertSame(rolEstudiante, captorUsuario.getValue().getRoles().get(0));
        assertSame(tipoEstudiante, captorUsuario.getValue().getObjTipoUsuario());

        assertEquals(UUID_USUARIO, creado.getUuidUsuario());
        AsignaturaMatriculada matriculada = creado.getAsignaturasMatriculadas().get(0);
        assertNotNull(matriculada.getUuidAsignaturaMatriculada());
        assertEquals(EstadoAsignaturaMatriculadaConstantes.ACTIVA, matriculada.getEstado());
        assertEquals("asig-1", matriculada.getAsignatura().getUuidAsignatura());
        verify(log).crearLog(eq("Crear estudiante"), anyString(), eq(TOKEN));
    }

    @Test
    void crearEstudianteConCodigoEstudiantilRepetidoNoCreaNada() {
        when(gateway.existePorCodigoEstudiantil("1001")).thenReturn(true);

        assertThrows(ErrorEntidadExisteExcepcion.class,
                () -> casoDeUso.crearEstudiante(estudiante("1001", materia("IS101", "Introduccion", "A")), TOKEN));

        verificarQueNoSeCreoNada();
        verifyNoInteractions(asignaturaGateway);
    }

    @Test
    void crearEstudianteConDocumentoRepetidoNoCreaNada() {
        when(gateway.existePorCodigoEstudiantil("1001")).thenReturn(false);
        when(usuarioGateway.existeUsuarioNumeroDocumento("D1001")).thenReturn(true);

        assertThrows(ErrorEntidadExisteExcepcion.class,
                () -> casoDeUso.crearEstudiante(estudiante("1001", materia("IS101", "Introduccion", "A")), TOKEN));

        verificarQueNoSeCreoNada();
        verifyNoInteractions(asignaturaGateway);
    }

    @Test
    void crearEstudianteConCorreoRepetidoNoCreaNada() {
        when(gateway.existePorCodigoEstudiantil("1001")).thenReturn(false);
        when(usuarioGateway.existeUsuarioCorreo("e1001@unicauca.edu.co")).thenReturn(true);

        assertThrows(ErrorEntidadExisteExcepcion.class,
                () -> casoDeUso.crearEstudiante(estudiante("1001", materia("IS101", "Introduccion", "A")), TOKEN));

        verificarQueNoSeCreoNada();
        verifyNoInteractions(asignaturaGateway);
    }

    @Test
    void crearEstudianteConUsernameRepetidoNoCreaNada() {
        when(gateway.existePorCodigoEstudiantil("1001")).thenReturn(false);
        when(usuarioGateway.existeUsuarioUsername("u1001")).thenReturn(true);

        assertThrows(ErrorEntidadExisteExcepcion.class,
                () -> casoDeUso.crearEstudiante(estudiante("1001", materia("IS101", "Introduccion", "A")), TOKEN));

        verificarQueNoSeCreoNada();
        verifyNoInteractions(asignaturaGateway);
    }

    @Test
    void crearEstudianteConMateriaRepetidaEnMismoGrupoNoCreaNada() {
        when(gateway.existePorCodigoEstudiantil("1001")).thenReturn(false);

        Estudiante peticion = estudiante("1001",
                materia("IS101", "Introduccion", "A"),
                materia("is101 ", "Introduccion", "a"));

        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.crearEstudiante(peticion, TOKEN));

        verificarQueNoSeCreoNada();
        verifyNoInteractions(asignaturaGateway);
    }

    @Test
    void crearEstudianteConMismaMateriaEnGruposDistintosEsValido() {
        prepararCreacion();
        when(asignaturaGateway.getPorCodigo("IS101")).thenReturn(existente("asig-1", "IS101", "Introduccion"));

        Estudiante creado = casoDeUso.crearEstudiante(estudiante("1001",
                materia("IS101", "Introduccion", "A"),
                materia("IS101", "Introduccion", "B")), TOKEN);

        assertEquals(2, creado.getAsignaturasMatriculadas().size());
        verify(asignaturaGateway, times(1)).getPorCodigo("IS101");
    }

    @Test
    void crearEstudianteSinRolEstudianteLanzaEntidadNoExisteYNoCreaNada() {
        when(gateway.existePorCodigoEstudiantil("1001")).thenReturn(false);
        when(rolGateway.getRoles()).thenReturn(List.of(rolDecano));

        assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.crearEstudiante(estudiante("1001", materia("IS101", "Introduccion", "A")), TOKEN));

        verificarQueNoSeCreoNada();
        verifyNoInteractions(asignaturaGateway);
    }

    @Test
    void crearEstudianteSinTipoUsuarioEstudianteLanzaEntidadNoExisteYNoCreaNada() {
        when(gateway.existePorCodigoEstudiantil("1001")).thenReturn(false);
        when(rolGateway.getRoles()).thenReturn(List.of(rolEstudiante));
        when(usuarioGateway.getTipoUsuarioPorNombre("Estudiante")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.crearEstudiante(estudiante("1001", materia("IS101", "Introduccion", "A")), TOKEN));

        verificarQueNoSeCreoNada();
        verifyNoInteractions(asignaturaGateway);
    }

    @Test
    void crearEstudianteConMateriaNuevaLaCreaEnElCatalogo() {
        prepararCreacion();
        when(asignaturaGateway.getPorCodigo("IS999")).thenReturn(null);
        when(asignaturaGateway.guardar(any(Asignatura.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Estudiante creado = casoDeUso.crearEstudiante(estudiante("1001", materia("IS999", "Materia Nueva", "A")), TOKEN);

        ArgumentCaptor<Asignatura> captor = ArgumentCaptor.forClass(Asignatura.class);
        verify(asignaturaGateway).guardar(captor.capture());
        assertNotNull(captor.getValue().getUuidAsignatura());
        assertEquals("IS999", captor.getValue().getCodigoAsignatura());
        assertEquals("Materia Nueva", captor.getValue().getNombreAsignatura());
        assertEquals(captor.getValue().getUuidAsignatura(),
                creado.getAsignaturasMatriculadas().get(0).getAsignatura().getUuidAsignatura());

        InOrder orden = inOrder(asignaturaGateway, usuarioCU);
        orden.verify(asignaturaGateway).guardar(any(Asignatura.class));
        orden.verify(usuarioCU).crearUsuario(any(Usuario.class), eq("ESTUDIANTE"), eq(TOKEN));
    }

    @Test
    void crearEstudianteConMateriaExistenteLaReutilizaSinCambiarSuNombre() {
        prepararCreacion();
        when(asignaturaGateway.getPorCodigo("IS101")).thenReturn(existente("asig-1", "IS101", "Introduccion a la Informatica"));

        Estudiante creado = casoDeUso.crearEstudiante(estudiante("1001", materia("IS101", "Otro nombre", "A")), TOKEN);

        verify(asignaturaGateway, never()).guardar(any());
        Asignatura usada = creado.getAsignaturasMatriculadas().get(0).getAsignatura();
        assertEquals("asig-1", usada.getUuidAsignatura());
        assertEquals("Introduccion a la Informatica", usada.getNombreAsignatura());
    }

    @Test
    void crearEstudiantesConCodigoEstudiantilRepetidoEnLaListaNoCreaNada() {
        when(gateway.existePorCodigoEstudiantil(anyString())).thenReturn(false);

        List<Estudiante> lote = List.of(
                estudiante("1001", materia("IS101", "Introduccion", "A")),
                estudiante("1001", materia("IS102", "Programacion", "A")));

        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.crearEstudiantes(lote, TOKEN));

        verificarQueNoSeCreoNada();
        verifyNoInteractions(asignaturaGateway);
    }

    @Test
    void crearEstudiantesConDocumentoRepetidoEnLaListaNoCreaNada() {
        when(gateway.existePorCodigoEstudiantil(anyString())).thenReturn(false);
        Estudiante segundo = estudiante("1002", materia("IS102", "Programacion", "A"));
        segundo.getUsuario().setNumeroDocumento("D1001");

        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.crearEstudiantes(
                List.of(estudiante("1001", materia("IS101", "Introduccion", "A")), segundo), TOKEN));

        verificarQueNoSeCreoNada();
        verifyNoInteractions(asignaturaGateway);
    }

    @Test
    void crearEstudiantesConCorreoRepetidoEnLaListaNoCreaNada() {
        when(gateway.existePorCodigoEstudiantil(anyString())).thenReturn(false);
        Estudiante segundo = estudiante("1002", materia("IS102", "Programacion", "A"));
        segundo.getUsuario().setCorreoElectronico("E1001@unicauca.edu.co");

        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.crearEstudiantes(
                List.of(estudiante("1001", materia("IS101", "Introduccion", "A")), segundo), TOKEN));

        verificarQueNoSeCreoNada();
        verifyNoInteractions(asignaturaGateway);
    }

    @Test
    void crearEstudiantesConUsernameRepetidoEnLaListaNoCreaNada() {
        when(gateway.existePorCodigoEstudiantil(anyString())).thenReturn(false);
        Estudiante segundo = estudiante("1002", materia("IS102", "Programacion", "A"));
        segundo.getUsuario().setUsername("u1001");

        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.crearEstudiantes(
                List.of(estudiante("1001", materia("IS101", "Introduccion", "A")), segundo), TOKEN));

        verificarQueNoSeCreoNada();
        verifyNoInteractions(asignaturaGateway);
    }

    @Test
    void crearEstudiantesConDocumentoYaRegistradoEnElSegundoNoCreaAlPrimero() {
        when(gateway.existePorCodigoEstudiantil(anyString())).thenReturn(false);
        when(usuarioGateway.existeUsuarioNumeroDocumento(anyString()))
                .thenAnswer(invocacion -> "D1002".equals(invocacion.getArgument(0)));

        assertThrows(ErrorEntidadExisteExcepcion.class, () -> casoDeUso.crearEstudiantes(List.of(
                estudiante("1001", materia("IS101", "Introduccion", "A")),
                estudiante("1002", materia("IS102", "Programacion", "A"))), TOKEN));

        verificarQueNoSeCreoNada();
    }

    @Test
    void crearEstudiantesValidaTodoAntesDeCrearAlPrimero() {
        when(gateway.existePorCodigoEstudiantil("1001")).thenReturn(false);
        when(gateway.existePorCodigoEstudiantil("1002")).thenReturn(true);

        List<Estudiante> lote = List.of(
                estudiante("1001", materia("IS101", "Introduccion", "A")),
                estudiante("1002", materia("IS102", "Programacion", "A")));

        assertThrows(ErrorEntidadExisteExcepcion.class, () -> casoDeUso.crearEstudiantes(lote, TOKEN));

        verificarQueNoSeCreoNada();
    }

    @Test
    void crearEstudiantesCreaCadaEstudiante() {
        prepararCreacion();
        when(asignaturaGateway.getPorCodigo(anyString())).thenAnswer(invocacion ->
                existente("asig-" + invocacion.getArgument(0), invocacion.getArgument(0), "Materia"));

        List<Estudiante> creados = casoDeUso.crearEstudiantes(List.of(
                estudiante("1001", materia("IS101", "Introduccion", "A")),
                estudiante("1002", materia("IS102", "Programacion", "A"))), TOKEN);

        assertEquals(2, creados.size());
        verify(usuarioCU, times(2)).crearUsuario(any(Usuario.class), eq("ESTUDIANTE"), eq(TOKEN));
        verify(gateway, times(2)).guardar(any(Estudiante.class));
    }

    @Test
    void crearEstudiantesConListaVaciaLanzaSinInformacion() {
        assertThrows(ErrorNoInformacionExcepcion.class, () -> casoDeUso.crearEstudiantes(List.of(), TOKEN));
        verificarQueNoSeCreoNada();
    }

    @Test
    void getEstudiantesPaginadoDevuelveLaPaginaDelGateway() {
        PaginacionRespuestaDTO<Estudiante> pagina = new PaginacionRespuestaDTO<>(List.of(registrado("1001")), 1L);
        when(gateway.getPaginado(0, 10)).thenReturn(pagina);

        assertSame(pagina, casoDeUso.getEstudiantesPaginado(0, 10));
    }

    @Test
    void getEstudiantesPaginadoSinResultadosDevuelvePaginaVacia() {
        when(gateway.getPaginado(0, 10)).thenReturn(new PaginacionRespuestaDTO<>(List.of(), 0L));

        PaginacionRespuestaDTO<Estudiante> respuesta = casoDeUso.getEstudiantesPaginado(0, 10);

        assertTrue(respuesta.getContent().isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"-1, 10", "0, 0", "0, -5"})
    void getEstudiantesPaginadoConPaginacionInvalidaLanzaMalFormato(int pagina, int tamanio) {
        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.getEstudiantesPaginado(pagina, tamanio));
        verify(gateway, never()).getPaginado(anyInt(), anyInt());
    }

    @Test
    void getEstudiantesPorFiltroPasaLosFiltrosAlGateway() {
        PaginacionRespuestaDTO<Estudiante> pagina = new PaginacionRespuestaDTO<>(List.of(registrado("1001")), 1L);
        when(gateway.getPorFiltro("Ana", "Perez", "1001", 0, 5)).thenReturn(pagina);

        assertSame(pagina, casoDeUso.getEstudiantesPorFiltro("Ana", "Perez", "1001", 0, 5));
    }

    @Test
    void getEstudiantesPorFiltroSinResultadosDevuelvePaginaVacia() {
        when(gateway.getPorFiltro(null, null, "9999", 0, 5)).thenReturn(new PaginacionRespuestaDTO<>(List.of(), 0L));

        PaginacionRespuestaDTO<Estudiante> respuesta = casoDeUso.getEstudiantesPorFiltro(null, null, "9999", 0, 5);

        assertTrue(respuesta.getContent().isEmpty());
    }

    @Test
    void getEstudiantesPorFiltroConPaginacionInvalidaLanzaMalFormato() {
        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.getEstudiantesPorFiltro("Ana", null, null, -1, 5));
        verifyNoInteractions(gateway);
    }

    @Test
    void getEstudianteExistenteLoDevuelve() {
        Estudiante estudiante = registrado("1001");
        when(gateway.getPorUuid(UUID_ESTUDIANTE)).thenReturn(estudiante);

        assertSame(estudiante, casoDeUso.getEstudiante(UUID_ESTUDIANTE));
    }

    @Test
    void getEstudianteInexistenteLanzaEntidadNoExiste() {
        when(gateway.getPorUuid("no-existe")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.getEstudiante("no-existe"));
    }

    @Test
    void getMisAsignaturasDevuelveSoloLasActivasDelEstudianteDelToken() {
        Usuario usuario = registrado("1001").getUsuario();
        when(jwtServicio.getUsername(TOKEN)).thenReturn("u1001");
        when(sesionGateway.getUsuario("u1001")).thenReturn(usuario);
        when(gateway.existePorUuid(UUID_ESTUDIANTE)).thenReturn(true);
        when(gateway.getAsignaturasMatriculadas(UUID_ESTUDIANTE)).thenReturn(List.of(
                matriculada("m1", "IS101", "A", EstadoAsignaturaMatriculadaConstantes.ACTIVA),
                matriculada("m2", "IS102", "A", EstadoAsignaturaMatriculadaConstantes.CANCELADA),
                matriculada("m3", "IS103", "A", EstadoAsignaturaMatriculadaConstantes.APROBADA),
                matriculada("m4", "IS104", "B", EstadoAsignaturaMatriculadaConstantes.ACTIVA)));

        List<AsignaturaMatriculada> activas = casoDeUso.getMisAsignaturas(TOKEN);

        assertEquals(List.of("m1", "m4"), activas.stream().map(AsignaturaMatriculada::getUuidAsignaturaMatriculada).toList());
    }

    @Test
    void getMisAsignaturasDeUsuarioQueNoEsEstudianteLanzaEntidadNoExiste() {
        Usuario usuario = registrado("1001").getUsuario();
        when(jwtServicio.getUsername(TOKEN)).thenReturn("u1001");
        when(sesionGateway.getUsuario("u1001")).thenReturn(usuario);
        when(gateway.existePorUuid(UUID_ESTUDIANTE)).thenReturn(false);

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.getMisAsignaturas(TOKEN));
        verify(gateway, never()).getAsignaturasMatriculadas(anyString());
    }

    @Test
    void getMisAsignaturasConUsuarioInexistenteLanzaEntidadNoExiste() {
        when(jwtServicio.getUsername(TOKEN)).thenReturn("fantasma");
        when(sesionGateway.getUsuario("fantasma")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.getMisAsignaturas(TOKEN));
        verify(gateway, never()).getAsignaturasMatriculadas(anyString());
    }

    @Test
    void actualizarEstudianteSoloCambiaDatosAcademicosYRegistraLog() {
        AsignaturaMatriculada materiaActual = matriculada("m1", "IS101", "A", EstadoAsignaturaMatriculadaConstantes.ACTIVA);
        Estudiante actual = registrado("1001", materiaActual);
        Usuario usuarioActual = actual.getUsuario();
        when(gateway.getPorUuid(UUID_ESTUDIANTE)).thenReturn(actual);
        when(gateway.existePorCodigoEstudiantil("2002")).thenReturn(false);
        when(gateway.guardar(any(Estudiante.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Estudiante cambios = estudiante("2002", materia("IS999", "Otra", "Z"));
        cambios.setProgramaAcademico("Ingenieria Electronica");
        cambios.setSemestre("6");
        cambios.setFacultad("FIET Popayan");
        cambios.getUsuario().setNombres("Otro nombre");

        Estudiante actualizado = casoDeUso.actualizarEstudiante(UUID_ESTUDIANTE, cambios, TOKEN);

        assertEquals("2002", actualizado.getCodigoEstudiantil());
        assertEquals("Ingenieria Electronica", actualizado.getProgramaAcademico());
        assertEquals("6", actualizado.getSemestre());
        assertEquals("FIET Popayan", actualizado.getFacultad());
        assertSame(usuarioActual, actualizado.getUsuario());
        assertEquals("Ana", actualizado.getUsuario().getNombres());
        assertEquals(List.of(materiaActual), actualizado.getAsignaturasMatriculadas());
        verifyNoInteractions(usuarioCU, usuarioGateway, asignaturaGateway);
        verify(log).crearLog(eq("Actualizar estudiante"), anyString(), eq(TOKEN));
    }

    @Test
    void actualizarEstudianteConCodigoDeOtroEstudianteLanzaEntidadExiste() {
        when(gateway.getPorUuid(UUID_ESTUDIANTE)).thenReturn(registrado("1001"));
        when(gateway.existePorCodigoEstudiantil("2002")).thenReturn(true);

        assertThrows(ErrorEntidadExisteExcepcion.class,
                () -> casoDeUso.actualizarEstudiante(UUID_ESTUDIANTE, estudiante("2002"), TOKEN));

        verify(gateway, never()).guardar(any());
        verifyNoInteractions(log);
    }

    @Test
    void actualizarEstudianteConSuMismoCodigoNoConsultaDuplicados() {
        when(gateway.getPorUuid(UUID_ESTUDIANTE)).thenReturn(registrado("ab1001"));
        when(gateway.guardar(any(Estudiante.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        casoDeUso.actualizarEstudiante(UUID_ESTUDIANTE, estudiante(" AB1001 "), TOKEN);

        verify(gateway, never()).existePorCodigoEstudiantil(anyString());
        verify(gateway).guardar(any(Estudiante.class));
    }

    @Test
    void actualizarEstudianteInexistenteLanzaEntidadNoExiste() {
        when(gateway.getPorUuid("no-existe")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.actualizarEstudiante("no-existe", estudiante("1001"), TOKEN));

        verify(gateway, never()).guardar(any());
    }

    @Test
    void agregarAsignaturaMatriculadaConAsignaturaExistenteLaDejaActivaYRegistraLog() {
        when(gateway.existePorUuid(UUID_ESTUDIANTE)).thenReturn(true);
        when(gateway.getAsignaturasMatriculadas(UUID_ESTUDIANTE)).thenReturn(List.of());
        when(asignaturaGateway.getPorCodigo("IS101")).thenReturn(existente("asig-1", "IS101", "Introduccion"));
        when(gateway.guardarAsignaturaMatriculada(eq(UUID_ESTUDIANTE), any(AsignaturaMatriculada.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(1));

        AsignaturaMatriculada agregada = casoDeUso.agregarAsignaturaMatriculada(
                UUID_ESTUDIANTE, materia("IS101", "Otro nombre", "A"), TOKEN);

        assertNotNull(agregada.getUuidAsignaturaMatriculada());
        assertEquals(EstadoAsignaturaMatriculadaConstantes.ACTIVA, agregada.getEstado());
        assertEquals("asig-1", agregada.getAsignatura().getUuidAsignatura());
        assertEquals("Introduccion", agregada.getAsignatura().getNombreAsignatura());
        verify(asignaturaGateway, never()).guardar(any());
        verify(log).crearLog(eq("Agregar asignatura matriculada"), anyString(), eq(TOKEN));
    }

    @Test
    void agregarAsignaturaMatriculadaConAsignaturaNuevaLaCreaEnElCatalogo() {
        when(gateway.existePorUuid(UUID_ESTUDIANTE)).thenReturn(true);
        when(gateway.getAsignaturasMatriculadas(UUID_ESTUDIANTE)).thenReturn(List.of());
        when(asignaturaGateway.getPorCodigo("IS999")).thenReturn(null);
        when(asignaturaGateway.guardar(any(Asignatura.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        when(gateway.guardarAsignaturaMatriculada(eq(UUID_ESTUDIANTE), any(AsignaturaMatriculada.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(1));

        AsignaturaMatriculada agregada = casoDeUso.agregarAsignaturaMatriculada(
                UUID_ESTUDIANTE, materia("IS999", "Materia Nueva", "A"), TOKEN);

        assertNotNull(agregada.getAsignatura().getUuidAsignatura());
        assertEquals("Materia Nueva", agregada.getAsignatura().getNombreAsignatura());
        verify(asignaturaGateway).guardar(any(Asignatura.class));
    }

    @Test
    void agregarAsignaturaMatriculadaYaActivaEnElMismoGrupoLanzaMalFormato() {
        when(gateway.existePorUuid(UUID_ESTUDIANTE)).thenReturn(true);
        when(gateway.getAsignaturasMatriculadas(UUID_ESTUDIANTE)).thenReturn(List.of(
                matriculada("m1", "IS101", "A", EstadoAsignaturaMatriculadaConstantes.ACTIVA)));

        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.agregarAsignaturaMatriculada(
                UUID_ESTUDIANTE, materia("is101", "Introduccion", "a "), TOKEN));

        verifyNoInteractions(asignaturaGateway, log);
        verify(gateway, never()).guardarAsignaturaMatriculada(anyString(), any());
    }

    @Test
    void agregarAsignaturaMatriculadaQueEstuvoCanceladaEnElMismoGrupoEsValida() {
        when(gateway.existePorUuid(UUID_ESTUDIANTE)).thenReturn(true);
        when(gateway.getAsignaturasMatriculadas(UUID_ESTUDIANTE)).thenReturn(List.of(
                matriculada("m1", "IS101", "A", EstadoAsignaturaMatriculadaConstantes.CANCELADA)));
        when(asignaturaGateway.getPorCodigo("IS101")).thenReturn(existente("asig-IS101", "IS101", "Materia IS101"));
        when(gateway.guardarAsignaturaMatriculada(eq(UUID_ESTUDIANTE), any(AsignaturaMatriculada.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(1));

        AsignaturaMatriculada agregada = casoDeUso.agregarAsignaturaMatriculada(
                UUID_ESTUDIANTE, materia("IS101", "Materia IS101", "A"), TOKEN);

        assertEquals(EstadoAsignaturaMatriculadaConstantes.ACTIVA, agregada.getEstado());
    }

    @Test
    void agregarAsignaturaMatriculadaAEstudianteInexistenteLanzaEntidadNoExiste() {
        when(gateway.existePorUuid("no-existe")).thenReturn(false);

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.agregarAsignaturaMatriculada(
                "no-existe", materia("IS101", "Introduccion", "A"), TOKEN));

        verifyNoInteractions(asignaturaGateway, log);
    }

    @ParameterizedTest
    @ValueSource(strings = {"cancelada", "aprobada", "perdida", " CANCELADA "})
    void cambiarEstadoDeActivaAUnEstadoFinalLoGuardaYRegistraLog(String estado) {
        when(gateway.getAsignaturaMatriculada(UUID_ESTUDIANTE, "m1"))
                .thenReturn(matriculada("m1", "IS101", "A", EstadoAsignaturaMatriculadaConstantes.ACTIVA));
        when(gateway.guardarAsignaturaMatriculada(eq(UUID_ESTUDIANTE), any(AsignaturaMatriculada.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(1));

        AsignaturaMatriculada cambiada = casoDeUso.cambiarEstadoAsignatura(UUID_ESTUDIANTE, "m1", estado, TOKEN);

        assertEquals(estado.trim().toLowerCase(), cambiada.getEstado());
        verify(log).crearLog(eq("Cambiar estado asignatura matriculada"), anyString(), eq(TOKEN));
    }

    @ParameterizedTest
    @ValueSource(strings = {"retirada", "", "activo"})
    void cambiarEstadoConEstadoNoValidoLanzaMalFormato(String estado) {
        assertThrows(ErrorMalFormatoExcepcion.class,
                () -> casoDeUso.cambiarEstadoAsignatura(UUID_ESTUDIANTE, "m1", estado, TOKEN));

        verifyNoInteractions(gateway, log);
    }

    @Test
    void cambiarEstadoConEstadoNuloLanzaMalFormato() {
        assertThrows(ErrorMalFormatoExcepcion.class,
                () -> casoDeUso.cambiarEstadoAsignatura(UUID_ESTUDIANTE, "m1", null, TOKEN));

        verifyNoInteractions(gateway, log);
    }

    @Test
    void cambiarEstadoDeMatriculaDeOtroEstudianteLanzaEntidadNoExiste() {
        when(gateway.getAsignaturaMatriculada(UUID_ESTUDIANTE, "matricula-ajena")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.cambiarEstadoAsignatura(UUID_ESTUDIANTE, "matricula-ajena", "cancelada", TOKEN));

        verify(gateway, never()).guardarAsignaturaMatriculada(anyString(), any());
        verifyNoInteractions(log);
    }

    @Test
    void cambiarEstadoDeActivaAActivaLanzaMalFormato() {
        when(gateway.getAsignaturaMatriculada(UUID_ESTUDIANTE, "m1"))
                .thenReturn(matriculada("m1", "IS101", "A", EstadoAsignaturaMatriculadaConstantes.ACTIVA));

        assertThrows(ErrorMalFormatoExcepcion.class,
                () -> casoDeUso.cambiarEstadoAsignatura(UUID_ESTUDIANTE, "m1", "activa", TOKEN));

        verify(gateway, never()).guardarAsignaturaMatriculada(anyString(), any());
        verifyNoInteractions(log);
    }

    @ParameterizedTest
    @CsvSource({
            "cancelada, activa",
            "cancelada, aprobada",
            "aprobada, activa",
            "aprobada, perdida",
            "perdida, activa",
            "perdida, cancelada"
    })
    void cambiarEstadoDeMateriaQueYaNoEstaActivaLanzaMalFormato(String actual, String nuevo) {
        when(gateway.getAsignaturaMatriculada(UUID_ESTUDIANTE, "m1"))
                .thenReturn(matriculada("m1", "IS101", "A", actual));

        assertThrows(ErrorMalFormatoExcepcion.class,
                () -> casoDeUso.cambiarEstadoAsignatura(UUID_ESTUDIANTE, "m1", nuevo, TOKEN));

        verify(gateway, never()).guardarAsignaturaMatriculada(anyString(), any());
        verifyNoInteractions(log);
    }
}
