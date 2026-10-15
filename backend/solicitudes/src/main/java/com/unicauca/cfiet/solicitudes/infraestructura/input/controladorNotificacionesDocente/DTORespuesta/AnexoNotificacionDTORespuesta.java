package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorNotificacionesDocente.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnexoNotificacionDTORespuesta {
    private String uuidAnexoAcademico;
    private String tipoAnexo;
    private String nombreArchivo;
    private String tipoArchivo;
    private LocalDateTime fechaSubida;
}
