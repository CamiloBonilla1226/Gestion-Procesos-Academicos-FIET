package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.output.EtapaEtiquetaRolGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.EtapaSolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.TipoSolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.EtapaEtiquetaRol;
import com.unicauca.cfiet.solicitudes.dominio.modelos.EtapaSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.RolEtiquetaEtapa;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorEntidadNoExisteExcepcion;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorMalFormatoExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EtapaSolicitudAcademicaCUImplAdaptadorTest {

    @Mock
    private EtapaSolicitudAcademicaGatewayIntPuerto gateway;

    @Mock
    private EtapaEtiquetaRolGatewayIntPuerto etiquetaGateway;

    @Mock
    private TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway;

    private EtapaSolicitudAcademicaCUImplAdaptador casoDeUso;

    @BeforeEach
    void setUp() {
        casoDeUso = new EtapaSolicitudAcademicaCUImplAdaptador(gateway, etiquetaGateway, tipoSolicitudGateway,
                new ExcepcionesFormateadorImplAdaptador());
    }

    @Test
    void getEtapasPorTipoExistenteDevuelveLasDelGateway() {
        List<EtapaSolicitudAcademica> etapas = List.of(EtapaSolicitudAcademica.builder().uuidEtapa("e1").codigo("RADICADA").build());
        when(tipoSolicitudGateway.existePorUuid("t1")).thenReturn(true);
        when(gateway.getPorTipoIncluyendoUniversales("t1")).thenReturn(etapas);

        assertSame(etapas, casoDeUso.getEtapasPorTipo("t1"));
    }

    @Test
    void getEtapasPorTipoInexistenteLanzaEntidadNoExiste() {
        when(tipoSolicitudGateway.existePorUuid("no-existe")).thenReturn(false);

        assertThrows(ErrorEntidadNoExisteExcepcion.class, () -> casoDeUso.getEtapasPorTipo("no-existe"));
        verifyNoInteractions(gateway);
    }

    @ParameterizedTest
    @CsvSource({"ESTUDIANTE, ESTUDIANTE", "funcionario, FUNCIONARIO", "' Decano ', DECANO"})
    void getEtiquetasPorRolValidoConsultaElRolNormalizado(String recibido, RolEtiquetaEtapa esperado) {
        List<EtapaEtiquetaRol> etiquetas = List.of(EtapaEtiquetaRol.builder().rol(esperado).etiqueta("Pendiente").build());
        when(etiquetaGateway.getPorRol(esperado)).thenReturn(etiquetas);

        assertSame(etiquetas, casoDeUso.getEtiquetasPorRol(recibido));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SECRETARIO", "", "estudiantes"})
    void getEtiquetasPorRolNoValidoLanzaMalFormato(String rol) {
        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.getEtiquetasPorRol(rol));
        verify(etiquetaGateway, never()).getPorRol(any());
    }

    @Test
    void getEtiquetasPorRolNuloLanzaMalFormato() {
        assertThrows(ErrorMalFormatoExcepcion.class, () -> casoDeUso.getEtiquetasPorRol(null));
        verifyNoInteractions(etiquetaGateway);
    }
}
