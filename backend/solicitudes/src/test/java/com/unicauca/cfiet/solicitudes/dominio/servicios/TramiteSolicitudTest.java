package com.unicauca.cfiet.solicitudes.dominio.servicios;

import com.unicauca.cfiet.solicitudes.aplicacion.output.IJwtServicio;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SesionGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.UsuarioGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorEntidadNoExisteExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorGenericoExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorReglaNegocioVioladaExcepcion;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TramiteSolicitudTest {

    private static final String SOLICITUD = "sol-1";
    private static final String RADICADO = "2026-CM-0001";
    private static final String ESTUDIANTE = "est-1";
    private static final String OTRO_ESTUDIANTE = "est-2";
    private static final String FUNCIONARIO = "fun-1";
    private static final String OTRO_FUNCIONARIO = "fun-2";
    private static final String DECANO = "dec-1";
    private static final String NOMBRE_TIPO = "Cancelación de Matrícula";

    @Mock
    private SolicitudAcademicaGatewayIntPuerto solicitudGateway;

    @Mock
    private UsuarioGatewayIntPuerto usuarioGateway;

    @Mock
    private SesionGatewayIntPuerto sesionGateway;

    @Mock
    private IJwtServicio jwtServicio;

    private TramiteSolicitud tramite;

    @BeforeEach
    void setUp() {
        ExcepcionesFormateadorImplAdaptador formateador = new ExcepcionesFormateadorImplAdaptador();
        tramite = new TramiteSolicitud(solicitudGateway, usuarioGateway, sesionGateway, jwtServicio, new MaquinaEtapas(formateador), formateador);
        registrar(usuario(ESTUDIANTE, "Estudiante"));
        registrar(usuario(OTRO_ESTUDIANTE, "Estudiante"));
        registrar(usuario(FUNCIONARIO, "Funcionario Académico"));
        registrar(usuario(OTRO_FUNCIONARIO, "Funcionario Académico"));
        registrar(usuario(DECANO, "Decano"));
    }

    @AfterEach
    void sinEscrituras() {
        verify(solicitudGateway, never()).crear(any(), any());
        verify(solicitudGateway, never()).actualizarEtapa(any(), any());
        verify(usuarioGateway, never()).guardarUsuario(any());
        verify(usuarioGateway, never()).guardarUsuarios(any());
    }

    private Usuario usuario(String uuid, String rol) {
        return Usuario.builder()
                .uuidUsuario(uuid)
                .roles(new ArrayList<>(List.of(Rol.builder().uuidRol("rol-" + rol).nombre(rol).estado(true).build())))
                .build();
    }

    private void registrar(Usuario usuario) {
        lenient().when(usuarioGateway.getUsuario(usuario.getUuidUsuario())).thenReturn(usuario);
    }

    private void sesion(String token, Usuario usuario) {
        when(jwtServicio.getUsername(token)).thenReturn("user-" + usuario.getUuidUsuario());
        when(sesionGateway.getUsuario("user-" + usuario.getUuidUsuario())).thenReturn(usuario);
    }

    private SolicitudAcademica solicitud(String etapa) {
        SolicitudAcademica solicitud = SolicitudAcademica.builder()
                .uuidSolicitudAcademica(SOLICITUD)
                .radicado(RADICADO)
                .estudiante(Estudiante.builder().uuidUsuario(ESTUDIANTE).build())
                .tipoSolicitudAcademica(TipoSolicitudAcademica.builder()
                        .uuidTipoSolicitudAcademica("tipo-1")
                        .nombre(NOMBRE_TIPO)
                        .funcionarioAcademico(FuncionarioAcademico.builder().uuidUsuario(FUNCIONARIO).build())
                        .build())
                .etapa(EtapaSolicitudAcademica.builder().uuidEtapa("et-" + etapa).codigo(etapa).build())
                .build();
        when(solicitudGateway.getPorUuid(SOLICITUD)).thenReturn(solicitud);
        return solicitud;
    }

    private ActorSolicitud actor(String uuid, RolEtiquetaEtapa rol) {
        return ActorSolicitud.builder().uuidUsuario(uuid).rol(rol).build();
    }

    @Test
    void actorDeDevuelveElUsuarioDelTokenConElRolDeLaAccion() {
        sesion("token-fun", usuario(FUNCIONARIO, "Funcionario Académico"));

        ActorSolicitud actor = tramite.actorDe("token-fun", RolEtiquetaEtapa.FUNCIONARIO);

        assertEquals(FUNCIONARIO, actor.getUuidUsuario());
        assertEquals(RolEtiquetaEtapa.FUNCIONARIO, actor.getRol());
    }

    @Test
    void actorDeReconoceElRolEntreVariosRolesDelUsuario() {
        Usuario conVariosRoles = usuario(DECANO, "Secretario General");
        conVariosRoles.getRoles().add(Rol.builder().uuidRol("rol-dec").nombre("Decano").estado(true).build());
        sesion("token-dec", conVariosRoles);

        assertEquals(RolEtiquetaEtapa.DECANO, tramite.actorDe("token-dec", RolEtiquetaEtapa.DECANO).getRol());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void actorDeRechazaUnTokenDelQueNoSaleUsername(String username) {
        when(jwtServicio.getUsername("token")).thenReturn(username);

        ErrorGenericoExcepcion error = assertThrows(ErrorGenericoExcepcion.class,
                () -> tramite.actorDe("token", RolEtiquetaEtapa.ESTUDIANTE));

        assertEquals(MensajesError.USERNAME_TOKEN, error.getMessage());
        verifyNoInteractions(sesionGateway);
    }

    @Test
    void actorDeNoSigueSiElTokenEsInvalido() {
        when(jwtServicio.getUsername("token-roto")).thenThrow(new MalformedJwtException("token mal formado"));

        assertThrows(MalformedJwtException.class, () -> tramite.actorDe("token-roto", RolEtiquetaEtapa.FUNCIONARIO));

        verifyNoInteractions(sesionGateway);
    }

    @Test
    void actorDeRechazaUnTokenSinUsuarioEnElSistema() {
        when(jwtServicio.getUsername("token")).thenReturn("fantasma");

        ErrorEntidadNoExisteExcepcion error = assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> tramite.actorDe("token", RolEtiquetaEtapa.DECANO));

        assertEquals(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA_FILTRO, "Usuario", "username", "fantasma"), error.getMessage());
    }

    @Test
    void actorDeRechazaAlUsuarioQueNoTieneElRolDeLaAccion() {
        sesion("token-est", usuario(ESTUDIANTE, "Estudiante"));

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> tramite.actorDe("token-est", RolEtiquetaEtapa.FUNCIONARIO));

        assertEquals(String.format(MensajesError.ACTOR_SIN_ROL, ESTUDIANTE, "Funcionario Académico"), error.getMessage());
    }

    @Test
    void actorDeNoConfundeElRolFuncionarioDeJulianConFuncionarioAcademico() {
        sesion("token-comite", usuario("comite-1", "Funcionario"));

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> tramite.actorDe("token-comite", RolEtiquetaEtapa.FUNCIONARIO));

        assertEquals(String.format(MensajesError.ACTOR_SIN_ROL, "comite-1", "Funcionario Académico"), error.getMessage());
    }

    @Test
    void actorDeRechazaAlUsuarioSinRoles() {
        Usuario sinRoles = Usuario.builder().uuidUsuario("sin-roles").build();
        sesion("token", sinRoles);

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> tramite.actorDe("token", RolEtiquetaEtapa.ESTUDIANTE));

        assertEquals(String.format(MensajesError.ACTOR_SIN_ROL, "sin-roles", "Estudiante"), error.getMessage());
    }

    @Test
    void solicitudValidadaDevuelveLaSolicitudCuandoActorEtapaYAccionCoinciden() {
        SolicitudAcademica esperada = solicitud(RADICADA);

        SolicitudAcademica obtenida = tramite.solicitudValidada(TipoProcesoAcademico.CANCELACION_MATRICULA, SOLICITUD,
                AccionEtapa.REMITIR_DECANO, actor(FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO));

        assertSame(esperada, obtenida);
        assertEquals(RADICADA, obtenida.getEtapa().getCodigo());
    }

    @Test
    void solicitudValidadaAceptaAlDecanoEnSuEtapa() {
        solicitud(EN_REVISION_DECANO);

        assertDoesNotThrow(() -> tramite.solicitudValidada(TipoProcesoAcademico.CANCELACION_MATRICULA, SOLICITUD,
                AccionEtapa.APROBAR_DECANO, actor(DECANO, RolEtiquetaEtapa.DECANO)));
    }

    @Test
    void solicitudValidadaAceptaAlEstudianteDuenioEnSuEtapa() {
        solicitud(PENDIENTE_PAGO);

        assertDoesNotThrow(() -> tramite.solicitudValidada(TipoProcesoAcademico.EXAMEN_SUPLETORIO, SOLICITUD,
                AccionEtapa.SUBIR_COMPROBANTE, actor(ESTUDIANTE, RolEtiquetaEtapa.ESTUDIANTE)));
    }

    @Test
    void solicitudValidadaRechazaUnaSolicitudInexistente() {
        when(solicitudGateway.getPorUuid("no-existe")).thenReturn(null);

        ErrorEntidadNoExisteExcepcion error = assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> tramite.solicitudValidada(TipoProcesoAcademico.CANCELACION_MATRICULA, "no-existe",
                        AccionEtapa.REMITIR_DECANO, actor(FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO)));

        assertEquals(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, "Solicitud académica", "no-existe"), error.getMessage());
        verify(usuarioGateway, never()).getUsuario(anyString());
    }

    @Test
    void solicitudValidadaRechazaUnaAccionQueNoSaleDeLaEtapaActual() {
        solicitud(EN_REVISION_DECANO);

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> tramite.solicitudValidada(TipoProcesoAcademico.CANCELACION_MATRICULA, SOLICITUD,
                        AccionEtapa.REMITIR_DECANO, actor(FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO)));

        assertEquals(String.format(MensajesError.TRANSICION_NO_PERMITIDA, AccionEtapa.REMITIR_DECANO, EN_REVISION_DECANO,
                "Cancelación de Matrícula"), error.getMessage());
    }

    @Test
    void solicitudValidadaRechazaUnaAccionDeOtroProceso() {
        solicitud(APROBADA_POR_DECANO);

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> tramite.solicitudValidada(TipoProcesoAcademico.CANCELACION_MATRICULA, SOLICITUD,
                        AccionEtapa.ENVIAR_RECIBO, actor(FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO)));

        assertEquals(String.format(MensajesError.TRANSICION_NO_PERMITIDA, AccionEtapa.ENVIAR_RECIBO, APROBADA_POR_DECANO,
                "Cancelación de Matrícula"), error.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {APROBADA, RECHAZADA})
    void solicitudValidadaRechazaCualquierAccionEnEtapaFinal(String etapa) {
        solicitud(etapa);

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> tramite.solicitudValidada(TipoProcesoAcademico.CANCELACION_MATRICULA, SOLICITUD,
                        AccionEtapa.ENVIAR_RESPUESTA, actor(FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO)));

        assertEquals(String.format(MensajesError.ETAPA_FINAL_SIN_ACCIONES, etapa), error.getMessage());
    }

    @Test
    void solicitudValidadaRechazaAlDecanoEnUnaAccionDelFuncionario() {
        solicitud(RADICADA);

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> tramite.solicitudValidada(TipoProcesoAcademico.CANCELACION_MATRICULA, SOLICITUD,
                        AccionEtapa.REMITIR_DECANO, actor(DECANO, RolEtiquetaEtapa.DECANO)));

        assertEquals(String.format(MensajesError.ROL_TRANSICION_NO_PERMITIDO, RolEtiquetaEtapa.DECANO, AccionEtapa.REMITIR_DECANO,
                RADICADA, RolEtiquetaEtapa.FUNCIONARIO), error.getMessage());
    }

    @Test
    void solicitudValidadaRechazaAlEstudianteDuenioEnUnaAccionDelFuncionario() {
        solicitud(RADICADA);

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> tramite.solicitudValidada(TipoProcesoAcademico.CANCELACION_MATRICULA, SOLICITUD,
                        AccionEtapa.RECHAZAR_FUNCIONARIO, actor(ESTUDIANTE, RolEtiquetaEtapa.ESTUDIANTE)));

        assertEquals(String.format(MensajesError.ROL_TRANSICION_NO_PERMITIDO, RolEtiquetaEtapa.ESTUDIANTE,
                AccionEtapa.RECHAZAR_FUNCIONARIO, RADICADA, RolEtiquetaEtapa.FUNCIONARIO), error.getMessage());
    }

    @Test
    void solicitudValidadaRechazaAlFuncionarioQueNoTieneAsignadoElTipo() {
        solicitud(RADICADA);

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> tramite.solicitudValidada(TipoProcesoAcademico.CANCELACION_MATRICULA, SOLICITUD,
                        AccionEtapa.REMITIR_DECANO, actor(OTRO_FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO)));

        assertEquals(String.format(MensajesError.TIPO_NO_ASIGNADO_FUNCIONARIO, NOMBRE_TIPO, OTRO_FUNCIONARIO), error.getMessage());
    }

    @Test
    void solicitudValidadaRechazaAlEstudianteQueNoEsDuenio() {
        solicitud(PENDIENTE_PAGO);

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> tramite.solicitudValidada(TipoProcesoAcademico.EXAMEN_SUPLETORIO, SOLICITUD,
                        AccionEtapa.SUBIR_COMPROBANTE, actor(OTRO_ESTUDIANTE, RolEtiquetaEtapa.ESTUDIANTE)));

        assertEquals(String.format(MensajesError.SOLICITUD_AJENA, RADICADO, OTRO_ESTUDIANTE), error.getMessage());
    }

    @Test
    void solicitudValidadaRevisaElAlcanceDelActorAntesQueLaEtapa() {
        solicitud(APROBADA);

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> tramite.solicitudValidada(TipoProcesoAcademico.CANCELACION_MATRICULA, SOLICITUD,
                        AccionEtapa.REMITIR_DECANO, actor(OTRO_FUNCIONARIO, RolEtiquetaEtapa.FUNCIONARIO)));

        assertEquals(String.format(MensajesError.TIPO_NO_ASIGNADO_FUNCIONARIO, NOMBRE_TIPO, OTRO_FUNCIONARIO), error.getMessage());
    }

    @Test
    void solicitudValidadaRechazaUnActorIncompleto() {
        solicitud(RADICADA);

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> tramite.solicitudValidada(TipoProcesoAcademico.CANCELACION_MATRICULA, SOLICITUD,
                        AccionEtapa.REMITIR_DECANO, actor(FUNCIONARIO, null)));

        assertEquals(MensajesError.DATOS_TRANSICION_INCOMPLETOS, error.getMessage());
        verify(usuarioGateway, never()).getUsuario(anyString());
    }

    @Test
    void tieneTextoDistingueValoresVaciosDeValoresConContenido() {
        assertFalse(tramite.tieneTexto(null));
        assertFalse(tramite.tieneTexto(""));
        assertFalse(tramite.tieneTexto("  \t"));
        assertTrue(tramite.tieneTexto(" a "));
    }
}
