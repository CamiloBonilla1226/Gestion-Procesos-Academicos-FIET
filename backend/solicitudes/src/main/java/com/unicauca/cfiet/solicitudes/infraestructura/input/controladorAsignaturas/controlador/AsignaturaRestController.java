package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorAsignaturas.controlador;

import com.unicauca.cfiet.solicitudes.aplicacion.input.AsignaturaCUIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Asignatura;
import com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.CatalogoAcademicoExcel;
import com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.ProcesadorArchivos;
import com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.validadoresArchivos.ValidadorPeticionesExcel;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorAsignaturas.DTOPeticion.AsignaturaActualizarDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorAsignaturas.DTOPeticion.AsignaturaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorAsignaturas.DTOPeticion.FilaAsignaturaExcelDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorAsignaturas.DTORespuesta.AsignaturaDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorAsignaturas.mapeador.MapperAsignaturaInfraestructuraDominio;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${url.application}asignaturas")
@CrossOrigin(origins = "${url.frontend}")
@Validated
@Tag(name = "Asignaturas", description = "Operaciones relacionadas con el catálogo de asignaturas.")
public class AsignaturaRestController {
    private static final String UUID_ASIGNATURA = "/{uuidAsignatura:[0-9a-fA-F\\-]{36}}";
    private static final String UUID_PROGRAMA = "/{uuidPrograma:[0-9a-fA-F\\-]{36}}";

    private final AsignaturaCUIntPuerto casoDeUso;
    private final MapperAsignaturaInfraestructuraDominio mapper;
    private final ProcesadorArchivos<FilaAsignaturaExcelDTOPeticion> procesadorArchivos;
    private final ValidadorPeticionesExcel<FilaAsignaturaExcelDTOPeticion> validadorPeticion;
    private final CatalogoAcademicoExcel catalogoExcel;

    public AsignaturaRestController(AsignaturaCUIntPuerto casoDeUso,
                                    MapperAsignaturaInfraestructuraDominio mapper,
                                    @Qualifier("archivos-asignaturas") ProcesadorArchivos<FilaAsignaturaExcelDTOPeticion> procesadorArchivos,
                                    @Qualifier("validador-asignaturas") ValidadorPeticionesExcel<FilaAsignaturaExcelDTOPeticion> validadorPeticion,
                                    CatalogoAcademicoExcel catalogoExcel) {
        this.casoDeUso = casoDeUso;
        this.mapper = mapper;
        this.procesadorArchivos = procesadorArchivos;
        this.validadorPeticion = validadorPeticion;
        this.catalogoExcel = catalogoExcel;
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @Transactional
    @PostMapping("/cargar/archivo")
    public ResponseEntity<?> crearAsignaturas(@RequestParam("file") MultipartFile file,
                                              @RequestHeader("Authorization") String token) {
        List<FilaAsignaturaExcelDTOPeticion> filas = procesadorArchivos.procesarArchivo(file);
        catalogoExcel.resolverProgramasAsignaturas(filas);
        for (FilaAsignaturaExcelDTOPeticion fila : filas) {
            Map<String, String> errores = validadorPeticion.validar(fila);
            if (errores != null)
                return new ResponseEntity<Map<String, String>>(errores, HttpStatus.BAD_REQUEST);
        }
        catalogoExcel.verificarNombresAsignaturas(filas);

        List<Asignatura> asignaturas = filas.stream()
                .map(fila -> mapper.mapearPeticionAModelo(fila.getPeticion()))
                .toList();
        List<Asignatura> creadas;
        try {
            creadas = casoDeUso.crearAsignaturas(asignaturas, token.substring(7));
        } catch (DataAccessException ex) {
            return errorBaseDeDatos(ex);
        }
        return new ResponseEntity<List<AsignaturaDTORespuesta>>(mapper.mapearModelosARespuesta(creadas), HttpStatus.OK);
    }

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
            @RequestParam(value = "uuidProgramaAcademico", required = false) String uuidProgramaAcademico,
            @RequestParam("pagina") int pagina,
            @RequestParam("tamanio") int tamanio) {
        PaginacionRespuestaDTO<Asignatura> respuesta = casoDeUso.getAsignaturasPorFiltro(texto, uuidProgramaAcademico, pagina, tamanio);
        return new ResponseEntity<>(mapper.mapearPaginaARespuesta(respuesta), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_FUNCIONARIO_ACADEMICO_ACCESO)
    @GetMapping("/programa" + UUID_PROGRAMA)
    public ResponseEntity<List<AsignaturaDTORespuesta>> getAsignaturasPorPrograma(@PathVariable String uuidPrograma) {
        List<Asignatura> asignaturas = casoDeUso.getAsignaturasPorPrograma(uuidPrograma);
        return new ResponseEntity<>(mapper.mapearModelosARespuesta(asignaturas), HttpStatus.OK);
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
                                                  @Valid @RequestBody AsignaturaActualizarDTOPeticion peticion,
                                                  @RequestHeader("Authorization") String token) {
        Asignatura asignatura;
        try {
            asignatura = casoDeUso.actualizarAsignatura(uuidAsignatura, mapper.mapearActualizacionAModelo(peticion), token.substring(7));
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
