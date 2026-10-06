package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.ConsultaSolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.MaquinaEtapas;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultaCancelacionMatriculaCUImplAdaptadorTest {

    private static final String TOKEN = "token-jwt";
    private static final String ESTUDIANTE = "est-1";
    private static final String SOLICITUD = "sol-1";
    private static final String TIPO_CM = "tipo-cm";

    @Mock
    private SolicitudCancelacionMatriculaGatewayIntPuerto gateway;

    @Mock
    private EstudianteGatewayIntPuerto estudianteGateway;

    @Mock
    private TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway;

    @Mock
    private TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway;

    @Mock
    private SesionGatewayIntPuerto sesionGateway;

    @Mock
    private IJwtServicio jwtServicio;

    @Mock
    private ConsultaSolicitudAcademicaCUIntPuerto consultaCU;

    private ConsultaCancelacionMatriculaCUImplAdaptador casoDeUso;

    private Usuario usuario(String uuid, String rol) {
        return Usuario.builder()
                .uuidUsuario(uuid)
                .roles(new ArrayList<>(List.of(Rol.builder().uuidRol("rol-" + rol).nombre(rol).estado(true).build())))
                .build();
    }

    private AsignaturaMatriculada materia(String uuid, String nombre, String estado) {
        return AsignaturaMatriculada.builder()
                .uuidAsignaturaMatriculada(uuid)
                .asignatura(Asignatura.builder().uuidAsignatura("a-" + uuid).codigoAsignatura("C-" + uuid).nombreAsignatura(nombre).build())
                .grupo("A")
                .estado(estado)
                .build();
    }

    private SituacionAcademicaAsignatura situacion(String uuid) {
        return SituacionAcademicaAsignatura.builder().uuidSituacionAcademica(uuid).codigo(uuid).nombre(uuid).build();
    }

    private void cancelacionEn(String etapa, RolEtiquetaEtapa rol) {
        when(gateway.getPorSolicitud(SOLICITUD)).thenReturn(SolicitudCancelacionMatricula.builder()
                .solicitudAcademica(SolicitudAcademica.builder().uuidSolicitudAcademica(SOLICITUD).build())
                .motivoCancelacion("Motivos personales")
                .asignaturas(new ArrayList<>(List.of(AsignaturaSolicitudAcademica.builder()
                        .uuidAsignaturaSolicitud("as-1")
                        .asignaturaMatriculada(materia("am-1", "Cálculo I", "activa"))
                        .numeroFaltas(2)
                        .situacionMatricula(situacion("sit-r0"))
                        .situacionCancelar(situacion("sit-r1"))
                        .build())))
                .build());
        when(consultaCU.getDetalle(SOLICITUD, TOKEN)).thenReturn(DetalleSolicitudAcademica.builder()
                .solicitudAcademica(SolicitudAcademica.builder()
                        .uuidSolicitudAcademica(SOLICITUD)
                        .etapa(EtapaSolicitudAcademica.builder().uuidEtapa("et-" + etapa).codigo(etapa).build())
                        .build())
                .etiqueta("Etiqueta")
                .build());
        when(consultaCU.resolverActor(SOLICITUD, TOKEN)).thenReturn(ActorSolicitud.builder().uuidUsuario("u-1").rol(rol).build());
    }

    @BeforeEach
    void setUp() {
        ExcepcionesFormateadorImplAdaptador formateador = new ExcepcionesFormateadorImplAdaptador();
        casoDeUso = new ConsultaCancelacionMatriculaCUImplAdaptador(gateway, estudianteGateway, tipoSolicitudGateway, tipoAnexoGateway,
                sesionGateway, jwtServicio, consultaCU, new MaquinaEtapas(formateador), formateador);
        lenient().when(jwtServicio.getUsername(TOKEN)).thenReturn("alumno");
        lenient().when(sesionGateway.getUsuario("alumno")).thenReturn(usuario(ESTUDIANTE, "Estudiante"));
        lenient().when(estudianteGateway.getPorUuid(ESTUDIANTE)).thenReturn(Estudiante.builder().uuidUsuario(ESTUDIANTE).build());
    }

    @Test
    void elEstudianteAutenticadoSaleDelToken() {
        assertEquals(ESTUDIANTE, casoDeUso.getEstudianteAutenticado(TOKEN));
    }

    @Test
    void unUsuarioSinRolEstudianteOSinFilaDeEstudianteSeRechaza() {
        when(sesionGateway.getUsuario("alumno")).thenReturn(usuario(ESTUDIANTE, "Decano"));
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> casoDeUso.getEstudianteAutenticado(TOKEN));

        when(sesionGateway.getUsuario("alumno")).thenReturn(usuario(ESTUDIANTE, "Estudiante"));
        when(estudianteGateway.getPorUuid(ESTUDIANTE)).thenReturn(null);
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> casoDeUso.getFormulario(TOKEN));
        verify(tipoAnexoGateway, never()).getPorTipo(any());
    }

    @Test
    void unUsuarioQueNoExisteSeRechaza() {
        when(sesionGateway.getUsuario("alumno")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.getEstudianteAutenticado(TOKEN));
    }

    @Test
    void elFormularioTraeLosAnexosDeCancelacionDeMatriculaYSoloLasAsignaturasActivas() {
        TipoSolicitudAcademica matricula = TipoSolicitudAcademica.builder().uuidTipoSolicitudAcademica(TIPO_CM).nombre("Cancelación de Matrícula").build();
        TipoSolicitudAcademica asignatura = TipoSolicitudAcademica.builder().uuidTipoSolicitudAcademica("tipo-ca").nombre("Cancelación de Asignatura").build();
        List<TipoAnexoAcademico> anexos = List.of(TipoAnexoAcademico.builder().uuidTipoAnexoAcademico("an-1").nombre("Paz y salvo").obligatorio(true).build());
        when(tipoSolicitudGateway.getTodos()).thenReturn(List.of(asignatura, matricula));
        when(tipoAnexoGateway.getPorTipo(TIPO_CM)).thenReturn(anexos);
        when(estudianteGateway.getAsignaturasMatriculadas(ESTUDIANTE)).thenReturn(List.of(
                materia("am-1", "Cálculo I", "activa"), materia("am-2", "Física I", "cancelada"), materia("am-3", "Química I", "activa")));

        FormularioCancelacionMatricula formulario = casoDeUso.getFormulario(TOKEN);

        assertSame(anexos, formulario.getAnexos());
        assertEquals(List.of("am-1", "am-3"), formulario.getAsignaturas().stream().map(AsignaturaMatriculada::getUuidAsignaturaMatriculada).toList());
    }

    @Test
    void sinTipoDeCancelacionDeMatriculaElFormularioFalla() {
        when(tipoSolicitudGateway.getTodos()).thenReturn(List.of());

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.getFormulario(TOKEN));
    }

    @Test
    void elDetalleSumaMotivoYAsignaturasAlDeSolicitudesAcademicas() {
        cancelacionEn("RADICADA", RolEtiquetaEtapa.FUNCIONARIO);

        DetalleCancelacionMatricula detalle = casoDeUso.getDetalle(SOLICITUD, TOKEN);

        assertEquals("Etiqueta", detalle.getDetalle().getEtiqueta());
        assertEquals("Motivos personales", detalle.getMotivoCancelacion());
        assertEquals(1, detalle.getAsignaturas().size());
        assertEquals("sit-r1", detalle.getAsignaturas().get(0).getSituacionCancelar().getUuidSituacionAcademica());
    }

    @Test
    void elDecanoVeLaSituacionAlCancelar() {
        cancelacionEn("APROBADA_POR_DECANO", RolEtiquetaEtapa.DECANO);

        assertNotNull(casoDeUso.getDetalle(SOLICITUD, TOKEN).getAsignaturas().get(0).getSituacionCancelar());
    }

    @ParameterizedTest
    @ValueSource(strings = {"RADICADA", "EN_REVISION_DECANO", "APROBADA_POR_DECANO", "RECHAZADA_POR_DECANO"})
    void elEstudianteNoVeLaSituacionAlCancelarAntesDeLaEtapaFinal(String etapa) {
        cancelacionEn(etapa, RolEtiquetaEtapa.ESTUDIANTE);

        AsignaturaSolicitudAcademica asignatura = casoDeUso.getDetalle(SOLICITUD, TOKEN).getAsignaturas().get(0);

        assertNull(asignatura.getSituacionCancelar());
        assertEquals("sit-r0", asignatura.getSituacionMatricula().getUuidSituacionAcademica());
        assertEquals(2, asignatura.getNumeroFaltas());
    }

    @ParameterizedTest
    @ValueSource(strings = {"APROBADA", "RECHAZADA"})
    void elEstudianteVeLaSituacionAlCancelarEnLaEtapaFinal(String etapa) {
        cancelacionEn(etapa, RolEtiquetaEtapa.ESTUDIANTE);

        assertEquals("sit-r1", casoDeUso.getDetalle(SOLICITUD, TOKEN).getAsignaturas().get(0).getSituacionCancelar().getUuidSituacionAcademica());
    }

    @Test
    void unaSolicitudQueNoEsCancelacionDeMatriculaRespondeComoInexistente() {
        when(gateway.getPorSolicitud("otra")).thenReturn(null);

        ErrorEntidadNoExisteExcepcion error = assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.getDetalle("otra", TOKEN));

        assertEquals(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, "Solicitud académica", "otra"), error.getMessage());
        verify(consultaCU, never()).getDetalle(any(), any());
    }

    @Test
    void unaSolicitudAjenaPropagaLaRespuestaDeSolicitudesAcademicas() {
        when(gateway.getPorSolicitud(SOLICITUD)).thenReturn(SolicitudCancelacionMatricula.builder().build());
        when(consultaCU.getDetalle(SOLICITUD, TOKEN)).thenThrow(new ErrorEntidadNoExisteExcepcion("no existe"));

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.getDetalle(SOLICITUD, TOKEN));
    }
}
