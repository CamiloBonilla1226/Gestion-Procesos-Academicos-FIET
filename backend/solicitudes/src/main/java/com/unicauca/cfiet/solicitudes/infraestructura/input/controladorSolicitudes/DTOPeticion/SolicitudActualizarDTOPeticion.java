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
public class SolicitudActualizarDTOPeticion {
    private String consecutivo;

    @NotNull(message = "El nombre no puede ser nulo")
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 5, max = 1000, message = "El nombre debe tener entre 5 y 1000 caracteres")
    private String nombre;

    @Size(max = 1500, message = "La descripción debe tener maximo 1500 caracteres")
    private String descripcion;

    @NotNull(message = "El estado no puede ser nulo")
    @NotBlank(message = "El estado no puede estar vacío")
    @Size(min = 5, max = 1000, message = "El estado debe tener entre 5 y 1000 caracteres")
    private String estado;

    private String uuidFuncionario;

    private String uuidOrdenDelDia;
}
