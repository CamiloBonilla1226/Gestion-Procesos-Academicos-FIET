package com.unicauca.cfiet.solicitudes.infraestructura.configuracion;

import com.unicauca.cfiet.solicitudes.aplicacion.input.AnexoAcademicoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ConsultaSolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.SolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.UsuarioCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.casosdeuso.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.MaquinaEtapas;
import com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.almacenador.AlmacenadorArchivos;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

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

    @Bean
    public AsignaturaCUImplAdaptador crearAsignaturaCU(AsignaturaGatewayIntPuerto gateway,
                                                       ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                       LogCUIntPuerto log){
        return new AsignaturaCUImplAdaptador(gateway, formateadorExcepciones, log);
    }

    @Bean
    public EstudianteCUImplAdaptador crearEstudianteCU(UsuarioCUIntPuerto usuarioCU,
                                                       EstudianteGatewayIntPuerto gateway,
                                                       AsignaturaGatewayIntPuerto asignaturaGateway,
                                                       RolGatewayIntPuerto rolGateway,
                                                       UsuarioGatewayIntPuerto usuarioGateway,
                                                       SesionGatewayIntPuerto sesionGateway,
                                                       IJwtServicio jwtServicio,
                                                       ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                       LogCUIntPuerto log){
        return new EstudianteCUImplAdaptador(usuarioCU, gateway, asignaturaGateway, rolGateway, usuarioGateway, sesionGateway, jwtServicio, formateadorExcepciones, log);
    }

    @Bean
    public FuncionarioAcademicoCUImplAdaptador crearFuncionarioAcademicoCU(UsuarioCUIntPuerto usuarioCU,
                                                                         FuncionarioAcademicoGatewayIntPuerto gateway,
                                                                         RolGatewayIntPuerto rolGateway,
                                                                         UsuarioGatewayIntPuerto usuarioGateway,
                                                                         ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                                         LogCUIntPuerto log){
        return new FuncionarioAcademicoCUImplAdaptador(usuarioCU, gateway, rolGateway, usuarioGateway, formateadorExcepciones, log);
    }

    @Bean
    public TipoSolicitudAcademicaCUImplAdaptador crearTipoSolicitudAcademicaCU(TipoSolicitudAcademicaGatewayIntPuerto gateway){
        return new TipoSolicitudAcademicaCUImplAdaptador(gateway);
    }

    @Bean
    public EtapaSolicitudAcademicaCUImplAdaptador crearEtapaSolicitudAcademicaCU(EtapaSolicitudAcademicaGatewayIntPuerto gateway,
                                                                               EtapaEtiquetaRolGatewayIntPuerto etiquetaGateway,
                                                                               TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                                                               ExcepcionesFormateadorIntPuerto formateadorExcepciones){
        return new EtapaSolicitudAcademicaCUImplAdaptador(gateway, etiquetaGateway, tipoSolicitudGateway, formateadorExcepciones);
    }

    @Bean
    public TipoAnexoAcademicoCUImplAdaptador crearTipoAnexoAcademicoCU(TipoAnexoAcademicoGatewayIntPuerto gateway,
                                                                     TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                                                     ExcepcionesFormateadorIntPuerto formateadorExcepciones){
        return new TipoAnexoAcademicoCUImplAdaptador(gateway, tipoSolicitudGateway, formateadorExcepciones);
    }

    @Bean
    public SituacionAcademicaAsignaturaCUImplAdaptador crearSituacionAcademicaAsignaturaCU(SituacionAcademicaAsignaturaGatewayIntPuerto gateway){
        return new SituacionAcademicaAsignaturaCUImplAdaptador(gateway);
    }

    @Bean
    public AsignacionFuncionarioAcademicoCUImplAdaptador crearAsignacionFuncionarioAcademicoCU(TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                                                                             FuncionarioAcademicoGatewayIntPuerto funcionarioGateway,
                                                                                             ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                                                             LogCUIntPuerto log){
        return new AsignacionFuncionarioAcademicoCUImplAdaptador(tipoSolicitudGateway, funcionarioGateway, formateadorExcepciones, log);
    }

    @Bean
    public MaquinaEtapas crearMaquinaEtapas(ExcepcionesFormateadorIntPuerto formateadorExcepciones){
        return new MaquinaEtapas(formateadorExcepciones);
    }

    @Bean
    public SolicitudAcademicaCUImplAdaptador crearSolicitudAcademicaCU(SolicitudAcademicaGatewayIntPuerto gateway,
                                                                     EstudianteGatewayIntPuerto estudianteGateway,
                                                                     TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                                                     EtapaSolicitudAcademicaGatewayIntPuerto etapaGateway,
                                                                     ResolucionAcademicaGatewayIntPuerto resolucionGateway,
                                                                     AnexoAcademicoGatewayIntPuerto anexoGateway,
                                                                     UsuarioGatewayIntPuerto usuarioGateway,
                                                                     AlmacenamientoAnexosIntPuerto almacenamiento,
                                                                     MaquinaEtapas maquinaEtapas,
                                                                     ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                                     LogCUIntPuerto log){
        return new SolicitudAcademicaCUImplAdaptador(gateway, estudianteGateway, tipoSolicitudGateway, etapaGateway,
                resolucionGateway, anexoGateway, usuarioGateway, almacenamiento, maquinaEtapas, formateadorExcepciones, log,
                Clock.system(ZoneId.of("America/Bogota")));
    }

    @Bean
    public AnexoAcademicoCUImplAdaptador crearAnexoAcademicoCU(AnexoAcademicoGatewayIntPuerto anexoGateway,
                                                             SolicitudAcademicaGatewayIntPuerto solicitudGateway,
                                                             TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                                             TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway,
                                                             UsuarioGatewayIntPuerto usuarioGateway,
                                                             AlmacenamientoAnexosIntPuerto almacenamiento,
                                                             ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                             LogCUIntPuerto log){
        return new AnexoAcademicoCUImplAdaptador(anexoGateway, solicitudGateway, tipoSolicitudGateway, tipoAnexoGateway,
                usuarioGateway, almacenamiento, formateadorExcepciones, log, Clock.system(ZoneId.of("America/Bogota")));
    }

    @Bean
    public ResolucionAcademicaCUImplAdaptador crearResolucionAcademicaCU(ResolucionAcademicaGatewayIntPuerto gateway,
                                                                       SolicitudAcademicaGatewayIntPuerto solicitudGateway,
                                                                       UsuarioGatewayIntPuerto usuarioGateway,
                                                                       AlmacenamientoAnexosIntPuerto almacenamiento,
                                                                       ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                                       LogCUIntPuerto log){
        return new ResolucionAcademicaCUImplAdaptador(gateway, solicitudGateway, usuarioGateway, almacenamiento,
                formateadorExcepciones, log, Clock.system(ZoneId.of("America/Bogota")));
    }

    @Bean
    public ConsultaSolicitudAcademicaCUImplAdaptador crearConsultaSolicitudAcademicaCU(SolicitudAcademicaGatewayIntPuerto gateway,
                                                                                     EtapaEtiquetaRolGatewayIntPuerto etiquetaGateway,
                                                                                     AnexoAcademicoGatewayIntPuerto anexoGateway,
                                                                                     HistorialSolicitudAcademicaGatewayIntPuerto historialGateway,
                                                                                     ResolucionAcademicaGatewayIntPuerto resolucionGateway,
                                                                                     SesionGatewayIntPuerto sesionGateway,
                                                                                     IJwtServicio jwtServicio,
                                                                                     AnexoAcademicoCUIntPuerto anexoCU,
                                                                                     MaquinaEtapas maquinaEtapas,
                                                                                     ExcepcionesFormateadorIntPuerto formateadorExcepciones){
        return new ConsultaSolicitudAcademicaCUImplAdaptador(gateway, etiquetaGateway, anexoGateway, historialGateway,
                resolucionGateway, sesionGateway, jwtServicio, anexoCU, maquinaEtapas, formateadorExcepciones);
    }

    @Bean
    public CancelacionMatriculaCUImplAdaptador crearCancelacionMatriculaCU(SolicitudCancelacionMatriculaGatewayIntPuerto gateway,
                                                                         EstudianteGatewayIntPuerto estudianteGateway,
                                                                         UsuarioGatewayIntPuerto usuarioGateway,
                                                                         TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                                                         TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway,
                                                                         SolicitudAcademicaCUIntPuerto solicitudCU,
                                                                         AnexoAcademicoCUIntPuerto anexoCU,
                                                                         ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                                         LogCUIntPuerto log){
        return new CancelacionMatriculaCUImplAdaptador(gateway, estudianteGateway, usuarioGateway, tipoSolicitudGateway,
                tipoAnexoGateway, solicitudCU, anexoCU, formateadorExcepciones, log);
    }

    @Bean
    public TramiteCancelacionMatriculaCUImplAdaptador crearTramiteCancelacionMatriculaCU(SolicitudCancelacionMatriculaGatewayIntPuerto gateway,
                                                                                       SolicitudAcademicaGatewayIntPuerto solicitudGateway,
                                                                                       SituacionAcademicaAsignaturaGatewayIntPuerto situacionGateway,
                                                                                       UsuarioGatewayIntPuerto usuarioGateway,
                                                                                       SesionGatewayIntPuerto sesionGateway,
                                                                                       IJwtServicio jwtServicio,
                                                                                       SolicitudAcademicaCUIntPuerto solicitudCU,
                                                                                       MaquinaEtapas maquinaEtapas,
                                                                                       ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                                                       LogCUIntPuerto log){
        return new TramiteCancelacionMatriculaCUImplAdaptador(gateway, solicitudGateway, situacionGateway, usuarioGateway,
                sesionGateway, jwtServicio, solicitudCU, maquinaEtapas, formateadorExcepciones, log);
    }

    @Bean
    public ConsultaCancelacionMatriculaCUImplAdaptador crearConsultaCancelacionMatriculaCU(SolicitudCancelacionMatriculaGatewayIntPuerto gateway,
                                                                                         EstudianteGatewayIntPuerto estudianteGateway,
                                                                                         TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                                                                         TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway,
                                                                                         SesionGatewayIntPuerto sesionGateway,
                                                                                         IJwtServicio jwtServicio,
                                                                                         ConsultaSolicitudAcademicaCUIntPuerto consultaCU,
                                                                                         MaquinaEtapas maquinaEtapas,
                                                                                         ExcepcionesFormateadorIntPuerto formateadorExcepciones){
        return new ConsultaCancelacionMatriculaCUImplAdaptador(gateway, estudianteGateway, tipoSolicitudGateway, tipoAnexoGateway,
                sesionGateway, jwtServicio, consultaCU, maquinaEtapas, formateadorExcepciones);
    }

    @Bean
    public CancelacionAsignaturaCUImplAdaptador crearCancelacionAsignaturaCU(SolicitudCancelacionAsignaturaGatewayIntPuerto gateway,
                                                                           SolicitudCancelacionMatriculaGatewayIntPuerto asignaturaSolicitudGateway,
                                                                           EstudianteGatewayIntPuerto estudianteGateway,
                                                                           UsuarioGatewayIntPuerto usuarioGateway,
                                                                           TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                                                           SolicitudAcademicaCUIntPuerto solicitudCU,
                                                                           AnexoAcademicoCUIntPuerto anexoCU,
                                                                           ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                                           LogCUIntPuerto log){
        return new CancelacionAsignaturaCUImplAdaptador(gateway, asignaturaSolicitudGateway, estudianteGateway, usuarioGateway,
                tipoSolicitudGateway, solicitudCU, anexoCU, formateadorExcepciones, log);
    }

    @Bean
    public TramiteCancelacionAsignaturaCUImplAdaptador crearTramiteCancelacionAsignaturaCU(SolicitudCancelacionAsignaturaGatewayIntPuerto gateway,
                                                                                         SolicitudCancelacionMatriculaGatewayIntPuerto asignaturaSolicitudGateway,
                                                                                         SolicitudAcademicaGatewayIntPuerto solicitudGateway,
                                                                                         SituacionAcademicaAsignaturaGatewayIntPuerto situacionGateway,
                                                                                         UsuarioGatewayIntPuerto usuarioGateway,
                                                                                         SesionGatewayIntPuerto sesionGateway,
                                                                                         IJwtServicio jwtServicio,
                                                                                         SolicitudAcademicaCUIntPuerto solicitudCU,
                                                                                         MaquinaEtapas maquinaEtapas,
                                                                                         ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                                                         LogCUIntPuerto log){
        return new TramiteCancelacionAsignaturaCUImplAdaptador(gateway, asignaturaSolicitudGateway, solicitudGateway, situacionGateway,
                usuarioGateway, sesionGateway, jwtServicio, solicitudCU, maquinaEtapas, formateadorExcepciones, log);
    }

    @Bean
    public ConsultaCancelacionAsignaturaCUImplAdaptador crearConsultaCancelacionAsignaturaCU(SolicitudCancelacionAsignaturaGatewayIntPuerto gateway,
                                                                                           EstudianteGatewayIntPuerto estudianteGateway,
                                                                                           SesionGatewayIntPuerto sesionGateway,
                                                                                           IJwtServicio jwtServicio,
                                                                                           ConsultaSolicitudAcademicaCUIntPuerto consultaCU,
                                                                                           MaquinaEtapas maquinaEtapas,
                                                                                           ExcepcionesFormateadorIntPuerto formateadorExcepciones){
        return new ConsultaCancelacionAsignaturaCUImplAdaptador(gateway, estudianteGateway, sesionGateway, jwtServicio,
                consultaCU, maquinaEtapas, formateadorExcepciones);
    }

    @Bean
    public ExamenSupletorioCUImplAdaptador crearExamenSupletorioCU(SolicitudExamenSupletorioGatewayIntPuerto gateway,
                                                                 EstudianteGatewayIntPuerto estudianteGateway,
                                                                 UsuarioGatewayIntPuerto usuarioGateway,
                                                                 TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                                                 TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway,
                                                                 SolicitudAcademicaCUIntPuerto solicitudCU,
                                                                 AnexoAcademicoCUIntPuerto anexoCU,
                                                                 ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                                 LogCUIntPuerto log){
        return new ExamenSupletorioCUImplAdaptador(gateway, estudianteGateway, usuarioGateway, tipoSolicitudGateway, tipoAnexoGateway,
                solicitudCU, anexoCU, formateadorExcepciones, log, Clock.system(ZoneId.of("America/Bogota")));
    }

    @Bean
    public TramiteExamenSupletorioCUImplAdaptador crearTramiteExamenSupletorioCU(SolicitudExamenSupletorioGatewayIntPuerto gateway,
                                                                               SolicitudAcademicaGatewayIntPuerto solicitudGateway,
                                                                               UsuarioGatewayIntPuerto usuarioGateway,
                                                                               SesionGatewayIntPuerto sesionGateway,
                                                                               IJwtServicio jwtServicio,
                                                                               SolicitudAcademicaCUIntPuerto solicitudCU,
                                                                               MaquinaEtapas maquinaEtapas,
                                                                               ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                                               LogCUIntPuerto log){
        return new TramiteExamenSupletorioCUImplAdaptador(gateway, solicitudGateway, usuarioGateway, sesionGateway, jwtServicio,
                solicitudCU, maquinaEtapas, formateadorExcepciones, log);
    }

    @Bean
    public ConsultaExamenSupletorioCUImplAdaptador crearConsultaExamenSupletorioCU(SolicitudExamenSupletorioGatewayIntPuerto gateway,
                                                                                 EstudianteGatewayIntPuerto estudianteGateway,
                                                                                 TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                                                                 TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway,
                                                                                 SesionGatewayIntPuerto sesionGateway,
                                                                                 IJwtServicio jwtServicio,
                                                                                 ConsultaSolicitudAcademicaCUIntPuerto consultaCU,
                                                                                 ExcepcionesFormateadorIntPuerto formateadorExcepciones){
        return new ConsultaExamenSupletorioCUImplAdaptador(gateway, estudianteGateway, tipoSolicitudGateway, tipoAnexoGateway,
                sesionGateway, jwtServicio, consultaCU, formateadorExcepciones);
    }
}
