package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.AnexoAcademicoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.dominio.servicios.ValidadorActorSolicitud;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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
    private static final int MAXIMO_NOMBRE = 255;
    private static final byte[] FIRMA_PDF = {'%', 'P', 'D', 'F', '-'};
    private static final byte[] FIRMA_PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] FIRMA_JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

    private final AnexoAcademicoGatewayIntPuerto anexoGateway;
    private final SolicitudAcademicaGatewayIntPuerto solicitudGateway;
    private final TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway;
    private final TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway;
    private final AlmacenamientoAnexosIntPuerto almacenamiento;
    private final ValidadorActorSolicitud validadorActor;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final Clock reloj;

    public AnexoAcademicoCUImplAdaptador(AnexoAcademicoGatewayIntPuerto anexoGateway,
                                         SolicitudAcademicaGatewayIntPuerto solicitudGateway,
                                         TipoSolicitudAcademicaGatewayIntPuerto tipoSolicitudGateway,
                                         TipoAnexoAcademicoGatewayIntPuerto tipoAnexoGateway,
                                         UsuarioGatewayIntPuerto usuarioGateway,
                                         AlmacenamientoAnexosIntPuerto almacenamiento,
                                         ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                         Clock reloj) {
        this.anexoGateway = anexoGateway;
        this.solicitudGateway = solicitudGateway;
        this.tipoSolicitudGateway = tipoSolicitudGateway;
        this.tipoAnexoGateway = tipoAnexoGateway;
        this.almacenamiento = almacenamiento;
        this.validadorActor = new ValidadorActorSolicitud(usuarioGateway, formateadorExcepciones);
        this.formateadorExcepciones = formateadorExcepciones;
        this.reloj = reloj;
    }

    @Override
    public AnexoAcademico adjuntarAnexo(String uuidSolicitudAcademica, String uuidTipoAnexoAcademico, ArchivoAdjunto archivo, ActorSolicitud actor) {
        SolicitudAcademica solicitud = obtenerSolicitud(uuidSolicitudAcademica);
        validadorActor.validar(solicitud, actor);
        TipoProcesoAcademico proceso = procesoDe(solicitud.getTipoSolicitudAcademica());
        TipoAnexoAcademico tipoAnexo = tipoAnexoDe(solicitud, uuidTipoAnexoAcademico, proceso);
        validarQuienYCuando(solicitud, tipoAnexo, actor);
        String extension = validarArchivo(archivo, tipoAnexo);

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
                .nombreArchivo(nombreOriginalSeguro(archivo.getNombreOriginal(), extension))
                .urlArchivo(ruta)
                .tipoArchivo(TIPOS_CONTENIDO.get(extension))
                .tamanioBytes((long) archivo.getContenido().length)
                .usuario(Usuario.builder().uuidUsuario(actor.getUuidUsuario()).build())
                .fechaSubida(LocalDateTime.now(reloj))
                .build();
        try {
            return anexoGateway.guardar(anexo);
        } catch (RuntimeException error) {
            try {
                almacenamiento.eliminar(ruta);
            } catch (RuntimeException errorAlBorrar) {
                error.addSuppressed(errorAlBorrar);
            }
            throw error;
        }
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
        return tipoAnexo;
    }

    private void validarQuienYCuando(SolicitudAcademica solicitud, TipoAnexoAcademico tipoAnexo, ActorSolicitud actor) {
        String nombre = tipoAnexo == null ? SOPORTE_LIBRE : tipoAnexo.getNombre();
        RolEtiquetaEtapa rolPermitido = RolEtiquetaEtapa.ESTUDIANTE;
        String etapaPermitida = RADICADA;
        if (RECIBO_PAGO.equals(nombre)) {
            rolPermitido = RolEtiquetaEtapa.FUNCIONARIO;
            etapaPermitida = APROBADA_POR_DECANO;
        } else if (COMPROBANTE_PAGO.equals(nombre)) {
            etapaPermitida = PENDIENTE_PAGO;
        }
        if (actor.getRol() != rolPermitido)
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.ANEXO_ACTOR_NO_PERMITIDO, nombre, rolPermitido));
        String etapaActual = solicitud.getEtapa() == null ? null : solicitud.getEtapa().getCodigo();
        if (!etapaPermitida.equals(etapaActual))
            formateadorExcepciones.lanzarReglaNegocioViolada(String.format(
                    MensajesError.ANEXO_ETAPA_NO_PERMITIDA, nombre, etapaPermitida, etapaActual));
    }

    private String validarArchivo(ArchivoAdjunto archivo, TipoAnexoAcademico tipoAnexo) {
        if (archivo == null || archivo.getContenido() == null || archivo.getContenido().length == 0)
            formateadorExcepciones.lanzarMalFormato(MensajesError.ARCHIVO_VACIO);
        long tamanio = archivo.getContenido().length;
        if (tamanio > TAMANIO_MAXIMO_BYTES)
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.ARCHIVO_MUY_GRANDE, tamanio, TAMANIO_MAXIMO_BYTES));

        String nombre = tipoAnexo == null ? SOPORTE_LIBRE : tipoAnexo.getNombre();
        String formatos = tipoAnexo == null ? FORMATOS_SOPORTE_LIBRE : tipoAnexo.getFormatosPermitidos();
        Set<String> permitidos = Arrays.stream(formatos == null ? new String[0] : formatos.split(","))
                .map(this::normalizarExtension)
                .collect(Collectors.toSet());
        String extension = normalizarExtension(extensionDe(archivo.getNombreOriginal()));
        if (extension.isEmpty() || !permitidos.contains(extension))
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.FORMATO_ANEXO_NO_PERMITIDO,
                    extension.isEmpty() ? "sin extension" : extension, nombre, formatos));
        if (!firmaCoincide(extension, archivo.getContenido()))
            formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.CONTENIDO_ANEXO_NO_COINCIDE, extension));
        return extension;
    }

    private String extensionDe(String nombreOriginal) {
        String nombre = nombreBase(nombreOriginal);
        int punto = nombre.lastIndexOf('.');
        return punto < 0 ? "" : nombre.substring(punto + 1);
    }

    private String normalizarExtension(String extension) {
        String limpia = extension == null ? "" : extension.trim().toLowerCase(Locale.ROOT);
        return "jpeg".equals(limpia) ? "jpg" : limpia;
    }

    private boolean firmaCoincide(String extension, byte[] contenido) {
        byte[] firma = switch (extension) {
            case "pdf" -> FIRMA_PDF;
            case "png" -> FIRMA_PNG;
            case "jpg" -> FIRMA_JPG;
            default -> null;
        };
        if (firma == null || contenido.length < firma.length) return false;
        for (int i = 0; i < firma.length; i++)
            if (contenido[i] != firma[i]) return false;
        return true;
    }

    private String nombreBase(String nombreOriginal) {
        if (nombreOriginal == null) return "";
        String nombre = nombreOriginal.replace('\\', '/');
        return nombre.substring(nombre.lastIndexOf('/') + 1).trim();
    }

    private String nombreOriginalSeguro(String nombreOriginal, String extension) {
        String nombre = nombreBase(nombreOriginal).replaceAll("\\p{Cntrl}", "");
        if (nombre.isBlank() || nombre.equals(".") || nombre.equals(".."))
            nombre = "anexo." + extension;
        return nombre.length() > MAXIMO_NOMBRE ? nombre.substring(nombre.length() - MAXIMO_NOMBRE) : nombre;
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
