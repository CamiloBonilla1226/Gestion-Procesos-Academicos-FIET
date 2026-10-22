package com.unicauca.cfiet.solicitudes.infraestructura.input.controladorCancelacionesAsignatura.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionResolucionDTORespuesta {
    private String estado;
    private LocalDateTime fechaProgramada;
    private boolean firmada;
    private String numeroResolucion;
    private Integer anioResolucion;
    private String sufijoResolucion;
    private String decision;
    private String mensajeInicial;
    private String textoComunicacion;
    private String textoDarca;
    private String textoDarcaFirma;
    private LocalDateTime fechaEnvioEstudiante;
    private LocalDateTime fechaEnvioDarca;
    private int intentos;
    private String ultimoError;
    private LocalDateTime fechaEnvio;
    private LocalDateTime fechaLiberacion;
    private boolean editable;
}
