package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.FuncionarioAcademicoGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.TipoSolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.FuncionarioAcademico;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorEntidadNoExisteExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsignacionFuncionarioAcademicoCUImplAdaptadorTest {

    private static final String TOKEN = "token";
    private static final String UUID_TIPO = "uuid-tipo";
    private static final String UUID_ANTERIOR = "uuid-funcionario-anterior";
    private static final String UUID_NUEVO = "uuid-funcionario-nuevo";

    @Mock
    private TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway;

    @Mock
    private FuncionarioAcademicoGatewayIntPuerto funcionarioGateway;

    @Mock
    private LogCUIntPuerto log;

    private AsignacionFuncionarioAcademicoCUImplAdaptador casoDeUso;

    @BeforeEach
    void setUp() {
        casoDeUso = new AsignacionFuncionarioAcademicoCUImplAdaptador(tipoSolicitudGateway, funcionarioGateway,
                new ExcepcionesFormateadorImplAdaptador(), log);
    }

    private FuncionarioAcademico funcionario(String uuid) {
        return FuncionarioAcademico.builder().uuidUsuario(uuid).dependencia("Dependencia").build();
    }

    private TipoSolicitudAcademica tipo(String uuidFuncionario) {
        return TipoSolicitudAcademica.builder()
                .uuidTipoSolicitudAcademica(UUID_TIPO)
                .nombre("Examen Supletorio")
                .funcionarioAcademico(funcionario(uuidFuncionario))
                .build();
    }

    @Test
    void asignarFuncionarioAcademicoCambiaElResponsableYRegistraElLog() {
        TipoSolicitudAcademica asignado = tipo(UUID_NUEVO);
        when(tipoSolicitudGateway.getPorUuid(UUID_TIPO)).thenReturn(tipo(UUID_ANTERIOR));
        when(funcionarioGateway.getPorUuid(UUID_NUEVO)).thenReturn(funcionario(UUID_NUEVO));
        when(tipoSolicitudGateway.asignarFuncionarioAcademico(UUID_TIPO, UUID_NUEVO)).thenReturn(asignado);

        TipoSolicitudAcademica resultado = casoDeUso.asignarFuncionarioAcademico(UUID_TIPO, UUID_NUEVO, TOKEN);

        assertSame(asignado, resultado);
        assertEquals(UUID_NUEVO, resultado.getFuncionarioAcademico().getUuidUsuario());
        ArgumentCaptor<String> descripcion = ArgumentCaptor.forClass(String.class);
        verify(log).crearLog(eq("Asignar funcionario académico a tipo de solicitud"), descripcion.capture(), eq(TOKEN));
        assertTrue(descripcion.getValue().contains(UUID_NUEVO));
        assertTrue(descripcion.getValue().contains(UUID_ANTERIOR));
    }

    @Test
    void reasignarAlMismoFuncionarioAcademicoEsValido() {
        when(tipoSolicitudGateway.getPorUuid(UUID_TIPO)).thenReturn(tipo(UUID_NUEVO));
        when(funcionarioGateway.getPorUuid(UUID_NUEVO)).thenReturn(funcionario(UUID_NUEVO));
        when(tipoSolicitudGateway.asignarFuncionarioAcademico(UUID_TIPO, UUID_NUEVO)).thenReturn(tipo(UUID_NUEVO));

        TipoSolicitudAcademica resultado = casoDeUso.asignarFuncionarioAcademico(UUID_TIPO, UUID_NUEVO, TOKEN);

        assertEquals(UUID_NUEVO, resultado.getFuncionarioAcademico().getUuidUsuario());
        verify(log).crearLog(anyString(), anyString(), eq(TOKEN));
    }

    @Test
    void tipoInexistenteLanzaEntidadNoExisteYNoAsigna() {
        when(tipoSolicitudGateway.getPorUuid("no-existe")).thenReturn(null);

        ErrorEntidadNoExisteExcepcion error = assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.asignarFuncionarioAcademico("no-existe", UUID_NUEVO, TOKEN));

        assertTrue(error.getMessage().contains("no-existe"));
        verifyNoInteractions(funcionarioGateway, log);
        verify(tipoSolicitudGateway, never()).asignarFuncionarioAcademico(anyString(), anyString());
    }

    @Test
    void funcionarioInexistenteLanzaEntidadNoExisteYNoAsigna() {
        when(tipoSolicitudGateway.getPorUuid(UUID_TIPO)).thenReturn(tipo(UUID_ANTERIOR));
        when(funcionarioGateway.getPorUuid("no-existe")).thenReturn(null);

        ErrorEntidadNoExisteExcepcion error = assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.asignarFuncionarioAcademico(UUID_TIPO, "no-existe", TOKEN));

        assertTrue(error.getMessage().contains("no-existe"));
        verify(tipoSolicitudGateway, never()).asignarFuncionarioAcademico(anyString(), anyString());
        verifyNoInteractions(log);
    }

    @Test
    void usuarioQueNoEsFuncionarioAcademicoLanzaEntidadNoExiste() {
        when(tipoSolicitudGateway.getPorUuid(UUID_TIPO)).thenReturn(tipo(UUID_ANTERIOR));
        when(funcionarioGateway.getPorUuid("uuid-estudiante")).thenReturn(null);

        assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.asignarFuncionarioAcademico(UUID_TIPO, "uuid-estudiante", TOKEN));

        verify(tipoSolicitudGateway, never()).asignarFuncionarioAcademico(anyString(), anyString());
    }

    @Test
    void uuidDeFuncionarioVacioLanzaEntidadNoExisteSinConsultar() {
        when(tipoSolicitudGateway.getPorUuid(UUID_TIPO)).thenReturn(tipo(UUID_ANTERIOR));

        assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.asignarFuncionarioAcademico(UUID_TIPO, "  ", TOKEN));

        verifyNoInteractions(funcionarioGateway, log);
        verify(tipoSolicitudGateway, never()).asignarFuncionarioAcademico(anyString(), anyString());
    }
}
