package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorUsuarios.controlador;

import com.unicauca.cfiet.solicitudes.aplicacion.input.UsuarioCUIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.PaginacionRespuestaDTO;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Funcionario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.TipoUsuario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Usuario;
import com.unicauca.cfiet.solicitudes.dominio.modelos.UsuarioLiviano;
import com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.ProcesadorArchivos;
import com.unicauca.cfiet.solicitudes.infraestructura.configuracion.lectorArchivos.validadoresArchivos.ValidadorPeticionesExcel;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorUsuarios.DTOPeticion.CambioContraseñaDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorUsuarios.DTOPeticion.UsuarioActualizarDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorUsuarios.DTOPeticion.UsuarioDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorUsuarios.DTORespuesta.TipoUsuarioDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorUsuarios.DTORespuesta.UsuarioDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorUsuarios.DTORespuesta.UsuarioLivianoDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorUsuarios.mapeador.MapperUsuarioInfraestructuraDominio;
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

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@RestController
@RequestMapping("${url.application}usuarios")
@CrossOrigin(origins = "${url.frontend}")
@Validated
@Tag(name = "Usuarios", description = "Operaciones relacionadas con la gestión de Usuarios.")
public class UsuarioRestController{
    private final UsuarioCUIntPuerto casoDeUso;
    private final MapperUsuarioInfraestructuraDominio mapper;
    private final ProcesadorArchivos<UsuarioDTOPeticion> procesadorArchivos;
    private final ValidadorPeticionesExcel<UsuarioDTOPeticion> validadorPeticion;

    public UsuarioRestController(UsuarioCUIntPuerto casoDeUso,
                                 MapperUsuarioInfraestructuraDominio mapper,
                                 @Qualifier("archivos-usuarios") ProcesadorArchivos<UsuarioDTOPeticion> procesadorArchivos,
                                 @Qualifier("validador-usuarios") ValidadorPeticionesExcel<UsuarioDTOPeticion> validadorPeticion){
        this.casoDeUso = casoDeUso;
        this.mapper = mapper;
        this.procesadorArchivos = procesadorArchivos;
        this.validadorPeticion = validadorPeticion;
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @GetMapping("/tipos")
    public ResponseEntity<?> getTiposUsuarios(){
        List<TipoUsuario> tiposUsuario = casoDeUso.getTiposUsuario();
        return new ResponseEntity<List<TipoUsuarioDTORespuesta>>(
                mapper.mapearTipoUsuarioARespuesta(tiposUsuario), HttpStatus.OK
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @GetMapping("/paginado")
    public ResponseEntity<?> indexPaginado(@RequestParam("pagina") int pagina,
                                           @RequestParam("tamanio") int tamanio) {
        var respuesta = casoDeUso.getUsuarios(pagina, tamanio);
        return new ResponseEntity<>(
                new PaginacionRespuestaDTO<>(
                        mapper.mapearModelosARespuestaLiviano(respuesta.getContent()),
                        respuesta.getTotalElements()
                ),
                HttpStatus.OK
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @GetMapping("/filtro")
    public ResponseEntity<?> getUsuariosByNombresApellidos(
            @RequestParam(value = "nombreCompleto", required = false) String nombreCompleto,
            @RequestParam("pagina") int pagina,
            @RequestParam("tamanio") int tamanio) {
        var respuesta = casoDeUso.getUsuariosByNombreCompleto(nombreCompleto, pagina, tamanio);
        return new ResponseEntity<>(
                new PaginacionRespuestaDTO<>(
                        mapper.mapearModelosARespuestaLiviano(respuesta.getContent()),
                        respuesta.getTotalElements()
                ),
                HttpStatus.OK
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @GetMapping
    public ResponseEntity<?> index(){
        List<UsuarioLiviano> usuarios = casoDeUso.getUsuarios();
        return new ResponseEntity<List<UsuarioLivianoDTORespuesta>>(
                mapper.mapearModelosARespuestaLiviano(usuarios), HttpStatus.OK
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @GetMapping("/funcionarios")
    public ResponseEntity<?> getFuncionarios(){
        List<Funcionario> funcionarios = casoDeUso.getFuncionarios();
        return new ResponseEntity<List<UsuarioLivianoDTORespuesta>>(
                mapper.mapearModelosARespuestaFuncionario(funcionarios), HttpStatus.OK
        );
    }

    @GetMapping("/{uuidUsuario}")
    public ResponseEntity<?> getUsuario(@PathVariable String uuidUsuario){
        Usuario usuario = casoDeUso.getUsuario(uuidUsuario);
        return new ResponseEntity<UsuarioDTORespuesta>(
                mapper.mapearModeloARespuesta(usuario), HttpStatus.OK
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @Transactional
    @PostMapping
    public ResponseEntity<?> crearUsuario(@Valid @RequestBody UsuarioDTOPeticion peticion, @RequestParam String tipoUsuario,
                                          @RequestHeader("Authorization") String token){
        Usuario usuario;
        try{
            usuario = casoDeUso.crearUsuario(mapper.mapearPeticionAModelo(peticion), tipoUsuario, token.substring(7));
        } catch (DataAccessException ex){
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error insertando en la base de datos....");
            response.put("error", ex.getMessage() + " " + ex.getMostSpecificCause().getMessage());
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<UsuarioDTORespuesta>(
                mapper.mapearModeloARespuesta(usuario), HttpStatus.OK
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @Transactional
    @PostMapping("/cargar/archivo")
    public ResponseEntity<?> crearUsuarios(@RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String token){
        List<UsuarioDTOPeticion> peticiones = procesadorArchivos.procesarArchivo(file);
        Map<String, String> erroresPeticiones;
        for(UsuarioDTOPeticion peticion : peticiones) {
            erroresPeticiones = validadorPeticion.validar(peticion);
            if(erroresPeticiones != null)
                return new ResponseEntity<Map<String, String>>(erroresPeticiones, HttpStatus.BAD_REQUEST);
        }

        List<Usuario> usuarios = mapper.mapearPeticionesAModelo(peticiones);
        List<Usuario> respuesta;
        try {
            respuesta = casoDeUso.crearUsuarios(usuarios, token.substring(7));
        } catch (DataAccessException ex){
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error insertando en la base de datos....");
            response.put("error", ex.getMessage() + " " + ex.getMostSpecificCause().getMessage());
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<List<UsuarioDTORespuesta>>(
                mapper.mapearModelosARespuesta(respuesta), HttpStatus.OK
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @Transactional
    @PutMapping("/{uuidUsuario}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable String uuidUsuario, @Valid @RequestBody UsuarioActualizarDTOPeticion peticion,
                                               @RequestHeader("Authorization") String token){
        Usuario usuario;
        try{
            usuario = casoDeUso.actualizarUsuario(uuidUsuario, mapper.mapearPeticionActualizarAModelo(peticion), token.substring(7));
        } catch (DataAccessException ex){
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error insertando en la base de datos....");
            response.put("error", ex.getMessage() + " " + ex.getMostSpecificCause().getMessage());
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<UsuarioDTORespuesta>(
                mapper.mapearModeloARespuesta(usuario), HttpStatus.OK
        );
    }

    @Transactional
    @PatchMapping("/{uuidUsuario}")
    public ResponseEntity<?> actualizarContraseña(@PathVariable String uuidUsuario, @Valid @RequestBody CambioContraseñaDTOPeticion peticion,
                                                  @RequestHeader("Authorization") String token){
        try{
            casoDeUso.cambiarContraseña(uuidUsuario, peticion.getContraseña(), peticion.getNuevaContraseña(), token.substring(7));
        } catch (DataAccessException ex){
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error insertando en la base de datos....");
            response.put("error", ex.getMessage() + " " + ex.getMostSpecificCause().getMessage());
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok().build();
    }
}
