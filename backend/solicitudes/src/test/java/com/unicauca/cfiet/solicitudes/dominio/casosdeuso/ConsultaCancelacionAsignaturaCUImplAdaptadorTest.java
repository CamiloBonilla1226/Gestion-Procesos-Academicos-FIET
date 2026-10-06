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
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultaCancelacionAsignaturaCUImplAdaptadorTest {

    private static final String TOKEN = "token-jwt";
    private static final String ESTUDIANTE = "est-1";
    private static final String SOLICITUD = "sol-1";

    @Mock
    private SolicitudCancelacionAsignaturaGatewayIntPuerto gateway;

    @Mock
    private EstudianteGatewayIntPuerto estudianteGateway;

    @Mock
    private SesionGatewayIntPuerto sesionGateway;

    @Mock
    private IJwtServicio jwtServicio;

    @Mock
    private ConsultaSolicitudAcademicaCUIntPuerto consultaCU;

    private ConsultaCancelacionAsignaturaCUImplAdaptador casoDeUso;

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

    private AsignaturaSolicitudAcademica evaluada(String uuid, boolean aprobada) {
        return AsignaturaSolicitudAcademica.builder()
                .uuidAsignaturaSolicitud(uuid)
                .asignaturaMatriculada(materia("am-" + uuid, "Materia " + uuid, "activa"))
                .numeroFaltas(2)
                .nota(new BigDecimal("3.5"))
                .situacionMatricula(situacion("sit-r0"))
                .cumpleCondiciones(aprobada)
                .observacionEvaluacion(aprobada ? null : "Supera las faltas")
                .aprobadaPorDecano(aprobada)
                .observacionDecision(aprobada ? null : "No procede")
                .situacionCancelar(aprobada ? situacion("sit-r1") : null)
                .build();
    }

    private void cancelacionEn(String etapa, RolEtiquetaEtapa rol) {
        when(gateway.getPorSolicitud(SOLICITUD)).thenReturn(SolicitudCancelacionAsignatura.builder()
                .solicitudAcademica(SolicitudAcademica.builder().uuidSolicitudAcademica(SOLICITUD).build())
                .motivoCancelacion("Cruce laboral")
                .asignaturas(new ArrayList<>(List.of(evaluada("as-1", true), evaluada("as-2", false))))
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
        casoDeUso = new ConsultaCancelacionAsignaturaCUImplAdaptador(gateway, estudianteGateway, sesionGateway, jwtServicio,
                consultaCU, new MaquinaEtapas(formateador), formateador);
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
        verify(estudianteGateway, never()).getAsignaturasMatriculadas(any());
    }

    @Test
    void unUsuarioQueNoExisteSeRechaza() {
        when(sesionGateway.getUsuario("alumno")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.getEstudianteAutenticado(TOKEN));
    }

    @Test
    void elFormularioTraeSoloLasAsignaturasActivasYElSoporteLibreConFormatoYTamano() {
        when(estudianteGateway.getAsignaturasMatriculadas(ESTUDIANTE)).thenReturn(List.of(
                materia("am-1", "Cálculo I", "activa"), materia("am-2", "Física I", "cancelada"), materia("am-3", "Química I", "activa")));

        FormularioCancelacionAsignatura formulario = casoDeUso.getFormulario(TOKEN);

        assertEquals(List.of("am-1", "am-3"), formulario.getAsignaturas().stream().map(AsignaturaMatriculada::getUuidAsignaturaMatriculada).toList());
        assertEquals(1, formulario.getSoportes().size());
        SoportePermitido soporte = formulario.getSoportes().get(0);
        assertNull(soporte.getUuidTipoAnexoAcademico());
        assertEquals("Soporte libre", soporte.getNombre());
        assertEquals("pdf,jpg,jpeg,png", soporte.getFormatosPermitidos());
        assertEquals(5L * 1024 * 1024, soporte.getTamanioMaximoBytes());
        assertFalse(soporte.getObligatorio());
    }

    @ParameterizedTest
    @EnumSource(value = RolEtiquetaEtapa.class, names = {"FUNCIONARIO", "DECANO"})
    void elFuncionarioYElDecanoVenTodaLaEvaluacionYLaDecision(RolEtiquetaEtapa rol) {
        cancelacionEn("APROBADA_POR_DECANO", rol);

        DetalleCancelacionAsignatura detalle = casoDeUso.getDetalle(SOLICITUD, TOKEN);

        assertEquals("Cruce laboral", detalle.getMotivoCancelacion());
        assertEquals("Etiqueta", detalle.getDetalle().getEtiqueta());
        AsignaturaSolicitudAcademica aprobada = detalle.getAsignaturas().get(0);
        AsignaturaSolicitudAcademica rechazada = detalle.getAsignaturas().get(1);
        assertEquals(2, aprobada.getNumeroFaltas());
        assertEquals(new BigDecimal("3.5"), aprobada.getNota());
        assertEquals("sit-r0", aprobada.getSituacionMatricula().getUuidSituacionAcademica());
        assertTrue(aprobada.getCumpleCondiciones());
        assertTrue(aprobada.getAprobadaPorDecano());
        assertEquals("sit-r1", aprobada.getSituacionCancelar().getUuidSituacionAcademica());
        assertEquals("Supera las faltas", rechazada.getObservacionEvaluacion());
        assertFalse(rechazada.getAprobadaPorDecano());
        assertEquals("No procede", rechazada.getObservacionDecision());
    }

    @ParameterizedTest
    @ValueSource(strings = {"RADICADA", "EN_REVISION_DECANO", "APROBADA_POR_DECANO", "RECHAZADA_POR_DECANO"})
    void antesDeLaEtapaFinalElEstudianteSoloVeLasAsignaturas(String etapa) {
        cancelacionEn(etapa, RolEtiquetaEtapa.ESTUDIANTE);

        List<AsignaturaSolicitudAcademica> asignaturas = casoDeUso.getDetalle(SOLICITUD, TOKEN).getAsignaturas();

        assertEquals(List.of("as-1", "as-2"), asignaturas.stream().map(AsignaturaSolicitudAcademica::getUuidAsignaturaSolicitud).toList());
        for (AsignaturaSolicitudAcademica asignatura : asignaturas) {
            assertNotNull(asignatura.getAsignaturaMatriculada().getAsignatura().getNombreAsignatura());
            assertNull(asignatura.getNumeroFaltas());
            assertNull(asignatura.getNota());
            assertNull(asignatura.getSituacionMatricula());
            assertNull(asignatura.getCumpleCondiciones());
            assertNull(asignatura.getObservacionEvaluacion());
            assertNull(asignatura.getAprobadaPorDecano());
            assertNull(asignatura.getObservacionDecision());
            assertNull(asignatura.getSituacionCancelar());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"APROBADA", "RECHAZADA"})
    void enLaEtapaFinalElEstudianteVeElResultadoPorAsignaturaPeroNuncaLaObservacionDeEvaluacion(String etapa) {
        cancelacionEn(etapa, RolEtiquetaEtapa.ESTUDIANTE);

        List<AsignaturaSolicitudAcademica> asignaturas = casoDeUso.getDetalle(SOLICITUD, TOKEN).getAsignaturas();

        assertTrue(asignaturas.get(0).getAprobadaPorDecano());
        assertNull(asignaturas.get(0).getObservacionDecision());
        assertFalse(asignaturas.get(1).getAprobadaPorDecano());
        assertEquals("No procede", asignaturas.get(1).getObservacionDecision());
        assertNull(asignaturas.get(1).getObservacionEvaluacion());
        assertNull(asignaturas.get(1).getCumpleCondiciones());
        assertNull(asignaturas.get(0).getNota());
        assertNull(asignaturas.get(0).getSituacionCancelar());
    }

    @Test
    void laVistaDelEstudianteNoModificaLasFilasLeidas() {
        cancelacionEn("RADICADA", RolEtiquetaEtapa.ESTUDIANTE);
        SolicitudCancelacionAsignatura leida = gateway.getPorSolicitud(SOLICITUD);

        casoDeUso.getDetalle(SOLICITUD, TOKEN);

        assertEquals("Supera las faltas", leida.getAsignaturas().get(1).getObservacionEvaluacion());
    }

    @Test
    void unaSolicitudQueNoEsCancelacionDeAsignaturaRespondeComoInexistente() {
        when(gateway.getPorSolicitud("otra")).thenReturn(null);

        ErrorEntidadNoExisteExcepcion error = assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.getDetalle("otra", TOKEN));

        assertEquals(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, "Solicitud académica", "otra"), error.getMessage());
        verify(consultaCU, never()).getDetalle(any(), any());
    }

    @Test
    void unaSolicitudAjenaOFueraDeSuEtapaPropagaLaRespuestaDeSolicitudesAcademicas() {
        when(gateway.getPorSolicitud(SOLICITUD)).thenReturn(SolicitudCancelacionAsignatura.builder().build());
        when(consultaCU.getDetalle(SOLICITUD, TOKEN)).thenThrow(new ErrorEntidadNoExisteExcepcion("no existe"));

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.getDetalle(SOLICITUD, TOKEN));
        verify(consultaCU, never()).resolverActor(any(), any());
    }
}
