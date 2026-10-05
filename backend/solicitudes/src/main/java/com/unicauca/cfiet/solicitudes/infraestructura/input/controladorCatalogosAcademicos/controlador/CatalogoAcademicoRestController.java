package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCatalogosAcademicos.controlador;

import com.unicauca.cfiet.solicitudes.aplicacion.input.AsignacionFuncionarioAcademicoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.EtapaSolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.SituacionAcademicaAsignaturaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.TipoAnexoAcademicoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.aplicacion.input.TipoSolicitudAcademicaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoSolicitudAcademica;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCatalogosAcademicos.DTOPeticion.AsignacionFuncionarioAcademicoDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCatalogosAcademicos.DTORespuesta.EtapaEtiquetaRolDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCatalogosAcademicos.DTORespuesta.EtapaSolicitudAcademicaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCatalogosAcademicos.DTORespuesta.SituacionAcademicaAsignaturaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCatalogosAcademicos.DTORespuesta.TipoAnexoAcademicoDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCatalogosAcademicos.DTORespuesta.TipoSolicitudAcademicaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCatalogosAcademicos.mapeador.MapperCatalogoAcademicoInfraestructuraDominio;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${url.application}catalogos-academicos")
@CrossOrigin(origins = "${url.frontend}")
@RequiredArgsConstructor
@Tag(name = "Catálogos académicos", description = "Consulta de tipos de solicitud académica, etapas, etiquetas por rol, tipos de anexo y situaciones académicas.")
public class CatalogoAcademicoRestController {
    private static final String UUID_TIPO = "/{uuidTipo:[0-9a-fA-F\\-]{36}}";

    private final TipoSolicitudAcademicaCUIntPuerto tipoSolicitudCU;
    private final EtapaSolicitudAcademicaCUIntPuerto etapaCU;
    private final TipoAnexoAcademicoCUIntPuerto tipoAnexoCU;
    private final SituacionAcademicaAsignaturaCUIntPuerto situacionCU;
    private final AsignacionFuncionarioAcademicoCUIntPuerto asignacionCU;
    private final MapperCatalogoAcademicoInfraestructuraDominio mapper;

    @PreAuthorize(ApplicationConstantes.AUTHENTICATED)
    @GetMapping("/tipos-solicitud")
    public ResponseEntity<List<TipoSolicitudAcademicaDTORespuesta>> getTiposSolicitud() {
        return new ResponseEntity<>(mapper.mapearTiposSolicitud(tipoSolicitudCU.getTiposSolicitudAcademica()), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.AUTHENTICATED)
    @GetMapping("/tipos-solicitud" + UUID_TIPO + "/etapas")
    public ResponseEntity<List<EtapaSolicitudAcademicaDTORespuesta>> getEtapasPorTipo(@PathVariable String uuidTipo) {
        return new ResponseEntity<>(mapper.mapearEtapas(etapaCU.getEtapasPorTipo(uuidTipo)), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.AUTHENTICATED)
    @GetMapping("/tipos-solicitud" + UUID_TIPO + "/tipos-anexo")
    public ResponseEntity<List<TipoAnexoAcademicoDTORespuesta>> getTiposAnexoPorTipo(@PathVariable String uuidTipo) {
        return new ResponseEntity<>(mapper.mapearTiposAnexo(tipoAnexoCU.getTiposAnexoPorTipo(uuidTipo)), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.AUTHENTICATED)
    @GetMapping("/etiquetas")
    public ResponseEntity<List<EtapaEtiquetaRolDTORespuesta>> getEtiquetasPorRol(@RequestParam("rol") String rol) {
        return new ResponseEntity<>(mapper.mapearEtiquetas(etapaCU.getEtiquetasPorRol(rol)), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.AUTHENTICATED)
    @GetMapping("/situaciones")
    public ResponseEntity<List<SituacionAcademicaAsignaturaDTORespuesta>> getSituaciones() {
        return new ResponseEntity<>(mapper.mapearSituaciones(situacionCU.getSituaciones()), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @Transactional
    @PutMapping("/tipos-solicitud" + UUID_TIPO + "/funcionario")
    public ResponseEntity<?> asignarFuncionarioAcademico(@PathVariable String uuidTipo,
                                                         @Valid @RequestBody AsignacionFuncionarioAcademicoDTOPeticion peticion,
                                                         @RequestHeader("Authorization") String token) {
        TipoSolicitudAcademica tipo;
        try {
            tipo = asignacionCU.asignarFuncionarioAcademico(uuidTipo, peticion.getFuncionarioUuid().trim(), token.substring(7));
        } catch (DataAccessException ex) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error actualizando en la base de datos....");
            response.put("error", ex.getMessage() + " " + ex.getMostSpecificCause().getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<TipoSolicitudAcademicaDTORespuesta>(mapper.mapearTipoSolicitud(tipo), HttpStatus.OK);
    }
}
