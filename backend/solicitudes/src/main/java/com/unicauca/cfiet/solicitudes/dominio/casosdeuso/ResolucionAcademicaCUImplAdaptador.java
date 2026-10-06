package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.ResolucionAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.AlmacenamientoAnexosIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ResolucionAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SolicitudAcademicaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.UsuarioGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.ValidadorActorSolicitud;
import com.unicauca.cfiet.solicitudes.dominio.servicios.ValidadorArchivoAdjunto;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.*;

public class ResolucionAcademicaCUImplAdaptador implements ResolucionAcademicaCUIntPuerto {
    private static final String SOLICITUD_ACADEMICA = "Solicitud académica";
    private static final String RESOLUCION = "Resolución académica";
    private static final String ARCHIVO = "Archivo de la Resolución";
    private static final String DOCUMENTO = "el escaneo de la Resolución";
    private static final String FORMATO = "pdf";
    private static final String TIPO_CONTENIDO = "application/pdf";
    private static final List<String> ETAPAS_PERMITIDAS = List.of(RADICADA, APROBADA_POR_DECANO, RECHAZADA_POR_DECANO);
    private static final List<String> ETAPAS_FINALES = List.of(APROBADA, RECHAZADA);

    private final ResolucionAcademicaGatewayIntPuerto gateway;
    private final SolicitudAcademicaGatewayIntPuerto solicitudGateway;
    private final AlmacenamientoAnexosIntPuerto almacenamiento;
    private final ValidadorActorSolicitud validadorActor;
    private final ValidadorArchivoAdjunto validadorArchivo;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final Clock reloj;

    public ResolucionAcademicaCUImplAdaptador(ResolucionAcademicaGatewayIntPuerto gateway,
                                              SolicitudAcademicaGatewayIntPuerto solicitudGateway,
                                              UsuarioGatewayIntPuerto usuarioGateway,
                                              AlmacenamientoAnexosIntPuerto almacenamiento,
                                              ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                              Clock reloj) {
        this.gateway = gateway;
        this.solicitudGateway = solicitudGateway;
        this.almacenamiento = almacenamiento;
        this.validadorActor = new ValidadorActorSolicitud(usuarioGateway, formateadorExcepciones);
        this.validadorArchivo = new ValidadorArchivoAdjunto(formateadorExcepciones);
        this.formateadorExcepciones = formateadorExcepciones;
        this.reloj = reloj;
    }

    @Override
    public ResolucionAcademica adjuntarResolucion(String uuidSolicitudAcademica, ArchivoAdjunto archivo, ActorSolicitud actor) {
        SolicitudAcademica solicitud = obtenerSolicitud(uuidSolicitudAcademica);
        validadorActor.validar(solicitud, actor);
        validarProceso(solicitud.getTipoSolicitudAcademica());
        if (actor.getRol() != RolEtiquetaEtapa.FUNCIONARIO)
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.RESOLUCION_SOLO_FUNCIONARIO);
        validarEtapa(solicitud);
        String extension = validadorArchivo.validar(archivo, DOCUMENTO, FORMATO);

        String uuidSolicitud = solicitud.getUuidSolicitudAcademica();
        ResolucionAcademica anterior = gateway.getPorSolicitud(uuidSolicitud);
        String ruta = null;
        try {
            ruta = almacenamiento.guardar(uuidSolicitud, UUID.randomUUID() + "." + extension, archivo.getContenido());
        } catch (RuntimeException error) {
            formateadorExcepciones.lanzarErrorGenerico(MensajesError.ERROR_GUARDANDO_ARCHIVO);
        }

        ResolucionAcademica resolucion = ResolucionAcademica.builder()
                .solicitudAcademica(solicitud)
                .urlArchivo(ruta)
                .nombreArchivo(validadorArchivo.nombreOriginalSeguro(archivo.getNombreOriginal(), extension))
                .fechaSubida(LocalDateTime.now(reloj))
                .funcionarioAcademico(FuncionarioAcademico.builder().uuidUsuario(actor.getUuidUsuario()).build())
                .build();
        ResolucionAcademica guardada;
        try {
            guardada = gateway.guardar(resolucion);
        } catch (RuntimeException error) {
            try {
                almacenamiento.eliminar(ruta);
            } catch (RuntimeException errorAlBorrar) {
                error.addSuppressed(errorAlBorrar);
            }
            throw error;
        }
        if (anterior != null)
            almacenamiento.eliminarTrasConfirmar(anterior.getUrlArchivo());
        return guardada;
    }

    @Override
    public ArchivoAdjunto obtenerResolucion(String uuidSolicitudAcademica, ActorSolicitud actor) {
        SolicitudAcademica solicitud = obtenerSolicitud(uuidSolicitudAcademica);
        validadorActor.validar(solicitud, actor);
        if (actor.getRol() == RolEtiquetaEtapa.ESTUDIANTE && !esFinal(solicitud))
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.RESOLUCION_NO_DISPONIBLE, solicitud.getRadicado()));

        ResolucionAcademica resolucion = gateway.getPorSolicitud(solicitud.getUuidSolicitudAcademica());
        if (resolucion == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, RESOLUCION, uuidSolicitudAcademica));
        byte[] contenido = almacenamiento.leer(resolucion.getUrlArchivo());
        if (contenido == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, ARCHIVO, uuidSolicitudAcademica));
        return ArchivoAdjunto.builder()
                .nombreOriginal(resolucion.getNombreArchivo())
                .tipoContenido(TIPO_CONTENIDO)
                .contenido(contenido)
                .build();
    }

    private SolicitudAcademica obtenerSolicitud(String uuidSolicitudAcademica) {
        SolicitudAcademica solicitud = tieneTexto(uuidSolicitudAcademica) ? solicitudGateway.getPorUuid(uuidSolicitudAcademica) : null;
        if (solicitud == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(
                    MensajesError.ENTIDAD_NO_ENCONTRADA, SOLICITUD_ACADEMICA, uuidSolicitudAcademica));
        return solicitud;
    }

    private void validarProceso(TipoSolicitudAcademica tipo) {
        TipoProcesoAcademico proceso = TipoProcesoAcademico.porNombre(tipo.getNombre());
        if (proceso == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.TIPO_SOLICITUD_SIN_PROCESO, tipo.getNombre()));
        if (proceso == TipoProcesoAcademico.EXAMEN_SUPLETORIO)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.RESOLUCION_SOLO_CANCELACIONES, tipo.getNombre()));
    }

    private void validarEtapa(SolicitudAcademica solicitud) {
        String etapa = codigoEtapa(solicitud);
        if (esFinal(solicitud))
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.RESOLUCION_ETAPA_FINAL, solicitud.getRadicado(), etapa));
        if (!ETAPAS_PERMITIDAS.contains(etapa))
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(
                    MensajesError.RESOLUCION_ETAPA_NO_PERMITIDA, String.join(", ", ETAPAS_PERMITIDAS), etapa));
    }

    private boolean esFinal(SolicitudAcademica solicitud) {
        return ETAPAS_FINALES.contains(codigoEtapa(solicitud));
    }

    private String codigoEtapa(SolicitudAcademica solicitud) {
        return solicitud.getEtapa() == null ? null : solicitud.getEtapa().getCodigo();
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
