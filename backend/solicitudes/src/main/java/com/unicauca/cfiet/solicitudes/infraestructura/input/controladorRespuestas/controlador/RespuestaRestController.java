package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorRespuestas.controlador;

import com.unicauca.cfiet.solicitudes.aplicacion.input.RespuestaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Respuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorRespuestas.DTOPeticion.RespuestaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorRespuestas.DTORespuesta.RespuestaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorRespuestas.mapeador.MapperRespuestaInfraestructuraDominio;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@RestController
@RequestMapping("${url.application}respuestas")
@CrossOrigin(origins = "${url.frontend}")
@Validated
@RequiredArgsConstructor
@Tag(name = "Solicitudes", description = "Operaciones relacionadas con la gestión de Respuestas.")
public class RespuestaRestController {
    private final RespuestaCUIntPuerto respuestaCU;
    private final MapperRespuestaInfraestructuraDominio mapper;
    @Value("${app.uploads.base-path}")
    private String basePath;

    @PreAuthorize(ApplicationConstantes.AUTHENTICATED)
    @GetMapping("/paginado")
    public ResponseEntity<?> indexPaginado(
            @RequestParam("pagina") int pagina,
            @RequestParam("tamanio") int tamanio) {
        var respuesta = respuestaCU.getRespuestas(pagina, tamanio);
        return ResponseEntity.ok(
                new PaginacionRespuestaDTO<>(
                        mapper.mapearModelosARespuesta(respuesta.getContent()),
                        respuesta.getTotalElements()
                )
        );
    }

    @PreAuthorize(ApplicationConstantes.AUTHENTICATED)
    @GetMapping("/paginado/nombre-solicitud")
    public ResponseEntity<?> indexPaginadoPorNombreSolicitud(
            @RequestParam("pagina") int pagina,
            @RequestParam("tamanio") int tamanio,
            @RequestParam("nombreSolicitud") String nombreSolicitud) {
        var respuesta = respuestaCU.getRespuestasPorNombreSolicitud(nombreSolicitud, pagina, tamanio);
        return ResponseEntity.ok(
                new PaginacionRespuestaDTO<>(
                        mapper.mapearModelosARespuesta(respuesta.getContent()),
                        respuesta.getTotalElements()
                )
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_FUNCIONARIO_ACCESO)
    @GetMapping("/paginado/funcionario")
    public ResponseEntity<?> indexPaginadoPorFuncionario(
            @RequestParam("pagina") int pagina,
            @RequestParam("tamanio") int tamanio,
            @RequestParam("uuidFuncionario") String uuidFuncionario) {
        var respuesta = respuestaCU.getRespuestasPorFuncionario(uuidFuncionario, pagina, tamanio);
        return ResponseEntity.ok(
                new PaginacionRespuestaDTO<>(
                        mapper.mapearModelosARespuesta(respuesta.getContent()),
                        respuesta.getTotalElements()
                )
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_FUNCIONARIO_ACCESO)
    @GetMapping("/paginado/funcionario-nombre-solicitud")
    public ResponseEntity<?> indexPaginadoPorFuncionarioYNombreSolicitud(
            @RequestParam("pagina") int pagina,
            @RequestParam("tamanio") int tamanio,
            @RequestParam("uuidFuncionario") String uuidFuncionario,
            @RequestParam("nombreSolicitud") String nombreSolicitud) {
        var respuesta = respuestaCU.getRespuestasPorFuncionarioNombreSolicitud(uuidFuncionario, nombreSolicitud, pagina, tamanio);
        return ResponseEntity.ok(
                new PaginacionRespuestaDTO<>(
                        mapper.mapearModelosARespuesta(respuesta.getContent()),
                        respuesta.getTotalElements()
                )
        );
    }

    @PreAuthorize(ApplicationConstantes.AUTHENTICATED)
    @GetMapping("/{uuidRespuesta}")
    public ResponseEntity<?> getRespuesta(@PathVariable String uuidRespuesta){
        return ResponseEntity.ok(
                mapper.mapearModeloARespuesta(respuestaCU.getRespuesta(uuidRespuesta))
        );
    }

    @PreAuthorize(ApplicationConstantes.AUTHENTICATED)
    @GetMapping("/solicitud/{uuidSolicitud}")
    public ResponseEntity<?> getRespuestaPorSolicitud(@PathVariable String uuidSolicitud){
        return ResponseEntity.ok(
                mapper.mapearModeloARespuesta(respuestaCU.getRespuestaPorSolicitud(uuidSolicitud))
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @Transactional
    @PostMapping("/{uuidSolicitud}")
    public ResponseEntity<?> registrarRespuesta(@PathVariable String uuidSolicitud,
                                                @Valid @RequestBody RespuestaDTOPeticion peticion,
                                                @RequestHeader("Authorization") String token){
        Respuesta respuesta;
        try{
            respuesta = respuestaCU.registrarRespuesta(uuidSolicitud, mapper.mapearPeticionAModelo(peticion), token.substring(7));
        } catch (DataAccessException ex){
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error insertando en la base de datos....");
            response.put("error", ex.getMessage() + " " + ex.getMostSpecificCause().getMessage());
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<RespuestaDTORespuesta>(
                mapper.mapearModeloARespuesta(respuesta), HttpStatus.OK
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_FUNCIONARIO_ACCESO)
    @Transactional
    @PostMapping("/{uuidRespuesta}/archivo")
    public ResponseEntity<?> responderSolicitud(@PathVariable String uuidRespuesta,
                                                @RequestParam("archivo") MultipartFile archivo,
                                                @RequestHeader("Authorization") String token) {
        Respuesta respuesta;
        try {
            respuesta = respuestaCU.responderSolicitud(uuidRespuesta, archivo, token.substring(7));
        } catch (DataAccessException ex) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error procesando la respuesta");
            response.put("error", ex.getMessage() + " " + ex.getMostSpecificCause().getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(
                mapper.mapearModeloARespuesta(respuesta),
                HttpStatus.OK
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_FUNCIONARIO_ACCESO)
    @Transactional
    @PatchMapping("/{uuidRespuesta}/archivo")
    public ResponseEntity<?> eliminarArchivoRespuesta(
            @PathVariable String uuidRespuesta,
            @RequestHeader("Authorization") String token) {

        Respuesta respuesta;
        try {
            respuesta = respuestaCU.eliminarArchivoRespuesta(uuidRespuesta, token.substring(7));
        } catch (DataAccessException ex) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error eliminando el archivo de la respuesta");
            response.put("error", ex.getMessage() + " " + ex.getMostSpecificCause().getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(
                mapper.mapearModeloARespuesta(respuesta),
                HttpStatus.OK
        );
    }

    @PreAuthorize(ApplicationConstantes.AUTHENTICATED)
    @GetMapping("/{uuidRespuesta}/{fileName:.+}")
    public ResponseEntity<Resource> descargarRespuesta(
            @PathVariable String uuidRespuesta,
            @PathVariable String fileName) {
        try {
            String decodedFileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
            File file = new File(basePath + "/respuestas/" + uuidRespuesta + "/" + decodedFileName);

            if (!file.exists() || !file.isFile())
                return ResponseEntity.notFound().build();

            Resource resource = new UrlResource(file.toURI());

            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null)
                contentType = "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + file.getName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
