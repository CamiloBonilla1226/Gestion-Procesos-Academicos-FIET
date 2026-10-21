package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.AnexoAcademicoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.ValidadorActorSolicitud;
import com.unicauca.cfiet.solicitudes.dominio.servicios.ValidadorArchivoAdjunto;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.AnexoAcademicoConstantes.*;
import static com.unicauca.cfiet.solicitudes.dominio.helper.constantes.EtapaSolicitudAcademicaConstantes.*;

public class AnexoAcademicoCUImplAdaptador implements AnexoAcademicoCUIntPuerto {
    private static final String SOLICITUD_ACADEMICA = "Solicitud académica";
    private static final String TIPO_SOLICITUD_ACADEMICA = "Tipo de solicitud académica";
    private static final String TIPO_ANEXO_ACADEMICO = "Tipo de anexo académico";
    private static final String ANEXO_ACADEMICO = "Anexo académico";
    private static final String ARCHIVO = "Archivo del anexo";
    private static final String SOPORTE_LIBRE = "Soporte libre";
    private static final List<String> ETAPAS_ADICIONAL_CANCELACION = List.of(RADICADA, APROBADA_POR_DECANO, RECHAZADA_POR_DECANO);
    private static final List<String> ETAPAS_ADICIONAL_SUPLETORIO = List.of(RADICADA, RECHAZADA_POR_DECANO, EN_VERIFICACION_PAGO);

    private final AnexoAcademicoGatewayIntPuerto anexoGateway;
    private final SolicitudAcademicaGatewayIntPuerto solicitudGateway;
    private final TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway;
    private final TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway;
    private final AlmacenamientoAnexosIntPuerto almacenamiento;
    private final ValidadorActorSolicitud validadorActor;
    private final ValidadorArchivoAdjunto validadorArchivo;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final LogCUIntPuerto log;
    private final Clock reloj;

    public AnexoAcademicoCUImplAdaptador(AnexoAcademicoGatewayIntPuerto anexoGateway,
                                         SolicitudAcademicaGatewayIntPuerto solicitudGateway,
                                         TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                         TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway,
                                         UsuarioGatewayIntPuerto usuarioGateway,
                                         AlmacenamientoAnexosIntPuerto almacenamiento,
                                         ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                         LogCUIntPuerto log,
                                         Clock reloj) {
        this.anexoGateway = anexoGateway;
        this.solicitudGateway = solicitudGateway;
        this.tipoSolicitudGateway = tipoSolicitudGateway;
        this.tipoAnexoGateway = tipoAnexoGateway;
        this.almacenamiento = almacenamiento;
        this.validadorActor = new ValidadorActorSolicitud(usuarioGateway, formateadorExcepciones);
        this.validadorArchivo = new ValidadorArchivoAdjunto(formateadorExcepciones);
        this.formateadorExcepciones = formateadorExcepciones;
        this.log = log;
        this.reloj = reloj;
    }

    @Override
    public AnexoAcademico adjuntarAnexo(String uuidSolicitudAcademica, String uuidTipoAnexoAcademico, ArchivoAdjunto archivo, ActorSolicitud actor,
                                        String token) {
        SolicitudAcademica solicitud = obtenerSolicitud(uuidSolicitudAcademica);
        validadorActor.validar(solicitud, actor);
        TipoProcesoAcademico proceso = procesoDe(solicitud.getTipoSolicitudAcademica());
        TipoAnexoAcademico tipoAnexo = tipoAnexoDe(solicitud, uuidTipoAnexoAcademico, proceso);
        validarQuienYCuando(solicitud, tipoAnexo, actor, proceso);
        List<AnexoAcademico> reemplazados = esDocumentoAdicional(tipoAnexo) ? documentosAdicionales(solicitud) : List.of();
        String extension = validadorArchivo.validar(archivo, tipoAnexo == null ? SOPORTE_LIBRE : tipoAnexo.getNombre(),
                tipoAnexo == null ? FORMATOS_SOPORTE_LIBRE : tipoAnexo.getFormatosPermitidos(),
                tamanioMaximoDe(tipoAnexo == null ? null : tipoAnexo.getNombre()));

        String uuidAnexo = UUID.randomUUID().toString();
        String ruta = null;
        try {
            ruta = almacenamiento.guardar(solicitud.getUuidSolicitudAcademica(), uuidAnexo + "." + extension, archivo.getContenido());
        } catch (RuntimeException error) {
            formateadorExcepciones.lanzarErrorGenerico(MensajesError.ERROR_GUARDANDO_ARCHIVO);
        }

        AnexoAcademico anexo = AnexoAcademico.builder()
                .uuidAnexoAcademico(uuidAnexo)
                .solicitudAcademica(solicitud)
                .tipoAnexoAcademico(tipoAnexo)
                .nombreArchivo(validadorArchivo.nombreOriginalSeguro(archivo.getNombreOriginal(), extension))
                .urlArchivo(ruta)
                .tipoArchivo(TIPOS_CONTENIDO.get(extension))
                .tamanioBytes((long) archivo.getContenido().length)
                .usuario(Usuario.builder().uuidUsuario(actor.getUuidUsuario()).build())
                .fechaSubida(LocalDateTime.now(reloj))
                .build();
        AnexoAcademico guardado;
        try {
            guardado = anexoGateway.guardar(anexo);
        } catch (RuntimeException error) {
            try {
                almacenamiento.eliminar(ruta);
            } catch (RuntimeException errorAlBorrar) {
                error.addSuppressed(errorAlBorrar);
            }
            throw error;
        }
        for (AnexoAcademico anterior : reemplazados) {
            anexoGateway.eliminar(anterior.getUuidAnexoAcademico());
            almacenamiento.eliminarTrasConfirmar(anterior.getUrlArchivo());
        }
        log.crearLog("Adjuntar anexo académico",
                String.format("Solicitud académica %s, acción ADJUNTAR_ANEXO: %s (%s)", solicitud.getRadicado(),
                        tipoAnexo == null ? SOPORTE_LIBRE : tipoAnexo.getNombre(), uuidAnexo),
                token);
        return guardado;
    }

    @Override
    public ArchivoAdjunto obtenerAnexo(String uuidAnexoAcademico, ActorSolicitud actor) {
        AnexoAcademico anexo = tieneTexto(uuidAnexoAcademico) ? anexoGateway.getPorUuid(uuidAnexoAcademico) : null;
        if (anexo == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, ANEXO_ACADEMICO, uuidAnexoAcademico));
        SolicitudAcademica solicitud = obtenerSolicitud(anexo.getSolicitudAcademica().getUuidSolicitudAcademica());
        validadorActor.validar(solicitud, actor);

        byte[] contenido = almacenamiento.leer(anexo.getUrlArchivo());
        if (contenido == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, ARCHIVO, uuidAnexoAcademico));
        return ArchivoAdjunto.builder()
                .nombreOriginal(anexo.getNombreArchivo())
                .tipoContenido(anexo.getTipoArchivo())
                .contenido(contenido)
                .build();
    }

    @Override
    public List<TipoAnexoAcademico> getAnexosObligatorios(String uuidTipoSolicitudAcademica, CausaSupletorio causa) {
        TipoSolicitudAcademica tipo = tieneTexto(uuidTipoSolicitudAcademica) ? tipoSolicitudGateway.getPorUuid(uuidTipoSolicitudAcademica) : null;
        if (tipo == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(
                    MensajesError.ENTIDAD_NO_ENCONTRADA, TIPO_SOLICITUD_ACADEMICA, uuidTipoSolicitudAcademica));
        return requeridos(tipo, causa);
    }

    @Override
    public List<TipoAnexoAcademico> verificarAnexosObligatorios(String uuidSolicitudAcademica, CausaSupletorio causa) {
        SolicitudAcademica solicitud = obtenerSolicitud(uuidSolicitudAcademica);
        List<TipoAnexoAcademico> requeridos = requeridos(solicitud.getTipoSolicitudAcademica(), causa);
        Set<String> adjuntados = anexoGateway.getPorSolicitud(solicitud.getUuidSolicitudAcademica()).stream()
                .filter(anexo -> anexo.getTipoAnexoAcademico() != null)
                .map(anexo -> anexo.getTipoAnexoAcademico().getUuidTipoAnexoAcademico())
                .collect(Collectors.toSet());
        return requeridos.stream()
                .filter(tipoAnexo -> !adjuntados.contains(tipoAnexo.getUuidTipoAnexoAcademico()))
                .toList();
    }

    private List<TipoAnexoAcademico> requeridos(TipoSolicitudAcademica tipo, CausaSupletorio causa) {
        TipoProcesoAcademico proceso = procesoDe(tipo);
        boolean supletorio = proceso == TipoProcesoAcademico.EXAMEN_SUPLETORIO;
        if (supletorio && causa == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.CAUSA_SUPLETORIO_REQUERIDA);
        return tipoAnexoGateway.getPorTipo(tipo.getUuidTipoSolicitudAcademica()).stream()
                .filter(TipoAnexoAcademico::estaVigente)
                .filter(tipoAnexo -> Boolean.TRUE.equals(tipoAnexo.getObligatorio())
                        || (supletorio && causa == CausaSupletorio.OTRA && SOPORTE_JUSTIFICACION.equals(tipoAnexo.getNombre()))
                        || (supletorio && causa == CausaSupletorio.CRUCE && FORMATO_DOCENTE_CRUCE.equals(tipoAnexo.getNombre())))
                .toList();
    }

    private SolicitudAcademica obtenerSolicitud(String uuidSolicitudAcademica) {
        SolicitudAcademica solicitud = tieneTexto(uuidSolicitudAcademica) ? solicitudGateway.getPorUuid(uuidSolicitudAcademica) : null;
        if (solicitud == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(
                    MensajesError.ENTIDAD_NO_ENCONTRADA, SOLICITUD_ACADEMICA, uuidSolicitudAcademica));
        return solicitud;
    }

    private TipoAnexoAcademico tipoAnexoDe(SolicitudAcademica solicitud, String uuidTipoAnexoAcademico, TipoProcesoAcademico proceso) {
        TipoSolicitudAcademica tipoSolicitud = solicitud.getTipoSolicitudAcademica();
        if (!tieneTexto(uuidTipoAnexoAcademico)) {
            if (proceso == TipoProcesoAcademico.EXAMEN_SUPLETORIO)
                formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.SOPORTE_LIBRE_NO_PERMITIDO, tipoSolicitud.getNombre()));
            return null;
        }
        TipoAnexoAcademico tipoAnexo = tipoAnexoGateway.getPorUuid(uuidTipoAnexoAcademico);
        if (tipoAnexo == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(
                    MensajesError.ENTIDAD_NO_ENCONTRADA, TIPO_ANEXO_ACADEMICO, uuidTipoAnexoAcademico));
        String tipoDelAnexo = tipoAnexo.getTipoSolicitudAcademica() == null ? null
                : tipoAnexo.getTipoSolicitudAcademica().getUuidTipoSolicitudAcademica();
        if (!tipoSolicitud.getUuidTipoSolicitudAcademica().equals(tipoDelAnexo))
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(
                    MensajesError.ANEXO_DE_OTRO_TIPO, tipoAnexo.getNombre(), tipoSolicitud.getNombre()));
        if (esDocumentoAdicional(tipoAnexo) && !proceso.admiteDocumentoAdicional())
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.DOCUMENTO_ADICIONAL_NO_ADMITIDO,
                    tipoAnexo.getNombre(), tipoSolicitud.getNombre()));
        if (!tipoAnexo.estaVigente())
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ANEXO_NO_VIGENTE, tipoAnexo.getNombre(),
                    tipoSolicitud.getNombre(), String.join("; ", tipoAnexoGateway.getPorTipo(tipoSolicitud.getUuidTipoSolicitudAcademica()).stream()
                            .filter(vigente -> vigente.estaVigente() && Boolean.TRUE.equals(vigente.getObligatorio()))
                            .map(TipoAnexoAcademico::getNombre)
                            .toList())));
        return tipoAnexo;
    }

    private void validarQuienYCuando(SolicitudAcademica solicitud, TipoAnexoAcademico tipoAnexo, ActorSolicitud actor,
                                     TipoProcesoAcademico proceso) {
        String nombre = tipoAnexo == null ? SOPORTE_LIBRE : tipoAnexo.getNombre();
        RolEtiquetaEtapa rolPermitido = RolEtiquetaEtapa.ESTUDIANTE;
        List<String> etapasPermitidas = List.of(RADICADA);
        if (RECIBO_PAGO.equals(nombre)) {
            rolPermitido = RolEtiquetaEtapa.FUNCIONARIO;
            etapasPermitidas = List.of(APROBADA_POR_DECANO);
        } else if (COMPROBANTE_PAGO.equals(nombre)) {
            etapasPermitidas = List.of(PENDIENTE_PAGO);
        } else if (esDocumentoAdicional(tipoAnexo)) {
            rolPermitido = RolEtiquetaEtapa.FUNCIONARIO;
            etapasPermitidas = proceso == TipoProcesoAcademico.EXAMEN_SUPLETORIO ? ETAPAS_ADICIONAL_SUPLETORIO : ETAPAS_ADICIONAL_CANCELACION;
        }
        if (actor.getRol() != rolPermitido)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ANEXO_ACTOR_NO_PERMITIDO, nombre, rolPermitido));
        String etapaActual = solicitud.getEtapa() == null ? null : solicitud.getEtapa().getCodigo();
        if (!etapasPermitidas.contains(etapaActual))
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(
                    MensajesError.ANEXO_ETAPA_NO_PERMITIDA, nombre, String.join(", ", etapasPermitidas), etapaActual));
    }

    private boolean esDocumentoAdicional(TipoAnexoAcademico tipoAnexo) {
        return tipoAnexo != null && DOCUMENTO_ADICIONAL.equals(tipoAnexo.getNombre());
    }

    private List<AnexoAcademico> documentosAdicionales(SolicitudAcademica solicitud) {
        return anexoGateway.getPorSolicitud(solicitud.getUuidSolicitudAcademica()).stream()
                .filter(anexo -> esDocumentoAdicional(anexo.getTipoAnexoAcademico()))
                .toList();
    }

    private TipoProcesoAcademico procesoDe(TipoSolicitudAcademica tipo) {
        TipoProcesoAcademico proceso = TipoProcesoAcademico.porNombre(tipo.getNombre());
        if (proceso == null)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.TIPO_SOLICITUD_SIN_PROCESO, tipo.getNombre()));
        return proceso;
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
