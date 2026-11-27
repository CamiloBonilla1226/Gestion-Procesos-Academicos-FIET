package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.FuncionarioAcademicoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.TipoSolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.TipoSolicitudConsejoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.UsuarioGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.EncargadoTipoSolicitud;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Funcionario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.FuncionarioAcademico;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitudConsejo;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorEntidadNoExisteExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorReglaNegocioVioladaExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TipoSolicitudConsejoCUImplAdaptadorTest {

    private static final String TOKEN = "token";
    private static final String TIPO = "tipo-julian";
    private static final String OTRO_TIPO = "otro-tipo-julian";
    private static final String ASIGNATURA = "proceso-asignatura";
    private static final String MATRICULA = "proceso-matricula";
    private static final String ACADEMICO = "funcionario-academico";
    private static final String FUNCIONARIO = "funcionario-julian";

    @Mock
    private TipoSolicitudConsejoGatewayIntPuerto gateway;

    @Mock
    private TipoSolicitudAcademicaGatewayIntPuerto tipoAcademicoGateway;

    @Mock
    private FuncionarioAcademicoGatewayIntPuerto funcionarioAcademicoGateway;

    @Mock
    private UsuarioGatewayIntPuerto usuarioGateway;

    @Mock
    private LogCUIntPuerto log;

    private TipoSolicitudConsejoCUImplAdaptador casoDeUso;

    @BeforeEach
    void setUp() {
        casoDeUso = new TipoSolicitudConsejoCUImplAdaptador(gateway, tipoAcademicoGateway, funcionarioAcademicoGateway, usuarioGateway,
                new ExcepcionesFormateadorImplAdaptador(), log);
    }

    private TipoSolicitudAcademica proceso(String uuid, String nombre) {
        return TipoSolicitudAcademica.builder().uuidTipoSolicitudAcademica(uuid).nombre(nombre).build();
    }

    private TipoSolicitudAcademica asignatura() {
        return proceso(ASIGNATURA, "Cancelación de Asignatura");
    }

    private FuncionarioAcademico academico(boolean activo) {
        return FuncionarioAcademico.builder()
                .uuidUsuario(ACADEMICO)
                .usuario(Usuario.builder().uuidUsuario(ACADEMICO).nombres("Ana").apellidos("Ruiz").estado(activo).build())
                .build();
    }

    private TipoSolicitudConsejo enlace(String uuidTipo) {
        return TipoSolicitudConsejo.builder().uuidTipoSolicitud(uuidTipo).tipoSolicitudAcademica(asignatura()).build();
    }

    @Test
    void sinProcesoAlCrearNoHayEnlace() {
        assertNull(casoDeUso.prepararEnlace(null, null, FUNCIONARIO));
        assertNull(casoDeUso.prepararEnlace(null, "  ", FUNCIONARIO));
        verifyNoInteractions(tipoAcademicoGateway, funcionarioAcademicoGateway);
    }

    @Test
    void conCancelacionDeAsignaturaYUnFuncionarioAcademicoElEncargadoQuedaEnElEnlace() {
        when(tipoAcademicoGateway.getPorUuid(ASIGNATURA)).thenReturn(asignatura());
        when(funcionarioAcademicoGateway.getPorUuid(ACADEMICO)).thenReturn(academico(true));

        TipoSolicitudConsejo resultado = casoDeUso.prepararEnlace(null, ASIGNATURA, ACADEMICO);

        assertEquals(ASIGNATURA, resultado.getTipoSolicitudAcademica().getUuidTipoSolicitudAcademica());
        assertEquals(ACADEMICO, resultado.getEncargadoAcademico().getUuidUsuario());
    }

    @Test
    void conUnFuncionarioDeJulianElEnlaceNoLlevaEncargadoAcademico() {
        when(tipoAcademicoGateway.getPorUuid(ASIGNATURA)).thenReturn(asignatura());

        TipoSolicitudConsejo resultado = casoDeUso.prepararEnlace(TIPO, ASIGNATURA, FUNCIONARIO);

        assertNotNull(resultado);
        assertNull(resultado.getEncargadoAcademico());
    }

    @Test
    void otroProcesoNoSePuedeEnlazar() {
        when(tipoAcademicoGateway.getPorUuid(MATRICULA)).thenReturn(proceso(MATRICULA, "Cancelación de Matrícula"));

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.prepararEnlace(null, MATRICULA, ACADEMICO));

        assertEquals(String.format(MensajesError.PROCESO_SIN_CONSEJO, "Cancelación de Matrícula"), error.getMessage());
    }

    @Test
    void unProcesoInexistenteNoSePuedeEnlazar() {
        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.prepararEnlace(null, "no-existe", ACADEMICO));
    }

    @Test
    void soloUnTipoPuedeQuedarEnlazado() {
        when(tipoAcademicoGateway.getPorUuid(ASIGNATURA)).thenReturn(asignatura());
        when(gateway.getPorTipoAcademico(ASIGNATURA)).thenReturn(enlace(OTRO_TIPO));

        ErrorReglaNegocioVioladaExcepcion error = assertThrows(ErrorReglaNegocioVioladaExcepcion.class,
                () -> casoDeUso.prepararEnlace(TIPO, ASIGNATURA, ACADEMICO));

        assertEquals(String.format(MensajesError.TIPO_CONSEJO_YA_ENLAZADO, "Cancelación de Asignatura"), error.getMessage());
    }

    @Test
    void elMismoTipoSePuedeVolverAGuardar() {
        when(tipoAcademicoGateway.getPorUuid(ASIGNATURA)).thenReturn(asignatura());
        when(gateway.getPorTipoAcademico(ASIGNATURA)).thenReturn(enlace(TIPO));

        assertNotNull(casoDeUso.prepararEnlace(TIPO, ASIGNATURA, FUNCIONARIO));
    }

    @Test
    void alEditarSinElCampoSeConservaElEnlaceActual() {
        when(gateway.getPorTipoSolicitud(TIPO)).thenReturn(enlace(TIPO));
        when(tipoAcademicoGateway.getPorUuid(ASIGNATURA)).thenReturn(asignatura());
        when(gateway.getPorTipoAcademico(ASIGNATURA)).thenReturn(enlace(TIPO));
        when(funcionarioAcademicoGateway.getPorUuid(ACADEMICO)).thenReturn(academico(true));

        TipoSolicitudConsejo resultado = casoDeUso.prepararEnlace(TIPO, null, ACADEMICO);

        assertEquals(ASIGNATURA, resultado.getTipoSolicitudAcademica().getUuidTipoSolicitudAcademica());
        assertEquals(ACADEMICO, resultado.getEncargadoAcademico().getUuidUsuario());
    }

    @Test
    void alEditarConElCampoVacioSeQuitaElEnlace() {
        assertNull(casoDeUso.prepararEnlace(TIPO, "", FUNCIONARIO));
        when(gateway.getPorTipoSolicitud(TIPO)).thenReturn(enlace(TIPO));

        assertNull(casoDeUso.guardarEnlace(TIPO, null, TOKEN));

        verify(gateway).eliminarPorTipoSolicitud(TIPO);
        verify(log).crearLog(eq("Desenlazar tipo de solicitud del Consejo"), anyString(), eq(TOKEN));
    }

    @Test
    void guardarElEnlaceLePoneElTipoYRegistraElLog() {
        TipoSolicitudConsejo nuevo = TipoSolicitudConsejo.builder().tipoSolicitudAcademica(asignatura()).encargadoAcademico(academico(true)).build();
        when(gateway.guardar(nuevo)).thenReturn(nuevo);

        casoDeUso.guardarEnlace(TIPO, nuevo, TOKEN);

        assertEquals(TIPO, nuevo.getUuidTipoSolicitud());
        ArgumentCaptor<String> descripcion = ArgumentCaptor.forClass(String.class);
        verify(log).crearLog(eq("Enlazar tipo de solicitud al Consejo"), descripcion.capture(), eq(TOKEN));
        assertTrue(descripcion.getValue().contains(ACADEMICO));
    }

    @Test
    void sinEnlaceNoSeBorraNiSeRegistraNada() {
        casoDeUso.guardarEnlace(TIPO, null, TOKEN);

        verify(gateway, never()).eliminarPorTipoSolicitud(any());
        verifyNoInteractions(log);
    }

    @Test
    void losCandidatosSonLosFuncionariosYParaAsignaturaTambienLosFuncionariosAcademicosActivos() {
        when(usuarioGateway.getFuncionarios()).thenReturn(List.of(Funcionario.builder().uuidUsuario(FUNCIONARIO).nombres("Luis").apellidos("Paz").build()));
        when(tipoAcademicoGateway.getPorUuid(ASIGNATURA)).thenReturn(asignatura());
        FuncionarioAcademico inactivo = academico(false);
        inactivo.setUuidUsuario("inactivo");
        when(funcionarioAcademicoGateway.getTodos()).thenReturn(List.of(academico(true), inactivo));

        List<EncargadoTipoSolicitud> sinProceso = casoDeUso.getEncargados(null);
        List<EncargadoTipoSolicitud> conProceso = casoDeUso.getEncargados(ASIGNATURA);

        assertEquals(List.of(FUNCIONARIO), sinProceso.stream().map(EncargadoTipoSolicitud::getUuidUsuario).toList());
        assertEquals(List.of(FUNCIONARIO, ACADEMICO), conProceso.stream().map(EncargadoTipoSolicitud::getUuidUsuario).toList());
        assertEquals(List.of("FUNCIONARIO", "FUNCIONARIO_ACADEMICO"), conProceso.stream().map(EncargadoTipoSolicitud::getTipo).toList());
    }

    @Test
    void losCandidatosDeOtroProcesoSeRechazan() {
        when(usuarioGateway.getFuncionarios()).thenReturn(List.of());
        when(tipoAcademicoGateway.getPorUuid(MATRICULA)).thenReturn(proceso(MATRICULA, "Cancelación de Matrícula"));

        assertThrows(ErrorReglaNegocioVioladaExcepcion.class, () -> casoDeUso.getEncargados(MATRICULA));
        verify(funcionarioAcademicoGateway, never()).getTodos();
    }
}
