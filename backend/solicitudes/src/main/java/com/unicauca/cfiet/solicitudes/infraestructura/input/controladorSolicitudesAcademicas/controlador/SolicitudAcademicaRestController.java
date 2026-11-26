package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.controlador;

import com.unicauca.cfiet.solicitudes.aplicacion.input.AnexoAcademicoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ConsultaSolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.CorreosPendientesCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.EliminacionRespuestasCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.FormatoOficialCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ResolucionAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ActorSolicitud;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AnexoAcademico;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ArchivoAdjunto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ResolucionAcademica;
import com.unicauca.cfiet.solicitudes.dominio.modelos.ValorProvisionalFormato;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.DTOPeticion.AjusteFormatoDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.DTOPeticion.FormatoCompuestoDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.DTORespuesta.AnexoAcademicoDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.DTORespuesta.CorreosPendientesDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.DTORespuesta.EliminacionRespuestasDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.DTORespuesta.HistorialSolicitudAcademicaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.DTORespuesta.ResolucionAcademicaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.DTORespuesta.SolicitudAcademicaDetalleDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.DTORespuesta.SolicitudAcademicaResumenDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.mapeador.MapperEliminacionRespuestasInfraestructuraDominio;
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
import java.time.LocalDate;
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
    private static final String FECHA_INICIAL = "la fecha inicial";
    private static final String FECHA_FINAL = "la fecha final";

    private final ConsultaSolicitudAcademicaCUIntPuerto consultaCU;
    private final AnexoAcademicoCUIntPuerto anexoCU;
    private final ResolucionAcademicaCUIntPuerto resolucionCU;
    private final MapperSolicitudAcademicaInfraestructuraDominio mapper;
    private final EliminacionRespuestasCUIntPuerto eliminacionCU;
    private final MapperEliminacionRespuestasInfraestructuraDominio mapperEliminacion;
    private final CorreosPendientesCUIntPuerto correosPendientesCU;
    private final FormatoOficialCUIntPuerto formatoCU;

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

    @PreAuthorize(ApplicationConstantes.FUNCIONARIO_ACADEMICO_ACCESO)
    @GetMapping("/funcionario/correos-pendientes")
    public ResponseEntity<CorreosPendientesDTORespuesta> getCorreosPendientes() {
        return new ResponseEntity<>(new CorreosPendientesDTORespuesta(correosPendientesCU.contarPendientes()), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.FUNCIONARIO_ACADEMICO_ACCESO)
    @Transactional(readOnly = true)
    @GetMapping("/funcionario/respuestas/conteo")
    public ResponseEntity<EliminacionRespuestasDTORespuesta> contarRespuestas(@RequestParam(value = "desde", required = false) String desde,
                                                                             @RequestParam(value = "hasta", required = false) String hasta,
                                                                             @RequestHeader("Authorization") String token) {
        LocalDate inicio = mapperEliminacion.mapearFecha(desde, FECHA_INICIAL);
        LocalDate fin = mapperEliminacion.mapearFecha(hasta, FECHA_FINAL);
        int cantidad = eliminacionCU.contarRespuestas(inicio, fin, token.substring(7));
        return new ResponseEntity<>(mapperEliminacion.mapearARespuesta(cantidad, inicio, fin), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.FUNCIONARIO_ACADEMICO_ACCESO)
    @Transactional
    @DeleteMapping("/funcionario/respuestas")
    public ResponseEntity<?> eliminarRespuestas(@RequestParam(value = "desde", required = false) String desde,
                                                @RequestParam(value = "hasta", required = false) String hasta,
                                                @RequestHeader("Authorization") String token) {
        LocalDate inicio = mapperEliminacion.mapearFecha(desde, FECHA_INICIAL);
        LocalDate fin = mapperEliminacion.mapearFecha(hasta, FECHA_FINAL);
        int cantidad;
        try {
            cantidad = eliminacionCU.eliminarRespuestas(inicio, fin, token.substring(7));
        } catch (DataAccessException ex) {
            return errorBaseDeDatos(ex);
        }
        return new ResponseEntity<EliminacionRespuestasDTORespuesta>(mapperEliminacion.mapearARespuesta(cantidad, inicio, fin), HttpStatus.OK);
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

    @PreAuthorize(ApplicationConstantes.ESTUDIANTE_FUNCIONARIO_ACADEMICO_DECANO_ACCESO)
    @Transactional(readOnly = true)
    @GetMapping(UUID_SOLICITUD + "/formato")
    public ResponseEntity<ByteArrayResource> descargarFormato(@PathVariable String uuidSolicitud,
                                                              @RequestHeader("Authorization") String token) {
        return descarga(formatoCU.descargarFormato(uuidSolicitud, token.substring(7)));
    }

    @PreAuthorize(ApplicationConstantes.ESTUDIANTE_ACCESO)
    @Transactional
    @PostMapping(value = UUID_SOLICITUD + "/estudiante/radicar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> radicarFormato(@PathVariable String uuidSolicitud,
                                            @RequestParam("archivo") MultipartFile archivo,
                                            @RequestHeader("Authorization") String token) {
        try {
            formatoCU.radicar(uuidSolicitud, mapper.mapearArchivo(archivo), token.substring(7));
            return new ResponseEntity<SolicitudAcademicaDetalleDTORespuesta>(
                    mapper.mapearDetalleARespuesta(consultaCU.getDetalle(uuidSolicitud, token.substring(7))), HttpStatus.OK);
        } catch (DataAccessException ex) {
            return errorBaseDeDatos(ex);
        }
    }

    @PreAuthorize(ApplicationConstantes.ESTUDIANTE_ACCESO)
    @Transactional
    @DeleteMapping(UUID_SOLICITUD + "/estudiante")
    public ResponseEntity<?> descartarSolicitud(@PathVariable String uuidSolicitud,
                                                @RequestHeader("Authorization") String token) {
        try {
            formatoCU.descartar(uuidSolicitud, token.substring(7));
        } catch (DataAccessException ex) {
            return errorBaseDeDatos(ex);
        }
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(ApplicationConstantes.FUNCIONARIO_ACADEMICO_DECANO_ACCESO)
    @Transactional(readOnly = true)
    @PostMapping(UUID_SOLICITUD + "/formato/compuesto")
    public ResponseEntity<ByteArrayResource> descargarFormatoCompuesto(@PathVariable String uuidSolicitud,
                                                                       @RequestBody(required = false) FormatoCompuestoDTOPeticion peticion,
                                                                       @RequestHeader("Authorization") String token) {
        List<ValorProvisionalFormato> valores = peticion == null || peticion.getValoresProvisionales() == null ? List.of()
                : peticion.getValoresProvisionales().stream()
                .filter(valor -> valor != null)
                .map(valor -> ValorProvisionalFormato.builder()
                        .uuidAsignaturaSolicitud(valor.getAsignaturaSolicitudUuid())
                        .uuidSituacionMatricula(valor.getSituacionMatriculaUuid())
                        .uuidSituacionCancelar(valor.getSituacionCancelarUuid())
                        .faltas(valor.getNumeroFaltas())
                        .nota(valor.getNota())
                        .aprobada(valor.getAprobada())
                        .build())
                .toList();
        return descarga(formatoCU.componer(uuidSolicitud, valores, token.substring(7)));
    }

    @PreAuthorize(ApplicationConstantes.FUNCIONARIO_ACADEMICO_DECANO_ACCESO)
    @Transactional
    @PostMapping(value = UUID_SOLICITUD + "/formato/decano", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> subirFormatoDecano(@PathVariable String uuidSolicitud,
                                                @RequestParam("archivo") MultipartFile archivo,
                                                @RequestHeader("Authorization") String token) {
        try {
            formatoCU.subirFormatoDecano(uuidSolicitud, mapper.mapearArchivo(archivo), token.substring(7));
            return new ResponseEntity<SolicitudAcademicaDetalleDTORespuesta>(
                    mapper.mapearDetalleARespuesta(consultaCU.getDetalle(uuidSolicitud, token.substring(7))), HttpStatus.OK);
        } catch (DataAccessException ex) {
            return errorBaseDeDatos(ex);
        }
    }

    @PreAuthorize(ApplicationConstantes.FUNCIONARIO_ACADEMICO_ACCESO)
    @Transactional
    @PutMapping(UUID_SOLICITUD + "/formato/ajuste")
    public ResponseEntity<?> guardarAjusteFormato(@PathVariable String uuidSolicitud,
                                                  @RequestBody(required = false) AjusteFormatoDTOPeticion peticion,
                                                  @RequestHeader("Authorization") String token) {
        try {
            formatoCU.guardarAjuste(uuidSolicitud, peticion == null ? null : peticion.getAjusteXmm(),
                    peticion == null ? null : peticion.getAjusteYmm(), token.substring(7));
            return new ResponseEntity<SolicitudAcademicaDetalleDTORespuesta>(
                    mapper.mapearDetalleARespuesta(consultaCU.getDetalle(uuidSolicitud, token.substring(7))), HttpStatus.OK);
        } catch (DataAccessException ex) {
            return errorBaseDeDatos(ex);
        }
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
