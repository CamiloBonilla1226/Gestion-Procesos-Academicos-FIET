package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.controlador;

import com.unicauca.cfiet.solicitudes.aplicacion.input.EstudianteCUIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.AsignaturaMatriculada;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Estudiante;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.DTOPeticion.AsignaturaMatriculadaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.DTOPeticion.CambioEstadoAsignaturaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.DTOPeticion.EstudianteActualizarDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.DTOPeticion.EstudianteDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.DTORespuesta.AsignaturaMatriculadaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.DTORespuesta.EstudianteDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.mapeador.MapperEstudianteInfraestructuraDominio;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${url.application}estudiantes")
@CrossOrigin(origins = "${url.frontend}")
@Validated
@RequiredArgsConstructor
@Tag(name = "Estudiantes", description = "Operaciones relacionadas con los estudiantes y sus asignaturas matriculadas.")
public class EstudianteRestController {
    private static final String UUID_ESTUDIANTE = "/{uuidEstudiante:[0-9a-fA-F\\-]{36}}";
    private static final String UUID_MATRICULA = "/{uuidMatricula:[0-9a-fA-F\\-]{36}}";

    private final EstudianteCUIntPuerto casoDeUso;
    private final MapperEstudianteInfraestructuraDominio mapper;

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @Transactional
    @PostMapping
    public ResponseEntity<?> crearEstudiante(@Valid @RequestBody EstudianteDTOPeticion peticion,
                                             @RequestHeader("Authorization") String token) {
        Estudiante estudiante;
        try {
            estudiante = casoDeUso.crearEstudiante(mapper.mapearPeticionAModelo(peticion), token.substring(7));
        } catch (DataAccessException ex) {
            return errorBaseDeDatos(ex);
        }
        return new ResponseEntity<EstudianteDTORespuesta>(mapper.mapearModeloARespuesta(estudiante), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_FUNCIONARIO_ACADEMICO_ACCESO)
    @GetMapping("/paginado")
    public ResponseEntity<PaginacionRespuestaDTO<EstudianteDTORespuesta>> getEstudiantesPaginado(
            @RequestParam("pagina") int pagina,
            @RequestParam("tamanio") int tamanio) {
        PaginacionRespuestaDTO<Estudiante> respuesta = casoDeUso.getEstudiantesPaginado(pagina, tamanio);
        return new ResponseEntity<>(mapper.mapearPaginaARespuesta(respuesta), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_FUNCIONARIO_ACADEMICO_ACCESO)
    @GetMapping("/filtro")
    public ResponseEntity<PaginacionRespuestaDTO<EstudianteDTORespuesta>> getEstudiantesPorFiltro(
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "apellido", required = false) String apellido,
            @RequestParam(value = "codigo", required = false) String codigo,
            @RequestParam("pagina") int pagina,
            @RequestParam("tamanio") int tamanio) {
        PaginacionRespuestaDTO<Estudiante> respuesta = casoDeUso.getEstudiantesPorFiltro(nombre, apellido, codigo, pagina, tamanio);
        return new ResponseEntity<>(mapper.mapearPaginaARespuesta(respuesta), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.ESTUDIANTE_ACCESO)
    @GetMapping("/mis-asignaturas")
    public ResponseEntity<List<AsignaturaMatriculadaDTORespuesta>> getMisAsignaturas(@RequestHeader("Authorization") String token) {
        List<AsignaturaMatriculada> materias = casoDeUso.getMisAsignaturas(token.substring(7));
        return new ResponseEntity<>(mapper.mapearMateriasARespuesta(materias), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_FUNCIONARIO_ACADEMICO_ACCESO)
    @GetMapping(UUID_ESTUDIANTE)
    public ResponseEntity<EstudianteDTORespuesta> getEstudiante(@PathVariable String uuidEstudiante) {
        Estudiante estudiante = casoDeUso.getEstudiante(uuidEstudiante);
        return new ResponseEntity<>(mapper.mapearModeloARespuesta(estudiante), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @Transactional
    @PutMapping(UUID_ESTUDIANTE)
    public ResponseEntity<?> actualizarEstudiante(@PathVariable String uuidEstudiante,
                                                  @Valid @RequestBody EstudianteActualizarDTOPeticion peticion,
                                                  @RequestHeader("Authorization") String token) {
        Estudiante estudiante;
        try {
            estudiante = casoDeUso.actualizarEstudiante(uuidEstudiante, mapper.mapearActualizacionAModelo(peticion), token.substring(7));
        } catch (DataAccessException ex) {
            return errorBaseDeDatos(ex);
        }
        return new ResponseEntity<EstudianteDTORespuesta>(mapper.mapearModeloARespuesta(estudiante), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @Transactional
    @PostMapping(UUID_ESTUDIANTE + "/asignaturas")
    public ResponseEntity<?> agregarAsignaturaMatriculada(@PathVariable String uuidEstudiante,
                                                          @Valid @RequestBody AsignaturaMatriculadaDTOPeticion peticion,
                                                          @RequestHeader("Authorization") String token) {
        AsignaturaMatriculada materia;
        try {
            materia = casoDeUso.agregarAsignaturaMatriculada(uuidEstudiante, mapper.mapearMateriaAModelo(peticion), token.substring(7));
        } catch (DataAccessException ex) {
            return errorBaseDeDatos(ex);
        }
        return new ResponseEntity<AsignaturaMatriculadaDTORespuesta>(mapper.mapearMateriaARespuesta(materia), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @Transactional
    @PatchMapping(UUID_ESTUDIANTE + "/asignaturas" + UUID_MATRICULA + "/estado")
    public ResponseEntity<?> cambiarEstadoAsignatura(@PathVariable String uuidEstudiante,
                                                     @PathVariable String uuidMatricula,
                                                     @Valid @RequestBody CambioEstadoAsignaturaDTOPeticion peticion,
                                                     @RequestHeader("Authorization") String token) {
        AsignaturaMatriculada materia;
        try {
            materia = casoDeUso.cambiarEstadoAsignatura(uuidEstudiante, uuidMatricula, peticion.getEstado(), token.substring(7));
        } catch (DataAccessException ex) {
            return errorBaseDeDatos(ex);
        }
        return new ResponseEntity<AsignaturaMatriculadaDTORespuesta>(mapper.mapearMateriaARespuesta(materia), HttpStatus.OK);
    }

    private ResponseEntity<Map<String, Object>> errorBaseDeDatos(DataAccessException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Error insertando en la base de datos....");
        response.put("error", ex.getMessage() + " " + ex.getMostSpecificCause().getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
