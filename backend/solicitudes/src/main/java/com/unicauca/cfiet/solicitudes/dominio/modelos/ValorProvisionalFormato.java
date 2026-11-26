package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValorProvisionalFormato {
    private String uuidAsignaturaSolicitud;
    private String uuidSituacionMatricula;
    private String uuidSituacionCancelar;
    private String faltas;
    private String nota;
    private Boolean aprobada;
}
