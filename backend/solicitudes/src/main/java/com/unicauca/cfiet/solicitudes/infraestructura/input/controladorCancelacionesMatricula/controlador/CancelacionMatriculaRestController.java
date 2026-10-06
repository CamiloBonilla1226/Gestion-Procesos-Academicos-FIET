package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.controlador;

import com.unicauca.cfiet.solicitudes.aplicacion.input.CancelacionMatriculaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.ConsultaCancelacionMatriculaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.TramiteCancelacionMatriculaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.SolicitudCancelacionMatricula;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.DTOPeticion.AprobacionDecanoDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.DTOPeticion.ObservacionDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.DTOPeticion.RemisionDecanoDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.DTORespuesta.CancelacionMatriculaDetalleDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.DTORespuesta.FormularioCancelacionMatriculaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.DTORespuesta.RadicacionCancelacionMatriculaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.mapeador.MapperCancelacionMatriculaInfraestructuraDominio;
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
import java.util.Map;
import java.util.function.Supplier;

@RestController
@RequestMapping("${url.application}cancelaciones-matricula")
@CrossOrigin(origins = "${url.frontend}")
@RequiredArgsConstructor
@Tag(name = "Cancelaciones de matrícula", description = "Radicación, detalle y trámite de las solicitudes de Cancelación de Matrícula.")
public class CancelacionMatriculaRestController {
    private static final String UUID_SOLICITUD = "/{uuidSolicitud:[0-9a-fA-F\\-]{36}}";

    private final CancelacionMatriculaCUIntPuerto radicacionCU;
    private final ConsultaCancelacionMatriculaCUIntPuerto consultaCU;
    private final TramiteCancelacionMatriculaCUIntPuerto tramiteCU;
    private final MapperCancelacionMatriculaInfraestructuraDominio mapper;

    @PreAuthorize(ApplicationConstantes.ESTUDIANTE_ACCESO)
    @Transactional(readOnly = true)
    @GetMapping("/formulario")
    public ResponseEntity<FormularioCancelacionMatriculaDTORespuesta> getFormulario(@RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(mapper.mapearFormularioARespuesta(consultaCU.getFormulario(token.substring(7))), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.ESTUDIANTE_ACCESO)
    @Transactional
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> radicar(@RequestParam(value = "motivo", required = false) String motivo,
                                     MultipartHttpServletRequest peticion,
                                     @RequestHeader("Authorization") String token) {
        SolicitudCancelacionMatricula cancelacion;
        try {
            String uuidEstudiante = consultaCU.getEstudianteAutenticado(token.substring(7));
            cancelacion = radicacionCU.radicarCancelacionMatricula(uuidEstudiante, motivo,
                    mapper.mapearAnexos(peticion.getMultiFileMap()), token.substring(7));
        } catch (DataAccessException ex) {
            return errorBaseDeDatos(ex);
        }
        return new ResponseEntity<RadicacionCancelacionMatriculaDTORespuesta>(mapper.mapearRadicacionARespuesta(cancelacion), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.ESTUDIANTE_FUNCIONARIO_ACADEMICO_DECANO_ACCESO)
    @Transactional(readOnly = true)
    @GetMapping(UUID_SOLICITUD)
    public ResponseEntity<CancelacionMatriculaDetalleDTORespuesta> getDetalle(@PathVariable String uuidSolicitud,
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
                                            @Valid @RequestBody RemisionDecanoDTOPeticion peticion,
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
                                              @Valid @RequestBody AprobacionDecanoDTOPeticion peticion,
                                              @RequestHeader("Authorization") String token) {
        return accion(uuidSolicitud, token, () -> tramiteCU.aprobarPorDecano(uuidSolicitud,
                mapper.mapearSituaciones(peticion.getSituaciones()), token.substring(7)));
    }

    @PreAuthorize(ApplicationConstantes.DECANO_ACCESO)
    @Transactional
    @PostMapping(UUID_SOLICITUD + "/decano/rechazar")
    public ResponseEntity<?> rechazarPorDecano(@PathVariable String uuidSolicitud,
                                               @RequestBody ObservacionDTOPeticion peticion,
                                               @RequestHeader("Authorization") String token) {
        return accion(uuidSolicitud, token, () -> tramiteCU.rechazarPorDecano(uuidSolicitud, peticion.getObservacion(), token.substring(7)));
    }

    private ResponseEntity<?> accion(String uuidSolicitud, String token, Supplier<SolicitudCancelacionMatricula> accion) {
        try {
            accion.get();
        } catch (DataAccessException ex) {
            return errorBaseDeDatos(ex);
        }
        return new ResponseEntity<CancelacionMatriculaDetalleDTORespuesta>(
                mapper.mapearDetalleARespuesta(consultaCU.getDetalle(uuidSolicitud, token.substring(7))), HttpStatus.OK);
    }

    private ResponseEntity<Map<String, Object>> errorBaseDeDatos(DataAccessException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Error insertando en la base de datos....");
        response.put("error", ex.getMessage() + " " + ex.getMostSpecificCause().getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
