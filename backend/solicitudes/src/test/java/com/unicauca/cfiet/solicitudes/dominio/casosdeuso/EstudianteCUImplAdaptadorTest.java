package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.UsuarioCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.AsignaturaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.EstudianteGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.RolGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.UsuarioGatewayIntPuerto;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstudianteCUImplAdaptadorTest {

    private static final String TOKEN = "token";
    private static final String UUID_USUARIO = "uuid-usuario-creado";

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
    private LogCUIntPuerto log;

    private EstudianteCUImplAdaptador casoDeUso;

    private final Rol rolEstudiante = Rol.builder().uuidRol("rol-est").nombre("Estudiante").estado(true).build();
    private final Rol rolDecano = Rol.builder().uuidRol("rol-dec").nombre("Decano").estado(true).build();
    private final TipoUsuario tipoEstudiante = TipoUsuario.builder().uuidTipoUsuario("tipo-est").nombre("Estudiante").build();

    @BeforeEach
    void setUp() {
        casoDeUso = new EstudianteCUImplAdaptador(usuarioCU, gateway, asignaturaGateway, rolGateway,
                usuarioGateway, new ExcepcionesFormateadorImplAdaptador(), log);
    }

    private AsignaturaMatriculada materia(String codigo, String nombre, String grupo) {
        return AsignaturaMatriculada.builder()
                .asignatura(Asignatura.builder().codigoAsignatura(codigo).nombreAsignatura(nombre).build())
                .grupo(grupo)
                .build();
    }

    private Estudiante estudiante(String codigoEstudiantil, AsignaturaMatriculada... materias) {
        Usuario usuario = new Usuario();
        usuario.setNombres("Ana");
        usuario.setApellidos("Perez");
        usuario.setUsername("aperez" + codigoEstudiantil);
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
        when(gateway.existePorCodigoEstudiantil("1001")).thenReturn(false);

        List<Estudiante> lote = List.of(
                estudiante("1001", materia("IS101", "Introduccion", "A")),
                estudiante("1001", materia("IS102", "Programacion", "A")));

        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.crearEstudiantes(lote, TOKEN));

        verificarQueNoSeCreoNada();
        verifyNoInteractions(asignaturaGateway);
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
}
