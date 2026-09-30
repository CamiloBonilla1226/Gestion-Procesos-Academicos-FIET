package com.unicauca.cfiet.solicitudes.infraestructura.configuracion;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.casosdeuso.*;
import com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.almacenador.AlmacenadorArchivos;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gestiona las implementaciones de los casos de uso.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Configuration
public class BeanConfiguracion {

    @Bean
    public RolCUImplAdaptador createRolCU(RolGatewayIntPuerto gateway, ExcepcionesFormateadorIntPuerto formateadorExcepciones, LogCUIntPuerto logCU){
        return new RolCUImplAdaptador(gateway, formateadorExcepciones, logCU);
    }

    @Bean
    public UsuarioCUImplAdaptador createUsuarioCU(UsuarioGatewayIntPuerto gateway,
                                                  ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                  LogCUIntPuerto logCU,
                                                  PasswordEncoderGatewayIntPuerto encoder){
        return new UsuarioCUImplAdaptador(gateway, formateadorExcepciones, logCU, encoder);
    }

    @Bean
    public SesionCUImplAdaptador createSesionCU(SesionGatewayIntPuerto gateway,
                                                LogCUIntPuerto logCU,
                                                ExcepcionesFormateadorIntPuerto formateadorExcepciones){
        return new SesionCUImplAdaptador(gateway, logCU, formateadorExcepciones);
    }
    @Bean
    public LogCUImplAdaptador crearLogCU(LogGatewayIntPuerto gateway,ExcepcionesFormateadorIntPuerto formateadorExcepciones, IJwtServicio jwtServicio){
        return new LogCUImplAdaptador(gateway, formateadorExcepciones, jwtServicio);
    }

    @Bean
    public TipoSolicitudCUImplAdaptador crearTipoSolicitudCU(TipoSolicitudGatewayIntPuerto gateway, UsuarioGatewayIntPuerto gatewayUsuario, ExcepcionesFormateadorIntPuerto formateadorExcepciones, LogCUIntPuerto log, RolGatewayIntPuerto rolGateway){
        return new TipoSolicitudCUImplAdaptador(gateway, gatewayUsuario, formateadorExcepciones, log, rolGateway);
    }

    @Bean
    public OrdenDelDiaCUImplAdaptador crearOrdenDelDiaCU(OrdenDelDiaGatewayIntPuerto gateway,
                                                         SolicitudGatewayIntPuerto gatewaySolicitud,
                                                         ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                         LogCUIntPuerto log,
                                                         OrdenDelDiaExportador exportador,
                                                         RespuestaGatewayIntPuerto gatewayRespuesta){
        return new OrdenDelDiaCUImplAdaptador(gateway, gatewaySolicitud,formateadorExcepciones, log, exportador, gatewayRespuesta);
    }

    @Bean
    public SolicitudCUImplAdaptador crearSolicitudCU(SolicitudGatewayIntPuerto gateway,
                                                     ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                     TipoSolicitudGatewayIntPuerto gatewayTipoSolicitud,
                                                     OrdenDelDiaGatewayIntPuerto gatewayOrdenDelDia,
                                                     UsuarioGatewayIntPuerto gatewayUsuario,
                                                     LogCUIntPuerto log,
                                                     SesionGatewayIntPuerto gatewaySesion,
                                                     IJwtServicio jwtServicio,
                                                     AlmacenadorArchivos almacenadorArchivos){
        return new SolicitudCUImplAdaptador(gateway, formateadorExcepciones, gatewayTipoSolicitud, gatewayOrdenDelDia, gatewayUsuario, log, gatewaySesion, jwtServicio, almacenadorArchivos);
    }

    @Bean
    public RespuestaCUImplAdaptador crearRespuestaCU(RespuestaGatewayIntPuerto gateway,
                                                     SolicitudGatewayIntPuerto solicitudGateway,
                                                     ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                     LogCUIntPuerto log,
                                                     AlmacenadorArchivos almacenadorArchivos){
        return new RespuestaCUImplAdaptador(gateway, solicitudGateway, formateadorExcepciones, log, almacenadorArchivos);
    }
}
