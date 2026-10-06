package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.controlador;

import com.unicauca.cfiet.solicitudes.aplicacion.input.CancelacionAsignaturaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ConsultaCancelacionAsignaturaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.TramiteCancelacionAsignaturaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudCancelacionAsignatura;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTOPeticion.AprobacionCancelacionAsignaturaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTOPeticion.RemisionCancelacionAsignaturaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTORespuesta.CancelacionAsignaturaDetalleDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTORespuesta.FormularioCancelacionAsignaturaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTORespuesta.RadicacionCancelacionAsignaturaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.mapeador.MapperCancelacionAsignaturaInfraestructuraDominio;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.DTOPeticion.ObservacionDTOPeticion;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@RestController
@RequestMapping("${url.application}cancelaciones-asignatura")
@CrossOrigin(origins = "${url.frontend}")
@RequiredArgsConstructor
@Tag(name = "Cancelaciones de asignatura", description = "Radicación, detalle y trámite de las solicitudes de Cancelación de Asignatura.")
public class CancelacionAsignaturaRestController {
    private static final String UUID_SOLICITUD = "/{uuidSolicitud:[0-9a-fA-F\\-]{36}}";

    private final CancelacionAsignaturaCUIntPuerto radicacionCU;
    private final ConsultaCancelacionAsignaturaCUIntPuerto consultaCU;
    private final TramiteCancelacionAsignaturaCUIntPuerto tramiteCU;
    private final MapperCancelacionAsignaturaInfraestructuraDominio mapper;

    @PreAuthorize(ApplicationConstantes.ESTUDIANTE_ACCESO)
    @Transactional(readOnly = true)
    @GetMapping("/formulario")
    public ResponseEntity<FormularioCancelacionAsignaturaDTORespuesta> getFormulario(@RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(mapper.mapearFormularioARespuesta(consultaCU.getFormulario(token.substring(7))), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.ESTUDIANTE_ACCESO)
    @Transactional
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> radicar(@RequestParam(value = "motivo", required = false) String motivo,
                                     @RequestParam(value = "asignaturas", required = false) List<String> asignaturas,
                                     MultipartHttpServletRequest peticion,
                                     @RequestHeader("Authorization") String token) {
        SolicitudCancelacionAsignatura cancelacion;
        try {
            String uuidEstudiante = consultaCU.getEstudianteAutenticado(token.substring(7));
            cancelacion = radicacionCU.radicarCancelacionAsignatura(uuidEstudiante, motivo, asignaturas,
                    mapper.mapearAnexos(peticion.getMultiFileMap()), token.substring(7));
        } catch (DataAccessException ex) {
            return errorBaseDeDatos(ex);
        }
        return new ResponseEntity<RadicacionCancelacionAsignaturaDTORespuesta>(mapper.mapearRadicacionARespuesta(cancelacion), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.ESTUDIANTE_FUNCIONARIO_ACADEMICO_DECANO_ACCESO)
    @Transactional(readOnly = true)
    @GetMapping(UUID_SOLICITUD)
    public ResponseEntity<CancelacionAsignaturaDetalleDTORespuesta> getDetalle(@PathVariable String uuidSolicitud,
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
                                            @Valid @RequestBody RemisionCancelacionAsignaturaDTOPeticion peticion,
                                            @RequestHeader("Authorization") String token) {
        return accion(uuidSolicitud, token, () -> tramiteCU.remitirADecano(uuidSolicitud,
                mapper.mapearEvaluaciones(peticion.getEvaluaciones()), peticion.getObservacion(), token.substring(7)));
    }

    @PreAuthorize(ApplicationConstantes.FUNCIONARIO_ACADEMICO_ACCESO)
    @Transactional
    @PostMapping(UUID_SOLICITUD + "/funcionario/responder")
    public ResponseEntity<?> enviarRespuesta(@PathVariable String uuidSolicitud,
                                             @RequestHeader("Authorization") String token) {
        return accion(uuidSolicitud, token, () -> tramiteCU.enviarRespuesta(uuidSolicitud, token.substring(7)));
    }

    @PreAuthorize(ApplicationConstantes.DECANO_ACCESO)
    @Transactional
    @PostMapping(UUID_SOLICITUD + "/decano/aprobar")
    public ResponseEntity<?> aprobarPorDecano(@PathVariable String uuidSolicitud,
                                              @Valid @RequestBody AprobacionCancelacionAsignaturaDTOPeticion peticion,
                                              @RequestHeader("Authorization") String token) {
        return accion(uuidSolicitud, token, () -> tramiteCU.aprobarPorDecano(uuidSolicitud,
                mapper.mapearDecisiones(peticion.getDecisiones()), token.substring(7)));
    }

    @PreAuthorize(ApplicationConstantes.DECANO_ACCESO)
    @Transactional
    @PostMapping(UUID_SOLICITUD + "/decano/rechazar")
    public ResponseEntity<?> rechazarPorDecano(@PathVariable String uuidSolicitud,
                                               @RequestBody ObservacionDTOPeticion peticion,
                                               @RequestHeader("Authorization") String token) {
        return accion(uuidSolicitud, token, () -> tramiteCU.rechazarPorDecano(uuidSolicitud, peticion.getObservacion(), token.substring(7)));
    }

    private ResponseEntity<?> accion(String uuidSolicitud, String token, Supplier<SolicitudCancelacionAsignatura> accion) {
        try {
            accion.get();
        } catch (DataAccessException ex) {
            return errorBaseDeDatos(ex);
        }
        return new ResponseEntity<CancelacionAsignaturaDetalleDTORespuesta>(
                mapper.mapearDetalleARespuesta(consultaCU.getDetalle(uuidSolicitud, token.substring(7))), HttpStatus.OK);
    }

    private ResponseEntity<Map<String, Object>> errorBaseDeDatos(DataAccessException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Error insertando en la base de datos....");
        response.put("error", ex.getMessage() + " " + ex.getMostSpecificCause().getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
