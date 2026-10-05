package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorEstudiantes.DTOPeticion;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstudianteActualizarDTOPeticion {
    @Size(max = 45, message = "El código estudiantil debe tener máximo 45 caracteres")
    private String codigoEstudiantil;

    @Size(max = 100, message = "El programa académico debe tener máximo 100 caracteres")
    private String programaAcademico;

    @Size(max = 10, message = "El semestre debe tener máximo 10 caracteres")
    private String semestre;

    @Size(max = 100, message = "La facultad debe tener máximo 100 caracteres")
    private String facultad;
}
