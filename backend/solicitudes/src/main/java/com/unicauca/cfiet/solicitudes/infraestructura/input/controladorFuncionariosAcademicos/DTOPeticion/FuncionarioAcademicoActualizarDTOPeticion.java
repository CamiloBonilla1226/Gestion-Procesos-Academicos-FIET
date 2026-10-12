package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorFuncionariosAcademicos.DTOPeticion;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FuncionarioAcademicoActualizarDTOPeticion {
    @Size(max = 1000, message = "El nombre debe tener maximo 1000 caracteres")
    private String nombres;

    @Size(max = 1000, message = "El apellido debe tener maximo 1000 caracteres")
    private String apellidos;

    @Pattern(regexp = "^\\s*$|^.{5,1000}$", message = "El tipo de documento debe tener entre 5 y 1000 caracteres")
    private String tipoDocumento;

    @Pattern(regexp = "^\\s*$|^.{5,255}$", message = "El número de documento debe tener entre 5 y 255 caracteres")
    private String numeroDocumento;

    @Pattern(regexp = "^\\s*$|^.{5,1000}$", message = "El teléfono debe tener entre 5 y 1000 caracteres")
    private String telefono;

    @Email(message = "El correo electrónico debe tener un formato válido")
    @Pattern(regexp = "^\\s*$|^.{5,1000}$", message = "El correo electrónico debe tener entre 5 y 1000 caracteres")
    private String correoElectronico;

    @Size(max = 100, message = "El cargo debe tener máximo 100 caracteres")
    private String cargo;
}
