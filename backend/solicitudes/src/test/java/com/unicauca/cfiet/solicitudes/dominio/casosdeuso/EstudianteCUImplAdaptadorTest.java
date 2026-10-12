package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.UsuarioCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.AsignaturaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.DatosPersonalesGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.EstudianteGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.IJwtServicio;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ProgramaAcademicoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.RolGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SesionGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.UsuarioGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EstadoAsignaturaMatriculadaConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Asignatura;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AsignaturaMatriculada;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Estudiante;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ProgramaAcademico;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Rol;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoUsuario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorEntidadExisteExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorEntidadNoExisteExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorMalFormatoExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorNoInformacionExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorReglaNegocioVioladaExcepcion;
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
    private static final String UUID_ISI = "uuid-isi";
    private static final ProgramaAcademico SISTEMAS = ProgramaAcademico.builder()
            .uuidProgramaAcademico(UUID_ISI).sigla("ISI").nombre("Ingeniería de Sistemas").ultimoConsecutivoAsignatura(5).build();
    private static final ProgramaAcademico ELECTRONICA = ProgramaAcademico.builder()
            .uuidProgramaAcademico("uuid-iet").sigla("IET").nombre("Ingeniería Electrónica y Telecomunicaciones").ultimoConsecutivoAsignatura(5).build();

    @Mock
    private UsuarioCUIntPuerto usuarioCU;

    @Mock
    private EstudianteGatewayIntPuerto gateway;

    @Mock
    private AsignaturaGatewayIntPuerto asignaturaGateway;

    @Mock
    private ProgramaAcademicoGatewayIntPuerto programaGateway;

    @Mock
    private RolGatewayIntPuerto rolGateway;

    @Mock
    private UsuarioGatewayIntPuerto usuarioGateway;

    @Mock
    private DatosPersonalesGatewayIntPuerto datosPersonalesGateway;

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
        casoDeUso = new EstudianteCUImplAdaptador(usuarioCU, gateway, asignaturaGateway, programaGateway, rolGateway,
                usuarioGateway, datosPersonalesGateway, sesionGateway, jwtServicio, new ExcepcionesFormateadorImplAdaptador(), log);
        lenient().when(programaGateway.getPorUuid(UUID_ISI)).thenReturn(SISTEMAS);
        lenient().when(asignaturaGateway.getPorCodigo(anyString())).thenAnswer(invocacion ->
                existente("asig-" + invocacion.getArgument(0), invocacion.getArgument(0), "Materia " + invocacion.getArgument(0)));
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
                .programaAcademico(ProgramaAcademico.builder().uuidProgramaAcademico(UUID_ISI).build())
                .semestre("5")
                .asignaturasMatriculadas(new ArrayList<>(List.of(materias)))
                .build();
    }

    private Estudiante registrado(String codigoEstudiantil, AsignaturaMatriculada... materias) {
        Estudiante estudiante = estudiante(codigoEstudiantil, materias);
        estudiante.setUuidUsuario(UUID_ESTUDIANTE);
        estudiante.setProgramaAcademico(SISTEMAS);
        estudiante.getUsuario().setUuidUsuario(UUID_ESTUDIANTE);
        return estudiante;
    }

    private Asignatura existente(String uuid, String codigo, String nombre) {
        return Asignatura.builder().uuidAsignatura(uuid).codigoAsignatura(codigo).nombreAsignatura(nombre).programaAcademico(SISTEMAS).build();
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
        assertEquals("FIET", creado.getFacultad());
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
        assertSame(creado.getAsignaturasMatriculadas().get(0).getAsignatura().getProgramaAcademico(), SISTEMAS);
    }

    @Test
    void crearEstudianteSinRolEstudianteLanzaEntidadNoExisteYNoCreaNada() {
        when(gateway.existePorCodigoEstudiantil("1001")).thenReturn(false);
        when(rolGateway.getRoles()).thenReturn(List.of(rolDecano));

        assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.crearEstudiante(estudiante("1001", materia("IS101", "Introduccion", "A")), TOKEN));

        verificarQueNoSeCreoNada();
    }

    @Test
    void crearEstudianteSinTipoUsuarioEstudianteLanzaEntidadNoExisteYNoCreaNada() {
        when(gateway.existePorCodigoEstudiantil("1001")).thenReturn(false);
        when(rolGateway.getRoles()).thenReturn(List.of(rolEstudiante));
        when(usuarioGateway.getTipoUsuarioPorNombre("Estudiante")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.crearEstudiante(estudiante("1001", materia("IS101", "Introduccion", "A")), TOKEN));

        verificarQueNoSeCreoNada();
    }

    @Test
    void crearEstudianteConAsignaturaInexistenteLanzaEntidadNoExisteYNoCreaNada() {
        when(gateway.existePorCodigoEstudiantil("1001")).thenReturn(false);
        when(asignaturaGateway.getPorCodigo("ISI-999")).thenReturn(null);

        ErrorEntidadNoExisteExcepcion error = assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.crearEstudiante(estudiante("1001", materia("ISI-999", null, "A")), TOKEN));

        assertEquals(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA_FILTRO, "Asignatura", "codigo", "ISI-999"), error.getMessage());
        verificarQueNoSeCreoNada();
    }

    @Test
    void crearEstudianteConAsignaturaDeOtroProgramaLanzaReglaNegocioYNoCreaNada() {
        when(gateway.existePorCodigoEstudiantil("1001")).thenReturn(false);
        Asignatura ajena = existente("asig-iet", "IET-001", "Circuitos I");
        ajena.setProgramaAcademico(ELECTRONICA);
        when(asignaturaGateway.getPorCodigo("IET-001")).thenReturn(ajena);

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.crearEstudiante(estudiante("1001", materia("IET-001", null, "A")), TOKEN));

        assertEquals(String.format(MensajesError.ASIGNATURA_OTRO_PROGRAMA, "IET-001", "Circuitos I",
                ELECTRONICA.getNombre(), SISTEMAS.getNombre()), error.getMessage());
        verificarQueNoSeCreoNada();
    }

    @Test
    void crearEstudianteConProgramaInexistenteLanzaEntidadNoExisteYNoCreaNada() {
        when(gateway.existePorCodigoEstudiantil("1001")).thenReturn(false);
        Estudiante peticion = estudiante("1001", materia("ISI-001", null, "A"));
        peticion.getProgramaAcademico().setUuidProgramaAcademico("no-existe");

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.crearEstudiante(peticion, TOKEN));

        verify(asignaturaGateway, never()).getPorCodigo(anyString());
        verificarQueNoSeCreoNada();
    }

    @Test
    void crearEstudianteSinProgramaLanzaMalFormatoYNoCreaNada() {
        when(gateway.existePorCodigoEstudiantil("1001")).thenReturn(false);
        Estudiante peticion = estudiante("1001", materia("ISI-001", null, "A"));
        peticion.setProgramaAcademico(null);

        ErrorMalFormatoExcepcion error = assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.crearEstudiante(peticion, TOKEN));

        assertEquals(MensajesError.PROGRAMA_ACADEMICO_REQUERIDO, error.getMessage());
        verificarQueNoSeCreoNada();
    }

    @Test
    void crearEstudianteBuscaElCodigoDeAsignaturaSinEspaciosYEnMayusculas() {
        prepararCreacion();

        Estudiante creado = casoDeUso.crearEstudiante(estudiante("1001", materia(" isi-001 ", null, "A")), TOKEN);

        verify(asignaturaGateway).getPorCodigo("ISI-001");
        assertEquals("asig-ISI-001", creado.getAsignaturasMatriculadas().get(0).getAsignatura().getUuidAsignatura());
        assertSame(SISTEMAS, creado.getProgramaAcademico());
    }

    @Test
    void crearEstudiantesConAsignaturaDeOtroProgramaEnElSegundoNoCreaAlPrimero() {
        when(gateway.existePorCodigoEstudiantil(anyString())).thenReturn(false);
        Asignatura ajena = existente("asig-iet", "IET-001", "Circuitos I");
        ajena.setProgramaAcademico(ELECTRONICA);
        when(asignaturaGateway.getPorCodigo("IET-001")).thenReturn(ajena);

        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> casoDeUso.crearEstudiantes(List.of(
                estudiante("1001", materia("ISI-001", null, "A")),
                estudiante("1002", materia("IET-001", null, "A"))), TOKEN));

        verificarQueNoSeCreoNada();
    }

    @Test
    void crearEstudianteConMateriaExistenteLaReutilizaSinCambiarSuNombre() {
        prepararCreacion();
        when(asignaturaGateway.getPorCodigo("IS101")).thenReturn(existente("asig-1", "IS101", "Introduccion a la Informatica"));

        Estudiante creado = casoDeUso.crearEstudiante(estudiante("1001", materia("IS101", "Otro nombre", "A")), TOKEN);

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
    }

    @Test
    void crearEstudiantesConDocumentoRepetidoEnLaListaNoCreaNada() {
        when(gateway.existePorCodigoEstudiantil(anyString())).thenReturn(false);
        Estudiante segundo = estudiante("1002", materia("IS102", "Programacion", "A"));
        segundo.getUsuario().setNumeroDocumento("D1001");

        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.crearEstudiantes(
                List.of(estudiante("1001", materia("IS101", "Introduccion", "A")), segundo), TOKEN));

        verificarQueNoSeCreoNada();
    }

    @Test
    void crearEstudiantesConCorreoRepetidoEnLaListaNoCreaNada() {
        when(gateway.existePorCodigoEstudiantil(anyString())).thenReturn(false);
        Estudiante segundo = estudiante("1002", materia("IS102", "Programacion", "A"));
        segundo.getUsuario().setCorreoElectronico("E1001@unicauca.edu.co");

        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.crearEstudiantes(
                List.of(estudiante("1001", materia("IS101", "Introduccion", "A")), segundo), TOKEN));

        verificarQueNoSeCreoNada();
    }

    @Test
    void crearEstudiantesConUsernameRepetidoEnLaListaNoCreaNada() {
        when(gateway.existePorCodigoEstudiantil(anyString())).thenReturn(false);
        Estudiante segundo = estudiante("1002", materia("IS102", "Programacion", "A"));
        segundo.getUsuario().setUsername("u1001");

        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.crearEstudiantes(
                List.of(estudiante("1001", materia("IS101", "Introduccion", "A")), segundo), TOKEN));

        verificarQueNoSeCreoNada();
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

    private Estudiante edicion(String codigoEstudiantil, String semestre) {
        return Estudiante.builder().usuario(new Usuario()).codigoEstudiantil(codigoEstudiantil).semestre(semestre).build();
    }

    private void verificarQueNoSeActualizoNada() {
        verify(datosPersonalesGateway, never()).actualizar(anyString(), any());
        verify(gateway, never()).guardar(any());
        verifyNoInteractions(log);
    }

    @Test
    void actualizarEstudianteCambiaDatosPersonalesYAcademicosSinTocarUsernameProgramaNiFacultad() {
        AsignaturaMatriculada materiaActual = matriculada("m1", "IS101", "A", EstadoAsignaturaMatriculadaConstantes.ACTIVA);
        Estudiante actual = registrado("1001", materiaActual);
        actual.setFacultad("Otra");
        when(gateway.getPorUuid(UUID_ESTUDIANTE)).thenReturn(actual);
        when(gateway.guardar(any(Estudiante.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Estudiante cambios = estudiante("2002", materia("IS999", "Otra", "Z"));
        cambios.setProgramaAcademico(ELECTRONICA);
        cambios.setSemestre(" 6 ");
        cambios.setFacultad("FIET Popayan");
        cambios.getUsuario().setNombres(" Maria ");
        cambios.getUsuario().setApellidos("Lopez");
        cambios.getUsuario().setTipoDocumento("tarjeta de identidad");
        cambios.getUsuario().setNumeroDocumento(" D2002 ");
        cambios.getUsuario().setTelefono("3109998877");
        cambios.getUsuario().setCorreoElectronico("maria@unicauca.edu.co");
        cambios.getUsuario().setUsername("otro-username");

        Estudiante actualizado = casoDeUso.actualizarEstudiante(UUID_ESTUDIANTE, cambios, TOKEN);

        assertEquals("2002", actualizado.getCodigoEstudiantil());
        assertSame(SISTEMAS, actualizado.getProgramaAcademico());
        assertEquals("6", actualizado.getSemestre());
        assertEquals("FIET", actualizado.getFacultad());
        assertEquals("Maria", actualizado.getUsuario().getNombres());
        assertEquals("Lopez", actualizado.getUsuario().getApellidos());
        assertEquals(ApplicationConstantes.TARJETA_IDENTIDAD, actualizado.getUsuario().getTipoDocumento());
        assertEquals("D2002", actualizado.getUsuario().getNumeroDocumento());
        assertEquals("3109998877", actualizado.getUsuario().getTelefono());
        assertEquals("maria@unicauca.edu.co", actualizado.getUsuario().getCorreoElectronico());
        assertEquals("u1001", actualizado.getUsuario().getUsername());
        assertEquals(List.of(materiaActual), actualizado.getAsignaturasMatriculadas());

        verify(gateway).existePorCodigoEstudiantilEnOtro("2002", UUID_ESTUDIANTE);
        verify(datosPersonalesGateway).existeNumeroDocumentoEnOtro("D2002", UUID_ESTUDIANTE);
        verify(datosPersonalesGateway).existeCorreoEnOtro("maria@unicauca.edu.co", UUID_ESTUDIANTE);
        ArgumentCaptor<Usuario> guardados = ArgumentCaptor.forClass(Usuario.class);
        verify(datosPersonalesGateway).actualizar(eq(UUID_ESTUDIANTE), guardados.capture());
        assertEquals("Maria", guardados.getValue().getNombres());
        assertEquals("D2002", guardados.getValue().getNumeroDocumento());
        assertNull(guardados.getValue().getUsername());
        verifyNoInteractions(usuarioCU, usuarioGateway, programaGateway);
        verify(log).crearLog(eq("Actualizar estudiante"), anyString(), eq(TOKEN));
    }

    @Test
    void actualizarEstudianteConCamposVaciosNoCambiaNadaDeLoQueYaTiene() {
        Estudiante actual = registrado("1001");
        when(gateway.getPorUuid(UUID_ESTUDIANTE)).thenReturn(actual);
        when(gateway.guardar(any(Estudiante.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        Estudiante cambios = edicion("  ", "");
        cambios.getUsuario().setNombres(" ");
        cambios.getUsuario().setCorreoElectronico("");

        Estudiante actualizado = casoDeUso.actualizarEstudiante(UUID_ESTUDIANTE, cambios, TOKEN);

        assertEquals("1001", actualizado.getCodigoEstudiantil());
        assertEquals("5", actualizado.getSemestre());
        assertEquals("Ana", actualizado.getUsuario().getNombres());
        assertEquals("FIET", actualizado.getFacultad());
        verify(gateway, never()).existePorCodigoEstudiantilEnOtro(anyString(), anyString());
        verifyNoInteractions(datosPersonalesGateway);
        verify(gateway).guardar(actual);
    }

    @Test
    void actualizarEstudianteConCodigoDeOtroEstudianteLanzaEntidadExiste() {
        when(gateway.getPorUuid(UUID_ESTUDIANTE)).thenReturn(registrado("1001"));
        when(gateway.existePorCodigoEstudiantilEnOtro("2002", UUID_ESTUDIANTE)).thenReturn(true);

        ErrorEntidadExisteExcepcion error = assertThrows(ErrorEntidadExisteExcepcion.class,
                () -> casoDeUso.actualizarEstudiante(UUID_ESTUDIANTE, edicion("2002", null), TOKEN));

        assertEquals(String.format(MensajesError.ATRIBUTO_UNICO_YA_EXISTE, "Estudiante", "codigo estudiantil", "2002"), error.getMessage());
        verificarQueNoSeActualizoNada();
    }

    @Test
    void actualizarEstudianteConDocumentoDeOtroUsuarioLanzaEntidadExiste() {
        when(gateway.getPorUuid(UUID_ESTUDIANTE)).thenReturn(registrado("1001"));
        when(datosPersonalesGateway.existeNumeroDocumentoEnOtro("D9999", UUID_ESTUDIANTE)).thenReturn(true);
        Estudiante cambios = edicion("2002", "6");
        cambios.getUsuario().setNumeroDocumento("D9999");

        ErrorEntidadExisteExcepcion error = assertThrows(ErrorEntidadExisteExcepcion.class,
                () -> casoDeUso.actualizarEstudiante(UUID_ESTUDIANTE, cambios, TOKEN));

        assertEquals(String.format(MensajesError.ATRIBUTO_UNICO_YA_EXISTE, "Usuario", "numero de documento", "D9999"), error.getMessage());
        verificarQueNoSeActualizoNada();
    }

    @Test
    void actualizarEstudianteConCorreoDeOtroUsuarioLanzaEntidadExiste() {
        when(gateway.getPorUuid(UUID_ESTUDIANTE)).thenReturn(registrado("1001"));
        when(datosPersonalesGateway.existeCorreoEnOtro("otro@unicauca.edu.co", UUID_ESTUDIANTE)).thenReturn(true);
        Estudiante cambios = edicion(null, null);
        cambios.getUsuario().setCorreoElectronico("otro@unicauca.edu.co");

        ErrorEntidadExisteExcepcion error = assertThrows(ErrorEntidadExisteExcepcion.class,
                () -> casoDeUso.actualizarEstudiante(UUID_ESTUDIANTE, cambios, TOKEN));

        assertEquals(String.format(MensajesError.ATRIBUTO_UNICO_YA_EXISTE, "Usuario", "correo electronico", "otro@unicauca.edu.co"),
                error.getMessage());
        verificarQueNoSeActualizoNada();
    }

    @Test
    void actualizarEstudianteConSusPropiosDatosUnicosLosAceptaPorqueLaConsultaLoExcluye() {
        Estudiante actual = registrado("1001");
        when(gateway.getPorUuid(UUID_ESTUDIANTE)).thenReturn(actual);
        when(gateway.guardar(any(Estudiante.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        Estudiante cambios = edicion("1001", null);
        cambios.getUsuario().setNumeroDocumento("D1001");
        cambios.getUsuario().setCorreoElectronico("e1001@unicauca.edu.co");

        casoDeUso.actualizarEstudiante(UUID_ESTUDIANTE, cambios, TOKEN);

        verify(gateway).existePorCodigoEstudiantilEnOtro("1001", UUID_ESTUDIANTE);
        verify(datosPersonalesGateway).existeNumeroDocumentoEnOtro("D1001", UUID_ESTUDIANTE);
        verify(datosPersonalesGateway).existeCorreoEnOtro("e1001@unicauca.edu.co", UUID_ESTUDIANTE);
        verify(gateway, never()).existePorCodigoEstudiantil(anyString());
        verify(gateway).guardar(actual);
    }

    @Test
    void actualizarEstudianteConTipoDeDocumentoInvalidoLanzaMalFormato() {
        when(gateway.getPorUuid(UUID_ESTUDIANTE)).thenReturn(registrado("1001"));
        Estudiante cambios = edicion(null, null);
        cambios.getUsuario().setTipoDocumento("Pasaporte");

        ErrorMalFormatoExcepcion error = assertThrows(ErrorMalFormatoExcepcion.class,
                () -> casoDeUso.actualizarEstudiante(UUID_ESTUDIANTE, cambios, TOKEN));

        assertEquals(MensajesError.TIPO_DOCUMENTO_ERRONEO, error.getMessage());
        verificarQueNoSeActualizoNada();
    }

    @Test
    void actualizarEstudianteInexistenteLanzaEntidadNoExiste() {
        when(gateway.getPorUuid("no-existe")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.actualizarEstudiante("no-existe", estudiante("1001"), TOKEN));

        verificarQueNoSeActualizoNada();
    }

    @Test
    void agregarAsignaturaMatriculadaConAsignaturaExistenteLaDejaActivaYRegistraLog() {
        when(gateway.getPorUuid(UUID_ESTUDIANTE)).thenReturn(registrado("1001"));
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
    void agregarAsignaturaMatriculadaInexistenteLanzaEntidadNoExisteYNoGuarda() {
        when(gateway.getPorUuid(UUID_ESTUDIANTE)).thenReturn(registrado("1001"));
        when(gateway.getAsignaturasMatriculadas(UUID_ESTUDIANTE)).thenReturn(List.of());
        when(asignaturaGateway.getPorCodigo("ISI-999")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.agregarAsignaturaMatriculada(
                UUID_ESTUDIANTE, materia("ISI-999", null, "A"), TOKEN));

        verify(gateway, never()).guardarAsignaturaMatriculada(anyString(), any());
        verifyNoInteractions(log);
    }

    @Test
    void agregarAsignaturaMatriculadaDeOtroProgramaLanzaReglaNegocioYNoGuarda() {
        when(gateway.getPorUuid(UUID_ESTUDIANTE)).thenReturn(registrado("1001"));
        when(gateway.getAsignaturasMatriculadas(UUID_ESTUDIANTE)).thenReturn(List.of());
        Asignatura ajena = existente("asig-iet", "IET-001", "Circuitos I");
        ajena.setProgramaAcademico(ELECTRONICA);
        when(asignaturaGateway.getPorCodigo("IET-001")).thenReturn(ajena);

        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> casoDeUso.agregarAsignaturaMatriculada(
                UUID_ESTUDIANTE, materia("iet-001", null, "A"), TOKEN));

        verify(gateway, never()).guardarAsignaturaMatriculada(anyString(), any());
        verifyNoInteractions(log);
    }

    @Test
    void agregarAsignaturaMatriculadaYaActivaEnElMismoGrupoLanzaMalFormato() {
        when(gateway.getPorUuid(UUID_ESTUDIANTE)).thenReturn(registrado("1001"));
        when(gateway.getAsignaturasMatriculadas(UUID_ESTUDIANTE)).thenReturn(List.of(
                matriculada("m1", "IS101", "A", EstadoAsignaturaMatriculadaConstantes.ACTIVA)));

        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.agregarAsignaturaMatriculada(
                UUID_ESTUDIANTE, materia("is101", "Introduccion", "a "), TOKEN));

        verifyNoInteractions(asignaturaGateway, log);
        verify(gateway, never()).guardarAsignaturaMatriculada(anyString(), any());
    }

    @Test
    void agregarAsignaturaMatriculadaQueEstuvoCanceladaEnElMismoGrupoEsValida() {
        when(gateway.getPorUuid(UUID_ESTUDIANTE)).thenReturn(registrado("1001"));
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
        when(gateway.getPorUuid("no-existe")).thenReturn(null);

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
