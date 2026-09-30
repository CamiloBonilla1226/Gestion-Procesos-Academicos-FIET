package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudes.DTOPeticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
@Setter
@NoArgsConstructor
public class InformacionSolicitanteDTOPeticion {
    @NotNull(message = "El tipo de documento no puede ser nulo")
    @NotBlank(message = "El tipo de documento no puede estar vacío")
    @Size(min = 5, max = 1000, message = "El tipo de documento debe tener entre 5 y 1000 caracteres")
    private String tipoDocumento;

    @NotNull(message = "El número de documento no puede ser nulo")
    @NotBlank(message = "El número de documento no puede estar vacío")
    @Size(min = 5, max = 1000, message = "El número de documento debe tener entre 5 y 1000 caracteres")
    private String numeroDocumento;

    @NotNull(message = "Los nombres no pueden ser nulos")
    @NotBlank(message = "Los nombres no pueden estar vacíos")
    @Size(min = 2, max = 1000, message = "Los nombres deben tener entre 2 y 1000 caracteres")
    private String nombres;

    @NotNull(message = "Los apellidos no pueden ser nulos")
    @NotBlank(message = "Los apellidos no pueden estar vacíos")
    @Size(min = 2, max = 1000, message = "Los apellidos deben tener entre 2 y 1000 caracteres")
    private String apellidos;

    @NotNull(message = "El teléfono no puede ser nulo")
    @NotBlank(message = "El teléfono no puede estar vacío")
    @Size(min = 5, max = 1000, message = "El teléfono debe tener entre 5 y 1000 caracteres")
    private String telefono;

    @NotNull(message = "El correo electrónico no puede ser nulo")
    @NotBlank(message = "El correo electrónico no puede estar vacío")
    @Size(min = 5, max = 1000, message = "El correo electrónico debe tener entre 5 y 1000 caracteres")
    private String correoElectronico;
}
