package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionResolucion {
    private String uuidSolicitudAcademica;
    private LocalDateTime fechaProgramada;
    private EstadoNotificacionResolucion estado;
    private boolean firmada;
    private String numeroResolucion;
    private Integer anioResolucion;
    private String decision;
    private String mensajeInicial;
    private String textoComunicacion;
    private String textoDarca;
    private LocalDateTime fechaEnvioEstudiante;
    private LocalDateTime fechaEnvioDarca;
    private int intentos;
    private String ultimoError;
    private LocalDateTime fechaReclamo;
    private LocalDateTime fechaEnvio;
    private LocalDateTime fechaLiberacion;
    private LocalDateTime fechaCreacion;
    private boolean editable;
    private String sufijoResolucion;
    private String textoDarcaFirma;
}
