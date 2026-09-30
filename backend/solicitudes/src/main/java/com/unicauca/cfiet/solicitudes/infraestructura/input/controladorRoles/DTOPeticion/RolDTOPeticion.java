package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorRoles.DTOPeticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RolDTOPeticion {
    @NotNull(message = "La descripción no puede ser nula")
    @NotBlank(message = "La descripción no puede estar vacía")
    @Size(min = 5, max = 1500, message = "La descripción debe tener entre 5 y 1500 caracteres")
    private String descripcion;

    @NotNull(message = "El estado no puede ser nulo")
    private Boolean estado;
}
