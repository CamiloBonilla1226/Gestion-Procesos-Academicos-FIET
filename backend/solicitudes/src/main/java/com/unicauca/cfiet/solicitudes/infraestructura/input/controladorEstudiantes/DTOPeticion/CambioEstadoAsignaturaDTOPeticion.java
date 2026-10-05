package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.DTOPeticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CambioEstadoAsignaturaDTOPeticion {
    @NotBlank(message = "El estado no puede estar vacío")
    @Size(max = 20, message = "El estado debe tener máximo 20 caracteres")
    private String estado;
}
