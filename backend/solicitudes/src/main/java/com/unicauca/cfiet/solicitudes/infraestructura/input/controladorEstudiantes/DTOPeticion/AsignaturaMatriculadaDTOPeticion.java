package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.DTOPeticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AsignaturaMatriculadaDTOPeticion {
    @NotBlank(message = "El código de la asignatura no puede estar vacío")
    @Size(max = 45, message = "El código de la asignatura debe tener máximo 45 caracteres")
    private String codigoAsignatura;

    @NotBlank(message = "El nombre de la asignatura no puede estar vacío")
    @Size(max = 150, message = "El nombre de la asignatura debe tener máximo 150 caracteres")
    private String nombreAsignatura;

    @NotBlank(message = "El grupo no puede estar vacío")
    @Size(max = 20, message = "El grupo debe tener máximo 20 caracteres")
    private String grupo;
}
