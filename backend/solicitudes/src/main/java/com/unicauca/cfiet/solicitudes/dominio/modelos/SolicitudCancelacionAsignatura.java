package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudCancelacionAsignatura {
    private SolicitudAcademica solicitudAcademica;
    private String motivoCancelacion;
    @Builder.Default
    private List<AsignaturaSolicitudAcademica> asignaturas = new ArrayList<>();
}
