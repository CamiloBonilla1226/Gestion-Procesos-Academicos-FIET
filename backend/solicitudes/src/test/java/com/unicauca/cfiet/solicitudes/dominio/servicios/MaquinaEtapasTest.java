package com.unicauca.cfiet.solicitudes.dominio.servicios;

import com.unicauca.cfiet.solicitudes.dominio.modelos.AccionEtapa;
import com.unicauca.cfiet.solicitudes.dominio.modelos.EntregaTransicion;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ResponsableEtapa;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ResultadoTransicion;
import com.unicauca.cfiet.solicitudes.dominio.modelos.RolEtiquetaEtapa;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoProcesoAcademico;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorReglaNegocioVioladaExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.*;
import static com.unicauca.cfiet.solicitudes.dominio.modelos.AccionEtapa.*;
import static com.unicauca.cfiet.solicitudes.dominio.modelos.RolEtiquetaEtapa.DECANO;
import static com.unicauca.cfiet.solicitudes.dominio.modelos.RolEtiquetaEtapa.ESTUDIANTE;
import static com.unicauca.cfiet.solicitudes.dominio.modelos.RolEtiquetaEtapa.FUNCIONARIO;
import static com.unicauca.cfiet.solicitudes.dominio.modelos.TipoProcesoAcademico.*;
import static org.junit.jupiter.api.Assertions.*;

class MaquinaEtapasTest {

    private static final String NO_PERMITIDA = "no está permitida";
    private static final String ROL_EQUIVOCADO = "no puede ejecutar";
    private static final String ETAPA_FINAL = "etapa final";
    private static final String EXIGE = "exige";

    private MaquinaEtapas maquina;

    @BeforeEach
    void setUp() {
        maquina = new MaquinaEtapas(new ExcepcionesFormateadorImplAdaptador());
    }

    private record Fila(TipoProcesoAcademico tipo, String origen, AccionEtapa accion, RolEtiquetaEtapa rol, String destino,
                        boolean observacion, boolean resolucion, boolean recibo, boolean comprobante) {
        boolean tieneRequisitos() {
            return observacion || resolucion || recibo || comprobante;
        }
    }

    private static List<Fila> tablaCancelacion(TipoProcesoAcademico tipo) {
        return List.of(
                new Fila(tipo, null, RADICAR, ESTUDIANTE, RADICADA, false, false, false, false),
                new Fila(tipo, RADICADA, RECHAZAR_FUNCIONARIO, FUNCIONARIO, RECHAZADA, true, true, false, false),
                new Fila(tipo, RADICADA, REMITIR_DECANO, FUNCIONARIO, EN_REVISION_DECANO, false, false, false, false),
                new Fila(tipo, EN_REVISION_DECANO, APROBAR_DECANO, DECANO, APROBADA_POR_DECANO, false, false, false, false),
                new Fila(tipo, EN_REVISION_DECANO, RECHAZAR_DECANO, DECANO, RECHAZADA_POR_DECANO, true, false, false, false),
                new Fila(tipo, APROBADA_POR_DECANO, ENVIAR_RESPUESTA, FUNCIONARIO, APROBADA, false, true, false, false),
                new Fila(tipo, RECHAZADA_POR_DECANO, ENVIAR_RESPUESTA, FUNCIONARIO, RECHAZADA, false, true, false, false));
    }

    private static List<Fila> tablaSupletorio() {
        return List.of(
                new Fila(EXAMEN_SUPLETORIO, null, RADICAR, ESTUDIANTE, RADICADA, false, false, false, false),
                new Fila(EXAMEN_SUPLETORIO, RADICADA, RECHAZAR_FUNCIONARIO, FUNCIONARIO, RECHAZADA, true, false, false, false),
                new Fila(EXAMEN_SUPLETORIO, RADICADA, REMITIR_DECANO, FUNCIONARIO, EN_REVISION_DECANO, false, false, false, false),
                new Fila(EXAMEN_SUPLETORIO, EN_REVISION_DECANO, APROBAR_DECANO, DECANO, APROBADA_POR_DECANO, false, false, false, false),
                new Fila(EXAMEN_SUPLETORIO, EN_REVISION_DECANO, RECHAZAR_DECANO, DECANO, RECHAZADA_POR_DECANO, true, false, false, false),
                new Fila(EXAMEN_SUPLETORIO, RECHAZADA_POR_DECANO, ENVIAR_RESPUESTA, FUNCIONARIO, RECHAZADA, false, false, false, false),
                new Fila(EXAMEN_SUPLETORIO, APROBADA_POR_DECANO, ENVIAR_RECIBO, FUNCIONARIO, PENDIENTE_PAGO, false, false, true, false),
                new Fila(EXAMEN_SUPLETORIO, PENDIENTE_PAGO, SUBIR_COMPROBANTE, ESTUDIANTE, EN_VERIFICACION_PAGO, false, false, false, true),
                new Fila(EXAMEN_SUPLETORIO, EN_VERIFICACION_PAGO, APROBAR_COMPROBANTE, FUNCIONARIO, APROBADA, false, false, false, false),
                new Fila(EXAMEN_SUPLETORIO, EN_VERIFICACION_PAGO, RECHAZAR_COMPROBANTE, FUNCIONARIO, RECHAZADA, true, false, false, false));
    }

    private static List<Fila> tablaCompleta() {
        List<Fila> filas = new ArrayList<>(tablaCancelacion(CANCELACION_MATRICULA));
        filas.addAll(tablaCancelacion(CANCELACION_ASIGNATURA));
        filas.addAll(tablaSupletorio());
        return filas;
    }

    static Stream<Arguments> transicionesValidas() {
        return tablaCompleta().stream().map(f -> Arguments.of(f.tipo(), f.origen(), f.accion(), f.rol(), f));
    }

    static Stream<Arguments> transicionesConRolEquivocado() {
        return tablaCompleta().stream()
                .flatMap(f -> Stream.of(RolEtiquetaEtapa.values())
                        .filter(rol -> rol != f.rol())
                        .map(rol -> Arguments.of(f.tipo(), f.origen(), f.accion(), rol)));
    }

    static Stream<Arguments> transicionesConRequisitos() {
        return tablaCompleta().stream().filter(Fila::tieneRequisitos)
                .map(f -> Arguments.of(f.tipo(), f.origen(), f.accion(), f.rol(), f));
    }

    static Stream<Arguments> accionesEnEtapaFinal() {
        List<Arguments> casos = new ArrayList<>();
        for (TipoProcesoAcademico tipo : TipoProcesoAcademico.values())
            for (String etapa : List.of(APROBADA, RECHAZADA))
                for (AccionEtapa accion : AccionEtapa.values())
                    for (RolEtiquetaEtapa rol : RolEtiquetaEtapa.values())
                        casos.add(Arguments.of(tipo, etapa, accion, rol));
        return casos.stream();
    }

    static Stream<Arguments> accionesDePagoEnCancelaciones() {
        List<Arguments> casos = new ArrayList<>();
        for (TipoProcesoAcademico tipo : List.of(CANCELACION_MATRICULA, CANCELACION_ASIGNATURA)) {
            casos.add(Arguments.of(tipo, APROBADA_POR_DECANO, ENVIAR_RECIBO, FUNCIONARIO));
            casos.add(Arguments.of(tipo, PENDIENTE_PAGO, SUBIR_COMPROBANTE, ESTUDIANTE));
            casos.add(Arguments.of(tipo, EN_VERIFICACION_PAGO, APROBAR_COMPROBANTE, FUNCIONARIO));
            casos.add(Arguments.of(tipo, EN_VERIFICACION_PAGO, RECHAZAR_COMPROBANTE, FUNCIONARIO));
        }
        return casos.stream();
    }

    static Stream<Arguments> accionesFueraDeOrden() {
        return Stream.of(
                Arguments.of(CANCELACION_MATRICULA, RADICADA, RADICAR, ESTUDIANTE),
                Arguments.of(CANCELACION_MATRICULA, null, REMITIR_DECANO, FUNCIONARIO),
                Arguments.of(CANCELACION_MATRICULA, RADICADA, APROBAR_DECANO, DECANO),
                Arguments.of(CANCELACION_MATRICULA, RADICADA, ENVIAR_RESPUESTA, FUNCIONARIO),
                Arguments.of(CANCELACION_ASIGNATURA, EN_REVISION_DECANO, REMITIR_DECANO, FUNCIONARIO),
                Arguments.of(CANCELACION_ASIGNATURA, EN_REVISION_DECANO, ENVIAR_RESPUESTA, FUNCIONARIO),
                Arguments.of(CANCELACION_ASIGNATURA, APROBADA_POR_DECANO, APROBAR_DECANO, DECANO),
                Arguments.of(CANCELACION_ASIGNATURA, RECHAZADA_POR_DECANO, RECHAZAR_FUNCIONARIO, FUNCIONARIO),
                Arguments.of(EXAMEN_SUPLETORIO, APROBADA_POR_DECANO, ENVIAR_RESPUESTA, FUNCIONARIO),
                Arguments.of(EXAMEN_SUPLETORIO, RADICADA, ENVIAR_RECIBO, FUNCIONARIO),
                Arguments.of(EXAMEN_SUPLETORIO, APROBADA_POR_DECANO, SUBIR_COMPROBANTE, ESTUDIANTE),
                Arguments.of(EXAMEN_SUPLETORIO, PENDIENTE_PAGO, APROBAR_COMPROBANTE, FUNCIONARIO),
                Arguments.of(EXAMEN_SUPLETORIO, EN_VERIFICACION_PAGO, SUBIR_COMPROBANTE, ESTUDIANTE),
                Arguments.of(EXAMEN_SUPLETORIO, RECHAZADA_POR_DECANO, ENVIAR_RECIBO, FUNCIONARIO));
    }

    private void assertRechazo(String fragmento, Runnable accion) {
        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class, accion::run);
        assertTrue(error.getMessage().contains(fragmento), () -> "Mensaje inesperado: " + error.getMessage());
    }

    private EntregaTransicion entregaCompleta() {
        return EntregaTransicion.builder().observacion("Motivo").resolucion(true).recibo(true).comprobante(true).build();
    }

    @ParameterizedTest(name = "{0}: {1} --{2}/{3}--> valida")
    @MethodSource("transicionesValidas")
    void cadaTransicionDeLaTablaLlevaALaEtapaSiguienteConSusRequisitos(TipoProcesoAcademico tipo, String origen,
                                                                     AccionEtapa accion, RolEtiquetaEtapa rol, Fila esperada) {
        ResultadoTransicion resultado = maquina.siguienteEtapa(tipo, origen, accion, rol);

        assertEquals(esperada.destino(), resultado.getEtapaSiguiente());
        assertEquals(esperada.observacion(), resultado.isObservacionObligatoria());
        assertEquals(esperada.resolucion(), resultado.isResolucionObligatoria());
        assertEquals(esperada.recibo(), resultado.isReciboObligatorio());
        assertEquals(esperada.comprobante(), resultado.isComprobanteObligatorio());
    }

    @ParameterizedTest(name = "{0}: {1} --{2}--> con todos los requisitos es valida")
    @MethodSource("transicionesValidas")
    void cadaTransicionDeLaTablaPasaConLaEntregaCompleta(TipoProcesoAcademico tipo, String origen,
                                                        AccionEtapa accion, RolEtiquetaEtapa rol, Fila esperada) {
        assertEquals(esperada.destino(), maquina.transicionar(tipo, origen, accion, rol, entregaCompleta()).getEtapaSiguiente());
    }

    @Test
    void laTablaTieneExactamenteVeinticuatroTransiciones() {
        assertEquals(24, tablaCompleta().size());
    }

    @Test
    void ningunaCombinacionFueraDeLaTablaEsValida() {
        List<String> origenes = new ArrayList<>();
        origenes.add(null);
        origenes.addAll(ETAPAS);
        List<Fila> tabla = tablaCompleta();
        int validas = 0;
        List<String> sobrantes = new ArrayList<>();
        for (TipoProcesoAcademico tipo : TipoProcesoAcademico.values())
            for (String origen : origenes)
                for (AccionEtapa accion : AccionEtapa.values())
                    for (RolEtiquetaEtapa rol : RolEtiquetaEtapa.values()) {
                        boolean esperada = tabla.stream().anyMatch(f -> f.tipo() == tipo && Objects.equals(f.origen(), origen)
                                && f.accion() == accion && f.rol() == rol);
                        try {
                            maquina.siguienteEtapa(tipo, origen, accion, rol);
                            if (esperada) validas++;
                            else sobrantes.add(tipo + " " + origen + " " + accion + " " + rol);
                        } catch (ErrorReglaNegocioVioladaExcepcion error) {
                            if (esperada) fail("Transicion de la tabla rechazada: " + tipo + " " + origen + " " + accion + " " + rol);
                        }
                    }
        assertTrue(sobrantes.isEmpty(), () -> "Transiciones que no estan en la tabla: " + sobrantes);
        assertEquals(24, validas);
    }

    @ParameterizedTest(name = "{0}: {1} --{2}--> por {3} se rechaza")
    @MethodSource("transicionesConRolEquivocado")
    void unaTransicionConRolEquivocadoSeRechaza(TipoProcesoAcademico tipo, String origen, AccionEtapa accion, RolEtiquetaEtapa rol) {
        assertRechazo(ROL_EQUIVOCADO, () -> maquina.siguienteEtapa(tipo, origen, accion, rol));
    }

    @ParameterizedTest(name = "{0}: {2} desde {1} por {3}")
    @MethodSource("accionesEnEtapaFinal")
    void lasEtapasFinalesNoTienenSalida(TipoProcesoAcademico tipo, String etapa, AccionEtapa accion, RolEtiquetaEtapa rol) {
        assertRechazo(ETAPA_FINAL, () -> maquina.siguienteEtapa(tipo, etapa, accion, rol));
    }

    @ParameterizedTest(name = "{0}: {2} desde {1}")
    @MethodSource("accionesDePagoEnCancelaciones")
    void lasAccionesDePagoNoExistenEnCancelaciones(TipoProcesoAcademico tipo, String etapa, AccionEtapa accion, RolEtiquetaEtapa rol) {
        assertRechazo(NO_PERMITIDA, () -> maquina.siguienteEtapa(tipo, etapa, accion, rol));
    }

    @ParameterizedTest(name = "{0}: {2} desde {1}")
    @MethodSource("accionesFueraDeOrden")
    void unaAccionFueraDeOrdenSeRechaza(TipoProcesoAcademico tipo, String etapa, AccionEtapa accion, RolEtiquetaEtapa rol) {
        assertRechazo(NO_PERMITIDA, () -> maquina.siguienteEtapa(tipo, etapa, accion, rol));
    }

    @ParameterizedTest(name = "{0}: {2} desde {1} sin entregar nada")
    @MethodSource("transicionesConRequisitos")
    void unaTransicionConRequisitosSinEntregaSeRechaza(TipoProcesoAcademico tipo, String origen,
                                                       AccionEtapa accion, RolEtiquetaEtapa rol, Fila esperada) {
        assertRechazo(EXIGE, () -> maquina.transicionar(tipo, origen, accion, rol, null));
        assertRechazo(EXIGE, () -> maquina.transicionar(tipo, origen, accion, rol, new EntregaTransicion()));
    }

    @ParameterizedTest
    @EnumSource(value = TipoProcesoAcademico.class, names = {"CANCELACION_MATRICULA", "CANCELACION_ASIGNATURA"})
    void rechazarFuncionarioEnCancelacionesExigeObservacionYEscaneo(TipoProcesoAcademico tipo) {
        EntregaTransicion soloObservacion = EntregaTransicion.builder().observacion("No cumple").build();
        EntregaTransicion soloEscaneo = EntregaTransicion.builder().resolucion(true).build();
        EntregaTransicion ambas = EntregaTransicion.builder().observacion("No cumple").resolucion(true).build();

        assertRechazo("Resolución", () -> maquina.transicionar(tipo, RADICADA, RECHAZAR_FUNCIONARIO, FUNCIONARIO, soloObservacion));
        assertRechazo("observación", () -> maquina.transicionar(tipo, RADICADA, RECHAZAR_FUNCIONARIO, FUNCIONARIO, soloEscaneo));
        assertEquals(RECHAZADA, maquina.transicionar(tipo, RADICADA, RECHAZAR_FUNCIONARIO, FUNCIONARIO, ambas).getEtapaSiguiente());
    }

    @Test
    void rechazarFuncionarioEnCancelacionesSinNadaNombraAmbosRequisitos() {
        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> maquina.transicionar(CANCELACION_MATRICULA, RADICADA, RECHAZAR_FUNCIONARIO, FUNCIONARIO, null));
        assertTrue(error.getMessage().contains("observación"));
        assertTrue(error.getMessage().contains("Resolución"));
    }

    @Test
    void rechazarFuncionarioEnSupletorioSoloExigeObservacion() {
        ResultadoTransicion resultado = maquina.transicionar(EXAMEN_SUPLETORIO, RADICADA, RECHAZAR_FUNCIONARIO, FUNCIONARIO,
                EntregaTransicion.builder().observacion("No cumple").build());

        assertEquals(RECHAZADA, resultado.getEtapaSiguiente());
        assertFalse(resultado.isResolucionObligatoria());
        assertRechazo("observación", () -> maquina.transicionar(EXAMEN_SUPLETORIO, RADICADA, RECHAZAR_FUNCIONARIO, FUNCIONARIO,
                EntregaTransicion.builder().resolucion(true).build()));
    }

    @Test
    void unaObservacionEnBlancoCuentaComoFaltante() {
        assertRechazo("observación", () -> maquina.transicionar(EXAMEN_SUPLETORIO, EN_REVISION_DECANO, RECHAZAR_DECANO, DECANO,
                EntregaTransicion.builder().observacion("   ").build()));
    }

    @ParameterizedTest
    @EnumSource(value = TipoProcesoAcademico.class, names = {"CANCELACION_MATRICULA", "CANCELACION_ASIGNATURA"})
    void enviarRespuestaEnCancelacionesExigeEscaneoDesdeAmbasDecisiones(TipoProcesoAcademico tipo) {
        for (String origen : List.of(APROBADA_POR_DECANO, RECHAZADA_POR_DECANO))
            assertRechazo("Resolución", () -> maquina.transicionar(tipo, origen, ENVIAR_RESPUESTA, FUNCIONARIO,
                    EntregaTransicion.builder().observacion("Respuesta").build()));
        EntregaTransicion conEscaneo = EntregaTransicion.builder().resolucion(true).build();
        assertEquals(APROBADA, maquina.transicionar(tipo, APROBADA_POR_DECANO, ENVIAR_RESPUESTA, FUNCIONARIO, conEscaneo).getEtapaSiguiente());
        assertEquals(RECHAZADA, maquina.transicionar(tipo, RECHAZADA_POR_DECANO, ENVIAR_RESPUESTA, FUNCIONARIO, conEscaneo).getEtapaSiguiente());
    }

    @Test
    void enviarRespuestaTrasRechazoDelDecanoEnSupletorioNoExigeNada() {
        ResultadoTransicion resultado = maquina.transicionar(EXAMEN_SUPLETORIO, RECHAZADA_POR_DECANO, ENVIAR_RESPUESTA, FUNCIONARIO, null);

        assertEquals(RECHAZADA, resultado.getEtapaSiguiente());
        assertFalse(resultado.isObservacionObligatoria() || resultado.isResolucionObligatoria()
                || resultado.isReciboObligatorio() || resultado.isComprobanteObligatorio());
    }

    @Test
    void enviarReciboExigeElRecibo() {
        assertRechazo("recibo", () -> maquina.transicionar(EXAMEN_SUPLETORIO, APROBADA_POR_DECANO, ENVIAR_RECIBO, FUNCIONARIO,
                EntregaTransicion.builder().comprobante(true).build()));
        assertEquals(PENDIENTE_PAGO, maquina.transicionar(EXAMEN_SUPLETORIO, APROBADA_POR_DECANO, ENVIAR_RECIBO, FUNCIONARIO,
                EntregaTransicion.builder().recibo(true).build()).getEtapaSiguiente());
    }

    @Test
    void subirComprobanteExigeElComprobante() {
        assertRechazo("comprobante", () -> maquina.transicionar(EXAMEN_SUPLETORIO, PENDIENTE_PAGO, SUBIR_COMPROBANTE, ESTUDIANTE,
                EntregaTransicion.builder().recibo(true).build()));
        assertEquals(EN_VERIFICACION_PAGO, maquina.transicionar(EXAMEN_SUPLETORIO, PENDIENTE_PAGO, SUBIR_COMPROBANTE, ESTUDIANTE,
                EntregaTransicion.builder().comprobante(true).build()).getEtapaSiguiente());
    }

    @Test
    void rechazarComprobanteExigeObservacionYAprobarloNoExigeNada() {
        assertRechazo("observación", () -> maquina.transicionar(EXAMEN_SUPLETORIO, EN_VERIFICACION_PAGO, RECHAZAR_COMPROBANTE, FUNCIONARIO, null));
        assertEquals(APROBADA, maquina.transicionar(EXAMEN_SUPLETORIO, EN_VERIFICACION_PAGO, APROBAR_COMPROBANTE, FUNCIONARIO, null).getEtapaSiguiente());
    }

    @Test
    void rechazarDecanoExigeObservacionYAprobarDecanoNoExigeNada() {
        assertRechazo("observación", () -> maquina.transicionar(CANCELACION_ASIGNATURA, EN_REVISION_DECANO, RECHAZAR_DECANO, DECANO, null));
        assertEquals(APROBADA_POR_DECANO, maquina.transicionar(CANCELACION_ASIGNATURA, EN_REVISION_DECANO, APROBAR_DECANO, DECANO, null).getEtapaSiguiente());
    }

    @Test
    void radicarConEtapaEnBlancoSeTrataComoSolicitudNueva() {
        assertEquals(RADICADA, maquina.siguienteEtapa(CANCELACION_MATRICULA, "  ", RADICAR, ESTUDIANTE).getEtapaSiguiente());
    }

    @Test
    void unaEtapaDesconocidaSeRechaza() {
        assertRechazo("no es valida", () -> maquina.siguienteEtapa(EXAMEN_SUPLETORIO, "ARCHIVADA", APROBAR_DECANO, DECANO));
        assertRechazo("no es valida", () -> maquina.responsableActual("ARCHIVADA"));
        assertRechazo("no es valida", () -> maquina.esFinal(null));
    }

    @Test
    void sinTipoAccionORolSeRechaza() {
        assertRechazo("se requieren", () -> maquina.siguienteEtapa(null, null, RADICAR, ESTUDIANTE));
        assertRechazo("se requieren", () -> maquina.siguienteEtapa(CANCELACION_MATRICULA, null, null, ESTUDIANTE));
        assertRechazo("se requieren", () -> maquina.siguienteEtapa(CANCELACION_MATRICULA, null, RADICAR, null));
    }

    @ParameterizedTest(name = "{0} la atiende {1}")
    @CsvSource({
            "RADICADA, FUNCIONARIO",
            "EN_REVISION_DECANO, DECANO",
            "APROBADA_POR_DECANO, FUNCIONARIO",
            "RECHAZADA_POR_DECANO, FUNCIONARIO",
            "PENDIENTE_PAGO, ESTUDIANTE",
            "EN_VERIFICACION_PAGO, FUNCIONARIO",
            "APROBADA, NINGUNO",
            "RECHAZADA, NINGUNO"
    })
    void responsableActualDeCadaEtapa(String etapa, ResponsableEtapa responsable) {
        assertEquals(responsable, maquina.responsableActual(etapa));
    }

    @ParameterizedTest(name = "{0} final: {1}")
    @CsvSource({
            "RADICADA, false",
            "EN_REVISION_DECANO, false",
            "APROBADA_POR_DECANO, false",
            "RECHAZADA_POR_DECANO, false",
            "PENDIENTE_PAGO, false",
            "EN_VERIFICACION_PAGO, false",
            "APROBADA, true",
            "RECHAZADA, true"
    })
    void esFinalDeCadaEtapa(String etapa, boolean esFinal) {
        assertEquals(esFinal, maquina.esFinal(etapa));
    }

    @Test
    void accionesDisponiblesCoincideConElBarridoDeTodasLasCombinaciones() {
        List<String> origenes = new ArrayList<>();
        origenes.add(null);
        origenes.addAll(ETAPAS);
        List<Fila> tabla = tablaCompleta();
        int ofrecidas = 0;
        for (TipoProcesoAcademico tipo : TipoProcesoAcademico.values())
            for (String origen : origenes)
                for (RolEtiquetaEtapa rol : RolEtiquetaEtapa.values()) {
                    List<AccionEtapa> disponibles = maquina.accionesDisponibles(tipo, origen, rol);
                    List<AccionEtapa> esperadas = tabla.stream()
                            .filter(f -> f.tipo() == tipo && Objects.equals(f.origen(), origen) && f.rol() == rol)
                            .map(Fila::accion)
                            .toList();
                    assertEquals(esperadas.size(), disponibles.size(), () -> tipo + " " + origen + " " + rol);
                    assertTrue(disponibles.containsAll(esperadas), () -> tipo + " " + origen + " " + rol + ": " + disponibles);
                    for (AccionEtapa accion : AccionEtapa.values()) {
                        boolean aceptada;
                        try {
                            maquina.siguienteEtapa(tipo, origen, accion, rol);
                            aceptada = true;
                        } catch (ErrorReglaNegocioVioladaExcepcion error) {
                            aceptada = false;
                        }
                        assertEquals(aceptada, disponibles.contains(accion), () -> tipo + " " + origen + " " + accion + " " + rol);
                    }
                    ofrecidas += disponibles.size();
                }
        assertEquals(24, ofrecidas);
    }

    @ParameterizedTest(name = "{0} en {1} no ofrece acciones")
    @MethodSource("etapasFinalesPorTipo")
    void unaEtapaFinalNoOfreceAcciones(TipoProcesoAcademico tipo, String etapa) {
        for (RolEtiquetaEtapa rol : RolEtiquetaEtapa.values())
            assertTrue(maquina.accionesDisponibles(tipo, etapa, rol).isEmpty());
    }

    static Stream<Arguments> etapasFinalesPorTipo() {
        return Stream.of(TipoProcesoAcademico.values())
                .flatMap(tipo -> Stream.of(Arguments.of(tipo, APROBADA), Arguments.of(tipo, RECHAZADA)));
    }

    @Test
    void accionesDisponiblesDeEjemplo() {
        assertEquals(List.of(RECHAZAR_FUNCIONARIO, REMITIR_DECANO), maquina.accionesDisponibles(CANCELACION_MATRICULA, RADICADA, FUNCIONARIO));
        assertEquals(List.of(APROBAR_DECANO, RECHAZAR_DECANO), maquina.accionesDisponibles(EXAMEN_SUPLETORIO, EN_REVISION_DECANO, DECANO));
        assertEquals(List.of(ENVIAR_RECIBO), maquina.accionesDisponibles(EXAMEN_SUPLETORIO, APROBADA_POR_DECANO, FUNCIONARIO));
        assertEquals(List.of(ENVIAR_RESPUESTA), maquina.accionesDisponibles(CANCELACION_ASIGNATURA, APROBADA_POR_DECANO, FUNCIONARIO));
        assertEquals(List.of(RADICAR), maquina.accionesDisponibles(CANCELACION_ASIGNATURA, "  ", ESTUDIANTE));
        assertTrue(maquina.accionesDisponibles(CANCELACION_MATRICULA, RADICADA, ESTUDIANTE).isEmpty());
        assertTrue(maquina.accionesDisponibles(CANCELACION_MATRICULA, PENDIENTE_PAGO, ESTUDIANTE).isEmpty());
    }

    @Test
    void accionesDisponiblesSinTipoORolOConEtapaDesconocidaSeRechaza() {
        assertRechazo("se requieren", () -> maquina.accionesDisponibles(null, RADICADA, FUNCIONARIO));
        assertRechazo("se requieren", () -> maquina.accionesDisponibles(CANCELACION_MATRICULA, RADICADA, null));
        assertRechazo("no es valida", () -> maquina.accionesDisponibles(CANCELACION_MATRICULA, "ARCHIVADA", FUNCIONARIO));
    }

    @Test
    void elActorDeCadaTransicionEsElResponsableDeSuEtapaDeOrigen() {
        for (Fila fila : tablaCompleta())
            if (fila.origen() != null)
                assertEquals(fila.rol().name(), maquina.responsableActual(fila.origen()).name(),
                        () -> "Origen " + fila.origen() + " accion " + fila.accion());
    }

    @Test
    void laEtapaDestinoCoincideConLaTablaCompleta() {
        for (Fila fila : tablaCompleta())
            assertEquals(fila.destino(), maquina.etapaDestino(fila.tipo(), fila.origen(), fila.accion()),
                    () -> fila.tipo() + " " + fila.origen() + " " + fila.accion());
    }

    @Test
    void laEtapaDestinoDeUnaTransicionInexistenteEsNulaSinLanzar() {
        assertNull(maquina.etapaDestino(CANCELACION_MATRICULA, APROBADA_POR_DECANO, ENVIAR_RECIBO));
        assertNull(maquina.etapaDestino(EXAMEN_SUPLETORIO, RADICADA, RADICAR));
        assertNull(maquina.etapaDestino(EXAMEN_SUPLETORIO, RADICADA, null));
        assertNull(maquina.etapaDestino(null, null, RADICAR));
        assertEquals(RADICADA, maquina.etapaDestino(EXAMEN_SUPLETORIO, "  ", RADICAR));
    }
}
