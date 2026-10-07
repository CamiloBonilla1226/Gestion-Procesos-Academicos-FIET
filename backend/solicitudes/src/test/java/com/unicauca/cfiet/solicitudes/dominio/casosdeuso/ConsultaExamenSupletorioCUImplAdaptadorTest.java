package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.ConsultaSolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorEntidadNoExisteExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorReglaNegocioVioladaExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultaExamenSupletorioCUImplAdaptadorTest {

    private static final String TOKEN = "token-jwt";
    private static final String ESTUDIANTE = "est-1";
    private static final String SOLICITUD = "sol-1";
    private static final String TIPO_ES = "tipo-es";

    @Mock
    private SolicitudExamenSupletorioGatewayIntPuerto gateway;

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

    private ConsultaExamenSupletorioCUImplAdaptador casoDeUso;

    private final TipoSolicitudAcademica supletorio = TipoSolicitudAcademica.builder().uuidTipoSolicitudAcademica(TIPO_ES).nombre("Examen Supletorio").build();

    private Usuario usuario(String uuid, String rol) {
        return Usuario.builder()
                .uuidUsuario(uuid)
                .roles(new ArrayList<>(List.of(Rol.builder().uuidRol("rol-" + rol).nombre(rol).estado(true).build())))
                .build();
    }

    private AsignaturaMatriculada materia(String uuid, String estado) {
        return AsignaturaMatriculada.builder().uuidAsignaturaMatriculada(uuid).grupo("A").estado(estado)
                .asignatura(Asignatura.builder().codigoAsignatura("C-" + uuid).nombreAsignatura("Materia " + uuid).build()).build();
    }

    private TipoAnexoAcademico tipoAnexo(String uuid, String nombre, String formatos) {
        return TipoAnexoAcademico.builder().uuidTipoAnexoAcademico(uuid).nombre(nombre).formatosPermitidos(formatos).build();
    }

    private void detalleGenerico(String etapa, RolEtiquetaEtapa rol, String etiqueta, List<AccionEtapa> acciones) {
        when(consultaCU.getDetalle(SOLICITUD, TOKEN)).thenReturn(DetalleSolicitudAcademica.builder()
                .solicitudAcademica(SolicitudAcademica.builder().uuidSolicitudAcademica(SOLICITUD).radicado("2026-ES-0001")
                        .etapa(EtapaSolicitudAcademica.builder().codigo(etapa).build()).build())
                .etiqueta(etiqueta)
                .accionesDisponibles(acciones)
                .build());
    }

    private SolicitudExamenSupletorio supletorio(CausaSupletorio causa, LocalDate fechaAcordada) {
        return SolicitudExamenSupletorio.builder()
                .solicitudAcademica(SolicitudAcademica.builder().uuidSolicitudAcademica(SOLICITUD).build())
                .asignaturaMatriculada(materia("am-1", "activa"))
                .fechaExamenNoPresentado(LocalDate.of(2026, 10, 5))
                .tipoCausa(causa)
                .cruce(causa == CausaSupletorio.CRUCE ? CruceSupletorio.builder().asignaturaMatriculadaCruzada(materia("am-2", "activa"))
                        .fechaExamenCruzada(LocalDate.of(2026, 10, 5)).horaExamenCruzada("10:00").build() : null)
                .fechaAcordadaExamen(fechaAcordada == null ? null : fechaAcordada.atStartOfDay())
                .build();
    }

    @BeforeEach
    void setUp() {
        casoDeUso = new ConsultaExamenSupletorioCUImplAdaptador(gateway, estudianteGateway, tipoSolicitudGateway, tipoAnexoGateway,
                sesionGateway, jwtServicio, consultaCU, new ExcepcionesFormateadorImplAdaptador());
        lenient().when(jwtServicio.getUsername(TOKEN)).thenReturn("alumno");
        lenient().when(sesionGateway.getUsuario("alumno")).thenReturn(usuario(ESTUDIANTE, "Estudiante"));
        lenient().when(estudianteGateway.getPorUuid(ESTUDIANTE)).thenReturn(Estudiante.builder().uuidUsuario(ESTUDIANTE).build());
        lenient().when(tipoSolicitudGateway.getTodos()).thenReturn(List.of(
                TipoSolicitudAcademica.builder().uuidTipoSolicitudAcademica("tipo-cm").nombre("Cancelación de Matrícula").build(), supletorio));
    }

    @Test
    void elEstudianteAutenticadoSaleDelTokenYOtroRolSeRechaza() {
        assertEquals(ESTUDIANTE, casoDeUso.getEstudianteAutenticado(TOKEN));

        when(sesionGateway.getUsuario("alumno")).thenReturn(usuario(ESTUDIANTE, "Funcionario Académico"));
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> casoDeUso.getFormulario(TOKEN));
        verify(tipoAnexoGateway, never()).getPorTipo(any());
    }

    @Test
    void elFormularioTraeSoloActivasLasDosCausasElPlazoYLosAnexosDeRadicacionConSuCausa() {
        when(estudianteGateway.getAsignaturasMatriculadas(ESTUDIANTE)).thenReturn(List.of(
                materia("am-1", "activa"), materia("am-2", "cancelada"), materia("am-3", "activa")));
        when(tipoAnexoGateway.getPorTipo(TIPO_ES)).thenReturn(List.of(
                tipoAnexo("an-doc", "Formato firmado por el docente de la asignatura con la que se cruza", "pdf,jpg,png"),
                tipoAnexo("an-recibo", "Recibo de pago", "pdf"),
                tipoAnexo("an-just", "Soporte de la justificación de la no presentación", "pdf,jpg,png"),
                tipoAnexo("an-comp", "Comprobante de pago", "pdf,jpg,png"),
                tipoAnexo("an-for23", "Formato PM-FO-4-FOR-23 firmado por el docente que orienta la asignatura", "pdf,jpg,png"),
                tipoAnexo("an-otro", "Otro documento", "pdf")));

        FormularioExamenSupletorio formulario = casoDeUso.getFormulario(TOKEN);

        assertEquals(List.of("am-1", "am-3"), formulario.getAsignaturas().stream().map(AsignaturaMatriculada::getUuidAsignaturaMatriculada).toList());
        assertEquals(List.of(CausaSupletorio.CRUCE, CausaSupletorio.OTRA), formulario.getCausas());
        assertEquals(3, formulario.getPlazoDiasHabiles());
        assertEquals(List.of("an-for23", "an-just", "an-doc"),
                formulario.getAnexos().stream().map(anexo -> anexo.getTipoAnexo().getUuidTipoAnexoAcademico()).toList());
        assertEquals(List.of(CausaSupletorio.CRUCE, CausaSupletorio.OTRA), formulario.getAnexos().get(0).getCausas());
        assertEquals(List.of(CausaSupletorio.OTRA), formulario.getAnexos().get(1).getCausas());
        assertEquals(List.of(CausaSupletorio.CRUCE), formulario.getAnexos().get(2).getCausas());
        assertTrue(formulario.getAnexos().stream().allMatch(anexo -> anexo.getTamanioMaximoBytes() == 5L * 1024 * 1024));
        assertEquals("pdf,jpg,png", formulario.getAnexos().get(0).getTipoAnexo().getFormatosPermitidos());
    }

    @Test
    void sinTipoExamenSupletorioElFormularioFalla() {
        when(tipoSolicitudGateway.getTodos()).thenReturn(List.of());

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.getFormulario(TOKEN));
    }

    @Test
    void elDetallePorCruceTraeElCruceYSinFechaAcordadaLaDejaVacia() {
        when(gateway.getPorSolicitud(SOLICITUD)).thenReturn(supletorio(CausaSupletorio.CRUCE, null));
        detalleGenerico("RADICADA", RolEtiquetaEtapa.FUNCIONARIO, "Pendiente", List.of(AccionEtapa.RECHAZAR_FUNCIONARIO, AccionEtapa.REMITIR_DECANO));

        DetalleExamenSupletorio detalle = casoDeUso.getDetalle(SOLICITUD, TOKEN);

        assertEquals("Pendiente", detalle.getDetalle().getEtiqueta());
        assertEquals(List.of(AccionEtapa.RECHAZAR_FUNCIONARIO, AccionEtapa.REMITIR_DECANO), detalle.getDetalle().getAccionesDisponibles());
        assertEquals("am-2", detalle.getSupletorio().getCruce().getAsignaturaMatriculadaCruzada().getUuidAsignaturaMatriculada());
        assertEquals("10:00", detalle.getSupletorio().getCruce().getHoraExamenCruzada());
        assertNull(detalle.getSupletorio().getFechaAcordadaExamen());
    }

    @Test
    void elDetallePorOtraCausaNoTraeCruceYEnAprobadaMuestraLaFechaAcordada() {
        when(gateway.getPorSolicitud(SOLICITUD)).thenReturn(supletorio(CausaSupletorio.OTRA, LocalDate.of(2026, 10, 20)));
        detalleGenerico("APROBADA", RolEtiquetaEtapa.ESTUDIANTE, "Aprobada", List.of());

        DetalleExamenSupletorio detalle = casoDeUso.getDetalle(SOLICITUD, TOKEN);

        assertNull(detalle.getSupletorio().getCruce());
        assertEquals(LocalDate.of(2026, 10, 20).atStartOfDay(), detalle.getSupletorio().getFechaAcordadaExamen());
        assertEquals("Aprobada", detalle.getDetalle().getEtiqueta());
        assertTrue(detalle.getDetalle().getAccionesDisponibles().isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"PENDIENTE_PAGO, ESTUDIANTE, Pendiente de pago, SUBIR_COMPROBANTE", "EN_VERIFICACION_PAGO, FUNCIONARIO, Pendiente de Verificación, APROBAR_COMPROBANTE"})
    void elDetalleConservaLaEtiquetaYLasAccionesDelRolEnCadaEtapa(String etapa, RolEtiquetaEtapa rol, String etiqueta, AccionEtapa accion) {
        when(gateway.getPorSolicitud(SOLICITUD)).thenReturn(supletorio(CausaSupletorio.OTRA, null));
        detalleGenerico(etapa, rol, etiqueta, List.of(accion));

        DetalleExamenSupletorio detalle = casoDeUso.getDetalle(SOLICITUD, TOKEN);

        assertEquals(etiqueta, detalle.getDetalle().getEtiqueta());
        assertEquals(List.of(accion), detalle.getDetalle().getAccionesDisponibles());
    }

    @Test
    void unaSolicitudQueNoEsSupletorioRespondeComoInexistenteSinConsultarElDetalle() {
        when(gateway.getPorSolicitud("otra")).thenReturn(null);

        ErrorEntidadNoExisteExcepcion error = assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.getDetalle("otra", TOKEN));

        assertEquals(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, "Solicitud académica", "otra"), error.getMessage());
        verify(consultaCU, never()).getDetalle(any(), any());
    }

    @Test
    void unaSolicitudAjenaOFueraDeLaEtapaVisibleNoSeVe() {
        when(gateway.getPorSolicitud(SOLICITUD)).thenReturn(supletorio(CausaSupletorio.CRUCE, null));
        when(consultaCU.getDetalle(SOLICITUD, TOKEN)).thenThrow(new ErrorEntidadNoExisteExcepcion("no existe"));

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.getDetalle(SOLICITUD, TOKEN));
    }
}
