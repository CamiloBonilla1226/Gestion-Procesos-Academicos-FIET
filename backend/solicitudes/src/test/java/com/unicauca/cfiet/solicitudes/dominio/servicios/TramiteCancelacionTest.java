package com.unicauca.cfiet.solicitudes.dominio.servicios;

import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorEntidadNoExisteExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorMalFormatoExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorReglaNegocioVioladaExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TramiteCancelacionTest {

    private static final String RADICADO = "2026-CM-0001";

    @Mock
    private SolicitudCancelacionMatriculaGatewayIntPuerto asignaturaGateway;

    @Mock
    private SolicitudAcademicaGatewayIntPuerto solicitudGateway;

    @Mock
    private SituacionAcademicaAsignaturaGatewayIntPuerto situacionGateway;

    @Mock
    private UsuarioGatewayIntPuerto usuarioGateway;

    @Mock
    private SesionGatewayIntPuerto sesionGateway;

    @Mock
    private IJwtServicio jwtServicio;

    private TramiteCancelacion tramite;

    private final Function<EvaluacionAsignatura, String> llave = EvaluacionAsignatura::getUuidAsignaturaSolicitud;

    @BeforeEach
    void setUp() {
        ExcepcionesFormateadorImplAdaptador formateador = new ExcepcionesFormateadorImplAdaptador();
        tramite = new TramiteCancelacion(asignaturaGateway, solicitudGateway, situacionGateway, usuarioGateway, sesionGateway,
                jwtServicio, new MaquinaEtapas(formateador), formateador);
    }

    private AsignaturaSolicitudAcademica fila(String uuid, String uuidMatriculada, String nombre, String estado) {
        return AsignaturaSolicitudAcademica.builder()
                .uuidAsignaturaSolicitud(uuid)
                .asignaturaMatriculada(AsignaturaMatriculada.builder()
                        .uuidAsignaturaMatriculada(uuidMatriculada)
                        .asignatura(Asignatura.builder().uuidAsignatura("a-" + uuidMatriculada).nombreAsignatura(nombre).build())
                        .grupo("A")
                        .estado(estado)
                        .build())
                .build();
    }

    private List<AsignaturaSolicitudAcademica> filas() {
        return List.of(fila("as-1", "am-1", "Cálculo I", "activa"), fila("as-2", "am-2", "Física I", "activa"));
    }

    private EvaluacionAsignatura evaluacion(String uuid) {
        return EvaluacionAsignatura.builder().uuidAsignaturaSolicitud(uuid).numeroFaltas(0).nota(new BigDecimal("4.0")).build();
    }

    private SituacionAcademicaAsignatura situacion(String uuid) {
        return SituacionAcademicaAsignatura.builder().uuidSituacionAcademica(uuid).codigo(uuid.toUpperCase()).nombre("Situación " + uuid).build();
    }

    @Test
    void coberturaDevuelveCadaRecibidaPorSuAsignatura() {
        EvaluacionAsignatura primera = evaluacion("as-1");
        EvaluacionAsignatura segunda = evaluacion("as-2");

        Map<String, EvaluacionAsignatura> porAsignatura = tramite.cobertura(filas(), RADICADO, List.of(segunda, primera), llave);

        assertEquals(2, porAsignatura.size());
        assertSame(primera, porAsignatura.get("as-1"));
        assertSame(segunda, porAsignatura.get("as-2"));
    }

    @Test
    void coberturaNombraLaAsignaturaQueFalta() {
        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> tramite.cobertura(filas(), RADICADO, List.of(evaluacion("as-1")), llave));

        assertEquals(String.format(MensajesError.ASIGNATURAS_SIN_EVALUAR, "Física I"), error.getMessage());
    }

    @Test
    void coberturaNombraTodasLasAsignaturasQueFaltanEnOrden() {
        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> tramite.cobertura(filas(), RADICADO, List.of(), llave));

        assertEquals(String.format(MensajesError.ASIGNATURAS_SIN_EVALUAR, "Cálculo I; Física I"), error.getMessage());
    }

    @Test
    void coberturaTrataUnaListaNulaComoVacia() {
        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> tramite.cobertura(filas(), RADICADO, null, llave));

        assertEquals(String.format(MensajesError.ASIGNATURAS_SIN_EVALUAR, "Cálculo I; Física I"), error.getMessage());
    }

    @Test
    void coberturaRechazaUnaAsignaturaRepetida() {
        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> tramite.cobertura(filas(), RADICADO, List.of(evaluacion("as-1"), evaluacion("as-2"), evaluacion("as-1")), llave));

        assertEquals(String.format(MensajesError.ASIGNATURA_REPETIDA, "Cálculo I"), error.getMessage());
    }

    @Test
    void coberturaRechazaUnaAsignaturaAjenaALaSolicitud() {
        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> tramite.cobertura(filas(), RADICADO, List.of(evaluacion("as-1"), evaluacion("as-2"), evaluacion("as-9")), llave));

        assertEquals(String.format(MensajesError.ASIGNATURA_AJENA, "as-9", RADICADO), error.getMessage());
    }

    @Test
    void coberturaRechazaUnaRecibidaNulaOSinUuidComoAjena() {
        List<EvaluacionAsignatura> conNula = Arrays.asList(evaluacion("as-1"), null);
        List<EvaluacionAsignatura> sinUuid = List.of(evaluacion("as-1"), evaluacion(null));

        ErrorReglaNegocioVioladaExcepcion nula = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> tramite.cobertura(filas(), RADICADO, conNula, llave));
        ErrorReglaNegocioVioladaExcepcion vacia = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> tramite.cobertura(filas(), RADICADO, sinUuid, llave));

        assertEquals(String.format(MensajesError.ASIGNATURA_AJENA, null, RADICADO), nula.getMessage());
        assertEquals(String.format(MensajesError.ASIGNATURA_AJENA, null, RADICADO), vacia.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 15, 200})
    void faltasDeAceptaCeroOMas(int faltas) {
        assertEquals(faltas, tramite.faltasDe(faltas, "Cálculo I"));
    }

    @Test
    void faltasDeRechazaUnNumeroNegativo() {
        ErrorMalFormatoExcepcion error = assertThrows(ErrorMalFormatoExcepcion.class, () -> tramite.faltasDe(-1, "Cálculo I"));

        assertEquals(String.format(MensajesError.NUMERO_FALTAS_NO_VALIDO, "Cálculo I"), error.getMessage());
    }

    @Test
    void faltasDeExigeElDato() {
        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> tramite.faltasDe(null, "Cálculo I"));

        assertEquals(String.format(MensajesError.DATO_ASIGNATURA_FALTANTE, "el número de faltas", "Cálculo I"), error.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.0", "3", "3.5", "4.50", "5", "5.0", "5.00"})
    void notaDeAceptaDeCeroACincoConUnDecimalYLaDejaConUnDecimal(String nota) {
        BigDecimal resultado = tramite.notaDe(new BigDecimal(nota), "Física I");

        assertEquals(1, resultado.scale());
        assertEquals(0, resultado.compareTo(new BigDecimal(nota)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.1", "-1", "5.1", "6", "10", "3.25", "4.99", "0.05"})
    void notaDeRechazaValoresFueraDeRangoOConMasDeUnDecimal(String nota) {
        ErrorMalFormatoExcepcion error = assertThrows(ErrorMalFormatoExcepcion.class,
                () -> tramite.notaDe(new BigDecimal(nota), "Física I"));

        assertEquals(String.format(MensajesError.NOTA_NO_VALIDA, "Física I"), error.getMessage());
    }

    @Test
    void notaDeExigeElDato() {
        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> tramite.notaDe(null, "Física I"));

        assertEquals(String.format(MensajesError.DATO_ASIGNATURA_FALTANTE, "la nota", "Física I"), error.getMessage());
    }

    @Test
    void catalogoSituacionesIndexaElCatalogoPorUuid() {
        when(situacionGateway.getTodas()).thenReturn(List.of(situacion("sit-a"), situacion("sit-b")));

        Map<String, SituacionAcademicaAsignatura> catalogo = tramite.catalogoSituaciones();

        assertEquals(2, catalogo.size());
        assertEquals("SIT-A", catalogo.get("sit-a").getCodigo());
        assertEquals("SIT-B", catalogo.get("sit-b").getCodigo());
    }

    @Test
    void situacionDeDevuelveLaSituacionDelCatalogo() {
        when(situacionGateway.getTodas()).thenReturn(List.of(situacion("sit-a"), situacion("sit-b")));

        SituacionAcademicaAsignatura encontrada = tramite.situacionDe(tramite.catalogoSituaciones(), "sit-b",
                "la situación en la matrícula", "Cálculo I");

        assertEquals("sit-b", encontrada.getUuidSituacionAcademica());
    }

    @Test
    void situacionDeRechazaUnaSituacionQueNoEstaEnElCatalogo() {
        when(situacionGateway.getTodas()).thenReturn(List.of(situacion("sit-a")));
        Map<String, SituacionAcademicaAsignatura> catalogo = tramite.catalogoSituaciones();

        ErrorEntidadNoExisteExcepcion error = assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> tramite.situacionDe(catalogo, "sit-x", "la situación en la matrícula", "Cálculo I"));

        assertEquals(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, "Situación académica de asignatura", "sit-x"), error.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void situacionDeExigeElDato(String uuid) {
        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> tramite.situacionDe(Map.of(), uuid, "la situación al cancelar", "Física I"));

        assertEquals(String.format(MensajesError.DATO_ASIGNATURA_FALTANTE, "la situación al cancelar", "Física I"), error.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void guardarAsignaturasGuardaLasFilasRecibidasYDevuelveLasGuardadas() {
        List<AsignaturaSolicitudAcademica> recibidas = filas();
        List<AsignaturaSolicitudAcademica> guardadas = List.of(fila("as-1", "am-1", "Cálculo I", "activa"));
        when(asignaturaGateway.actualizarAsignaturas(any())).thenReturn(guardadas);

        assertSame(guardadas, tramite.guardarAsignaturas(recibidas));

        ArgumentCaptor<List<AsignaturaSolicitudAcademica>> captor = ArgumentCaptor.forClass(List.class);
        verify(asignaturaGateway).actualizarAsignaturas(captor.capture());
        assertEquals(List.of("as-1", "as-2"), captor.getValue().stream().map(AsignaturaSolicitudAcademica::getUuidAsignaturaSolicitud).toList());
    }

    @Test
    @SuppressWarnings("unchecked")
    void cancelarActivasCancelaSoloLasMatriculadasQueSiguenActivas() {
        List<AsignaturaSolicitudAcademica> asignaturas = List.of(
                fila("as-1", "am-1", "Cálculo I", "activa"),
                fila("as-2", "am-2", "Física I", "cancelada"),
                fila("as-3", "am-3", "Química", "aprobada"),
                fila("as-4", "am-4", "Dibujo", "perdida"),
                fila("as-5", "am-5", "Inglés", "activa"));

        int canceladas = tramite.cancelarActivas(asignaturas);

        assertEquals(2, canceladas);
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        verify(asignaturaGateway).cambiarEstadoAsignaturasMatriculadas(captor.capture(), eq("cancelada"));
        assertEquals(List.of("am-1", "am-5"), captor.getValue());
        assertEquals(List.of("cancelada", "cancelada", "aprobada", "perdida", "cancelada"),
                asignaturas.stream().map(a -> a.getAsignaturaMatriculada().getEstado()).toList());
    }

    @Test
    void cancelarActivasNoEscribeNadaSiNingunaSigueActiva() {
        List<AsignaturaSolicitudAcademica> asignaturas = List.of(
                fila("as-1", "am-1", "Cálculo I", "cancelada"),
                fila("as-2", "am-2", "Física I", "aprobada"));

        assertEquals(0, tramite.cancelarActivas(asignaturas));

        verify(asignaturaGateway, never()).cambiarEstadoAsignaturasMatriculadas(any(), any());
        assertEquals(List.of("cancelada", "aprobada"), asignaturas.stream().map(a -> a.getAsignaturaMatriculada().getEstado()).toList());
    }

    @Test
    void cancelarActivasConListaVaciaNoEscribeNada() {
        assertEquals(0, tramite.cancelarActivas(new ArrayList<>()));

        verifyNoInteractions(asignaturaGateway);
    }

    @Test
    void nombreDeUsaElNombreDeLaAsignaturaOElUuidSiNoLoHay() {
        assertEquals("Cálculo I", tramite.nombreDe(fila("as-1", "am-1", "Cálculo I", "activa")));
        assertEquals("as-7", tramite.nombreDe(AsignaturaSolicitudAcademica.builder().uuidAsignaturaSolicitud("as-7").build()));
        assertEquals("as-8", tramite.nombreDe(AsignaturaSolicitudAcademica.builder().uuidAsignaturaSolicitud("as-8")
                .asignaturaMatriculada(AsignaturaMatriculada.builder().uuidAsignaturaMatriculada("am-8").build()).build()));
    }

    @Test
    void textoAcotadoRecortaEspaciosYDevuelveNuloSiNoHayTexto() {
        assertEquals("No cumple", tramite.textoAcotado("  No cumple  ", 255, "La observación", "Cálculo I"));
        assertNull(tramite.textoAcotado("   ", 255, "La observación", "Cálculo I"));
        assertNull(tramite.textoAcotado(null, 255, "La observación", "Cálculo I"));
        assertEquals("x".repeat(10), tramite.textoAcotado(" " + "x".repeat(10) + " ", 10, "La observación", "Cálculo I"));
    }

    @Test
    void textoAcotadoRechazaUnTextoMasLargoQueElMaximo() {
        ErrorMalFormatoExcepcion error = assertThrows(ErrorMalFormatoExcepcion.class,
                () -> tramite.textoAcotado("x".repeat(11), 10, "La observación", "Cálculo I"));

        assertEquals(String.format(MensajesError.OBSERVACION_ASIGNATURA_MUY_LARGA, "La observación", "Cálculo I", 10), error.getMessage());
    }
}
