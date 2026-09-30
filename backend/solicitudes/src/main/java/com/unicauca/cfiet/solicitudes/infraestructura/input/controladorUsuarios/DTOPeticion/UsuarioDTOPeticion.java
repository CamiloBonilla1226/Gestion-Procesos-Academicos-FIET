package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorUsuarios.DTOPeticion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@SuperBuilder
@Getter
@Setter
public class UsuarioDTOPeticion extends UsuarioLivianoDTOPeticion{
    @NotNull(message = "El tipo de documento no puede ser nulo")
    @NotBlank(message = "El tipo de documento no puede estar vacío")
    @Size(min = 5, max = 1000, message = "El tipo de documento debe tener entre 5 y 1000 caracteres")
    private String tipoDocumento;

    @NotNull(message = "El número de documento no puede ser nulo")
    @NotBlank(message = "El número de documento no puede estar vacío")
    @Size(min = 5, max = 255, message = "El número de documento debe tener entre 5 y 255 caracteres")
    private String numeroDocumento;

    @NotNull(message = "El teléfono no puede ser nulo")
    @NotBlank(message = "El teléfono no puede estar vacío")
    @Size(min = 5, max = 1000, message = "El teléfono debe tener entre 5 y 1000 caracteres")
    private String telefono;

    @NotNull(message = "El correo electrónico no puede ser nulo")
    @NotBlank(message = "El correo electrónico no puede estar vacío")
    @Email(message = "El correo electrónico debe tener un formato válido")
    @Size(min = 5, max = 1000, message = "El correo electrónico debe tener entre 5 y 1000 caracteres")
    private String correoElectronico;


    @NotNull(message = "El nombre de usuario no puede ser nulo")
    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    @Size(min = 5, max = 255, message = "El nombre de usuario debe tener entre 5 y 255 caracteres")
    private String username;

    @NotNull(message = "La contraseña no puede ser nula")
    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 5, max = 255, message = "La contraseña debe tener entre 5 y 255 caracteres")
    private String password;

    @Valid
    @NotNull(message = "El tipo de usuario no puede ser nulo")
    private TipoUsuarioDTOPeticion objTipoUsuario;

    @Valid
    @NotNull(message = "La lista de roles no puede ser nula")
    @Size(min = 1, message = "Debe asignar al menos un rol")
    private List<RolUsuarioDTOPeticion> roles;


    public UsuarioDTOPeticion(){
        super();
        this.roles = new ArrayList<>();
    }
}
