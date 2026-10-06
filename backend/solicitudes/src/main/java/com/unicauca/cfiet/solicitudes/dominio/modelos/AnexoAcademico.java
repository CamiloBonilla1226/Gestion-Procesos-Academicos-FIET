package com.unicauca.cfiet.solicitudes.dominio.modelos;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnexoAcademico {
    private String uuidAnexoAcademico;
    private SolicitudAcademica solicitudAcademica;
    private TipoAnexoAcademico tipoAnexoAcademico;
    private String nombreArchivo;
    private String urlArchivo;
    private String tipoArchivo;
    private Long tamanioBytes;
    private Usuario usuario;
    private LocalDateTime fechaSubida;
}
