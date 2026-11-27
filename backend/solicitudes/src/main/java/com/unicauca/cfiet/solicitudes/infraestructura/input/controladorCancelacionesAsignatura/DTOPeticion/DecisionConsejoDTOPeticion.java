package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTOPeticion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DecisionConsejoDTOPeticion {
    @Valid
    private List<DecisionAsignaturaDTOPeticion> decisiones;
    @Size(max = 1000, message = "El tipo de respuesta no puede exceder los 1000 caracteres")
    private String tipoRespuesta;
    @Size(max = 1000, message = "El consecutivo no puede exceder los 1000 caracteres")
    private String consecutivoFiet;
    @Size(max = 5000, message = "Las indicaciones no pueden exceder los 5000 caracteres")
    private String indicaciones;
}
