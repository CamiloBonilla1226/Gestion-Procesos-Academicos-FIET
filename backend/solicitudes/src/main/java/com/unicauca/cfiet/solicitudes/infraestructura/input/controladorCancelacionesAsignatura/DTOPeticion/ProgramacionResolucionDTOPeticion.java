package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTOPeticion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProgramacionResolucionDTOPeticion {
    private String fechaProgramada;
    private String numeroResolucion;
    private Integer anioResolucion;
    private String decision;
}
