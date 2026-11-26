package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilaFormatoOficial {
    private String uuidAsignaturaSolicitud;
    private String codigo;
    private String nombre;
    private String faltas;
    private String nota;
    private String situacionMatricula;
    private String situacionCancelar;
}
