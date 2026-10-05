package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorFuncionariosAcademicos.controlador;

import com.unicauca.cfiet.solicitudes.aplicacion.input.FuncionarioAcademicoCUIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.FuncionarioAcademico;
import com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.DuplicadosFuncionariosAcademicosExcel;
import com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.ProcesadorArchivos;
import com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.validadoresArchivos.ValidadorPeticionesExcel;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorFuncionariosAcademicos.DTOPeticion.FilaFuncionarioAcademicoExcelDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorFuncionariosAcademicos.DTOPeticion.FuncionarioAcademicoActualizarDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorFuncionariosAcademicos.DTOPeticion.FuncionarioAcademicoDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorFuncionariosAcademicos.DTORespuesta.FuncionarioAcademicoDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorFuncionariosAcademicos.mapeador.MapperFuncionarioAcademicoInfraestructuraDominio;
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
@RequestMapping("${url.application}funcionarios-academicos")
@CrossOrigin(origins = "${url.frontend}")
@Validated
@Tag(name = "Funcionarios Académicos", description = "Operaciones relacionadas con los funcionarios académicos.")
public class FuncionarioAcademicoRestController {
    private static final String UUID_FUNCIONARIO = "/{uuidFuncionario:[0-9a-fA-F\\-]{36}}";

    private final FuncionarioAcademicoCUIntPuerto casoDeUso;
    private final MapperFuncionarioAcademicoInfraestructuraDominio mapper;
    private final ProcesadorArchivos<FilaFuncionarioAcademicoExcelDTOPeticion> procesadorArchivos;
    private final ValidadorPeticionesExcel<FilaFuncionarioAcademicoExcelDTOPeticion> validadorPeticion;
    private final DuplicadosFuncionariosAcademicosExcel duplicados;

    public FuncionarioAcademicoRestController(FuncionarioAcademicoCUIntPuerto casoDeUso,
                                              MapperFuncionarioAcademicoInfraestructuraDominio mapper,
                                              @Qualifier("archivos-funcionarios-academicos") ProcesadorArchivos<FilaFuncionarioAcademicoExcelDTOPeticion> procesadorArchivos,
                                              @Qualifier("validador-funcionarios-academicos") ValidadorPeticionesExcel<FilaFuncionarioAcademicoExcelDTOPeticion> validadorPeticion,
                                              DuplicadosFuncionariosAcademicosExcel duplicados) {
        this.casoDeUso = casoDeUso;
        this.mapper = mapper;
        this.procesadorArchivos = procesadorArchivos;
        this.validadorPeticion = validadorPeticion;
        this.duplicados = duplicados;
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @Transactional
    @PostMapping
    public ResponseEntity<?> crearFuncionarioAcademico(@Valid @RequestBody FuncionarioAcademicoDTOPeticion peticion,
                                                       @RequestHeader("Authorization") String token) {
        FuncionarioAcademico funcionarioAcademico;
        try {
            funcionarioAcademico = casoDeUso.crearFuncionarioAcademico(mapper.mapearPeticionAModelo(peticion), token.substring(7));
        } catch (DataAccessException ex) {
            return errorBaseDeDatos(ex);
        }
        return new ResponseEntity<FuncionarioAcademicoDTORespuesta>(mapper.mapearModeloARespuesta(funcionarioAcademico), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @Transactional
    @PostMapping("/cargar/archivo")
    public ResponseEntity<?> crearFuncionariosAcademicos(@RequestParam("file") MultipartFile file,
                                                         @RequestHeader("Authorization") String token) {
        List<FilaFuncionarioAcademicoExcelDTOPeticion> filas = procesadorArchivos.procesarArchivo(file);
        for (FilaFuncionarioAcademicoExcelDTOPeticion fila : filas) {
            Map<String, String> errores = validadorPeticion.validar(fila);
            if (errores != null)
                return new ResponseEntity<Map<String, String>>(errores, HttpStatus.BAD_REQUEST);
        }
        duplicados.verificar(filas);

        List<FuncionarioAcademico> funcionarios = filas.stream()
                .map(fila -> mapper.mapearPeticionAModelo(fila.getPeticion()))
                .toList();
        List<FuncionarioAcademico> creados;
        try {
            creados = casoDeUso.crearFuncionariosAcademicos(funcionarios, token.substring(7));
        } catch (DataAccessException ex) {
            return errorBaseDeDatos(ex);
        }
        return new ResponseEntity<List<FuncionarioAcademicoDTORespuesta>>(mapper.mapearModelosARespuesta(creados), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_FUNCIONARIO_ACADEMICO_ACCESO)
    @GetMapping("/paginado")
    public ResponseEntity<PaginacionRespuestaDTO<FuncionarioAcademicoDTORespuesta>> getFuncionariosAcademicosPaginado(
            @RequestParam("pagina") int pagina,
            @RequestParam("tamanio") int tamanio) {
        PaginacionRespuestaDTO<FuncionarioAcademico> respuesta = casoDeUso.getFuncionariosAcademicosPaginado(pagina, tamanio);
        return new ResponseEntity<>(mapper.mapearPaginaARespuesta(respuesta), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_FUNCIONARIO_ACADEMICO_ACCESO)
    @GetMapping("/filtro")
    public ResponseEntity<PaginacionRespuestaDTO<FuncionarioAcademicoDTORespuesta>> getFuncionariosAcademicosPorFiltro(
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "apellido", required = false) String apellido,
            @RequestParam(value = "dependencia", required = false) String dependencia,
            @RequestParam("pagina") int pagina,
            @RequestParam("tamanio") int tamanio) {
        PaginacionRespuestaDTO<FuncionarioAcademico> respuesta =
                casoDeUso.getFuncionariosAcademicosPorFiltro(nombre, apellido, dependencia, pagina, tamanio);
        return new ResponseEntity<>(mapper.mapearPaginaARespuesta(respuesta), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_FUNCIONARIO_ACADEMICO_ACCESO)
    @GetMapping(UUID_FUNCIONARIO)
    public ResponseEntity<FuncionarioAcademicoDTORespuesta> getFuncionarioAcademico(@PathVariable String uuidFuncionario) {
        FuncionarioAcademico funcionarioAcademico = casoDeUso.getFuncionarioAcademico(uuidFuncionario);
        return new ResponseEntity<>(mapper.mapearModeloARespuesta(funcionarioAcademico), HttpStatus.OK);
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @Transactional
    @PutMapping(UUID_FUNCIONARIO)
    public ResponseEntity<?> actualizarFuncionarioAcademico(@PathVariable String uuidFuncionario,
                                                            @Valid @RequestBody FuncionarioAcademicoActualizarDTOPeticion peticion,
                                                            @RequestHeader("Authorization") String token) {
        FuncionarioAcademico funcionarioAcademico;
        try {
            funcionarioAcademico = casoDeUso.actualizarFuncionarioAcademico(
                    uuidFuncionario, mapper.mapearActualizacionAModelo(peticion), token.substring(7));
        } catch (DataAccessException ex) {
            return errorBaseDeDatos(ex);
        }
        return new ResponseEntity<FuncionarioAcademicoDTORespuesta>(mapper.mapearModeloARespuesta(funcionarioAcademico), HttpStatus.OK);
    }

    private ResponseEntity<Map<String, Object>> errorBaseDeDatos(DataAccessException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Error insertando en la base de datos....");
        response.put("error", ex.getMessage() + " " + ex.getMostSpecificCause().getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
