package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorFuncionariosAcademicos.DTOPeticion;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FuncionarioAcademicoDTOPeticion {
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 1000, message = "El nombre debe tener maximo 1000 caracteres")
    private String nombres;

    @NotBlank(message = "El apellido no puede estar vacío")
    @Size(max = 1000, message = "El apellido debe tener maximo 1000 caracteres")
    private String apellidos;

    @NotBlank(message = "El tipo de documento no puede estar vacío")
    @Size(min = 5, max = 1000, message = "El tipo de documento debe tener entre 5 y 1000 caracteres")
    private String tipoDocumento;

    @NotBlank(message = "El número de documento no puede estar vacío")
    @Size(min = 5, max = 255, message = "El número de documento debe tener entre 5 y 255 caracteres")
    private String numeroDocumento;

    @NotBlank(message = "El teléfono no puede estar vacío")
    @Size(min = 5, max = 1000, message = "El teléfono debe tener entre 5 y 1000 caracteres")
    private String telefono;

    @NotBlank(message = "El correo electrónico no puede estar vacío")
    @Email(message = "El correo electrónico debe tener un formato válido")
    @Size(min = 5, max = 1000, message = "El correo electrónico debe tener entre 5 y 1000 caracteres")
    private String correoElectronico;

    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    @Size(min = 5, max = 255, message = "El nombre de usuario debe tener entre 5 y 255 caracteres")
    private String username;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 5, max = 255, message = "La contraseña debe tener entre 5 y 255 caracteres")
    private String password;

    @NotBlank(message = "La dependencia no puede estar vacía")
    @Size(max = 100, message = "La dependencia debe tener máximo 100 caracteres")
    private String dependencia;
}
