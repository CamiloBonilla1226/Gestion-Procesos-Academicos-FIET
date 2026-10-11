package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.AsignaturaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ProgramaAcademicoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Asignatura;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ProgramaAcademico;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorEntidadExisteExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorEntidadNoExisteExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorMalFormatoExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorNoInformacionExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsignaturaCUImplAdaptadorTest {

    private static final String TOKEN = "token";
    private static final String UUID_ISI = "uuid-isi";
    private static final String UUID_IET = "uuid-iet";

    @Mock
    private AsignaturaGatewayIntPuerto gateway;

    @Mock
    private ProgramaAcademicoGatewayIntPuerto programaGateway;

    @Mock
    private LogCUIntPuerto log;

    private AsignaturaCUImplAdaptador casoDeUso;

    @BeforeEach
    void setUp() {
        casoDeUso = new AsignaturaCUImplAdaptador(gateway, programaGateway, new ExcepcionesFormateadorImplAdaptador(), log);
    }

    private ProgramaAcademico programa(String uuid, String sigla, String nombre, int ultimo) {
        return ProgramaAcademico.builder()
                .uuidProgramaAcademico(uuid)
                .sigla(sigla)
                .nombre(nombre)
                .ultimoConsecutivoAsignatura(ultimo)
                .build();
    }

    private ProgramaAcademico sistemas(int ultimo) {
        return programa(UUID_ISI, "ISI", "Ingeniería de Sistemas", ultimo);
    }

    private ProgramaAcademico electronica(int ultimo) {
        return programa(UUID_IET, "IET", "Ingeniería Electrónica y Telecomunicaciones", ultimo);
    }

    private Asignatura nueva(String nombre, String uuidPrograma) {
        return Asignatura.builder()
                .nombreAsignatura(nombre)
                .programaAcademico(uuidPrograma == null ? null : ProgramaAcademico.builder().uuidProgramaAcademico(uuidPrograma).build())
                .build();
    }

    private Asignatura existente(String uuid, String codigo, String nombre, ProgramaAcademico programa) {
        return Asignatura.builder()
                .uuidAsignatura(uuid)
                .codigoAsignatura(codigo)
                .nombreAsignatura(nombre)
                .programaAcademico(programa)
                .build();
    }

    private void prepararPrograma(ProgramaAcademico programa, Asignatura... existentes) {
        lenient().when(programaGateway.getPorUuid(programa.getUuidProgramaAcademico())).thenReturn(programa);
        lenient().when(programaGateway.bloquear(programa.getUuidProgramaAcademico())).thenReturn(programa);
        lenient().when(gateway.getPorPrograma(programa.getUuidProgramaAcademico())).thenReturn(List.of(existentes));
    }

    private void verificarQueNoSeCreoNada() {
        verify(gateway, never()).guardar(any());
        verify(programaGateway, never()).guardarUltimoConsecutivo(anyString(), anyInt());
        verifyNoInteractions(log);
    }

    @Test
    void crearAsignaturaGeneraElCodigoConLaSiglaYElSiguienteConsecutivo() {
        prepararPrograma(sistemas(0));
        when(gateway.guardar(any(Asignatura.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Asignatura creada = casoDeUso.crearAsignatura(nueva("Calculo I", UUID_ISI), TOKEN);

        assertEquals("ISI-001", creada.getCodigoAsignatura());
        assertEquals(UUID_ISI, creada.getProgramaAcademico().getUuidProgramaAcademico());
        assertDoesNotThrow(() -> UUID.fromString(creada.getUuidAsignatura()));
        verify(programaGateway).guardarUltimoConsecutivo(UUID_ISI, 1);
        verify(log).crearLog(eq("Crear asignatura"), anyString(), eq(TOKEN));
    }

    @ParameterizedTest
    @CsvSource({"6, ISI-007", "98, ISI-099", "998, ISI-999", "999, ISI-1000", "1234, ISI-1235"})
    void elCodigoTieneTresDigitosMinimoYCuatroSiPasaDe999(int ultimo, String esperado) {
        prepararPrograma(sistemas(ultimo));
        when(gateway.guardar(any(Asignatura.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        assertEquals(esperado, casoDeUso.crearAsignatura(nueva("Calculo I", UUID_ISI), TOKEN).getCodigoAsignatura());
        verify(programaGateway).guardarUltimoConsecutivo(UUID_ISI, ultimo + 1);
    }

    @Test
    void elConsecutivoNoSeReutilizaAunqueYaNoQuedenAsignaturasDelPrograma() {
        prepararPrograma(sistemas(5));
        when(gateway.guardar(any(Asignatura.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        assertEquals("ISI-006", casoDeUso.crearAsignatura(nueva("Fisica I", UUID_ISI), TOKEN).getCodigoAsignatura());
    }

    @Test
    void elContadorEnNuloSeTomaComoCero() {
        ProgramaAcademico programa = sistemas(0);
        programa.setUltimoConsecutivoAsignatura(null);
        prepararPrograma(programa);
        when(gateway.guardar(any(Asignatura.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        assertEquals("ISI-001", casoDeUso.crearAsignatura(nueva("Fisica I", UUID_ISI), TOKEN).getCodigoAsignatura());
    }

    @Test
    void crearAsignaturaGuardaElNombreSinEspaciosSobrantes() {
        prepararPrograma(sistemas(0));
        when(gateway.guardar(any(Asignatura.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Asignatura creada = casoDeUso.crearAsignatura(nueva("  Calculo    Integral ", UUID_ISI), TOKEN);

        assertEquals("Calculo Integral", creada.getNombreAsignatura());
    }

    @Test
    void elNombreSeRevisaDespuesDeBloquearElPrograma() {
        prepararPrograma(sistemas(0));
        when(gateway.guardar(any(Asignatura.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        casoDeUso.crearAsignatura(nueva("Calculo I", UUID_ISI), TOKEN);

        InOrder orden = inOrder(programaGateway, gateway);
        orden.verify(programaGateway).bloquear(UUID_ISI);
        orden.verify(gateway).getPorPrograma(UUID_ISI);
        orden.verify(programaGateway).guardarUltimoConsecutivo(UUID_ISI, 1);
        orden.verify(gateway).guardar(any(Asignatura.class));
    }

    @Test
    void crearAsignaturaConNombreRepetidoEnElProgramaSinDistinguirMayusculasNiEspaciosLanzaEntidadExiste() {
        ProgramaAcademico programa = sistemas(3);
        prepararPrograma(programa, existente("a1", "ISI-002", "Calculo Integral", programa));

        ErrorEntidadExisteExcepcion error = assertThrows(ErrorEntidadExisteExcepcion.class,
                () -> casoDeUso.crearAsignatura(nueva("  CALCULO   integral ", UUID_ISI), TOKEN));

        assertEquals(String.format(MensajesError.ASIGNATURA_NOMBRE_EXISTE, "CALCULO integral", "Ingeniería de Sistemas", "ISI-002"),
                error.getMessage());
        verificarQueNoSeCreoNada();
    }

    @Test
    void elMismoNombreEnOtroProgramaEsValido() {
        prepararPrograma(electronica(0));
        when(gateway.guardar(any(Asignatura.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        assertEquals("IET-001", casoDeUso.crearAsignatura(nueva("Calculo Integral", UUID_IET), TOKEN).getCodigoAsignatura());
    }

    @Test
    void crearAsignaturaConProgramaInexistenteLanzaEntidadNoExiste() {
        when(programaGateway.getPorUuid("no-existe")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.crearAsignatura(nueva("Calculo I", "no-existe"), TOKEN));

        verify(programaGateway, never()).bloquear(anyString());
        verificarQueNoSeCreoNada();
    }

    @Test
    void crearAsignaturaSinProgramaLanzaMalFormato() {
        ErrorMalFormatoExcepcion error = assertThrows(ErrorMalFormatoExcepcion.class,
                () -> casoDeUso.crearAsignatura(nueva("Calculo I", null), TOKEN));

        assertEquals(MensajesError.PROGRAMA_ACADEMICO_REQUERIDO, error.getMessage());
        verifyNoInteractions(programaGateway);
        verificarQueNoSeCreoNada();
    }

    @Test
    void crearAsignaturaConNombreVacioLanzaMalFormato() {
        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.crearAsignatura(nueva("   ", UUID_ISI), TOKEN));
        verifyNoInteractions(programaGateway);
        verificarQueNoSeCreoNada();
    }

    @Test
    void crearAsignaturaConNombreDeMasDe150LanzaMalFormato() {
        ErrorMalFormatoExcepcion error = assertThrows(ErrorMalFormatoExcepcion.class,
                () -> casoDeUso.crearAsignatura(nueva("x".repeat(151), UUID_ISI), TOKEN));

        assertEquals(String.format(MensajesError.NOMBRE_ASIGNATURA_MUY_LARGO, 150), error.getMessage());
        verificarQueNoSeCreoNada();
    }

    @Test
    void crearAsignaturasAsignaCodigosConsecutivosPorProgramaEnElOrdenDeLaLista() {
        prepararPrograma(sistemas(10));
        prepararPrograma(electronica(0));
        when(gateway.guardar(any(Asignatura.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        List<Asignatura> creadas = casoDeUso.crearAsignaturas(new ArrayList<>(List.of(
                nueva("Calculo I", UUID_ISI),
                nueva("Circuitos I", UUID_IET),
                nueva("Calculo II", UUID_ISI),
                nueva("Calculo I", UUID_IET))), TOKEN);

        assertEquals(List.of("ISI-011", "IET-001", "ISI-012", "IET-002"),
                creadas.stream().map(Asignatura::getCodigoAsignatura).toList());
        verify(programaGateway).guardarUltimoConsecutivo(UUID_ISI, 12);
        verify(programaGateway).guardarUltimoConsecutivo(UUID_IET, 2);
        verify(log, times(4)).crearLog(eq("Crear asignatura"), anyString(), eq(TOKEN));
    }

    @Test
    void crearAsignaturasBloqueaLosProgramasEnOrdenDeSigla() {
        prepararPrograma(sistemas(0));
        prepararPrograma(electronica(0));
        when(gateway.guardar(any(Asignatura.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        casoDeUso.crearAsignaturas(new ArrayList<>(List.of(nueva("Calculo I", UUID_ISI), nueva("Circuitos", UUID_IET))), TOKEN);

        InOrder orden = inOrder(programaGateway);
        orden.verify(programaGateway).bloquear(UUID_IET);
        orden.verify(programaGateway).bloquear(UUID_ISI);
    }

    @Test
    void crearAsignaturasConNombreRepetidoEnLaListaLanzaMalFormatoSinBloquear() {
        when(programaGateway.getPorUuid(UUID_ISI)).thenReturn(sistemas(0));

        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.crearAsignaturas(new ArrayList<>(List.of(
                nueva("Calculo I", UUID_ISI), nueva(" calculo  i", UUID_ISI))), TOKEN));

        verify(programaGateway, never()).bloquear(anyString());
        verificarQueNoSeCreoNada();
    }

    @Test
    void crearAsignaturasConUnaYaExistenteNoGuardaNinguna() {
        ProgramaAcademico programa = sistemas(1);
        prepararPrograma(programa, existente("a1", "ISI-001", "Fisica I", programa));

        assertThrows(ErrorEntidadExisteExcepcion.class, () -> casoDeUso.crearAsignaturas(new ArrayList<>(List.of(
                nueva("Calculo I", UUID_ISI), nueva("Fisica I", UUID_ISI))), TOKEN));

        verificarQueNoSeCreoNada();
    }

    @Test
    void crearAsignaturasConListaVaciaLanzaSinInformacion() {
        assertThrows(ErrorNoInformacionExcepcion.class, () -> casoDeUso.crearAsignaturas(List.of(), TOKEN));
        verificarQueNoSeCreoNada();
    }

    @Test
    void creacionesSimultaneasEnElMismoProgramaNoRepitenCodigos() throws Exception {
        ProgramaFalso programas = new ProgramaFalso(sistemas(0));
        AsignaturasFalsas asignaturas = new AsignaturasFalsas();
        AsignaturaCUImplAdaptador concurrente = new AsignaturaCUImplAdaptador(asignaturas, programas,
                new ExcepcionesFormateadorImplAdaptador(), log);

        ExecutorService hilos = Executors.newFixedThreadPool(8);
        try {
            List<Callable<String>> tareas = IntStream.rangeClosed(1, 40)
                    .mapToObj(i -> (Callable<String>) () -> concurrente
                            .crearAsignatura(nueva("Asignatura " + i, UUID_ISI), TOKEN).getCodigoAsignatura())
                    .toList();
            List<String> codigos = new ArrayList<>();
            for (Future<String> resultado : hilos.invokeAll(tareas))
                codigos.add(resultado.get());

            List<String> esperados = IntStream.rangeClosed(1, 40).mapToObj(i -> String.format("ISI-%03d", i)).toList();
            assertEquals(esperados, codigos.stream().sorted().toList());
            assertEquals(40, programas.ultimo);
        } finally {
            hilos.shutdownNow();
        }
    }

    @Test
    void getAsignaturasPaginadoDevuelveLaPaginaDelGateway() {
        PaginacionRespuestaDTO<Asignatura> pagina = new PaginacionRespuestaDTO<>(
                List.of(existente("u1", "ISI-001", "Introduccion", sistemas(1))), 1);
        when(gateway.getPaginado(0, 10)).thenReturn(pagina);

        assertSame(pagina, casoDeUso.getAsignaturasPaginado(0, 10));
    }

    @ParameterizedTest
    @CsvSource({"-1, 10", "0, 0"})
    void getAsignaturasPaginadoConPaginacionInvalidaLanzaMalFormato(int pagina, int tamanio) {
        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.getAsignaturasPaginado(pagina, tamanio));
        verify(gateway, never()).getPaginado(anyInt(), anyInt());
    }

    @Test
    void getAsignaturasPorFiltroSinProgramaPasaNuloAlGateway() {
        PaginacionRespuestaDTO<Asignatura> pagina = new PaginacionRespuestaDTO<>(List.of(), 0);
        when(gateway.getPorFiltro("intro", null, 0, 5)).thenReturn(pagina);

        assertSame(pagina, casoDeUso.getAsignaturasPorFiltro("intro", " ", 0, 5));
        verifyNoInteractions(programaGateway);
    }

    @Test
    void getAsignaturasPorFiltroConProgramaFiltraPorEl() {
        PaginacionRespuestaDTO<Asignatura> pagina = new PaginacionRespuestaDTO<>(List.of(), 0);
        when(programaGateway.getPorUuid(UUID_ISI)).thenReturn(sistemas(0));
        when(gateway.getPorFiltro(null, UUID_ISI, 0, 5)).thenReturn(pagina);

        assertSame(pagina, casoDeUso.getAsignaturasPorFiltro(null, UUID_ISI, 0, 5));
    }

    @Test
    void getAsignaturasPorFiltroConProgramaInexistenteLanzaEntidadNoExiste() {
        when(programaGateway.getPorUuid("no-existe")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.getAsignaturasPorFiltro(null, "no-existe", 0, 5));
        verify(gateway, never()).getPorFiltro(any(), any(), anyInt(), anyInt());
    }

    @Test
    void getAsignaturasPorFiltroConPaginacionInvalidaLanzaMalFormato() {
        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.getAsignaturasPorFiltro("x", null, -1, 5));
        verifyNoInteractions(gateway);
    }

    @Test
    void getAsignaturasPorProgramaDevuelveLasDelGateway() {
        List<Asignatura> lista = List.of(existente("u1", "ISI-001", "Introduccion", sistemas(1)));
        when(programaGateway.getPorUuid(UUID_ISI)).thenReturn(sistemas(1));
        when(gateway.getPorPrograma(UUID_ISI)).thenReturn(lista);

        assertSame(lista, casoDeUso.getAsignaturasPorPrograma(UUID_ISI));
    }

    @Test
    void getAsignaturasPorProgramaInexistenteLanzaEntidadNoExiste() {
        when(programaGateway.getPorUuid("no-existe")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.getAsignaturasPorPrograma("no-existe"));
        verifyNoInteractions(gateway);
    }

    @Test
    void getAsignaturaInexistenteLanzaEntidadNoExiste() {
        when(gateway.getPorUuid("no-existe")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.getAsignatura("no-existe"));
    }

    @Test
    void actualizarAsignaturaSoloCambiaElNombreYConservaCodigoYPrograma() {
        ProgramaAcademico programa = sistemas(3);
        Asignatura actual = existente("u1", "ISI-001", "Introduccion", programa);
        prepararPrograma(programa, actual);
        when(gateway.getPorUuid("u1")).thenReturn(actual);
        when(gateway.guardar(any(Asignatura.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Asignatura cambios = existente(null, "IET-999", " Programacion   I ", electronica(0));
        Asignatura actualizada = casoDeUso.actualizarAsignatura("u1", cambios, TOKEN);

        assertEquals("ISI-001", actualizada.getCodigoAsignatura());
        assertEquals(UUID_ISI, actualizada.getProgramaAcademico().getUuidProgramaAcademico());
        assertEquals("Programacion I", actualizada.getNombreAsignatura());
        verify(programaGateway, never()).guardarUltimoConsecutivo(anyString(), anyInt());
        verify(log).crearLog(eq("Actualizar asignatura"), anyString(), eq(TOKEN));
    }

    @Test
    void actualizarAsignaturaConSuMismoNombreEnOtraEscrituraEsValido() {
        ProgramaAcademico programa = sistemas(1);
        Asignatura actual = existente("u1", "ISI-001", "Introduccion", programa);
        prepararPrograma(programa, actual);
        when(gateway.getPorUuid("u1")).thenReturn(actual);
        when(gateway.guardar(any(Asignatura.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        assertEquals("INTRODUCCION", casoDeUso.actualizarAsignatura("u1", nueva("INTRODUCCION", null), TOKEN).getNombreAsignatura());
    }

    @Test
    void actualizarAsignaturaConNombreDeOtraDelMismoProgramaLanzaEntidadExiste() {
        ProgramaAcademico programa = sistemas(2);
        Asignatura actual = existente("u1", "ISI-001", "Introduccion", programa);
        prepararPrograma(programa, actual, existente("u2", "ISI-002", "Programacion I", programa));
        when(gateway.getPorUuid("u1")).thenReturn(actual);

        assertThrows(ErrorEntidadExisteExcepcion.class,
                () -> casoDeUso.actualizarAsignatura("u1", nueva("programacion i", null), TOKEN));

        verify(gateway, never()).guardar(any());
        verifyNoInteractions(log);
    }

    @Test
    void actualizarAsignaturaInexistenteLanzaEntidadNoExiste() {
        when(gateway.getPorUuid("no-existe")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.actualizarAsignatura("no-existe", nueva("X", null), TOKEN));

        verify(gateway, never()).guardar(any());
        verifyNoInteractions(log);
    }

    private static class ProgramaFalso implements ProgramaAcademicoGatewayIntPuerto {
        private final ProgramaAcademico programa;
        private final ReentrantLock bloqueo = new ReentrantLock();
        private volatile int ultimo;

        ProgramaFalso(ProgramaAcademico programa) {
            this.programa = programa;
            this.ultimo = programa.getUltimoConsecutivoAsignatura();
        }

        @Override
        public List<ProgramaAcademico> getProgramas() {
            return List.of(programa);
        }

        @Override
        public ProgramaAcademico getPorUuid(String uuid) {
            return programa;
        }

        @Override
        public ProgramaAcademico bloquear(String uuid) {
            bloqueo.lock();
            return ProgramaAcademico.builder()
                    .uuidProgramaAcademico(programa.getUuidProgramaAcademico())
                    .sigla(programa.getSigla())
                    .nombre(programa.getNombre())
                    .ultimoConsecutivoAsignatura(ultimo)
                    .build();
        }

        @Override
        public void guardarUltimoConsecutivo(String uuid, int ultimoConsecutivo) {
            ultimo = ultimoConsecutivo;
            bloqueo.unlock();
        }
    }

    private static class AsignaturasFalsas implements AsignaturaGatewayIntPuerto {
        private final List<Asignatura> guardadas = Collections.synchronizedList(new ArrayList<>());

        @Override
        public Asignatura getPorCodigo(String codigo) {
            return null;
        }

        @Override
        public Asignatura guardar(Asignatura asignatura) {
            guardadas.add(asignatura);
            return asignatura;
        }

        @Override
        public PaginacionRespuestaDTO<Asignatura> getPaginado(int pagina, int tamanio) {
            return null;
        }

        @Override
        public PaginacionRespuestaDTO<Asignatura> getPorFiltro(String texto, String uuidPrograma, int pagina, int tamanio) {
            return null;
        }

        @Override
        public List<Asignatura> getPorPrograma(String uuidPrograma) {
            synchronized (guardadas) {
                return new ArrayList<>(guardadas);
            }
        }

        @Override
        public Asignatura getPorUuid(String uuid) {
            return null;
        }
    }
}
