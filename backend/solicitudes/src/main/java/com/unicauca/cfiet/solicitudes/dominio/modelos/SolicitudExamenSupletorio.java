package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudExamenSupletorio {
    private SolicitudAcademica solicitudAcademica;
    private AsignaturaMatriculada asignaturaMatriculada;
    private LocalDate fechaExamenNoPresentado;
    private CausaSupletorio tipoCausa;
    private CruceSupletorio cruce;
    private Docente docente;
}
