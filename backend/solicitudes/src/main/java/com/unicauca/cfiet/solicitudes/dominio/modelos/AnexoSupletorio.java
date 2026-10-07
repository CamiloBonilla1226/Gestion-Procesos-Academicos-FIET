package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnexoSupletorio {
    private TipoAnexoAcademico tipoAnexo;
    private Long tamanioMaximoBytes;
    @Builder.Default
    private List<CausaSupletorio> causas = new ArrayList<>();
}
