package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesMatricula.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnexoRequeridoDTORespuesta {
    private String uuidTipoAnexoAcademico;
    private String nombre;
    private String formatosPermitidos;
    private Boolean obligatorio;
}
