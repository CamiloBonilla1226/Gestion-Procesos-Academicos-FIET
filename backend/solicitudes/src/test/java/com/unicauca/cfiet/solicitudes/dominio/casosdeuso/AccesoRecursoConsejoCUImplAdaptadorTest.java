package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.output.IJwtServicio;
import com.unicauca.cfiet.solicitudes.aplicacion.output.RespuestaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SesionGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SolicitudGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.UsuarioGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Funcionario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.InformacionSolicitante;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Respuesta;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Rol;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Solicitud;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorEntidadNoExisteExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorReglaNegocioVioladaExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccesoRecursoConsejoCUImplAdaptadorTest {

    private static final String CEDULA = "Cédula de ciudadanía";
    private static final String UUID_SOLICITUD = "sol-1";
    private static final String UUID_RESPUESTA = "resp-1";

    @Mock
    private UsuarioGatewayIntPuerto usuarioGateway;
    @Mock
    private SesionGatewayIntPuerto sesionGateway;
    @Mock
    private SolicitudGatewayIntPuerto solicitudGateway;
    @Mock
    private RespuestaGatewayIntPuerto respuestaGateway;
    @Mock
    private IJwtServicio jwtServicio;

    private AccesoRecursoConsejoCUImplAdaptador casoDeUso;

    @BeforeEach
    void setUp() {
        casoDeUso = new AccesoRecursoConsejoCUImplAdaptador(usuarioGateway, sesionGateway, solicitudGateway, respuestaGateway,
                jwtServicio, new ExcepcionesFormateadorImplAdaptador());
    }

    private Usuario sesion(String uuid, String documento, String... roles) {
        List<Rol> lista = new ArrayList<>();
        for (String rol : roles)
            lista.add(Rol.builder().uuidRol("rol-" + rol).nombre(rol).estado(true).build());
        Usuario usuario = Usuario.builder().uuidUsuario(uuid).username("user-" + uuid)
                .tipoDocumento(CEDULA).numeroDocumento(documento).roles(lista).build();
        lenient().when(jwtServicio.getUsername("token-" + uuid)).thenReturn("user-" + uuid);
        lenient().when(sesionGateway.getUsuario("user-" + uuid)).thenReturn(usuario);
        return usuario;
    }

    private Solicitud solicitud(String uuidFuncionario, String documentoSolicitante) {
        Solicitud solicitud = Solicitud.builder()
                .uuidSolicitud(UUID_SOLICITUD)
                .objFuncionario(Funcionario.builder().uuidUsuario(uuidFuncionario).build())
                .informacionSolicitante(InformacionSolicitante.builder().tipoDocumento(CEDULA).numeroDocumento(documentoSolicitante).build())
                .build();
        lenient().when(solicitudGateway.getSolicitud(UUID_SOLICITUD)).thenReturn(solicitud);
        return solicitud;
    }

    @Test
    void elUsuarioVeSusPropiosDatos() {
        Usuario propio = sesion("est-1", "100", "Estudiante");
        when(usuarioGateway.getUsuario("est-1")).thenReturn(propio);

        assertSame(propio, casoDeUso.getUsuarioVisible("est-1", "token-est-1"));
    }

    @Test
    void elSecretarioGeneralVeCualquierUsuario() {
        sesion("sec-1", "1", "Secretario General");
        Usuario otro = Usuario.builder().uuidUsuario("doc-1").build();
        when(usuarioGateway.getUsuario("doc-1")).thenReturn(otro);

        assertSame(otro, casoDeUso.getUsuarioVisible("doc-1", "token-sec-1"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Estudiante", "Decano", "Funcionario", "Funcionario Académico", "Docente"})
    void otroRolNoVeDatosAjenosYRecibeNoExiste(String rol) {
        sesion("act-1", "200", rol);

        ErrorEntidadNoExisteExcepcion error = assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.getUsuarioVisible("otro-1", "token-act-1"));

        assertEquals(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, "Usuario", "otro-1"), error.getMessage());
        verify(usuarioGateway, never()).getUsuario(anyString());
    }

    @Test
    void usuarioInexistenteParaElSecretarioRecibeNoExiste() {
        sesion("sec-1", "1", "Secretario General");
        when(usuarioGateway.getUsuario("nadie")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.getUsuarioVisible("nadie", "token-sec-1"));
    }

    @Test
    void cambioDeContrasenaPropioOPorSecretarioSePermite() {
        sesion("est-1", "100", "Estudiante");
        sesion("sec-1", "1", "Secretario General");

        assertDoesNotThrow(() -> casoDeUso.verificarCambioPropio("est-1", "token-est-1"));
        assertDoesNotThrow(() -> casoDeUso.verificarCambioPropio("est-1", "token-sec-1"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Estudiante", "Decano", "Funcionario Académico", "Funcionario"})
    void cambioDeContrasenaAjenoEsReglaVioladaParaQuienNoEsSecretario(String rol) {
        sesion("act-1", "200", rol);

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.verificarCambioPropio("otro-1", "token-act-1"));

        assertEquals(String.format(MensajesError.USUARIO_AJENO, "act-1", "otro-1"), error.getMessage());
    }

    @Test
    void tokenSinUsernameEsErrorGenerico() {
        when(jwtServicio.getUsername("token-x")).thenReturn(null);

        RuntimeException error = assertThrows(RuntimeException.class, () -> casoDeUso.verificarCambioPropio("x", "token-x"));

        assertEquals(MensajesError.USERNAME_TOKEN, error.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Secretario General", "Decano", "Secretaria Decanatura FIET"})
    void rolesInternosDescarganAnexos(String rol) {
        sesion("int-1", "1", rol);
        solicitud("fun-1", "300");

        assertDoesNotThrow(() -> casoDeUso.verificarAccesoAnexo(UUID_SOLICITUD, "token-int-1"));
    }

    @Test
    void funcionarioAsignadoYSolicitanteDescarganAnexos() {
        sesion("fun-1", "1", "Funcionario");
        sesion("doc-1", "300", "Docente");
        solicitud("fun-1", "300");

        assertDoesNotThrow(() -> casoDeUso.verificarAccesoAnexo(UUID_SOLICITUD, "token-fun-1"));
        assertDoesNotThrow(() -> casoDeUso.verificarAccesoAnexo(UUID_SOLICITUD, "token-doc-1"));
    }

    @Test
    void funcionarioNoAsignadoEstudianteYAjenoRecibenNoExiste() {
        sesion("fun-2", "2", "Funcionario");
        sesion("est-1", "100", "Estudiante");
        sesion("fa-1", "4", "Funcionario Académico");
        solicitud("fun-1", "300");

        for (String token : List.of("token-fun-2", "token-est-1", "token-fa-1")) {
            ErrorEntidadNoExisteExcepcion error = assertThrows(ErrorEntidadNoExisteExcepcion.class,
                    () -> casoDeUso.verificarAccesoAnexo(UUID_SOLICITUD, token));
            assertEquals(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, "Solicitud", UUID_SOLICITUD), error.getMessage());
        }
    }

    @Test
    void elSolicitanteDebeCoincidirEnTipoYNumeroDeDocumento() {
        Usuario usuario = sesion("doc-1", "300", "Docente");
        usuario.setTipoDocumento("Cédula de extranjería");
        solicitud("fun-1", "300");

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.verificarAccesoAnexo(UUID_SOLICITUD, "token-doc-1"));
    }

    @Test
    void solicitudInexistenteRecibeNoExiste() {
        sesion("sec-1", "1", "Secretario General");
        when(solicitudGateway.getSolicitud("nada")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.verificarAccesoAnexo("nada", "token-sec-1"));
    }

    @Test
    void respuestaSeValidaContraSuSolicitud() {
        sesion("fun-1", "1", "Funcionario");
        sesion("est-1", "100", "Estudiante");
        solicitud("fun-1", "300");
        when(respuestaGateway.getRespuesta(UUID_RESPUESTA)).thenReturn(Respuesta.builder()
                .uuidRespuesta(UUID_RESPUESTA)
                .solicitud(Solicitud.builder().uuidSolicitud(UUID_SOLICITUD).build())
                .build());

        assertDoesNotThrow(() -> casoDeUso.verificarAccesoRespuesta(UUID_RESPUESTA, "token-fun-1"));
        ErrorEntidadNoExisteExcepcion error = assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.verificarAccesoRespuesta(UUID_RESPUESTA, "token-est-1"));
        assertEquals(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, "Respuesta", UUID_RESPUESTA), error.getMessage());
    }

    @Test
    void respuestaInexistenteRecibeNoExiste() {
        sesion("sec-1", "1", "Secretario General");
        when(respuestaGateway.getRespuesta("nada")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.verificarAccesoRespuesta("nada", "token-sec-1"));
    }

    @Test
    void funcionarioAsignadoActualizaSuSolicitudSinReasignar() {
        sesion("fun-1", "1", "Funcionario");
        solicitud("fun-1", "300");

        assertDoesNotThrow(() -> casoDeUso.verificarActualizacionSolicitud(UUID_SOLICITUD, null, "token-fun-1"));
        assertDoesNotThrow(() -> casoDeUso.verificarActualizacionSolicitud(UUID_SOLICITUD, "", "token-fun-1"));
        assertDoesNotThrow(() -> casoDeUso.verificarActualizacionSolicitud(UUID_SOLICITUD, "fun-1", "token-fun-1"));
    }

    @Test
    void funcionarioResponsableDeLaRespuestaActualizaComoEnSuBandejaDeRespuestas() {
        sesion("fun-1", "1", "Funcionario");
        Solicitud propia = solicitud("fun-1", "300");
        Respuesta respuesta = Respuesta.builder().uuidRespuesta(UUID_RESPUESTA).solicitud(propia).build();

        assertDoesNotThrow(() -> casoDeUso.verificarActualizacionSolicitud(respuesta.getSolicitud().getUuidSolicitud(),
                propia.getObjFuncionario().getUuidUsuario(), "token-fun-1"));
    }

    @Test
    void funcionarioAjenoRecibeNoExiste() {
        sesion("fun-2", "2", "Funcionario");
        solicitud("fun-1", "300");

        ErrorEntidadNoExisteExcepcion error = assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.verificarActualizacionSolicitud(UUID_SOLICITUD, null, "token-fun-2"));

        assertEquals(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, "Solicitud", UUID_SOLICITUD), error.getMessage());
    }

    @Test
    void funcionarioNoPuedeReasignar() {
        sesion("fun-1", "1", "Funcionario");
        solicitud("fun-1", "300");

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.verificarActualizacionSolicitud(UUID_SOLICITUD, "fun-2", "token-fun-1"));

        assertEquals(String.format(MensajesError.REASIGNACION_NO_PERMITIDA, "fun-1", UUID_SOLICITUD), error.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Secretario General", "Decano"})
    void secretarioGeneralYDecanoPuedenReasignarCualquierSolicitud(String rol) {
        sesion("adm-1", "1", rol);
        solicitud("fun-1", "300");

        assertDoesNotThrow(() -> casoDeUso.verificarActualizacionSolicitud(UUID_SOLICITUD, "fun-2", "token-adm-1"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Secretaria Decanatura FIET", "Estudiante", "Funcionario Académico", "Docente"})
    void otrosRolesNoActualizanSolicitudes(String rol) {
        sesion("otro-1", "300", rol);
        solicitud("fun-1", "300");

        assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.verificarActualizacionSolicitud(UUID_SOLICITUD, null, "token-otro-1"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Secretario General", "Funcionario"})
    void actualizarUnaSolicitudInexistenteEsEntidadNoExiste(String rol) {
        sesion("act-1", "1", rol);
        when(solicitudGateway.getSolicitud("nada")).thenReturn(null);

        ErrorEntidadNoExisteExcepcion error = assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.verificarActualizacionSolicitud("nada", "fun-2", "token-act-1"));

        assertEquals(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, "Solicitud", "nada"), error.getMessage());
    }

    @Test
    void funcionarioSinSolicitudAsignadaRecibeNoExiste() {
        sesion("fun-1", "1", "Funcionario");
        when(solicitudGateway.getSolicitud(UUID_SOLICITUD)).thenReturn(Solicitud.builder().uuidSolicitud(UUID_SOLICITUD).build());

        assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.verificarActualizacionSolicitud(UUID_SOLICITUD, null, "token-fun-1"));
    }
}
