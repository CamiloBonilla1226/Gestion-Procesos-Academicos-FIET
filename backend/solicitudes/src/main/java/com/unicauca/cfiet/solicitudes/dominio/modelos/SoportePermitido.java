package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoportePermitido {
    private String uuidTipoAnexoAcademico;
    private String nombre;
    private String formatosPermitidos;
    private Long tamanioMaximoBytes;
    private Boolean obligatorio;
}
