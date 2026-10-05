package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCatalogosAcademicos.DTOPeticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AsignacionFuncionarioAcademicoDTOPeticion {
    @NotBlank(message = "El uuid del funcionario académico no puede estar vacío")
    @Size(max = 100, message = "El uuid del funcionario académico debe tener máximo 100 caracteres")
    private String funcionarioUuid;
}
