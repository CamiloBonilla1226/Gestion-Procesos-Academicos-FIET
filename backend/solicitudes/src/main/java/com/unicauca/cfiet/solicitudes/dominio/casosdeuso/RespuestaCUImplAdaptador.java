package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.RespuestaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.ExcepcionesFormateadorIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.RespuestaGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.SolicitudGatewayIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Respuesta;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Solicitud;
import com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.almacenador.AlmacenadorArchivos;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Implementación de la interfaz de los casos de uso para la gestión de respuestas.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public class RespuestaCUImplAdaptador implements RespuestaCUIntPuerto {
    private final RespuestaGatewayIntPuerto gateway;
    private final SolicitudGatewayIntPuerto solicitudGateway;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final LogCUIntPuerto log;
    private final AlmacenadorArchivos almacenadorArchivos;
    /* Constantes */
    private static final String RESPUESTAS = "respuestas";
    private static final String RESPUESTA = "Respuesta";
    private static final String SOLICITUD = "Solicitud";

    @Value("${url.backend}")
    private String urlBackend;

    @Value("${url.application}")
    private String urlApplication;

    @Value("${app.uploads.base-path}")
    private String basePath;

    public RespuestaCUImplAdaptador(RespuestaGatewayIntPuerto gateway,
                                    SolicitudGatewayIntPuerto solicitudGateway,
                                    ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                    LogCUIntPuerto log,
                                    AlmacenadorArchivos almacenadorArchivos) {
        this.gateway = gateway;
        this.solicitudGateway = solicitudGateway;
        this.formateadorExcepciones = formateadorExcepciones;
        this.log = log;
        this.almacenadorArchivos = almacenadorArchivos;
    }

    @Override
    public PaginacionRespuestaDTO<Respuesta> getRespuestas(int pagina, int tamanio) {
        if (pagina < 0 || tamanio < 0)
            formateadorExcepciones.lanzarMalFormato(MensajesError.PAGINACION_ERROR);

        PaginacionRespuestaDTO<Respuesta> respuestas = gateway.getRespuestas(pagina, tamanio);
        if(respuestas.getContent().isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format(MensajesError.SIN_REGISTROS, RESPUESTAS));

        return respuestas;
    }

    @Override
    public PaginacionRespuestaDTO<Respuesta> getRespuestasPorNombreSolicitud(String nombreSolicitud, int pagina, int tamanio) {
        if (pagina < 0 || tamanio < 0)
            formateadorExcepciones.lanzarMalFormato(MensajesError.PAGINACION_ERROR);

        PaginacionRespuestaDTO<Respuesta> respuestas = gateway.getRespuestasPorNombreSolicitud(nombreSolicitud, pagina, tamanio);
        if(respuestas.getContent().isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format(MensajesError.SIN_REGISTROS, RESPUESTAS));

        return respuestas;
    }

    @Override
    public PaginacionRespuestaDTO<Respuesta> getRespuestasPorFuncionario(String uuidFuncionario, int pagina, int tamanio) {
        if (pagina < 0 || tamanio < 0)
            formateadorExcepciones.lanzarMalFormato(MensajesError.PAGINACION_ERROR);

        PaginacionRespuestaDTO<Respuesta> respuestas = gateway.getRespuestasPorFuncionario(uuidFuncionario, pagina, tamanio);
        if(respuestas.getContent().isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format(MensajesError.SIN_REGISTROS, RESPUESTAS));

        return respuestas;
    }

    @Override
    public PaginacionRespuestaDTO<Respuesta> getRespuestasPorFuncionarioNombreSolicitud(String uuidFuncionario, String nombreSolicitud, int pagina, int tamanio) {
        if (pagina < 0 || tamanio < 0)
            formateadorExcepciones.lanzarMalFormato(MensajesError.PAGINACION_ERROR);

        PaginacionRespuestaDTO<Respuesta> respuestas = gateway.getRespuestasPorFuncionarioNombreSolicitud(uuidFuncionario, nombreSolicitud,pagina, tamanio);
        if(respuestas.getContent().isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format(MensajesError.SIN_REGISTROS, RESPUESTAS));

        return respuestas;
    }

    @Override
    public Respuesta getRespuesta(String uuidRespuesta) {
        Respuesta respuesta = gateway.getRespuesta(uuidRespuesta);
        if(respuesta == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, RESPUESTA, uuidRespuesta));
        return  respuesta;
    }

    @Override
    public Respuesta getRespuestaPorSolicitud(String uuidSolicitud) {
        Respuesta respuesta = gateway.getRespuestaPorSolicitud(uuidSolicitud);
        if(respuesta == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, RESPUESTA, uuidSolicitud));
        return  respuesta;
    }

    @Override
    public Respuesta registrarRespuesta(String uuidSolicitud, Respuesta respuesta, String token) {
        Solicitud solicitud = solicitudGateway.getSolicitud(uuidSolicitud);
        if(solicitud == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, SOLICITUD, uuidSolicitud));

        if(!gateway.solicitudTieneRespuesta(uuidSolicitud)){
            // Creamos la respuesta
            if(!respuesta.esValidoTipoRespuesta())
                formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.TIPO_RESPUESTA_NO_VALIDO,  respuesta.getTipoRespuesta()));

            solicitud = solicitudGateway.getSolicitud(uuidSolicitud);

            String uuidRespuesta = UUID.randomUUID().toString();

            log.crearLog("Registrar respuesta de Solicitud", String.format("Se registro una respuesta para la solicitud: %s", solicitud.getNombre()), token);
            return gateway.guardarRespuesta(
                    Respuesta.builder()
                            .uuidRespuesta(uuidRespuesta)
                            .tipoRespuesta(respuesta.getTipoRespuesta())
                            .consecutivoFiet(respuesta.getConsecutivoFiet())
                            .respuestaConsejo(respuesta.getRespuestaConsejo())
                            .indicaciones(respuesta.getIndicaciones())
                            .solicitud(solicitud)
                            .build()
            );
        } else {
            Respuesta respuestaActual = getRespuestaPorSolicitud(uuidSolicitud);

            if(!respuesta.esValidoTipoRespuesta())
                formateadorExcepciones.lanzarReglaNegocioViolada(String.format(MensajesError.TIPO_RESPUESTA_NO_VALIDO,  respuesta.getTipoRespuesta()));

            respuestaActual.setTipoRespuesta(respuesta.getTipoRespuesta());
            respuestaActual.setConsecutivoFiet(respuesta.getConsecutivoFiet());
            respuestaActual.setRespuestaConsejo(respuesta.getRespuestaConsejo());
            respuestaActual.setIndicaciones(respuesta.getIndicaciones());
            log.crearLog("Modificar respuesta de Solicitud", String.format("Se modifico la respuesta con uuid: %s", respuesta.getUuidRespuesta()), token);
            return gateway.guardarRespuesta(respuestaActual);
        }
    }

    @Override
    public Respuesta responderSolicitud(String uuidRespuesta, MultipartFile respuesta, String token) {
        Respuesta respuestaOriginal = getRespuesta(uuidRespuesta);

        if (respuesta != null && !respuesta.isEmpty()) {
            String extension = "";
            String originalName = respuesta.getOriginalFilename();
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf(".") + 1).toUpperCase();
                if (!ApplicationConstantes.FORMATO_PDF.equals(extension) && !ApplicationConstantes.FORMATO_DOCX.equals(extension))
                    formateadorExcepciones.lanzarReglaNegocioViolada(MensajesError.FORMATO_RESPUESTA_NO_VALIDO);
            }

            try {
                if (respuestaOriginal.getUrlRespuesta() != null) {
                    File archivoAnterior = new File(basePath + "/respuestas/" + uuidRespuesta + "/" + new File(respuestaOriginal.getUrlRespuesta()).getName());
                    if (archivoAnterior.exists())
                        archivoAnterior.delete();
                }

                String rutaArchivo = almacenadorArchivos.guardarArchivoRespuesta(respuestaOriginal.getUuidRespuesta(), respuesta);

                String urlRespuesta = urlApplication + "respuestas/" + uuidRespuesta + "/" + new File(rutaArchivo).getName();
                respuestaOriginal.setUrlRespuesta(urlRespuesta);

            } catch (IOException e) {
                throw new RuntimeException(String.format("Error en el archivo de la respuesta (guardar o eliminar), error: %s", e.getMessage()), e);
            }
        }

        log.crearLog("Responder Solicitud", String.format("Se respondio la solicitud con respuesta con uuid : %s", respuestaOriginal.getUuidRespuesta()), token);
        return gateway.guardarRespuesta(respuestaOriginal);
    }

    @Override
    public Respuesta eliminarArchivoRespuesta(String uuidRespuesta, String token) {
        Respuesta respuesta = getRespuesta(uuidRespuesta);

        if (respuesta.getUrlRespuesta() == null || respuesta.getUrlRespuesta().isBlank())
            formateadorExcepciones.lanzarReglaNegocioViolada("La respuesta no tiene un archivo asociado para eliminar");

        try {
            String nombreArchivo = new File(respuesta.getUrlRespuesta()).getName();
            File archivo = new File(basePath + "/respuestas/" + uuidRespuesta + "/" + nombreArchivo);

            if (archivo.exists())
                archivo.delete();

            respuesta.setUrlRespuesta(null);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Error eliminando el archivo de la respuesta: %s", e.getMessage()), e);
        }

        log.crearLog("Eliminar archivo de respuesta", String.format("Se eliminó el archivo de la respuesta con uuid: %s", uuidRespuesta), token);
        return gateway.guardarRespuesta(respuesta);
    }

}
