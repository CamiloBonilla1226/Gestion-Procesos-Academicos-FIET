package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuncionarioAcademico {
    private String uuidUsuario;
    private Usuario usuario;
    private String dependencia;
}
