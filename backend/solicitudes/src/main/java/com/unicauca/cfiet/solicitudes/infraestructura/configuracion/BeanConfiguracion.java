package com.unicauca.cfiet.solicitudes.infraestructura.configuracion;

import com.unicauca.cfiet.solicitudes.aplicacion.input.AnexoAcademicoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ConsultaCancelacionAsignaturaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ConsultaCancelacionMatriculaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ConsultaExamenSupletorioCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ConsultaSolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.NotificacionResolucionCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ResolucionAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.SolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.UsuarioCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.casosdeuso.*;
import com.unicauca.cfiet.solicitudes.dominio.modelos.FirmaCorreo;
import com.unicauca.cfiet.solicitudes.dominio.servicios.ComunicacionResolucion;
import com.unicauca.cfiet.solicitudes.dominio.servicios.MaquinaEtapas;
import com.unicauca.cfiet.solicitudes.dominio.servicios.NotificadorRespuestaFinal;
import com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.almacenador.AlmacenadorArchivos;
import org.springframework.beans.factory.annotation.Value;
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
                                                       ProgramaAcademicoGatewayIntPuerto programaGateway,
                                                       ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                       LogCUIntPuerto log){
        return new AsignaturaCUImplAdaptador(gateway, programaGateway, formateadorExcepciones, log);
    }

    @Bean
    public ProgramaAcademicoCUImplAdaptador crearProgramaAcademicoCU(ProgramaAcademicoGatewayIntPuerto gateway){
        return new ProgramaAcademicoCUImplAdaptador(gateway);
    }

    @Bean
    public EstudianteCUImplAdaptador crearEstudianteCU(UsuarioCUIntPuerto usuarioCU,
                                                       EstudianteGatewayIntPuerto gateway,
                                                       AsignaturaGatewayIntPuerto asignaturaGateway,
                                                       ProgramaAcademicoGatewayIntPuerto programaGateway,
                                                       RolGatewayIntPuerto rolGateway,
                                                       UsuarioGatewayIntPuerto usuarioGateway,
                                                       DatosPersonalesGatewayIntPuerto datosPersonalesGateway,
                                                       SesionGatewayIntPuerto sesionGateway,
                                                       IJwtServicio jwtServicio,
                                                       ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                       LogCUIntPuerto log){
        return new EstudianteCUImplAdaptador(usuarioCU, gateway, asignaturaGateway, programaGateway, rolGateway, usuarioGateway, datosPersonalesGateway, sesionGateway, jwtServicio, formateadorExcepciones, log);
    }

    @Bean
    public FuncionarioAcademicoCUImplAdaptador crearFuncionarioAcademicoCU(UsuarioCUIntPuerto usuarioCU,
                                                                         FuncionarioAcademicoGatewayIntPuerto gateway,
                                                                         RolGatewayIntPuerto rolGateway,
                                                                         UsuarioGatewayIntPuerto usuarioGateway,
                                                                         DatosPersonalesGatewayIntPuerto datosPersonalesGateway,
                                                                         ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                                         LogCUIntPuerto log){
        return new FuncionarioAcademicoCUImplAdaptador(usuarioCU, gateway, rolGateway, usuarioGateway, datosPersonalesGateway, formateadorExcepciones, log);
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
                                                                     UsuarioActorGatewayIntPuerto usuarioActorGateway,
                                                                     AlmacenamientoAnexosIntPuerto almacenamiento,
                                                                     MaquinaEtapas maquinaEtapas,
                                                                     ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                                     LogCUIntPuerto log,
                                                                     CorreoIntPuerto correo,
                                                                     NotificadorRespuestaFinal notificador){
        return new SolicitudAcademicaCUImplAdaptador(gateway, estudianteGateway, tipoSolicitudGateway, etapaGateway,
                resolucionGateway, anexoGateway, usuarioActorGateway, almacenamiento, maquinaEtapas, formateadorExcepciones, log,
                Clock.system(ZoneId.of("America/Bogota")), correo, notificador);
    }

    @Bean
    public NotificadorRespuestaFinal crearNotificadorRespuestaFinal(SolicitudCancelacionAsignaturaGatewayIntPuerto cancelacionAsignaturaGateway,
                                                                    SolicitudCancelacionMatriculaGatewayIntPuerto cancelacionMatriculaGateway,
                                                                    SolicitudExamenSupletorioGatewayIntPuerto supletorioGateway,
                                                                    ResolucionAcademicaGatewayIntPuerto resolucionGateway,
                                                                    AnexoAcademicoGatewayIntPuerto anexoGateway,
                                                                    UsuarioGatewayIntPuerto usuarioGateway,
                                                                    GeneradorPdfRespuestaIntPuerto generadorPdf,
                                                                    PreparacionCorreoIntPuerto preparacion,
                                                                    CorreoIntPuerto correo,
                                                                    @Value("${correo.darca.destinatario:}") String destinatarioDarca){
        return new NotificadorRespuestaFinal(cancelacionAsignaturaGateway, cancelacionMatriculaGateway, supletorioGateway, resolucionGateway, anexoGateway, usuarioGateway,
                generadorPdf, preparacion, correo, destinatarioDarca);
    }

    @Bean
    public AnexoAcademicoCUImplAdaptador crearAnexoAcademicoCU(AnexoAcademicoGatewayIntPuerto anexoGateway,
                                                             SolicitudAcademicaGatewayIntPuerto solicitudGateway,
                                                             TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                                             TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway,
                                                             UsuarioActorGatewayIntPuerto usuarioActorGateway,
                                                             AlmacenamientoAnexosIntPuerto almacenamiento,
                                                             ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                             LogCUIntPuerto log){
        return new AnexoAcademicoCUImplAdaptador(anexoGateway, solicitudGateway, tipoSolicitudGateway, tipoAnexoGateway,
                usuarioActorGateway, almacenamiento, formateadorExcepciones, log, Clock.system(ZoneId.of("America/Bogota")));
    }

    @Bean
    public ResolucionAcademicaCUImplAdaptador crearResolucionAcademicaCU(ResolucionAcademicaGatewayIntPuerto gateway,
                                                                       SolicitudAcademicaGatewayIntPuerto solicitudGateway,
                                                                       UsuarioActorGatewayIntPuerto usuarioActorGateway,
                                                                       AlmacenamientoAnexosIntPuerto almacenamiento,
                                                                       ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                                       LogCUIntPuerto log,
                                                                       NotificacionResolucionGatewayIntPuerto notificacionGateway){
        return new ResolucionAcademicaCUImplAdaptador(gateway, solicitudGateway, usuarioActorGateway, almacenamiento,
                formateadorExcepciones, log, Clock.system(ZoneId.of("America/Bogota")), notificacionGateway);
    }

    @Bean
    public NotificacionResolucionCUImplAdaptador crearNotificacionResolucionCU(NotificacionResolucionGatewayIntPuerto gateway,
                                                                             SolicitudAcademicaGatewayIntPuerto solicitudGateway,
                                                                             SolicitudCancelacionAsignaturaGatewayIntPuerto cancelacionGateway,
                                                                             SesionGatewayIntPuerto sesionGateway,
                                                                             IJwtServicio jwtServicio,
                                                                             ResolucionAcademicaCUIntPuerto resolucionCU,
                                                                             NotificadorRespuestaFinal notificador,
                                                                             GeneradorPdfRespuestaIntPuerto generadorPdf,
                                                                             CorreoIntPuerto correo,
                                                                             ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                                             LogCUIntPuerto log,
                                                                             EjecucionDiferidaIntPuerto ejecucionDiferida,
                                                                             @Value("${correo.firma.nombre:}") String firmaNombre,
                                                                             @Value("${correo.firma.cargo:}") String firmaCargo,
                                                                             @Value("${correo.firma.dependencia:}") String firmaDependencia,
                                                                             @Value("${correo.firma.facultad:}") String firmaFacultad,
                                                                             @Value("${correo.resolucion.prefijo:}") String prefijo){
        FirmaCorreo firma = FirmaCorreo.builder().nombre(firmaNombre).cargo(firmaCargo).dependencia(firmaDependencia).facultad(firmaFacultad).build();
        return new NotificacionResolucionCUImplAdaptador(gateway, solicitudGateway, cancelacionGateway, sesionGateway, jwtServicio, resolucionCU,
                notificador, generadorPdf, correo, new ComunicacionResolucion(firma, prefijo), formateadorExcepciones, log,
                Clock.system(ZoneId.of("America/Bogota")), ejecucionDiferida);
    }

    @Bean
    public ConsultaSolicitudAcademicaCUImplAdaptador crearConsultaSolicitudAcademicaCU(SolicitudAcademicaGatewayIntPuerto gateway,
                                                                                     EtapaEtiquetaRolGatewayIntPuerto etiquetaGateway,
                                                                                     AnexoAcademicoGatewayIntPuerto anexoGateway,
                                                                                     HistorialSolicitudAcademicaGatewayIntPuerto historialGateway,
                                                                                     ResolucionAcademicaGatewayIntPuerto resolucionGateway,
                                                                                     UsuarioActorGatewayIntPuerto usuarioActorGateway,
                                                                                     IJwtServicio jwtServicio,
                                                                                     AnexoAcademicoCUIntPuerto anexoCU,
                                                                                     MaquinaEtapas maquinaEtapas,
                                                                                     ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                                                     NotificacionResolucionGatewayIntPuerto notificacionGateway){
        return new ConsultaSolicitudAcademicaCUImplAdaptador(gateway, etiquetaGateway, anexoGateway, historialGateway,
                resolucionGateway, usuarioActorGateway, jwtServicio, anexoCU, maquinaEtapas, formateadorExcepciones, notificacionGateway);
    }

    @Bean
    public EliminacionRespuestasCUImplAdaptador crearEliminacionRespuestasCU(EliminacionRespuestasGatewayIntPuerto gateway,
                                                                           SesionGatewayIntPuerto sesionGateway,
                                                                           IJwtServicio jwtServicio,
                                                                           AlmacenamientoAnexosIntPuerto almacenamiento,
                                                                           ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                                           LogCUIntPuerto log){
        return new EliminacionRespuestasCUImplAdaptador(gateway, sesionGateway, jwtServicio, almacenamiento, formateadorExcepciones, log,
                Clock.system(ZoneId.of("America/Bogota")));
    }

    @Bean
    public CorreosPendientesCUImplAdaptador crearCorreosPendientesCU(CorreoIntPuerto correo){
        return new CorreosPendientesCUImplAdaptador(correo);
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
                                                                                       UsuarioActorGatewayIntPuerto usuarioActorGateway,
                                                                                       SesionGatewayIntPuerto sesionGateway,
                                                                                       IJwtServicio jwtServicio,
                                                                                       SolicitudAcademicaCUIntPuerto solicitudCU,
                                                                                       MaquinaEtapas maquinaEtapas,
                                                                                       ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                                                       LogCUIntPuerto log){
        return new TramiteCancelacionMatriculaCUImplAdaptador(gateway, solicitudGateway, situacionGateway, usuarioActorGateway,
                sesionGateway, jwtServicio, solicitudCU, maquinaEtapas, formateadorExcepciones, log);
    }

    @Bean
    public ConsultaCancelacionMatriculaCUImplAdaptador crearConsultaCancelacionMatriculaCU(SolicitudCancelacionMatriculaGatewayIntPuerto gateway,
                                                                                         EstudianteGatewayIntPuerto estudianteGateway,
                                                                                         TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                                                                         TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway,
                                                                                         UsuarioActorGatewayIntPuerto usuarioActorGateway,
                                                                                         IJwtServicio jwtServicio,
                                                                                         ConsultaSolicitudAcademicaCUIntPuerto consultaCU,
                                                                                         MaquinaEtapas maquinaEtapas,
                                                                                         ExcepcionesFormateadorIntPuerto formateadorExcepciones){
        return new ConsultaCancelacionMatriculaCUImplAdaptador(gateway, estudianteGateway, tipoSolicitudGateway, tipoAnexoGateway,
                usuarioActorGateway, jwtServicio, consultaCU, maquinaEtapas, formateadorExcepciones);
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
                                                                                         UsuarioActorGatewayIntPuerto usuarioActorGateway,
                                                                                         SesionGatewayIntPuerto sesionGateway,
                                                                                         IJwtServicio jwtServicio,
                                                                                         SolicitudAcademicaCUIntPuerto solicitudCU,
                                                                                         MaquinaEtapas maquinaEtapas,
                                                                                         ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                                                         LogCUIntPuerto log,
                                                                                         NotificacionResolucionCUIntPuerto notificacionCU){
        return new TramiteCancelacionAsignaturaCUImplAdaptador(gateway, asignaturaSolicitudGateway, solicitudGateway, situacionGateway,
                usuarioActorGateway, sesionGateway, jwtServicio, solicitudCU, maquinaEtapas, formateadorExcepciones, log, notificacionCU);
    }

    @Bean
    public ConsultaCancelacionAsignaturaCUImplAdaptador crearConsultaCancelacionAsignaturaCU(SolicitudCancelacionAsignaturaGatewayIntPuerto gateway,
                                                                                           EstudianteGatewayIntPuerto estudianteGateway,
                                                                                           UsuarioActorGatewayIntPuerto usuarioActorGateway,
                                                                                           IJwtServicio jwtServicio,
                                                                                           ConsultaSolicitudAcademicaCUIntPuerto consultaCU,
                                                                                           MaquinaEtapas maquinaEtapas,
                                                                                           ExcepcionesFormateadorIntPuerto formateadorExcepciones){
        return new ConsultaCancelacionAsignaturaCUImplAdaptador(gateway, estudianteGateway, usuarioActorGateway, jwtServicio,
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
                                                                               UsuarioActorGatewayIntPuerto usuarioActorGateway,
                                                                               SesionGatewayIntPuerto sesionGateway,
                                                                               IJwtServicio jwtServicio,
                                                                               DocenteGatewayIntPuerto docenteGateway,
                                                                               NotificacionDocenteGatewayIntPuerto notificacionGateway,
                                                                               SolicitudAcademicaCUIntPuerto solicitudCU,
                                                                               MaquinaEtapas maquinaEtapas,
                                                                               ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                                               LogCUIntPuerto log,
                                                                               NotificadorRespuestaFinal notificador){
        return new TramiteExamenSupletorioCUImplAdaptador(gateway, solicitudGateway, usuarioActorGateway, sesionGateway, jwtServicio,
                docenteGateway, notificacionGateway, solicitudCU, maquinaEtapas, formateadorExcepciones, log,
                Clock.system(ZoneId.of("America/Bogota")), notificador);
    }

    @Bean
    public DocenteCUImplAdaptador crearDocenteCU(DocenteGatewayIntPuerto gateway,
                                                 ExcepcionesFormateadorIntPuerto formateadorExcepciones){
        return new DocenteCUImplAdaptador(gateway, formateadorExcepciones);
    }

    @Bean
    public NotificacionDocenteCUImplAdaptador crearNotificacionDocenteCU(NotificacionDocenteGatewayIntPuerto gateway,
                                                                         SolicitudAcademicaGatewayIntPuerto solicitudGateway,
                                                                         SolicitudExamenSupletorioGatewayIntPuerto supletorioGateway,
                                                                         AnexoAcademicoGatewayIntPuerto anexoGateway,
                                                                         AlmacenamientoAnexosIntPuerto almacenamiento,
                                                                         SesionGatewayIntPuerto sesionGateway,
                                                                         IJwtServicio jwtServicio,
                                                                         ExcepcionesFormateadorIntPuerto formateadorExcepciones){
        return new NotificacionDocenteCUImplAdaptador(gateway, solicitudGateway, supletorioGateway, anexoGateway, almacenamiento,
                sesionGateway, jwtServicio, formateadorExcepciones, Clock.system(ZoneId.of("America/Bogota")));
    }

    @Bean
    public ConsultaExamenSupletorioCUImplAdaptador crearConsultaExamenSupletorioCU(SolicitudExamenSupletorioGatewayIntPuerto gateway,
                                                                                 EstudianteGatewayIntPuerto estudianteGateway,
                                                                                 TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                                                                 TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway,
                                                                                 UsuarioActorGatewayIntPuerto usuarioActorGateway,
                                                                                 IJwtServicio jwtServicio,
                                                                                 ConsultaSolicitudAcademicaCUIntPuerto consultaCU,
                                                                                 ExcepcionesFormateadorIntPuerto formateadorExcepciones){
        return new ConsultaExamenSupletorioCUImplAdaptador(gateway, estudianteGateway, tipoSolicitudGateway, tipoAnexoGateway,
                usuarioActorGateway, jwtServicio, consultaCU, formateadorExcepciones);
    }

    @Bean
    public AccesoRecursoConsejoCUImplAdaptador crearAccesoRecursoConsejoCU(UsuarioGatewayIntPuerto usuarioGateway,
                                                                         SesionGatewayIntPuerto sesionGateway,
                                                                         SolicitudGatewayIntPuerto solicitudGateway,
                                                                         RespuestaGatewayIntPuerto respuestaGateway,
                                                                         IJwtServicio jwtServicio,
                                                                         ExcepcionesFormateadorIntPuerto formateadorExcepciones){
        return new AccesoRecursoConsejoCUImplAdaptador(usuarioGateway, sesionGateway, solicitudGateway, respuestaGateway,
                jwtServicio, formateadorExcepciones);
    }

    @Bean
    public FormatoOficialCUImplAdaptador crearFormatoOficialCU(ConsultaSolicitudAcademicaCUIntPuerto consultaCU,
                                                               ConsultaCancelacionMatriculaCUIntPuerto consultaMatriculaCU,
                                                               ConsultaCancelacionAsignaturaCUIntPuerto consultaAsignaturaCU,
                                                               ConsultaExamenSupletorioCUIntPuerto consultaSupletorioCU,
                                                               SolicitudAcademicaCUIntPuerto solicitudCU,
                                                               AnexoAcademicoCUIntPuerto anexoCU,
                                                               SolicitudAcademicaGatewayIntPuerto solicitudGateway,
                                                               TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway,
                                                               SituacionAcademicaAsignaturaGatewayIntPuerto situacionGateway,
                                                               EliminacionRespuestasGatewayIntPuerto eliminacionGateway,
                                                               AlmacenamientoAnexosIntPuerto almacenamiento,
                                                               ComposicionFormatoOficialIntPuerto composicion,
                                                               MaquinaEtapas maquinaEtapas,
                                                               ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                               LogCUIntPuerto log){
        return new FormatoOficialCUImplAdaptador(consultaCU, consultaMatriculaCU, consultaAsignaturaCU, consultaSupletorioCU, solicitudCU,
                anexoCU, solicitudGateway, tipoAnexoGateway, situacionGateway, eliminacionGateway, almacenamiento, composicion,
                maquinaEtapas, formateadorExcepciones, log);
    }

    @Bean
    public ResolucionCancelacionAsignaturaCUImplAdaptador crearResolucionCancelacionAsignaturaCU(ConsultaCancelacionAsignaturaCUIntPuerto consultaAsignaturaCU,
                                                                                               ConsultaSolicitudAcademicaCUIntPuerto consultaCU,
                                                                                               SituacionAcademicaAsignaturaGatewayIntPuerto situacionGateway,
                                                                                               GeneradorResolucionIntPuerto generador,
                                                                                               ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                                                                               @Value("${correo.resolucion.prefijo:}") String prefijo,
                                                                                               @Value("${resolucion.firmante:}") String firmante,
                                                                                               @Value("${resolucion.reviso:}") String reviso){
        return new ResolucionCancelacionAsignaturaCUImplAdaptador(consultaAsignaturaCU, consultaCU, situacionGateway, generador,
                formateadorExcepciones, Clock.system(ZoneId.of("America/Bogota")), prefijo, firmante, reviso);
    }
}
