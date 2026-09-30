package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudes.controlador;

import com.unicauca.cfiet.solicitudes.aplicacion.input.OrdenDelDiaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.SolicitudCUintPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.OrdenDelDia;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Solicitud;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudes.DTOPeticion.OrdenDelDiaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudes.DTOPeticion.SolicitudActualizarDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudes.DTOPeticion.SolicitudDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudes.DTOPeticion.SolicitudPublicaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudes.ValidadorAnexosService;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudes.mapeador.MapperSolicitudesInfraestructuraDominio;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@RestController
@RequestMapping("${url.application}solicitudes")
@CrossOrigin(origins = "${url.frontend}")
@Validated
@RequiredArgsConstructor
@Tag(name = "Solicitudes", description = "Operaciones relacionadas con la gestión de Solicitudes.")
public class SolicitudesRestController {
    private final OrdenDelDiaCUIntPuerto ordenDelDiaCU;
    private final SolicitudCUintPuerto solicitudCU;
    private final MapperSolicitudesInfraestructuraDominio mapper;
    private final ValidadorAnexosService validadorAnexosService;
    @Value("${app.uploads.base-path}")
    private String basePath;

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @GetMapping("/orden-del-dia/paginado")
    public ResponseEntity<?> indexPaginado(
            @RequestParam("pagina") int pagina,
            @RequestParam("tamanio") int tamanio) {
        var respuesta = ordenDelDiaCU.getOrdenesDelDia(pagina, tamanio);
        return ResponseEntity.ok(
                new PaginacionRespuestaDTO<>(
                        mapper.mapearModelosARespuesta(respuesta.getContent()),
                        respuesta.getTotalElements()
                )
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @GetMapping("/orden-del-dia")
    public ResponseEntity<?> index(){
        List<OrdenDelDia> ordenesDelDia = ordenDelDiaCU.getOrdenesDelDia();
        return ResponseEntity.ok(
                mapper.mapearModelosARespuesta(ordenesDelDia)
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @GetMapping("/orden-del-dia/{uuidOrdenDelDia}")
    public ResponseEntity<?> getOrdenDelDia(@PathVariable String uuidOrdenDelDia){
        OrdenDelDia ordenDelDia = ordenDelDiaCU.getOrdenDelDia(uuidOrdenDelDia);
        return ResponseEntity.ok(
                mapper.mapearModeloARespuesta(ordenDelDia)
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @Transactional
    @PostMapping("/orden-del-dia")
    public ResponseEntity<?> crearOrdenDelDia(@Valid @RequestBody OrdenDelDiaDTOPeticion peticion,
                                                @RequestHeader("Authorization") String token){
        OrdenDelDia ordenDelDia;
        try{
            ordenDelDia = ordenDelDiaCU.crearOrdenDelDia(mapper.mapearPeticionAModelo(peticion), token.substring(7));
        } catch (DataAccessException ex){
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error insertando en la base de datos....");
            response.put("error", ex.getMessage() + " " + ex.getMostSpecificCause().getMessage());
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok(
                mapper.mapearModeloARespuesta(ordenDelDia)
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @Transactional
    @PutMapping("/orden-del-dia/{uuidOrdenDelDia}")
    public ResponseEntity<?> actualizarOrdenDelDia(@PathVariable String uuidOrdenDelDia,
                                                   @Valid @RequestBody OrdenDelDiaDTOPeticion peticion,
                                                   @RequestHeader("Authorization") String token){
        OrdenDelDia ordenDelDia;
        try{
            ordenDelDia = ordenDelDiaCU.actualizarOrdenDelDia (uuidOrdenDelDia, mapper.mapearPeticionAModelo(peticion), token.substring(7));
        } catch (DataAccessException ex){
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error insertando en la base de datos....");
            response.put("error", ex.getMessage() + " " + ex.getMostSpecificCause().getMessage());
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok(
                mapper.mapearModeloARespuesta(ordenDelDia)
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_FUNCIONARIO_ACCESO)
    @GetMapping("/paginado")
    public ResponseEntity<?> solicitudesPaginado(
            @RequestParam("pagina") int pagina,
            @RequestParam("tamanio") int tamanio) {
        var respuesta = solicitudCU.getSolicitudes(pagina, tamanio);
        return ResponseEntity.ok(
                new PaginacionRespuestaDTO<>(
                        mapper.mapearModelosARespuestaSolicitud(respuesta.getContent()),
                        respuesta.getTotalElements()
                )
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_FUNCIONARIO_ACCESO)
    @GetMapping
    public ResponseEntity<?> solicitudesIndex(){
        List<Solicitud> ordenesDelDia = solicitudCU.getSolicitudes();
        return ResponseEntity.ok(
                mapper.mapearModelosARespuestaSolicitud(ordenesDelDia)
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_FUNCIONARIO_ACCESO)
    @GetMapping("/{uuidSolicitud}")
    public ResponseEntity<?> getSolicitud(@PathVariable String uuidSolicitud){
        Solicitud solicitud = solicitudCU.getSolicitud(uuidSolicitud);
        return ResponseEntity.ok(
                mapper.mapearModeloARespuesta(solicitud)
        );
    }

    @PreAuthorize(ApplicationConstantes.AUTHENTICATED)
    @Transactional
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> enviarSolicitud(@Valid @RequestPart("solicitud") SolicitudDTOPeticion peticion,
                                             @RequestPart(value = "archivos", required = false) List<MultipartFile> archivos,
                                             @RequestHeader("Authorization") String token){

        validadorAnexosService.validarYAsignarArchivos(peticion.getAnexos(), archivos);
        Solicitud solicitud;
        try{
            solicitud = solicitudCU.crearSolicitud(mapper.mapearPeticionAModelo(peticion),  token.substring(7));
        } catch (DataAccessException ex){
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error insertando en la base de datos....");
            response.put("error", ex.getMessage() + " " + ex.getMostSpecificCause().getMessage());
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok(
                mapper.mapearModeloARespuesta(solicitud)
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_FUNCIONARIO_ACCESO)
    @Transactional
    @PostMapping(value = "/public", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> enviarSolicitudPublica(@Valid @RequestPart("solicitud") SolicitudPublicaDTOPeticion peticion,
                                                    @RequestPart(value = "archivos", required = false) List<MultipartFile> archivos,
                                                    @RequestHeader("Authorization") String token){

        validadorAnexosService.validarYAsignarArchivos(peticion.getAnexos(), archivos);
        Solicitud solicitud;
        try{
            solicitud = solicitudCU.crearSolicitudPublica(mapper.mapearPeticionAModelo(peticion), token.substring(7));
        } catch (DataAccessException ex){
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error insertando en la base de datos....");
            response.put("error", ex.getMessage() + " " + ex.getMostSpecificCause().getMessage());
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok(
                mapper.mapearModeloARespuesta(solicitud)
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @Transactional
    @PutMapping("/{uuidSolicitud}")
    public ResponseEntity<?> actualizarOrdenDelDia(@PathVariable String uuidSolicitud,
                                                   @Valid @RequestBody SolicitudActualizarDTOPeticion peticion,
                                                   @RequestHeader("Authorization") String token){
        Solicitud solicitud;
        try{
            solicitud = solicitudCU.actualizarSolicitud(uuidSolicitud, mapper.mapearPeticionAModelo(peticion), token.substring(7));
        } catch (DataAccessException ex){
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error insertando en la base de datos....");
            response.put("error", ex.getMessage() + " " + ex.getMostSpecificCause().getMessage());
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok(
                mapper.mapearModeloARespuesta(solicitud)
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @GetMapping("/funcionario/{uuidFuncionario}")
    public ResponseEntity<?> getSolicitudesPorFuncionario(
            @PathVariable String uuidFuncionario,
            @RequestParam("pagina") int pagina,
            @RequestParam("tamanio") int tamanio) {

        var respuesta = solicitudCU.getSolicitudesPorFuncionario(uuidFuncionario, pagina, tamanio);
        return ResponseEntity.ok(
                new PaginacionRespuestaDTO<>(
                        mapper.mapearModelosARespuestaSolicitud(respuesta.getContent()),
                        respuesta.getTotalElements()
                )
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @GetMapping("/orden-del-dia/{uuidOrdenDelDia}/solicitudes")
    public ResponseEntity<?> getSolicitudesPorOrdenDelDia(@PathVariable String uuidOrdenDelDia) {
        List<Solicitud> lista = solicitudCU.getSolicitudesPorOrdenDelDia(uuidOrdenDelDia);
        return ResponseEntity.ok(
                mapper.mapearModelosARespuestaSolicitud(lista)
        );
    }

    @PreAuthorize(ApplicationConstantes.AUTHENTICATED)
    @GetMapping("/estado")
    public ResponseEntity<?> getSolicitudesPorEstado(@RequestParam("estado") String estado) {
        List<Solicitud> lista = solicitudCU.getSolicitudesPorEstado(estado);
        return ResponseEntity.ok(
                mapper.mapearModelosARespuestaSolicitud(lista)
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_FUNCIONARIO_ACCESO)
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarSolicitudesPorNombre(
            @RequestParam("filtro") String filtro,
            @RequestParam("pagina") int pagina,
            @RequestParam("tamanio") int tamanio) {

        var respuesta = solicitudCU.buscarSolicitudesPorNombre(filtro, pagina, tamanio);
        return ResponseEntity.ok(
                new PaginacionRespuestaDTO<>(
                        mapper.mapearModelosARespuestaSolicitud(respuesta.getContent()),
                        respuesta.getTotalElements()
                )
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @GetMapping("/buscar/solicitante")
    public ResponseEntity<?> buscarSolicitudesPorSolicitante(
            @RequestParam("solicitud") String nombreSolicitud,
            @RequestParam("solicitante") String solicitante,
            @RequestParam("estado") String estado,
            @RequestParam("pagina") int pagina,
            @RequestParam("tamanio") int tamanio) {

        var respuesta = solicitudCU.buscarSolicitudesPorSolicitante(nombreSolicitud, solicitante, estado, pagina, tamanio);
        return ResponseEntity.ok(
                new PaginacionRespuestaDTO<>(
                        mapper.mapearModelosARespuestaSolicitud(respuesta.getContent()),
                        respuesta.getTotalElements()
                )
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_FUNCIONARIO_ACCESO)
    @GetMapping("fun/buscar/")
    public ResponseEntity<?> buscarSolicitudesPorNombreYFuncionario(
            @RequestParam("uuidFuncionario") String uuidFuncionario,
            @RequestParam("filtro") String filtro,
            @RequestParam("pagina") int pagina,
            @RequestParam("tamanio") int tamanio) {

        var respuesta = solicitudCU.buscarSolicitudesPorNombreYFuncionario(uuidFuncionario, filtro, pagina, tamanio);
        return ResponseEntity.ok(
                new PaginacionRespuestaDTO<>(
                        mapper.mapearModelosARespuestaSolicitud(respuesta.getContent()),
                        respuesta.getTotalElements()
                )
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @GetMapping("/orden-del-dia/buscar")
    public ResponseEntity<?> buscarOrdenDelDiaPorNumeroActa(
            @RequestParam("filtro") String filtro,
            @RequestParam("pagina") int pagina,
            @RequestParam("tamanio") int tamanio) {
        var respuesta = ordenDelDiaCU.buscarOrdenDelDiaPorNumeroActa(filtro, pagina, tamanio);
        return ResponseEntity.ok(
                new PaginacionRespuestaDTO<>(
                        mapper.mapearModelosARespuesta(respuesta.getContent()),
                        respuesta.getTotalElements()
                )
        );
    }

    @PreAuthorize(ApplicationConstantes.AUTHENTICATED)
    @GetMapping("/anexos/{uuidSolicitud}/{fileName:.+}")
    public ResponseEntity<Resource> descargarAnexo(
            @PathVariable String uuidSolicitud,
            @PathVariable String fileName) {

        try {
            String decodedFileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
            File file = new File(basePath + "/anexos/" + uuidSolicitud + "/" + decodedFileName);

            if (!file.exists() || !file.isFile()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(file.toURI());

            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) contentType = "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + file.getName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @GetMapping("/anexos/download")
    public ResponseEntity<?> descargarAnexos(
            @RequestParam String uuidOrden,
            @RequestParam String nombreOrden) {
        byte[] zipBytes = solicitudCU.generarZipAnexosPorOrdenDelDia(uuidOrden, basePath);
        String nombreArchivo = nombreOrden.replaceAll("[^a-zA-Z0-9-_\\.]", "_").replaceAll("_+", "_") + ".zip";

        ByteArrayResource resource = new ByteArrayResource(zipBytes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(resource.contentLength())
                .body(resource);
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportarOrden(@RequestParam String uuidOrden) {

        byte[] archivo = ordenDelDiaCU.generarOrdenDelDia(uuidOrden);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"Orden_Del_Dia.docx\"")
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .body(archivo);
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @GetMapping("/exportar/respuestas")
    public ResponseEntity<byte[]> exportarOrdenRespuestas(@RequestParam String uuidOrden) {

        byte[] archivo = ordenDelDiaCU.generarOrdenDelDiaConRespuestas(uuidOrden);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"Orden_Del_Dia.docx\"")
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .body(archivo);
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @GetMapping("/exportar/reunion")
    public ResponseEntity<byte[]> exportarOrdenDesarrolloReunion(@RequestParam String uuidOrden) {

        byte[] archivo = ordenDelDiaCU.generarOrdenDelDiaMerge(uuidOrden);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"Orden_Del_Dia.docx\"")
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .body(archivo);
    }

    @PreAuthorize(ApplicationConstantes.AUTHENTICATED)
    @GetMapping("/orden-del-dia/estado")
    public ResponseEntity<?> getOrdenesDelDiaPorEstado(@RequestParam("estado") boolean estado) {
        List<OrdenDelDia> ordenesDelDia = ordenDelDiaCU.getOrdenesDelDiaPorEstado(estado);
        return ResponseEntity.ok(
                mapper.mapearModelosARespuesta(ordenesDelDia)
        );
    }
}
