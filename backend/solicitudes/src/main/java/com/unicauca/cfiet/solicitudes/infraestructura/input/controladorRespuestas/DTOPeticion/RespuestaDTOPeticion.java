package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorRespuestas.DTOPeticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * @author Julian David Camacho Erazo  {@literal <jdacamacho@unicauca.edu.co>}
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RespuestaDTOPeticion {
    @NotNull(message = "El tipo de respuesta no puede ser nulo")
    @NotBlank(message = "El tipo de respuesta no puede estar vacío")
    @Size(min = 5, max = 1000, message = "El tipo de respuesta debe tener entre 5 y 1000 caracteres")
    private String tipoRespuesta;

    @NotNull(message = "El consecutivo FIET no puede ser nulo")
    @NotBlank(message = "El consecutivo FIET no puede estar vacío")
    @Size(min = 5, max = 1000, message = "El consecutivo FIET debe tener entre 5 y 1000 caracteres")
    private String consecutivoFiet;

    @NotNull(message = "La respuesta del consejo no puede ser nula")
    @NotBlank(message = "La respuesta del consejo no puede estar vacía")
    @Size(min = 5, max = 5000, message = "La respuesta del consejo debe tener entre 5 y 5000 caracteres")
    private String respuestaConsejo;

    @Size(max = 5000, message = "Las indicaciones no pueden exceder 5000 caracteres")
    private String indicaciones;

}
