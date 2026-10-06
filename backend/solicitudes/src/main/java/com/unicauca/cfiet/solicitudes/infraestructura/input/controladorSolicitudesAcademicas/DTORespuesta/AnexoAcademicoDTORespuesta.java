package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorSolicitudesAcademicas.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnexoAcademicoDTORespuesta {
    private String uuidAnexoAcademico;
    private String nombreArchivo;
    private String uuidTipoAnexoAcademico;
    private String tipoAnexo;
    private String tipoArchivo;
    private Long tamanioBytes;
    private LocalDateTime fechaSubida;
}
