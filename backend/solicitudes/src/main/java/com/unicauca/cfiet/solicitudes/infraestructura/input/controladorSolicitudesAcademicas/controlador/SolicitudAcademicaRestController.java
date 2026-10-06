package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.controlador;

import com.unicauca.cfiet.solicitudes.aplicacion.input.AnexoAcademicoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ConsultaSolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ResolucionAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ActorSolicitud;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AnexoAcademico;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ArchivoAdjunto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ResolucionAcademica;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.DTORespuesta.AnexoAcademicoDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.DTORespuesta.HistorialSolicitudAcademicaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.DTORespuesta.ResolucionAcademicaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.DTORespuesta.SolicitudAcademicaDetalleDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.DTORespuesta.SolicitudAcademicaResumenDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.mapeador.MapperSolicitudAcademicaInfraestructuraDominio;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${url.application}solicitudes-academicas")
@CrossOrigin(origins = "${url.frontend}")
@RequiredArgsConstructor
@Tag(name = "Solicitudes académicas", description = "Consulta, anexos y Resolución de las solicitudes académicas.")
public class SolicitudAcademicaRestController {
    private static final String UUID_SOLICITUD = "/{uuidSolicitud:[0-9a-fA-F\\-]{36}}";
    private static final String UUID_ANEXO = "/{uuidAnexo:[0-9a-fA-F\\-]{36}}";

    private final ConsultaSolicitudAcademicaCUIntPuerto consultaCU;
    private final AnexoAcademicoCUIntPuerto anexoCU;
    private final ResolucionAcademicaCUIntPuerto resolucionCU;
    private final MapperSolicitudAcademicaInfraestructuraDominio mapper;

    @PreAuthorize(ApplicationConstantes.ESTUDIANTE_ACCESO)
    @Transactional(readOnly = true)
    @GetMapping("/estudiante")
    public ResponseEntity<List<SolicitudAcademicaResumenDTORespuesta>> getBandejaEstudiante(@RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(mapper.mapearBandejaARespuesta(consultaCU.getBandejaEstudiante(token.substring(7))), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.FUNCIONARIO_ACADEMICO_ACCESO)
    @Transactional(readOnly = true)
    @GetMapping("/funcionario")
    public ResponseEntity<List<SolicitudAcademicaResumenDTORespuesta>> getBandejaFuncionario(@RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(mapper.mapearBandejaARespuesta(consultaCU.getBandejaFuncionario(token.substring(7))), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.DECANO_ACCESO)
    @Transactional(readOnly = true)
    @GetMapping("/decano")
    public ResponseEntity<List<SolicitudAcademicaResumenDTORespuesta>> getBandejaDecano(@RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(mapper.mapearBandejaARespuesta(consultaCU.getBandejaDecano(token.substring(7))), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.ESTUDIANTE_FUNCIONARIO_ACADEMICO_DECANO_ACCESO)
    @Transactional(readOnly = true)
    @GetMapping(UUID_SOLICITUD)
    public ResponseEntity<SolicitudAcademicaDetalleDTORespuesta> getDetalle(@PathVariable String uuidSolicitud,
                                                                           @RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(mapper.mapearDetalleARespuesta(consultaCU.getDetalle(uuidSolicitud, token.substring(7))), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.ESTUDIANTE_FUNCIONARIO_ACADEMICO_DECANO_ACCESO)
    @Transactional(readOnly = true)
    @GetMapping(UUID_SOLICITUD + "/historial")
    public ResponseEntity<List<HistorialSolicitudAcademicaDTORespuesta>> getHistorial(@PathVariable String uuidSolicitud,
                                                                                     @RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(mapper.mapearHistorialARespuesta(consultaCU.getHistorial(uuidSolicitud, token.substring(7))), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.ESTUDIANTE_FUNCIONARIO_ACADEMICO_ACCESO)
    @Transactional
    @PostMapping(value = UUID_SOLICITUD + "/anexos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> adjuntarAnexo(@PathVariable String uuidSolicitud,
                                           @RequestParam("archivo") MultipartFile archivo,
                                           @RequestParam(value = "tipoAnexo", required = false) String tipoAnexo,
                                           @RequestHeader("Authorization") String token) {
        AnexoAcademico anexo;
        try {
            ActorSolicitud actor = consultaCU.resolverActor(uuidSolicitud, token.substring(7));
            anexo = anexoCU.adjuntarAnexo(uuidSolicitud, tipoAnexo, mapper.mapearArchivo(archivo), actor, token.substring(7));
        } catch (DataAccessException ex) {
            return errorBaseDeDatos(ex);
        }
        return new ResponseEntity<AnexoAcademicoDTORespuesta>(mapper.mapearAnexoARespuesta(anexo), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.ESTUDIANTE_FUNCIONARIO_ACADEMICO_DECANO_ACCESO)
    @Transactional(readOnly = true)
    @GetMapping(UUID_SOLICITUD + "/anexos" + UUID_ANEXO)
    public ResponseEntity<ByteArrayResource> descargarAnexo(@PathVariable String uuidSolicitud,
                                                            @PathVariable String uuidAnexo,
                                                            @RequestHeader("Authorization") String token) {
        return descarga(consultaCU.descargarAnexo(uuidSolicitud, uuidAnexo, token.substring(7)));
    }

    @PreAuthorize(ApplicationConstantes.FUNCIONARIO_ACADEMICO_ACCESO)
    @Transactional
    @PostMapping(value = UUID_SOLICITUD + "/resolucion", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> adjuntarResolucion(@PathVariable String uuidSolicitud,
                                                @RequestParam("archivo") MultipartFile archivo,
                                                @RequestHeader("Authorization") String token) {
        ResolucionAcademica resolucion;
        try {
            ActorSolicitud actor = consultaCU.resolverActor(uuidSolicitud, token.substring(7));
            resolucion = resolucionCU.adjuntarResolucion(uuidSolicitud, mapper.mapearArchivo(archivo), actor, token.substring(7));
        } catch (DataAccessException ex) {
            return errorBaseDeDatos(ex);
        }
        return new ResponseEntity<ResolucionAcademicaDTORespuesta>(mapper.mapearResolucionARespuesta(resolucion), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.ESTUDIANTE_FUNCIONARIO_ACADEMICO_DECANO_ACCESO)
    @Transactional(readOnly = true)
    @GetMapping(UUID_SOLICITUD + "/resolucion")
    public ResponseEntity<ByteArrayResource> descargarResolucion(@PathVariable String uuidSolicitud,
                                                                 @RequestHeader("Authorization") String token) {
        ActorSolicitud actor = consultaCU.resolverActor(uuidSolicitud, token.substring(7));
        return descarga(resolucionCU.obtenerResolucion(uuidSolicitud, actor));
    }

    private ResponseEntity<ByteArrayResource> descarga(ArchivoAdjunto archivo) {
        ByteArrayResource recurso = new ByteArrayResource(archivo.getContenido());
        String tipo = archivo.getTipoContenido() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : archivo.getTipoContenido();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(archivo.getNombreOriginal(), StandardCharsets.UTF_8).build().toString())
                .contentType(MediaType.parseMediaType(tipo))
                .contentLength(recurso.contentLength())
                .body(recurso);
    }

    private ResponseEntity<Map<String, Object>> errorBaseDeDatos(DataAccessException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Error insertando en la base de datos....");
        response.put("error", ex.getMessage() + " " + ex.getMostSpecificCause().getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
