package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Docente {
    private String uuidUsuario;
    private String nombres;
    private String apellidos;
    private String numeroDocumento;
    private String username;
}
