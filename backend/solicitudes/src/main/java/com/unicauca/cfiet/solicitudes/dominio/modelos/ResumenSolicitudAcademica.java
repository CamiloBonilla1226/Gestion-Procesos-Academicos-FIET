package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumenSolicitudAcademica {
    private SolicitudAcademica solicitudAcademica;
    private String etiqueta;
    private String etapaCodigo;
    private String numeroDocumento;
    private String decisionDecano;
    private List<ResultadoAsignaturaSolicitud> resultadosAsignaturas;
}
