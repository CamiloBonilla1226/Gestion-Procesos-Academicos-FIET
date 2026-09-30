package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.OrdenDelDiaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.OrdenDelDia;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Respuesta;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Solicitud;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import java.util.*;

/**
 * Implementación de la interfaz de los casos de uso para la gestión de ordenes del día.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public class OrdenDelDiaCUImplAdaptador implements OrdenDelDiaCUIntPuerto {
    private final OrdenDelDiaGatewayIntPuerto gateway;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final LogCUIntPuerto log;
    private final OrdenDelDiaExportador exportador;
    private final SolicitudGatewayIntPuerto gatewaySolicitud;
    private final RespuestaGatewayIntPuerto gatewayRespuesta;
    /* Constantes */
    private static final String ORDENES_DEL_DIA = "ordenes del día";
    private static final String ORDEN_DEL_DIA = "Orden del Día";
    private static final String PLANTILLA = "templates/orden_del_dia_info.docx";
    private static final String PLANTILLA_TEMAS_RESUELTOS = "templates/orden_del_dia_temas_resueltos.docx";

    public OrdenDelDiaCUImplAdaptador(OrdenDelDiaGatewayIntPuerto gateway,
                                      SolicitudGatewayIntPuerto gatewaySolicitud,
                                      ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                      LogCUIntPuerto log,
                                      OrdenDelDiaExportador exportador,
                                      RespuestaGatewayIntPuerto gatewayRespuesta){
        this.gateway = gateway;
        this.formateadorExcepciones = formateadorExcepciones;
        this.log = log;
        this.exportador = exportador;
        this.gatewaySolicitud = gatewaySolicitud;
        this.gatewayRespuesta = gatewayRespuesta;
    }

    @Override
    public List<OrdenDelDia> getOrdenesDelDia() {
        List<OrdenDelDia> respuesta = gateway.getOrdenesDelDia();
        if(respuesta.isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format(MensajesError.SIN_REGISTROS, ORDENES_DEL_DIA));
        return respuesta;
    }

    @Override
    public List<OrdenDelDia> getOrdenesDelDiaPorEstado(boolean estado) {
        List<OrdenDelDia> respuesta = gateway.getOrdenesDelDiaPorEstado(estado);
        if(respuesta.isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format(MensajesError.SIN_REGISTROS, ORDENES_DEL_DIA));
        return respuesta;
    }

    @Override
    public PaginacionRespuestaDTO<OrdenDelDia> getOrdenesDelDia(int pagina, int tamanio) {
        if (pagina < 0 || tamanio < 0)
            formateadorExcepciones.lanzarMalFormato(MensajesError.PAGINACION_ERROR);
        PaginacionRespuestaDTO<OrdenDelDia> respuesta = gateway.getOrdenesDelDia(pagina, tamanio);
        if (respuesta.getContent().isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format(MensajesError.SIN_REGISTROS, ORDENES_DEL_DIA));
        return respuesta;
    }

    @Override
    public OrdenDelDia getOrdenDelDia(String uuidOrdenDelDia) {
        OrdenDelDia ordenDelDia = gateway.getOrdenDelDia(uuidOrdenDelDia);
        if(ordenDelDia == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, ORDEN_DEL_DIA, uuidOrdenDelDia));
        return ordenDelDia;
    }

    @Override
    public OrdenDelDia crearOrdenDelDia(OrdenDelDia ordenDelDia, String token) {
        String uuidOrdenDelDia = UUID.randomUUID().toString();
        ordenDelDia.setUuidOrdenDelDia(uuidOrdenDelDia);
        ordenDelDia.setEstado(true);
        log.crearLog("Crear Orden del Día", String.format("Orden del día con uuid %s creada: %s", uuidOrdenDelDia, ordenDelDia.getNombre()), token);
        return gateway.guardarOrdenDelDia(ordenDelDia);
    }

    @Override
    public OrdenDelDia actualizarOrdenDelDia(String uuidOrdenDelDia, OrdenDelDia ordenDelDia, String token) {
        OrdenDelDia ordenDelDiaActualizar = getOrdenDelDia(uuidOrdenDelDia);
        ordenDelDiaActualizar.actualizar(ordenDelDia);
        log.crearLog("Actualizar Orden del Día", String.format("Orden del día con uuid %s actualizado: %s", uuidOrdenDelDia, ordenDelDia.getNombre()), token);
        return gateway.guardarOrdenDelDia(ordenDelDiaActualizar);
    }

    @Override
    public PaginacionRespuestaDTO<OrdenDelDia> buscarOrdenDelDiaPorNumeroActa(String nombre, int pagina, int tamanio) {
        PaginacionRespuestaDTO<OrdenDelDia> resultado = gateway.getOrdenesDelDia(nombre, pagina, tamanio);
        if (resultado.getContent().isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format("No existen órdenes del día cuyo nombre coincida con '%s'", nombre));
        return resultado;
    }

    @Override
    public byte[] generarOrdenDelDia(String uuidOrden) {
        OrdenDelDia ordenDelDia = getOrdenDelDia(uuidOrden);
        List<Solicitud> solicitudes = gatewaySolicitud.getSolicitudesPorOrdenDelDia(uuidOrden);

        HashMap<String, String> datosOrden = inicializarDatosOrden(ordenDelDia);
        if (!solicitudes.isEmpty())
            datosOrden.putAll(procesarSolicitudes(solicitudes, false));

        return exportador.exportarOrdenDelDia(datosOrden, PLANTILLA);
    }

    @Override
    public byte[] generarOrdenDelDiaConRespuestas(String uuidOrden) {
        OrdenDelDia ordenDelDia = getOrdenDelDia(uuidOrden);
        List<Solicitud> solicitudes = gatewaySolicitud.getSolicitudesPorOrdenDelDia(uuidOrden);

        HashMap<String, String> datosOrden = inicializarDatosOrden(ordenDelDia);
        if (!solicitudes.isEmpty())
            datosOrden.putAll(procesarSolicitudes(solicitudes, true));

        return exportador.exportarOrdenDelDia(datosOrden, PLANTILLA);
    }

    @Override
    public byte[] generarOrdenDelDiaMerge(String uuidOrden) {
        OrdenDelDia ordenDelDia = getOrdenDelDia(uuidOrden);
        List<Solicitud> solicitudes = gatewaySolicitud.getSolicitudesPorOrdenDelDia(uuidOrden);

        HashMap<String, String> datosOrden = inicializarDatosOrden(ordenDelDia);
        if (!solicitudes.isEmpty())
            datosOrden.putAll(procesarSolicitudesMerge(solicitudes));

        return exportador.exportarOrdenDelDia(datosOrden, PLANTILLA_TEMAS_RESUELTOS);
    }

    /**
     * Procesa las solicitudes para el merge: nombre + descripción + respuesta si existe
     */
    private HashMap<String, String> procesarSolicitudesMerge(List<Solicitud> solicitudes) {
        HashMap<String, StringBuilder> secciones = new HashMap<>();
        HashMap<String, StringBuilder> seccionesRespuesta = new HashMap<>();
        HashMap<String, Integer> contadores = new HashMap<>();

        for (Solicitud solicitud : solicitudes) {
            String seccion = solicitud.getObjTipoSolicitud() != null &&
                    solicitud.getObjTipoSolicitud().getSeccion() != null
                    ? solicitud.getObjTipoSolicitud().getSeccion().toLowerCase()
                    : "";

            secciones.putIfAbsent(seccion, new StringBuilder());
            seccionesRespuesta.putIfAbsent(seccion, new StringBuilder());
            contadores.putIfAbsent(seccion, 0);

            int subNumero = contadores.get(seccion) + 1;
            contadores.put(seccion, subNumero);

            int numeroSeccion;
            switch (seccion) {
                case ApplicationConstantes.ASUNTOS_DECANO: numeroSeccion = 1; break;
                case ApplicationConstantes.ASUNTOS_PREGRADO: numeroSeccion = 2; break;
                case ApplicationConstantes.ASUNTOS_POSGRADOS: numeroSeccion = 3; break;
                case ApplicationConstantes.ASUNTOS_DELEGADOS_EN_DECANO: numeroSeccion = 4; break;
                case ApplicationConstantes.SOLICITUD_COMISION_ACADEMICA_INTERIOR_PAIS: numeroSeccion = 5; break;
                case ApplicationConstantes.SOLICITUD_COMISION_ACADEMICA_EXTERIOR_PAIS: numeroSeccion = 6; break;
                case ApplicationConstantes.INFORME_COMISION_ACADEMICA: numeroSeccion = 7; break;
                case ApplicationConstantes.ASUNTOS_VARIOS: numeroSeccion = 8; break;
                default: continue;
            }

            StringBuilder contenido = secciones.get(seccion);
            StringBuilder contenidoRespuesta = seccionesRespuesta.get(seccion);

            // Nombre + descripción para la sección original
            String nombre = solicitud.getNombre() != null ? solicitud.getNombre() : "";
            String descripcion = solicitud.getDescripcion() != null ? solicitud.getDescripcion() : "";
            contenido.append(numeroSeccion).append(".").append(subNumero)
                    .append(" ").append(nombre)
                    .append("\n").append(descripcion)
                    .append("\n\n");

            // Nombre + descripción + respuesta para la sección RESPUESTA
            Respuesta respuesta = gatewayRespuesta.getRespuestaPorSolicitud(solicitud.getUuidSolicitud());
            String respuestaConsejo = respuesta != null && respuesta.getRespuestaConsejo() != null
                    ? respuesta.getRespuestaConsejo()
                    : "";

            contenidoRespuesta.append(numeroSeccion).append(".").append(subNumero)
                    .append(" ").append(nombre)
                    .append("\n").append(descripcion);
            if (!respuestaConsejo.isEmpty()) {
                contenidoRespuesta.append("\n").append(respuestaConsejo);
            }
            contenidoRespuesta.append("\n\n");
        }

        // Mapeo final
        HashMap<String, String> resultado = new HashMap<>();
        for (String seccion : Arrays.asList(
                ApplicationConstantes.ASUNTOS_DECANO,
                ApplicationConstantes.ASUNTOS_PREGRADO,
                ApplicationConstantes.ASUNTOS_POSGRADOS,
                ApplicationConstantes.ASUNTOS_DELEGADOS_EN_DECANO,
                ApplicationConstantes.SOLICITUD_COMISION_ACADEMICA_INTERIOR_PAIS,
                ApplicationConstantes.SOLICITUD_COMISION_ACADEMICA_EXTERIOR_PAIS,
                ApplicationConstantes.INFORME_COMISION_ACADEMICA,
                ApplicationConstantes.ASUNTOS_VARIOS)) {

            String contenido = secciones.getOrDefault(seccion, new StringBuilder()).toString().trim();
            String contenidoResp = seccionesRespuesta.getOrDefault(seccion, new StringBuilder()).toString().trim();

            switch (seccion) {
                case ApplicationConstantes.ASUNTOS_DECANO:
                    resultado.put("{{ASUNTOS_DECANO}}", contenido);
                    resultado.put("{{ASUNTOS_DECANO_RESPUESTA}}", contenidoResp);
                    break;
                case ApplicationConstantes.ASUNTOS_PREGRADO:
                    resultado.put("{{ASUNTOS_PREGRADO}}", contenido);
                    resultado.put("{{ASUNTOS_PREGRADO_RESPUESTA}}", contenidoResp);
                    break;
                case ApplicationConstantes.ASUNTOS_POSGRADOS:
                    resultado.put("{{ASUNTOS_POSGRADOS}}", contenido);
                    resultado.put("{{ASUNTOS_POSGRADOS_RESPUESTA}}", contenidoResp);
                    break;
                case ApplicationConstantes.ASUNTOS_DELEGADOS_EN_DECANO:
                    resultado.put("{{ASUNTOS_DELEGADOS_DECANO}}", contenido);
                    resultado.put("{{ASUNTOS_DELEGADOS_DECANO_RESPUESTA}}", contenidoResp);
                    break;
                case ApplicationConstantes.SOLICITUD_COMISION_ACADEMICA_INTERIOR_PAIS:
                    resultado.put("{{SOLICITUDES_COMISION_ACADEMICA_INTERIOR}}", contenido);
                    resultado.put("{{SOLICITUDES_COMISION_ACADEMICA_INTERIOR_RESPUESTA}}", contenidoResp);
                    break;
                case ApplicationConstantes.SOLICITUD_COMISION_ACADEMICA_EXTERIOR_PAIS:
                    resultado.put("{{SOLICITUDES_COMISION_ACADEMICA_EXTERIOR}}", contenido);
                    resultado.put("{{SOLICITUDES_COMISION_ACADEMICA_EXTERIOR_RESPUESTA}}", contenidoResp);
                    break;
                case ApplicationConstantes.INFORME_COMISION_ACADEMICA:
                    resultado.put("{{INFORME_COMISION_ACADEMICA}}", contenido);
                    resultado.put("{{INFORME_COMISION_ACADEMICA_RESPUESTA}}", contenidoResp);
                    break;
                case ApplicationConstantes.ASUNTOS_VARIOS:
                    resultado.put("{{ASUNTOS_VARIOS}}", contenido);
                    resultado.put("{{ASUNTOS_VARIOS_RESPUESTA}}", contenidoResp);
                    break;
            }
        }

        return resultado;
    }

    /**
     * Extrae los componentes de la fecha en formato AAAA-MM-DD
     */
    private HashMap<String, String> extraerFecha(String fecha) {
        HashMap<String, String> partes = new HashMap<>();
        partes.put("DIA", "");
        partes.put("MES", "");
        partes.put("ANIO", "");

        if (fecha != null && fecha.contains("-")) {
            String[] split = fecha.split("-");
            if (split.length == 3) {
                partes.put("ANIO", split[0] != null ? split[0] : "");
                partes.put("MES", split[1] != null ? split[1] : "");
                partes.put("DIA", split[2] != null ? split[2] : "");
            }
        }
        return partes;
    }

    /**
     * Inicializa el mapa de datos de la orden con información básica y secciones vacías
     */
    private HashMap<String, String> inicializarDatosOrden(OrdenDelDia ordenDelDia) {
        HashMap<String, String> datosOrden = new HashMap<>();
        HashMap <String, String> fecha = extraerFecha(ordenDelDia.getFecha());

        datosOrden.put("{{CIUDAD}}", ordenDelDia.getCiudad() != null ? ordenDelDia.getCiudad() : "");
        datosOrden.put("{{DIA}}", fecha.get("DIA"));
        datosOrden.put("{{MES}}", fecha.get("MES"));
        datosOrden.put("{{ANIO}}", fecha.get("ANIO"));
        datosOrden.put("{{HORA_INICIO}}", ordenDelDia.getHoraInicio() != null ? ordenDelDia.getHoraInicio() : "");
        datosOrden.put("{{HORA_FIN}}", ordenDelDia.getHoraFin() != null ? ordenDelDia.getHoraFin() : "");
        datosOrden.put("{{LUGAR}}", ordenDelDia.getLugarReunion() != null ? ordenDelDia.getLugarReunion() : "");
        datosOrden.put("{{ACTA}}", ordenDelDia.getNumeroActa() != null ? ordenDelDia.getNumeroActa() : "");

        // Inicializamos todas las secciones vacías
        datosOrden.put("{{ASUNTOS_DECANO}}", "");
        datosOrden.put("{{ASUNTOS_PREGRADO}}", "");
        datosOrden.put("{{ASUNTOS_POSGRADOS}}", "");
        datosOrden.put("{{ASUNTOS_DELEGADOS_DECANO}}", "");
        datosOrden.put("{{SOLICITUDES_COMISION_ACADEMICA_INTERIOR}}", "");
        datosOrden.put("{{SOLICITUDES_COMISION_ACADEMICA_EXTERIOR}}", "");
        datosOrden.put("{{INFORME_COMISION_ACADEMICA}}", "");
        datosOrden.put("{{ASUNTOS_VARIOS}}", "");

        return datosOrden;
    }

    /**
     * Procesa las solicitudes y genera el contenido de las secciones.
     * @param solicitudes Lista de solicitudes
     * @param incluirRespuestas True si se deben incluir respuestas del consejo
     */
    private HashMap<String, String> procesarSolicitudes(List<Solicitud> solicitudes, boolean incluirRespuestas) {
        HashMap<String, StringBuilder> secciones = new HashMap<>();
        HashMap<String, Integer> contadores = new HashMap<>();

        for (Solicitud solicitud : solicitudes) {
            String seccion = solicitud.getObjTipoSolicitud() != null &&
                    solicitud.getObjTipoSolicitud().getSeccion() != null
                    ? solicitud.getObjTipoSolicitud().getSeccion().toLowerCase()
                    : "";

            secciones.putIfAbsent(seccion, new StringBuilder());
            contadores.putIfAbsent(seccion, 0);

            int subNumero = contadores.get(seccion) + 1;
            contadores.put(seccion, subNumero);

            int numeroSeccion;
            switch (seccion) {
                case ApplicationConstantes.ASUNTOS_DECANO: numeroSeccion = 1; break;
                case ApplicationConstantes.ASUNTOS_PREGRADO: numeroSeccion = 2; break;
                case ApplicationConstantes.ASUNTOS_POSGRADOS: numeroSeccion = 3; break;
                case ApplicationConstantes.ASUNTOS_DELEGADOS_EN_DECANO: numeroSeccion = 4; break;
                case ApplicationConstantes.SOLICITUD_COMISION_ACADEMICA_INTERIOR_PAIS: numeroSeccion = 5; break;
                case ApplicationConstantes.SOLICITUD_COMISION_ACADEMICA_EXTERIOR_PAIS: numeroSeccion = 6; break;
                case ApplicationConstantes.INFORME_COMISION_ACADEMICA: numeroSeccion = 7; break;
                case ApplicationConstantes.ASUNTOS_VARIOS: numeroSeccion = 8; break;
                default: continue;
            }

            StringBuilder contenido = secciones.get(seccion);

            String respuestaConsejo = "";
            if (incluirRespuestas) {
                Respuesta respuesta = gatewayRespuesta.getRespuestaPorSolicitud(solicitud.getUuidSolicitud());
                if (respuesta != null) respuestaConsejo = respuesta.getRespuestaConsejo();
            }

            contenido.append(numeroSeccion)
                    .append(".")
                    .append(subNumero)
                    .append(" ")
                    .append(solicitud.getNombre() != null ? solicitud.getNombre() : "")
                    .append("\n")
                    .append(solicitud.getDescripcion() != null ? solicitud.getDescripcion() : "")
                    .append("\n");

            if (incluirRespuestas) contenido.append(respuestaConsejo != null ? respuestaConsejo : "");

            contenido.append("\n\n");
        }

        HashMap<String, String> resultado = new HashMap<>();
        secciones.forEach((seccion, texto) -> {
            switch (seccion) {
                case ApplicationConstantes.ASUNTOS_DECANO:
                    resultado.put("{{ASUNTOS_DECANO}}", texto.toString().trim());
                    break;
                case ApplicationConstantes.ASUNTOS_PREGRADO:
                    resultado.put("{{ASUNTOS_PREGRADO}}", texto.toString().trim());
                    break;
                case ApplicationConstantes.ASUNTOS_POSGRADOS:
                    resultado.put("{{ASUNTOS_POSGRADOS}}", texto.toString().trim());
                    break;
                case ApplicationConstantes.ASUNTOS_DELEGADOS_EN_DECANO:
                    resultado.put("{{ASUNTOS_DELEGADOS_DECANO}}", texto.toString().trim());
                    break;
                case ApplicationConstantes.SOLICITUD_COMISION_ACADEMICA_INTERIOR_PAIS:
                    resultado.put("{{SOLICITUDES_COMISION_ACADEMICA_INTERIOR}}", texto.toString().trim());
                    break;
                case ApplicationConstantes.SOLICITUD_COMISION_ACADEMICA_EXTERIOR_PAIS:
                    resultado.put("{{SOLICITUDES_COMISION_ACADEMICA_EXTERIOR}}", texto.toString().trim());
                    break;
                case ApplicationConstantes.INFORME_COMISION_ACADEMICA:
                    resultado.put("{{INFORME_COMISION_ACADEMICA}}", texto.toString().trim());
                    break;
                case ApplicationConstantes.ASUNTOS_VARIOS:
                    resultado.put("{{ASUNTOS_VARIOS}}", texto.toString().trim());
                    break;
            }
        });

        return resultado;
    }

}
