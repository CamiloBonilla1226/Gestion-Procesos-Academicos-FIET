package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorAsignaturas.controlador;

import com.unicauca.cfiet.solicitudes.aplicacion.input.AsignaturaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Asignatura;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorAsignaturas.DTOPeticion.AsignaturaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorAsignaturas.DTORespuesta.AsignaturaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorAsignaturas.mapeador.MapperAsignaturaInfraestructuraDominio;
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
import java.util.Map;

@RestController
@RequestMapping("${url.application}asignaturas")
@CrossOrigin(origins = "${url.frontend}")
@Validated
@RequiredArgsConstructor
@Tag(name = "Asignaturas", description = "Operaciones relacionadas con el catálogo de asignaturas.")
public class AsignaturaRestController {
    private static final String UUID_ASIGNATURA = "/{uuidAsignatura:[0-9a-fA-F\\-]{36}}";

    private final AsignaturaCUIntPuerto casoDeUso;
    private final MapperAsignaturaInfraestructuraDominio mapper;

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @Transactional
    @PostMapping
    public ResponseEntity<?> crearAsignatura(@Valid @RequestBody AsignaturaDTOPeticion peticion,
                                             @RequestHeader("Authorization") String token) {
        Asignatura asignatura;
        try {
            asignatura = casoDeUso.crearAsignatura(mapper.mapearPeticionAModelo(peticion), token.substring(7));
        } catch (DataAccessException ex) {
            return errorBaseDeDatos(ex);
        }
        return new ResponseEntity<AsignaturaDTORespuesta>(mapper.mapearModeloARespuesta(asignatura), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_FUNCIONARIO_ACADEMICO_ACCESO)
    @GetMapping("/paginado")
    public ResponseEntity<PaginacionRespuestaDTO<AsignaturaDTORespuesta>> getAsignaturasPaginado(
            @RequestParam("pagina") int pagina,
            @RequestParam("tamanio") int tamanio) {
        PaginacionRespuestaDTO<Asignatura> respuesta = casoDeUso.getAsignaturasPaginado(pagina, tamanio);
        return new ResponseEntity<>(mapper.mapearPaginaARespuesta(respuesta), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_FUNCIONARIO_ACADEMICO_ACCESO)
    @GetMapping("/filtro")
    public ResponseEntity<PaginacionRespuestaDTO<AsignaturaDTORespuesta>> getAsignaturasPorFiltro(
            @RequestParam(value = "texto", required = false) String texto,
            @RequestParam("pagina") int pagina,
            @RequestParam("tamanio") int tamanio) {
        PaginacionRespuestaDTO<Asignatura> respuesta = casoDeUso.getAsignaturasPorFiltro(texto, pagina, tamanio);
        return new ResponseEntity<>(mapper.mapearPaginaARespuesta(respuesta), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_FUNCIONARIO_ACADEMICO_ACCESO)
    @GetMapping(UUID_ASIGNATURA)
    public ResponseEntity<AsignaturaDTORespuesta> getAsignatura(@PathVariable String uuidAsignatura) {
        Asignatura asignatura = casoDeUso.getAsignatura(uuidAsignatura);
        return new ResponseEntity<>(mapper.mapearModeloARespuesta(asignatura), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @Transactional
    @PutMapping(UUID_ASIGNATURA)
    public ResponseEntity<?> actualizarAsignatura(@PathVariable String uuidAsignatura,
                                                  @Valid @RequestBody AsignaturaDTOPeticion peticion,
                                                  @RequestHeader("Authorization") String token) {
        Asignatura asignatura;
        try {
            asignatura = casoDeUso.actualizarAsignatura(uuidAsignatura, mapper.mapearPeticionAModelo(peticion), token.substring(7));
        } catch (DataAccessException ex) {
            return errorBaseDeDatos(ex);
        }
        return new ResponseEntity<AsignaturaDTORespuesta>(mapper.mapearModeloARespuesta(asignatura), HttpStatus.OK);
    }

    private ResponseEntity<Map<String, Object>> errorBaseDeDatos(DataAccessException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Error insertando en la base de datos....");
        response.put("error", ex.getMessage() + " " + ex.getMostSpecificCause().getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
