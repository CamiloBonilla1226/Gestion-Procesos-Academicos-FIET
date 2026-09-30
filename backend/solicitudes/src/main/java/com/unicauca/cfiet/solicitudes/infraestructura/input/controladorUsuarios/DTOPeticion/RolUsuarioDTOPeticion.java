package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorUsuarios.DTOPeticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
@Setter
@NoArgsConstructor
public class RolUsuarioDTOPeticion {
    @NotNull(message = "El UUID del rol no puede ser nulo")
    @NotBlank(message = "El UUID del rol no puede estar vacío")
    private String uuidRol;

    @NotNull(message = "El nombre del rol no puede ser nulo")
    @NotBlank(message = "El nombre del rol no puede estar vacío")
    private String nombre;

    @NotNull(message = "La descripción del rol no puede ser nula")
    @NotBlank(message = "La descripción del rol no puede estar vacía")
    private String descripcion;

    @NotNull(message = "El estado del rol no puede ser nulo")
    private Boolean estado;
}
