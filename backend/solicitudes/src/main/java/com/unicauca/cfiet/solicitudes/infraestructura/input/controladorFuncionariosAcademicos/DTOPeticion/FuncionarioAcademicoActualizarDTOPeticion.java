package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorFuncionariosAcademicos.DTOPeticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FuncionarioAcademicoActualizarDTOPeticion {
    @NotBlank(message = "La dependencia no puede estar vacía")
    @Size(max = 100, message = "La dependencia debe tener máximo 100 caracteres")
    private String dependencia;
}
