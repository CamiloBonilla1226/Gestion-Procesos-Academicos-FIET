package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorExamenesSupletorios.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CruceSupletorioDTORespuesta {
    private AsignaturaSupletorioDTORespuesta asignatura;
    private String fechaExamenCruzada;
    private String horaExamenCruzada;
}
