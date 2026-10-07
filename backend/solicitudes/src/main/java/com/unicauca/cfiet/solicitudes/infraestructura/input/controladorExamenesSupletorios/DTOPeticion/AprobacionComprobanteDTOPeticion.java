package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorExamenesSupletorios.DTOPeticion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AprobacionComprobanteDTOPeticion {
    private String fechaAcordadaExamen;
}
