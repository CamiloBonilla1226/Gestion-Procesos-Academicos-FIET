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
public class OrdenDelDiaDTOPeticion {
    @NotNull(message = "El nombre no puede ser nulo")
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 5, max = 1000, message = "El nombre debe tener entre 5 y 1000 caracteres")
    private String nombre;

    @Size(max = 1500, message = "La descripción debe tener maximo 1500 caracteres")
    private String descripcion;

    @Size(max = 1000, message = "La ciudad debe tener maximo 1000 caracteres")
    private String ciudad;

    @Size(max = 1000, message = "La fecha debe tener maximo 1000 caracteres")
    private String fecha;

    @Size(max = 1000, message = "La hora de inicio debe tener maximo 1000 caracteres")
    private String horaInicio;

    @Size(max = 1000, message = "La hora de fin debe tener maximo 1000 caracteres")
    private String horaFin;

    @Size(max = 1000, message = "El lugar de reunión debe tener maximo 1000 caracteres")
    private String lugarReunion;

    @NotNull(message = "El número de acta no puede ser nulo")
    @NotBlank(message = "El número de acta no puede estar vacío")
    @Size(min = 2, max = 1000, message = "El número de acta debe tener entre 2 y 1000 caracteres")
    private String numeroActa;

    @NotNull(message = "El estado es obligatorio")
    private boolean estado;
}
