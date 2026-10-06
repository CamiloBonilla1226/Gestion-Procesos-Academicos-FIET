package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SoportePermitidoDTORespuesta {
    private String uuidTipoAnexoAcademico;
    private String nombre;
    private String formatosPermitidos;
    private Long tamanioMaximoBytes;
    private Boolean obligatorio;
}
