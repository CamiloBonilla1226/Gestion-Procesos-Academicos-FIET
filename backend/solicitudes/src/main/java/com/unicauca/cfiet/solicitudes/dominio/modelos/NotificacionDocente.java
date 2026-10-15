package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionDocente {
    private Long idNotificacion;
    private String uuidNotificacion;
    private String uuidSolicitudAcademica;
    private Docente docente;
    private String titulo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaLectura;
}
