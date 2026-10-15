package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.IJwtServicio;
import com.unicauca.cfiet.solicitudes.aplicacion.output.OrdenDelDiaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SesionGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SolicitudGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.TipoSolicitudGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.UsuarioGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Funcionario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Solicitud;
import com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.almacenador.AlmacenadorArchivos;
import com.unicauca.cfiet.solicitudes.infraestructura.output.formateador.ExcepcionesFormateadorImplAdaptador;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.estructura.CodigoError;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.excepcionesPropias.ErrorEntidadNoExisteExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudCUImplAdaptadorTest {

    private static final String UUID_SOLICITUD = "sol-1";

    @Mock
    private SolicitudGatewayIntPuerto gateway;
    @Mock
    private TipoSolicitudGatewayIntPuerto gatewayTipoSolicitud;
    @Mock
    private OrdenDelDiaGatewayIntPuerto gatewayOrdenDelDia;
    @Mock
    private UsuarioGatewayIntPuerto gatewayUsuario;
    @Mock
    private LogCUIntPuerto log;
    @Mock
    private SesionGatewayIntPuerto gatewaySesion;
    @Mock
    private IJwtServicio jwtServicio;
    @Mock
    private AlmacenadorArchivos almacenadorArchivos;

    private SolicitudCUImplAdaptador casoDeUso;

    @BeforeEach
    void setUp() {
        casoDeUso = new SolicitudCUImplAdaptador(gateway, new ExcepcionesFormateadorImplAdaptador(), gatewayTipoSolicitud,
                gatewayOrdenDelDia, gatewayUsuario, log, gatewaySesion, jwtServicio, almacenadorArchivos);
    }

    private Solicitud cambios(String uuidFuncionario) {
        return Solicitud.builder()
                .nombre("Solicitud editada")
                .descripcion("descripcion")
                .estado("SIN RESPONDER")
                .consecutivo("C-1")
                .uuidFuncionario(uuidFuncionario)
                .build();
    }

    private Solicitud original(String uuidFuncionario) {
        Solicitud solicitud = Solicitud.builder()
                .uuidSolicitud(UUID_SOLICITUD)
                .nombre("Solicitud original")
                .objFuncionario(Funcionario.builder().uuidUsuario(uuidFuncionario).build())
                .build();
        when(gateway.getSolicitud(UUID_SOLICITUD)).thenReturn(solicitud);
        return solicitud;
    }

    @Test
    void actualizarUnaSolicitudInexistenteEsEntidadNoExisteConCodigoTres() {
        when(gateway.getSolicitud("nada")).thenReturn(null);

        ErrorEntidadNoExisteExcepcion error = assertThrows(ErrorEntidadNoExisteExcepcion.class,
                () -> casoDeUso.actualizarSolicitud("nada", cambios("fun-2"), "token"));

        assertEquals(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, "Solicitud", "nada"), error.getMessage());
        assertEquals("3", CodigoError.ERROR_ENTIDAD_NO_EXISTE.getCodigo());
        verify(gateway, never()).guardarSolicitud(any());
        verifyNoInteractions(log);
    }

    @Test
    void elSecretarioGeneralReasignaAOtroFuncionario() {
        original("fun-1");
        Funcionario nuevo = Funcionario.builder().uuidUsuario("fun-2").build();
        when(gatewayUsuario.getUsuario("fun-2")).thenReturn(nuevo);
        when(gateway.guardarSolicitud(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

        Solicitud guardada = casoDeUso.actualizarSolicitud(UUID_SOLICITUD, cambios("fun-2"), "token");

        assertSame(nuevo, guardada.getObjFuncionario());
        assertEquals("Solicitud editada", guardada.getNombre());
        verify(log).crearLog(anyString(), anyString(), eq("token"));
    }

    @Test
    void sinCambioDeFuncionarioConservaElAsignado() {
        Solicitud solicitud = original("fun-1");
        Funcionario asignado = solicitud.getObjFuncionario();
        when(gateway.guardarSolicitud(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

        casoDeUso.actualizarSolicitud(UUID_SOLICITUD, cambios("fun-1"), "token");
        casoDeUso.actualizarSolicitud(UUID_SOLICITUD, cambios(null), "token");

        ArgumentCaptor<Solicitud> captor = ArgumentCaptor.forClass(Solicitud.class);
        verify(gateway, times(2)).guardarSolicitud(captor.capture());
        captor.getAllValues().forEach(guardada -> assertSame(asignado, guardada.getObjFuncionario()));
        verify(gatewayUsuario, never()).getUsuario(anyString());
    }
}
