package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorExamenesSupletorios.controlador;

import com.unicauca.cfiet.solicitudes.aplicacion.input.ConsultaExamenSupletorioCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ExamenSupletorioCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.TramiteExamenSupletorioCUIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudExamenSupletorio;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.DTOPeticion.ObservacionDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorExamenesSupletorios.DTOPeticion.AprobacionComprobanteDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorExamenesSupletorios.DTOPeticion.RemisionSupletorioDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorExamenesSupletorios.DTORespuesta.ExamenSupletorioDetalleDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorExamenesSupletorios.DTORespuesta.FormularioExamenSupletorioDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorExamenesSupletorios.DTORespuesta.RadicacionExamenSupletorioDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorExamenesSupletorios.mapeador.MapperExamenSupletorioInfraestructuraDominio;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@RestController
@RequestMapping("${url.application}examenes-supletorios")
@CrossOrigin(origins = "${url.frontend}")
@RequiredArgsConstructor
@Tag(name = "Exámenes supletorios", description = "Radicación, detalle y trámite de las solicitudes de Examen Supletorio.")
public class ExamenSupletorioRestController {
    private static final String UUID_SOLICITUD = "/{uuidSolicitud:[0-9a-fA-F\\-]{36}}";
    private static final String FECHA_EXAMEN = "la fecha del examen no presentado";
    private static final String FECHA_CRUZADA = "la fecha del examen cruzado";
    private static final String FECHA_ACORDADA = "la fecha acordada del supletorio";

    private final ExamenSupletorioCUIntPuerto radicacionCU;
    private final ConsultaExamenSupletorioCUIntPuerto consultaCU;
    private final TramiteExamenSupletorioCUIntPuerto tramiteCU;
    private final MapperExamenSupletorioInfraestructuraDominio mapper;

    @PreAuthorize(ApplicationConstantes.ESTUDIANTE_ACCESO)
    @Transactional(readOnly = true)
    @GetMapping("/formulario")
    public ResponseEntity<FormularioExamenSupletorioDTORespuesta> getFormulario(@RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(mapper.mapearFormularioARespuesta(consultaCU.getFormulario(token.substring(7))), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.ESTUDIANTE_ACCESO)
    @Transactional
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> radicar(@RequestParam(value = "asignaturaMatriculada", required = false) String asignaturaMatriculada,
                                     @RequestParam(value = "fechaExamenNoPresentado", required = false) String fechaExamenNoPresentado,
                                     @RequestParam(value = "tipoCausa", required = false) String tipoCausa,
                                     @RequestParam(value = "asignaturaCruzada", required = false) String asignaturaCruzada,
                                     @RequestParam(value = "fechaExamenCruzada", required = false) String fechaExamenCruzada,
                                     @RequestParam(value = "horaExamenCruzada", required = false) String horaExamenCruzada,
                                     MultipartHttpServletRequest peticion,
                                     @RequestHeader("Authorization") String token) {
        SolicitudExamenSupletorio supletorio;
        try {
            String uuidEstudiante = consultaCU.getEstudianteAutenticado(token.substring(7));
            supletorio = radicacionCU.radicarExamenSupletorio(uuidEstudiante, asignaturaMatriculada,
                    mapper.mapearFecha(fechaExamenNoPresentado, FECHA_EXAMEN), tipoCausa, asignaturaCruzada,
                    mapper.mapearFecha(fechaExamenCruzada, FECHA_CRUZADA), horaExamenCruzada,
                    mapper.mapearAnexos(peticion.getMultiFileMap()), token.substring(7));
        } catch (DataAccessException ex) {
            return errorBaseDeDatos(ex);
        }
        return new ResponseEntity<RadicacionExamenSupletorioDTORespuesta>(mapper.mapearRadicacionARespuesta(supletorio), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.ESTUDIANTE_FUNCIONARIO_ACADEMICO_DECANO_ACCESO)
    @Transactional(readOnly = true)
    @GetMapping(UUID_SOLICITUD)
    public ResponseEntity<ExamenSupletorioDetalleDTORespuesta> getDetalle(@PathVariable String uuidSolicitud,
                                                                         @RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(mapper.mapearDetalleARespuesta(consultaCU.getDetalle(uuidSolicitud, token.substring(7))), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.FUNCIONARIO_ACADEMICO_ACCESO)
    @Transactional
    @PostMapping(UUID_SOLICITUD + "/funcionario/rechazar")
    public ResponseEntity<?> rechazarPorFuncionario(@PathVariable String uuidSolicitud,
                                                    @RequestBody ObservacionDTOPeticion peticion,
                                                    @RequestHeader("Authorization") String token) {
        return accion(uuidSolicitud, token, () -> tramiteCU.rechazarPorFuncionario(uuidSolicitud, peticion.getObservacion(), token.substring(7)));
    }

    @PreAuthorize(ApplicationConstantes.FUNCIONARIO_ACADEMICO_ACCESO)
    @Transactional
    @PostMapping(UUID_SOLICITUD + "/funcionario/remitir")
    public ResponseEntity<?> remitirADecano(@PathVariable String uuidSolicitud,
                                            @RequestBody RemisionSupletorioDTOPeticion peticion,
                                            @RequestHeader("Authorization") String token) {
        return accion(uuidSolicitud, token, () -> tramiteCU.remitirADecano(uuidSolicitud, peticion.getRequisitosVerificados(),
                peticion.getObservacion(), token.substring(7)));
    }

    @PreAuthorize(ApplicationConstantes.FUNCIONARIO_ACADEMICO_ACCESO)
    @Transactional
    @PostMapping(UUID_SOLICITUD + "/funcionario/responder")
    public ResponseEntity<?> enviarRespuesta(@PathVariable String uuidSolicitud,
                                             @RequestHeader("Authorization") String token) {
        return accion(uuidSolicitud, token, () -> tramiteCU.enviarRespuesta(uuidSolicitud, token.substring(7)));
    }

    @PreAuthorize(ApplicationConstantes.FUNCIONARIO_ACADEMICO_ACCESO)
    @Transactional
    @PostMapping(UUID_SOLICITUD + "/funcionario/recibo")
    public ResponseEntity<?> enviarRecibo(@PathVariable String uuidSolicitud,
                                          @RequestHeader("Authorization") String token) {
        return accion(uuidSolicitud, token, () -> tramiteCU.enviarRecibo(uuidSolicitud, token.substring(7)));
    }

    @PreAuthorize(ApplicationConstantes.FUNCIONARIO_ACADEMICO_ACCESO)
    @Transactional
    @PostMapping(UUID_SOLICITUD + "/funcionario/comprobante/aprobar")
    public ResponseEntity<?> aprobarComprobante(@PathVariable String uuidSolicitud,
                                                @RequestBody(required = false) AprobacionComprobanteDTOPeticion peticion,
                                                @RequestHeader("Authorization") String token) {
        return accion(uuidSolicitud, token, () -> tramiteCU.aprobarComprobante(uuidSolicitud,
                mapper.mapearFecha(peticion == null ? null : peticion.getFechaAcordadaExamen(), FECHA_ACORDADA), token.substring(7)));
    }

    @PreAuthorize(ApplicationConstantes.FUNCIONARIO_ACADEMICO_ACCESO)
    @Transactional
    @PostMapping(UUID_SOLICITUD + "/funcionario/comprobante/rechazar")
    public ResponseEntity<?> rechazarComprobante(@PathVariable String uuidSolicitud,
                                                 @RequestBody ObservacionDTOPeticion peticion,
                                                 @RequestHeader("Authorization") String token) {
        return accion(uuidSolicitud, token, () -> tramiteCU.rechazarComprobante(uuidSolicitud, peticion.getObservacion(), token.substring(7)));
    }

    @PreAuthorize(ApplicationConstantes.DECANO_ACCESO)
    @Transactional
    @PostMapping(UUID_SOLICITUD + "/decano/aprobar")
    public ResponseEntity<?> aprobarPorDecano(@PathVariable String uuidSolicitud,
                                              @RequestBody(required = false) ObservacionDTOPeticion peticion,
                                              @RequestHeader("Authorization") String token) {
        return accion(uuidSolicitud, token, () -> tramiteCU.aprobarPorDecano(uuidSolicitud,
                peticion == null ? null : peticion.getObservacion(), token.substring(7)));
    }

    @PreAuthorize(ApplicationConstantes.DECANO_ACCESO)
    @Transactional
    @PostMapping(UUID_SOLICITUD + "/decano/rechazar")
    public ResponseEntity<?> rechazarPorDecano(@PathVariable String uuidSolicitud,
                                               @RequestBody ObservacionDTOPeticion peticion,
                                               @RequestHeader("Authorization") String token) {
        return accion(uuidSolicitud, token, () -> tramiteCU.rechazarPorDecano(uuidSolicitud, peticion.getObservacion(), token.substring(7)));
    }

    @PreAuthorize(ApplicationConstantes.ESTUDIANTE_ACCESO)
    @Transactional
    @PostMapping(UUID_SOLICITUD + "/estudiante/comprobante")
    public ResponseEntity<?> subirComprobante(@PathVariable String uuidSolicitud,
                                              @RequestHeader("Authorization") String token) {
        return accion(uuidSolicitud, token, () -> tramiteCU.subirComprobante(uuidSolicitud, token.substring(7)));
    }

    private ResponseEntity<?> accion(String uuidSolicitud, String token, Supplier<SolicitudExamenSupletorio> accion) {
        try {
            accion.get();
        } catch (DataAccessException ex) {
            return errorBaseDeDatos(ex);
        }
        return new ResponseEntity<ExamenSupletorioDetalleDTORespuesta>(
                mapper.mapearDetalleARespuesta(consultaCU.getDetalle(uuidSolicitud, token.substring(7))), HttpStatus.OK);
    }

    private ResponseEntity<Map<String, Object>> errorBaseDeDatos(DataAccessException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Error insertando en la base de datos....");
        response.put("error", ex.getMessage() + " " + ex.getMostSpecificCause().getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
