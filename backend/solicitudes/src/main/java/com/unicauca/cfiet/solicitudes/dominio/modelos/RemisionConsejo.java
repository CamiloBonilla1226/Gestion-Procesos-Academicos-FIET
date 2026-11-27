package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RemisionConsejo {
    private SolicitudAcademica solicitudAcademica;
    private String uuidSolicitud;
    private LocalDateTime fechaRemision;
}
