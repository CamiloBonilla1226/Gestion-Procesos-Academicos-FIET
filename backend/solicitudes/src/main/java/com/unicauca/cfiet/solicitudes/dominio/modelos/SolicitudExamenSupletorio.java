package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudExamenSupletorio {
    private SolicitudAcademica solicitudAcademica;
    private AsignaturaMatriculada asignaturaMatriculada;
    private LocalDate fechaExamenNoPresentado;
    private LocalDateTime fechaAcordadaExamen;
    private CausaSupletorio tipoCausa;
    private CruceSupletorio cruce;
}
