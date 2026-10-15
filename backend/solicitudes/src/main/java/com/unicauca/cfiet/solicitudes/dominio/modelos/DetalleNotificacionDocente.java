package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleNotificacionDocente {
    private NotificacionDocente notificacion;
    private SolicitudAcademica solicitudAcademica;
    private SolicitudExamenSupletorio supletorio;
    @Builder.Default
    private List<AnexoAcademico> anexos = new ArrayList<>();
}
