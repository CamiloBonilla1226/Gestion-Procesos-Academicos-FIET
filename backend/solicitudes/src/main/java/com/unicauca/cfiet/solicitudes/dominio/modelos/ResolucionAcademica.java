package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResolucionAcademica {
    private SolicitudAcademica solicitudAcademica;
    private String urlArchivo;
    private String nombreArchivo;
    private LocalDateTime fechaSubida;
    private FuncionarioAcademico funcionarioAcademico;
}
