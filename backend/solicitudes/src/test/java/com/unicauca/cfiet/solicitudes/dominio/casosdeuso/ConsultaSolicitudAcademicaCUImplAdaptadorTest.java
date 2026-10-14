package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.AnexoAcademicoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.MaquinaEtapas;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorEntidadNoExisteExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorGenericoExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorReglaNegocioVioladaExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultaSolicitudAcademicaCUImplAdaptadorTest {

    private static final String ESTUDIANTE = "est-1";
    private static final String OTRO_ESTUDIANTE = "est-2";
    private static final String FUNCIONARIO = "fa-1";
    private static final String OTRO_FUNCIONARIO = "fa-2";
    private static final String DECANO = "dec-1";
    private static final String SIN_ROL = "func-1";
    private static final String SOLICITUD = "sol-1";

    @Mock
    private SolicitudAcademicaGatewayIntPuerto gateway;

    @Mock
    private EtapaEtiquetaRolGatewayIntPuerto etiquetaGateway;

    @Mock
    private AnexoAcademicoGatewayIntPuerto anexoGateway;

    @Mock
    private HistorialSolicitudAcademicaGatewayIntPuerto historialGateway;

    @Mock
    private ResolucionAcademicaGatewayIntPuerto resolucionGateway;

    @Mock
    private SesionGatewayIntPuerto sesionGateway;

    @Mock
    private IJwtServicio jwtServicio;

    @Mock
    private AnexoAcademicoCUIntPuerto anexoCU;

    private ConsultaSolicitudAcademicaCUImplAdaptador casoDeUso;

    private final TipoSolicitudAcademica matricula = TipoSolicitudAcademica.builder()
            .uuidTipoSolicitudAcademica("tipo-cm")
            .nombre("Cancelación de Matrícula")
            .funcionarioAcademico(FuncionarioAcademico.builder().uuidUsuario(FUNCIONARIO).build())
            .build();

    private EtapaSolicitudAcademica etapa(String codigo) {
        return EtapaSolicitudAcademica.builder().uuidEtapa("etapa-" + codigo).codigo(codigo).build();
    }

    private List<EtapaEtiquetaRol> etiquetas(RolEtiquetaEtapa rol, String... excluidas) {
        List<EtapaEtiquetaRol> filas = new ArrayList<>();
        for (String codigo : ETAPAS)
            if (!Arrays.asList(excluidas).contains(codigo))
                filas.add(EtapaEtiquetaRol.builder().etapa(etapa(codigo)).rol(rol).etiqueta(rol + ":" + codigo).build());
        return filas;
    }

    private SolicitudAcademica solicitud(String uuid, String duenio, String codigoEtapa, LocalDateTime fecha) {
        return SolicitudAcademica.builder()
                .uuidSolicitudAcademica(uuid)
                .radicado("2026-CM-" + uuid)
                .estudiante(Estudiante.builder().uuidUsuario(duenio).build())
                .tipoSolicitudAcademica(matricula)
                .etapa(etapa(codigoEtapa))
                .fechaCreacion(fecha)
                .build();
    }

    private SolicitudAcademica solicitud(String codigoEtapa) {
        return solicitud(SOLICITUD, ESTUDIANTE, codigoEtapa, LocalDateTime.of(2026, 3, 1, 8, 0));
    }

    private void sesion(String uuid, String... roles) {
        List<Rol> lista = new ArrayList<>();
        for (String rol : roles)
            lista.add(Rol.builder().uuidRol("rol-" + rol).nombre(rol).estado(true).build());
        lenient().when(jwtServicio.getUsername("tok-" + uuid)).thenReturn("user-" + uuid);
        lenient().when(sesionGateway.getUsuario("user-" + uuid)).thenReturn(Usuario.builder().uuidUsuario(uuid).roles(lista).build());
    }

    private String token(String uuid) {
        return "tok-" + uuid;
    }

    @BeforeEach
    void setUp() {
        ExcepcionesFormateadorImplAdaptador formateador = new ExcepcionesFormateadorImplAdaptador();
        casoDeUso = new ConsultaSolicitudAcademicaCUImplAdaptador(gateway, etiquetaGateway, anexoGateway, historialGateway,
                resolucionGateway, sesionGateway, jwtServicio, anexoCU, new MaquinaEtapas(formateador), formateador);
        lenient().when(etiquetaGateway.getPorRol(RolEtiquetaEtapa.ESTUDIANTE)).thenReturn(etiquetas(RolEtiquetaEtapa.ESTUDIANTE));
        lenient().when(etiquetaGateway.getPorRol(RolEtiquetaEtapa.FUNCIONARIO)).thenReturn(etiquetas(RolEtiquetaEtapa.FUNCIONARIO));
        lenient().when(etiquetaGateway.getPorRol(RolEtiquetaEtapa.DECANO)).thenReturn(etiquetas(RolEtiquetaEtapa.DECANO, RADICADA));
        sesion(ESTUDIANTE, "Estudiante");
        sesion(OTRO_ESTUDIANTE, "Estudiante");
        sesion(FUNCIONARIO, "Funcionario Académico");
        sesion(OTRO_FUNCIONARIO, "Funcionario Académico");
        sesion(DECANO, "Decano");
        sesion(SIN_ROL, "Funcionario");
    }

    @Test
    void laBandejaDelEstudianteTraeSusSolicitudesConSuEtiquetaEnElOrdenDelGateway() {
        SolicitudAcademica reciente = solicitud("s2", ESTUDIANTE, APROBADA, LocalDateTime.of(2026, 3, 2, 8, 0));
        SolicitudAcademica antigua = solicitud("s1", ESTUDIANTE, RADICADA, LocalDateTime.of(2026, 3, 1, 8, 0));
        when(gateway.getPorEstudiante(ESTUDIANTE)).thenReturn(List.of(reciente, antigua));

        List<ResumenSolicitudAcademica> bandeja = casoDeUso.getBandejaEstudiante(token(ESTUDIANTE));

        assertEquals(List.of("s2", "s1"), bandeja.stream().map(r -> r.getSolicitudAcademica().getUuidSolicitudAcademica()).toList());
        assertEquals(List.of("ESTUDIANTE:APROBADA", "ESTUDIANTE:RADICADA"), bandeja.stream().map(ResumenSolicitudAcademica::getEtiqueta).toList());
    }

    @Test
    void laBandejaDelFuncionarioConsultaLosTiposAsignadosAEl() {
        when(gateway.getPorFuncionarioAcademico(FUNCIONARIO)).thenReturn(List.of(solicitud(RADICADA)));

        List<ResumenSolicitudAcademica> bandeja = casoDeUso.getBandejaFuncionario(token(FUNCIONARIO));

        assertEquals(1, bandeja.size());
        assertEquals("FUNCIONARIO:RADICADA", bandeja.get(0).getEtiqueta());
        verify(gateway).getPorFuncionarioAcademico(FUNCIONARIO);
    }

    @Test
    void elFuncionarioVeEnviarReciboEnElSupletorioAprobadoPorElDecanoYLaEtiquetaDeLaTablaEnLosDemas() {
        TipoSolicitudAcademica supletorio = TipoSolicitudAcademica.builder()
                .uuidTipoSolicitudAcademica("tipo-es")
                .nombre("Examen Supletorio")
                .funcionarioAcademico(FuncionarioAcademico.builder().uuidUsuario(FUNCIONARIO).build())
                .build();
        SolicitudAcademica supletorioAprobado = solicitud("s1", ESTUDIANTE, APROBADA_POR_DECANO, LocalDateTime.of(2026, 3, 3, 8, 0));
        supletorioAprobado.setTipoSolicitudAcademica(supletorio);
        SolicitudAcademica supletorioRechazado = solicitud("s2", ESTUDIANTE, RECHAZADA_POR_DECANO, LocalDateTime.of(2026, 3, 2, 8, 0));
        supletorioRechazado.setTipoSolicitudAcademica(supletorio);
        SolicitudAcademica matriculaAprobada = solicitud("s3", ESTUDIANTE, APROBADA_POR_DECANO, LocalDateTime.of(2026, 3, 1, 8, 0));
        when(gateway.getPorFuncionarioAcademico(FUNCIONARIO)).thenReturn(List.of(supletorioAprobado, supletorioRechazado, matriculaAprobada));
        when(gateway.getTodas()).thenReturn(List.of(supletorioAprobado));
        when(gateway.getPorUuid("s1")).thenReturn(supletorioAprobado);

        List<ResumenSolicitudAcademica> bandeja = casoDeUso.getBandejaFuncionario(token(FUNCIONARIO));

        assertEquals(List.of("Enviar Recibo", "FUNCIONARIO:RECHAZADA_POR_DECANO", "FUNCIONARIO:APROBADA_POR_DECANO"),
                bandeja.stream().map(ResumenSolicitudAcademica::getEtiqueta).toList());
        assertEquals("Enviar Recibo", casoDeUso.getDetalle("s1", token(FUNCIONARIO)).getEtiqueta());
        assertEquals("DECANO:APROBADA_POR_DECANO", casoDeUso.getBandejaDecano(token(DECANO)).get(0).getEtiqueta());
    }

    private SolicitudAcademica deTipo(String uuid, String codigoEtapa, String nombreTipo) {
        SolicitudAcademica solicitud = solicitud(uuid, ESTUDIANTE, codigoEtapa, LocalDateTime.of(2026, 3, 1, 8, 0));
        solicitud.setTipoSolicitudAcademica(TipoSolicitudAcademica.builder()
                .uuidTipoSolicitudAcademica("tipo-" + nombreTipo)
                .nombre(nombreTipo)
                .funcionarioAcademico(FuncionarioAcademico.builder().uuidUsuario(FUNCIONARIO).build())
                .build());
        return solicitud;
    }

    @Test
    void elResumenDelFuncionarioYDelDecanoTraeLaDecisionDelDecanoTambienDespuesDeResponder() {
        List<SolicitudAcademica> solicitudes = List.of(
                deTipo("s1", APROBADA, "Cancelación de Matrícula"),
                deTipo("s2", APROBADA_POR_DECANO, "Cancelación de Asignatura"),
                deTipo("s3", APROBADA, "Cancelación de Asignatura"),
                deTipo("s4", RECHAZADA, "Examen Supletorio"),
                deTipo("s5", RECHAZADA, "Cancelación de Matrícula"),
                deTipo("s6", EN_REVISION_DECANO, "Examen Supletorio"),
                deTipo("s7", RECHAZADA, "Examen Supletorio"));
        when(gateway.getPorFuncionarioAcademico(FUNCIONARIO)).thenReturn(solicitudes);
        when(gateway.getTodas()).thenReturn(solicitudes);
        when(gateway.getUltimaAccion(List.of("s1", "s2", "s3", "s4", "s5", "s6", "s7"), List.of("APROBAR_DECANO", "RECHAZAR_DECANO")))
                .thenReturn(Map.of("s1", "APROBAR_DECANO", "s2", "APROBAR_DECANO", "s3", "APROBAR_DECANO",
                        "s4", "RECHAZAR_DECANO", "s7", "APROBAR_DECANO"));
        when(gateway.getConAsignaturaNoAprobadaPorDecano(List.of("s2", "s3"))).thenReturn(Set.of("s2"));

        List<String> esperadas = Arrays.asList("APROBADA", "APROBADA_PARCIAL", "APROBADA", "RECHAZADA", null, null, "APROBADA");
        assertEquals(esperadas, casoDeUso.getBandejaFuncionario(token(FUNCIONARIO)).stream().map(ResumenSolicitudAcademica::getDecisionDecano).toList());
        assertEquals(esperadas, casoDeUso.getBandejaDecano(token(DECANO)).stream().map(ResumenSolicitudAcademica::getDecisionDecano).toList());
    }

    @Test
    void elResumenDelEstudianteNoTraeLaDecisionDelDecanoNiLaConsulta() {
        when(gateway.getPorEstudiante(ESTUDIANTE)).thenReturn(List.of(deTipo("s1", APROBADA, "Cancelación de Matrícula")));

        List<ResumenSolicitudAcademica> bandeja = casoDeUso.getBandejaEstudiante(token(ESTUDIANTE));

        assertNull(bandeja.get(0).getDecisionDecano());
        verify(gateway, never()).getUltimaAccion(any(), any());
        verify(gateway, never()).getConAsignaturaNoAprobadaPorDecano(any());
    }

    @Test
    void laBandejaDelDecanoOcultaLasEtapasSinEtiquetaParaEl() {
        SolicitudAcademica radicada = solicitud("s1", ESTUDIANTE, RADICADA, LocalDateTime.of(2026, 3, 2, 8, 0));
        SolicitudAcademica enRevision = solicitud("s2", OTRO_ESTUDIANTE, EN_REVISION_DECANO, LocalDateTime.of(2026, 3, 1, 8, 0));
        when(gateway.getTodas()).thenReturn(List.of(radicada, enRevision));

        List<ResumenSolicitudAcademica> bandeja = casoDeUso.getBandejaDecano(token(DECANO));

        assertEquals(List.of("s2"), bandeja.stream().map(r -> r.getSolicitudAcademica().getUuidSolicitudAcademica()).toList());
        assertEquals("DECANO:EN_REVISION_DECANO", bandeja.get(0).getEtiqueta());
    }

    @Test
    void unaBandejaPedidaSinElRolSeRechazaSinConsultar() {
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> casoDeUso.getBandejaDecano(token(FUNCIONARIO)));
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> casoDeUso.getBandejaEstudiante(token(DECANO)));
        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> casoDeUso.getBandejaFuncionario(token(SIN_ROL)));
        verifyNoInteractions(gateway);
    }

    @Test
    void unTokenSinUsuarioEsUnErrorGenerico() {
        when(jwtServicio.getUsername("roto")).thenReturn(null);

        assertThrows(ErrorGenericoExcepcion.class, () -> casoDeUso.getDetalle(SOLICITUD, "roto"));
    }

    @Test
    void elDuenioVeElDetalleComoEstudianteSinAccionesYSinDescargaAntesDelFinal() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(APROBADA_POR_DECANO));
        when(resolucionGateway.existePorSolicitud(SOLICITUD)).thenReturn(true);
        when(anexoGateway.getPorSolicitud(SOLICITUD)).thenReturn(List.of(AnexoAcademico.builder().uuidAnexoAcademico("a1").build()));

        DetalleSolicitudAcademica detalle = casoDeUso.getDetalle(SOLICITUD, token(ESTUDIANTE));

        assertEquals("ESTUDIANTE:APROBADA_POR_DECANO", detalle.getEtiqueta());
        assertTrue(detalle.isTieneResolucion());
        assertFalse(detalle.isPuedeDescargarResolucion());
        assertTrue(detalle.getAccionesDisponibles().isEmpty());
        assertEquals(1, detalle.getAnexos().size());
    }

    @Test
    void elDuenioPuedeDescargarLaResolucionEnEtapaFinal() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(RECHAZADA));
        when(resolucionGateway.existePorSolicitud(SOLICITUD)).thenReturn(true);

        DetalleSolicitudAcademica detalle = casoDeUso.getDetalle(SOLICITUD, token(ESTUDIANTE));

        assertTrue(detalle.isPuedeDescargarResolucion());
        assertEquals("ESTUDIANTE:RECHAZADA", detalle.getEtiqueta());
    }

    @Test
    void elFuncionarioAsignadoVeElDetalleConSusAccionesYPuedeDescargarEnCualquierEtapa() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(RADICADA));
        when(resolucionGateway.existePorSolicitud(SOLICITUD)).thenReturn(true);

        DetalleSolicitudAcademica detalle = casoDeUso.getDetalle(SOLICITUD, token(FUNCIONARIO));

        assertEquals("FUNCIONARIO:RADICADA", detalle.getEtiqueta());
        assertEquals(List.of(AccionEtapa.RECHAZAR_FUNCIONARIO, AccionEtapa.REMITIR_DECANO), detalle.getAccionesDisponibles());
        assertTrue(detalle.isPuedeDescargarResolucion());
    }

    @Test
    void sinResolucionNadieLaPuedeDescargar() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(APROBADA_POR_DECANO));
        when(resolucionGateway.existePorSolicitud(SOLICITUD)).thenReturn(false);

        DetalleSolicitudAcademica detalle = casoDeUso.getDetalle(SOLICITUD, token(FUNCIONARIO));

        assertFalse(detalle.isTieneResolucion());
        assertFalse(detalle.isPuedeDescargarResolucion());
        assertEquals(List.of(AccionEtapa.ENVIAR_RESPUESTA), detalle.getAccionesDisponibles());
    }

    @Test
    void elDecanoVeElDetalleEnRevisionConSusAcciones() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(EN_REVISION_DECANO));

        DetalleSolicitudAcademica detalle = casoDeUso.getDetalle(SOLICITUD, token(DECANO));

        assertEquals("DECANO:EN_REVISION_DECANO", detalle.getEtiqueta());
        assertEquals(List.of(AccionEtapa.APROBAR_DECANO, AccionEtapa.RECHAZAR_DECANO), detalle.getAccionesDisponibles());
    }

    private List<SolicitudAcademica> unaPorEtapa(String... codigos) {
        List<SolicitudAcademica> solicitudes = new ArrayList<>();
        for (String codigo : codigos)
            solicitudes.add(solicitud("s-" + codigo, ESTUDIANTE, codigo, LocalDateTime.of(2026, 3, 1, 8, 0)));
        return solicitudes;
    }

    @Test
    void enLaBandejaElEstudianteVeLaDecisionDelDecanoComoEnRevision() {
        String[] etapas = {EN_REVISION_DECANO, APROBADA_POR_DECANO, RECHAZADA_POR_DECANO, PENDIENTE_PAGO, EN_VERIFICACION_PAGO, APROBADA, RECHAZADA};
        when(gateway.getPorEstudiante(ESTUDIANTE)).thenReturn(unaPorEtapa(etapas));
        when(gateway.getPorFuncionarioAcademico(FUNCIONARIO)).thenReturn(unaPorEtapa(etapas));
        when(gateway.getTodas()).thenReturn(unaPorEtapa(etapas));

        assertEquals(List.of(EN_REVISION_DECANO, EN_REVISION_DECANO, EN_REVISION_DECANO, PENDIENTE_PAGO, EN_VERIFICACION_PAGO, APROBADA, RECHAZADA),
                casoDeUso.getBandejaEstudiante(token(ESTUDIANTE)).stream().map(ResumenSolicitudAcademica::getEtapaCodigo).toList());
        assertEquals(List.of(etapas),
                casoDeUso.getBandejaFuncionario(token(FUNCIONARIO)).stream().map(ResumenSolicitudAcademica::getEtapaCodigo).toList());
        assertEquals(List.of(etapas),
                casoDeUso.getBandejaDecano(token(DECANO)).stream().map(ResumenSolicitudAcademica::getEtapaCodigo).toList());
    }

    private SolicitudAcademica conDocumento(String codigoEtapa, String numeroDocumento) {
        SolicitudAcademica solicitud = solicitud(codigoEtapa);
        solicitud.setEstudiante(Estudiante.builder().uuidUsuario(ESTUDIANTE)
                .usuario(Usuario.builder().uuidUsuario(ESTUDIANTE).numeroDocumento(numeroDocumento).build()).build());
        return solicitud;
    }

    @Test
    void elFuncionarioYElDecanoVenElNumeroDeDocumentoEnLaBandejaYElEstudianteNo() {
        when(gateway.getPorEstudiante(ESTUDIANTE)).thenReturn(List.of(conDocumento(EN_REVISION_DECANO, "1061789234")));
        when(gateway.getPorFuncionarioAcademico(FUNCIONARIO)).thenReturn(List.of(conDocumento(RADICADA, "1061789234")));
        when(gateway.getTodas()).thenReturn(List.of(conDocumento(EN_REVISION_DECANO, "1061789234")));

        assertNull(casoDeUso.getBandejaEstudiante(token(ESTUDIANTE)).get(0).getNumeroDocumento());
        assertEquals("1061789234", casoDeUso.getBandejaFuncionario(token(FUNCIONARIO)).get(0).getNumeroDocumento());
        assertEquals("1061789234", casoDeUso.getBandejaDecano(token(DECANO)).get(0).getNumeroDocumento());
    }

    @Test
    void sinUsuarioDelEstudianteElNumeroDeDocumentoQuedaNulo() {
        when(gateway.getPorFuncionarioAcademico(FUNCIONARIO)).thenReturn(List.of(solicitud(RADICADA)));

        assertNull(casoDeUso.getBandejaFuncionario(token(FUNCIONARIO)).get(0).getNumeroDocumento());
    }

    @Test
    void elFuncionarioYElDecanoVenElNumeroDeDocumentoEnElDetalleYElEstudianteNo() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(conDocumento(EN_REVISION_DECANO, "1061789234"));

        assertNull(casoDeUso.getDetalle(SOLICITUD, token(ESTUDIANTE)).getNumeroDocumento());
        assertEquals("1061789234", casoDeUso.getDetalle(SOLICITUD, token(FUNCIONARIO)).getNumeroDocumento());
        assertEquals("1061789234", casoDeUso.getDetalle(SOLICITUD, token(DECANO)).getNumeroDocumento());
    }

    @Test
    void sinUsuarioDelEstudianteElNumeroDeDocumentoDelDetalleQuedaNulo() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(EN_REVISION_DECANO));

        assertNull(casoDeUso.getDetalle(SOLICITUD, token(DECANO)).getNumeroDocumento());
    }

    @Test
    void enElDetalleElEstudianteVeLaDecisionDelDecanoComoEnRevisionYLosDemasElCodigoReal() {
        for (String etapa : List.of(APROBADA_POR_DECANO, RECHAZADA_POR_DECANO)) {
            when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(etapa));

            DetalleSolicitudAcademica delEstudiante = casoDeUso.getDetalle(SOLICITUD, token(ESTUDIANTE));

            assertEquals(EN_REVISION_DECANO, delEstudiante.getEtapaCodigo());
            assertEquals("ESTUDIANTE:" + etapa, delEstudiante.getEtiqueta());
            assertEquals(etapa, delEstudiante.getSolicitudAcademica().getEtapa().getCodigo());
            assertEquals(etapa, casoDeUso.getDetalle(SOLICITUD, token(FUNCIONARIO)).getEtapaCodigo());
            assertEquals(etapa, casoDeUso.getDetalle(SOLICITUD, token(DECANO)).getEtapaCodigo());
        }
    }

    @Test
    void lasEtapasPosterioresALaDecisionNoSeEnmascaranParaElEstudiante() {
        for (String etapa : List.of(PENDIENTE_PAGO, EN_VERIFICACION_PAGO, APROBADA, RECHAZADA, EN_REVISION_DECANO, RADICADA)) {
            when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(etapa));

            assertEquals(etapa, casoDeUso.getDetalle(SOLICITUD, token(ESTUDIANTE)).getEtapaCodigo());
        }
    }

    @Test
    void elDecanoNoVeUnaSolicitudRadicadaYRecibeLaMismaRespuestaQueSiNoExistiera() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(RADICADA));
        ErrorEntidadNoExisteExcepcion oculta = assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.getDetalle(SOLICITUD, token(DECANO)));

        when(gateway.getPorUuid(SOLICITUD)).thenReturn(null);
        ErrorEntidadNoExisteExcepcion inexistente = assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.getDetalle(SOLICITUD, token(DECANO)));

        assertEquals(inexistente.getMessage(), oculta.getMessage());
    }

    @Test
    void unaSolicitudAjenaRespondeIgualQueUnaInexistente() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(RADICADA));
        ErrorEntidadNoExisteExcepcion deOtroEstudiante = assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.getDetalle(SOLICITUD, token(OTRO_ESTUDIANTE)));
        ErrorEntidadNoExisteExcepcion deFuncionarioNoAsignado = assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.getDetalle(SOLICITUD, token(OTRO_FUNCIONARIO)));
        ErrorEntidadNoExisteExcepcion deOtroRol = assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.getDetalle(SOLICITUD, token(SIN_ROL)));

        when(gateway.getPorUuid(SOLICITUD)).thenReturn(null);
        ErrorEntidadNoExisteExcepcion inexistente = assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.getDetalle(SOLICITUD, token(OTRO_ESTUDIANTE)));

        assertEquals(inexistente.getMessage(), deOtroEstudiante.getMessage());
        assertEquals(inexistente.getMessage(), deFuncionarioNoAsignado.getMessage());
        assertEquals(inexistente.getMessage(), deOtroRol.getMessage());
        verifyNoInteractions(anexoGateway, resolucionGateway);
    }

    @Test
    void elRolLoDecideElServidorEnElOrdenDuenioFuncionarioDecano() {
        sesion("todo", "Estudiante", "Funcionario Académico", "Decano");
        TipoSolicitudAcademica asignadoATodo = TipoSolicitudAcademica.builder()
                .uuidTipoSolicitudAcademica("tipo-cm").nombre("Cancelación de Matrícula")
                .funcionarioAcademico(FuncionarioAcademico.builder().uuidUsuario("todo").build()).build();
        SolicitudAcademica propia = solicitud(SOLICITUD, "todo", EN_REVISION_DECANO, LocalDateTime.of(2026, 3, 1, 8, 0));
        propia.setTipoSolicitudAcademica(asignadoATodo);
        SolicitudAcademica asignada = solicitud("s2", ESTUDIANTE, EN_REVISION_DECANO, LocalDateTime.of(2026, 3, 1, 8, 0));
        asignada.setTipoSolicitudAcademica(asignadoATodo);
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(propia);
        when(gateway.getPorUuid("s2")).thenReturn(asignada);
        when(gateway.getPorUuid("s3")).thenReturn(solicitud("s3", ESTUDIANTE, EN_REVISION_DECANO, LocalDateTime.of(2026, 3, 1, 8, 0)));

        assertEquals(RolEtiquetaEtapa.ESTUDIANTE, casoDeUso.resolverActor(SOLICITUD, token("todo")).getRol());
        assertEquals(RolEtiquetaEtapa.FUNCIONARIO, casoDeUso.resolverActor("s2", token("todo")).getRol());
        assertEquals(RolEtiquetaEtapa.DECANO, casoDeUso.resolverActor("s3", token("todo")).getRol());
        assertEquals("todo", casoDeUso.resolverActor("s3", token("todo")).getUuidUsuario());
    }

    @Test
    void elHistorialSoloSeConsultaSiHayAcceso() {
        HistorialSolicitudAcademica fila = HistorialSolicitudAcademica.builder().accion("RADICAR").build();
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(RADICADA));
        when(historialGateway.getPorSolicitud(SOLICITUD)).thenReturn(List.of(fila));

        assertEquals(List.of(fila), casoDeUso.getHistorial(SOLICITUD, token(ESTUDIANTE)));
        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.getHistorial(SOLICITUD, token(OTRO_ESTUDIANTE)));
        verify(historialGateway, times(1)).getPorSolicitud(any());
    }

    private HistorialSolicitudAcademica fila(String accion) {
        return HistorialSolicitudAcademica.builder().accion(accion).fecha(LocalDateTime.of(2026, 3, 1, 8, 0)).build();
    }

    @Test
    void cadaFilaDelHistorialTraeLaEtapaALaQueLlevoAunqueLasFechasEmpaten() {
        HistorialSolicitudAcademica respuesta = fila("ENVIAR_RESPUESTA");
        HistorialSolicitudAcademica radicar = fila("RADICAR");
        HistorialSolicitudAcademica rechazo = fila("RECHAZAR_DECANO");
        HistorialSolicitudAcademica remision = fila("REMITIR_DECANO");
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(RECHAZADA));
        when(historialGateway.getPorSolicitud(SOLICITUD)).thenReturn(List.of(respuesta, radicar, rechazo, remision));

        List<HistorialSolicitudAcademica> historial = casoDeUso.getHistorial(SOLICITUD, token(FUNCIONARIO));

        assertEquals(List.of(respuesta, radicar, rechazo, remision), historial);
        assertEquals(Arrays.asList(RECHAZADA, RADICADA, RECHAZADA_POR_DECANO, EN_REVISION_DECANO),
                historial.stream().map(HistorialSolicitudAcademica::getEtapaCodigo).toList());
    }

    private List<HistorialSolicitudAcademica> historialConObservaciones() {
        return List.of(
                HistorialSolicitudAcademica.builder().accion("RADICAR").observaciones("Radicada").build(),
                HistorialSolicitudAcademica.builder().accion("REMITIR_DECANO").observaciones("Para el Decano").build(),
                HistorialSolicitudAcademica.builder().accion("RECHAZAR_DECANO").observaciones("No procede").build(),
                HistorialSolicitudAcademica.builder().accion("ENVIAR_RESPUESTA").observaciones("Mensaje al estudiante").build());
    }

    @Test
    void elEstudianteVeVaciaLaObservacionDeLaRemisionYNoVeLaDecisionDelDecano() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(RECHAZADA));
        when(historialGateway.getPorSolicitud(SOLICITUD)).thenReturn(historialConObservaciones());

        List<HistorialSolicitudAcademica> historial = casoDeUso.getHistorial(SOLICITUD, token(ESTUDIANTE));

        assertEquals(Arrays.asList("Radicada", null, "Mensaje al estudiante"),
                historial.stream().map(HistorialSolicitudAcademica::getObservaciones).toList());
        assertEquals(List.of("RADICAR", "REMITIR_DECANO", "ENVIAR_RESPUESTA"),
                historial.stream().map(HistorialSolicitudAcademica::getAccion).toList());
    }

    @Test
    void elEstudianteNoVeLasDecisionesDelDecanoEnNingunProceso() {
        for (String proceso : List.of("Cancelación de Matrícula", "Cancelación de Asignatura", "Examen Supletorio")) {
            SolicitudAcademica solicitud = solicitud(EN_REVISION_DECANO);
            solicitud.setTipoSolicitudAcademica(TipoSolicitudAcademica.builder().uuidTipoSolicitudAcademica("tipo").nombre(proceso)
                    .funcionarioAcademico(FuncionarioAcademico.builder().uuidUsuario(FUNCIONARIO).build()).build());
            when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud);
            when(historialGateway.getPorSolicitud(SOLICITUD)).thenReturn(List.of(fila("RADICAR"), fila("REMITIR_DECANO"),
                    fila("APROBAR_DECANO"), fila("RECHAZAR_DECANO")));

            assertEquals(List.of("RADICAR", "REMITIR_DECANO"),
                    casoDeUso.getHistorial(SOLICITUD, token(ESTUDIANTE)).stream().map(HistorialSolicitudAcademica::getAccion).toList());
            assertEquals(List.of("RADICAR", "REMITIR_DECANO", "APROBAR_DECANO", "RECHAZAR_DECANO"),
                    casoDeUso.getHistorial(SOLICITUD, token(FUNCIONARIO)).stream().map(HistorialSolicitudAcademica::getAccion).toList());
            assertEquals(4, casoDeUso.getHistorial(SOLICITUD, token(DECANO)).size());
        }
    }

    @Test
    void elFuncionarioYElDecanoVenTodasLasObservacionesDelHistorial() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(RECHAZADA));
        when(historialGateway.getPorSolicitud(SOLICITUD)).thenReturn(historialConObservaciones(), historialConObservaciones());

        for (String usuario : List.of(FUNCIONARIO, DECANO))
            assertEquals(List.of("Radicada", "Para el Decano", "No procede", "Mensaje al estudiante"),
                    casoDeUso.getHistorial(SOLICITUD, token(usuario)).stream().map(HistorialSolicitudAcademica::getObservaciones).toList());
    }

    @Test
    void elEstudianteNoVeLaFilaDeAprobarDecanoHastaLaRespuesta() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(APROBADA_POR_DECANO));
        when(historialGateway.getPorSolicitud(SOLICITUD)).thenReturn(List.of(fila("RADICAR"), fila("REMITIR_DECANO"),
                HistorialSolicitudAcademica.builder().accion("APROBAR_DECANO").observaciones("Procede").build()));

        List<HistorialSolicitudAcademica> historial = casoDeUso.getHistorial(SOLICITUD, token(ESTUDIANTE));

        assertEquals(List.of("RADICAR", "REMITIR_DECANO"), historial.stream().map(HistorialSolicitudAcademica::getAccion).toList());
        assertEquals(List.of(RADICADA, EN_REVISION_DECANO), historial.stream().map(HistorialSolicitudAcademica::getEtapaCodigo).toList());
    }

    @Test
    void elHistorialDelEstudianteNoTraeCodigosDeDecisionDelDecanoYCoincideConSuDetalle() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(RECHAZADA_POR_DECANO));
        when(historialGateway.getPorSolicitud(SOLICITUD)).thenReturn(List.of(fila("RADICAR"), fila("REMITIR_DECANO"), fila("RECHAZAR_DECANO")));

        List<String> codigos = casoDeUso.getHistorial(SOLICITUD, token(ESTUDIANTE)).stream().map(HistorialSolicitudAcademica::getEtapaCodigo).toList();

        assertEquals(List.of(RADICADA, EN_REVISION_DECANO), codigos);
        assertEquals(codigos.get(codigos.size() - 1), casoDeUso.getDetalle(SOLICITUD, token(ESTUDIANTE)).getEtapaCodigo());
    }

    @Test
    void conLaRespuestaElEstudianteVeLaEtapaFinalSinLaFilaDelDecano() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(APROBADA));
        when(historialGateway.getPorSolicitud(SOLICITUD)).thenReturn(List.of(fila("RADICAR"), fila("REMITIR_DECANO"),
                fila("APROBAR_DECANO"), fila("ENVIAR_RESPUESTA")));

        List<HistorialSolicitudAcademica> historial = casoDeUso.getHistorial(SOLICITUD, token(ESTUDIANTE));

        assertEquals(List.of(RADICADA, EN_REVISION_DECANO, APROBADA), historial.stream().map(HistorialSolicitudAcademica::getEtapaCodigo).toList());
    }

    @Test
    void laRespuestaDespuesDeLaAprobacionLlevaAAprobada() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(APROBADA));
        when(historialGateway.getPorSolicitud(SOLICITUD)).thenReturn(List.of(fila("RADICAR"), fila("REMITIR_DECANO"),
                fila("APROBAR_DECANO"), fila("ENVIAR_RESPUESTA")));

        List<HistorialSolicitudAcademica> historial = casoDeUso.getHistorial(SOLICITUD, token(FUNCIONARIO));

        assertEquals(List.of(RADICADA, EN_REVISION_DECANO, APROBADA_POR_DECANO, APROBADA),
                historial.stream().map(HistorialSolicitudAcademica::getEtapaCodigo).toList());
    }

    @Test
    void elHistorialDelSupletorioSigueSusPropiasTransiciones() {
        SolicitudAcademica supletorio = solicitud(APROBADA);
        supletorio.setTipoSolicitudAcademica(TipoSolicitudAcademica.builder().uuidTipoSolicitudAcademica("tipo-es").nombre("Examen Supletorio")
                .funcionarioAcademico(FuncionarioAcademico.builder().uuidUsuario(FUNCIONARIO).build()).build());
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(supletorio);
        when(historialGateway.getPorSolicitud(SOLICITUD)).thenReturn(List.of(fila("RADICAR"), fila("REMITIR_DECANO"), fila("APROBAR_DECANO"),
                fila("ENVIAR_RECIBO"), fila("SUBIR_COMPROBANTE"), fila("APROBAR_COMPROBANTE")));

        List<HistorialSolicitudAcademica> historial = casoDeUso.getHistorial(SOLICITUD, token(ESTUDIANTE));

        assertEquals(List.of(RADICADA, EN_REVISION_DECANO, PENDIENTE_PAGO, EN_VERIFICACION_PAGO, APROBADA),
                historial.stream().map(HistorialSolicitudAcademica::getEtapaCodigo).toList());
        assertEquals(List.of(RADICADA, EN_REVISION_DECANO, APROBADA_POR_DECANO, PENDIENTE_PAGO, EN_VERIFICACION_PAGO, APROBADA),
                casoDeUso.getHistorial(SOLICITUD, token(FUNCIONARIO)).stream().map(HistorialSolicitudAcademica::getEtapaCodigo).toList());
    }

    @Test
    void unaAccionQueNoEncajaEnElRecorridoQuedaSinEtapa() {
        HistorialSolicitudAcademica desconocida = fila("ACCION_RARA");
        HistorialSolicitudAcademica sinOrigen = fila("APROBAR_COMPROBANTE");
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(RADICADA));
        when(historialGateway.getPorSolicitud(SOLICITUD)).thenReturn(List.of(fila("RADICAR"), desconocida, sinOrigen));

        List<HistorialSolicitudAcademica> historial = casoDeUso.getHistorial(SOLICITUD, token(ESTUDIANTE));

        assertEquals(RADICADA, historial.get(0).getEtapaCodigo());
        assertNull(desconocida.getEtapaCodigo());
        assertNull(sinOrigen.getEtapaCodigo());
    }

    @Test
    void descargarUnAnexoDeLaSolicitudDelegaConElActorResuelto() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(RADICADA));
        when(anexoGateway.getPorUuid("a1")).thenReturn(AnexoAcademico.builder().uuidAnexoAcademico("a1")
                .solicitudAcademica(SolicitudAcademica.builder().uuidSolicitudAcademica(SOLICITUD).build()).build());
        ArchivoAdjunto archivo = ArchivoAdjunto.builder().nombreOriginal("paz.pdf").build();
        when(anexoCU.obtenerAnexo(eq("a1"), any())).thenReturn(archivo);

        assertSame(archivo, casoDeUso.descargarAnexo(SOLICITUD, "a1", token(FUNCIONARIO)));

        ArgumentCaptor<ActorSolicitud> actor = ArgumentCaptor.forClass(ActorSolicitud.class);
        verify(anexoCU).obtenerAnexo(eq("a1"), actor.capture());
        assertEquals(FUNCIONARIO, actor.getValue().getUuidUsuario());
        assertEquals(RolEtiquetaEtapa.FUNCIONARIO, actor.getValue().getRol());
    }

    @Test
    void unAnexoDeOtraSolicitudOInexistenteNoSeDescarga() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(RADICADA));
        when(anexoGateway.getPorUuid("a2")).thenReturn(AnexoAcademico.builder().uuidAnexoAcademico("a2")
                .solicitudAcademica(SolicitudAcademica.builder().uuidSolicitudAcademica("otra").build()).build());
        when(anexoGateway.getPorUuid("a3")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.descargarAnexo(SOLICITUD, "a2", token(ESTUDIANTE)));
        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.descargarAnexo(SOLICITUD, "a3", token(ESTUDIANTE)));
        verifyNoInteractions(anexoCU);
    }

    @Test
    void descargarUnAnexoDeUnaSolicitudAjenaRespondeComoInexistente() {
        when(gateway.getPorUuid(SOLICITUD)).thenReturn(solicitud(RADICADA));

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.descargarAnexo(SOLICITUD, "a1", token(OTRO_ESTUDIANTE)));
        verifyNoInteractions(anexoGateway, anexoCU);
    }
}
