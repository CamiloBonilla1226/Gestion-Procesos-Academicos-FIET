package com.unicauca.cfiet.solicitudes.dominio.casosdeuso;

import com.unicauca.cfiet.solicitudes.aplicacion.input.LogCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.SolicitudCUintPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.output.*;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.*;
import com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.almacenador.AlmacenadorArchivos;
import com.unicauca.cfiet.solicitudes.infraestructura.output.manejadorExcepciones.MensajesError;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Implementación de la interfaz de los casos de uso para la gestión de solicitudes.
 *
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
public class SolicitudCUImplAdaptador implements SolicitudCUintPuerto {
    private final SolicitudGatewayIntPuerto gateway;
    private final TipoSolicitudGatewayIntPuerto gatewayTipoSolicitud;
    private final OrdenDelDiaGatewayIntPuerto gatewayOrdenDelDia;
    private final UsuarioGatewayIntPuerto gatewayUsuario;
    private final SesionGatewayIntPuerto gatewaySesion;
    private final ExcepcionesFormateadorIntPuerto formateadorExcepciones;
    private final LogCUIntPuerto log;
    private final IJwtServicio jwtServicio;
    private final AlmacenadorArchivos almacenadorArchivos;
    @Value("${url.backend}")
    private String urlBackend;

    @Value("${url.application}")
    private String urlApplication;

    /* Constantes */
    private static final String SOLICITUD_CONTROLLER_URL = "solicitudes/anexos/";
    private static final String SOLICITUDES = "Solicitudes";
    private static final String SOLICITUD = "Solicitud";
    private static final String ORDEN_DEL_DIA = "Orden del Día";
    private static final String TIPOS_SOLICITUD = "tipos de solicitud";
    private static final String USUARIO = "Usuario";

    public SolicitudCUImplAdaptador(SolicitudGatewayIntPuerto gateway,
                                    ExcepcionesFormateadorIntPuerto formateadorExcepciones,
                                    TipoSolicitudGatewayIntPuerto gatewayTipoSolicitud,
                                    OrdenDelDiaGatewayIntPuerto gatewayOrdenDelDia,
                                    UsuarioGatewayIntPuerto gatewayUsuario,
                                    LogCUIntPuerto log,
                                    SesionGatewayIntPuerto gatewaySesion,
                                    IJwtServicio jwtServicio,
                                    AlmacenadorArchivos almacenadorArchivos){
        this.gateway = gateway;
        this.gatewayTipoSolicitud = gatewayTipoSolicitud;
        this.gatewayOrdenDelDia = gatewayOrdenDelDia;
        this.formateadorExcepciones = formateadorExcepciones;
        this.gatewayUsuario = gatewayUsuario;
        this.log = log;
        this.gatewaySesion = gatewaySesion;
        this.jwtServicio = jwtServicio;
        this.almacenadorArchivos = almacenadorArchivos;
    }

    @Override
    public List<Solicitud> getSolicitudes() {
        List<Solicitud> respuesta = gateway.getSolicitudes();
        if(respuesta.isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format(MensajesError.SIN_REGISTROS, SOLICITUDES));
        return respuesta;
    }

    @Override
    public PaginacionRespuestaDTO<Solicitud> getSolicitudes(int pagina, int tamanio) {
        if (pagina < 0 || tamanio < 0)
            formateadorExcepciones.lanzarMalFormato(MensajesError.PAGINACION_ERROR);
        PaginacionRespuestaDTO<Solicitud> respuesta = gateway.getSolicitudes(pagina, tamanio);
        if (respuesta.getContent().isEmpty())
            formateadorExcepciones.lanzarSinInformacion(String.format(MensajesError.SIN_REGISTROS, SOLICITUDES));
        return respuesta;
    }

    @Override
    public Solicitud getSolicitud(String uuidSolicitud) {
        Solicitud solicitud = gateway.getSolicitud(uuidSolicitud);
        if(solicitud == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, SOLICITUD, uuidSolicitud));
        return  solicitud;
    }

    @Override
    public Solicitud crearSolicitud(Solicitud solicitud, String token) {
        checkSolicitud(solicitud, token);
        String uuidSolicitud = UUID.randomUUID().toString();
        solicitud.setUuidSolicitud(uuidSolicitud);
        solicitud.getInformacionSolicitante().setUuidInformacionSolicitante(UUID.randomUUID().toString());
        solicitud.getInformacionSolicitante().setSolicitud(solicitud);

        for(Anexo currentAnexo : solicitud.getAnexos()){
            currentAnexo.setUuidAnexo(UUID.randomUUID().toString());
            currentAnexo.setObjSolicitud(solicitud);

            MultipartFile archivo = currentAnexo.getAnexoFile();
            if (archivo != null && !archivo.isEmpty()) {
                try {
                    String rutaArchivo = almacenadorArchivos.guardarArchivo(uuidSolicitud, archivo, currentAnexo.getNombre());
                    String urlAnexo =  urlApplication + SOLICITUD_CONTROLLER_URL
                            + uuidSolicitud + "/" + new File(rutaArchivo).getName();
                    currentAnexo.setUrlAnexo(urlAnexo);
                } catch (IOException e) {
                    throw new RuntimeException("Error guardando el archivo del anexo: " + currentAnexo.getNombre(), e);
                }
            }
        }

        log.crearLog("Solicitud Creada", String.format("Se realizó una solicitud  %s" , solicitud.getNombre()), token);
        return gateway.guardarSolicitud(solicitud);
    }

    @Override
    public Solicitud crearSolicitudPublica(Solicitud solicitud, String token) {
        checkSolicitudPublica(solicitud);
        String uuidSolicitud = UUID.randomUUID().toString();
        solicitud.setUuidSolicitud(uuidSolicitud);
        solicitud.setEstado(ApplicationConstantes.SIN_RESPONDER);
        solicitud.getInformacionSolicitante().setUuidInformacionSolicitante(UUID.randomUUID().toString());
        solicitud.getInformacionSolicitante().setSolicitud(solicitud);

        for(Anexo currentAnexo : solicitud.getAnexos()){
            currentAnexo.setUuidAnexo(UUID.randomUUID().toString());
            currentAnexo.setObjSolicitud(solicitud);

            MultipartFile archivo = currentAnexo.getAnexoFile();
            if (archivo != null && !archivo.isEmpty()) {
                try {
                    String rutaArchivo = almacenadorArchivos.guardarArchivo(uuidSolicitud, archivo, currentAnexo.getNombre());
                    String urlAnexo =  urlApplication + SOLICITUD_CONTROLLER_URL
                            + uuidSolicitud + "/" + new File(rutaArchivo).getName();
                    currentAnexo.setUrlAnexo(urlAnexo);
                } catch (IOException e) {
                    throw new RuntimeException("Error guardando el archivo del anexo: " + currentAnexo.getNombre(), e);
                }
            }
        }

        log.crearLog("Solicitud Publica Creada", String.format("Se realizó una solicitud publica %s" , solicitud.getNombre()), token);
        return gateway.guardarSolicitud(solicitud);
    }

    @Override
    public Solicitud actualizarSolicitud(String uuidSolicitud, Solicitud solicitud, String token) {
        Solicitud solicitudOriginal = gateway.getSolicitud(uuidSolicitud);

        if(solicitud.getUuidFuncionario() != null && !solicitud.getUuidFuncionario().isBlank()) {
            if (!solicitud.getUuidFuncionario().equals(solicitudOriginal.getObjFuncionario().getUuidUsuario())) {
                Usuario usuario = gatewayUsuario.getUsuario(solicitud.getUuidFuncionario());
                if (usuario == null)
                    formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, USUARIO, solicitud.getUuidFuncionario()));

                if (!(usuario instanceof Funcionario))
                    formateadorExcepciones.lanzarMalFormato(String.format(MensajesError.TIPO_DE_USUARIO_NO_VALIDO));
                Funcionario funcionarioNuevo = (Funcionario) usuario;
                solicitudOriginal.setObjFuncionario(funcionarioNuevo);
            }
        }

        if (solicitud.getUuidOrdenDelDia() == null || solicitud.getUuidOrdenDelDia().isBlank())
            solicitudOriginal.setObjOrdenDelDia(null);
        else {
            boolean actualizarOrden = solicitudOriginal.getObjOrdenDelDia() == null ||
                    !solicitud.getUuidOrdenDelDia().equals(solicitudOriginal.getObjOrdenDelDia().getUuidOrdenDelDia());

            if (actualizarOrden) {
                OrdenDelDia ordenNuevo = gatewayOrdenDelDia.getOrdenDelDia(solicitud.getUuidOrdenDelDia());
                if (ordenNuevo == null)
                    formateadorExcepciones.lanzarEntidadNoExiste(
                            String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, ORDEN_DEL_DIA, solicitud.getUuidOrdenDelDia())
                    );
                solicitudOriginal.setObjOrdenDelDia(ordenNuevo);
            }
        }

        solicitudOriginal.actualizar(solicitud);
        log.crearLog("Solicitud modificada", String.format("Se modifico la información de la solicitud %s" , solicitudOriginal.getNombre()), token);
        return gateway.guardarSolicitud(solicitudOriginal);
    }

    @Override
    public PaginacionRespuestaDTO<Solicitud> getSolicitudesPorFuncionario(String uuidFuncionario, int pagina, int tamanio) {
        if (pagina < 0 || tamanio < 0)
            formateadorExcepciones.lanzarMalFormato(MensajesError.PAGINACION_ERROR);

        PaginacionRespuestaDTO<Solicitud> respuesta = gateway.getSolicitudesPorFuncionario(uuidFuncionario, pagina, tamanio);

        if (respuesta.getContent().isEmpty())
            formateadorExcepciones.lanzarSinInformacion("No existen solicitudes asociadas al funcionario");

        return respuesta;
    }

    @Override
    public List<Solicitud> getSolicitudesPorOrdenDelDia(String uuidOrdenDelDia) {
        List<Solicitud> lista = gateway.getSolicitudesPorOrdenDelDia(uuidOrdenDelDia);
        if (lista.isEmpty())
            formateadorExcepciones.lanzarSinInformacion("No existen solicitudes asociadas a este Orden del Día");
        return lista;
    }

    @Override
    public List<Solicitud> getSolicitudesPorEstado(String estado) {
        String estadoFormateado = estado.replace("_", " ");
        List<Solicitud> lista = gateway.getSolicitudesPorEstado(estadoFormateado);
        if (lista.isEmpty())
            formateadorExcepciones.lanzarSinInformacion(
                    String.format("No hay solicitudes con el estado %s", estadoFormateado)
            );
        return lista;
    }

    @Override
    public PaginacionRespuestaDTO<Solicitud> buscarSolicitudesPorNombre(String filtro, int pagina, int tamanio) {
        if (pagina < 0 || tamanio < 0)
            formateadorExcepciones.lanzarMalFormato(MensajesError.PAGINACION_ERROR);

        PaginacionRespuestaDTO<Solicitud> respuesta = gateway.buscarSolicitudesPorNombre(filtro, pagina, tamanio);
        if (respuesta.getContent().isEmpty())
            formateadorExcepciones.lanzarSinInformacion("No se encontraron solicitudes que coincidan con la búsqueda");
        return respuesta;
    }

    @Override
    public PaginacionRespuestaDTO<Solicitud> buscarSolicitudesPorSolicitante(String nombreSolicitud, String solicitante, String estado, int pagina, int tamanio) {
        if (pagina < 0 || tamanio < 0)
            formateadorExcepciones.lanzarMalFormato(MensajesError.PAGINACION_ERROR);

        PaginacionRespuestaDTO<Solicitud> respuesta = gateway.buscarSolicitudesPorSolicitante(nombreSolicitud, solicitante, estado, pagina, tamanio);
        if (respuesta.getContent().isEmpty())
            formateadorExcepciones.lanzarSinInformacion("No se encontraron solicitudes que coincidan con la búsqueda");
        return respuesta;
    }

    @Override
    public PaginacionRespuestaDTO<Solicitud> buscarSolicitudesPorNombreYFuncionario(String uuidFuncionario, String filtro, int pagina, int tamanio) {
        if (pagina < 0 || tamanio < 0)
            formateadorExcepciones.lanzarMalFormato(MensajesError.PAGINACION_ERROR);

        PaginacionRespuestaDTO<Solicitud> respuesta = gateway.buscarSolicitudesPorNombreYFuncionario(uuidFuncionario, filtro, pagina, tamanio);
        if (respuesta.getContent().isEmpty())
            formateadorExcepciones.lanzarSinInformacion("No se encontraron solicitudes que coincidan con la búsqueda");
        return respuesta;
    }

    @Override
    public byte[] generarZipAnexosPorOrdenDelDia(String uuidOrden, String basePath) {
        List<Solicitud> solicitudes = gateway.getSolicitudesPorOrdenDelDia(uuidOrden);
        if (solicitudes.isEmpty())
            formateadorExcepciones.lanzarSinInformacion("No existen solicitudes asociadas a este Orden del Día");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(baos)) {

            Map<String, List<Solicitud>> solicitudesPorSeccion = new HashMap<>();
            for (Solicitud s : solicitudes) {
                String seccion = s.getObjTipoSolicitud().getSeccion();
                solicitudesPorSeccion.computeIfAbsent(seccion, k -> new ArrayList<>()).add(s);
            }

            for (Map.Entry<String, List<Solicitud>> entrySeccion : solicitudesPorSeccion.entrySet()) {
                String seccionName = sanitizeFileName(entrySeccion.getKey());

                for (Solicitud solicitud : entrySeccion.getValue()) {
                    String solicitudName = sanitizeFileName(solicitud.getNombre());
                    Path solicitudFolder = Paths.get(basePath, "anexos", solicitud.getUuidSolicitud());
                    File folder = solicitudFolder.toFile();

                    if (!folder.exists() || !folder.isDirectory())
                        formateadorExcepciones.lanzarMalFormato(String.format("La carpeta de anexos no fue encontrada o fue eliminada: %s", folder.getAbsolutePath()));

                    int contadorAnexos = 1;
                    for (Anexo anexo : solicitud.getAnexos()) {
                        String url = anexo.getUrlAnexo();
                        String realFileName = url.substring(url.lastIndexOf("/") + 1);
                        File file = new File(folder, realFileName);

                        if (!file.exists() || !file.isFile())
                            formateadorExcepciones.lanzarMalFormato("El archivo de anexo no fue encontrado o fue eliminado: " + file.getAbsolutePath());

                        String zipEntryPath = String.format("%s/%s/%d_%s",
                                seccionName,
                                solicitudName,
                                contadorAnexos,
                                sanitizeFileName(file.getName())
                        );

                        zos.putNextEntry(new ZipEntry(zipEntryPath));

                        try (FileInputStream fis = new FileInputStream(file)) {
                            byte[] buffer = new byte[4096];
                            int len;
                            while ((len = fis.read(buffer)) > 0)
                                zos.write(buffer, 0, len);
                        }

                        zos.closeEntry();
                        contadorAnexos++;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return baos.toByteArray();
    }

    /**
     * Verifica la validez de una solicitud y completa su información antes de crearla.
     *
     * @param solicitud la solicitud a validar.
     * @param token el token del usuario que realiza la acción.
     */
    private void checkSolicitud(Solicitud solicitud, String token){
        TipoSolicitud tipoSolicitud = gatewayTipoSolicitud.getTipoSolicitud(solicitud.getUuidTipoSolicitud());
        if(tipoSolicitud == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, TIPOS_SOLICITUD, solicitud.getUuidTipoSolicitud()));

        solicitud.setObjTipoSolicitud(tipoSolicitud);

        List<Anexo> anexosSolicitud = solicitud.getAnexos();
        List<TipoAnexo> tiposAnexo = tipoSolicitud.getAnexos();

        for (TipoAnexo tipo : tiposAnexo) {
            Anexo anexoEncontrado = anexosSolicitud.stream()
                    .filter(a -> a.getNombre().equalsIgnoreCase(tipo.getNombre()))
                    .findFirst()
                    .orElse(null);

            if (tipo.getObligatoriedad() && anexoEncontrado == null)
                formateadorExcepciones.lanzarMalFormato(String.format("El anexo %s es obligatorio", tipo.getNombre()));

            if (anexoEncontrado != null){
                MultipartFile archivo = anexoEncontrado.getAnexoFile();
                if (tipo.getObligatoriedad() && (archivo == null || archivo.isEmpty()))
                    formateadorExcepciones.lanzarMalFormato(String.format("El anexo %s debe contener un archivo", tipo.getNombre()));

                if (archivo != null && !archivo.isEmpty()){
                    String nombreArchivo = archivo.getOriginalFilename();
                    String extension = obtenerExtension(nombreArchivo).toLowerCase();

                    if (!extension.equalsIgnoreCase(tipo.getFormato()))
                        formateadorExcepciones.lanzarMalFormato(String.format("El anexo %s debe ser un archivo %sytg", tipo.getNombre(), tipo.getFormato()));
                }
            }

        }

        if(solicitud.getUuidOrdenDelDia() != null && !solicitud.getUuidOrdenDelDia().isBlank() ) {
            OrdenDelDia ordenDelDia = gatewayOrdenDelDia.getOrdenDelDia(solicitud.getUuidOrdenDelDia());
            if (ordenDelDia == null)
                formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, ORDEN_DEL_DIA, solicitud.getUuidOrdenDelDia()));
            solicitud.setObjOrdenDelDia(ordenDelDia);
            solicitud.setEstado(ApplicationConstantes.AGREGADO_EN_EL_ORDEN_DEL_DIA);
        } else
            solicitud.setEstado(ApplicationConstantes.SIN_RESPONDER);

        Funcionario funcionario = tipoSolicitud.getObjFuncionarioEncargado();
        if(funcionario != null)
            solicitud.setObjFuncionario(funcionario);

        String username = jwtServicio.getUsername(token);
        if(username == null || username.isBlank())
            formateadorExcepciones.lanzarErrorGenerico(MensajesError.USERNAME_TOKEN);

        Usuario usuario = gatewaySesion.getUsuario(username);
        if(usuario != null){
            InformacionSolicitante solicitante = new InformacionSolicitante();
            solicitante.setTipoDocumento(usuario.getTipoDocumento());
            solicitante.setNumeroDocumento(usuario.getNumeroDocumento());
            solicitante.setNombres(usuario.getNombres());
            solicitante.setApellidos(usuario.getApellidos());
            solicitante.setTelefono(usuario.getTelefono());
            solicitante.setCorreoElectronico(usuario.getCorreoElectronico());
            solicitud.setInformacionSolicitante(solicitante);
        }
    }

    /**
     * Verifica la validez de una solicitud pública y completa su información antes de crearla.
     *
     * @param solicitud la solicitud pública a validar.
     */
    private void checkSolicitudPublica(Solicitud solicitud){
        TipoSolicitud tipoSolicitud = gatewayTipoSolicitud.getTipoSolicitud(solicitud.getUuidTipoSolicitud());
        if(tipoSolicitud == null)
            formateadorExcepciones.lanzarEntidadNoExiste(String.format(MensajesError.ENTIDAD_NO_ENCONTRADA, TIPOS_SOLICITUD, solicitud.getUuidTipoSolicitud()));

        solicitud.setObjTipoSolicitud(tipoSolicitud);

        Funcionario funcionario = tipoSolicitud.getObjFuncionarioEncargado();
        if(funcionario != null)
            solicitud.setObjFuncionario(funcionario);;
    }

    /**
     * Obtiene la extensión de un archivo a partir de su nombre.
     *
     * @param archivo el nombre del archivo.
     * @return la extensión del archivo en minúsculas, o una cadena vacía si no tiene extensión.
     */
    private String obtenerExtension(String archivo) {
        if (archivo == null || !archivo.contains(".")) {
            return "";
        }
        return archivo.substring(archivo.lastIndexOf(".") + 1);
    }

    /**
     * Sanitiza un nombre de archivo eliminando caracteres inválidos y reemplazándolos por guiones bajos.
     *
     * @param name el nombre original del archivo.
     * @return el nombre de archivo sanitizado.
     */
    private String sanitizeFileName(String name) {
        if (name == null) return "SinNombre";
        return name.replaceAll("[^a-zA-Z0-9-_\\.]", "_").replaceAll("_+", "_");
    }
}
