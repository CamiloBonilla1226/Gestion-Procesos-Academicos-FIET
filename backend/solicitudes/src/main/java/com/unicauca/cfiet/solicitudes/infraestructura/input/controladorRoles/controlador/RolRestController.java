package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorRoles.controlador;

import com.unicauca.cfiet.solicitudes.aplicacion.input.RolCUIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.helper.constantes.ApplicationConstantes;
import com.unicauca.cfiet.solicitudes.dominio.modelos.Rol;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorRoles.DTOPeticion.RolDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorRoles.DTORespuesta.RolDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorRoles.mapeador.MapperRolInfraestructuraDominio;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@RestController
@RequestMapping("${url.application}roles")
@CrossOrigin(origins = "${url.frontend}")
@Validated
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Operaciones relacionadas con la gestión de roles.")
public class RolRestController {
    private final RolCUIntPuerto casoDeUso;
    private final MapperRolInfraestructuraDominio mapper;

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @GetMapping
    public ResponseEntity<List<RolDTORespuesta>> index(){
        List<Rol> roles = casoDeUso.getRoles();
        return new ResponseEntity<List<RolDTORespuesta>>(
                mapper.mapearModelosARespuesta(roles), HttpStatus.OK
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @GetMapping("/paginado")
    public ResponseEntity<List<RolDTORespuesta>> indexPaginado(@RequestParam("pagina") int pagina, @RequestParam("tamanio") int tamanio){
        List<Rol> roles = casoDeUso.getRoles(pagina, tamanio);
        return new ResponseEntity<List<RolDTORespuesta>>(
                mapper.mapearModelosARespuesta(roles), HttpStatus.OK
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @GetMapping("/{uuidRol}")
    public ResponseEntity<RolDTORespuesta> getRol(@PathVariable String uuidRol){
        Rol rol = casoDeUso.getRol(uuidRol);
        return new ResponseEntity<RolDTORespuesta>(
                mapper.mapearModeloARespuesta(rol), HttpStatus.OK
        );
    }

    @PreAuthorize(ApplicationConstantes.SECRETARIO_DECANO_ACCESO)
    @PutMapping("/{uuidRol}")
    @Transactional
    public ResponseEntity<?> actualizarRol(@PathVariable String uuidRol, @Valid @RequestBody RolDTOPeticion rolPeticion,
                                           @RequestHeader("Authorization") String token){
        Rol rol;
        try {
             rol = casoDeUso.actualizarRol(uuidRol, mapper.mapearPeticionAModelo(rolPeticion), token.substring(7));
        } catch (DataAccessException ex){
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error insertando en la base de datos....");
            response.put("error", ex.getMessage() + " " + ex.getMostSpecificCause().getMessage());
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<RolDTORespuesta>(
                mapper.mapearModeloARespuesta(rol), HttpStatus.OK
        );
    }
}
