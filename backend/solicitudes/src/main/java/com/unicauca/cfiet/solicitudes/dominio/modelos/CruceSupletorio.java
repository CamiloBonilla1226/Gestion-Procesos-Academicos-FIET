package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CruceSupletorio {
    private AsignaturaMatriculada asignaturaMatriculadaCruzada;
    private LocalDate fechaExamenCruzada;
    private String horaExamenCruzada;
}
