package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialSolicitudAcademica {
    private String uuidHistorial;
    private SolicitudAcademica solicitudAcademica;
    private Usuario usuario;
    private String accion;
    private String observaciones;
    private LocalDateTime fecha;
}
