package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnexoRadicacion {
    private String uuidTipoAnexoAcademico;
    private ArchivoAdjunto archivo;
}
