package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorUsuarios.DTOPeticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@SuperBuilder
@Getter
@Setter
public class UsuarioLivianoDTOPeticion {
    @NotNull(message = "El nombre no puede ser nulo")
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 1000, message = "El nombre debe tener maximo 1000 caracteres")
    private String nombres;

    @NotNull(message = "El apellido no puede ser nulo")
    @NotBlank(message = "El apellido no puede estar vacío")
    @Size(max = 1000, message = "El apellido debe tener maximo 1000 caracteres")
    private String apellidos;

    @NotNull(message = "El estado no puede ser nulo")
    private Boolean estado;

    public UsuarioLivianoDTOPeticion(){}
}