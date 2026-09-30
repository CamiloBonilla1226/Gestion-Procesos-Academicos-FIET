package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudes.DTOPeticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
@Setter
@NoArgsConstructor
public class SolicitudDTOPeticion {
    @Size(max = 1000, message = "El consecutivo debe tener maximo 1000 caracteres")
    private String consecutivo;

    @NotNull(message = "El nombre no puede ser nulo")
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 5, max = 1000, message = "El nombre debe tener entre 5 y 1000 caracteres")
    private String nombre;

    @Size(max = 1500, message = "La descripción debe tener maximo 1500 caracteres")
    private String descripcion;

    @NotNull(message = "El tipo de solicitud es obligatorio")
    @NotBlank(message = "El tipo de solicitud no puede estar vacío")
    @Size(min = 5, max = 1000, message = "El tipo de solicitud debe tener entre 5 y 1000 caracteres")
    private String uuidTipoSolicitud;

    private List<AnexoDTOPeticion> anexos;

    @Size(max = 1000, message = "La orden del día debe maximo 1000 caracteres")
    private String uuidOrdenDelDia;
}
