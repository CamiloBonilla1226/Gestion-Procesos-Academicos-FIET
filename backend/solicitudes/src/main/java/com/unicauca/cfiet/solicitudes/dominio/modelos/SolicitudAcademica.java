package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudAcademica {
    private String uuidSolicitudAcademica;
    private String radicado;
    private Estudiante estudiante;
    private TipoSolicitudAcademica tipoSolicitudAcademica;
    private EtapaSolicitudAcademica etapa;
    private LocalDateTime fechaCreacion;
}
