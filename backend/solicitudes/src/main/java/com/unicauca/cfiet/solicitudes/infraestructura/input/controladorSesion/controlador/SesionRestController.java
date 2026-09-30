package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSesion.controlador;

import com.unicauca.cfiet.solicitudes.aplicacion.input.SesionCUIntPuerto;
import com.unicauca.cfiet.solicitudes.dominio.modelos.UsuarioTokenizado;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSesion.DTOPeticion.SesionDTOPeticion;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSesion.DTORespuesta.UsuarioTokenizadoDTORespuesta;
import com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSesion.mapeador.MapperSesionInfraestructuraDominio;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@RestController
@RequestMapping("${url.application}sesiones")
@Validated
@RequiredArgsConstructor
@CrossOrigin(origins = "${url.frontend}")
@Tag(name = "Sesión", description = "Autenticación en la aplicación.")
public class SesionRestController {
    private final SesionCUIntPuerto casoDeUso;
    private final MapperSesionInfraestructuraDominio mapper;

    @PostMapping
    public ResponseEntity<?> auth(@Valid @RequestBody SesionDTOPeticion peticion){
        UsuarioTokenizado usuario = casoDeUso.login(peticion.getUsername(), peticion.getPassword());
        return new ResponseEntity<UsuarioTokenizadoDTORespuesta>(
                mapper.mapearModeloARespuesta(usuario) , HttpStatus.OK
        );
    }
}
