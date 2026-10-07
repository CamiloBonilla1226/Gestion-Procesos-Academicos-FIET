package com.unicauca.cfiet.solicitudes.dominio.servicios;

import com.unicauca.cfiet.solicitudes.aplicacion.output.UsuarioGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorEntidadNoExisteExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorReglaNegocioVioladaExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidadorActorSolicitudTest {

    private static final String RADICADO = "2026-CA-0007";
    private static final String NOMBRE_TIPO = "Cancelación de Asignatura";
    private static final String DUENIO = "est-duenio";
    private static final String OTRO_ESTUDIANTE = "est-otro";
    private static final String ASIGNADO = "fun-asignado";
    private static final String NO_ASIGNADO = "fun-otro";
    private static final String DECANO = "dec-1";
    private static final String SECRETARIO = "sec-1";
    private static final String COMITE = "comite-1";

    @Mock
    private UsuarioGatewayIntPuerto usuarioGateway;

    private ValidadorActorSolicitud validador;

    private final SolicitudAcademica solicitud = SolicitudAcademica.builder()
            .uuidSolicitudAcademica("sol-1")
            .radicado(RADICADO)
            .estudiante(Estudiante.builder().uuidUsuario(DUENIO).build())
            .tipoSolicitudAcademica(TipoSolicitudAcademica.builder()
                    .uuidTipoSolicitudAcademica("tipo-ca")
                    .nombre(NOMBRE_TIPO)
                    .funcionarioAcademico(FuncionarioAcademico.builder().uuidUsuario(ASIGNADO).build())
                    .build())
            .build();

    @BeforeEach
    void setUp() {
        validador = new ValidadorActorSolicitud(usuarioGateway, new ExcepcionesFormateadorImplAdaptador());
        registrar(DUENIO, "Estudiante");
        registrar(OTRO_ESTUDIANTE, "Estudiante");
        registrar(ASIGNADO, "Funcionario Académico");
        registrar(NO_ASIGNADO, "Funcionario Académico");
        registrar(DECANO, "Decano");
        registrar(SECRETARIO, "Secretario General");
        registrar(COMITE, "Funcionario");
    }

    private void registrar(String uuid, String... roles) {
        List<Rol> lista = new ArrayList<>();
        for (String rol : roles)
            lista.add(Rol.builder().uuidRol("rol-" + rol).nombre(rol).estado(true).build());
        lenient().when(usuarioGateway.getUsuario(uuid)).thenReturn(Usuario.builder().uuidUsuario(uuid).roles(lista).build());
    }

    private ActorSolicitud actor(String uuid, RolEtiquetaEtapa rol) {
        return ActorSolicitud.builder().uuidUsuario(uuid).rol(rol).build();
    }

    static Stream<Arguments> actoresValidos() {
        return Stream.of(
                Arguments.of(DUENIO, RolEtiquetaEtapa.ESTUDIANTE),
                Arguments.of(ASIGNADO, RolEtiquetaEtapa.FUNCIONARIO),
                Arguments.of(DECANO, RolEtiquetaEtapa.DECANO));
    }

    @ParameterizedTest
    @MethodSource("actoresValidos")
    void aceptaAlDuenioAlFuncionarioAsignadoYAlDecano(String uuid, RolEtiquetaEtapa rol) {
        assertDoesNotThrow(() -> validador.validar(solicitud, actor(uuid, rol)));
    }

    @Test
    void aceptaAlDecanoEnCualquierSolicitudSinImportarDuenioNiFuncionario() {
        SolicitudAcademica sinDuenioNiFuncionario = SolicitudAcademica.builder()
                .radicado("2026-CM-0002")
                .tipoSolicitudAcademica(TipoSolicitudAcademica.builder().nombre("Cancelación de Matrícula").build())
                .build();

        assertDoesNotThrow(() -> validador.validar(sinDuenioNiFuncionario, actor(DECANO, RolEtiquetaEtapa.DECANO)));
    }

    @Test
    void rechazaAlEstudianteQueNoEsDuenio() {
        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> validador.validar(solicitud, actor(OTRO_ESTUDIANTE, RolEtiquetaEtapa.ESTUDIANTE)));

        assertEquals(String.format(MensajesError.SOLICITUD_AJENA, RADICADO, OTRO_ESTUDIANTE), error.getMessage());
    }

    @Test
    void rechazaAlEstudianteSiLaSolicitudNoTieneEstudiante() {
        SolicitudAcademica sinEstudiante = SolicitudAcademica.builder().radicado(RADICADO)
                .tipoSolicitudAcademica(solicitud.getTipoSolicitudAcademica()).build();

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> validador.validar(sinEstudiante, actor(DUENIO, RolEtiquetaEtapa.ESTUDIANTE)));

        assertEquals(String.format(MensajesError.SOLICITUD_AJENA, RADICADO, DUENIO), error.getMessage());
    }

    @Test
    void rechazaAlFuncionarioQueNoTieneAsignadoElTipo() {
        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> validador.validar(solicitud, actor(NO_ASIGNADO, RolEtiquetaEtapa.FUNCIONARIO)));

        assertEquals(String.format(MensajesError.TIPO_NO_ASIGNADO_FUNCIONARIO, NOMBRE_TIPO, NO_ASIGNADO), error.getMessage());
    }

    @Test
    void rechazaAlFuncionarioSiElTipoNoTieneResponsable() {
        SolicitudAcademica sinResponsable = SolicitudAcademica.builder().radicado(RADICADO)
                .estudiante(solicitud.getEstudiante())
                .tipoSolicitudAcademica(TipoSolicitudAcademica.builder().nombre(NOMBRE_TIPO).build())
                .build();

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> validador.validar(sinResponsable, actor(ASIGNADO, RolEtiquetaEtapa.FUNCIONARIO)));

        assertEquals(String.format(MensajesError.TIPO_NO_ASIGNADO_FUNCIONARIO, NOMBRE_TIPO, ASIGNADO), error.getMessage());
    }

    static Stream<Arguments> rolesQueNoTiene() {
        return Stream.of(
                Arguments.of(DUENIO, RolEtiquetaEtapa.FUNCIONARIO, "Funcionario Académico"),
                Arguments.of(DUENIO, RolEtiquetaEtapa.DECANO, "Decano"),
                Arguments.of(ASIGNADO, RolEtiquetaEtapa.ESTUDIANTE, "Estudiante"),
                Arguments.of(ASIGNADO, RolEtiquetaEtapa.DECANO, "Decano"),
                Arguments.of(DECANO, RolEtiquetaEtapa.ESTUDIANTE, "Estudiante"),
                Arguments.of(DECANO, RolEtiquetaEtapa.FUNCIONARIO, "Funcionario Académico"),
                Arguments.of(SECRETARIO, RolEtiquetaEtapa.DECANO, "Decano"),
                Arguments.of(SECRETARIO, RolEtiquetaEtapa.FUNCIONARIO, "Funcionario Académico"),
                Arguments.of(SECRETARIO, RolEtiquetaEtapa.ESTUDIANTE, "Estudiante"),
                Arguments.of(COMITE, RolEtiquetaEtapa.FUNCIONARIO, "Funcionario Académico"));
    }

    @ParameterizedTest
    @MethodSource("rolesQueNoTiene")
    void rechazaAlUsuarioQueNoTieneElRolConQueActua(String uuid, RolEtiquetaEtapa rol, String rolRequerido) {
        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> validador.validar(solicitud, actor(uuid, rol)));

        assertEquals(String.format(MensajesError.ACTOR_SIN_ROL, uuid, rolRequerido), error.getMessage());
    }

    @Test
    void elRolSeExigeAunqueElUsuarioSeaElDuenio() {
        registrar(DUENIO, "Funcionario");

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> validador.validar(solicitud, actor(DUENIO, RolEtiquetaEtapa.ESTUDIANTE)));

        assertEquals(String.format(MensajesError.ACTOR_SIN_ROL, DUENIO, "Estudiante"), error.getMessage());
    }

    @Test
    void aceptaAUnUsuarioConVariosRolesSiUnoEsElRequerido() {
        registrar(ASIGNADO, "Secretario General", "Funcionario Académico");

        assertDoesNotThrow(() -> validador.validar(solicitud, actor(ASIGNADO, RolEtiquetaEtapa.FUNCIONARIO)));
    }

    @Test
    void rechazaAlUsuarioSinRoles() {
        when(usuarioGateway.getUsuario("sin-roles")).thenReturn(Usuario.builder().uuidUsuario("sin-roles").build());

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> validador.validar(solicitud, actor("sin-roles", RolEtiquetaEtapa.DECANO)));

        assertEquals(String.format(MensajesError.ACTOR_SIN_ROL, "sin-roles", "Decano"), error.getMessage());
    }

    @Test
    void rechazaAUnUsuarioQueNoExiste() {
        ErrorEntidadNoExisteExcepcion error = assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> validador.validar(solicitud, actor("fantasma", RolEtiquetaEtapa.DECANO)));

        assertEquals(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, "Usuario", "fantasma"), error.getMessage());
    }

    static Stream<ActorSolicitud> actoresIncompletos() {
        return Stream.of(
                null,
                ActorSolicitud.builder().rol(RolEtiquetaEtapa.ESTUDIANTE).build(),
                ActorSolicitud.builder().uuidUsuario("  ").rol(RolEtiquetaEtapa.ESTUDIANTE).build(),
                ActorSolicitud.builder().uuidUsuario(DUENIO).build());
    }

    @ParameterizedTest
    @MethodSource("actoresIncompletos")
    void rechazaUnActorIncompletoSinConsultarAlUsuario(ActorSolicitud incompleto) {
        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> validador.validar(solicitud, incompleto));

        assertEquals(MensajesError.DATOS_TRANSICION_INCOMPLETOS, error.getMessage());
        verify(usuarioGateway, never()).getUsuario(anyString());
    }

    @Test
    void revisaElRolAntesQueElAlcance() {
        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> validador.validar(solicitud, actor(OTRO_ESTUDIANTE, RolEtiquetaEtapa.FUNCIONARIO)));

        assertEquals(String.format(MensajesError.ACTOR_SIN_ROL, OTRO_ESTUDIANTE, "Funcionario Académico"), error.getMessage());
    }
}
