package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormularioCancelacionAsignatura {
    private String codigoEstudiantil;
    @Builder.Default
    private List<AsignaturaMatriculada> asignaturas = new ArrayList<>();
    @Builder.Default
    private List<SoportePermitido> soportes = new ArrayList<>();
}
