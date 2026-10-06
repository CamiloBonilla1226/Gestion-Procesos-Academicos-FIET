package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormularioCancelacionMatricula {
    @Builder.Default
    private List<TipoAnexoAcademico> anexos = new ArrayList<>();
    @Builder.Default
    private List<AsignaturaMatriculada> asignaturas = new ArrayList<>();
}
